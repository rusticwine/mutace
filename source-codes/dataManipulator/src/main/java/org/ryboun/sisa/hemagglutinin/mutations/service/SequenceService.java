package org.ryboun.sisa.hemagglutinin.mutations.service;

import org.apache.commons.lang3.tuple.Pair;
import org.ryboun.sisa.hemagglutinin.mutations.model.AlignedSequence;
import org.ryboun.sisa.hemagglutinin.mutations.model.Sequence;
import org.ryboun.sisa.hemagglutinin.mutations.model.SequencesProcessingStatus;
import org.ryboun.sisa.hemagglutinin.mutations.repository.AlignedSequenceRepository;
import org.ryboun.sisa.hemagglutinin.mutations.repository.SequenceRepository;
import org.ryboun.sisa.hemagglutinin.mutations.repository.SequencesProcessingRepository;
import org.ryboun.sisa.module.alignment.AlignDto;
import org.ryboun.sisa.module.alignment.Aligner;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;


@Service
public class SequenceService {


    @Value("${alignment.submitJob.email}")
    private String email;
    @Value("${alignment.submitJob.jobType}")
    private String jobType;


    @Autowired
    SequenceRepository sequenceRepository;

    @Autowired
    AlignedSequenceRepository alignedSequenceRepository;

    @Autowired
    SequencesProcessingRepository sequencesProcessingRepository;

    @Autowired
    Aligner aligner;

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

    @Transactional
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
    @Transactional
    public void alignSequences() {
        List<Sequence> downloadedSequences = sequenceRepository.findByStatus(
                Sequence.STATUS.DOWNLOADED);
        //TODO - find reference sequence for each sequence
        Map<Sequence, List<Sequence>> sequencesToAlign = downloadedSequences.stream()
                .map(sequence -> Pair.of(this.getReferenceForAlignment_mock(sequence), sequence))
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
        Optional<SequencesProcessingStatus> sequencesProcessingStatus =
                sequencesToAlign.entrySet().stream()
                        .map(entry -> SequencesProcessingStatus.builder()
                                .referenceSequence(entry.getKey())
                                .rawSequences(entry.getValue())
                                .alignJobId("not_started")
                                .status(Sequence.STATUS.TO_BE_ALIGNED)
                                .build())
                        .findFirst();

        //if there is an entity for alignment submit it for alignment
        //functional in imperative style?
        sequencesProcessingStatus.ifPresent(sps -> {
            //aligning status entity changed to dto for alignment request
            AlignDto alignDto = AlignDto.builder()
                    .format(jobType)
                    .email(email)
                    .addSequence(sps.getReferenceSequence())
                    .sequences(sps.getRawSequences())
                    .build();

            //launch alinment and get job id from server
            String alignJobId = aligner.alignWithSingleReference(alignDto);
            //change status of job to being aligned
            sps.setAlignJobId(alignJobId);
        });
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
    private Sequence getReferenceForAlignment_mock(Sequence sequence) {
        return sequenceRepository.findByAccver("KC899669.1").get(0);
    }

    public List<AlignedSequence> findAllAlignedSequences() {
        return alignedSequenceRepository.findAll();
    }

//    @Transactional
//    public AlignedSequence saveAlignedSequence(List<AlignedSequence> sequence) {
//        return alignedSequenceRepository.save(sequence.get(0));
//    }
    @Transactional
    public List<AlignedSequence> saveAlignedSequence(List<AlignedSequence> sequence) {
        return alignedSequenceRepository.saveAll(sequence);
    }


}
