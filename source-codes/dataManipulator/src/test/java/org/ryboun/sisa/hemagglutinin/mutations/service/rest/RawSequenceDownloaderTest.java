package org.ryboun.sisa.hemagglutinin.mutations.service.rest;

import org.junit.jupiter.api.Test;
import org.ryboun.sisa.hemagglutinin.mutations.dto.SequenceGenepeptList;
import org.ryboun.sisa.hemagglutinin.mutations.dto.SequenceTestable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.util.Arrays;

//@ExtendWith({SpringExtension.class})
@TestPropertySource("classpath:application-dev.properties")
@SpringBootTest
class RawSequenceDownloaderTest {

//    RawSequenceDownloader rawSequenceDownloader = new RawSequenceDownloader();

    LocalDate testDateFrom = LocalDate.of(2013, 1, 1);
    LocalDate testDateTo = LocalDate.of(2013, 3, 1);

    @Autowired
    NcbiRawSequenceDownloader ncbiRawSequenceDownloader;


    @Test
    void downloadSequencesFrom() {
        Mono<SequenceTestable> sequencesMono = ncbiRawSequenceDownloader.downloadSequencesFromTo(testDateFrom, testDateTo);
        SequenceTestable sequences = sequencesMono.block();
        System.out.println(sequences.getSequenceList().size());
        Arrays.toString(sequences.getSequenceList().toArray());
    }

//    downloadSequencesFromTo
}