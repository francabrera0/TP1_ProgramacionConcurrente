package org.compurrentes;

import org.compurrentes.actors.DataConsumer;
import org.compurrentes.actors.DataReviewer;
import org.compurrentes.beans.Data;
import org.compurrentes.beans.Stats;

import java.util.*;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class DataHandler {

    private final List<Data> pendingBuffer;
    private final List<Data> validatedBuffer;

    private final ReadWriteLock pendingBufferLock = new ReentrantReadWriteLock(true);

    private final Map<DataConsumer, Integer> consumedDataInfo = new HashMap<>();
    private int createdData = 0;
    private int validatedData = 0;

    public DataHandler() {
        pendingBuffer = new ArrayList<>();
        validatedBuffer = new ArrayList<>();
    }

    public void putData(Data data) {

        pendingBufferLock.writeLock().lock();
        try {
            if (pendingBuffer.size() < 100) {
                pendingBuffer.add(data);
                createdData++;
            }
        } finally {
            pendingBufferLock.writeLock().unlock();
        }

    }

    public Optional<Data> getCopy(DataReviewer reviewer) {
        pendingBufferLock.readLock().lock();
        try {
            return pendingBuffer.stream().filter(data -> !data.getReviewerStatus().get(reviewer)).findFirst();
        } finally {
            pendingBufferLock.readLock().unlock();
        }
    }

    public void setReviewer(String dataId, DataReviewer reviewer) {
        pendingBufferLock.writeLock().lock();
        try {
            Data reviewedData = pendingBuffer.stream().filter(data -> data.getId().equals(dataId)).findFirst().get();
            reviewedData.getReviewerStatus().put(reviewer, true);
            if (!reviewedData.getReviewerStatus().containsValue(false)) {
                synchronized (validatedBuffer) {
                    validatedBuffer.add(reviewedData);
                    validatedData++;
                    // System.out.println(Instant.now() + " " + Thread.currentThread().getName() + "
                    // finished review!");
                }
            }
        } finally {
            pendingBufferLock.writeLock().unlock();
        }
    }

    public Optional<Data> consumeValidated() {
        synchronized (validatedBuffer) {
            Optional<Data> optionalTargetData = validatedBuffer.stream().findFirst();
            optionalTargetData.ifPresent(validatedBuffer::remove);
            return optionalTargetData;
        }
    }

    public void deleteConsumed(String dataId, DataConsumer consumer) {
        pendingBufferLock.writeLock().lock();
        try {
            Data targetData = pendingBuffer.stream().filter(data -> data.getId().equals(dataId)).findFirst().get();
            pendingBuffer.remove(targetData);
            consumedDataInfo.put(consumer, consumedDataInfo.getOrDefault(consumer, 0) + 1);
        } finally {
            pendingBufferLock.writeLock().unlock();
        }
    }

    public static int collectIntegerMap(Map<?, Integer> targetMap) {
        return targetMap.values().stream().mapToInt(value -> value).sum();
    }

    public Stats getStats() {
        return new Stats(pendingBuffer.size(), validatedBuffer.size(), consumedDataInfo, this.createdData,
                this.validatedData);
    }

}