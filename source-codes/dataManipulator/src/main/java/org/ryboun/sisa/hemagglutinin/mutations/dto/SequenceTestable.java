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
        
        LocalDate getDateCreated();
//        default LocalDate getDateCreated() {
//            return null;
//        }
        default LocalDate getDateUpdated() {
            return null;
        }

    }
}
