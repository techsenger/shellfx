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

package com.techsenger.tabshell.text.viewer;

import com.techsenger.patternfx.core.History;
import com.techsenger.tabshell.core.history.HistoryManager;
import com.techsenger.toolkit.core.function.Factory;
import java.util.UUID;

/**
 *
 * @author Pavel Castornii
 */
public class DummyHistoryManager implements HistoryManager {

    @Override
    public <T extends History> T getHistory(Class<T> historyClass) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public <T extends History> T getOrCreateHistory(Class<T> historyClass, Factory<T> factory) {
        return (T) new FindPanelHistory();
    }

    @Override
    public <T extends History> void putHistory(Class<T> historyClass, T history) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public <T extends History> T removeHistory(Class<T> historyClass) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public History getHistory(UUID uuid) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public History getOrCreateHistory(UUID uuid, Factory<? extends History> factory) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void putHistory(UUID uuid, History history) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public History removeHistory(UUID uuid) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
