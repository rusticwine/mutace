package org.ryboun.sisa.hemagglutinin.mutations.service;

//import com.fasterxml.jackson.dataformat.xml.XmlMapper;

import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.*;
import org.ryboun.sisa.hemagglutinin.mutations.Utils;
import org.ryboun.sisa.hemagglutinin.mutations.model.AlignedSequences;
import org.ryboun.sisa.hemagglutinin.mutations.model.Sequence;
import org.ryboun.sisa.hemagglutinin.mutations.model.SequencesProcessingStatus;
import org.ryboun.sisa.hemagglutinin.mutations.service.rest.EbiAligner;
import org.ryboun.sisa.module.alignment.AlignDto;
import org.ryboun.sisa.module.alignment.Aligner;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import reactor.core.publisher.Mono;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;


@DataMongoTest//(includeFilters = @ComponentScan.Filter(Service.class))
//@SpringBootTest(classes = Application.class)
//@RunWith(SpringRunner.class)
//@AutoConfigureDataMongo
//@ExtendWith({SpringExtension.class})
//@TestPropertySource("classpath:application.properties")
@TestPropertySource("classpath:application-dev-mock.properties")
//@EnableMongoRepositories(basePackageClasses = SequenceRepository.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
//@Import(SequenceServiceTest.MockConfig.class)
@ActiveProfiles({"dev-mock"})
class SequenceServiceTest {


    @Autowired
//    AlignerServiceMock ma;
    Aligner ma;

    @Autowired
    SequenceService sequenceService;

    private static List<Sequence> savedSequences;

    private static SequencesProcessingStatus downloadedSequences;

    @Value("${alignment.submitJob.email}")
    private String email;
    @Value("${alignment.submitJob.jobType}")
    private String jobType;

    private static final String TEST_SEQUENCES_RAW = "sequences/rawSequences.fasta";

    private static final String TEST_JOB_ID = "mafft-R20220213-162814-0268-85185325-p2m";

    @MockBean

    EbiAligner ebiAligner;

    @BeforeAll
    static void init(@Autowired SequenceService sequenceService) {
//        -Djavax.xml.accessExternalDTD=all

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


    @Test
    @Order(2)
    void downloadSequencesFrom() {
        LocalDate testDateFrom = LocalDate.of(2013, 1, 1);
        LocalDate testDateTo = LocalDate.of(2013, 3, 1);

        Mono<List<Sequence>> sequencesMono = sequenceService.downloadSequencesFromTo(testDateFrom, testDateTo);
        List<Sequence> sequences = sequencesMono.block();

        Assertions.assertEquals(6L, sequences.size(), "There should be 6 sequences downloaded");

//        System.out.println("Are some sequences downloaded?" + sequences != null);
//        if (sequences != null) {
//            System.out.println(sequences.size());
//            Arrays.toString(sequences.toArray());
//        }
    }

    @Test
    @Order(3)
    void findAllSequencesTest() {
        List<Sequence> fs = sequenceService.findAllSequences();
//        fs.stream()
//                .forEach(System.out::println);
        Assertions.assertEquals(6L, sequenceService.getSequenceCount(), "There should be 6 sequences in the main sequence collection");
    }


    @Test
    @Order(4)
    void alignWithSingleReference() throws IOException {
        SequenceService.AlignSubmitResult submitResult = sequenceService.alignSequences();
        System.out.println("alignment submit result: " + submitResult);
    }


    @Test
    @Order(5)
    void testAlign1_checkJobStatus() throws InterruptedException {
        List<SequencesProcessingStatus> alignmentsStatusesJobsFinished = sequenceService.checkAlignmentDoneAndReturn();
        System.out.println(String.format("Aligner jobs updated: %d, \ncontent: %s", alignmentsStatusesJobsFinished.size(), StringUtils.join(alignmentsStatusesJobsFinished, "\n")));
        Assertions.assertEquals( 1L, alignmentsStatusesJobsFinished.size(), "Expected alignment job finished count is 1");
    }


    @Test
    @Order(6)
    void testAlign1_getResult() {
        List<AlignedSequences> alignedSequences = sequenceService.processAlignedSequences();
        System.out.println("aligned sequence count:" + alignedSequences.get(0).getAlignedSequences().size());
        System.out.println("all sequences:" + StringUtils.join(alignedSequences.get(0).getAlignedSequences(), ", "));
    }


//    private List<Sequence> parseSequences(String sequences) {
//        return Arrays.stream(sequences.split(AlignDto.SEQUENCE_SPLITTER))
//                .map(s -> Sequence.builder()
//                        .sequence(s)
//                        .build())
//                .collect(Collectors.toList());
//    }


//    @Test
//    @Order(4)
//    void alignWithSingleReference_former() throws IOException {
//        String sequencesString = loadStringFileFromResources(TEST_SEQUENCES_REFERENCE);
//        List<Sequence> sequences = parseSequences(sequencesString);
//
//        AlignDto alignDto = AlignDto.builder()
//                .email(email)
//                .format(jobType)
//                .sequences(sequences)
//                .build();
//        String result = ma.alignWithSingleReference(alignDto);
//        System.out.println("test result: " + result);
//    }


//    @Test
//    @Order(5)
//    void testAlign1_checkJobStatus() throws InterruptedException {
//        String jobStatus = ma.checkJobStatus(TEST_JOB_ID);
//        System.out.println("JOB STATUS: " + jobStatus);
//        Assertions.assertEquals("FINISHED", jobStatus, "Job should be already finished");
//    }

//    @Test
//    @Order(6)
//    void testAlign1_getResult() {
//        String jobResult = ma.getJobResult(TEST_JOB_ID);
//        Assertions.assertNotNull(jobResult, "There should be some data in job result");
//        jobResult.lines().forEach(l -> System.out.println("LINE: " + l));
//        System.out.println("job result" + System.lineSeparator() + jobResult);
//    }

//    private String loadStringFileFromResources(String filePath) throws IOException {
//        try (BufferedInputStream bis = new BufferedInputStream(this.getClass().getClassLoader()
//                .getResourceAsStream(filePath))) {
//            byte[] contents = new byte[1024];
//
//            int bytesRead = 0;
//            String strFileContents = null;
//            while((bytesRead = bis.read(contents)) != -1) {
//                strFileContents += new String(contents, 0, bytesRead);
//            }
//
//            return strFileContents.substring(4);
//        }
//    }
}