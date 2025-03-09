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

package com.techsenger.tabshell.core;

/**
 * Defines the scope of a close operation. Indicates whether the close event originated from a single tab
 * or the entire shell (main application window).
 *
 * @author Pavel Castornii
 */
public enum CloseScope {

    /**
     * The close request was triggered for a specific tab
     * (e.g., user clicked the tab's close button).
     */
    TAB,

    /**
     * The close request was triggered at the shell level
     * (e.g., main window closure, application exit).
     */
    SHELL
}
