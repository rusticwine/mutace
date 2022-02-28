package org.ryboun.sisa.hemagglutinin.mutations.service;

import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.Data;
import lombok.Getter;
import org.apache.commons.io.IOUtils;
import org.ryboun.sisa.hemagglutinin.mutations.Utils;
import org.ryboun.sisa.hemagglutinin.mutations.model.AlignedSequence;
import org.ryboun.sisa.hemagglutinin.mutations.model.Sequence;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.text.DateFormat;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class SequenceServiceForTest {


    @Autowired
    SequenceService sequenceService;

    //////////////////////////////////////////////
    //////////////////////////////////////////////
    /////           test related             /////
    //////////////////////////////////////////////
    //////////////////////////////////////////////

//    @PostConstruct //not to be run anymore
    @Deprecated
    void init() {
        try {
            SequenceTest st = loadDbData();
            List<Sequence> sequences = Utils.mapperNotYetWorkingForMe(st);
            List<Sequence> savedSequences = sequences
                    .stream()
                    .map(s -> sequenceService.saveSequence(s))
                    .collect(Collectors.toList());

//            SequencesProcessingStatus downloadedSequences = addDownloadedSequences(savedSequences);

        } catch (JAXBException e) {
            e.printStackTrace();
        }

        try {
            loadAlignedSequences();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private SequenceTest loadDbData() throws JAXBException {

        JAXBContext context = JAXBContext.newInstance(SequenceTest.class);
        InputStream is = this.getClass().getClassLoader().getResourceAsStream(
                "sequences1Hemagglutinin.xml");
        SequenceTest st = (SequenceTest) context.createUnmarshaller()
                .unmarshal(is);

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
        sequenceService.saveAlignedSequence(Arrays.asList(alignments));
    }

    @XmlRootElement(name = "TSeqSet")
    @XmlAccessorType(XmlAccessType.FIELD)
    @Data
    public static class SequenceTest {

        @XmlElement(name = "TSeq")
        private List<SequenceTest.SequenceT2> sequenceList;

        @XmlRootElement(name = "TSeq")
//    @Data
        @Getter
        public static class SequenceT2 {

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
