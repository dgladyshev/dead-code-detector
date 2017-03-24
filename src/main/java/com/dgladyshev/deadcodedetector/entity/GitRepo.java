package com.dgladyshev.deadcodedetector.entity;

import static org.apache.commons.lang.StringUtils.trimToEmpty;

import com.dgladyshev.deadcodedetector.util.GitHubRepositoryName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@AllArgsConstructor
@Builder
@Data
public class GitRepo {

    private String name;
    private String user;
    private String host;
    private String url;
    private String language;
    private String branch;

    public GitRepo(String url, String language, String branch) {
        GitHubRepositoryName parsedUrl = GitHubRepositoryName.create(url);
        this.name = parsedUrl.getRepositoryName();
        this.user = parsedUrl.getUserName();
        this.host = parsedUrl.getHost();
        this.url = trimToEmpty(url);
        this.language = language;
        this.branch = trimToEmpty(branch);
    }

}
