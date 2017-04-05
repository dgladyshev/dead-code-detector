package com.dgladyshev.deadcodedetector.entities;

import com.dgladyshev.deadcodedetector.util.GitHubRepositoryName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.neo4j.ogm.annotation.GraphId;
import org.neo4j.ogm.annotation.NodeEntity;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
@NodeEntity
public class GitRepo {

    @GraphId
    private Long id;

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
