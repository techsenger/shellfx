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

module com.techsenger.tabshell.demo {
    requires com.techsenger.toolkit.core;
    requires com.techsenger.toolkit.fx;
    requires com.techsenger.tabshell.core;
    requires com.techsenger.tabshell.material;
    requires com.techsenger.patternfx.core;
    requires com.techsenger.patternfx.mvp;
    requires com.techsenger.tabshell.layout;
    requires com.techsenger.tabshell.shared;
//    requires com.techsenger.tabshell.text;
//    requires com.techsenger.tabshell.hex;
    requires com.techsenger.tabshell.devtools;
    requires com.techsenger.tabshell.web;
    requires com.techsenger.tabshell.storage;
    requires com.techsenger.tabshell.dialogs;
    requires com.techsenger.tabshell.terminal;
//    requires com.techsenger.tabshell.registrars;
    requires com.techsenger.tabshell.icons;
    requires com.techsenger.tabshell.demos.core;
    requires com.techsenger.tabpanepro.core;

    requires org.slf4j;
    requires org.apache.logging.log4j;
    requires org.apache.logging.log4j.core;
    requires org.apache.logging.log4j.slf4j2.impl;
    requires javafx.base;
    requires javafx.graphics;
    requires javafx.controls;
    requires atlantafx.base;

    opens com.techsenger.tabshell.demo;
}
