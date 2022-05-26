package org.ryboun.sisa.hemagglutinin.mutations.controller;

import org.ryboun.sisa.hemagglutinin.mutations.model.AlignedSequence;
import org.ryboun.sisa.hemagglutinin.mutations.model.Sequence;
import org.ryboun.sisa.hemagglutinin.mutations.service.SequenceService;
import org.ryboun.sisa.hemagglutinin.mutations.service.rest.NcbiRawSequenceDownloader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("sequence")
public class SequenceController {

    @Autowired
    SequenceService sequenceService;

//    AlignedSe

    @GetMapping
    public List<Sequence> getAllSequences() {
        return sequenceService.findAllSequences();
    }

    @GetMapping(path = "/aligned")
    public List<AlignedSequence> getFilteredAlignedSequences(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime downloadedDateTimeFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime downloadedDateTimeTo) {

        return sequenceService.findAlignedSequences(downloadedDateTimeFrom, downloadedDateTimeTo);
    }

    @GetMapping(path = "/testDownloadSequences")
    public Mono<List<Sequence>> testDownloadSequences(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime downloadedDateTimeFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime downloadedDateTimeTo) {

        return sequenceService.downloadSequencesFromTo(downloadedDateTimeFrom.toLocalDate(), downloadedDateTimeTo.toLocalDate());
    }
//
//    @GetMapping(path = "/testDownloadSequences2")
//    public Mono<NcbiRawSequenceDownloader.EsearchResponse> testDownloadSequences2(
//            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime downloadedDateTimeFrom,
//            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime downloadedDateTimeTo) {
//
//        return sequenceService.downloadSequencesFromTo2(downloadedDateTimeFrom.toLocalDate(), downloadedDateTimeTo.toLocalDate());
//    }
}
