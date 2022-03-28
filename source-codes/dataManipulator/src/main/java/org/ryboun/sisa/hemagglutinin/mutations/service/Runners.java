package org.ryboun.sisa.hemagglutinin.mutations.service;

import java.time.Duration;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import javax.annotation.PostConstruct;
import org.ryboun.sisa.hemagglutinin.mutations.model.Sequence;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;


@Service
public class Runners {
/*
 - Ryboun
 - Ravena
 - Sazet hrasky
 - studena koupel
 - odvezt hlinu
 */

    @Value("${sequenceDownloader.period.seconds}")
    Integer downloaderPeriodSeconds;

    @Value("${sequenceDownloader.periodDuration.days}")
    Duration downloaderPeriod;

    @Value("${sequenceAligner.submit.period}")
    Duration alignerSubmitterPeriod;
    @Value("${sequenceAligner.checker.period}")
    Duration alignerCheckerPeriod;
    @Value("${sequenceAligner.download.period}")
    Duration alignerDownloadererPeriod;

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

        Runnable sequenceAlignSubmitter = getSequenceAlignerSubmitter();
        alignerService = Executors.newSingleThreadScheduledExecutor();
        downloaderService.scheduleAtFixedRate(sequenceAlignSubmitter, 1, alignerSubmitterPeriod.getSeconds(), TimeUnit.SECONDS);

        Runnable sequenceAlignSubmitter = getSequenceAlignerSubmitter();
        alignerService = Executors.newSingleThreadScheduledExecutor();
        downloaderService.scheduleAtFixedRate(sequenceAlignSubmitter, 1, alignerSubmitterPeriod.getSeconds(), TimeUnit.SECONDS);


    }


    private Runnable getSequenceDownloader() {
        Runnable sequenceDownloader = () -> {
            System.out.println("launching sequence download, currently downloaded sequences: " +
                               sequenceService.getAllDownloadedSequences()
                                              .stream()
                                              .map(Sequence::getAccver)
                                              .collect(Collectors.joining(", ")));

            int downloadSequenceCount = sequenceService.downloadAndSaveNewSequences(downloaderPeriod);
            System.out.println("downloadSequenceCount: " + downloadSequenceCount);
        };
        return sequenceDownloader;
    }


    private Runnable getSequenceAlignerSubmitter() {
        Runnable sequenceAlignerSubmitter = () -> {
            SequenceService.AlignSubmitResult alignSubmitResult = sequenceService.alignSequences();
            System.out.println(String.format("Align submit attempt, downloaded sequences: %d, submitted sequences %d",
                                             alignSubmitResult.getDownloadedSequencesSince(),
                                             alignSubmitResult.getSequenceSubmitForAlignment()));
        };

        return sequenceAlignerSubmitter;
    }

    private Runnable getSequenceAlignerChecker() {
        Runnable sequenceAlignerSubmitter = () -> {
            long jobsFinished = sequenceService.updateAligningSequences();
            System.out.println(String.format("Align submit attempt, downloaded sequences: %d, submitted sequences %d",
                                             alignSubmitResult.getDownloadedSequencesSince(),
                                             alignSubmitResult.getSequenceSubmitForAlignment()));
        };

        return sequenceAlignerSubmitter;
    }
}
