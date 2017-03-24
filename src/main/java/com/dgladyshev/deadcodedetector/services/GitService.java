package com.dgladyshev.deadcodedetector.services;

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

    public void downloadRepo(String repoUrl, String repoPath) throws GitAPIException, IOException {
        //TODO add ability to choose branch
        final String baseBranch = "master";
        try {
            cloneRepo(repoUrl, repoPath, baseBranch);
        } catch (TransportException ex) {
            //TODO remove this when JGit will support HTTP 301 redirects
            //BugTracker link: https://bugs.eclipse.org/bugs/show_bug.cgi?id=465167
            //try to clone repository by replacing http to https in the url if HTTP 301 redirect happened
            FileUtils.deleteDirectory(new File(repoPath));
            if (ex.getMessage().contains(": 301 Moved Permanently")) {
                cloneRepo("https" + repoUrl.substring(4), repoPath, baseBranch);
            }
        }
        File dirToDelete = new File(repoPath + "/.git");
        FileUtils.deleteDirectory(dirToDelete);
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
