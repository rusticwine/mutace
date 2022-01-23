package org.ryboun.sisa.hemagglutinin.mutations.service.rest;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

//@SpringBootTest
class RawSequenceDownloaderTest {

    RawSequenceDownloader rawSequenceDownloader = new RawSequenceDownloader();

    LocalDate testDateFrom = LocalDate.of(2013, 1, 1);
    LocalDate testDateTo = LocalDate.of(2013, 3, 1);

//    @Test
    void downloadSequencisFromNcbi() {
        rawSequenceDownloader.downloadSequencisFromNcbi(testDateFrom, testDateTo);
    }

    @Test
    void downloadSequencesFrom() {
        rawSequenceDownloader.downloadSequencesFrom(testDateFrom, testDateTo);
    }
}