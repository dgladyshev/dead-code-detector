package com.dgladyshev.deadcodedetector.repositories;

import com.dgladyshev.deadcodedetector.entities.Inspection;
import com.dgladyshev.deadcodedetector.entities.InspectionState;
import java.util.List;
import java.util.Set;
import org.springframework.data.repository.PagingAndSortingRepository;

@SuppressWarnings("PMD.MethodNamingConventions")
// must contain _ for Spring Data to generate query
public interface InspectionsRepository extends PagingAndSortingRepository<Inspection, Long> {

    List<Inspection> findByGitRepo_Name_AndGitRepo_User_AndGitRepo_Host(String name, String user, String
            host);

    Set<Inspection> findAllByStateNotContains(InspectionState state);

    Set<Inspection> findAllByStateContains(InspectionState state);
}