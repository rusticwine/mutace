package org.ryboun.sisa.hemagglutinin.mutations;

import java.util.Arrays;
import org.junit.jupiter.api.Assertions;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;


class UtilsTest {

    
    @Test
    void getAlignedPositions() {
        String rawReference = "ABCDEFGH";
        String alignedReference = "--ABCDE---FGH-";
        int[] expectedPositions = new int[] {2, 3, 4, 5, 6, 10, 11, 12};
        
        int[] actualPositions = Utils.getAlignedPositions(rawReference, alignedReference);
        System.out.println(Arrays.toString(actualPositions));

        Assertions.assertArrayEquals(expectedPositions, actualPositions, "Positions of raw and aligned sequence don't correspond");
    }
}