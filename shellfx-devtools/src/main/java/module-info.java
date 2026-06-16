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

module com.techsenger.shellfx.devtools {
    requires org.slf4j;
    requires com.techsenger.connectorfx;
    requires com.techsenger.patternfx.core;
    requires com.techsenger.patternfx.mvp;
    requires com.techsenger.toolkit.core;
    requires com.techsenger.toolkit.fx;
    requires com.techsenger.annotations;
    requires com.techsenger.tabpanepro.core;
    requires com.techsenger.shellfx.core;
    requires com.techsenger.shellfx.shared;
    requires com.techsenger.shellfx.layout;
    requires com.techsenger.shellfx.dialogs;
    requires com.techsenger.shellfx.material;
    requires javafx.base;
    requires javafx.graphics;
    requires javafx.controls;
    requires jfx.incubator.richtext;
    requires atlantafx.base;

    exports com.techsenger.shellfx.devtools;
    exports com.techsenger.shellfx.devtools.component;
    exports com.techsenger.shellfx.devtools.environment;
    exports com.techsenger.shellfx.devtools.event;
    exports com.techsenger.shellfx.devtools.node;
    exports com.techsenger.shellfx.devtools.style;
    exports com.techsenger.shellfx.devtools.stylesheet;
}
