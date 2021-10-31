package org.ryboun.sisa.hemagglutinin.mutations.service;

//import com.fasterxml.jackson.dataformat.xml.XmlMapper;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.stream.Collectors;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.ryboun.sisa.hemagglutinin.mutations.model.Sequence;
import org.ryboun.sisa.hemagglutinin.mutations.model.SequenceTest;
import org.ryboun.sisa.hemagglutinin.mutations.model.SequencesProcessingStatus;
import org.ryboun.sisa.hemagglutinin.mutations.repository.SequenceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.core.io.Resource;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;
import org.springframework.test.context.junit.jupiter.SpringExtension;


@DataMongoTest
//@RunWith(SpringRunner.class)
//@SpringBootTest
@ExtendWith({SpringExtension.class})
@EnableMongoRepositories(basePackageClasses = SequenceRepository.class)
class SequenceServiceTest {

    @Autowired
    SequenceService sequenceService;

    private static List<Sequence> savedSequences;

    @BeforeAll
    static void init(@Autowired SequenceService sequenceService) {
        try {
            SequenceTest st = loadDbData();
            System.out.println("IS NULL: " + (st == null));
            List<Sequence> sequences = mapperNotYetWorkingForMe(st);
            savedSequences = sequences
                    .stream()
                    .map(s -> sequenceService.saveSequence(s))
                    .collect(Collectors.toList());
        } catch (JAXBException e) {
            e.printStackTrace();
        }
    }

    private static List<Sequence> mapperNotYetWorkingForMe(SequenceTest sequenceTest) {
        return sequenceTest.getSequenceList()
                .stream()
                .map(st -> Sequence
                        .builder()
                        .originalSequence(st.getSequence())
                        .organism(st.getOrganism())
                        .build())
                .collect(Collectors.toList());
    }

    @Value("classpath:sequences1Hemagglutinin.xml")
    private Resource resource2;

    @Test
    void findAllSequencesTest() {
        System.out.println("STARTING");

        List<Sequence> fs = sequenceService.findAllSequences();

        System.out.println("JUST BEFORE, pulled from repo:");
        fs.stream()
                .forEach(System.out::println);
//        fs.subscribe(sequence -> {
//            System.out.println("WITHING ITERATOR");
//            System.out.println(sequence.getAlignedSequence());
//        });
        System.out.println("loaded sequence count: " + (fs == null ? 0 : fs.size()));

        System.out.println("status processing start");
        List<SequencesProcessingStatus>  sequenceProcessingStatusesStored = sequenceService.addDownloadedSequences(fs);
        System.out.println("stored status processing count: " + (sequenceProcessingStatusesStored == null ? 0 : sequenceProcessingStatusesStored.size()));

        sequenceProcessingStatusesStored = sequenceService.findAllSequencesProcessingStatuses();
        System.out.println("loaded statuses processing count: " + (sequenceProcessingStatusesStored == null ? 0 : sequenceProcessingStatusesStored.size()));

        long count = sequenceService.getSequenceCount();
        System.out.println("count: " + count);

        System.out.println("ending");
    }


//    @Test
    void getSequenceCount() {
    }

    @Value("classpath:sequences1Hemagglutinin.xml")
    private static Resource resource1;

    private static SequenceTest loadDbData() throws JAXBException {

        JAXBContext context = JAXBContext.newInstance(SequenceTest.class);
        InputStream is = SequenceServiceTest.class.getClassLoader().getResourceAsStream(
                "sequences1Hemagglutinin.xml");
        SequenceTest st = (SequenceTest) context.createUnmarshaller()
                             .unmarshal(is);

        return st;
    }
}