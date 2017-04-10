package com.dgladyshev.deadcodedetector.services;

import static com.dgladyshev.deadcodedetector.util.FileSystemUtils.checkFileContainsString;
import static com.dgladyshev.deadcodedetector.util.FileSystemUtils.deleteDirectoryIfExists;
import static com.dgladyshev.deadcodedetector.util.FileSystemUtils.getCanonicalPath;

import com.dgladyshev.deadcodedetector.entities.AntiPatternCodeOccurrence;
import com.dgladyshev.deadcodedetector.entities.GitRepo;
import com.dgladyshev.deadcodedetector.entities.Inspection;
import com.dgladyshev.deadcodedetector.entities.InspectionState;
import com.dgladyshev.deadcodedetector.exceptions.ExecProcessException;
import com.dgladyshev.deadcodedetector.services.rules.MatchRuleFunction;
import com.dgladyshev.deadcodedetector.util.CommandLineUtils;
import com.scitools.understand.Database;
import com.scitools.understand.Understand;
import com.scitools.understand.UnderstandException;
import java.io.IOException;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@SuppressWarnings("PMD.CommentSize")
public class CodeAnalyzerService {

    @Autowired
    private Set<MatchRuleFunction> matchRules;

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

    //TODO use not embedded mongo or remove this code
    /*@PostConstruct
    void checkPossiblyStuckInspections() {
        Set<Inspection> notCompleted = inspectionsRepository.findAllByStateNotContains(InspectionState.COMPLETED);
        Set<Inspection> failed = inspectionsRepository.findAllByStateContains(InspectionState.FAILED);
        notCompleted.removeAll(failed);
        notCompleted
                .stream()
                .filter(inspection ->
                        //TODO move constant to settings
                        System.currentTimeMillis() - inspection.getTimestampInspectionCreated() > 10 * MILLIS_PER_MINUTE
                )
                .forEach(inspection -> codeAnalyzerService.inspectCode(inspection));
    }*/

    /**
     * Downloads git repository for given Inspection entities, creates .udb file and then searches for problems in code.
     * Returns nothings but changes state of Inspection entities on each step of processing.
     *
     * @param inspection entity
     */
    @Async
    public void inspectCode(Inspection inspection) {
        GitRepo gitRepo = inspection.getGitRepo();
        String inspectionDirPath = dataDir + "/" + inspection.getId();
        deleteDirectoryIfExists(inspectionDirPath);
        try {
            inspectionStateMachine.changeState(inspection, InspectionState.DOWNLOADING);
            gitService.downloadRepo(gitRepo, inspectionDirPath, inspection.getBranch());
            inspectionStateMachine.changeState(inspection, InspectionState.IN_QUEUE);
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
                } catch (IOException | ExecProcessException | UnderstandException | UnsatisfiedLinkError ex) {
                    inspectionStateMachine.fail(inspection, ex);
                }
            });
        } catch (GitAPIException | IOException ex) {
            inspectionStateMachine.fail(inspection, ex);
        }
    }

    //TODO refactor
    private List<AntiPatternCodeOccurrence> postProcessCodeOccurrences(
            List<AntiPatternCodeOccurrence> antiPatternCodeOccurrences) {
        return antiPatternCodeOccurrences
                .stream()
                .filter(occurrence -> !("Parameter".equalsIgnoreCase(occurrence.getType())
                        && checkFileContainsString(occurrence.getFile(), "abstract class")))
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

    private List<AntiPatternCodeOccurrence> findAntiPatterns(String inspectionDirPath)
            throws UnderstandException, IOException {
        Database database = Understand.open(getCanonicalPath(inspectionDirPath + "/db.udb"));
        return matchRules.stream()
                //.filter() //TODO add filtering by anti-pattern type if needed
                .map(rule -> rule.findMatches(database))
                .flatMap(List::stream)
                .collect(Collectors.toList());
    }

}