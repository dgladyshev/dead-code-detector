package com.dgladyshev.deadcodedetector.repositories;

import static com.dgladyshev.deadcodedetector.util.FileSystemUtils.deleteDirectoryIfExists;
import static org.apache.commons.lang.StringUtils.trimToEmpty;

import com.dgladyshev.deadcodedetector.entity.GitRepo;
import com.dgladyshev.deadcodedetector.entity.Inspection;
import com.dgladyshev.deadcodedetector.entity.InspectionState;
import com.dgladyshev.deadcodedetector.exceptions.InspectionAlreadyExistsException;
import com.dgladyshev.deadcodedetector.exceptions.InspectionIsLockedException;
import com.dgladyshev.deadcodedetector.exceptions.MalformedRequestException;
import com.dgladyshev.deadcodedetector.exceptions.NoSuchInspectionException;
import com.google.common.collect.Sets;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.jgit.util.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class InspectionsRepository {

    private static final Set<InspectionState> INSPECTION_COMPLETED_STATES = Sets.newHashSet(
            InspectionState.COMPLETED,
            InspectionState.FAILED
    );

    @Value("${max.stored.inspections}")
    private int maxStoredInspections;

    @Value("${data.dir}")
    private String dataDir;

    @Autowired
    private GitRepositoriesRepository gitRepositoriesRepository;

    private final Map<String, Inspection> inspections = Collections.synchronizedMap(
            new LinkedHashMap<String, Inspection>() {
                @Override
                protected boolean removeEldestEntry(final Map.Entry eldest) {
                    return size() > maxStoredInspections;
                }
            }
    );

    public Inspection createInspection(GitRepo repo, String branch, String language) {
        checkBranch(branch);
        if (isInspectionExists(repo, branch)) {
            throw new InspectionAlreadyExistsException("Inspection for that branch and that repository has "
                                                       + "already been created. Use inspections/refresh endpoint or "
                                                       + "choose another branch to inspect.");
        }
        String id = java.util.UUID.randomUUID().toString();
        Inspection inspection = new Inspection(id, repo, language, branch);
        inspections.put(id, inspection);
        inspection.changeState(InspectionState.ADDED);
        gitRepositoriesRepository.addInspection(repo, id);
        log.info("Exception with id: {} has been created", id);
        return inspections.get(id);
    }

    public Inspection getRefreshableInspection(GitRepo repo, String branch) {
        checkBranch(branch);
        Inspection inspection = getInspection(repo, branch);
        checkIfInspectionIsLocked(inspection.getInspectionId());
        return inspection;
    }

    private boolean isInspectionExists(GitRepo repo, String branch) {
        return gitRepositoriesRepository.isRepositoryExist(repo)
               && gitRepositoriesRepository.getRepositoryInspections(repo)
                       .stream()
                       .map(inspections::get)
                       .map(Inspection::getBranch)
                       .anyMatch(branch::equalsIgnoreCase);
    }

    private void checkBranch(String branch) throws MalformedRequestException {
        if (StringUtils.isEmptyOrNull(trimToEmpty(branch))) {
            throw new MalformedRequestException("Branch name is empty");
        }
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

    //TODO add enpoint to this
    private Inspection getInspection(GitRepo repo, String branch) {
        return gitRepositoriesRepository.getRepositoryInspections(repo)
                .stream()
                .map(inspections::get)
                .filter(inspection -> branch.equalsIgnoreCase(inspection.getBranch()))
                .findAny()
                .orElseThrow(() -> new NoSuchInspectionException("There is no inspection with specified"
                                                                 + " url and branch."));
    }

    public void deleteInspection(String id) throws NoSuchInspectionException, InspectionIsLockedException {
        if (id != null && inspections.containsKey(id)) {
            checkIfInspectionIsLocked(id);
            deleteDirectoryIfExists(dataDir + "/" + id);
            inspections.remove(id);
            log.info("Inspection with id: {} has been deleted", id);
        } else {
            throw new NoSuchInspectionException("Cannot delete inspection because there is none with such id");
        }
    }

    private void checkIfInspectionIsLocked(String id) {
        InspectionState state = inspections.get(id).getState();
        if (!INSPECTION_COMPLETED_STATES.contains(state)) {
            throw new InspectionIsLockedException("Inspection is locked to any changes until it would be completed.");
        }
    }
}


