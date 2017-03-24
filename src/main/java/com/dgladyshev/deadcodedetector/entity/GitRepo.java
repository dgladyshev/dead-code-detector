package com.dgladyshev.deadcodedetector.entity;

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

    public GitRepo(String url, String language) {
        GitHubRepositoryName parsedUrl = GitHubRepositoryName.create(url);
        this.host = parsedUrl.getHost();
        this.user = parsedUrl.getUserName();
        this.name = parsedUrl.getRepositoryName();
        this.url = url;
        this.language = language;
    }

}
