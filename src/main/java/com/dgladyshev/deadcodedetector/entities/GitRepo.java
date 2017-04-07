package com.dgladyshev.deadcodedetector.entities;

import static org.apache.commons.lang3.StringUtils.trimToEmpty;

import com.dgladyshev.deadcodedetector.util.GitHubRepositoryName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.NotEmpty;
import org.neo4j.ogm.annotation.GraphId;
import org.neo4j.ogm.annotation.NodeEntity;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class GitRepo {

    @NotEmpty
    private String name;
    @NotEmpty
    private String user;
    @NotEmpty
    private String host;
    @NotEmpty
    private String url;

    public GitRepo(String repositoryUrl) {
        this.url = trimToEmpty(repositoryUrl).replace(".git", "");
        GitHubRepositoryName parsedUrl = GitHubRepositoryName.create(url);
        this.name = parsedUrl.getRepositoryName();
        this.user = parsedUrl.getUserName();
        this.host = parsedUrl.getHost();
    }

}
