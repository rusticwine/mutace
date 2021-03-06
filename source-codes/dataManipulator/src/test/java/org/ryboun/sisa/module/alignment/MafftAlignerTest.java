package org.ryboun.sisa.module.alignment;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.ryboun.sisa.TestUtils;
import org.ryboun.sisa.hemagglutinin.mutations.model.BareSequenceWithAccver;
import org.ryboun.sisa.hemagglutinin.mutations.model.Sequence;
import org.ryboun.sisa.hemagglutinin.mutations.repository.SequenceRepository;
import org.ryboun.sisa.hemagglutinin.mutations.repository.SequencesProcessingStatusRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.AutoConfigureDataMongo;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Deprecated
@AutoConfigureDataMongo()
@ExtendWith({SpringExtension.class})
//@DataMongoTest(includeFilters = @ComponentScan.Filter(MafftAligner.class))
@EnableMongoRepositories(basePackageClasses = {SequenceRepository.class, SequencesProcessingStatusRepository.class})
//@SpringBootTest(classes = MafftAligner.class)
class MafftAlignerTest {

//    private static final String MAFFT_JOB_ID_PREFIX = "mafft-";

    private static final String TEST_SEQUENCES_RAW = "sequences/rawSequences_test1.fasta";

    private static final String TEST_SEQUENCES_REFERENCE = "sequences/referenceSequences_multiple_noAccver.fasta";

    private static final String TEST_JOB_ID = "mafft-R20211106-102801-0729-88936282-p1m";

    @Autowired
    Aligner ma;
//    MafftAligner ma = new MafftAligner();


//    @Test
    void alignWithSingleReference() throws IOException {
        String sequencesString = TestUtils.loadStringFileFromResources(TEST_SEQUENCES_REFERENCE);

        List<Sequence> sequences = parseSequences(sequencesString);

        AlignDto alignDto = AlignDto.builder()
                .email("valerius@centrum.cz")
                .format("fasta")
                .sequences(sequences.stream()
                        .map(seq -> BareSequenceWithAccver.builder()
                                .accver(seq.getAccver())
                                .bareSequence(seq.getSequence())
                                .build())
                        .collect(Collectors.toList()))
                .build();

        String result = ma.alignWithSingleReference(alignDto);

        System.out.println("test result: " + result);
    }

    @Test
    void testAlign1_checkJobStatus() {
        String jobStatus = ma.checkJobStatus(TEST_JOB_ID);
        System.out.println("JOB STATUS: " + jobStatus);
        Assertions.assertEquals("FINISHED", jobStatus, "Job should be already finished");
    }

    @Test
    void testAlign1_getResult() {
        String jobResult = ma.getJobResult(TEST_JOB_ID);
        Assertions.assertNotNull(jobResult, "There should be some data in job result");
        System.out.println("job result" + System.lineSeparator() + jobResult);
    }

    private List<Sequence> parseSequences(String sequences) {
        return Arrays.stream(sequences.split(AlignDto.SEQUENCE_SPLITTER))
                .map(s -> Sequence.builder()
                        .sequence(s)
                        .build())
                .collect(Collectors.toList());
    }
}