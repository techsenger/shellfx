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

package com.techsenger.shellfx.devtools;

import com.techsenger.connectorfx.Connector;
import com.techsenger.shellfx.core.area.AreaParams;
import com.techsenger.shellfx.core.history.HistoryManager;
import com.techsenger.shellfx.core.settings.ShellSettings;
import java.util.Objects;

/**
 *
 * @author Pavel Castornii
 */
public class DevToolsTabDockParams extends AreaParams {

    private final DevToolsHostType hostType;

    private final ShellSettings settings;

    private final HistoryManager historyManager;

    private final Connector connector;

    private final int shellWindowUid;

    public DevToolsTabDockParams(DevToolsHostType hostType, ShellSettings settings, HistoryManager historyManager,
            Connector connector, int shellWindowUid) {
        this.hostType = hostType;
        this.settings = settings;
        this.historyManager = historyManager;
        this.connector = connector;
        this.shellWindowUid = shellWindowUid;
        this.setHistoryProvider(() -> historyManager
                .getOrCreateHistory(DevToolsTabDockHistory.class, DevToolsTabDockHistory::new));
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

    public Connector getConnector() {
        return connector;
    }

    public int getShellWindowUid() {
        return shellWindowUid;
    }

    @Override
    public void validate() {
        super.validate();
        Objects.requireNonNull(hostType);
        Objects.requireNonNull(settings);
        Objects.requireNonNull(historyManager);
        Objects.requireNonNull(connector);
    }
}
