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

import com.techsenger.shellfx.core.close.CloseCheckResult;
import com.techsenger.shellfx.core.close.ClosePreparationResult;
import com.techsenger.shellfx.core.config.ConfigManager;
import com.techsenger.shellfx.core.window.AbstractWindowViewModel;
import com.techsenger.shellfx.core.window.WindowComposer;
import com.techsenger.shellfx.devtools.style.DevToolsIcons;
import java.util.function.Consumer;

/**
 *
 * @author Pavel Castornii
 */
public class DevToolsWindowViewModel<C extends WindowComposer> extends AbstractWindowViewModel<C> {

    private final ConfigManager configManager;

    public DevToolsWindowViewModel(DevToolsWindowParams params) {
        super(params);
        this.configManager = params.getConfigManager();
    }

    @Override
    public CloseCheckResult isReadyToClose() {
        return CloseCheckResult.READY;
    }

    @Override
    public void prepareToClose(Consumer<ClosePreparationResult> resultCallback) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }

    @Override
    protected void postInitialize() {
        super.postInitialize();
        setTitle("DevTools");
        setIcon(DevToolsIcons.TOOLS);
        setMaximizable(true);
        setMinimizable(true);
    }

    @Override
    protected DevToolsWindowConfig getConfig() {
        return (DevToolsWindowConfig) super.getConfig();
    }
}
