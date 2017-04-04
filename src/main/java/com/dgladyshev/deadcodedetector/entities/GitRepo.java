package com.dgladyshev.deadcodedetector.entities;

import com.dgladyshev.deadcodedetector.util.GitHubRepositoryName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class GitRepo {

    private String name;
    private String user;
    private String host;

    public GitRepo(String url) {
        GitHubRepositoryName parsedUrl = GitHubRepositoryName.create(url);
        this.name = parsedUrl.getRepositoryName();
        this.user = parsedUrl.getUserName();
        this.host = parsedUrl.getHost();
    }

}
