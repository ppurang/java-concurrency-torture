package net.shipilev.concurrent.torture;

/**
 * This test accepts single actors (thread actively mutating the state),
 * and one observer (thread observing the state *concurrently* with actor doing dirty work).
 *
 * @param <S> specimen type
 */
public interface OneActorOneObserverTest<S> extends Evaluator {

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
     *
     * @param specimen specimen to work on
     */
    void actor1(S specimen);

    /**
     * Body for the observer.
     *
     * Conventions:
     *   - this method is called only by observer thread, once per specimen
     *   - for any given specimen, observer would run concurrently with the actor
     *   - observer can store the observed state in the result array
     *   - observer can not store the reference to result array
     *
     * @param specimen specimen to work on
     * @param result result array
     */
    void observe(S specimen, byte[] result);

}
