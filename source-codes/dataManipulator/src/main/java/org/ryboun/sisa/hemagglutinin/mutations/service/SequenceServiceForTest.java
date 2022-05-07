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
import org.ryboun.sisa.hemagglutinin.mutations.model.AlignedSequence;
import org.ryboun.sisa.hemagglutinin.mutations.model.ReferenceSequence;
import org.ryboun.sisa.hemagglutinin.mutations.model.Sequence;
import org.ryboun.sisa.hemagglutinin.mutations.repository.ReferenceSequenceRepository;
import org.springframework.beans.factory.annotation.Autowired;
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


    //not to be run anymore
    //    @Deprecated
    @PostConstruct
    void init() {
        try {
            SequenceTestable st = loadDbData();
            List<Sequence> sequences = Utils.mapperNotYetWorkingForMe(st);
            List<Sequence> savedSequences = sequences.stream()
                                                     .map(s -> sequenceService.saveSequence(s))
                                                     .collect(Collectors.toList());

            //            SequencesProcessingStatus downloadedSequences = addDownloadedSequences(savedSequences);

            List<AlignedSequence> testAlignedSequences = loadAlignedNormalizedSequences();
            System.out.println("number of test aligned sequences: " + testAlignedSequences.size());
        } catch (JAXBException | IOException e) {
            e.printStackTrace();
        }

        //        try {
        //            loadAlignedSequences();
        //        } catch (IOException e) {
        //            e.printStackTrace();
        //        }
    }


    private SequenceTestGenepept loadDbData() throws JAXBException {

        return loadTestSequencesInGenepept();
    }


    private SequenceTest loadTestSequencesInFasta() throws JAXBException {
        JAXBContext context = JAXBContext.newInstance(SequenceTest.class);
        InputStream is = this.getClass().getClassLoader().getResourceAsStream("sequences1Hemagglutinin.xml");
        SequenceTest st = (SequenceTest) context.createUnmarshaller().unmarshal(is);

        return st;
    }

    private SequenceTestGenepept loadTestSequencesInGenepept() throws JAXBException {
        JAXBContext context = JAXBContext.newInstance(SequenceTestGenepept.class);
        InputStream is = this.getClass().getClassLoader().getResourceAsStream("sequences1HemagglutininGenepept.xml");
        SequenceTestGenepept st = (SequenceTestGenepept) context.createUnmarshaller().unmarshal(is);

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


    public List<AlignedSequence> loadAlignedNormalizedSequences() throws IOException {
        String NEW_SEQUENCE_MARKER = ">New|";

        InputStream is = this.getClass().getClassLoader().getResourceAsStream("vysledny_alignment_mafft_v1.fasta");
        BufferedReader br = new BufferedReader(new InputStreamReader(is));
        StringBuilder sb = new StringBuilder();

        String line;
        while ((line = br.readLine()) != null) {
            sb.append(line.replace("\r", "").replace("\n", ""));
        }
        String normalizedAlignedSequencesStr = sb.toString();
        String[] normalizedAlignedSequences = StringUtils.splitByWholeSeparator(normalizedAlignedSequencesStr,
                                                                                NEW_SEQUENCE_MARKER);

        String[] referenceSequenceParameters = StringUtils.split(normalizedAlignedSequences[0], "[]");

        String alignedReferenceSequence = referenceSequenceParameters[2].trim();
        String rawReferenceSequence = StringUtils.remove(alignedReferenceSequence,"-");
        ReferenceSequence referenceSequence = ReferenceSequence.builder()
                                                               .accver(referenceSequenceParameters[0].trim())
                                                               .organism(referenceSequenceParameters[1].trim())
                                                               .sequence(rawReferenceSequence)
                                                               .build();

        referenceSequenceRepository.save(referenceSequence);

        int[] positions = Utils.getAlignedPositions(rawReferenceSequence, alignedReferenceSequence);

        LocalDateTime alignDateTimeNow = LocalDateTime.now();
        LocalDateTime downloadDateTime = alignDateTimeNow.minusDays(1);

        AtomicInteger hoursDeductor = new AtomicInteger(0);

        final AtomicInteger counter = new AtomicInteger(0); //so ugly
        List<Sequence> sequences = IntStream.range(1, normalizedAlignedSequences.length)
                                            .mapToObj(i -> normalizedAlignedSequences[i])
                                            .map(sequence -> sequence.split("[\\[\\]]"))
                                            .map(this::<Sequence>createSequenceFromStrings)
                                            .map(sequenceService::saveSequence) //use saved sequence for AlignedSequence creation
                                            .collect(Collectors.toList());

        int BATCH = 100;
        List<AlignedSequence> alignedSequences = IntStream.range(0, (sequences.size() + BATCH - 1) / BATCH)
                 .mapToObj(i -> sequences.subList(i * BATCH, Math.min(sequences.size(), (i + 1) * BATCH)))
                 .map(sequenceBatch -> {
                     List<AlignedSequence.Alignment> alignments = sequenceBatch.stream()
                                                                       .map(sequence -> AlignedSequence.Alignment.builder()
                                                                                                                 .accver(sequence.getAccver())
                                                                                                                 .alignedSequence(
                                                                                                                         sequence.getSequence())
                                                                                                                 .build())
                                                                       .collect(Collectors.toList());

                     return AlignedSequence.builder()
                                                          .reference(AlignedSequence.ReferenceSequence.builder()
                                                                                                      .accver(referenceSequence.getAccver())
                                                                             .positions(positions)
                                                                             .rawReferenceSequence(rawReferenceSequence)
                                                                                                      .alignedReferenceSequence(
                                                                                                              alignedReferenceSequence)
                                                                                                      .build())
                                                          .alignment(alignments)
                                                          .downloadDate(downloadDateTime.minusHours(
                                                                  hoursDeductor.incrementAndGet() * 3))
                                                          .alignDate(alignDateTimeNow.minusHours(hoursDeductor.incrementAndGet())) //not proud of this
                                                          .build();
                 })
                .collect(Collectors.toList());

        sequenceService.saveAlignedSequences(alignedSequences);

        return alignedSequences;
    }


    private Sequence createSequenceFromStrings(String[] sequence) {
        return Sequence.builder() //store the sequence, though alrady aligned, just to have the reference in main collections
                       .accver(StringUtils.remove(sequence[0], "hemagglutinin").trim())
                       .organism(sequence[1].trim())
                       .sequence(sequence[2].trim())
                       .build();
    }


    @XmlRootElement(name = "GBSet")
    @XmlAccessorType(XmlAccessType.FIELD)
    @Data
    public static class SequenceTestGenepept implements SequenceTestable {

        @XmlElement(name = "GBSeq")
        @Getter
        private List<SequenceTestableInner> sequenceList;

        @XmlRootElement(name = "GBSeq")
        @Getter
        public static class SequenceT2 implements SequenceTestableInner {

            @XmlElement(name = "GBSeq_sequence")
            private String sequence;

            @XmlElement(name = "GBSeq_organism")
            private String organism;

            @XmlElement(name = "GBSeq_feature/GBFeature/GBFeature_quals/GBQualifier/GBQualifier_value")
            private String taxid;

            @XmlElement(name = "GBSeq_accession-version")
            private String accver;
        }
    }

    public interface SequenceTestable {

        List<SequenceTestableInner> getSequenceList();

        public interface SequenceTestableInner {
            String getSequence();
            String getOrganism();
            String getTaxid();
            String getAccver();
        }
    }

    @XmlRootElement(name = "TSeqSet")
    @XmlAccessorType(XmlAccessType.FIELD)
    @Data
    public static class SequenceTest implements SequenceTestable {

        @XmlElement(name = "TSeq")
        private List<SequenceTestableInner> sequenceList;

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
