package org.compurrentes.actors;

import org.compurrentes.DataHandler;
import org.compurrentes.beans.Data;

import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.stream.Collectors;

public class DataCreator extends Actor {

    private final static int MEAN_CREATE_DELAY_MS = 38000;
    private final static int STD_CREATE_DELAY_MS = 2000;

    private final List<DataReviewer> reviewers;
    private int created = 0;

    public DataCreator(DataHandler dataHandler, ReadWriteLock logLock, List<DataReviewer> reviewers) {
        super(dataHandler, logLock);
        this.reviewers = reviewers;
    }

    private Data create() {
        try {
            TimeUnit.MICROSECONDS.sleep((long) random.nextGaussian() * STD_CREATE_DELAY_MS + MEAN_CREATE_DELAY_MS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        Map<DataReviewer, Boolean> reviewersMap = reviewers.stream()
                .collect(Collectors.toMap(r -> (DataReviewer) r, r -> false));

        return new Data(Thread.currentThread().getName() + "N" + created++, reviewersMap);
    }

    @Override
    public void run() {
        while (true) {
            logLock.readLock().lock();
            try {
                Data createdData = create();
                // System.out.println(Instant.now() + " " + Thread.currentThread().getName() + "
                // created!");
                dataHandler.putData(createdData);
            } finally {
                logLock.readLock().unlock();
            }
        }
    }

}