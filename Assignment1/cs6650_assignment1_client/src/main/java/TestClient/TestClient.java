package TestClient;

import ClientPart2.ClientThreadPart2;

public class TestClient {

    public static void main(String[] args) throws InterruptedException {
        String ipAddress = "18.236.244.19";
        long start = System.currentTimeMillis();
        SingleThreadForLittlesLaw singleThreadForLittlesLaw = new SingleThreadForLittlesLaw(1, 20000, 0, 10, 10000,
                ipAddress, 40);
        Thread t = new Thread(singleThreadForLittlesLaw);
        t.start();

        t.join();
        long end = System.currentTimeMillis();
        System.out.println("Wall Time for 10000 requests single thread：" + (end - start));
    }
}
