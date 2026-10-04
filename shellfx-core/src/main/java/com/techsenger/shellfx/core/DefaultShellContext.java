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

package com.techsenger.shellfx.core;

import com.techsenger.shellfx.core.config.ConfigManager;
import com.techsenger.shellfx.core.registry.ControlRegistry;
import com.techsenger.shellfx.core.registry.SlotRegistry;
import javafx.application.HostServices;
import com.techsenger.shellfx.core.settings.ShellSettings;

/**
 *
 * @author Pavel Castornii
 */
public class DefaultShellContext implements ShellViewModelContext, ShellViewContext {

    private final ShellSettings settings;

    private final ConfigManager configManager;

    private final HostServices hostServices;

    private final SlotRegistry slotRegistry;

    private final ControlRegistry controlRegistry;

    public DefaultShellContext(ShellSettings settings, ConfigManager configManager, HostServices hostServices,
            SlotRegistry slotRegistry, ControlRegistry controlRegistry) {
        this.settings = settings;
        this.configManager = configManager;
        this.hostServices = hostServices;
        this.slotRegistry = slotRegistry;
        this.controlRegistry = controlRegistry;
    }

    @Override
    public ShellSettings getSettings() {
        return settings;
    }

    @Override
    public ConfigManager getConfigManager() {
        return configManager;
    }

    @Override
    public HostServices getHostServices() {
        return hostServices;
    }

    @Override
    public SlotRegistry getSlotRegistry() {
        return slotRegistry;
    }

    @Override
    public ControlRegistry getControlRegistry() {
        return controlRegistry;
    }
}
