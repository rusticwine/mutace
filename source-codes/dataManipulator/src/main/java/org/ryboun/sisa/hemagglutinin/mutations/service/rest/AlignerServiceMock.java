package org.ryboun.sisa.hemagglutinin.mutations.service.rest;

import org.ryboun.sisa.hemagglutinin.mutations.Parsers;
import org.ryboun.sisa.hemagglutinin.mutations.Utils;
import org.ryboun.sisa.hemagglutinin.mutations.model.*;
import org.ryboun.sisa.module.alignment.AlignDto;
import org.ryboun.sisa.module.alignment.Aligner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.validation.constraints.NotEmpty;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Service
@Profile("dev-mock")
public class AlignerServiceMock implements Aligner {

    private static final String REFERENCE_SEQUENCE_FASTA = "sequences/referenceSequence.fasta";
    private static final String TEST_SEQUENCES_RAW_FASTA = "sequences/rawSequences.fasta";
//    private static final String TEST_SEQUENCES_ALIGNED_FASTA = "sequences/alignedSequences_test1.fasta";
private static final String TEST_SEQUENCES_ALIGNED_FASTA = "sequences/alignedSequences_1_1_2013-30_6_2014_279_fasta.xml";
    public static final String ALIGN_JOB_ID = "_pm1_sdfsd_alignJobId";
    private final String ALIGNED_SEQUENCE_SEPARATOR = ">";
    @NotEmpty
    private List<Sequence> sequncesDownloaded;

    private static final AtomicInteger invocationCounter = new AtomicInteger(0);
    @Deprecated
    private AlignedSequences singleBigAlignment;
    @Deprecated
    private List<ReferenceSequence> referenceSequences;

    @Deprecated
    private SequencesProcessingStatus sequencesProcessingStatusMock;

    public final String ALIGNEMENT_ID_MOCK = "alignment_id_mock";

    private String sequencesStr;
    private String sequencesAlignedStr;

    Random random = new Random();


    //map to hold mocked alignment jobs Map<alignment_job_id, accver>
    Map<String, List<String>> alignmentJobs = new ConcurrentHashMap<>();

    Map<String, String> alignedSequencesByAccverMap;

    @PostConstruct
    void initMockData() throws IOException {
        sequencesStr = Utils.loadResourceToString(TEST_SEQUENCES_RAW_FASTA);
        sequncesDownloaded = Parsers.parseFastaSequences(sequencesStr);

        referenceSequences = Parsers.loadReferenceSequenceFromResource();

        sequencesProcessingStatusMock = SequencesProcessingStatus.builder()
                .status(Sequence.STATUS.DOWNLOADED)
                .referenceSequence(referenceSequences.get(0))
                .alidnmentSubmitted(LocalDateTime.now())
                .rawSequences(sequncesDownloaded.stream()
                        .map(seq -> BareSequenceWithAccver.builder()
                                .accver(seq.getAccver())
                                .bareSequence(seq.getSequence())
                                .build())
                        .collect(Collectors.toList()))
                .rawSequenceCount(sequncesDownloaded.size()) //WHY ALL???
                .alignJobId(ALIGNEMENT_ID_MOCK)
                .build();
        sequencesAlignedStr = Utils.loadResourceToString(TEST_SEQUENCES_ALIGNED_FASTA);
        singleBigAlignment = Parsers.parseAlignedSequences(sequencesAlignedStr, sequencesProcessingStatusMock);

        alignedSequencesByAccverMap = singleBigAlignment.getAlignedSequences()
                .stream()
                .collect(Collectors.toMap(BareSequenceWithAccver::getAccver, BareSequenceWithAccver::getBareSequence));
//        alignedSequences.st
    }

    @Override
    public String alignWithSingleReference(AlignDto alignDto) {
        String jobId = ALIGN_JOB_ID + invocationCounter.incrementAndGet();
        alignmentJobs.put(jobId, getAccversFromAlignDtoSequences(alignDto));
        return jobId;
    }

    @Override
    public String getJobResult(String jobId) {
        return alignmentJobs.get(jobId)
                .stream()
                .map(accver -> ALIGNED_SEQUENCE_SEPARATOR + accver + " " + alignedSequencesByAccverMap.get(accver))
                .collect(Collectors.joining(System.lineSeparator()));
    }

    @Override
    public String checkJobStatus(String jobId) {
        return random50Boolean() ? "FINISHED" : "RUNNNING";
    }


    private List<String> getAccversFromAlignDtoSequences(AlignDto alignDto) {
        return alignDto.getSequences()
                .stream()
                .map(BareSequenceWithAccver::getAccver)
                .collect(Collectors.toList());
    }

    private boolean random50Boolean() {
        return random.nextBoolean();
    }
}
