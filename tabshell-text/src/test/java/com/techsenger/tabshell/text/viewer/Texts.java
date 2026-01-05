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

package com.techsenger.tabshell.text.viewer;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Pavel Castornii
 */
final class Texts {

    static final String TEXT =
            "<xml>\n"
            + "    <modelVersion>4.0.0</modelVersion>\n"
            + "    <groupId>com.techsenger.ansi4j</groupId>\n"
            + "    <artifactId>ansi4j</artifactId>\n"
            + "    <version>1.0.0</version>\n"
            + "    <packaging>pom</packaging>\n"
            + "    <name>ANSI4J</name>\n"
            + "    <description>Version 1 released in January 2020 has many features</description>\n"
            + "\n"
            + "    <properties>\n"
            + "        <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>\n"
            + "        <maven.compiler.source>11</maven.compiler.source>\n"
            + "        <maven.compiler.target>11</maven.compiler.target>\n"
            + "    </properties>\n"
            + "\n"
            + "    <modules>\n"
            + "        <module>ansi4j-core-api</module>\n"
            + "        <module>ansi4j-core-impl</module>\n"
            + "        <module>ansi4j-core-it</module>\n"
            + "        <module>ansi4j-css-api</module>\n"
            + "        <module>ansi4j-css-impl</module>\n"
            + "    </modules>\n"
            + "</xml>";

    static final String REPLACED_ALL_TEXT =
            "<xml>\n"
            + "    <modelVersion>4.0.0</modelVersion>\n"
            + "    <groupId>com.techsenger.test</groupId>\n"
            + "    <artifactId>test</artifactId>\n"
            + "    <version>1.0.0</version>\n"
            + "    <packaging>pom</packaging>\n"
            + "    <name>test</name>\n"
            + "    <description>Version 1 released in Jtest</description>\n"
            + "\n"
            + "    <properties>\n"
            + "        <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>\n"
            + "        <maven.compiler.source>11</maven.compiler.source>\n"
            + "        <maven.compiler.target>11</maven.compiler.target>\n"
            + "    </properties>\n"
            + "\n"
            + "    <modules>\n"
            + "        <module>test</module>\n"
            + "        <module>test</module>\n"
            + "        <module>test</module>\n"
            + "        <module>test</module>\n"
            + "        <module>test</module>\n"
            + "    </modules>\n"
            + "</xml>";

    public static List<String> rangesToSubStrings(List<MatchRange> ranges, String text) {
        var result = new ArrayList<String>();
        for (var r : ranges) {
            result.add(text.substring(r.getStart(), r.getEnd()));
        }
        return result;
    }

    private Texts() {
        //empty
    }

}
