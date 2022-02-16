package Model;

public class Record {
    private long startTime;
    private HttpMethod reqType;
    private int resCode;
    private long latency;

    public Record(long startTime, HttpMethod reqType, int resCode, long latency) {
        this.startTime = startTime;
        this.reqType = reqType;
        this.resCode = resCode;
        this.latency = latency;
    }

    public long getStartTime() {
        return this.startTime;
    }

    public HttpMethod getReqType() {
        return this.reqType;
    }

    public int getResCode() {
        return this.resCode;
    }

    public long getLatency() {
        return this.latency;
    }
}
