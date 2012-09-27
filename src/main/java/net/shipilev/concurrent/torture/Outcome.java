package net.shipilev.concurrent.torture;

public enum Outcome {

    /**
     * Should always be present.
     */
    EXPECTED("Should always be present"),

    /**
     * This outcome may be present.
     */
    ACCEPTABLE("Generally present, may be missed at times"),

    /**
     * This outcome may not be present.
     */
    TRANSIENT("Generally absent, may be present at times"),

    /**
     * Should not be present.
     */
    NOT_EXPECTED("Should not be present"),

    ;

    private final String descr;

    Outcome(String descr) {
        this.descr = descr;
    }

    @Override
    public String toString() {
        return descr;
    }
}
