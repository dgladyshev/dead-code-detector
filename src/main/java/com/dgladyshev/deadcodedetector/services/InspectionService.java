package com.dgladyshev.deadcodedetector.services;

import com.dgladyshev.deadcodedetector.entity.DeadCodeOccurence;
import com.dgladyshev.deadcodedetector.entity.GitRepo;
import com.dgladyshev.deadcodedetector.entity.Inspection;
import com.dgladyshev.deadcodedetector.entity.InspectionStatus;
import com.dgladyshev.deadcodedetector.exceptions.NoSuchInspectionException;
import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.extern.slf4j.Slf4j;
import org.buildobjects.process.ProcBuilder;
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

    @Value("${max.stored.inspections}")
    private int maxStoredInspections;

    @Autowired
    private GitService gitService;

    private final Map<String, Inspection> inspections = Collections.synchronizedMap(
            new LinkedHashMap<String, Inspection>() {
                @Override
                protected boolean removeEldestEntry(final Map.Entry eldest) {
                    return size() > maxStoredInspections;
                }
            }
    );

    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    @Async
    public void inspectCode(String id) {
        Inspection inspection = inspections.get(id);
        String inspectionPath = dataDir + "/" + id;
        String repoPath = inspectionPath + "/" + inspection.getGitRepo().getName();
        try {
            inspection.startProcessing();
            gitService.downloadRepo(inspection.getGitRepo().getUrl(), repoPath);
            inspection.repoDownloaded();
            executor.submit(() -> {
                try {
                    inspection.analyzeRepository();
                    analyzeRepo(inspectionPath, repoPath, inspection.getGitRepo().getLanguage());
                    inspection.inspectRepository();
                    List<DeadCodeOccurence> deadCodeOccurrences = findDeadCodeOccurences(inspectionPath);
                    inspection.completeInspection(deadCodeOccurrences);
                } catch (IOException ex) {
                    inspection.fail(ex);
                }
            });
        } catch (GitAPIException | IOException ex) {
            inspection.fail(ex);
        }
    }

    public Inspection createInspection(GitRepo repo) {
        String id = java.util.UUID.randomUUID().toString();
        inspections.put(
                id,
                Inspection.builder()
                        .inspectionId(id)
                        .gitRepo(repo)
                        .timestampAdded(System.currentTimeMillis())
                        .status(InspectionStatus.ADDED)
                        .build()
        );
        return inspections.get(id);
    }

    public Map<String, Inspection> getInspections() {
        return inspections;
    }

    public Inspection getInspection(String id) throws NoSuchInspectionException {
        if (id != null && inspections.containsKey(id)) {
            return inspections.get(id);
        } else {
            throw new NoSuchInspectionException("There is no record of any inspection with specified id");
        }
    }

    public void deleteInspection(String id) throws NoSuchInspectionException {
        if (id != null && inspections.containsKey(id)) {
            inspections.remove(id);
        } else {
            throw new NoSuchInspectionException("Cannot delete inspection because there is none with such id");
        }
    }


    //Example: und -db ./db.udb create -languages Java add ./dead-code-detector settings analyze
    private void analyzeRepo(String inspectionPath, String repoPath, String repoLanguage) throws IOException {
        //TODO cleanup
        String inspectionCanonicalPath = new File(inspectionPath).getCanonicalPath();
        String sciToolsUndPath = new File(scitoolsDir + "/und").getCanonicalPath();
        String udbPath = inspectionCanonicalPath + "/db.udb";
        String repoCanonicalPath = new File(repoPath).getCanonicalPath();

        execProcess(sciToolsUndPath, "-db", udbPath, "create", "-languages", repoLanguage, "add", repoCanonicalPath,
                    "settings", "analyze");
    }

    //Example: und uperl ./unused.pl -db ./db.udb > results.txt
    private List<DeadCodeOccurence> findDeadCodeOccurences(String inspectionPath) throws IOException {
        //TODO cleanup
        String sciToolsUndPath = new File(scitoolsDir + "/und").getCanonicalPath();
        String inspectionCanonicalPath = new File(inspectionPath).getCanonicalPath();
        String udbPath = inspectionCanonicalPath + "/db.udb";
        String perlScriptPath = new File(".").getCanonicalPath() + "/unused.pl";

        String outputString = execProcess(sciToolsUndPath, "uperl", perlScriptPath, "-db", udbPath);
        return toDeadCodeOccurences(outputString, inspectionCanonicalPath);
    }

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

    private String execProcess(String cmd, String... args) {
        return new ProcBuilder(cmd)
                .withArgs(args)
                .withTimeoutMillis(15000)
                .withExpectedExitStatuses(0)
                .run()
                .getOutputString();
    }
}


