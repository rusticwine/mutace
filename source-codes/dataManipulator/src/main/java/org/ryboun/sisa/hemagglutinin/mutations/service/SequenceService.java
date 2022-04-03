package org.ryboun.sisa.hemagglutinin.mutations.service;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import javax.annotation.PostConstruct;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.ryboun.sisa.hemagglutinin.mutations.Utils;
import org.ryboun.sisa.hemagglutinin.mutations.model.AlignedSequence;
import org.ryboun.sisa.hemagglutinin.mutations.model.Sequence;
import org.ryboun.sisa.hemagglutinin.mutations.model.SequenceDownloadEvent;
import org.ryboun.sisa.hemagglutinin.mutations.model.SequencesProcessingStatus;
import org.ryboun.sisa.hemagglutinin.mutations.repository.AlignedSequenceRepository;
import org.ryboun.sisa.hemagglutinin.mutations.repository.ReferenceSequenceRepository;
import org.ryboun.sisa.hemagglutinin.mutations.repository.SequenceDownloadEventRepository;
import org.ryboun.sisa.hemagglutinin.mutations.repository.SequenceRepository;
import org.ryboun.sisa.hemagglutinin.mutations.repository.SequencesProcessingRepository;
import org.ryboun.sisa.hemagglutinin.mutations.service.rest.RawSequenceDownloader;
import org.ryboun.sisa.module.alignment.AlignDto;
import org.ryboun.sisa.module.alignment.Aligner;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;


@Slf4j
@Service
public class SequenceService {

    @Builder
    //    @Getter
    //    @Setter
    @Data
    static class AlignSubmitResult {

        int downloadedSequencesSince;
        int sequenceSubmitForAlignment;
    }

    @Value("${alignment.submitJob.email}")
    private String email;
    @Value("${alignment.submitJob.jobType}")
    private String jobType;

    @Autowired
    ReferenceSequenceRepository referenceSequenceRepository;

    @Autowired
    SequenceRepository sequenceRepository;

    @Autowired
    RawSequenceDownloader rawSequenceDownloader;

    @Autowired
    AlignedSequenceRepository alignedSequenceRepository;

    @Autowired
    SequencesProcessingRepository sequencesProcessingRepository;

    @Autowired
    Aligner aligner;

    @Autowired
    SequenceDownloadEventRepository sequenceDownloadEventRepository;


    public static MockWebServer mockBackEnd;
    @PostConstruct
    void init() {

    }

//    void initMockServer() throws IOException {
//        mockBackEnd = new MockWebServer();
//        mockBackEnd.start();
//        String baseUrl = String.format("http://localhost:%s",
//                                       mockBackEnd.getPort());
//
//        mockBackEnd.enqueue(new MockResponse()
//                                    .setBody(esearch)
//                                    .addHeader("Content-Type", "application/json"));
//    }

    void initWireMock() {
        WireMockServer wireMockServer;
        WebClient webClient;

        wireMockServer = new WireMockServer(WireMockConfiguration.wireMockConfig().dynamicPort());
        wireMockServer.start();
        webClient = WebClient.builder().baseUrl(wireMockServer.baseUrl()).build();
    }



    public List<Sequence> findAllSequences() {
        //        alignSequences();
        return sequenceRepository.findAll();
    }


    public long getSequenceCount() {
        return sequenceRepository.count();
    }


    public long getInAlignmetnProcessSequenceCount() {
        return sequencesProcessingRepository.count();
    }


    @Transactional(propagation = Propagation.REQUIRED)
    public Sequence saveSequence(Sequence sequence) {
        return sequenceRepository.save(sequence);
    }

    //    public SequencesProcessingStatus addDownloadedSequences(List<Sequence> downloadedSequences) {
    //        SequencesProcessingStatus sequencesProcessingStatus = SequencesProcessingStatus.builder()
    //                .rawSequences(downloadedSequences)
    //                .status(SequencesProcessingStatus.STATUS.DOWNLOADED)
    //                .build();
    //
    //        return sequencesProcessingRepository.save(sequencesProcessingStatus);
    //    }


    public Optional<SequencesProcessingStatus> findSequenceProcessingStatusById(String id) {
        return sequencesProcessingRepository.findById(id);
    }


    public List<SequencesProcessingStatus> findAllSequencesProcessingStatuses() {
        return sequencesProcessingRepository.findAll();
    }


