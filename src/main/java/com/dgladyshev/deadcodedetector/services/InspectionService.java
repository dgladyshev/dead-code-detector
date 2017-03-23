package com.dgladyshev.deadcodedetector.services;

import com.dgladyshev.deadcodedetector.entity.DeadCodeOccurence;
import com.dgladyshev.deadcodedetector.entity.GitRepo;
import com.dgladyshev.deadcodedetector.entity.Inspection;
import com.dgladyshev.deadcodedetector.entity.InspectionStatus;
import com.dgladyshev.deadcodedetector.exceptions.NoSuchInspectionException;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.apache.tomcat.util.http.fileupload.FileUtils;
import org.buildobjects.process.ProcBuilder;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.util.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
@Service
public class InspectionService {

	@Value("${scitools.dir}")
	private String scitoolsDir;

	@Value("${data.dir}")
	private String dataDir;

	private ConcurrentHashMap<String, Inspection> inspections = new ConcurrentHashMap<>();

	private final ExecutorService executor = Executors.newSingleThreadExecutor();

	@Async
	public void inspectCode(String id) {
		Inspection inspection = inspections.get(id);
		String inspectionPath = dataDir + "/" + id;
		String repoPath = inspectionPath + "/" + inspection.getGitRepo().getName();
		try {
			inspection.setStatus(InspectionStatus.PROCESSING);
			inspection.setStepDescription("Step 1/5. Downloading git repository");
			cloneGitRepo(inspection, repoPath);
			inspection.setStepDescription("Step 2/5. Request to analyze repository has been added to a queue");
			executor.submit(() -> {
				try {
					inspection.setStepDescription("Step 3/5. Analyzing git repository and creating .udb file");
					analyzeRepo(inspectionPath, repoPath, inspection.getGitRepo().getLanguage());
					inspection.setStepDescription("Step 4/5. Searching for dead code occurrences");
					List<DeadCodeOccurence> deadCodeOccurences = findDeadCodeOccurences(inspectionPath);
					inspection.setDeadCodeOccurrences(deadCodeOccurences);
					inspection.setTimestampFinished(System.currentTimeMillis());
					inspection.setTimeSpentMillis(inspection.getTimestampFinished() - inspection.getTimestampAdded());
					inspection.setStepDescription("Step 5/5. Processing completed");
					inspection.setStatus(InspectionStatus.COMPLETED);
				} catch (Exception e) {
					log.error("Error occurred for inspection id: {}. Error cased by: {}, details: {}", id, e.getCause(), e.getMessage());
					inspection.setStatus(InspectionStatus.FAILED);
				}
			});
		} catch (Exception e) {
			log.error("Error occured for inspection id: {}. Error cased by: {}, details: {}", id, e.getCause(), e.getMessage());
			inspection.setStatus(InspectionStatus.FAILED);
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
			throw new NoSuchInspectionException();
		}
	}

	public void deleteInspection(String id) throws NoSuchInspectionException {
		if (id != null && inspections.containsKey(id)) {
			inspections.remove(id);
		} else {
			throw new NoSuchInspectionException();
		}
	}

	private void cloneGitRepo(Inspection inspection, String repoPath) throws GitAPIException, IOException {
		final String baseBranch = "master";
		Git git = Git.cloneRepository()
				.setURI(inspection.getGitRepo().getUrl())
				.setDirectory(new File(repoPath))
				.setBranch(baseBranch)
				.setBranchesToClone(Lists.newArrayList(baseBranch))
				.call();
		git.getRepository().close();
		File dirToDelete = new File(repoPath + "/.git");
		FileUtils.deleteDirectory(dirToDelete);
	}

	//Example: und -db ./db.udb create -languages Java add ./dead-code-detector settings analyze
	private void analyzeRepo(String inspectionPath, String repoPath, String repoLanguage) throws Exception {
		String inspectionCanonicalPath = new File(inspectionPath).getCanonicalPath();
		String sciToolsUndPath = new File(scitoolsDir + "/und").getCanonicalPath();
		String udbPath = inspectionCanonicalPath + "/db.udb";
		String repoCanonicalPath = new File(repoPath).getCanonicalPath();

		execProcess(sciToolsUndPath, "-db", udbPath, "create", "-languages", repoLanguage, "add", repoCanonicalPath, "settings", "analyze");
	}

	//Example: und uperl ./unused.pl -db ./db.udb > results.txt
	private List<DeadCodeOccurence> findDeadCodeOccurences(String inspectionPath) throws Exception {
		String sciToolsUndPath = new File(scitoolsDir + "/und").getCanonicalPath();
		String inspectionCanonicalPath = new File(inspectionPath).getCanonicalPath();
		String udbPath = inspectionCanonicalPath + "/db.udb";
		String perlScriptPath = new File(".").getCanonicalPath() + "/unused.pl";

		String outputString = execProcess(sciToolsUndPath, "uperl", perlScriptPath, "-db", udbPath);
		return toDeadCodeOccurences(outputString, inspectionCanonicalPath);
	}

	private List<DeadCodeOccurence> toDeadCodeOccurences(String outputString, String inspectionCanonicalPath) {
		String lines[] = outputString.split("\\r?\\n");
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


