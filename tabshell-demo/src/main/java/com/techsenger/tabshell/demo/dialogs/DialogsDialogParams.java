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

package com.techsenger.tabshell.demo.dialogs;

import com.techsenger.tabshell.core.dialog.DialogParams;
import com.techsenger.tabshell.core.history.HistoryManager;
import com.techsenger.tabshell.core.settings.AppearanceSettings;
import java.util.Objects;

/**
 *
 * @author Pavel Castornii
 */
public class DialogsDialogParams extends DialogParams {

    private final AppearanceSettings settings;

    private final HistoryManager manager;

    public DialogsDialogParams(AppearanceSettings settings, HistoryManager manager) {
        this.settings = settings;
        this.manager = manager;
    }

    public AppearanceSettings getSettings() {
        return settings;
    }

    public HistoryManager getManager() {
        return manager;
    }

    @Override
    protected void validate() {
        super.validate();
        Objects.requireNonNull(settings);
        Objects.requireNonNull(manager);
    }
}
