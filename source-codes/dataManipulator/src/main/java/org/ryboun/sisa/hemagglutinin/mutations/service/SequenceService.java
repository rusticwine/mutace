package org.ryboun.sisa.hemagglutinin.mutations.service;

//import com.github.tomakehurst.wiremock.WireMockServer;
//import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
//import okhttp3.mockwebserver.MockWebServer;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.ryboun.sisa.hemagglutinin.mutations.Parsers;
import org.ryboun.sisa.hemagglutinin.mutations.Utils;
import org.ryboun.sisa.hemagglutinin.mutations.model.*;
import org.ryboun.sisa.hemagglutinin.mutations.repository.*;
import org.ryboun.sisa.hemagglutinin.mutations.service.rest.AlignerServiceMock;
import org.ryboun.sisa.hemagglutinin.mutations.service.rest.RawSequenceDownloaderService;
import org.ryboun.sisa.module.alignment.AlignDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import reactor.core.publisher.Mono;

import javax.annotation.PostConstruct;
import javax.validation.constraints.NotNull;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;


@Slf4j
@Service
public class SequenceService {

    private final String EBI_ALIGNER_JOB_FINISHED = "FINISHED";

    @Builder
    @Data
    static class AlignSubmitResult {

        int downloadedSequencesSince;
        int sequenceSubmitForAlignment;

        private static AlignSubmitResult emptyAlignSubmitResult = AlignSubmitResult.builder()
                .sequenceSubmitForAlignment(0)
                .downloadedSequencesSince(0)
                .build();

        public static AlignSubmitResult emptyAlignSubmitResult() {
            return emptyAlignSubmitResult;
        }
    }

    @Value("${alignment.submitJob.email}")
    private String email;
    @Value("${alignment.submitJob.jobType}")
    private String jobType;

    @Value("${alignment.sequenceCount}")
    private int alignmentSequencesCount;

    @Value("${date.start.downloading}")
    @DateTimeFormat(pattern = "dd.MM.yyyy")
    LocalDate dateStartDownloading;

    @Autowired
    RawSequenceDownloaderService rawSequenceDownloader;
    @Autowired
    SequenceRepository sequenceRepository;
    @Autowired
    ReferenceSequenceRepository referenceSequenceRepository;
    @Autowired
    SequencesProcessingStatusRepository sequencesProcessingStatusRepository;
    @Autowired
    AlignedSequenceRepository alignedSequenceRepository;
    @Autowired
    @Lazy //TODO - check why
    AlignerServiceMock alignerService;

    @Autowired
    SequenceDownloadEventRepository sequenceDownloadEventRepository;


    @PostConstruct
    void init() throws IOException {
        List<ReferenceSequence> referenceSequences = Parsers.loadReferenceSequenceFromResource();
        log.debug(CollectionUtils.isEmpty(referenceSequences) ? "no reference sequence loaded" : referenceSequences.stream().map(ReferenceSequence::getAccver).collect(Collectors.joining()));
        referenceSequenceRepository.saveAll(referenceSequences);
//        System.out.println(referenceSequenceRepository.findAll());
//        referenceSequenceRepository.saveAll(referenceSequences);
//        System.out.println(referenceSequenceRepository.findAll());
    }

    public List<Sequence> findAllSequences() {
        //        alignSequences();
        return sequenceRepository.findAll();
    }


    public long getSequenceCount() {
        return sequenceRepository.count();
    }


    public long getInAlignmetnProcessSequenceCount() {
        return sequencesProcessingStatusRepository.count();
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
        return sequencesProcessingStatusRepository.findById(id);
    }


    public List<SequencesProcessingStatus> findAllSequencesProcessingStatuses() {
        return sequencesProcessingStatusRepository.findAll();
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
        Pageable topTen = PageRequest.of(0, 10);
        List<Sequence> downloadedSequences = sequenceRepository.findByStatus(Sequence.STATUS.DOWNLOADED, topTen);
        //TODO - better and all? Maybe introduce min/max
        if (downloadedSequences != null && downloadedSequences.size() >= alignmentSequencesCount) {
            return alignSequences(downloadedSequences.subList(0, alignmentSequencesCount));
        }
        return AlignSubmitResult.emptyAlignSubmitResult();
    }

