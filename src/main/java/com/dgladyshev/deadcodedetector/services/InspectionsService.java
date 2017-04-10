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
import com.dgladyshev.deadcodedetector.repositories.InspectionsRepository;
import com.dgladyshev.deadcodedetector.util.InspectionUtils;
import com.google.common.collect.Sets;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.jgit.util.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import reactor.core.Exceptions;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

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

    public Mono<Inspection> createInspection(GitRepo repo, String language, String branch) {
        checkBranch(branch);
        if (isInspectionExists(repo, branch)) {
            throw new InspectionAlreadyExistsException();
        }
        Inspection inspection = InspectionUtils.createInspection(repo, language, branch);
        return inspectionsRepository.save(inspection);
    }

    public Mono<Inspection> getRefreshableInspection(GitRepo repo, String branch) {
        checkBranch(branch);
        return getInspection(repo, branch)
                .doOnNext(this::checkIfInspectionIsLocked);
    }

    //TODO validate in constructor as a separate entity
    private void checkBranch(String branch) throws MalformedRequestException {
        if (StringUtils.isEmptyOrNull(trimToEmpty(branch))) {
            throw new MalformedRequestException("Branch name is empty");
        }
    }

    public Flux<Inspection> getInspections() {
        return inspectionsRepository.findAll();
    }

    /*  TODO uncomment after reactive repository will support such approach
        public Flux<Inspection> getPaginatedInspections(int pageNumber, int pageSize) {
            return Lists.newArrayList(inspectionsRepository.findAll(new PageRequest(pageNumber, pageSize)));
        }*/

    public Mono<Inspection> getInspection(String id) throws NoSuchInspectionException {
        return inspectionsRepository.findOne(id);
    }

    private Mono<Inspection> getInspection(GitRepo repo, String branch) {
        return inspectionsRepository.findByGitRepoAndBranch(Mono.just(repo), Mono.just(branch)).next();
    }

    private boolean isInspectionExists(GitRepo repo, String branch) {
        return getInspection(repo, branch).block() != null;
    }

    public Mono<Void> deleteInspection(String id) throws NoSuchInspectionException, InspectionIsLockedException {
        return Mono.just(id)
                .doOnNext(id1 -> {
                    if (inspectionsRepository.exists(id).block()) {
                        Inspection inspection = inspectionsRepository.findOne(id).block();
                        checkIfInspectionIsLocked(inspection); //to do on next
                        inspectionsRepository.delete(id).block();
                        deleteDirectoryIfExists(dataDir + "/" + id);
                        log.info("Inspection with id: {} has been deleted", id);
                    } else {
                        throw new NoSuchInspectionException();
                    }
                })
                .doOnError(ex -> {
                    throw Exceptions.propagate(ex);
                })
                .then();
    }

    private void checkIfInspectionIsLocked(Inspection inspection) throws InspectionIsLockedException {
        Mono.just(inspection)
                .map(Inspection::getState)
                .filter(state -> !INSPECTION_COMPLETED_STATES.contains(state))
                .doOnNext(state -> {
                            throw new InspectionIsLockedException();
                            }
                )
                .doOnError(ex -> {
                    throw Exceptions.propagate(ex);
                })
                .block();
    }

    public Flux<Inspection> getRepositoryInspections(GitRepo repo) {
        return inspectionsRepository.findByGitRepo_Name_AndGitRepo_User_AndGitRepo_Host(
                repo.getName(),
                repo.getUser(),
                repo.getHost()
        );
    }

    @Async
    public void saveInspection(Inspection inspection) {
        inspectionsRepository.save(inspection).block();
        log.info(
                "Inspection updated. Id: {}. State: {}. Description: {}",
                inspection.getId(),
                inspection.getState(),
                inspection.getStateDescription()
        );
    }
}


