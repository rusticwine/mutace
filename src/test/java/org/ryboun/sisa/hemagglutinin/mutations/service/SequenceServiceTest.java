package org.ryboun.sisa.hemagglutinin.mutations.service;

//import com.fasterxml.jackson.dataformat.xml.XmlMapper;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.runner.RunWith;
import org.ryboun.sisa.hemagglutinin.mutations.model.Sequence;
import org.ryboun.sisa.hemagglutinin.mutations.model.SequenceTest;
import org.ryboun.sisa.hemagglutinin.mutations.model.TestSequenceMapper;
import org.ryboun.sisa.hemagglutinin.mutations.repository.SequenceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.core.io.Resource;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.junit4.SpringRunner;
import reactor.core.publisher.Flux;


@DataMongoTest
//@RunWith(SpringRunner.class)
//@SpringBootTest
@ExtendWith({SpringExtension.class})
@EnableMongoRepositories(basePackageClasses = SequenceRepository.class)
class SequenceServiceTest {

    @Autowired
    SequenceService sequenceService;

    @BeforeAll
    public static void initDbData( @Autowired SequenceService sequenceService) throws JAXBException, IOException {
        SequenceTest st = loadDbData();
        System.out.println("IS NULL: " + (st == null));
        //List<Sequence> sequences = TestSequenceMapper.INSTANCE.testSequenceXmlToProductionSequence(st);
//        Sequence testSequence = new Sequence();
//        testSequence.setAlignedSequence("align seq");
//        testSequence.setOriginalSequence("original seq str");
//        testSequence.setOrganism("flue str");
//        testSequence.setProtein("hemagglutinin str");
//
//        sequenceService.saveSequence(testSequence).block();
    }

    @Value("classpath:sequences1Hemagglutinin.xml")
    private Resource resource2;

    @Test
    void findAllSequencesTest() {
        System.out.println("STARTING");

        List<Sequence> fs = sequenceService.findAllSequences();

        System.out.println("JUST BEFORE");
//        fs.subscribe(sequence -> {
//            System.out.println("WITHING ITERATOR");
//            System.out.println(sequence.getAlignedSequence());
//        });
        System.out.println("loaded sequence count: " + (fs == null ? 0 : fs.size()));


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