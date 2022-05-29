package org.ryboun.sisa.hemagglutinin.mutations.service;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.ryboun.sisa.hemagglutinin.mutations.model.Sequence;
import org.ryboun.sisa.hemagglutinin.mutations.model.SequencesProcessingStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

//@Service
public class Runners {
/*
 - Ryboun
 - Ravena
 - Sazet hrasky
 - studena koupel
 - odvezt hlinu
 */

    @Value("${sequenceDownloader.period}")
    Duration downloaderPeriod;


    @Value("${sequenceDownloader.periodDuration.days}")
    int downloaderPeriodDays;

    @Value("${sequenceAligner.submit.period}")
    Duration alignerSubmitterPeriod;
    @Value("${sequenceAligner.checker.period}")
    Duration alignerCheckerPeriod;
    @Value("${sequenceAligner.download.period}")
    Duration alignerDownloaderPeriod;

    @Value("${sequenceAligner.download.period}")
    Duration alignerDownloaderPeriodSeconds;

    ScheduledExecutorService downloaderService;

    ScheduledExecutorService alignerService;

    private final SequenceService sequenceService;


    @Autowired
    public Runners(SequenceService sequenceService) {
        this.sequenceService = sequenceService;
    }


//    @PostConstruct
    void initExecutors() {
        //somehow handle dates to download from and to
        Runnable sequenceDownloader = getSequenceDownloader();
//        System.out.println("initExecutors: sequence download, currently downloaded sequences: " +
//                           sequenceService.getAllDownloadedSequences().toString());
        downloaderService = Executors.newSingleThreadScheduledExecutor();
        downloaderService.scheduleAtFixedRate(sequenceDownloader, 10, downloaderPeriod.getSeconds(), TimeUnit.SECONDS);

        Runnable sequenceAlignSubmitter = getSequenceAlignerSubmitter();
        alignerService = Executors.newSingleThreadScheduledExecutor();
        downloaderService.scheduleAtFixedRate(sequenceAlignSubmitter, 7, alignerSubmitterPeriod.getSeconds(), TimeUnit.SECONDS);

        Runnable sequenceAlignChecker = getSequenceAlignerChecker();
        alignerService = Executors.newSingleThreadScheduledExecutor();
        downloaderService.scheduleAtFixedRate(sequenceAlignChecker, 14, alignerCheckerPeriod.getSeconds(), TimeUnit.SECONDS);

        Runnable sequenceAlignDownloader = getSequenceAlignerDownloader();
        alignerService = Executors.newSingleThreadScheduledExecutor();
        downloaderService.scheduleAtFixedRate(sequenceAlignDownloader, 21, alignerDownloaderPeriod.getSeconds(), TimeUnit.SECONDS);

    }


    private Runnable getSequenceDownloader() {
        Runnable sequenceDownloader = () -> {
            System.out.println("INITIATE DOWNLOADER getSequenceDownloader: launching sequence download, currently downloaded sequences: " +
                               sequenceService.getAllDownloadedSequences()
                                              .stream()
                                              .map(Sequence::getAccver)
                                              .collect(Collectors.joining(", ")));

            int downloadSequenceCount = sequenceService.downloadAndSaveNewSequences(downloaderPeriodDays);
            System.out.println("downloadSequenceCount: " + downloadSequenceCount);
        };
        return new CatchingRunnable(sequenceDownloader);
    }


    private Runnable getSequenceAlignerSubmitter() {
        Runnable sequenceAlignerSubmitter = () -> {
            System.out.println("INITIATE ALIGNER JOB SUBMIT");
            SequenceService.AlignSubmitResult alignSubmitResult = sequenceService.alignSequences();
            System.out.println(String.format("Align submit attempt, downloaded sequences: %d, submitted sequences %d",
                                             alignSubmitResult.getDownloadedSequencesSince(),
                                             alignSubmitResult.getSequenceSubmitForAlignment()));
        };

        return sequenceAlignerSubmitter;
    }


    private Runnable getSequenceAlignerChecker() {
        Runnable sequenceAlignerSubmitter = () -> {
            System.out.println("INITIATE ALIGNER JOB CHECK");
            List<SequencesProcessingStatus> alignmentsStatusesJobsFinished = sequenceService.checkAlignmentDoneAndReturn();
            System.out.println(String.format("Aligner jobs updated: %d, \ncontent: %s", alignmentsStatusesJobsFinished.size(), StringUtils.join(alignmentsStatusesJobsFinished, "\n")));
        };

        return sequenceAlignerSubmitter;
    }


    private Runnable getSequenceAlignerDownloader() {
        Runnable sequenceAlignerDownloader = () -> {
            System.out.println(String.format("INITIATE ALIGNED SEQUENCE DOWNLOADER"));
            long jobsFinished = sequenceService.processAlignedSequences();
            System.out.println(String.format("Sequences aligned: %d", jobsFinished));
        };

        return sequenceAlignerDownloader;
    }


    public static class CatchingRunnable implements Runnable{
        private final Runnable delegate;

        public CatchingRunnable(Runnable runnable) {
            this.delegate = runnable;
        }

        @Override
        public void run() {
            try {
                delegate.run();
            } catch (Exception e) {
                System.out.println("Exception Occurred "+e.getMessage()); // Log, notify etc...
                e.printStackTrace();
//                throw e;
            }
        }
    }
}