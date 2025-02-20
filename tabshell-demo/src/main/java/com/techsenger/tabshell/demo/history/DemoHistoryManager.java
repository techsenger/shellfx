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

package com.techsenger.tabshell.demo.history;

import com.techsenger.mvvm4fx.core.ComponentHistory;
import com.techsenger.tabshell.core.history.HistoryManager;
import com.techsenger.toolkit.core.function.Factory;
import java.util.HashMap;
import java.util.Map;

/**
 * For the demo application the history map is not saved to a file. Always on the JavaFX thread.
 *
 * @author Pavel Castornii
 */
public class DemoHistoryManager implements HistoryManager {

    private final Map<Class<? extends ComponentHistory>, ComponentHistory> historiesByClass = new HashMap<>();

    @Override
    public <T extends ComponentHistory> T getHistory(Class<T> historyClass, Factory<T> factory) {
        var history = (T) this.historiesByClass.get(historyClass);
        if (history == null) {
            history = factory.create();
            history.setDefaultValues();
            this.historiesByClass.put(historyClass, history);
        }
        return history;
    }
}
