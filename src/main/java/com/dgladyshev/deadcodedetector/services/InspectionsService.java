package com.dgladyshev.deadcodedetector.services;

import static com.dgladyshev.deadcodedetector.util.FileSystemUtils.deleteDirectoryIfExists;
import static org.apache.commons.lang.StringUtils.trimToEmpty;

import com.dgladyshev.deadcodedetector.entities.GitRepo;
import com.dgladyshev.deadcodedetector.entities.Inspection;
import com.dgladyshev.deadcodedetector.entities.InspectionState;
import com.dgladyshev.deadcodedetector.exceptions.InspectionAlreadyExistsException;
import com.dgladyshev.deadcodedetector.exceptions.InspectionIsLockedException;
import com.dgladyshev.deadcodedetector.exceptions.MalformedRequestException;
import com.dgladyshev.deadcodedetector.exceptions.NoSuchInspectionException;
import com.dgladyshev.deadcodedetector.exceptions.NoSuchRepositoryException;
import com.dgladyshev.deadcodedetector.repositories.InspectionsRepository;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import java.util.List;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.jgit.util.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class InspectionsService {

    private static final Set<InspectionState> INSPECTION_COMPLETED_STATES = Sets.newHashSet(
            InspectionState.COMPLETED,
            InspectionState.FAILED
    );

    @Value("${data.dir}")
    private String dataDir;

    @Autowired
    private InspectionsRepository inspectionsRepository;

    @Autowired
    private InspectionStateMachine inspectionStateMachine;

    public Inspection createInspection(GitRepo repo, String language, String branch, String url) {
        checkBranch(branch);
        if (isInspectionExists(repo, branch)) {
            throw new InspectionAlreadyExistsException("Inspection for that branch and that repository has "
                                                       + "already been created. Use inspections/refresh endpoint or "
                                                       + "choose another branch to inspect.");
        }
        Inspection inspection = new Inspection(repo, language, branch, url);
        inspectionStateMachine.changeState(inspection, InspectionState.ADDED);
        return inspectionsRepository.save(inspection);
    }

    public Inspection getRefreshableInspection(GitRepo repo, String branch) {
        checkBranch(branch);
        Inspection inspection = getInspection(repo, branch);
        checkIfInspectionIsLocked(inspection.getId());
        return inspection;
    }

    private boolean isInspectionExists(GitRepo repo, String branch) {
        return inspectionsRepository.findByGitRepo(repo)
                .stream()
                .map(Inspection::getBranch)
                .anyMatch(branch::equalsIgnoreCase);
    }

    private void checkBranch(String branch) throws MalformedRequestException {
        if (StringUtils.isEmptyOrNull(trimToEmpty(branch))) {
            throw new MalformedRequestException("Branch name is empty");
        }
    }

    public List<Inspection> getInspections() {
        return Lists.newArrayList(inspectionsRepository.findAll());
    }

    public List<Inspection> getPaginatedInspections(int pageNumber, int pageSize) {
        return Lists.newArrayList(inspectionsRepository.findAll(new PageRequest(pageNumber, pageSize)));
    }

    public Inspection getInspection(Long id) throws NoSuchInspectionException {
        if (id != null && inspectionsRepository.exists(id)) {
            return inspectionsRepository.findOne(id);
        } else {
            throw new NoSuchInspectionException("There is no record of any inspection with specified id");
        }
    }

    //TODO add endpoint for this
    private Inspection getInspection(GitRepo repo, String branch) {
        return inspectionsRepository.findByGitRepo(repo)
                .stream()
                .filter(inspection -> branch.equalsIgnoreCase(inspection.getBranch()))
                .findAny()
                .orElseThrow(() -> new NoSuchInspectionException("There is no inspection with specified"
                                                                 + " url and branch."));
    }

    public void deleteInspection(Long id) throws NoSuchInspectionException, InspectionIsLockedException {
        if (id != null && inspectionsRepository.exists(id)) {
            checkIfInspectionIsLocked(id);
            inspectionsRepository.delete(id);
            deleteDirectoryIfExists(dataDir + "/" + id);
            log.info("Inspection with id: {} has been deleted", id);
        } else {
            throw new NoSuchInspectionException("Cannot delete inspection because there is none with such id");
        }
    }

    private void checkIfInspectionIsLocked(Long id) {
        Inspection inspection = inspectionsRepository.findOne(id);
        if (inspection == null) {
            throw new NoSuchInspectionException("There is no inspection with such id");
        }
        InspectionState state = inspection.getState();
        if (!INSPECTION_COMPLETED_STATES.contains(state)) {
            throw new InspectionIsLockedException("Inspection is locked to any changes until it would be completed.");
        }
    }

    public List<Inspection> getRepositoryInspections(GitRepo gitRepo) throws NoSuchRepositoryException {
        return inspectionsRepository.findByGitRepo(gitRepo);
    }

}


