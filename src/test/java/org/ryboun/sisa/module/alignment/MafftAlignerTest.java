package org.ryboun.sisa.module.alignment;

import com.sun.jna.platform.unix.Resource;
import org.apache.commons.compress.utils.IOUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import reactor.core.publisher.Mono;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

class MafftAlignerTest {

    private static final String MAFFT_JOB_ID_PREFIX = "mafft-";

    private static final String TEST_SEQUENCES_RAW = "sequences/rawSequences_test1.fasta";

    private static final String TEST_SEQUENCES_REFERENCE = "sequences/references_test1.fasta";

    private static final String TEST_JOB_ID = "R20211024-164102-0407-27440657-p1m";
    MafftAligner ma = new MafftAligner();


    @Test
    void alignWithSingleReference() throws IOException {
        String sequencesString = loadStringFileFromResources(TEST_SEQUENCES_REFERENCE);

        AlignDto alignDto = AlignDto.builder()
                .email("valerius@centrum.cz")
                .format("fasta")
                .sequence(sequencesString)
                .build();

        String result = ma.alignWithSingleReference(alignDto);

        System.out.println("test result: " + result);
    }

    @Test
    void testAlign1_checkJobStatus() {
        String jobStatus = ma.testAlign1_checkJobStatus(MAFFT_JOB_ID_PREFIX + TEST_JOB_ID);
        Assertions.assertEquals("FINISHED", jobStatus, "Job should be already finished");
    }

    @Test
    void testAlign1_getResult() {
        String jobResult = ma.testAlign1_getResult(MAFFT_JOB_ID_PREFIX, TEST_JOB_ID);
        Assertions.assertNotNull(jobResult, "There should be some data in job result");
        System.out.println("job result" + System.lineSeparator() + jobResult);
    }

    String loadStringFileFromResources(String filePath) throws IOException {
        try (BufferedInputStream bis = new BufferedInputStream(this.getClass().getClassLoader()
                .getResourceAsStream(filePath))) {
            byte[] contents = new byte[1024];

            int bytesRead = 0;
            String strFileContents = null;
            while((bytesRead = bis.read(contents)) != -1) {
                strFileContents += new String(contents, 0, bytesRead);
            }

            return strFileContents.substring(4);
        }
    }
}