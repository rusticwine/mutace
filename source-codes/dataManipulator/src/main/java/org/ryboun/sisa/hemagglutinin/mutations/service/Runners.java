package org.ryboun.sisa.hemagglutinin.mutations.service;

import java.time.LocalDate;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledFuture;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

@Service
public class Runners {


    @Value("${sequenceDownloader.period.seconds}")
    Integer downloaderPeriodSeconds;

    @Value("${sequenceDownloader.periodDuration.days}")
    Integer downloaderPeriodDurationDays;

    ScheduledExecutorService downloaderService;

    ScheduledExecutorService aligner;

    private final SequenceService sequenceService;


    @Autowired
    public Runners(SequenceService sequenceService) {
        this.sequenceService = sequenceService;
    }

    @PostConstruct
    void initExecutors() {
        //somehow handle dates to download from and to
        Runnable sequenceDownloader = () -> {
            System.out.println("launching sequence download");
            LocalDate dateFrom = sequenceService.getLstSequenceDownloadDate();
            System.out.println("date to start from: " + dateFrom.toString());
            int downloadSequenceCount = sequenceService.downloadAndSaveNewSequences(dateFrom, dateFrom.plusDays(downloaderPeriodDurationDays));
            System.out.println("downloadSequenceCount: " + downloadSequenceCount);
        };

        downloaderService = Executors.newSingleThreadScheduledExecutor();
        downloaderService.scheduleAtFixedRate(sequenceDownloader, 1, downloaderPeriodSeconds, TimeUnit.SECONDS);

//        ScheduledExecutorService exec = Executors.newSingleThreadScheduledExecutor();
//        ScheduledFuture sf = exec.scheduleAtFixedRate(new Runnable()
//        {
//            @Override
//            public void run() {
//                // do stuff
//                System.out.println("garbage garbage garbage");
//                LocalDate dateFrom = sequenceService.getLstSequenceDownloadDate();
//                int downloadSequenceCount = sequenceService.downloadAndSaveNewSequences(dateFrom, dateFrom.plusDays(downloaderPeriodDurationDays));
//                System.out.println("downloadSequenceCount: " + downloadSequenceCount);
//
//            }
//        }, 5, 10, TimeUnit.SECONDS);
    }
}
