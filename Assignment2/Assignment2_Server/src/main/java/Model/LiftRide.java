package Model;

public class LiftRide {
    private String time;
    private String liftID;
    private String waitTime;
    private String resortID;
    private String seasonID;
    private String skierID;
    private String dayID;

    public LiftRide(String time, String liftID, String waitTime, String resortID,
                    String seasonID, String skierID, String dayID) {
        this.time = time;
        this.liftID = liftID;
        this.waitTime = waitTime;
        this.resortID = resortID;
        this.seasonID = seasonID;
        this.skierID = skierID;
        this.dayID = dayID;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public void setLiftID(String liftID) {
        this.liftID = liftID;
    }

    public void setWaitTime(String waitTime) {
        this.waitTime = waitTime;
    }

    public void setResortID(String resortID) {
        this.resortID = resortID;
    }

    public void setSeasonID(String seasonID) {
        this.seasonID = seasonID;
    }

    public void setSkierID(String skierID) {
        this.skierID = skierID;
    }

    public void setDayID(String dayID) {
        this.dayID = dayID;
    }

    public String getTime() {
        return time;
    }

    public String getLiftID() {
        return liftID;
    }

    public String getWaitTime() {
        return waitTime;
    }

    public String getResortID() {
        return resortID;
    }

    public String getSeasonID() {
        return seasonID;
    }

    public String getSkierID() {
        return skierID;
    }

    public String getDayID() {
        return dayID;
    }

    @Override
    public String toString() {
        return "LiftRide{" +
                "time=" + time +
                ", liftID=" + liftID +
                ", waitTime=" + waitTime +
                ", resortID='" + resortID + '\'' +
                ", seasonID='" + seasonID + '\'' +
                ", skierID='" + skierID + '\'' +
                ", dayID='" + dayID + '\'' +
                '}';
    }
}
