package org.compurrentes;

public class Main {

    private final static int CREATORS_NUMBER_ARG = 0;
    private final static int REVIEWER_NUMBER_ARG = 1;
    private final static int CONSUMER_NUMBER_ARG = 2;
    private final static int OUTPUT_FILE_ARG = 3;

    private static int getActorsNumber(int i, String[] args) {
        return Integer.parseInt(args[i]);
    }

    public static void main(String[] args) {
        Program program = new Program(getActorsNumber(CREATORS_NUMBER_ARG, args),
                getActorsNumber(REVIEWER_NUMBER_ARG, args),
                getActorsNumber(CONSUMER_NUMBER_ARG, args),
                args[OUTPUT_FILE_ARG]);
        program.start();
    }

}