package org.ryboun.sisa.hemagglutinin.mutations.service;

import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import javax.annotation.PostConstruct;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import lombok.Data;
import lombok.Getter;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.ryboun.sisa.hemagglutinin.mutations.Utils;
import org.ryboun.sisa.hemagglutinin.mutations.dto.SequenceGenepeptList;
import org.ryboun.sisa.hemagglutinin.mutations.dto.SequenceTestable;
import org.ryboun.sisa.hemagglutinin.mutations.model.AlignedSequence;
import org.ryboun.sisa.hemagglutinin.mutations.model.ReferenceSequence;
import org.ryboun.sisa.hemagglutinin.mutations.model.Sequence;
import org.ryboun.sisa.hemagglutinin.mutations.repository.ReferenceSequenceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;


@Service
public class SequenceServiceForTest {

    @Autowired
    SequenceService sequenceService;

    @Autowired
    ReferenceSequenceRepository referenceSequenceRepository;

    //////////////////////////////////////////////
    //////////////////////////////////////////////
    /////           test related             /////
    //////////////////////////////////////////////
    //////////////////////////////////////////////

    @Value("${profile.my}")
    String profile;
    @Autowired
    Environment env;
    //not to be run anymore
    //    @Deprecated



    private SequenceTest loadTestSequencesInFasta() throws JAXBException {
        JAXBContext context = JAXBContext.newInstance(SequenceTest.class);
        InputStream is = this.getClass().getClassLoader().getResourceAsStream("sequences1Hemagglutinin.xml");
        SequenceTest st = (SequenceTest) context.createUnmarshaller().unmarshal(is);

        return st;
    }


    private static String readFileFromResources(String fileName) throws IOException {
        return IOUtils.resourceToString(fileName, StandardCharsets.UTF_8);
    }


    //    @Test
    public void loadAlignedSequences() throws IOException {
        ObjectMapper objectMapper = JsonMapper.builder()
                                              .addModule(new JavaTimeModule())
                                              .enable(MapperFeature.ACCEPT_CASE_INSENSITIVE_ENUMS)
                                              .enable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
                                              .build();

        String json = readFileFromResources("/fromTest/sequences/aligned/alignmentMultiple2.json");
        AlignedSequence[] alignments = objectMapper.readValue(json, AlignedSequence[].class);
        sequenceService.saveAlignedSequences(Arrays.asList(alignments));
    }


    @XmlRootElement(name = "TSeqSet")
    @XmlAccessorType(XmlAccessType.FIELD)
    @Data
    public static class SequenceTest implements SequenceTestable {

        @XmlElement(name = "TSeq")
        private List<SequenceTest.SequenceT2> sequenceList;

        @XmlRootElement(name = "TSeq")
        //    @Data
        @Getter
        public static class SequenceT2 implements SequenceTestableInner {

            @XmlElement(name = "TSeq_sequence")
            private String sequence;

            @XmlElement(name = "TSeq_orgname")
            private String organism;

            @XmlElement(name = "TSeq_taxid")
            private String taxid;

            @XmlElement(name = "TSeq_accver")
            private String accver;
        }
    }
}
