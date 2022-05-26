package org.ryboun.sisa.hemagglutinin.mutations.service.rest;

import org.ryboun.sisa.hemagglutinin.mutations.dto.SequenceGenepeptList;
import org.ryboun.sisa.hemagglutinin.mutations.dto.SequenceTestable;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.LocalDate;

public interface RawSequenceDownloaderService {

//    Mono<SequenceGenepeptList> downloadSequencesFromTo(LocalDate from, LocalDate to);
    Mono<SequenceTestable> downloadSequencesFromTo(LocalDate from, LocalDate to);

}
