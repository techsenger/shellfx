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

package com.techsenger.shellfx.demo.dialogs;

import com.techsenger.annotations.Nullable;
import com.techsenger.shellfx.core.config.ConfigManager;
import com.techsenger.shellfx.core.dialog.DialogConfig;
import com.techsenger.shellfx.core.dialog.DialogParams;
import com.techsenger.shellfx.core.settings.AppearanceSettings;
import com.techsenger.shellfx.core.window.WindowType;
import java.util.Objects;

/**
 *
 * @author Pavel Castornii
 */
public class DialogsDialogParams extends DialogParams {

    private final AppearanceSettings settings;

    private final ConfigManager manager;

    public DialogsDialogParams(@Nullable DialogConfig config, AppearanceSettings settings, ConfigManager manager) {
        super(config, WindowType.NESTED, settings);
        this.settings = settings;
        this.manager = manager;
    }

    public AppearanceSettings getSettings() {
        return settings;
    }

    public ConfigManager getManager() {
        return manager;
    }

    @Override
    public void validate() {
        super.validate();
        Objects.requireNonNull(settings);
        Objects.requireNonNull(manager);
    }
}
