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

module com.techsenger.tabshell.layout {
    requires org.slf4j;
    requires com.techsenger.toolkit.core;
    requires com.techsenger.toolkit.fx;
    requires com.techsenger.patternfx.core;
    requires com.techsenger.patternfx.mvp;
    requires com.techsenger.tabshell.material;
    requires com.techsenger.tabshell.core;
    requires com.techsenger.tabshell.shared;
    requires com.techsenger.tabpanepro.core;
    requires javafx.base;
    requires javafx.graphics;
    requires javafx.controls;
    requires javafx.swing;
    requires org.controlsfx.controls;
    requires atlantafx.base;
    requires jakarta.xml.bind;

    exports com.techsenger.tabshell.layout;
    exports com.techsenger.tabshell.layout.dock;
    exports com.techsenger.tabshell.layout.tabhost;
}
