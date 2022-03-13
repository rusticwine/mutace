package org.ryboun.sisa.hemagglutinin.mutations.service;

import java.io.IOException;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;


class SequenceServiceForTestTest {

    SequenceServiceForTest sequenceServiceForTest;

    @BeforeEach
    void init() {
        sequenceServiceForTest = new SequenceServiceForTest();
    }

    @Test
    void loadAlignedNormalizedSequences() throws IOException {
        sequenceServiceForTest.loadAlignedNormalizedSequences();
    }

}