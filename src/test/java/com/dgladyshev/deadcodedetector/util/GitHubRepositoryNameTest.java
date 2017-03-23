package com.dgladyshev.deadcodedetector.util;

import com.dgladyshev.deadcodedetector.exceptions.MalformedRepositoryURL;
import com.tngtech.java.junit.dataprovider.DataProvider;
import com.tngtech.java.junit.dataprovider.DataProviderRunner;
import org.junit.Test;
import org.junit.runner.RunWith;

import static com.dgladyshev.deadcodedetector.util.GitHubRepoNameMatchers.*;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

/** 
 * Unit tests of {@link GitHubRepositoryName}
 * Original source code is from from https://github.com/jenkinsci/github-plugin 
 * I modified it in a way that suited my needs. 
 **/

@RunWith(DataProviderRunner.class)
public class GitHubRepositoryNameTest {

	@Test
	@DataProvider({
			"git@github.com:jenkinsci/jenkins.git, github.com, jenkinsci, jenkins",
			"git@github.com:jenkinsci/jenkins/, github.com, jenkinsci, jenkins",
			"git@github.com:jenkinsci/jenkins, github.com, jenkinsci, jenkins",
			"git@gh.company.com:jenkinsci/jenkins.git, gh.company.com, jenkinsci, jenkins",
			"git@gh.company.com:jenkinsci/jenkins, gh.company.com, jenkinsci, jenkins",
			"git@gh.company.com:jenkinsci/jenkins/, gh.company.com, jenkinsci, jenkins",
			"git://github.com/jenkinsci/jenkins.git, github.com, jenkinsci, jenkins",
			"git://github.com/jenkinsci/jenkins/, github.com, jenkinsci, jenkins",
			"git://github.com/jenkinsci/jenkins, github.com, jenkinsci, jenkins",
			"https://user@github.com/jenkinsci/jenkins.git, github.com, jenkinsci, jenkins",
			"https://user@github.com/jenkinsci/jenkins, github.com, jenkinsci, jenkins",
			"https://user@github.com/jenkinsci/jenkins/, github.com, jenkinsci, jenkins",
			"https://employee@gh.company.com/jenkinsci/jenkins.git, gh.company.com, jenkinsci, jenkins",
			"https://employee@gh.company.com/jenkinsci/jenkins, gh.company.com, jenkinsci, jenkins",
			"https://employee@gh.company.com/jenkinsci/jenkins/, gh.company.com, jenkinsci, jenkins",
			"git://company.net/jenkinsci/jenkins.git, company.net, jenkinsci, jenkins",
			"git://company.net/jenkinsci/jenkins, company.net, jenkinsci, jenkins",
			"git://company.net/jenkinsci/jenkins/, company.net, jenkinsci, jenkins",
			"https://github.com/jenkinsci/jenkins.git, github.com, jenkinsci, jenkins",
			"https://github.com/jenkinsci/jenkins, github.com, jenkinsci, jenkins",
			"https://github.com/jenkinsci/jenkins/, github.com, jenkinsci, jenkins",
			"ssh://git@github.com/jenkinsci/jenkins.git, github.com, jenkinsci, jenkins",
			"ssh://git@github.com/jenkinsci/jenkins, github.com, jenkinsci, jenkins",
			"ssh://git@github.com/jenkinsci/jenkins/, github.com, jenkinsci, jenkins",
			"ssh://github.com/jenkinsci/jenkins.git, github.com, jenkinsci, jenkins",
			"ssh://github.com/jenkinsci/jenkins, github.com, jenkinsci, jenkins",
			"ssh://github.com/jenkinsci/jenkins/, github.com, jenkinsci, jenkins",
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

	@Test(expected=MalformedRepositoryURL.class)
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