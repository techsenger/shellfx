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

package com.techsenger.tabshell.demos.core.history;

import com.techsenger.mvvm4fx.core.ComponentHistory;
import com.techsenger.tabshell.core.history.HistoryManager;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * For the demo application the history map is not saved to a file. Always on the JavaFX thread.
 *
 * @author Pavel Castornii
 */
public class DemoHistoryManager implements HistoryManager {

    private final Map<Class<? extends ComponentHistory>, ComponentHistory<?>> historiesByClass = new HashMap<>();

    private final Map<UUID, ComponentHistory<?>> historiesByUuid = new HashMap<>();

    @Override
    public <T extends ComponentHistory> T getHistory(Class<T> historyClass) {
        return (T) this.historiesByClass.get(historyClass);
    }

    @Override
    public <T extends ComponentHistory> void putHistory(Class<T> historyClass, T history) {
        this.historiesByClass.put(historyClass, history);
    }

    @Override
    public ComponentHistory getHistory(UUID uuid) {
        return this.historiesByUuid.get(uuid);
    }

    @Override
    public void putHistory(UUID uuid, ComponentHistory history) {
        this.historiesByUuid.put(uuid, history);
    }

    @Override
    public <T extends ComponentHistory> T removeHistory(Class<T> historyClass) {
        return (T) this.historiesByClass.remove(historyClass);
    }

    @Override
    public ComponentHistory removeHistory(UUID uuid) {
        return this.historiesByUuid.remove(uuid);
    }





}
