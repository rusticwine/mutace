package org.ryboun.sisa.hemagglutinin.mutations.service;

import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.Data;
import lombok.Getter;
import org.apache.commons.io.IOUtils;
import org.ryboun.sisa.hemagglutinin.mutations.dto.SequenceTestable;
import org.ryboun.sisa.hemagglutinin.mutations.model.AlignedSequences;
import org.ryboun.sisa.hemagglutinin.mutations.repository.ReferenceSequenceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;


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
        AlignedSequences[] alignments = objectMapper.readValue(json, AlignedSequences[].class);
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
