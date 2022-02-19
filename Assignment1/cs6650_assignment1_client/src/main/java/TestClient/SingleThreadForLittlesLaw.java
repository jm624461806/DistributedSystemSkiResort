package TestClient;

import io.swagger.client.ApiException;
import io.swagger.client.ApiResponse;
import io.swagger.client.api.SkiersApi;
import io.swagger.client.model.LiftRide;

import java.util.concurrent.ThreadLocalRandom;

public class SingleThreadForLittlesLaw implements Runnable{
    private String ipAddress;
    private int numOfRequests;
    private int startTime;
    private int endTime;
    private int skierIDsEnd;
    private int skierIdsStart;
    private int numSkiLifts;

    public SingleThreadForLittlesLaw(int idStart, int idEnd, int startTime, int endTime, int numOfRequest,
                                     String ipAddress, int numLifts) {
        this.ipAddress = ipAddress;
        this.skierIDsEnd = idEnd;
        this.skierIdsStart = idStart;
        this.startTime = startTime;
        this.endTime = endTime;
        this.numOfRequests = numOfRequest;
        this.numSkiLifts = numLifts;
    }

    @Override
    public void run() {

        String url = "http://" + this.ipAddress + ":8080/CS6650-assignment1_war/"; //CS6650-assignment1_war
        SkiersApi apiInstance = new SkiersApi();
        apiInstance.getApiClient().setBasePath(url);

        for(int i=0; i < numOfRequests; i++) {
            LiftRide newLiftRide = new LiftRide();
            newLiftRide.setLiftID(ThreadLocalRandom.current().nextInt(1, numSkiLifts + 1));
            newLiftRide.setTime(ThreadLocalRandom.current().nextInt(startTime, endTime + 1));
            newLiftRide.setWaitTime(ThreadLocalRandom.current().nextInt(0, 11));
            int skierId = ThreadLocalRandom.current().nextInt(skierIdsStart, skierIDsEnd + 1);
            try{
                ApiResponse<Void> res = apiInstance.writeNewLiftRideWithHttpInfo(newLiftRide,
                        12, "2022", "213", skierId);
                } catch (ApiException e) {
                e.printStackTrace();
            }
        }
    }


}
