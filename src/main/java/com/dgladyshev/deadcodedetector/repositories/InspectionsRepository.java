package com.dgladyshev.deadcodedetector.repositories;

import com.dgladyshev.deadcodedetector.entity.GitRepo;
import com.dgladyshev.deadcodedetector.entity.Inspection;
import com.dgladyshev.deadcodedetector.entity.InspectionStatus;
import com.dgladyshev.deadcodedetector.exceptions.NoSuchInspectionException;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class InspectionsRepository {

    @Value("${max.stored.inspections}")
    private int maxStoredInspections;

    private final Map<String, Inspection> inspections = Collections.synchronizedMap(
            new LinkedHashMap<String, Inspection>() {
                @Override
                protected boolean removeEldestEntry(final Map.Entry eldest) {
                    return size() > maxStoredInspections;
                }
            }
    );

    public Inspection createInspection(GitRepo repo) {
        String id = java.util.UUID.randomUUID().toString();
        inspections.put(
                id,
                Inspection.builder()
                        .inspectionId(id)
                        .gitRepo(repo)
                        .timestampAdded(System.currentTimeMillis())
                        .status(InspectionStatus.ADDED)
                        .build()
        );
        return inspections.get(id);
    }

    public Map<String, Inspection> getInspections() {
        return inspections;
    }

    public Inspection getInspection(String id) throws NoSuchInspectionException {
        if (id != null && inspections.containsKey(id)) {
            return inspections.get(id);
        } else {
            throw new NoSuchInspectionException("There is no record of any inspection with specified id");
        }
    }

    public void deleteInspection(String id) throws NoSuchInspectionException {
        if (id != null && inspections.containsKey(id)) {
            inspections.remove(id);
        } else {
            throw new NoSuchInspectionException("Cannot delete inspection because there is none with such id");
        }
    }

}


