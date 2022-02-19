package ClientPart2;

import Model.HttpMethod;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

public class RecordUtil {

    public static void generateReport(long wallTime, String path, int totalFails, int totalSuccess){
        List<Long> singleRequestLatencyList = sortTheFile(path);
        double mean = getMeanResponseTime(singleRequestLatencyList);
        double median = getMedianResponseTime(singleRequestLatencyList);
        long max = getMaxResponseTime(singleRequestLatencyList);
        long min = getMinResponseTime(singleRequestLatencyList);
        long ninetyNine = get99Percentile(singleRequestLatencyList);

        System.out.println("Part 2 Report: ");
        System.out.println("==================================================");
        System.out.println("Mean response time: " + mean + " ms");
        System.out.println("Median response time: " + median + " ms");
        System.out.println("Wall time: " + wallTime + " ms");
        System.out.println("The total throughput in requests per second: " +
                (totalFails + totalSuccess) / (double) (wallTime / 1000) + "(req/sec)");
        System.out.println("The p99 response time: " + ninetyNine + " ms");
        System.out.println("Max response time: " + max + " ms");
        System.out.println("Min response time: " + min + " ms");
    }

    private static List<Long> sortTheFile(String path) {
        List<Long> singleRequestLatencyList = new ArrayList<>();
        try {
            FileReader fileReader = new FileReader(path);
            BufferedReader br = new BufferedReader(fileReader);
            String line;
            while ((line = br.readLine()) != null) {
                String[] lineArr = line.split(",");
                if(lineArr[2].equals("Latency")) continue;
                singleRequestLatencyList.add(Long.parseLong(lineArr[2]));
            }
            br.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        Collections.sort(singleRequestLatencyList);
        return singleRequestLatencyList;
    }

    private static double getMeanResponseTime(List<Long> list) {
        long sum = 0;
        for(long l : list) sum += l;
        return Math.round((sum / (double) list.size() * 100) ) / 100.0;
    }

    private static double getMedianResponseTime(List<Long> list) {
        int size = list.size();
        long right= list.get(size/2);
        if (size % 2 == 1) return (double)right;
        long left = list.get(size/2 - 1);
        return (double) (left + right) / 2;
    }

    private static long getMaxResponseTime(List<Long> list){
        return list.get(list.size() - 1);
    }

    private static long getMinResponseTime(List<Long> list){
        return list.get(0);
    }

    private static long get99Percentile(List<Long> list){
        int index = (int) Math.ceil(0.99 * list.size());
        return list.get(index-1);
    }
}
