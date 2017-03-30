package com.dgladyshev.deadcodedetector.entity;

import com.dgladyshev.deadcodedetector.util.GitHubRepositoryName;
import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@AllArgsConstructor
@Builder
@Getter
@Setter
@ToString
public class GitRepo implements Serializable {

    private String name;
    private String user;
    private String host;

    public GitRepo(String url) {
        GitHubRepositoryName parsedUrl = GitHubRepositoryName.create(url);
        this.name = parsedUrl.getRepositoryName();
        this.user = parsedUrl.getUserName();
        this.host = parsedUrl.getHost();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }

        GitRepo gitRepo = (GitRepo) obj;

        if (!name.equals(gitRepo.name)) {
            return false;
        }
        if (!user.equals(gitRepo.user)) {
            return false;
        }
        return host.equals(gitRepo.host);
    }

    @Override
    public int hashCode() {
        int result = name.hashCode();
        result = 31 * result + user.hashCode();
        result = 31 * result + host.hashCode();
        return result;
    }
}
