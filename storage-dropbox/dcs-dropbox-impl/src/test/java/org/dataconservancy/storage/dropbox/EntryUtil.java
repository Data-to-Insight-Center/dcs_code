/*
 * Copyright 2013 Johns Hopkins University
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

package org.dataconservancy.storage.dropbox;

import com.dropbox.client2.DropboxAPI;

import java.io.PrintStream;
import java.lang.reflect.Field;
import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * Util class for working with Dropbox Entry objects.
 */
public class EntryUtil {

    private static final String NL = "\n";

    private static final String IN = "  ";

    private static final String CL = ": ";

    /**
     * Prints the supplied entry to the stream.
     *
     * @param entry
     * @param stream
     * @throws IllegalAccessException
     */
    static void print(DropboxAPI.Entry entry, PrintStream stream) throws IllegalAccessException {
        Field[] fields = entry.getClass().getFields();

        StringBuilder sb = new StringBuilder("Entry: " + entry.parentPath() + entry.fileName() + NL);

        for (Field f : fields) {
            sb.append(IN).append(f.getName()).append(CL).append(f.get(entry)).append(NL);
        }

        stream.print(sb.toString());
    }

    /**
     * Asserts that two Entry objects are equal by asserting equality between all of their fields.
     *
     * @param entryOne
     * @param entryTwo
     * @throws IllegalAccessException
     */
    static void assertEqualsByValue(DropboxAPI.Entry entryOne, DropboxAPI.Entry entryTwo) throws IllegalAccessException {
        Field[] fieldsOne = entryOne.getClass().getFields();
        Field[] fieldsTwo = entryTwo.getClass().getFields();

        assertEquals(fieldsOne, fieldsTwo);
        for (int i = 0; i < fieldsOne.length; i++) {
            final Object value1 = fieldsOne[i].get(entryOne);
            final Object value2 = fieldsTwo[i].get(entryTwo);
            assertEquals("'" + fieldsOne[i].getName() + "' values differ.", value1, value2);
        }
    }

    /**
     * Asserts that two Entry objects are equal by asserting equality between all fields, except those specified in
     * {@code ignoredFields}.
     *
     * @param entryOne
     * @param entryTwo
     * @param ignoredFields
     * @throws IllegalAccessException
     */
    static void assertEqualsByValue(DropboxAPI.Entry entryOne, DropboxAPI.Entry entryTwo, String... ignoredFields)
            throws IllegalAccessException {
        Field[] fieldsOne = entryOne.getClass().getFields();
        Field[] fieldsTwo = entryTwo.getClass().getFields();

        assertEquals(fieldsOne, fieldsTwo);
        NEXTFIELD : for (int i = 0; i < fieldsOne.length; i++) {
            for (String ignoredField : ignoredFields) {
                if (ignoredField.equals(fieldsOne[i].getName())) {
                    continue NEXTFIELD;
                }
            }
            final Object value1 = fieldsOne[i].get(entryOne);
            final Object value2 = fieldsTwo[i].get(entryTwo);
            assertEquals("'" + fieldsOne[i].getName() + "' values differ.", value1, value2);
        }
    }

    /**
     * Asserts that two List&lt;Entry> objects are equal by asserting equality of the list members.
     *
     * @param entryOne
     * @param entryTwo
     * @throws IllegalAccessException
     */
    static void assertEqualsByValue(List<DropboxAPI.Entry> entryOne, List<DropboxAPI.Entry> entryTwo)
            throws IllegalAccessException {
        assertEquals(entryOne.size(), entryTwo.size());
        for (int i = 0; i < entryOne.size(); i++) {
            assertEqualsByValue(entryOne.get(i), entryTwo.get(i));
        }
    }

    /**
     * Asserts that two List&lt;Entry> objects are equal by asserting equality of the list members. except those specified in
     * {@code ignoredFields}.
     *
     * @param entryOne
     * @param entryTwo
     * @throws IllegalAccessException
     */
    static void assertEqualsByValue(List<DropboxAPI.Entry> entryOne, List<DropboxAPI.Entry> entryTwo, String... ignoredFields)
            throws IllegalAccessException {
        assertEquals(entryOne.size(), entryTwo.size());
        for (int i = 0; i < entryOne.size(); i++) {
            assertEqualsByValue(entryOne.get(i), entryTwo.get(i), ignoredFields);
        }
    }
}
