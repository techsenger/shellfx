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
import com.techsenger.shellfx.core.registries.ActionRegistry;
import com.techsenger.shellfx.core.registries.ControlRegistry;
import com.techsenger.shellfx.core.registries.SlotRegistry;
import com.techsenger.shellfx.core.registries.StylesheetRegistry;
import com.techsenger.shellfx.core.settings.ShellSettings;
import com.techsenger.shellfx.core.registries.BundleRegistry;
import javafx.application.HostServices;

/**
 * The context that creates the registries of the shell itself. A subclass can replace a registry by overriding its
 * {@code create*Registry} method; the method is called while the context is being constructed, so it must not use
 * the state of the subclass.
 *
 * @author Pavel Castornii
 */
public class DefaultShellContext implements ShellViewModelContext, ShellViewContext {

    private final ShellSettings settings;

    private final ConfigManager configManager;

    private final HostServices hostServices;

    private final SlotRegistry slotRegistry = createSlotRegistry();

    private final ControlRegistry controlRegistry = createControlRegistry();

    private final StylesheetRegistry stylesheetRegistry = createStylesheetRegistry();

    private final ActionRegistry actionRegistry = createActionRegistry();

    private final BundleRegistry bundleRegistry = createBundleRegistry();

    public DefaultShellContext(ShellSettings settings, ConfigManager configManager, HostServices hostServices) {
        this.settings = settings;
        this.configManager = configManager;
        this.hostServices = hostServices;
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

    @Override
    public StylesheetRegistry getStylesheetRegistry() {
        return stylesheetRegistry;
    }

    @Override
    public ActionRegistry getActionRegistry() {
        return actionRegistry;
    }

    @Override
    public BundleRegistry getBundleRegistry() {
        return bundleRegistry;
    }

    protected SlotRegistry createSlotRegistry() {
        return new SlotRegistry();
    }

    protected ControlRegistry createControlRegistry() {
        return new ControlRegistry();
    }

    protected StylesheetRegistry createStylesheetRegistry() {
        return new StylesheetRegistry();
    }

    protected ActionRegistry createActionRegistry() {
        return new ActionRegistry();
    }

    protected BundleRegistry createBundleRegistry() {
        return new BundleRegistry();
    }
}
