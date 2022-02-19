package ClientPart1;

import ClientPart2.ClientThreadPart2;
import Model.Record;

import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

public class ClientUtil {

    public static boolean isValidNumber(String number, int min, int max) {
        try {
            int num = Integer.parseInt(number);
            if (num < min || num > max) {
                System.out.println("The Number should be bigger than " + min + " and smaller than " + max);
                return false;
            }
        } catch (Exception e) {
            System.out.println("This is not a number, please enter a number.");
            return false;
        }
        return true;
    }

    public static void createAndStartTheThread(int phaseThreads, Integer skierIDsRange,
                                               int startTime, int endTime, CountDownLatch phase,
                                               Integer numSkiLifts, CountDownLatch allPhases, int numOfRequest,
                                               String ipAddress, AtomicInteger totalSuccess, AtomicInteger totalFails,
                                               int numSkiers) {
        for(int i = 0; i < phaseThreads; i++) {
            Integer skierIDsStart = i * skierIDsRange + 1;
            Integer skierIDsEnd = (i == phaseThreads - 1) ? numSkiers : (i + 1 ) * skierIDsRange;
            ClientThread clientThread = new ClientThread(skierIDsStart, skierIDsEnd, startTime, endTime,
                    phase, numSkiLifts, allPhases, numOfRequest, ipAddress, totalSuccess, totalFails);
            Thread t = new Thread(clientThread);
            t.start();
        }
    }

    public static void createAndStartTheThreadPart2(int phaseThreads, Integer skierIDsRange,
                                                    int startTime, int endTime, CountDownLatch phase,
                                                    Integer numSkiLifts, CountDownLatch allPhases, int numOfRequest,
                                                    String ipAddress, AtomicInteger totalSuccess, AtomicInteger totalFails,
                                                    BlockingQueue<Record> bq, int numSkiers) {
        for(int i = 0; i < phaseThreads; i++) {
            Integer skierIDsStart = i * skierIDsRange + 1;
            Integer skierIDsEnd = (i == phaseThreads - 1) ? numSkiers : (i + 1) * skierIDsRange;
            ClientThreadPart2 clientThread = new ClientThreadPart2(skierIDsStart, skierIDsEnd, startTime, endTime,
                    phase, numSkiLifts, allPhases, numOfRequest, ipAddress, totalSuccess, totalFails, bq);
            Thread t = new Thread(clientThread);
            t.start();
        }
    }

    public static void printOutTheData(long wallTime, int totalSuccess, int totalFail) {
        System.out.println("Part1 Report: ");
        System.out.println("==================================================");
        System.out.println("Number of successful requests sent:" + totalSuccess);
        System.out.println("Number of unsuccessful requests sent:" + totalFail);
        System.out.println("Wall time: " + wallTime);
        System.out.println("The total throughput in requests per second: " +
                (totalFail + totalSuccess) / (double) (wallTime / 1000) + "(req/sec)");
    }
}
