/*
 * Copyright 2019-present Open Networking Foundation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.atomix.client;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

/**
 * Versioned unit tests.
 */
public class VersionedTest {

    private final Versioned<Integer> stats1 = new Versioned<>(1, 2, 3);

    private final Versioned<Integer> stats2 = new Versioned<>(1, 2);

    /**
     * Tests the creation of the MapEvent object.
     */
    @Test
    public void testConstruction() {
        assertEquals(Integer.valueOf(1), stats1.value());
        assertEquals(2, stats1.version());
        assertEquals(3, stats1.creationTime());
    }

    /**
     * Maps an Integer to a String - Utility function to test the map function.
     *
     * @param a Actual Integer parameter.
     * @return String Mapped valued.
     */
    public static String transform(Integer a) {
        return Integer.toString(a);
    }

    /**
     * Tests the map function.
     */
    @Test
    public void testMap() {
        Versioned<String> tempObj = stats1.map(VersionedTest::transform);
        assertEquals("1", tempObj.value());
    }

    /**
     * Tests the valueOrElse method.
     */
    @Test
    public void testOrElse() {
        Versioned<String> vv = new Versioned<>("foo", 1);
        Versioned<String> nullVV = null;
        assertEquals("foo", Versioned.valueOrElse(vv, "bar"));
        assertEquals("bar", Versioned.valueOrElse(nullVV, "bar"));
    }

    /**
     * Tests the equals, hashCode and toString methods using Guava EqualsTester.
     */
    @Test
    public void testEquals() {
        assertEquals(stats1, stats1);
        assertNotEquals(stats1, stats2);
    }

}
