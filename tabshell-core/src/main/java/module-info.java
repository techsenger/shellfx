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

module com.techsenger.tabshell.core {
    requires com.techsenger.toolkit.core;
    requires com.techsenger.toolkit.fx;
    requires org.slf4j;
    requires javafx.base;
    requires javafx.graphics;
    requires javafx.controls;
    requires javafx.swing;
    requires atlantafx.base;
    requires jakarta.xml.bind;
    requires com.techsenger.stagepro.core;
    requires com.techsenger.tabpanepro.core;
    requires com.techsenger.patternfx.core;
    requires com.techsenger.tabshell.material;

    exports com.techsenger.tabshell.core;
    exports com.techsenger.tabshell.core.area;
    exports com.techsenger.tabshell.core.dialog;
    exports com.techsenger.tabshell.core.element;
    exports com.techsenger.tabshell.core.history;
    exports com.techsenger.tabshell.core.menu;
    exports com.techsenger.tabshell.core.page;
    exports com.techsenger.tabshell.core.registry;
    exports com.techsenger.tabshell.core.settings;
    exports com.techsenger.tabshell.core.settings.xml;
    exports com.techsenger.tabshell.core.tab;

    opens com.techsenger.tabshell.core.settings.xml to jakarta.xml.bind;
    opens com.techsenger.tabshell.core.style;

}
