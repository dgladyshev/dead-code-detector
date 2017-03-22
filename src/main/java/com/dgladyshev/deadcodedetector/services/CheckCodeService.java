package com.dgladyshev.deadcodedetector.services;

import com.dgladyshev.deadcodedetector.entity.Check;
import com.dgladyshev.deadcodedetector.entity.CheckStatus;
import com.dgladyshev.deadcodedetector.entity.DeadCodeOccurence;
import com.dgladyshev.deadcodedetector.exceptions.NoSuchCheckException;
import com.dgladyshev.deadcodedetector.util.CommandUtil;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.apache.tomcat.util.http.fileupload.FileUtils;
import org.buildobjects.process.ProcBuilder;
import org.eclipse.jgit.api.Git;
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

	private static final String BASE_BRANCH = "master";

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
		String repoPath = checkPath + "/" + check.getRepoName();
		try {
			Git git = Git.cloneRepository()
					.setURI(check.getRepoUrl())
					.setDirectory(new File(repoPath))
					.setBranch(BASE_BRANCH)
					.setBranchesToClone(Lists.newArrayList(BASE_BRANCH))
					.call();
			git.getRepository().close();
			File dirToDelete = new File(repoPath + "/.git");
			FileUtils.deleteDirectory(dirToDelete);
		} catch (Exception e) {
			check.setCheckStatus(CheckStatus.FAILED);
		}
		executor.submit(() -> {
			check.setCheckStatus(CheckStatus.PROCESSING);
			try {
				createUDB(checkPath, repoPath, check.getRepoLanguage());
			} catch (Exception e) {
				log.error("Error occured for check id {}. Error is {}", checkId, e.getCause());
				check.setCheckStatus(CheckStatus.FAILED);
			}
			try {
				List<DeadCodeOccurence> deadCodeOccurences = analyzeUDB(checkPath);
				check.setDeadCodeOccurences(deadCodeOccurences);
				check.setTimeCheckFinished(System.currentTimeMillis());
				check.setCheckStatus(CheckStatus.COMPLETED);
			} catch (Exception e) {
				log.error("Error occured for check id {}. Error is {}", checkId, e.getCause());
				check.setCheckStatus(CheckStatus.FAILED);
			}
		});
	}

	public Check createCheck(String url, String name, String language) {
		String checkId = java.util.UUID.randomUUID().toString(); //TODO generate unique id
		checks.put(
				checkId,
				Check.builder()
						.checkId(checkId)
						.repoUrl(url)
						.repoName(name)
						.repoLanguage(language)
						.timeAdded(System.currentTimeMillis())
						.checkStatus(CheckStatus.ADDED)
						.build()
		);
		return checks.get(checkId);
	}

	public Map<String, Check> getChecks() {
		return checks;
	}

	public Check getCheckById(String id) throws NoSuchCheckException {
		if (checks.containsKey(id)) {
			return checks.get(id);
		} else {
			throw new NoSuchCheckException();
		}
	}

	//Example: und -db ./db.udb create -languages Java add ./dead-code-detector settings analyze
	private void createUDB(String checkPath, String repoPath, String repoLanguage) throws IOException {
		String checkCanonicalPath = new File(checkPath).getCanonicalPath();
		String sciToolsUndPath = new File(scitoolsDir + "/und").getCanonicalPath();
		String udbPath = checkCanonicalPath + "/db.udb";
		String repoCanonicalPath = new File(repoPath).getCanonicalPath();
		//TODO refactor with ProcBuilde
		String command = new StringBuilder()
				.append(sciToolsUndPath)
				.append(" -db ")
				.append(udbPath)
				.append(" create -languages ")
				.append(repoLanguage)
				.append(" add ")
				.append(repoCanonicalPath)
				.append(" settings analyze")
				.toString();
		int code = CommandUtil.runCommand(command);
		assert code == 0 && new File(udbPath).isFile();
	}

	//Example: und uperl ./unused.pl -db ./db.udb > results.txt
	private List<DeadCodeOccurence> analyzeUDB(String checkPath) throws IOException {
		String sciToolsUndPath = new File(scitoolsDir + "/und").getCanonicalPath();
		String checkCanonicalPath = new File(checkPath).getCanonicalPath();
		String udbPath = checkCanonicalPath + "/db.udb";
		String perlScriptPath = new File(".").getCanonicalPath() + "/unused.pl";
		String output = ProcBuilder.run(sciToolsUndPath, "uperl", perlScriptPath, "-db", udbPath);
		//TODO set bigger timeout
		String lines[] = output.split("\\r?\\n");
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

}


