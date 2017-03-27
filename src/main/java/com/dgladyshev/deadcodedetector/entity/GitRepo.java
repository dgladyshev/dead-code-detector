package com.dgladyshev.deadcodedetector.entity;

import static org.apache.commons.lang.StringUtils.trimToEmpty;

import com.dgladyshev.deadcodedetector.util.GitHubRepositoryName;
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
public class GitRepo {

    private String name;
    private String user;
    private String host;
    private String url;

    public GitRepo(String url) {
        GitHubRepositoryName parsedUrl = GitHubRepositoryName.create(url);
        this.name = parsedUrl.getRepositoryName();
        this.user = parsedUrl.getUserName();
        this.host = parsedUrl.getHost();
        this.url = trimToEmpty(url);
    }

    //Note: url field doesn't used in equals method
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        GitRepo gitRepo = (GitRepo) o;

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
