package ClientPart2;

import Model.HttpMethod;
import Model.Record;
import io.swagger.client.ApiException;
import io.swagger.client.ApiResponse;
import io.swagger.client.api.SkiersApi;
import io.swagger.client.model.LiftRide;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicInteger;

public class ClientThreadPart2 implements Runnable{
    private static final Integer NUM_OF_RE_TRIES = 5;

    private Integer skierIdsStart;
    private Integer skierIDsEnd;
    private int startTime;
    private int endTime;
    CountDownLatch phase;
    CountDownLatch allPhases;
    private Integer numSkiLifts;
    private int numOfRequest;
    private String ipAddress;
    private int numOfSuccess = 0;
    private int numOfFails = 0;
    AtomicInteger totalSuccess;
    AtomicInteger totalFails;
    BlockingQueue<Record> bq;
    public ClientThreadPart2(Integer skierIDsStart, Integer skierIDsEnd, int startTime, int endTime,
                        CountDownLatch phase, Integer numSkiLifts, CountDownLatch allPhases,
                        int numOfRequest, String ipAddress, AtomicInteger totalSuccess, AtomicInteger totalFails,
                             BlockingQueue<Record> bq) {
        this.skierIdsStart = skierIDsStart;
        this.skierIDsEnd = skierIDsEnd;
        this.startTime = startTime;
        this.endTime = endTime;
        this.phase = phase;
        this.numSkiLifts = numSkiLifts;
        this.allPhases = allPhases;
        this.numOfRequest = numOfRequest;
        this.ipAddress = ipAddress;
        this.totalSuccess = totalSuccess;
        this.totalFails = totalFails;
        this.bq = bq;
    }

    @Override
    public void run() {
        String url = "http://" + this.ipAddress + ":8080/CS6650-assignment1_war/";
        SkiersApi apiInstance = new SkiersApi();
        apiInstance.getApiClient().setBasePath(url);

        for(int i=0; i < numOfRequest; i++) {
            LiftRide newLiftRide = new LiftRide();
            newLiftRide.setLiftID(ThreadLocalRandom.current().nextInt(1, numSkiLifts));
            newLiftRide.setTime(ThreadLocalRandom.current().nextInt(startTime, endTime + 1));
            newLiftRide.setWaitTime(ThreadLocalRandom.current().nextInt(0, 10));
            int skierId = ThreadLocalRandom.current().nextInt(skierIdsStart, skierIDsEnd);
            try{
                long start = System.currentTimeMillis();
                ApiResponse<Void> res = apiInstance.writeNewLiftRideWithHttpInfo(newLiftRide,
                        12, "2022", "213", skierId);
                long end = System.currentTimeMillis();

                if (res.getStatusCode() == 201 || res.getStatusCode() == 200) {
                    this.numOfSuccess++;
                    totalSuccess.getAndIncrement();
                } else {
                    int fails = 0;
                    for (int j = 0; j < NUM_OF_RE_TRIES; j++) {
                        res = apiInstance.writeNewLiftRideWithHttpInfo(newLiftRide,
                                12, "2022", "213", skierId);
                        if (res.getStatusCode() == 201 || res.getStatusCode() == 200) {
                            this.numOfSuccess++;
                            totalSuccess.getAndIncrement();
                            break;
                        } else {
                            fails++;
                        }
                    }
                    if(fails == NUM_OF_RE_TRIES) totalFails.getAndIncrement();
                }
                Record record = new Record(start, HttpMethod.POST, res.getStatusCode(), end - start);
                bq.put(record);
            } catch (ApiException e){
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        }
        if(phase != null) this.phase.countDown();
        this.allPhases.countDown();
    }
}
