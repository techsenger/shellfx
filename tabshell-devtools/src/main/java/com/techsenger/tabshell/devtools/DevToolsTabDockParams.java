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

package com.techsenger.tabshell.devtools;

import com.techsenger.patternfx.core.HistoryPolicy;
import com.techsenger.tabshell.core.area.AreaParams;
import com.techsenger.tabshell.core.history.HistoryManager;
import com.techsenger.tabshell.core.settings.ShellSettings;
import java.util.Objects;

/**
 *
 * @author Pavel Castornii
 */
public class DevToolsTabDockParams extends AreaParams {

    private final DevToolsHostType hostType;

    private final ShellSettings settings;

    private final HistoryManager historyManager;

    public DevToolsTabDockParams(DevToolsHostType hostType, ShellSettings settings, HistoryManager historyManager) {
        this.hostType = hostType;
        this.settings = settings;
        this.historyManager = historyManager;
        this.setHistoryProvider(() -> historyManager
                .getOrCreateHistory(DevToolsTabDockHistory.class, DevToolsTabDockHistory::new));
        this.setHistoryPolicy(HistoryPolicy.APPEARANCE);
    }

    public DevToolsHostType getHostType() {
        return hostType;
    }

    public ShellSettings getSettings() {
        return settings;
    }

    public HistoryManager getHistoryManager() {
        return historyManager;
    }

    @Override
    protected void validate() {
        super.validate();
        Objects.requireNonNull(hostType);
        Objects.requireNonNull(settings);
        Objects.requireNonNull(historyManager);
    }
}
