package net.shipilev.concurrent.torture;

public class Constants {

    /**
     * Time to run each test
     */
    public static final int TIME_MSEC = Integer.getInteger("time", 5_000);

    /**
     * Number of internal loops to do before making heavy operations (i.e. enforcing ordering)
     */
    public static final int LOOPS = Integer.getInteger("loops", 10_000);

}
