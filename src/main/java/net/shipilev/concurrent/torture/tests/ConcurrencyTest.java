package net.shipilev.concurrent.torture.tests;

/**
 * Marks concurrency tests.
 *
 * This interface is used to track the test definitions.
 *
 * @author Aleksey Shipilev (aleksey.shipilev@oracle.com)
 */
public interface ConcurrencyTest {

    /**
     * Returns the expected result size.
     * @return result size
     */
    int resultSize();
}
