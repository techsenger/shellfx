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

open module com.techsenger.tabshell.hex {
    requires org.slf4j;
    requires com.techsenger.tabshell.material;
    requires com.techsenger.tabshell.core;
    requires com.techsenger.tabshell.layout;
    requires com.techsenger.tabshell.storage;
    requires com.techsenger.tabshell.dialogs;
    requires com.techsenger.mvvm4fx.core;
    requires com.techsenger.toolkit.core;
    requires com.techsenger.toolkit.fx;
    requires org.fxmisc.flowless;
    requires javafx.base;
    requires javafx.controls;
    requires javafx.graphics;
    requires atlantafx.base;

    requires org.junit.jupiter.api;
    requires org.assertj.core;

}
