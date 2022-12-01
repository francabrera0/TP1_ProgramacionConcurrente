package org.compurrentes.actors;

import org.compurrentes.DataHandler;

import java.util.Random;
import java.util.concurrent.locks.ReadWriteLock;

public abstract class Actor implements Runnable {

    protected final DataHandler dataHandler;
    protected final ReadWriteLock logLock;

    protected final static Random random = new Random();

    protected Actor(DataHandler dataHandler, ReadWriteLock testLock) {
        this.dataHandler = dataHandler;
        this.logLock = testLock;
    }

}