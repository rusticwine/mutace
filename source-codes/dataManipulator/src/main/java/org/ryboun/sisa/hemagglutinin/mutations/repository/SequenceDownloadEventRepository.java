package org.ryboun.sisa.hemagglutinin.mutations.repository;

import java.util.Optional;
import org.ryboun.sisa.hemagglutinin.mutations.model.SequenceDownloadEvent;
import org.springframework.data.mongodb.repository.MongoRepository;


public interface SequenceDownloadEventRepository extends MongoRepository<SequenceDownloadEvent, String> {

//    Optional<Batch> findFirstByBatchIdStartingWithOrderByBatchIdDesc(String batchPrefix);

    //TODO - add success conditional
    Optional<SequenceDownloadEvent> findFirstByOrderByDownloadTillDesc();
}
