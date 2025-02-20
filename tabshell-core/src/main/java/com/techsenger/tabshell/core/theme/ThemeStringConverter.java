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

package com.techsenger.tabshell.core.theme;

import javafx.util.StringConverter;

/**
 *
 * @author Pavel Castornii
 */
public class ThemeStringConverter extends StringConverter<TabShellTheme> {

    @Override
    public String toString(TabShellTheme t) {
        if (t != null) {
            return t.getName();
        } else {
            return null;
        }
    }

    @Override
    public TabShellTheme fromString(String string) {
        throw new IllegalStateException();
    }
}
