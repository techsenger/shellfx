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

package com.techsenger.tabshell.core.history;

import com.techsenger.mvvm4fx.core.ComponentHistory;
import com.techsenger.toolkit.core.function.Factory;

/**
 *
 * @author Pavel Castornii
 */
public class DefaultHistoryManager implements HistoryManager {

    private final HistoryFile file;

    public DefaultHistoryManager(HistoryFile file) {
        this.file = file;
    }

    public <T extends ComponentHistory> T getHistory(Class<T> historyClass, Factory<T> factory) {
        //always on JavaFX thread.
        var history = (T) file.getHistoriesByClass().get(historyClass);
        if (history == null) {
            history = factory.create();
            history.setDefaultValues();
            file.getHistoriesByClass().put(historyClass, history);
        }
        return history;
    }
}
