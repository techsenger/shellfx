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

package com.techsenger.tabshell.devtools;

import com.techsenger.connectorfx.scenegraph.Element;

/**
 *
 * @author Pavel Castornii
 */
public final class ElementUtils {

    public static String getTitle(Element element) {
        var text = element.getClassInfo().simpleClassName();
        if (element.getNodeProperties() != null) {
            var styleClasses = element.getNodeProperties().styleClass();
            if (styleClasses != null && !styleClasses.isEmpty()) {
                text += " class=\"" + styleClasses + "\"";
            }
            var id = element.getNodeProperties().id();
            if (id != null) {
                text += " id=\"" + id + "\"";
            }
        }
        return text;
    }

    private ElementUtils() {

    }
}
