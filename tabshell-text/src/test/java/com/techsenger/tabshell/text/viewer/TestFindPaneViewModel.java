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

package com.techsenger.tabshell.text.viewer;

import com.techsenger.mvvm4fx.core.ComponentHistory;
import com.techsenger.tabshell.core.history.HistoryManager;
import java.util.UUID;

/**
 *
 * @author Pavel Castornii
 */
public class TestFindPaneViewModel  extends DefaultFindPaneViewModel {

    private static HistoryManager createHistoryManager() {
        return new HistoryManager() {
            @Override
            public <T extends ComponentHistory> T getHistory(Class<T> historyClass) {
                var history = new FindPaneHistory();
                history.setDefaultValues();
                return (T) history;
            }

            @Override
            public <T extends ComponentHistory> void putHistory(Class<T> historyClass, T history) {
                throw new UnsupportedOperationException("Not supported yet.");
            }

            @Override
            public ComponentHistory getHistory(UUID uuid) {
                throw new UnsupportedOperationException("Not supported yet.");
            }

            @Override
            public void putHistory(UUID uuid, ComponentHistory history) {
                throw new UnsupportedOperationException("Not supported yet.");
            }

            @Override
            public <T extends ComponentHistory> T removeHistory(Class<T> historyClass) {
                throw new UnsupportedOperationException("Not supported yet.");
            }

            @Override
            public ComponentHistory removeHistory(UUID uuid) {
                throw new UnsupportedOperationException("Not supported yet.");
            }

        };
    }

    public TestFindPaneViewModel(FindMatchesResetPolicy resetPolicy) {
        super(resetPolicy, createHistoryManager());
    }
}
