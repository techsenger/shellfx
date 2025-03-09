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

module com.techsenger.tabshell.kit.dialog {
    requires org.slf4j;
    requires com.techsenger.toolkit.core;
    requires com.techsenger.mvvm4fx.core;
    requires com.techsenger.tabshell.core;
    requires com.techsenger.tabshell.material;
    requires com.techsenger.tabshell.kit.core;
    requires com.techsenger.tabshell.kit.material;
    requires javafx.base;
    requires javafx.graphics;
    requires javafx.controls;
    requires com.techsenger.toolkit.fx;
    requires atlantafx.base;
    requires org.fxmisc.richtext;

    exports com.techsenger.tabshell.kit.dialog;
    exports com.techsenger.tabshell.kit.dialog.alert;
    exports com.techsenger.tabshell.kit.dialog.file;
    exports com.techsenger.tabshell.kit.dialog.page;
    exports com.techsenger.tabshell.kit.dialog.style;

    opens com.techsenger.tabshell.kit.dialog.style;
}
