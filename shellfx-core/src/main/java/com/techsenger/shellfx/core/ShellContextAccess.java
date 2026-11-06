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
import com.techsenger.shellfx.core.registries.BundleRegistry;
import javafx.application.HostServices;
import com.techsenger.shellfx.core.settings.ShellSettings;

/**
 * The part of the shell context that is available to view models: the settings, the config manager, the host services
 * and the bundles. The registries that serve the view side are in {@link ShellContext}.
 *
 * @author Pavel Castornii
 */
public interface ShellContextAccess {

    /**
     * Returns the settings of the shell.
     *
     * @return
     */
    ShellSettings getSettings();

    /**
     * Returns the config manager.
     *
     * @return
     */
    ConfigManager getConfigManager();

    /**
     * Returns application host services.
     *
     * @return
     */
    HostServices getHostServices();

    /**
     * Returns the registry of the bundles of texts.
     *
     * @return the registry, never {@code null}
     */
    BundleRegistry getBundleRegistry();
}
