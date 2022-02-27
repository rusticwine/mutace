package org.ryboun.sisa.hemagglutinin.mutations.service;

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
            sequenceService.downloadAndSaveNewSequences(null, null);
        };

        downloaderService = new ScheduledThreadPoolExecutor(1);
        downloaderService.schedule(sequenceDownloader, downloaderPeriodSeconds, TimeUnit.SECONDS);
        sequenceService.downloadAndSaveNewSequences(null, null);
    }
}
