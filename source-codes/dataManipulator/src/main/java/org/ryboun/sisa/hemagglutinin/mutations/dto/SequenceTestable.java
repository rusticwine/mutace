package org.ryboun.sisa.hemagglutinin.mutations.dto;

import java.time.LocalDate;
import java.util.List;

public interface SequenceTestable {

    List<? extends SequenceTestableInner> getSequenceList();

    public interface SequenceTestableInner {
        String getSequence();

        String getOrganism();

        String getTaxid();

        String getAccver();

        default LocalDate getDateCreated() {
            return null;
//            return LocalDate.of(1970, 12, 12);
        }

    }
}
