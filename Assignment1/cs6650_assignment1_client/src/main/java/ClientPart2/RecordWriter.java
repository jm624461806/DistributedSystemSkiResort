package ClientPart2;

import Model.Record;

import java.io.*;
import java.util.concurrent.BlockingQueue;

public class RecordWriter implements Runnable{

    BlockingQueue<Record> bq;
    private String path;

    public RecordWriter(BlockingQueue<Record> bq, String csv) {
        this.bq = bq;
        this.path = csv;
    }

    @Override
    public void run() {
        try {
            File csvFile = new File(path);
            if (csvFile.exists())
                csvFile.delete();
            csvFile.createNewFile();
            FileWriter fileWriter = new FileWriter(csvFile);
            addHeader(fileWriter);
            //flush makes sure that any buffered data is written to disk
            fileWriter.flush();

            while (true) {
                Record record = bq.take();
                if (record.getStartTime() == -1) break;
                addBody(fileWriter, record);
                fileWriter.flush();
            }
            fileWriter.close();
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void addHeader(FileWriter fileWriter) throws IOException {
        fileWriter.append("Start Time");
        fileWriter.append(",");
        fileWriter.append("Request Type");
        fileWriter.append(",");
        fileWriter.append("Latency");
        fileWriter.append(",");
        fileWriter.append("Response Code");
        fileWriter.append("\n");
    }

    private void addBody(FileWriter fileWriter, Record record) throws IOException {
        fileWriter.append(String.valueOf(record.getStartTime()));
        fileWriter.append(",");
        fileWriter.append(String.valueOf(record.getReqType()));
        fileWriter.append(",");
        fileWriter.append(String.valueOf(record.getLatency()));
        fileWriter.append(",");
        fileWriter.append(String.valueOf(record.getResCode()));
        fileWriter.append("\n");
    }
}
