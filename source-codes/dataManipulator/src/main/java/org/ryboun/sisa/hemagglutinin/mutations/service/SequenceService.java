package org.ryboun.sisa.hemagglutinin.mutations.service;

import lombok.Data;
import lombok.Getter;
import org.ryboun.sisa.hemagglutinin.mutations.model.AlignedSequence;
import org.ryboun.sisa.hemagglutinin.mutations.model.Sequence;
import org.ryboun.sisa.hemagglutinin.mutations.model.SequencesProcessingStatus;
import org.ryboun.sisa.hemagglutinin.mutations.repository.ReactiveSequenceRepository;
import org.ryboun.sisa.hemagglutinin.mutations.repository.SequenceRepository;
import org.ryboun.sisa.hemagglutinin.mutations.repository.SequencesProcessingRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.InputStream;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;


@Service
public class SequenceService {

    @Autowired
    ReactiveSequenceRepository reactiveSequenceRepository;

    @Autowired
    SequenceRepository sequenceRepository;

    @Autowired
    SequencesProcessingRepository sequencesProcessingRepository;

    public List<Sequence> findAllSequences() {
        return sequenceRepository.findAll();
    }

    public long getSequenceCount() {
        return sequenceRepository.count();
    }

    @Transactional
    public Sequence saveSequence(Sequence sequence) {
        return sequenceRepository.save(sequence);
    }

    public SequencesProcessingStatus addDownloadedSequences(List<Sequence> downloadedSequences) {
        SequencesProcessingStatus sequencesProcessingStatus = SequencesProcessingStatus.builder()
                .sequences(downloadedSequences)
                .status(SequencesProcessingStatus.STATUS.DOWNLOADED)
                .build();

        return sequencesProcessingRepository.save(sequencesProcessingStatus);
    }

    public Optional<SequencesProcessingStatus> findSequenceProcessingStatusById(String id) {
        return sequencesProcessingRepository.findById(id);
    }

    public List<SequencesProcessingStatus> findAllSequencesProcessingStatuses() {
        return sequencesProcessingRepository.findAll();
    }

    ///// test relate

    @PostConstruct
    void init() {
        try {
            SequenceTest st = loadDbData();
            List<Sequence> sequences = mapperNotYetWorkingForMe(st);
            List<Sequence> savedSequences = sequences
                    .stream()
                    .map(s -> saveSequence(s))
                    .collect(Collectors.toList());

            SequencesProcessingStatus downloadedSequences = addDownloadedSequences(savedSequences);

        } catch (JAXBException e) {
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
    private List<Sequence> mapperNotYetWorkingForMe(SequenceTest sequenceTest) {
        AlignedSequence as = null;

        return sequenceTest.getSequenceList()
                .stream()
                .map(st -> Sequence
                        .builder()
                        .sequence(st.getSequence())
                        .organism(st.getOrganism())
                        .taxid(st.getTaxid())
                        .accver(st.getAccver())
                        .build())
                .collect(Collectors.toList());
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
