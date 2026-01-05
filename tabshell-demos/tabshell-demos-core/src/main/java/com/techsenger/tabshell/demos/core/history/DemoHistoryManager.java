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

package com.techsenger.tabshell.demos.core.history;

import com.techsenger.patternfx.core.History;
import com.techsenger.tabshell.core.history.HistoryManager;
import com.techsenger.toolkit.core.function.Factory;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * For the demo application the history map is not saved to a file. Always on the JavaFX thread.
 *
 * @author Pavel Castornii
 */
public class DemoHistoryManager implements HistoryManager {

    private final Map<Class<? extends History>, History> historiesByClass = new HashMap<>();

    private final Map<UUID, History> historiesByUuid = new HashMap<>();

    @Override
    public <T extends History> T getHistory(Class<T> historyClass) {
        return (T) this.historiesByClass.get(historyClass);
    }

    @Override
    public <T extends History> T getOrCreateHistory(Class<T> historyClass, Factory<T> factory) {
        var history = getHistory(historyClass);
        if (history == null) {
            history = factory.create();
            putHistory(historyClass, history);
        }
        return history;
    }

    @Override
    public <T extends History> void putHistory(Class<T> historyClass, T history) {
        this.historiesByClass.put(historyClass, history);
    }

    @Override
    public History getHistory(UUID uuid) {
        return this.historiesByUuid.get(uuid);
    }

    @Override
    public History getOrCreateHistory(UUID uuid, Factory<? extends History> factory) {
        var history = getHistory(uuid);
        if (history == null) {
            history = factory.create();
            putHistory(uuid, history);
        }
        return history;
    }

    @Override
    public void putHistory(UUID uuid, History history) {
        this.historiesByUuid.put(uuid, history);
    }

    @Override
    public <T extends History> T removeHistory(Class<T> historyClass) {
        return (T) this.historiesByClass.remove(historyClass);
    }

    @Override
    public History removeHistory(UUID uuid) {
        return this.historiesByUuid.remove(uuid);
    }
}