    /**
     * checks the downloaded sequences and tries to upload it. Idea is to run it regularly, like once a day
     */
    //    @Transactional
    //    public void alignSequences() {
    //        List<Sequence> downloadedSequences = sequenceRepository.findByStatus(
    //                Sequence.STATUS.DOWNLOADED);
    //        //TODO - find reference sequence for each sequence
    //        Map<Sequence, List<Sequence>> sequencesToAlign = downloadedSequences.stream()
    //                .map(sequence -> Pair.of(this.getReferenceForAlignment_mock(sequence), sequence))
    //                .<Map<Sequence, List<Sequence>>>reduce(
    //                        new HashMap<Sequence, List<Sequence>>(),
    //                        (resultMap, item) -> {
    //                            resultMap.putIfAbsent(item.getKey(), new ArrayList<>());
    //                            resultMap.get(item.getKey()).add(item.getValue());
    //                            return resultMap;
    //                        },
    //                        (resultMap1, resultMap2) -> {
    //                            resultMap1.putAll(resultMap2);
    //                            return resultMap1;
    //                        });
    //
    //        //from sequences to align create from first a sequence entity for alignment
    //        Optional<SequencesProcessingStatus> sequencesProcessingStatus =
    //                sequencesToAlign.entrySet().stream()
    //                        .map(entry -> SequencesProcessingStatus.builder()
    //                                .referenceSequence(entry.getKey())
    //                                .rawSequences(entry.getValue())
    //                                .alignJobId("not_started")
    //                                .status(Sequence.STATUS.TO_BE_ALIGNED)
    //                                .build())
    //                        .findFirst();
    //
    //        //if there is an entity for alignment submit it for alignment
    //        //functional in imperative style?
    //        sequencesProcessingStatus.ifPresent(sps -> {
    //            //aligning status entity changed to dto for alignment request
    //            AlignDto alignDto = AlignDto.builder()
    //                    .format(jobType)
    //                    .email(email)
    //                    .addSequence(sps.getReferenceSequence())
    //                    .sequences(sps.getRawSequences())
    //                    .build();
    //
    //            //launch alinment and get job id from server
    //            String alignJobId = aligner.alignWithSingleReference(alignDto);
    //            //change status of job to being aligned
    //            sps.setAlignJobId(alignJobId);
    //        });
    //    }
    @Transactional
    public AlignSubmitResult alignSequences() {
        List<Sequence> downloadedSequences = sequenceRepository.findByStatus(Sequence.STATUS.DOWNLOADED);

        Map<Sequence, List<Sequence>> sequencesToAlign = Map.of(this.getReferenceForAlignment_mock(),
                                                                downloadedSequences);

        //TODO - there may be just single "bunch" to align with 1 or 2 reference sequence
        //from sequences to align create from first a sequence entity for alignment
        List<SequencesProcessingStatus> sequencesProcessingStatuses = sequencesToAlign.entrySet()
                                                                                      .stream()
                                                                                      .map(entry -> SequencesProcessingStatus.builder()
                                                                                                                             .referenceSequence(
                                                                                                                                     entry.getKey())
                                                                                                                             .rawSequences(
                                                                                                                                     entry.getValue())
                                                                                                                             //                                .alignJobId("not_started")
                                                                                                                             .status(Sequence.STATUS.TO_BE_ALIGNED)
                                                                                                                             .build())
                                                                                      .map(sequencesProcessingRepository::save)
                                                                                      .collect(Collectors.toList());
        //.findFirst();

        //TODO - why tuple? Handle errors!
        //now create alignment DTO and submit the alignment process
        List<SequencesProcessingStatus> submitAlignments = sequencesProcessingStatuses.stream()
                                                                                      .map(sps -> Pair.of(sps,
                                                                                                          AlignDto.builder()
                                                                                                                  .format(jobType)
                                                                                                                  .email(email)
                                                                                                                  .addSequence(
                                                                                                                          sps.getReferenceSequence())
                                                                                                                  .sequences(
                                                                                                                          sps.getRawSequences())
                                                                                                                  .build()))
                                                                                      .map(sequenceDtoPair -> Pair.of(
                                                                                              sequenceDtoPair.getLeft(),
                                                                                              aligner.alignWithSingleReference(
                                                                                                      sequenceDtoPair.getRight())))
                                                                                      .peek(sequenceJobIdPair -> sequenceJobIdPair.getLeft()
                                                                                                                                  .setAlignJobId(
                                                                                                                                          sequenceJobIdPair.getRight()))
                                                                                      .peek(sequenceJobIdPair -> sequenceJobIdPair.getLeft()
                                                                                                                                  .setStatus(
                                                                                                                                          Sequence.STATUS.ALIGNING))
                                                                                      .map(sequenceJobIdPair -> sequencesProcessingRepository.save(
                                                                                              sequenceJobIdPair.getLeft()))//TODO - probably no need to save
                                                                                      .collect(Collectors.toList());

        //awkward - not encapsulated. Maybe not using status in general sequence collection as it may be realigned anyway
        downloadedSequences.stream()
                           .peek(sequence -> sequence.setStatus(Sequence.STATUS.ALIGNING))
                           .forEach(sequence -> sequenceRepository.save(sequence)); //FIXME - 1) no need to save? 2) Also not so separated. 3) Not being saved one by one

        return new AlignSubmitResult(sequencesToAlign.values().size(), submitAlignments.size());
    }


