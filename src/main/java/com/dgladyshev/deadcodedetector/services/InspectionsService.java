package com.dgladyshev.deadcodedetector.services;

import static com.dgladyshev.deadcodedetector.util.FileSystemUtils.deleteDirectoryIfExists;

import com.dgladyshev.deadcodedetector.entities.GitRepo;
import com.dgladyshev.deadcodedetector.entities.Inspection;
import com.dgladyshev.deadcodedetector.exceptions.InspectionAlreadyExistsException;
import com.dgladyshev.deadcodedetector.exceptions.InspectionIsLockedException;
import com.dgladyshev.deadcodedetector.exceptions.NoSuchInspectionException;
import com.dgladyshev.deadcodedetector.repositories.InspectionsRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Slf4j
@Service
public class InspectionsService {

    @Value("${data.dir}")
    private String dataDir;

    @Autowired
    private InspectionsRepository inspectionsRepository;

    public Mono<Inspection> createInspection(GitRepo repo, String language, String branch) {
        if (getInspection(repo, branch).block() != null) {
            throw new InspectionAlreadyExistsException();
        }
        Inspection inspection = Inspection.buildInspection(repo, language, branch);
        return inspectionsRepository.save(inspection);
    }

    public Flux<Inspection> getInspections() {
        return inspectionsRepository.findAll();
    }

    public Mono<Inspection> getInspection(String id) throws NoSuchInspectionException {
        return inspectionsRepository.findOne(id);
    }

    public Mono<Inspection> getInspection(GitRepo repo, String branch) {
        return inspectionsRepository.findByGitRepoAndBranch(Mono.just(repo), Mono.just(branch)).next();
    }

    public Flux<Inspection> getRepositoryInspections(GitRepo repo) {
        return inspectionsRepository.findByGitRepo_Name_AndGitRepo_User_AndGitRepo_Host(
                repo.getName(),
                repo.getUser(),
                repo.getHost()
        );
    }

    //TODO remove block() then stable version of Spring will arrive
    public Mono<Void> deleteInspection(Inspection inspection) throws NoSuchInspectionException,
            InspectionIsLockedException {
        String id = inspection.getId();
        deleteDirectoryIfExists(dataDir + "/" + id);
        inspectionsRepository.delete(id).doOnSuccess(
                Void -> log.info("Inspection with id: {} has been deleted", id)
        ).block();
        return Mono.empty();
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

    /*  TODO uncomment after reactive repository will support such approach
        public Flux<Inspection> getPaginatedInspections(int pageNumber, int pageSize) {
            return Lists.newArrayList(inspectionsRepository.findAll(new PageRequest(pageNumber, pageSize)));
        }*/
}


