package net.shipilev.concurrent.torture;

/**
 * Marks tests, which can evaluate the tests results.
 *
 * Implementors of this interface should provide the basic test() method,
 * which judges the outcome given the result.
 *
 * @author Aleksey Shipilev (aleksey.shipilev@oracle.com)
 */
public interface ConcurrencyTest {

    /**
     * Expected result size.
     * @return result size.
     */
    int resultSize();

    /**
     * Analyze the result.
     *
     * @param result result to be analyzed
     * @return graded outcome
     */
    Outcome test(byte[] result);

}
