package com.dgladyshev.deadcodedetector.services;

import com.dgladyshev.deadcodedetector.entity.DeadCodeOccurence;
import com.dgladyshev.deadcodedetector.entity.GitRepo;
import com.dgladyshev.deadcodedetector.entity.Inspection;
import com.dgladyshev.deadcodedetector.entity.InspectionState;
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

    @Async
    public void inspectCode(String id) {
        Inspection inspection = inspectionsRepository.getInspection(id);
        GitRepo gitRepo = inspection.getGitRepo();
        String inspectionDirPath = dataDir + "/" + id;
        try {
            inspection.changeState(InspectionState.DOWNLOADING);
            gitService.downloadRepo(gitRepo, inspectionDirPath); //TODO add ability to switch branch
            inspection.changeState(InspectionState.IN_QUEUE);
            executor.submit(() -> {
                try {
                    inspection.changeState(InspectionState.PROCESSING);
                    analyzeRepo(
                            inspectionDirPath,
                            gitRepo.getName(),
                            gitRepo.getLanguage()
                    );
                    List<DeadCodeOccurence> deadCodeOccurrences = findDeadCodeOccurences(inspectionDirPath);
                    inspection.complete(deadCodeOccurrences);
                } catch (IOException ex) {
                    inspection.fail(ex);
                }
            });
        } catch (GitAPIException | IOException ex) {
            inspection.fail(ex);
        }
    }

    //TODO add javadoc
    //Example: und -db ./db.udb create -languages Java add ./dead-code-detector settings analyze
    public void analyzeRepo(String inspectionDirPath, String repoName, String repoLanguage) throws IOException {
        CommandLineUtils.execProcess(
                getCanonicalPath(scitoolsDir + "/und"),
                "-db", getCanonicalPath(inspectionDirPath + "/db.udb"), "create",
                "-languages", repoLanguage,
                "add", getCanonicalPath(inspectionDirPath + "/" + repoName),
                "settings", "analyze"
        );
    }

    //TODO add javadoc
    //Example: und uperl ./unused.pl -db ./db.udb > results.txt
    public List<DeadCodeOccurence> findDeadCodeOccurences(String inspectionDirPath) throws IOException {
        String outputString = CommandLineUtils.execProcess(
                getCanonicalPath(scitoolsDir + "/und"),
                "uperl", getCanonicalPath("./unused.pl"),
                "-db", getCanonicalPath(inspectionDirPath + "/db.udb")
        );
        return toDeadCodeOccurences(outputString, getCanonicalPath(inspectionDirPath));
    }

    //TODO add javadoc
    private List<DeadCodeOccurence> toDeadCodeOccurences(String outputString, String inspectionCanonicalPath) {
        String[] lines = outputString.split("\\r?\\n");
        return Stream.of(lines)
                .map(line -> {
                    String[] elements = line.split("&");
                    if (!StringUtils.isEmptyOrNull(line) && elements.length > 3) {
                        return new DeadCodeOccurence(
                                elements[0],
                                elements[1],
                                elements[2].replace(inspectionCanonicalPath + "/", ""),
                                elements[3]
                        );
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


