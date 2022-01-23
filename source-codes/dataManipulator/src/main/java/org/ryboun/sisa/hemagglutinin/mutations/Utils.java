package org.ryboun.sisa.hemagglutinin.mutations;

import org.ryboun.sisa.hemagglutinin.mutations.model.AlignedSequence;
import org.ryboun.sisa.hemagglutinin.mutations.model.Sequence;
import org.ryboun.sisa.hemagglutinin.mutations.service.SequenceServiceForTest;

import java.util.List;
import java.util.stream.Collectors;

public class Utils {

    public static List<Sequence> mapperNotYetWorkingForMe(SequenceServiceForTest.SequenceTest sequenceTest) {
        return sequenceTest.getSequenceList()
                .stream()
                .map(st -> Sequence
                        .builder()
                        .sequence(st.getSequence())
                        .organism(st.getOrganism())
                        .taxid(st.getTaxid())
                        .accver(st.getAccver())
                        .status(Sequence.STATUS.DOWNLOADED)
                        .build())
                .collect(Collectors.toList());
    }
}
