package org.ryboun.sisa.hemagglutinin.mutations.model;

import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import lombok.Data;


@XmlRootElement(name = "TSeqSet")
@XmlAccessorType(XmlAccessType.FIELD)
@Data
public class SequenceTest {

    @XmlElement(name = "TSeq")
    private List<SequenceT2> sequenceList;

    @XmlRootElement(name = "TSeq")
//    @Data
    public static class SequenceT2 {

        @XmlElement(name = "TSeq_sequence")
        private String sequence;
//
        @XmlElement(name = "TSeq_orgname")
        private String organism;
    }
}
