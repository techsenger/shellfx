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

package com.techsenger.tabshell.core;

import com.techsenger.tabshell.core.history.HistoryManager;
import com.techsenger.tabshell.core.settings.Settings;
import javafx.application.HostServices;

/**
 *
 * @author Pavel Castornii
 */
public class DefaultShellContext implements ShellContext {

    private final Settings settings;

    private final HistoryManager historyManager;

    private final HostServices hostServices;

    public DefaultShellContext(Settings settings, HistoryManager historyManager, HostServices hostServices) {
        this.settings = settings;
        this.historyManager = historyManager;
        this.hostServices = hostServices;
    }

    @Override
    public Settings getSettings() {
        return settings;
    }

    @Override
    public HistoryManager getHistoryManager() {
        return historyManager;
    }

    @Override
    public HostServices getHostServices() {
        return hostServices;
    }
}
