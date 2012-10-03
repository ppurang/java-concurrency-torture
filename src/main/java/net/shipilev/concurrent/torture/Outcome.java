package net.shipilev.concurrent.torture;

public enum Outcome {

    /**
     * Should always be present.
     */
    EXPECTED("Should always be present"),

    /**
     * This outcome may be present.
     */
    ACCEPTABLE("General case"),

    /**
     * This outcome may not be present.
     */
    TRANSIENT("Non-general case"),

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
