/*
 * Copyright 2024-2026 Pavel Castornii.
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

package com.techsenger.shellfx.demo.styles;

import java.util.List;

/**
 *
 * @author Pavel Castornii
 */
final class StylePageData {

    static final String DEFAULT_STYLE_NAME = "default";

    static final String TEXT = "ABCQWZabcfgjqry";

    static final List<String> TEXTS = List.of(TEXT, TEXT, TEXT, TEXT, TEXT);

    static final List<Person> PERSONS = List.of(
            new Person("Alice",   "Martin",   "Female", "United States"),
            new Person("Bob",     "Johnson",  "Male",   "United Kingdom"),
            new Person("Carol",   "Williams", "Female", "France"),
            new Person("David",   "Brown",    "Male",   "Germany"),
            new Person("Eva",     "Davis",    "Female", "Japan"),
            new Person("Frank",   "Miller",   "Male",   "Australia"),
            new Person("Grace",   "Wilson",   "Female", "Italy"),
            new Person("Henry",   "Moore",    "Male",   "Spain"),
            new Person("Isla",    "Taylor",   "Female", "Italy"),
            new Person("Jack",    "Anderson", "Male",   "Netherlands"));

    private StylePageData() {
        // empty
    }
}
