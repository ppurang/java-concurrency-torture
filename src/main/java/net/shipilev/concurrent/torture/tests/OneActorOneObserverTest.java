/*
 * Copyright (c) 2012 Aleksey Shipilev
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.shipilev.concurrent.torture.tests;

/**
 * This test accepts single actors (thread actively mutating the state),
 * and one observer (thread observing the state *concurrently* with actor doing dirty work).
 *
 * Shared state is represented by state object. Runners will ensure enough fresh state objects would
 * be provided to the tests methods to unfold even the finest races.
 *
 * @param <S> state object type
 *
 * @author Aleksey Shipilev (aleksey.shipilev@oracle.com)
 */
public interface OneActorOneObserverTest<S> extends ConcurrencyTest {

    /**
     * Create new object to work on.
     *
     * Conventions:
     *   - this method is called only within the exclusive thread
     *   - this method should return new object at every call; answering cached object will interfere with test correctness
     *   - there are safe publication guarantees enforced by Runner
     *       (i.e. for any given s, (newState(s) hb actor1(s)) and (newState(s) hb observe(s))
     *
     * @return fresh state object
     */
     S newState();

    /**
     * Body for actor 1.
     *
     * Conventions:
     *   - this method is called only by actor1 thread, and only once per state
     *
     * @param state state to work on
     */
    void actor1(S state);

    /**
     * Body for the observer.
     *
     * Conventions:
     *   - this method is called only by observer thread, and only once per state
     *   - for any given state, observer would run concurrently with the actor
     *   - observer can store the observed state in the result array
     *   - observer should not rely on the default values in the result array, and should set all elements on every call
     *   - observer can not store the reference to result array
     *
     * @param state state to work on
     * @param result result array
     */
    void observe(S state, byte[] result);

}
