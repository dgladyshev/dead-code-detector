package com.dgladyshev.deadcodedetector.services;

import com.dgladyshev.deadcodedetector.entity.DeadCodeOccurrence;
import com.dgladyshev.deadcodedetector.entity.GitRepo;
import com.dgladyshev.deadcodedetector.entity.Inspection;
import com.dgladyshev.deadcodedetector.entity.InspectionState;
import com.dgladyshev.deadcodedetector.exceptions.ExecProcessException;
import com.dgladyshev.deadcodedetector.repositories.InspectionsRepository;
import com.dgladyshev.deadcodedetector.util.CommandLineUtils;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.util.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class InspectionService {

    @Value("${scitools.dir}")
    private String scitoolsDir;

    @Value("${data.dir}")
    private String dataDir;

    private final GitService gitService;
    private final InspectionsRepository inspectionsRepository;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    @Autowired
    public InspectionService(GitService gitService, InspectionsRepository inspectionsRepository) {
        this.gitService = gitService;
        this.inspectionsRepository = inspectionsRepository;
    }

    /**
     * Downloads git repository for given Inspection entity, creates .udb file and then searches for problems in code.
     * Returns nothings but changes state of Inspection entity on each step of processing.
     *
     * @param id unique inspection id
     */
    @Async
    public void inspectCode(String id) {
        Inspection inspection = inspectionsRepository.getInspection(id);
        GitRepo gitRepo = inspection.getGitRepo();
        String inspectionDirPath = dataDir + "/" + id;
        try {
            inspection.changeState(InspectionState.DOWNLOADING);
            gitService.downloadRepo(gitRepo, inspectionDirPath);
            inspection.changeState(InspectionState.IN_QUEUE);
            executor.submit(() -> {
                try {
                    inspection.changeState(InspectionState.PROCESSING);
                    analyzeRepo(
                            inspectionDirPath,
                            gitRepo.getName(),
                            gitRepo.getLanguage()
                    );
                    List<DeadCodeOccurrence> deadCodeOccurrences = findDeadCodeOccurrences(inspectionDirPath);
                    inspection.complete(deadCodeOccurrences);
                } catch (IOException | ExecProcessException ex) {
                    inspection.fail(ex);
                }
            });
        } catch (GitAPIException | IOException ex) {
            inspection.fail(ex);
        }
    }

    /**
     * Executes shell command in order to create db.udb file which could be analyzed in order to find problems in code
     * Example of generated command: und -db ./db.udb create -languages Java add ./dead-code-detector settings analyze
     *
     * @param inspectionDirPath path to the inspection directory which must contain repository subdirectory
     * @param repoName          name of repository to be analyzed
     * @param repoLanguage      programming language of the repository
     * @throws IOException          if some paths cannot be converted to canonical form
     * @throws ExecProcessException if shell command failed to be executed or return non-zero error code
     */
    private void analyzeRepo(String inspectionDirPath, String repoName, String repoLanguage)
            throws IOException, ExecProcessException {
        CommandLineUtils.execProcess(
                getCanonicalPath(scitoolsDir + "/und"),
                "-db", getCanonicalPath(inspectionDirPath + "/db.udb"), "create",
                "-languages", repoLanguage,
                "add", getCanonicalPath(inspectionDirPath + "/" + repoName),
                "settings", "analyze"
        );
    }

    /**
     * Executes shell command in order to analyze existing db.udb with unused.pl Perl script
     * Example of generated command: und uperl ./unused.pl -db ./db.udb > results.txt
     *
     * @param inspectionDirPath path to the inspection directory which must contain repository subdirectory
     * @throws IOException          if some paths cannot be converted to canonical form
     * @throws ExecProcessException if shell command failed to be executed or return non-zero error code
     */
    private List<DeadCodeOccurrence> findDeadCodeOccurrences(String inspectionDirPath)
            throws IOException, ExecProcessException {
        String sciptOutput = CommandLineUtils.execProcess(
                getCanonicalPath(scitoolsDir + "/und"),
                "uperl", getCanonicalPath("./unused.pl"),
                "-db", getCanonicalPath(inspectionDirPath + "/db.udb")
        );
        return toDeadCodeOccurrences(sciptOutput, getCanonicalPath(inspectionDirPath));
    }

    /**
     * Parses output of unused.pl Perl script and converts it to the list of dead code occurrences
     *
     * @param scriptOutput            path to the inspection directory which must contain repository subdirectory
     * @param inspectionCanonicalPath canonical path to the inspection directory
     * @return the list of dead code occurrences
     */
    private List<DeadCodeOccurrence> toDeadCodeOccurrences(String scriptOutput, String inspectionCanonicalPath) {
        String[] lines = scriptOutput.split("\\r?\\n");
        return Stream.of(lines)
                .map(line -> {
                    String[] elements = line.split("&");
                    if (!StringUtils.isEmptyOrNull(line) && elements.length > 3) {
                        return DeadCodeOccurrence.builder()
                                .type(elements[0])
                                .name(elements[1])
                                .file(elements[2].replace(inspectionCanonicalPath + "/", ""))
                                .line(elements[3])
                                .column(elements[4])
                        .build();
                    } else {
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    private static String getCanonicalPath(String relativePath) throws IOException {
        return new File(relativePath).getCanonicalPath();
    }

}


