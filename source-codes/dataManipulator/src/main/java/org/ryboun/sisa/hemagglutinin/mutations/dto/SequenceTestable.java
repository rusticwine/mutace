package org.ryboun.sisa.hemagglutinin.mutations.dto;

import java.util.List;

public interface SequenceTestable {

    List<? extends SequenceTestableInner> getSequenceList();

    public interface SequenceTestableInner {
        String getSequence();

        String getOrganism();

        String getTaxid();

        String getAccver();
    }
}
