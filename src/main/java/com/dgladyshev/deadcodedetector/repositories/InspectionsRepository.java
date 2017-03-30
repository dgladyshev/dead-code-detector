package com.dgladyshev.deadcodedetector.repositories;

import com.dgladyshev.deadcodedetector.entity.GitRepo;
import com.dgladyshev.deadcodedetector.entity.Inspection;
import java.util.List;
import org.springframework.data.repository.PagingAndSortingRepository;

public interface InspectionsRepository extends PagingAndSortingRepository<Inspection, Long> {

    List<Inspection> findByGitRepo(GitRepo repo);

}