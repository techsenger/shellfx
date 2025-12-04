/*
 * Copyright 2024-2025 Pavel Castornii.
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

package com.techsenger.tabshell.jfx.model;

import com.techsenger.tabshell.jfx.inspector.PropertyInfo;

/**
 *
 * @author Pavel Castornii
 */
public final class UrlUtils {

    private static final String BASE_URL = "https://openjfx.io/javadoc/25/";

    public static final String CSS_REFERENCE_URL = BASE_URL + "javafx.graphics/javafx/scene/doc-files/cssref.html";

    public static String getPropertyJavadocUrl(String nodeClass, PropertyInfo info) {
        return BASE_URL + "search.html?q=" + nodeClass + "." + info.getAttribute().name() + "Property";
    }

    public static String getCssPropertyJavadocUrl(String nodeSimpleClass) {
        return CSS_REFERENCE_URL + "#" + nodeSimpleClass.toLowerCase();
    }

    private UrlUtils() {
        // empty
    }
}
