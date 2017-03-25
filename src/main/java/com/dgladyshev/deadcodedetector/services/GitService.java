package com.dgladyshev.deadcodedetector.services;

import com.dgladyshev.deadcodedetector.entity.GitRepo;
import com.dgladyshev.deadcodedetector.exceptions.NoSuchGitBranchException;
import com.google.common.collect.Lists;
import java.io.File;
import java.io.IOException;
import lombok.extern.slf4j.Slf4j;
import org.apache.tomcat.util.http.fileupload.FileUtils;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.TransportException;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class GitService {

    /**
     * Downloads any public git repository to given path.
     *
     * @param gitRepo          GitRepo entity which contains git url, branch and other related information
     * @param inspectionDirPath path to the inspection directory in which subdirectory with repository should be created
     *
     * @throws IOException          if input/output exceptions occur
     * @throws GitAPIException      if fails to download repository
     * @throws NoSuchGitBranchException if there is no specified branch in the repository
     */
    public void downloadRepo(GitRepo gitRepo, String inspectionDirPath) throws GitAPIException, IOException {
        String repoPath = inspectionDirPath + "/" + gitRepo.getName();
        String branch = gitRepo.getBranch();
        String url = gitRepo.getUrl();
        try {
            cloneRepo(url, repoPath, branch);
        } catch (TransportException ex) {
            //TODO remove this when JGit will support HTTP 301 redirects
            //Issue: https://bugs.eclipse.org/bugs/show_bug.cgi?id=465167
            FileUtils.deleteDirectory(new File(repoPath));
            if (ex.getMessage().contains(": 301 Moved Permanently")) {
                cloneRepo("https" + url.substring(4), repoPath, branch);
            }
        }
        File dirToDelete = new File(repoPath + "/.git");
        FileUtils.deleteDirectory(dirToDelete);
        File repoDir = new File(repoPath);
        String[] elementsInRepoDir = repoDir.list();
        if (repoDir.isDirectory() && elementsInRepoDir != null && elementsInRepoDir.length == 0) {
            throw new NoSuchGitBranchException(
                    String.format("There is no such branch as: %s. Repository URL: %s", branch, url)
            );
        }
    }

    private void cloneRepo(String repoUrl, String repoPath, String baseBranch) throws GitAPIException {
        Git git = Git.cloneRepository()
                .setURI(repoUrl)
                .setDirectory(new File(repoPath))
                .setBranch(baseBranch)
                .setBranchesToClone(Lists.newArrayList(baseBranch))
                .call();
        git.getRepository().close();
    }

}
