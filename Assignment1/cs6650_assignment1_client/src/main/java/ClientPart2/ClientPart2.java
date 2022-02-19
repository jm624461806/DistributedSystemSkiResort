package ClientPart2;

import ClientPart1.ClientUtil;
import Model.HttpMethod;
import Model.Record;
import org.apache.commons.cli.*;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.atomic.AtomicInteger;

public class ClientPart2 {
    private static final Integer maxNumOfThreads = 1024;
    private static final Integer maxNumOfSkiers = 100000;
    private static final Integer minNumOfLift = 5;
    private static final Integer maxNumOfLift = 60;

    private static ClientUtil clientUtil = new ClientUtil();
    private static RecordUtil recordUtil = new RecordUtil();
    private static ChartUtil chartUtil = new ChartUtil();
    private static Integer numThreads = 16;
    private static Integer numOfSkiers = 1000;
    private static Integer numSkiLifts = 40;
    private static Integer numRuns = 10;
    private static String ipAddress;

    public static void main(String[] args) throws InterruptedException {
        parseCommandLine(args);

        int phaseOneThreads = numThreads / 4;
        int phaseTwoThreads = numThreads;
        int phaseThreeThreads = numThreads / 10;

        int numOfRequestPhaseOne = (int) (numRuns * 0.2 * (numOfSkiers / (double)phaseOneThreads));
        int numOfRequestPhaseTwo = (int) (numRuns * 0.6 * (numOfSkiers / (double)phaseTwoThreads));
        int numOfRequestPhaseThree = (int) (numRuns * 0.1);

        int phaseOneEndThreads = (int) Math.round(phaseOneThreads * 0.2);
        int phaseTwoEndThreads = (int) Math.round(phaseTwoThreads * 0.2);

        Integer skierIDsRangePhaseOne = numOfSkiers / phaseOneThreads;
        Integer skierIDsRangePhaseTwo = numOfSkiers / phaseTwoThreads;
        Integer skierIDsRangePhaseThree = numOfSkiers / phaseThreeThreads;

        int phaseOneStartTime = 1;
        int phaseOneEndTime = 90;

        int phaseTwoStartTime = 91;
        int phaseTwoEndTime = 360;

        int phaseThreeStartTime = 361;
        int phaseThreeEndTime = 420;

        CountDownLatch phaseOne = new CountDownLatch(phaseOneEndThreads);
        CountDownLatch phaseTwo = new CountDownLatch(phaseTwoEndThreads);
        CountDownLatch allPhases = new CountDownLatch(phaseOneThreads + phaseTwoThreads + phaseThreeThreads);

        AtomicInteger totalSuccess = new AtomicInteger(0);
        AtomicInteger totalFails = new AtomicInteger(0);

        BlockingQueue<Record> bQueue = new LinkedBlockingDeque<>();
        List<Record> recordList = new ArrayList<>();

        String csv = "records-" + numThreads + "-threads.csv";
        RecordWriter recordWriter = new RecordWriter(bQueue, csv, recordList);
        Thread consumer = new Thread(recordWriter);
        consumer.start();

        long start = System.currentTimeMillis();

        // phase one
        ClientUtil.createAndStartTheThreadPart2(phaseOneThreads, skierIDsRangePhaseOne, phaseOneStartTime,
                phaseOneEndTime, phaseOne, numSkiLifts, allPhases, numOfRequestPhaseOne, ipAddress, totalSuccess,
                totalFails, bQueue, numOfSkiers);

        phaseOne.await();


        ClientUtil.createAndStartTheThreadPart2(phaseTwoThreads, skierIDsRangePhaseTwo, phaseTwoStartTime,
                phaseTwoEndTime, phaseTwo, numSkiLifts, allPhases, numOfRequestPhaseTwo, ipAddress,
                totalSuccess, totalFails, bQueue, numOfSkiers);
        phaseTwo.await();


        ClientUtil.createAndStartTheThreadPart2(phaseThreeThreads,skierIDsRangePhaseThree, phaseThreeStartTime,
                phaseThreeEndTime, null, numSkiLifts, allPhases, numOfRequestPhaseThree, ipAddress,
                totalSuccess, totalFails, bQueue, numOfSkiers);
        allPhases.await();

        long end = System.currentTimeMillis();
        long wallTime = end - start;

        //ClientUtil.printOutTheData(wallTime, totalSuccess.get(), totalFails.get());
        bQueue.put(new Record(-1,  HttpMethod.POST, -1, -1));
        consumer.join();

        recordUtil.generateReport(wallTime, csv, totalFails.get(), totalSuccess.get());
        ChartUtil.generateChartData(recordList, numThreads);
    }

