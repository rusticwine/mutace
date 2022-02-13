package org.ryboun.sisa.hemagglutinin.mutations.service;

//import com.fasterxml.jackson.dataformat.xml.XmlMapper;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.ryboun.sisa.hemagglutinin.mutations.Utils;
import org.ryboun.sisa.hemagglutinin.mutations.model.Sequence;
import org.ryboun.sisa.hemagglutinin.mutations.model.SequencesProcessingStatus;
import org.ryboun.sisa.hemagglutinin.mutations.service.rest.EbiAligner;
import org.ryboun.sisa.module.alignment.AlignDto;
import org.ryboun.sisa.module.alignment.Aligner;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;


@DataMongoTest//(includeFilters = @ComponentScan.Filter(Service.class))
//@RunWith(SpringRunner.class)
//@AutoConfigureDataMongo
@ExtendWith({SpringExtension.class})

//@TestPropertySource("classpath:application.properties")
@TestPropertySource("classpath:application-dev.properties")

//@EnableMongoRepositories(basePackageClasses = SequenceRepository.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
//@ActiveProfiles("test")
class SequenceServiceTest {


    @Autowired
    Aligner ma;

    @Autowired
    SequenceService sequenceService;

    private static List<Sequence> savedSequences;

    private static SequencesProcessingStatus downloadedSequences;

    @Value("${alignment.submitJob.email}")
    private String email;
    @Value("${alignment.submitJob.jobType}")
    private String jobType;

    private static final String TEST_SEQUENCES_RAW = "sequences/rawSequences_test1.fasta";

    private static final String TEST_SEQUENCES_REFERENCE = "sequences/references_test1.fasta";

    private static final String TEST_JOB_ID = "mafft-R20220213-162814-0268-85185325-p2m";

    @Mock
    EbiAligner ebiAligner;


    @BeforeAll
    static void init(@Autowired SequenceService sequenceService) {
        try {
            SequenceServiceForTest.SequenceTest st = loadDbData();
            List<Sequence> sequences = Utils.mapperNotYetWorkingForMe(st);
            savedSequences = sequences
                    .stream()
                    .map(s -> sequenceService.saveSequence(s))
                    .collect(Collectors.toList());
        } catch (JAXBException e) {
            e.printStackTrace();
        }
    }

    private static SequenceServiceForTest.SequenceTest loadDbData() throws JAXBException {

        JAXBContext context = JAXBContext.newInstance(SequenceServiceForTest.SequenceTest.class);
        InputStream is = SequenceServiceTest.class.getClassLoader().getResourceAsStream(
                "sequences1Hemagglutinin.xml");
        SequenceServiceForTest.SequenceTest st = (SequenceServiceForTest.SequenceTest) context.createUnmarshaller()
                .unmarshal(is);

        return st;
    }
//
//    private static List<Sequence> mapperNotYetWorkingForMe(SequenceTest sequenceTest) {
//        return sequenceTest.getSequenceList()
//                .stream()
//                .map(st -> Sequence
//                        .builder()
//                        .sequence(st.getSequence())
//                        .organism(st.getOrganism())
//                        .build())
//                .collect(Collectors.toList());
//    }

//    @Test
    @Order(1)
    void findAllSequencesTest() {
        System.out.println("STARTING");

        List<Sequence> fs = sequenceService.findAllSequences();

        System.out.println("JUST BEFORE, pulled from repo:");
        fs.stream()
                .forEach(System.out::println);
        System.out.println("loaded sequence count: " + (fs == null ? 0 : fs.size()));

        System.out.println("status processing start");
//        SequencesProcessingStatus sequenceProcessingStatusesStored = sequenceService.addDownloadedSequences(fs);
//        System.out.println("stored status processing count: " + (sequenceProcessingStatusesStored == null
//                ? 0
//                : sequenceProcessingStatusesStored.getRawSequences().size()));

//        Optional<SequencesProcessingStatus > sequenceProcessingStatusesConfirmed = sequenceService
//                .findSequenceProcessingStatusById(sequenceProcessingStatusesStored.getId());
//        sequenceProcessingStatusesConfirmed.ifPresentOrElse(s ->
//                System.out.println("loaded statuses processing count: " + sequenceProcessingStatusesStored.getRawSequences().size()),
//                () -> System.out.println("No statuses were loaded (or saved)"));

        long count = sequenceService.getSequenceCount();
        long justAligningCount = sequenceService.getInAlignmetnProcessSequenceCount();
        System.out.println("count: " + count + ", justAligningCount: " + justAligningCount);

        System.out.println("ending");
    }

    @Test
    @Order(4)
    void alignWithSingleReference() throws IOException {
        String sequencesString = loadStringFileFromResources(TEST_SEQUENCES_REFERENCE);

        List<Sequence> sequences = parseSequences(sequencesString);

        AlignDto alignDto = AlignDto.builder()
                .email(email)
                .format(jobType)
                .sequences(sequences)
                .build();

        String result = ma.alignWithSingleReference(alignDto);

        System.out.println("test result: " + result);
    }

    @Test
    @Order(5)
    void testAlign1_checkJobStatus() throws InterruptedException {
        String jobStatus = ma.checkJobStatus(TEST_JOB_ID);
        System.out.println("JOB STATUS: " + jobStatus);
        Assertions.assertEquals("FINISHED", jobStatus, "Job should be already finished");
    }

    @Test
    @Order(6)
    void testAlign1_getResult() {
        String jobResult = ma.getJobResult(TEST_JOB_ID);
        Assertions.assertNotNull(jobResult, "There should be some data in job result");
        jobResult.lines().forEach(l -> System.out.println("LINE: " + l));
        System.out.println("job result" + System.lineSeparator() + jobResult);
    }


    private List<Sequence> parseSequences(String sequences) {
        return Arrays.stream(sequences.split(AlignDto.SEQUENCE_SPLITTER))
                .map(s -> Sequence.builder()
                        .sequence(s)
                        .build())
                .collect(Collectors.toList());
    }

    private String loadStringFileFromResources(String filePath) throws IOException {
        try (BufferedInputStream bis = new BufferedInputStream(this.getClass().getClassLoader()
                .getResourceAsStream(filePath))) {
            byte[] contents = new byte[1024];

            int bytesRead = 0;
            String strFileContents = null;
            while((bytesRead = bis.read(contents)) != -1) {
                strFileContents += new String(contents, 0, bytesRead);
            }

            return strFileContents.substring(4);
        }
    }
}