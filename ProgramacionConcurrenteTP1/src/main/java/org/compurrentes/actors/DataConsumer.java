package org.compurrentes.actors;

import org.compurrentes.DataHandler;
import org.compurrentes.beans.Data;

import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReadWriteLock;

public class DataConsumer extends Actor {

    private final static int MEAN_CONSUME_DELAY_MS = 15000;
    private final static int STD_CONSUME_DELAY_MS = 2000;

    public DataConsumer(DataHandler dataHandler, ReadWriteLock logLock) {
        super(dataHandler, logLock);
    }

    private String consume(Data data) {
        try {
            TimeUnit.MICROSECONDS.sleep((long) random.nextGaussian() * STD_CONSUME_DELAY_MS + MEAN_CONSUME_DELAY_MS);
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
                Optional<Data> optionalTargetData = dataHandler.consumeValidated();
                optionalTargetData.ifPresent(targetData -> {
                    // System.out.println(Instant.now() + " " + Thread.currentThread().getName() +
                    // "consumed!");
                    String consumedDataId = consume(targetData);
                    dataHandler.deleteConsumed(consumedDataId, this);
                });
            } finally {
                logLock.readLock().unlock();
            }
        }
    }

}