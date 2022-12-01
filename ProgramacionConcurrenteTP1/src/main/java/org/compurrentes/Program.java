package org.compurrentes;

import org.compurrentes.actors.*;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Supplier;

public class Program {

    private List<DataCreator> creators;
    private List<DataReviewer> reviewers;
    private List<DataConsumer> consumers;
    private DataHandler dataHandler;
    private DataLogger logger;
    private ReadWriteLock logLock = new ReentrantReadWriteLock();

    public Program(int creatorsNumber, int reviewersNumber, int consumersNumber, String outputFile) {
        dataHandler = new DataHandler();
        logger = new DataLogger(dataHandler, logLock, outputFile);
        reviewers = createList(reviewersNumber, () -> new DataReviewer(dataHandler, logLock));
        creators = createList(creatorsNumber, () -> new DataCreator(dataHandler, logLock, reviewers));
        consumers = createList(consumersNumber, () -> new DataConsumer(dataHandler, logLock));

    }

    private <T> List<T> createList(int actorsNumber, Supplier<T> supplier) {
        List<T> list = new ArrayList<>();
        for (int i = 0; i < actorsNumber; i++) {
            list.add(supplier.get());
        }
        return list;
    }

    private <T extends Actor> void startList(List<T> list, String keyWord) {
        for (int i = 0; i < list.size(); i++) {
            Thread actorThread = new Thread(list.get(i));
            actorThread.setName(keyWord + " " + i);
            actorThread.start();
        }
    }

    public void start() {
        startList(creators, "Creator");
        startList(reviewers, "Reviewer");
        startList(consumers, "Consumer");
        Thread loggerThread = new Thread(logger);
        loggerThread.start();
    }

}