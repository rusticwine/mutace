package org.ryboun.sisa.hemagglutinin.mutations.service.rest;

import org.ryboun.sisa.hemagglutinin.mutations.dto.SequenceGenepeptList;
import reactor.core.publisher.Mono;

import java.time.LocalDate;

public interface RawSequenceDownloaderService {

    Mono<SequenceGenepeptList> downloadSequencesFromTo(LocalDate from, LocalDate to);
}
