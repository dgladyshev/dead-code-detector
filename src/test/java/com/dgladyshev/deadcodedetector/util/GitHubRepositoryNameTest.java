package com.dgladyshev.deadcodedetector.util;

import static com.dgladyshev.deadcodedetector.util.GitHubRepoNameMatchers.repo;
import static com.dgladyshev.deadcodedetector.util.GitHubRepoNameMatchers.withHost;
import static com.dgladyshev.deadcodedetector.util.GitHubRepoNameMatchers.withRepoName;
import static com.dgladyshev.deadcodedetector.util.GitHubRepoNameMatchers.withUserName;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;

import com.dgladyshev.deadcodedetector.exceptions.MalformedRequestException;
import com.tngtech.java.junit.dataprovider.DataProvider;
import com.tngtech.java.junit.dataprovider.DataProviderRunner;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Unit tests of {@link GitHubRepositoryName}
 * Original source code is here https://github.com/jenkinsci/github-plugin
 * I modified it in a way that suited my needs.
 **/

@RunWith(DataProviderRunner.class)
@SuppressWarnings("PMD.UseObjectForClearerAPI")
public class GitHubRepositoryNameTest {

    @Test
    @DataProvider({
            "https://user@github.com/jenkinsci/jenkins.git, github.com, jenkinsci, jenkins",
            "https://user@github.com/jenkinsci/jenkins, github.com, jenkinsci, jenkins",
            "https://user@github.com/jenkinsci/jenkins/, github.com, jenkinsci, jenkins",
            "https://employee@gh.company.com/jenkinsci/jenkins.git, gh.company.com, jenkinsci, jenkins",
            "https://employee@gh.company.com/jenkinsci/jenkins, gh.company.com, jenkinsci, jenkins",
            "https://employee@gh.company.com/jenkinsci/jenkins/, gh.company.com, jenkinsci, jenkins",
            "https://github.com/jenkinsci/jenkins.git, github.com, jenkinsci, jenkins",
            "https://github.com/jenkinsci/jenkins, github.com, jenkinsci, jenkins",
            "https://github.com/jenkinsci/jenkins/, github.com, jenkinsci, jenkins",
    })
    public void githubFullRepo(String url, String host, String user, String repo) {
        assertThat(url, repo(allOf(
                withHost(host),
                withUserName(user),
                withRepoName(repo)
        )));
    }

    @Test
    public void trimWhitespace() {
        assertThat("               https://user@github.com/jenkinsci/jenkins/      ", repo(allOf(
                withHost("github.com"),
                withUserName("jenkinsci"),
                withRepoName("jenkins")
        )));
    }

    @Test(expected = MalformedRequestException.class)
    @DataProvider(value = {
            "gopher://gopher.floodgap.com",
            "https//github.com/jenkinsci/jenkins",
            "",
            "null"
            }, trimValues = false)
    public void badUrl(String url) {
        assertThat(url, repo(nullValue(GitHubRepositoryName.class)));
    }


}