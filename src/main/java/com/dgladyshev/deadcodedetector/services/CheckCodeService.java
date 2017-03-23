package com.dgladyshev.deadcodedetector.services;

import com.dgladyshev.deadcodedetector.entity.Check;
import com.dgladyshev.deadcodedetector.entity.CheckStatus;
import com.dgladyshev.deadcodedetector.entity.DeadCodeOccurence;
import com.dgladyshev.deadcodedetector.entity.GitRepo;
import com.dgladyshev.deadcodedetector.exceptions.NoSuchCheckException;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.apache.tomcat.util.http.fileupload.FileUtils;
import org.buildobjects.process.ProcBuilder;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
@Service
public class CheckCodeService {

	@Value("${scitools.dir}")
	private String scitoolsDir;

	@Value("${data.dir}")
	private String dataDir;

	private ConcurrentHashMap<String, Check> checks = new ConcurrentHashMap<>();

	private final ExecutorService executor = Executors.newSingleThreadExecutor();

	@Async
	public void checkCode(String checkId) {
		Check check = checks.get(checkId);
		String checkPath = dataDir + "/" + checkId;
		String repoPath = checkPath + "/" + check.getGitRepo().getName();
		try {
			check.setStatus(CheckStatus.PROCESSING);
			check.setStepDescription("Step 1/5. Downloading git repository");
			cloneGitRepo(check, repoPath);
			check.setStepDescription("Step 2/5. Request to analyze repository has been added to a queue");
			executor.submit(() -> {
				try {
					check.setStepDescription("Step 3/5. Analyzing git repository and creating .udb file");
					analyzeRepo(checkPath, repoPath, check.getGitRepo().getLanguage());
					check.setStepDescription("Step 4/5. Searching for dead code occurrences");
					List<DeadCodeOccurence> deadCodeOccurences = findDeadCodeOccurences(checkPath);
					check.setDeadCodeOccurences(deadCodeOccurences);
					check.setTimestampFinished(System.currentTimeMillis());
					check.setTimeSpentMillis(check.getTimestampFinished() - check.getTimestampAdded());
					check.setStepDescription("Step 5/5. Processing completed");
					check.setStatus(CheckStatus.COMPLETED);
				} catch (Exception e) {
					log.error("Error occurred for check id: {}. Error cased by: {}, details: {}", checkId, e.getCause(), e.getMessage());
					check.setStatus(CheckStatus.FAILED);
				}
			});
		} catch (Exception e) {
			log.error("Error occured for check id: {}. Error cased by: {}, details: {}", checkId, e.getCause(), e.getMessage());
			check.setStatus(CheckStatus.FAILED);
		}
	}

	public Check createCheck(GitRepo repo) {
		String checkId = java.util.UUID.randomUUID().toString();
		checks.put(
				checkId,
				Check.builder()
						.checkId(checkId)
						.gitRepo(repo)
						.timestampAdded(System.currentTimeMillis())
						.status(CheckStatus.ADDED)
						.build()
		);
		return checks.get(checkId);
	}

	public Map<String, Check> getChecks() {
		return checks;
	}

	public Check getCheck(String id) throws NoSuchCheckException {
		if (id != null && checks.containsKey(id)) {
			return checks.get(id);
		} else {
			throw new NoSuchCheckException();
		}
	}

	public void deleteCheck(String id) throws NoSuchCheckException {
		if (id != null && checks.containsKey(id)) {
			checks.remove(id);
		} else {
			throw new NoSuchCheckException();
		}
	}

	private void cloneGitRepo(Check check, String repoPath) throws GitAPIException, IOException {
		final String baseBranch = "master";
		Git git = Git.cloneRepository()
				.setURI(check.getGitRepo().getUrl())
				.setDirectory(new File(repoPath))
				.setBranch(baseBranch)
				.setBranchesToClone(Lists.newArrayList(baseBranch))
				.call();
		git.getRepository().close();
		File dirToDelete = new File(repoPath + "/.git");
		FileUtils.deleteDirectory(dirToDelete);
	}

	//Example: und -db ./db.udb create -languages Java add ./dead-code-detector settings analyze
	private void analyzeRepo(String checkPath, String repoPath, String repoLanguage) throws Exception {
		String checkCanonicalPath = new File(checkPath).getCanonicalPath();
		String sciToolsUndPath = new File(scitoolsDir + "/und").getCanonicalPath();
		String udbPath = checkCanonicalPath + "/db.udb";
		String repoCanonicalPath = new File(repoPath).getCanonicalPath();

		execProcess(sciToolsUndPath, "-db", udbPath, "create", "-languages", repoLanguage, "add", repoCanonicalPath, "settings", "analyze");
	}

	//Example: und uperl ./unused.pl -db ./db.udb > results.txt
	private List<DeadCodeOccurence> findDeadCodeOccurences(String checkPath) throws Exception {
		String sciToolsUndPath = new File(scitoolsDir + "/und").getCanonicalPath();
		String checkCanonicalPath = new File(checkPath).getCanonicalPath();
		String udbPath = checkCanonicalPath + "/db.udb";
		String perlScriptPath = new File(".").getCanonicalPath() + "/unused.pl";

		String outputString = execProcess(sciToolsUndPath, "uperl", perlScriptPath, "-db", udbPath);
		return toDeadCodeOccurences(outputString, checkCanonicalPath);
	}

	private List<DeadCodeOccurence> toDeadCodeOccurences(String outputString, String checkCanonicalPath) {
		String lines[] = outputString.split("\\r?\\n");
		return Stream.of(lines)
				.map(line -> {
					String[] elements = line.split("&");
					return new DeadCodeOccurence(
							elements[0],
							elements[1],
							elements[2].replace(checkCanonicalPath + "/", ""),
							elements[3]
					);
				})
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


