package org.compurrentes.actors;

import org.compurrentes.DataHandler;
import org.compurrentes.beans.Stats;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.Buffer;
import java.sql.Array;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReadWriteLock;

public class DataLogger extends Actor {

    private static final int PRINT_DELAY_S = 2;
    private static final int RUNNING_TIME_S = 10;
    private final String outputFile;

    private Map<DataConsumer, Integer> oldConsumedDataInfo = new HashMap<>();

    public DataLogger(DataHandler dataHandler, ReadWriteLock logLock, String outputFile) {
        super(dataHandler, logLock);
        this.outputFile = outputFile;
        printLines(Collections.singletonList("PROCESS STARTS\n"), false);

    }

    private void printLines(List<String> lines, boolean appends) {
        try (FileWriter fw = new FileWriter(outputFile, appends);
             BufferedWriter bw = new BufferedWriter(fw);
             PrintWriter pw = new PrintWriter(bw)) {
            lines.forEach(pw::println);
        } catch (IOException e) {
            throw new RuntimeException("Could not log output file " + outputFile);
        }
    }

    private void logStats(Stats stats, int totalConsumedData) {
        List<String> lines = new ArrayList<>();
        lines.add(Instant.now() + " Pending Buffer size " + stats.getPendingBufferSize());
        lines.add(Instant.now() + " Validated Buffer size " + stats.getValidatedBufferSize());
        lines.add(Instant.now() + " created size " + stats.getCreatedData());
        lines.add(Instant.now() + " validated size " + stats.getValidatedData());
        lines.add(Instant.now() + " consumed size " + totalConsumedData);
        lines.add("");
        lines.forEach(System.out::println);
        printLines(lines, true);
    }

    private void testFailures(Stats stats, int totalConsumedData) {
        if (stats.getCreatedData() - totalConsumedData != stats.getPendingBufferSize()) {
            System.out.println("\ncreatedData - consumedData != pendingBSize\n SE ROMPIO :,)");
            System.exit(0);
        }

        if (stats.getValidatedData() - totalConsumedData != stats.getValidatedBufferSize()) {
            System.out.println("\nvalidatedData - consumedData != validatedBSize\n SE ROMPIO :,)");
            System.exit(0);
        }

        if (stats.getConsumedDataInfo().equals(oldConsumedDataInfo)) {
            System.out.println("Algun revisor o todos los creadores/consumidores están en deadlock ");
            System.exit(0);
        }

        stats.getConsumedDataInfo().entrySet().stream().filter(entry -> {
            DataConsumer consumer = entry.getKey();
            return entry.getValue().equals(oldConsumedDataInfo.get(consumer));
        }).findFirst().ifPresent(entry -> {
            System.out.println("Consumidor " + entry.getKey() + " está en deadLock");
            System.exit(0);
        });
    }

    @Override
    public void run() {
        for (int i = 0; i < RUNNING_TIME_S / PRINT_DELAY_S; i++) {
            try {
                TimeUnit.SECONDS.sleep(PRINT_DELAY_S);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            Stats stats;
            int totalConsumedData;
            logLock.writeLock().lock();
            try {
                stats = dataHandler.getStats();
                totalConsumedData = DataHandler.collectIntegerMap(stats.getConsumedDataInfo());
            } finally {
                logLock.writeLock().unlock();
            }
            testFailures(stats, totalConsumedData);
            oldConsumedDataInfo = new HashMap<>(stats.getConsumedDataInfo());
            logStats(stats, totalConsumedData);
        }

        String endMessage = RUNNING_TIME_S + " seconds have passed. Finished execution";
        System.out.println(endMessage);
        printLines(Collections.singletonList(endMessage), true);
        System.exit(0);
    }

}