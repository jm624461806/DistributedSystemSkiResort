package ClientPart2;

import Model.Record;

import java.io.*;
import java.util.*;

public class ChartUtil {


    public static void generateChartData(List<Record> recordList, int numThreads) {
        List<long[]> secondLatencySum = new ArrayList<>();
        Collections.sort(recordList, new Comparator<Record>(){
            public int compare(Record r1, Record r2) {
                return (int) (r1.getStartTime() - r2.getStartTime());
            }
        });

        long sec = 0;
        int start = 0;
        long sum = 0;
        long howMany = 0;
        double finalSec = 0.0;
        double finalMean = 0.0;

        int size = recordList.size();
        for(int i = 0; i < size; i++) {
            if(recordList.get(i).getStartTime() - recordList.get(start).getStartTime() < 1000) {
                howMany += 1;
                sum += recordList.get(i).getLatency();
            }else{
                sec++;
                secondLatencySum.add(new long[]{sec, sum ,howMany});
                sum = 0;
                howMany = 0;
                start = i;
            }
        }
        if(start != size - 1) {
            finalMean = sum / howMany;
            finalSec = sec + (recordList.get(size-1).getStartTime() - recordList.get(start).getStartTime())/ 1000.0;
        }

        String path = "chart-" + numThreads +"-threads.csv";
        generateCsv(path, secondLatencySum, finalSec, finalMean);

    }

    private static void generateCsv(String path, List<long[]> secondLatencySum, double finalSec, double finalMean) {
        try {
            File csvFile = new File(path);
            if (csvFile.exists())
                csvFile.delete();
            csvFile.createNewFile();
            FileWriter fileWriter = new FileWriter(csvFile);
            addHeader(fileWriter);
            //flush makes sure that any buffered data is written to disk
            fileWriter.flush();

            for(long[] arr : secondLatencySum) {
                double meanLatency = arr[1] / arr[2];
                fileWriter.append(String.valueOf(arr[0]));
                fileWriter.append(",");
                fileWriter.append(String.valueOf(meanLatency));
                fileWriter.append("\n");
            }

            fileWriter.append(String.valueOf(finalSec));
            fileWriter.append(",");
            fileWriter.append(String.valueOf(finalMean));
            fileWriter.append("\n");

            fileWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private static void addHeader(FileWriter fileWriter) throws IOException {
        fileWriter.append("Sec");
        fileWriter.append(",");
        fileWriter.append("MeanLatency");
        fileWriter.append("\n");
    }



}
