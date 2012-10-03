package net.shipilev.concurrent.torture;

/**
 * Marks evaluators, which can evaluate the tests results.
 *
 * Implementors of this interface should provide the basic test() method, which judges the outcome given the result.
 */
public interface Evaluator {

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
