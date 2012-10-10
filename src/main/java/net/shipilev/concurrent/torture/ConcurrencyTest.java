package net.shipilev.concurrent.torture;

import net.shipilev.concurrent.torture.evaluators.Evaluator;

import java.io.Serializable;

/**
 * Marks tests, which can evaluate the tests results.
 *
 * Implementors of this interface should provide the basic test() method,
 * which judges the outcome given the result.
 *
 * @author Aleksey Shipilev (aleksey.shipilev@oracle.com)
 */
public interface ConcurrencyTest extends Serializable {

    Evaluator getEvaluator();

}
