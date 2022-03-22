package org.ryboun.sisa.hemagglutinin.mutations.service;

import java.time.LocalDate;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;
import org.ryboun.sisa.hemagglutinin.mutations.model.Sequence;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Service
public class Runners {


    @Value("${sequenceDownloader.period.seconds}")
    Integer downloaderPeriodSeconds;

    @Value("${sequenceDownloader.periodDuration.days}")
    Integer downloaderPeriodDurationDays;

    @Value("${sequenceAligner.submit.period.seconds}")
    Integer alignerSubmitterPeriodSeconds;

    @Value("${sequenceAligner.download.period.seconds}")
    Integer alignerDownloaderPeriodSeconds;

    ScheduledExecutorService downloaderService;

    ScheduledExecutorService alignerService;

    private final SequenceService sequenceService;


    @Autowired
    public Runners(SequenceService sequenceService) {
        this.sequenceService = sequenceService;
    }

    @PostConstruct
    void initExecutors() {
        //somehow handle dates to download from and to
        Runnable sequenceDownloader = getSequenceDownloader();
        System.out.println("launching sequence download, currently downloaded sequences: " +
                           sequenceService.getAllDownloadedSequences().toString());
        downloaderService = Executors.newSingleThreadScheduledExecutor();
        downloaderService.scheduleAtFixedRate(sequenceDownloader, 1, downloaderPeriodSeconds, TimeUnit.SECONDS);
    }


    private Runnable getSequenceDownloader() {
        Runnable sequenceDownloader = () -> {
                System.out.println("launching sequence download, currently downloaded sequences: " +
                                   sequenceService.getAllDownloadedSequences().stream()
                                           .map(Sequence::getAccver)
                                           .collect(Collectors.joining(", ")));
            LocalDate dateFrom = sequenceService.getLstSequenceDownloadDate();
            System.out.println("date to start from: " + dateFrom.toString());
            int downloadSequenceCount = sequenceService.downloadAndSaveNewSequences(dateFrom, dateFrom.plusDays(downloaderPeriodDurationDays));
            System.out.println("downloadSequenceCount: " + downloadSequenceCount);
        };
        return sequenceDownloader;
    }


    private Runnable getSequenceAlignerSubmitter() {
        Runnable sequenceAlignerSubmitter = () -> {

        };

        return sequenceAlignerSubmitter;
    }
}
