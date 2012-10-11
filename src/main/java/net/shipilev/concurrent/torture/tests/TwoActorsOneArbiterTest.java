package net.shipilev.concurrent.torture.tests;

/**
 * This test accepts two actors (threads actively mutating the state),
 * and one arbiter (thread observing the state *after* two actors finished).
 *
 * Shared state is represented by state object. Runners will ensure enough fresh state objects would
 * be provided to the tests methods to unfold even the finest races.
 *
 * @param <S> state object type
 * @author Aleksey Shipilev (aleksey.shipilev@oracle.com)
 */
public interface TwoActorsOneArbiterTest<S> extends ConcurrencyTest {

    /**
     * Create new object to work on.
     *
     * Conventions:
     *   - this method is called only within the exclusive thread
     *   - this method should return new object at every call; answering cached object will interfere with test correctness
     *   - there are safe publication guarantees enforced by Runner
     *       (i.e. for any given s, (newState(s) hb actor1(s)) and (newState(s) hb actor2(s))
     *
     * @return fresh state object
     */
    S newState();

    /**
     * Body for actor 1.
     *
     * Conventions:
     *   - this method is called only by actor1 thread, and only once per state
     *   - the order vs. other actors is unspecified
     *
     * @param state state to work on
     */
    void actor1(S state);

    /**
     * Body for actor 2.
     *
     * Conventions:
     *   - this method is called only by actor2 thread, only once per state
     *   - the order vs. other actors is unspecified
     *
     * @param state state to work on
     */
    void actor2(S state);

    /**
     * Body for the arbiter.
     *
     * Conventions:
     *   - this method is called only by arbiter thread, once per state
     *   - for any given state, arbiter would be called *after* both actors finished with the state
     *   - all memory effects on state would make the effect before arbitrate() call
     *      (i.e. for given state, (actor1() hb arbitrate()) and (actor2() hb arbitrate()))
     *   - arbiter can store the arbitrated state in the result array
     *   - arbiter should not rely on the default values in the result array, and should set all elements on every call
     *   - arbiter can not store the reference to result array
     *
     * @param state state to work on
     * @param result result array
     * @see #resultSize()
     */
    void arbitrate(S state, byte[] result);


}
