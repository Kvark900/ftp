package com.kvark900.util;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 * Class responsible for calculating and displaying file transfer time and speed
 */
public class FileTransferStats {
    private static List<String> NAMES = new ArrayList<>();
    private static List<Double> SPEED = new ArrayList<>();
    private static List<Double> TIME = new ArrayList<>();
    private static final Logger LOGGER = Logger.getLogger(FileTransferStats.class.toString());


    public synchronized static void addStatistics(String name, double speed, double time) {
        NAMES.add(name);
        SPEED.add(speed);
        TIME.add(time);
    }

    /**
     * Displaying single file statistics: transfer time, transfer speed
     *
     * @param name filename
     * @param v speed in KB/s
     * @param timetook transfer time
     */
    public static void displaySingleFileStats(String name, double v, double timetook){
        addStatistics(name, v, timetook);
        String singleFile = name + " transferred at " + v + " KB/s, in " + timetook + " seconds. ";
        System.out.println("=====================================================================================");
        LOGGER.info(singleFile);
        System.out.println("=====================================================================================");
    }


    /**
     * Displaying summary statistics for all transferred files.
     * Calculates and displays transfers average speed
     */
    public static void displaySummaryStats() {
        double totalSpeed = 0;
        double totalTime = 0;

        for (int i = 0; i < NAMES.size(); i++) {
            double speed = SPEED.get(i);
            double singleTime = TIME.get(i);
            totalSpeed += speed;

            if (totalTime < singleTime) {
                totalTime = singleTime;
            }
        }
        System.out.println("=====================================================================================");
        LOGGER.info(String.format("Average speed for all files: %f KB/s. All finished in: %f ", totalSpeed / NAMES.size(), totalTime));
        System.out.println("=====================================================================================");

    }
}
