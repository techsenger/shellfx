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

module com.techsenger.shellfx.demo {
    requires com.techsenger.annotations;
    requires com.techsenger.toolkit.core;
    requires com.techsenger.toolkit.fx;
    requires com.techsenger.shellfx.core;
    requires com.techsenger.shellfx.material;
    requires com.techsenger.patternfx.core;
    requires com.techsenger.patternfx.mvp;
    requires com.techsenger.shellfx.layout;
    requires com.techsenger.shellfx.shared;
    requires com.techsenger.shellfx.devtools;
    requires com.techsenger.shellfx.storage;
    requires com.techsenger.shellfx.dialogs;
    requires com.techsenger.shellfx.icons;
    requires com.techsenger.tabpanepro.core;

    requires org.slf4j;
    requires org.apache.logging.log4j;
    requires org.apache.logging.log4j.core;
    requires org.apache.logging.log4j.slf4j2.impl;
    requires javafx.base;
    requires javafx.graphics;
    requires javafx.controls;
    requires atlantafx.base;

    opens com.techsenger.shellfx.demo;
}
