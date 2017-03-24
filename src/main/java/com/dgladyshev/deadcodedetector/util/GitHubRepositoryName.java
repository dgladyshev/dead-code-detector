package com.dgladyshev.deadcodedetector.util;

import static org.apache.commons.lang.StringUtils.trimToEmpty;

import com.dgladyshev.deadcodedetector.exceptions.MalformedRepositoryUrl;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

/**
 * Original source code is here https://github.com/jenkinsci/github-plugin
 * I modified it in a way that suited my needs.
 */
@Slf4j
@AllArgsConstructor
@Data
public class GitHubRepositoryName {

    private String host;
    private String userName;
    private String repositoryName;

    private static final Pattern[] URL_PATTERNS = {
            /**
             * The first set of patterns extract the host, owner and repository names
             * from URLs that include a '.git' suffix, removing the suffix from the
             * repository name.
            */
            Pattern.compile("https?://[^/]+@([^/]+)/([^/]+)/([^/]+)\\.git"),
            Pattern.compile("https?://([^/]+)/([^/]+)/([^/]+)\\.git"),
            /**
             * The second set of patterns extract the host, owner and repository names
             * from all other URLs. Note that these patterns must be processed *after*
             * the first set, to avoid any '.git' suffix that may be present being included
             * in the repository name.
            */
            Pattern.compile("https?://[^/]+@([^/]+)/([^/]+)/([^/]+)/?"),
            Pattern.compile("https?://([^/]+)/([^/]+)/([^/]+)/?"),
            };

    /**
     * Create {@link GitHubRepositoryName} from URL
     *
     * @param url repo url. Can be null
     * @return parsed {@link GitHubRepositoryName} or throws exception if it cannot be parsed from the specified URL
     */
    public static GitHubRepositoryName create(String url) {
        log.debug("Constructing from URL {}", url);
        for (Pattern p : URL_PATTERNS) {
            Matcher matcher = p.matcher(trimToEmpty(url));
            if (matcher.matches()) {
                log.debug("URL matches {}", matcher);
                GitHubRepositoryName ret = new GitHubRepositoryName(
                        matcher.group(1),
                        matcher.group(2),
                        matcher.group(3)
                );
                log.debug("Object is {}", ret);
                return ret;
            }
        }
        log.warn("Could not match URL {}", url);
        throw new MalformedRepositoryUrl("The specified URL is not in format of git repository");
    }


}