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

module com.techsenger.tabshell.material {
    requires com.techsenger.patternfx.core;
    requires com.techsenger.toolkit.core;
    requires com.techsenger.toolkit.fx;
    requires org.slf4j;
    requires javafx.base;
    requires javafx.graphics;
    requires javafx.controls;
    requires atlantafx.base;
    requires org.fxmisc.richtext;
    requires reactfx;
    requires org.fxmisc.flowless;
    requires org.fxmisc.undo;
    requires wellbehavedfx;

    exports com.techsenger.tabshell.material;
    exports com.techsenger.tabshell.material.button;
    exports com.techsenger.tabshell.material.icon;
    exports com.techsenger.tabshell.material.list;
    exports com.techsenger.tabshell.material.menu;
    exports com.techsenger.tabshell.material.pane;
    exports com.techsenger.tabshell.material.style;
    exports com.techsenger.tabshell.material.table;
    exports com.techsenger.tabshell.material.textarea;
    exports com.techsenger.tabshell.material.theme;

    opens com.techsenger.tabshell.material.style;
}
