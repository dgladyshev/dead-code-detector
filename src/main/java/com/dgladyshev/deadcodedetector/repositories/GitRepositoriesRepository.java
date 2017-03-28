package com.dgladyshev.deadcodedetector.repositories;

import com.dgladyshev.deadcodedetector.entity.GitRepo;
import com.dgladyshev.deadcodedetector.exceptions.NoSuchInspectionException;
import com.dgladyshev.deadcodedetector.exceptions.NoSuchRepositoryException;
import com.google.common.collect.Sets;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class GitRepositoriesRepository {

    @Value("${max.stored.repositories}")
    private int maxStoredRepositories;

    private final Map<GitRepo, Set<String>> gitRepositories = Collections.synchronizedMap(
            new LinkedHashMap<GitRepo, Set<String>>() {
                @Override
                protected boolean removeEldestEntry(final Map.Entry eldest) {
                    return size() > maxStoredRepositories;
                }
            }
    );

    public void addInspection(GitRepo gitRepo, String inspectionId) {
        if (!gitRepositories.containsKey(gitRepo)) {
            gitRepositories.put(gitRepo, Sets.newHashSet());
        }
        Set<String> inspectionIds = gitRepositories.get(gitRepo);
        inspectionIds.add(inspectionId);
        log.info("Inspection with id: {} has been added to repository {}", inspectionId, gitRepo);
    }

    public Map<GitRepo, Set<String>> getRepositories() {
        return gitRepositories;
    }

    public Set<String> getRepositoryInspections(GitRepo gitRepo) throws NoSuchRepositoryException {
        if (isRepositoryExist(gitRepo)) {
            return gitRepositories.get(gitRepo);
        } else {
            throw new NoSuchInspectionException("There is no record of such repository");
        }
    }

    public boolean isRepositoryExist(GitRepo gitRepo) {
        return gitRepo != null && getRepositories().containsKey(gitRepo);
    }


}


