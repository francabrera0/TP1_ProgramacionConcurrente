package org.compurrentes.beans;

import org.compurrentes.actors.DataConsumer;

import java.util.Map;

public class Stats {

    private final int pendingBufferSize;
    private final int validatedBufferSize;
    private final Map<DataConsumer, Integer> consumedDataInfo;
    private final int createdData;
    private final int validatedData;

    public Stats(int pendingBufferSize, int validatedBufferSize, Map<DataConsumer, Integer> consumedData,
                 int createdData, int validatedData) {
        this.pendingBufferSize = pendingBufferSize;
        this.validatedBufferSize = validatedBufferSize;
        this.consumedDataInfo = consumedData;
        this.createdData = createdData;
        this.validatedData = validatedData;
    }

    public int getPendingBufferSize() {
        return pendingBufferSize;
    }

    public int getValidatedBufferSize() {
        return validatedBufferSize;
    }

    public Map<DataConsumer, Integer> getConsumedDataInfo() {
        return consumedDataInfo;
    }

    public int getCreatedData() {
        return createdData;
    }

    public int getValidatedData() {
        return validatedData;
    }

}