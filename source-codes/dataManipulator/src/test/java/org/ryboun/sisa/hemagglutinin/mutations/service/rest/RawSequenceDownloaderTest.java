package org.ryboun.sisa.hemagglutinin.mutations.service.rest;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.ryboun.sisa.hemagglutinin.mutations.service.SequenceServiceForTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

//@ExtendWith({SpringExtension.class})
@TestPropertySource("classpath:application-dev.properties")
@SpringBootTest
class RawSequenceDownloaderTest {

//    RawSequenceDownloader rawSequenceDownloader = new RawSequenceDownloader();

    LocalDate testDateFrom = LocalDate.of(2013, 1, 1);
    LocalDate testDateTo = LocalDate.of(2013, 3, 1);

    @Autowired
    RawSequenceDownloader rawSequenceDownloader;


    @Test
    void downloadSequencesFrom() {
        Mono<SequenceServiceForTest.SequenceTest> sequencesMono = rawSequenceDownloader.downloadSequencesFrom(testDateFrom, testDateTo);
        SequenceServiceForTest.SequenceTest sequences = sequencesMono.block();
        System.out.println(sequences.getSequenceList().size());
        Arrays.toString(sequences.getSequenceList().toArray());
    }

//    downloadSequencesFromTo
}