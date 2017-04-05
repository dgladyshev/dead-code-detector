package com.dgladyshev.deadcodedetector.services;

import static com.dgladyshev.deadcodedetector.util.FileSystemUtils.deleteDirectoryIfExists;

import com.dgladyshev.deadcodedetector.entities.AntiPatternCodeOccurrence;
import com.dgladyshev.deadcodedetector.entities.GitRepo;
import com.dgladyshev.deadcodedetector.entities.Inspection;
import com.dgladyshev.deadcodedetector.entities.InspectionState;
import com.dgladyshev.deadcodedetector.exceptions.ExecProcessException;
import com.dgladyshev.deadcodedetector.util.CommandLineUtils;
import com.google.common.collect.Sets;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.util.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class CodeAnalyzerService {

    private static final Set<String> ALLOWED_INSPECTION_TYPES = Sets.newHashSet(
            "Parameter",
            "Private Method",
            "Private Static Generic Method",
            "Private Static Method",
            "Variable",
            "Private Variable"
    );

    @Value("${scitools.dir}")
    private String scitoolsDir;

    @Value("${data.dir}")
    private String dataDir;

    @Value("${command.line.timeout}")
    private long timeout;

    private final GitService gitService;
    private final InspectionStateMachine inspectionStateMachine;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    @Autowired
    public CodeAnalyzerService(GitService gitService,
                               InspectionStateMachine inspectionStateMachine) {
        this.gitService = gitService;
        this.inspectionStateMachine = inspectionStateMachine;
    }

    /**
     * Downloads git repository for given Inspection entities, creates .udb file and then searches for problems in code.
     * Returns nothings but changes state of Inspection entities on each step of processing.
     *
     * @param inspection inspection entity
     */
    @Async
    public Inspection inspectCode(Inspection inspection) {
        GitRepo gitRepo = inspection.getGitRepo();
        String inspectionDirPath = dataDir + "/" + inspection.getId();
        deleteDirectoryIfExists(inspectionDirPath);
        try {
            inspectionStateMachine.changeState(inspection, InspectionState.DOWNLOADING);
            gitService.downloadRepo(gitRepo, inspectionDirPath, inspection.getBranch(), inspection.getUrl());
            inspectionStateMachine.changeState(inspection, InspectionState.IN_QUEUE);
            CompletableFuture<Inspection> future = new CompletableFuture<>();
            executor.submit(() -> {
                try {
                    inspectionStateMachine.changeState(inspection, InspectionState.PROCESSING);
                    analyzeRepo(
                            inspectionDirPath,
                            gitRepo.getName(),
                            inspection.getLanguage()
                    );
                    List<AntiPatternCodeOccurrence> codeOccurrences = findAntiPatterns(inspectionDirPath);
                    inspectionStateMachine.complete(inspection, postProcessCodeOccurrences(codeOccurrences));
                } catch (IOException | ExecProcessException ex) {
                    inspectionStateMachine.fail(inspection, ex);
                } finally {
                    future.complete(inspection);
                }
            });
            return future.get();
        } catch (GitAPIException | IOException | InterruptedException | ExecutionException ex) {
            inspectionStateMachine.fail(inspection, ex);
            return inspection;
        }
    }

    //TODO refactor
    private List<AntiPatternCodeOccurrence> postProcessCodeOccurrences(
            List<AntiPatternCodeOccurrence> antiPatternCodeOccurrences) {
        return antiPatternCodeOccurrences
                .stream()
                .filter(occurrence -> !occurrence.getName().contains("lambda"))
                .filter(occurrence -> !occurrence.getName().contains(".valueOf.s"))
                .collect(Collectors.toList());
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
                timeout,
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
    private List<AntiPatternCodeOccurrence> findAntiPatterns(String inspectionDirPath)
            throws IOException, ExecProcessException {
        String sciptOutput = CommandLineUtils.execProcess(
                getCanonicalPath(scitoolsDir + "/und"),
                timeout,
                "uperl", getCanonicalPath("./unused.pl"),
                "-db", getCanonicalPath(inspectionDirPath + "/db.udb")
        );
        return toAntiPatternCodeOccurrences(sciptOutput, getCanonicalPath(inspectionDirPath));
    }

    /**
     * Parses output of unused.pl Perl script and converts it to the list of dead code occurrences
     *
     * @param scriptOutput            path to the inspection directory which must contain repository subdirectory
     * @param inspectionCanonicalPath canonical path to the inspection directory
     * @return the list of dead code occurrences
     */
    private List<AntiPatternCodeOccurrence> toAntiPatternCodeOccurrences(
            String scriptOutput, String inspectionCanonicalPath) {
        String[] lines = scriptOutput.split("\\r?\\n");
        return Stream.of(lines)
                .map(line -> {
                    String[] elements = line.split("&");
                    //Note: SciTools can't process lambda correctly and generate false positives
                    //It also produces non-existent occurrence for "valueOf" method in enums
                    if (
                            !StringUtils.isEmptyOrNull(line)
                                    && elements.length > 3
                                    //all further checks are custom hacks
                                    && ALLOWED_INSPECTION_TYPES.contains(elements[0])
                                    && !elements[1].contains("lambda")
                                    && !elements[1].contains(".valueOf.s")
                                    && !(elements[0].equalsIgnoreCase("Parameter")
                                    && checkFileContainsString(elements[2], "abstract class"))) {
                        return AntiPatternCodeOccurrence.builder()
                                .type(elements[0])
                                .name(elements[1])
                                .file(elements[2].replace(inspectionCanonicalPath + "/", ""))
                                .line(Integer.valueOf(elements[3]))
                                .column(Integer.valueOf(elements[4]))
                                .build();
                    } else {
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    //Return false if check fails because of IOException
    private boolean checkFileContainsString(String filePath, String substring) {
        try {
            return FileUtils
                    .readFileToString(new File(filePath), Charset.defaultCharset())
                    .contains(substring);
        } catch (IOException ex) {
            log.error("Failed to read contents of the file {} because of exception {}", filePath, ex);
            return false;
        }
    }

    private static String getCanonicalPath(String relativePath) throws IOException {
        return new File(relativePath).getCanonicalPath();
    }

}