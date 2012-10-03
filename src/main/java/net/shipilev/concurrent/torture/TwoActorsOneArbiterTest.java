package net.shipilev.concurrent.torture;

/**
 * This test accepts two actors (threads actively mutating the state),
 * and one arbiter (thread observing the state *after* two actors finished).
 *
 * @param <S> specimen type
 */
public interface TwoActorsOneArbiterTest<S> extends Evaluator {

    /**
     * Create new object to work on.
     *
     * Conventions:
     *   - this method is called only within injector thread
     *   - this method should return new object at every call, no caching
     *
     * @return fresh specimen
     */
    S newSpecimen();

    /**
     * Body for actor 1.
     *
     * Conventions:
     *   - this method is called only by actor 1, only once per specimen
     *   - the order vs. other actor is unspecified
     *
     * @param specimen specimen to work on
     */
    void actor1(S specimen);

    /**
     * Body for actor 2.
     *
     * Conventions:
     *   - this method is called only by actor 2, only once per specimen
     *   - the order vs. other actor is unspecified
     *
     * @param specimen specimen to work on
     */
    void actor2(S specimen);

    /**
     * Body for the arbiter.
     *
     * Conventions:
     *   - this method is called only by arbiter thread, once per specimen
     *   - for any given specimen, arbiter would be called *after* both actors finished with the specimen
     *   - all memory effects on specimen would make the effect before arbitrate() call
     *      (i.e. for given specimen, (actor1() hb arbitrate()) and (actor2() hb arbitrate()))
     *   - arbiter can store the arbitrated state in the result array
     *   - arbiter can not store the reference to result array
     *
     * @param specimen specimen to work on
     * @param result result array
     * @see #resultSize()
     */
    void arbitrate(S specimen, byte[] result);


}