    //@Getter
    //record AlignerChecker(SequencesProcessingStatus sequencesProcessingStatus, String jobResult) {};
    @Data
    @AllArgsConstructor
    static class AlignerChecker {

        SequencesProcessingStatus sequencesProcessingStatus;
        String jobResult;
    }


    @Transactional
    public long updateAligningSequences() {
        List<SequencesProcessingStatus> aligningSequences = sequencesProcessingRepository.findByStatus(Sequence.STATUS.ALIGNING);
        return Utils.<SequencesProcessingStatus>createLoggingStream(aligningSequences,
                                                                    (SequencesProcessingStatus as) -> String.format(
                                                                            "alignment check, job ID %s, prior status %s, accvers %s",
                                                                            as.getAlignJobId(),
                                                                            as.getStatus(),
                                                                            Utils.accverFromSequencesToString(as.getRawSequences())))
                    .map(sequenceProcessing -> new AlignerChecker(sequenceProcessing,
                                                                  aligner.checkJobStatus(sequenceProcessing.getAlignJobId())))
                    .peek(alignerChecker -> System.out.println(String.format(
                            "Alignment checker/updater. For sequences %s aligning in a job id %s job the result is %s",
                            Utils.accverFromSequencesToString(alignerChecker.getSequencesProcessingStatus()
                                                                            .getRawSequences()),
                            alignerChecker.getSequencesProcessingStatus().getAlignJobId(),
                            alignerChecker.getJobResult())))
                    .filter(alignerChecker -> StringUtils.endsWithAny(alignerChecker.getJobResult(),
                                                                      "FINISHED",
                                                                      "DONE")) //TODO - what's the correct one?
                    .peek(alignerChecker -> alignerChecker.getSequencesProcessingStatus()
                                                          .setStatus(Sequence.STATUS.ALIGNED))
                    .map(alignerChecker -> sequencesProcessingRepository.save(alignerChecker.getSequencesProcessingStatus()))
                    .count();
    }


    Integer[] build(String alignedSequences, SequencesProcessingStatus inputSequences) {
        final StringBuilder alignedSequencesSb = new StringBuilder(alignedSequences);
        List<Integer> indexes = inputSequences.getRawSequences().stream()
                .map(Sequence::getAccver)
                .map(alignedSequencesSb::indexOf)
                .collect(Collectors.toList());

        return indexes.toArray(new Integer[indexes.size()]);
    }
    @Transactional
    public long processAlignedSequences() {
        List<SequencesProcessingStatus> alignedSequences = sequencesProcessingRepository.findByStatus(Sequence.STATUS.ALIGNED);
        List<Integer[]> result = alignedSequences.stream()
                .map(sequenceProcessingAligned -> Pair.of(sequenceProcessingAligned, aligner.getJobResult(sequenceProcessingAligned.getAlignJobId())))
                .map(statusAlignmentPair -> build(statusAlignmentPair.getRight(), statusAlignmentPair.getLeft()))
                .collect(Collectors.toList());
//                                              .map(sequenceProcessingAligned -> {
//            String jobId = sequenceProcessingAligned.getAlignJobId();
//            String alignment = aligner.getJobResult(jobId);
//            System.out.println("Aligned sequences: " + alignment);
//            return alignment;
//        }).collect(Collectors.toList());
//remove from statuses?
        return result.size();
    }


    ///  aligned sequences ///
    @Deprecated //to be replaced withreal reference, maybe in a file too?
    public Sequence getReferenceForAlignment_mock() {
        //return sequenceRepository.findByAccver("KC899669.1").get(0);
        return referenceSequenceRepository.findAll().get(0);
    }