    private static void parseCommandLine(String[] args) {
        Options options = new Options();
        Option numThreadsOpt = new Option("numTh", "numThreads", true,
                "maximum number of threads to run ");
        numThreadsOpt.setRequired(true);
        options.addOption(numThreadsOpt);

        Option numSkiersOpt = new Option("numSk", "numSkiers", true, "number of skiers " +
                "generate lift rides" );
        numSkiersOpt.setRequired(true);
        options.addOption(numSkiersOpt);

        Option numSkiLiftsOpt = new Option("numSkLf", "numLifts", true,
                "number of ski lifts");
        numSkiLiftsOpt.setRequired(true);
        options.addOption(numSkiLiftsOpt);

        Option meanNumOfSkiLiftsPerSkierRidePerDayOpt= new Option("numRuns",
                "numRuns", true,
                "mean numbers of ski lifts each skier rides each day");
        meanNumOfSkiLiftsPerSkierRidePerDayOpt.setRequired(true);
        options.addOption(meanNumOfSkiLiftsPerSkierRidePerDayOpt);

        Option ipOpt = new Option("ip", "ipAddress", true,
                "ip/port address of the server");
        ipOpt.setRequired(true);
        options.addOption(ipOpt);

        CommandLineParser parser = new DefaultParser();
        HelpFormatter formatter = new HelpFormatter();
        CommandLine cmd = null;
        try {
            cmd = parser.parse(options, args);
        } catch (ParseException e) {
            System.out.println(e.getMessage());
            formatter.printHelp("Somethings wrong: ", options);
            System.exit(1);
        }
        if (!isValidCommand(cmd)) {
            System.out.println("Invalid command line parameters");
            System.exit(1);
        }
    }

    private static boolean isValidCommand(CommandLine cmd) {
        // max thread > 0 and <= 1024
        String commandLineMaxThreads = cmd.getOptionValue("numThreads");
        if(commandLineMaxThreads != null && !commandLineMaxThreads.isEmpty()) {
            if (!clientUtil.isValidNumber(commandLineMaxThreads, 1, maxNumOfThreads)) return false;
            numThreads = Integer.parseInt(commandLineMaxThreads);
        }

        // num of skier max 100000
        String commandLineNumSkiers = cmd.getOptionValue("numSkiers");
        if(commandLineNumSkiers != null && !commandLineNumSkiers.isEmpty()) {
            if (!clientUtil.isValidNumber(commandLineNumSkiers, 1, maxNumOfSkiers)) return false;
            numOfSkiers = Integer.parseInt(commandLineNumSkiers);
        }


        // number of ski lifts range 5 - 60
        String commandLineNumSkierLifts = cmd.getOptionValue("numLifts");
        if(commandLineNumSkierLifts != null && !commandLineNumSkierLifts.isEmpty()) {
            if (!clientUtil.isValidNumber(commandLineNumSkierLifts, minNumOfLift, maxNumOfLift)) return false;
            numSkiLifts = Integer.parseInt(commandLineNumSkierLifts);
        }

        // number of run range 0 - 20
        String commandLineNumRuns = cmd.getOptionValue("numRuns");
        if(commandLineNumSkierLifts == null && !commandLineNumSkierLifts.isEmpty()) {
            if (!clientUtil.isValidNumber(commandLineNumSkierLifts, minNumOfLift, maxNumOfLift)) return false;
            numSkiLifts = Integer.parseInt(commandLineNumSkierLifts);
        }

        String commandLineIp = cmd.getOptionValue("ipAddress");
        if (commandLineIp == null || commandLineIp.isEmpty()) return false;
        ipAddress = commandLineIp;

        return true;
    }

}
