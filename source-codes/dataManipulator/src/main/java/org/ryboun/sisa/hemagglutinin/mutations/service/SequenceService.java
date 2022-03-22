package org.ryboun.sisa.hemagglutinin.mutations.service;

import java.util.stream.Collectors;
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
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;


@Service
public class SequenceService {


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
     * checks the downloaded sequences and tries to upload it.
     * Idea is to run it regularly, like once a day
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
    public void alignSequences() {
        List<Sequence> downloadedSequences = sequenceRepository.findByStatus(
                Sequence.STATUS.DOWNLOADED);
        //TODO - find reference sequence for each sequence
        Map<Sequence, List<Sequence>> sequencesToAlign = downloadedSequences.stream()
                .map(sequence -> Pair.of(this.getReferenceForAlignment_mock(), sequence))
                .<Map<Sequence, List<Sequence>>>reduce(
                        new HashMap<Sequence, List<Sequence>>(),
                        (resultMap, item) -> {
                            resultMap.putIfAbsent(item.getKey(), new ArrayList<>());
                            resultMap.get(item.getKey()).add(item.getValue());
                            return resultMap;
                        },
                        (resultMap1, resultMap2) -> {
                            resultMap1.putAll(resultMap2);
                            return resultMap1;
                        });

        //from sequences to align create from first a sequence entity for alignment
        //Optional<SequencesProcessingStatus> sequencesProcessingStatus =
        List<SequencesProcessingStatus> sequencesProcessingStatuses =
                sequencesToAlign.entrySet().stream()
                        .map(entry -> SequencesProcessingStatus.builder()
                                .referenceSequence(entry.getKey())
                                .rawSequences(entry.getValue())
                                .alignJobId("not_started")
                                .status(Sequence.STATUS.TO_BE_ALIGNED)
                                .build())
                        .map(sequencesProcessingRepository::save)
                        .collect(Collectors.toList());
                        //.findFirst();

        sequencesProcessingStatuses.stream()
                .map(sps -> AlignDto.builder()
                                    .format(jobType)
                                                .email(email)
                                                .addSequence(sps.getReferenceSequence())
                                                .sequences(sps.getRawSequences())
                                                .build())
                .peek(aligner::alignWithSingleReference)
                                   .forEach(System.out::println);
//                .forEach(sps -> SequencesProcessingStatus::setAlignJobId);

        //launch alinment and get job id from server
//        String alignJobId = aligner.alignWithSingleReference(alignDto);

        //change status of job to being aligned
//        sps.setAlignJobId(alignJobId);


        //if there is an entity for alignment submit it for alignment
        //functional in imperative style?
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
    }


    @Transactional
    public void updateAligningSequences() {
        List<SequencesProcessingStatus> aligningSequences = sequencesProcessingRepository.findByStatus(Sequence.STATUS.ALIGNING);
        aligningSequences.stream()
                .forEach(sequenceProcessing -> {
                    String jobStatus = aligner.getJobResult(sequenceProcessing.getAlignJobId());
                    System.out.println("JOB for ID: " + sequenceProcessing.getAlignJobId() +
                            " has status: " + jobStatus);
                    if (jobStatus.equals("DONE")) {
                        sequenceProcessing.setStatus(Sequence.STATUS.ALIGNED);
                    }
                });
    }

    @Transactional
    public void processAlignedSequences() {
        List<SequencesProcessingStatus> alignedSequences = sequencesProcessingRepository.findByStatus(Sequence.STATUS.ALIGNED);
        alignedSequences.stream()
                .forEach(sequenceProcessingAligned -> {
                    String jobId = sequenceProcessingAligned.getAlignJobId();
                    String alinedSequences = aligner.getJobResult(jobId);
                    //TODO - MAP result to object
                    System.out.println("Aligned sequences: " + alinedSequences);
                    //create AlignedSequence isntance

                    //persist
                    //remove corresponding SequencesProcessingStatus document
                });
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

    public int downloadAndSaveNewSequences(LocalDate downloadedDateTimeFrom, LocalDate downloadedDateTimeTo) {
        //move here some audit - like collection with data: from, to, sequenceCount
//        System.out.println("periodic sequence download");
        int newDownloadedSequnceCount = processNewSequences(downloadSequencesFromTo(downloadedDateTimeFrom, downloadedDateTimeTo));

        SequenceDownloadEvent sequenceDownloadEvent = SequenceDownloadEvent.builder()
                .downloadedOn(LocalDateTime.now())
                .downloadFrom(downloadedDateTimeFrom)
                .downloadTill(downloadedDateTimeTo)
                .downloadedSequenceCount(newDownloadedSequnceCount)
                .build();

        sequenceDownloadEventRepository.save(sequenceDownloadEvent);
        return newDownloadedSequnceCount;
    }

    private int processNewSequences(Mono<List<Sequence>> sequences) {
        //TODO - move status setting over here
//        sequences.map(sequenceRepository::saveAll);
        List<Sequence> savedSequences = sequences.block().stream()
                .peek(sequence -> sequence.setStatus(Sequence.STATUS.DOWNLOADED))
                .filter(sequence -> !CollectionUtils.isEmpty(sequenceRepository.findByAccver(sequence.getAccver())))
                .map(sequenceRepository::save)
                .collect(Collectors.toList());

        return savedSequences != null ? savedSequences.size() : 0;
    }

    @Transactional
    public Mono<List<Sequence>> downloadSequencesFromTo(LocalDate downloadedDateTimeFrom, LocalDate downloadedDateTimeTo) {
        return rawSequenceDownloader
                .downloadSequencesFrom(downloadedDateTimeFrom, downloadedDateTimeTo)
                .map(Utils::mapperNotYetWorkingForMe)
                .map(sequences -> {
                    sequences.stream().forEach(sequenceRepository::save);
                    return sequences;
                });
    }

    @Transactional
    public Mono<RawSequenceDownloader.EsearchResponse> downloadSequencesFromTo2(LocalDate downloadedDateTimeFrom, LocalDate downloadedDateTimeTo) {
        Mono<RawSequenceDownloader.EsearchResponse> sequenceTest = rawSequenceDownloader
                .downloadSequencesFrom2(downloadedDateTimeFrom, downloadedDateTimeTo);
        return sequenceTest;
    }

    //TODO - move to distinc service??
    public LocalDate getLstSequenceDownloadDate() {
        //TODO - date when to start with downloads
        //TODO - check if the date is somewhat current and download more often if not - maybe in initilLoad method?
        return sequenceDownloadEventRepository.findFirstByOrderByDownloadTillDesc()
                .map(SequenceDownloadEvent::getDownloadTill)
                .orElse(LocalDate.of(2021, 1, 1));
    }

    public List<Sequence> getAllDownloadedSequences() {
        return sequenceRepository.findByStatus(Sequence.STATUS.DOWNLOADED);
    }
}
