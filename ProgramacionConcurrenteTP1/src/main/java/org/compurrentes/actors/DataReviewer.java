package org.compurrentes.actors;

import org.compurrentes.DataHandler;
import org.compurrentes.beans.Data;

import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReadWriteLock;

public class DataReviewer extends Actor {

    private final static int MEAN_REVIEW_DELAY_MS = 9500;
    private final static int STD_REVIEW_DELAY_MS = 500;

    public DataReviewer(DataHandler dataHandler, ReadWriteLock logLock) {
        super(dataHandler, logLock);
    }

    private String reviewData(Data data) {
        try {
            TimeUnit.MICROSECONDS.sleep((long) random.nextGaussian() * STD_REVIEW_DELAY_MS + MEAN_REVIEW_DELAY_MS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return data.getId();
    }

    @Override
    public void run() {
        while (true) {
            logLock.readLock().lock();
            try {
                Optional<Data> optionalCopy = dataHandler.getCopy(this);
                optionalCopy.ifPresent(copy -> {
                    // System.out.println(Instant.now() + " " + Thread.currentThread().getName() + "
                    // reviewed!");
                    String copyId = reviewData(copy);
                    dataHandler.setReviewer(copyId, this);
                });
            } finally {
                logLock.readLock().unlock();
            }
            // OPCIONAL DE PONER UN WAIT ACÁ TAMBIÉN
        }
    }

}