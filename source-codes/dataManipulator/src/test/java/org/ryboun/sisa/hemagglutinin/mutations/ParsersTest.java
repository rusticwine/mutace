package org.ryboun.sisa.hemagglutinin.mutations;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.ryboun.sisa.hemagglutinin.mutations.model.ReferenceSequence;

import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ParsersTest {

    @Test
    void loadFastaSequenceFromResource() throws IOException {
        List<ReferenceSequence> referenceSequences = Parsers.loadFastaSequenceFromResource();
        Assertions.assertNotNull(referenceSequences);
        Assertions.assertEquals(1, referenceSequences.size(), "One reference sequence is expected");
        Assertions.assertEquals("AAR02640.2", referenceSequences.get(0).getAccver(), "accver does not correspond");
        Assertions.assertEquals("hemagglutinin", referenceSequences.get(0).getProtein());
        Assertions.assertEquals("Influenza A virus (A/Netherlands/219/2003(H7N7))", referenceSequences.get(0).getOrganism());
        Assertions.assertEquals(562, referenceSequences.get(0).getSequence().length(), "Sequence has unexpected size");
    }
}