    public AlignSubmitResult alignSequences(List<Sequence> downloadedSequences) {
        Map<Sequence, List<Sequence>> sequencesToAlign = Map.of(this.getReferenceForAlignment_mock(),
                downloadedSequences);

        //TODO - there may be just single "bunch" to align with 1 or 2 reference sequence
        //from sequences to align create from first a sequence entity for alignment
        List<SequencesProcessingStatus> sequencesProcessingStatuses = sequencesToAlign.entrySet()
                .stream()
                .map(entry -> SequencesProcessingStatus.builder()
                        .referenceSequence(
                                new ReferenceSequence(entry.getKey()))
                        .rawSequences(
                                entry.getValue().stream()
                                        .map(sequence ->
                                                BareSequenceWithAccver.builder()
                                                        .accver(sequence.getAccver())
                                                        .bareSequence(sequence.getSequence())
                                                        .build())
                                        .collect(Collectors.toList())
                        )
                        .oldestSequenceDownloadedOn(Utils.getMinDate(entry.getValue()).getRecordCreatedOn())
                        .youngestSequenceDownloadedOn(Utils.getMaxDate(entry.getValue()).getRecordCreatedOn())
                        //                                .alignJobId("not_started")
                        .status(Sequence.STATUS.TO_BE_ALIGNED)
                        .rawSequenceCount(entry.getValue().size())
                        .build())
                .map(sequencesProcessingStatusRepository::save)
                .collect(Collectors.toList());
        //.findFirst();

        //TODO - why tuple? Handle errors!
        //now create alignment DTO and submit the alignment process
        List<SequencesProcessingStatus> submitAlignments = sequencesProcessingStatuses.stream()
                .map(sps -> Pair.of(sps,
                        AlignDto.builder()
                                .format(jobType)
                                .email(email)
                                .addSequence(BareSequenceWithAccver.builder()
                                                .accver(sps.getReferenceSequence().getAccver())
                                                .bareSequence(sps.getReferenceSequence().getSequence())
                                                .build()
                                        )
                                .sequences(
                                        sps.getRawSequences())
                                .build()))
                .map(sequenceDtoPair -> Pair.of(
                        sequenceDtoPair.getLeft(),
                        alignerService.alignWithSingleReference(
                                sequenceDtoPair.getRight())))
                .peek(sequenceJobIdPair -> sequenceJobIdPair.getLeft()
                        .setAlignJobId(
                                sequenceJobIdPair.getRight()))
                .peek(sequenceJobIdPair -> sequenceJobIdPair.getLeft()
                        .setStatus(
                                Sequence.STATUS.ALIGNING))
                .map(sequenceJobIdPair -> sequencesProcessingStatusRepository.save(
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
        String jobStatus;
    }


    //TODO - separate check job and finished job proccessing
    @Transactional
    public @NotNull List<SequencesProcessingStatus> checkAlignmentDoneAndReturn() {
        List<SequencesProcessingStatus> aligningSequences = sequencesProcessingStatusRepository.findByStatus(Sequence.STATUS.ALIGNING);
        return Utils.<SequencesProcessingStatus>createLoggingStream(aligningSequences,
                        (SequencesProcessingStatus as) -> String.format(
                                "alignment check, job ID %s, prior status %s, accvers %s",
                                as.getAlignJobId(),
                                as.getStatus(),
                                Utils.accverFromSequencesToString(as.getRawSequences())))
                .map(sequenceProcessing -> new AlignerChecker(sequenceProcessing,
                        alignerService.checkJobStatus(sequenceProcessing.getAlignJobId())))
                .peek(alignerChecker -> System.out.println(String.format(
                        "Alignment checker/updater. For sequences %s aligning in a job id %s job the result is %s",
                        Utils.accverFromSequencesToString(alignerChecker.getSequencesProcessingStatus()
                                .getRawSequences()),
                        alignerChecker.getSequencesProcessingStatus().getAlignJobId(),
                        alignerChecker.getJobStatus())))
                .filter(alignerChecker -> StringUtils.endsWith(alignerChecker.getJobStatus(), EBI_ALIGNER_JOB_FINISHED)) //TODO - um, aligner implementation  details ;|
                .peek(alignerChecker -> alignerChecker.getSequencesProcessingStatus()
                        .setStatus(Sequence.STATUS.ALIGNED_NOT_DOWNLOADED))
                //set the status in "main" sequence collection - this is to be refactored, main collection should be kept intact
                .peek(alignerChecker -> alignerChecker.getSequencesProcessingStatus()
                        .getRawSequences().stream()
                            .map(BareSequenceWithAccver::getAccver)
                            .map(sequenceRepository::findByAccver)
                            .forEach(sequence -> sequence.setStatus(Sequence.STATUS.ALIGNED_NOT_DOWNLOADED)))
                .map(alignerChecker -> sequencesProcessingStatusRepository.save(alignerChecker.getSequencesProcessingStatus()))
                .peek(s -> System.out.println("really SAVED"))
                .collect(Collectors.toList());
    }


    Integer[] build(SequencesProcessingStatus inputSequences, String alignedSequences) {
        final StringBuilder alignedSequencesSb = new StringBuilder(alignedSequences);
        List<Integer> indexes = inputSequences.getRawSequences().stream()
                .map(BareSequenceWithAccver::getAccver)
                .map(alignedSequencesSb::indexOf)
                .collect(Collectors.toList());

        return indexes.toArray(new Integer[indexes.size()]);
    }

    @Transactional
    public List<AlignedSequences> processAlignedSequences() {
        List<SequencesProcessingStatus> alignedSequencesStatus = sequencesProcessingStatusRepository.findByStatus(Sequence.STATUS.ALIGNED_NOT_DOWNLOADED);

        List<AlignedSequences> alignedSequences = alignedSequencesStatus.stream()
                .map(sequenceProcessingStatusAligned -> Pair.of(sequenceProcessingStatusAligned, alignerService.getJobResult(sequenceProcessingStatusAligned.getAlignJobId())))
                .map(statusAlignmentPair -> Pair.of(statusAlignmentPair.getLeft(), Parsers.parseAlignedSequences(statusAlignmentPair.getRight(), statusAlignmentPair.getLeft())))
                .map(this::postProcessAndSaveAlignedSequences)
                .collect(Collectors.toList());
        return alignedSequences;
    }

    /**
     *
     * @param statusAlignedPair
     * @return
     */
    private AlignedSequences postProcessAndSaveAlignedSequences(Pair<SequencesProcessingStatus, AlignedSequences> statusAlignedPair) {
        var savedAlignedSequences = alignedSequenceRepository.save(statusAlignedPair.getRight());
        statusAlignedPair.getLeft().setStatus(Sequence.STATUS.ALIGNED_DOWNLOADED);
        sequencesProcessingStatusRepository.save(statusAlignedPair.getLeft());
        statusAlignedPair.getRight().getAlignedSequences()
                .stream()
                .map(BareSequenceWithAccver::getAccver)
                .map(sequenceRepository::findByAccver)
                .peek(sequence -> sequence.setStatus(Sequence.STATUS.ALIGNED_DOWNLOADED))
                .forEach(sequenceRepository::save);//TODO - not to have status in main sequence collection!!
        return savedAlignedSequences;
    }


    ///  aligned sequences ///
    @Deprecated //to be replaced withreal reference, maybe in a file too?
    public Sequence getReferenceForAlignment_mock() {
        //return sequenceRepository.findByAccver("KC899669.1").get(0);
        return referenceSequenceRepository.findAll().get(0);
    }


    public List<AlignedSequences> findAlignedSequences(LocalDateTime startDate, LocalDateTime endDate) {
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
    public List<AlignedSequences> saveAlignedSequences(List<AlignedSequences> sequence) {
        return alignedSequenceRepository.saveAll(sequence);
    }


    @Deprecated //just for example sequences. Not usable so far
    public AlignedSequences saveAlignedSequence(AlignedSequences sequence) {
        return alignedSequenceRepository.save(sequence);
    }


    @Transactional
    public int downloadAndSaveNewSequences(int downloaderPeriodDays) {
        LocalDate dateFrom = getLstSequenceDownloadDate();
        LocalDate dateTo = dateFrom.plusDays(downloaderPeriodDays);
        System.out.println("date to start from - to: " + dateFrom + " - " + dateTo);
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
     * @return
     */
    private int processNewDownloadedSequences(Mono<List<Sequence>> sequences) {
        //TODO - move status setting over here
        //        sequences.map(sequenceRepository::saveAll);
        List<Sequence> savedSequences = sequences.block()
                .stream()
                .peek(sequence -> sequence.setStatus(Sequence.STATUS.DOWNLOADED))
                .filter(sequence -> sequenceRepository.findByAccver(
                        sequence.getAccver()) == null)
                .map(sequenceRepository::save)
                .collect(Collectors.toList());

        System.out.println("savedSequences.size(): " + savedSequences.size());
        System.out.println("sequenceRepository.findAll().size(): " + sequenceRepository.findAll().size());
        return savedSequences != null ? savedSequences.size() : 0;
    }


    @Transactional
    public Mono<List<Sequence>> downloadSequencesFromTo(LocalDate downloadedDateTimeFrom,
                                                        LocalDate downloadedDateTimeTo) {
        return rawSequenceDownloader.downloadSequencesFromTo(downloadedDateTimeFrom, downloadedDateTimeTo)
                .map(Utils::mapperNotYetWorkingForMe)
                .map(sequences -> {
                    sequences.stream().forEach(sequenceRepository::save);
                    return sequences;
                });
    }


//    @Transactional
//    public Mono<NcbiRawSequenceDownloader.EsearchResponse> downloadSequencesFromTo2(LocalDate downloadedDateTimeFrom,
//                                                                                    LocalDate downloadedDateTimeTo) {
//        Mono<NcbiRawSequenceDownloader.EsearchResponse> sequenceTest = rawSequenceDownloader.downloadSequencesFrom2(
//                downloadedDateTimeFrom,
//                downloadedDateTimeTo);
//        return sequenceTest;
//    }


    //TODO - move to distinc service??
    public LocalDate getLstSequenceDownloadDate() {
        //TODO - date when to start with downloads
        //TODO - check if the date is somewhat current and download more often if not - maybe in initilLoad method?
        return sequenceDownloadEventRepository.findFirstByOrderByDownloadTillDesc()
                .map(SequenceDownloadEvent::getDownloadTill)
                .orElse(dateStartDownloading);
//                                              .orElse(LocalDate.of(2021, 1, 1)); //TODO - property for initial load?
    }


    @Transactional(readOnly = true)
    public List<Sequence> getAllDownloadedSequences() {
        return sequenceRepository.findByStatus(Sequence.STATUS.DOWNLOADED);
    }
}