    public List<AlignedSequence> findAlignedSequences(LocalDateTime startDate, LocalDateTime endDate) {
        if (startDate != null && endDate != null) {
            return alignedSequenceRepository.downloadDateBetween(startDate, endDate);
        } else if (startDate != null) {
            return alignedSequenceRepository.downloadDateAfter(startDate);
        } else if (endDate != null) {
            return alignedSequenceRepository.downloadDateBefore(endDate);
        } else {
            return alignedSequenceRepository.findAll();
        }
    }


    @Transactional
    public List<AlignedSequence> saveAlignedSequences(List<AlignedSequence> sequence) {
        return alignedSequenceRepository.saveAll(sequence);
    }


    @Deprecated //just for example sequences. Not usable so far
    public AlignedSequence saveAlignedSequence(AlignedSequence sequence) {
        return alignedSequenceRepository.save(sequence);
    }


    @Transactional
    public int downloadAndSaveNewSequences(int downloaderPeriodDays) {
        LocalDate dateFrom = getLstSequenceDownloadDate();
        LocalDate dateTo = dateFrom.plusDays(downloaderPeriodDays);
        System.out.println("date to start from - to: " + dateFrom.toString() + " - " + dateTo.toString());
        //move here some audit - like collection with data: from, to, sequenceCount
        int newDownloadedSequnceCount = processNewDownloadedSequences(downloadSequencesFromTo(dateFrom, dateTo));

        SequenceDownloadEvent sequenceDownloadEvent = SequenceDownloadEvent.builder()
                                                                           .downloadedOn(LocalDateTime.now())
                                                                           .downloadFrom(dateFrom)
                                                                           .downloadTill(dateTo)
                                                                           .downloadedSequenceCount(
                                                                                   newDownloadedSequnceCount)
                                                                           .build();

        sequenceDownloadEventRepository.save(sequenceDownloadEvent);
        return newDownloadedSequnceCount;
    }


    /**
     * Sequences are set with {@link Sequence.STATUS#DOWNLOADED} and persisted in main sequence repository
     *
     * @param sequences
     *
     * @return
     */
    private int processNewDownloadedSequences(Mono<List<Sequence>> sequences) {
        //TODO - move status setting over here
        //        sequences.map(sequenceRepository::saveAll);
        List<Sequence> savedSequences = sequences.block()
                                                 .stream()
                                                 .peek(sequence -> sequence.setStatus(Sequence.STATUS.DOWNLOADED))
                                                 .filter(sequence -> CollectionUtils.isEmpty(sequenceRepository.findByAccver(
                                                         sequence.getAccver())))
                                                 .map(sequenceRepository::save)
                                                 .collect(Collectors.toList());

        return savedSequences != null ? savedSequences.size() : 0;
    }


    @Transactional
    public Mono<List<Sequence>> downloadSequencesFromTo(LocalDate downloadedDateTimeFrom,
                                                        LocalDate downloadedDateTimeTo) {
        return rawSequenceDownloader.downloadSequencesFrom(downloadedDateTimeFrom, downloadedDateTimeTo)
                                    .map(Utils::mapperNotYetWorkingForMe)
                                    .map(sequences -> {
                                        sequences.stream().forEach(sequenceRepository::save);
                                        return sequences;
                                    });
    }


    @Transactional
    public Mono<RawSequenceDownloader.EsearchResponse> downloadSequencesFromTo2(LocalDate downloadedDateTimeFrom,
                                                                                LocalDate downloadedDateTimeTo) {
        Mono<RawSequenceDownloader.EsearchResponse> sequenceTest = rawSequenceDownloader.downloadSequencesFrom2(
                downloadedDateTimeFrom,
                downloadedDateTimeTo);
        return sequenceTest;
    }


    //TODO - move to distinc service??
    public LocalDate getLstSequenceDownloadDate() {
        //TODO - date when to start with downloads
        //TODO - check if the date is somewhat current and download more often if not - maybe in initilLoad method?
        return sequenceDownloadEventRepository.findFirstByOrderByDownloadTillDesc()
                                              .map(SequenceDownloadEvent::getDownloadTill)
                                              .orElse(LocalDate.of(2021, 1, 1)); //TODO - property for initial load?
    }


    @Transactional(readOnly = true)
    public List<Sequence> getAllDownloadedSequences() {
        return sequenceRepository.findByStatus(Sequence.STATUS.DOWNLOADED);
    }
}
