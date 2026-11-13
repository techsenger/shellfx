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

package com.techsenger.shellfx.core.dialog;

import com.techsenger.annotations.Nullable;
import com.techsenger.shellfx.core.settings.ShellSettings;
import com.techsenger.shellfx.core.window.FrameWindowParams;
import com.techsenger.shellfx.core.window.WindowType;

/**
 *
 * @author Pavel Castornii
 */
public class DialogParams extends FrameWindowParams {

    /**
     * Creates the parameters of a modal dialog.
     *
     * @param config the config of the dialog, or {@code null} for none
     * @param type the type of the dialog window
     * @param settings the settings of the shell
     */
    public DialogParams(@Nullable DialogConfig config, WindowType type, ShellSettings settings) {
        this(config, type, settings, true);
    }

    /**
     * Creates the parameters of a dialog that is modal or not, as given.
     *
     * @param config the config of the dialog, or {@code null} for none
     * @param type the type of the dialog window
     * @param settings the settings of the shell
     * @param modal whether the dialog blocks the window that owns it while it is visible
     */
    public DialogParams(@Nullable DialogConfig config, WindowType type, ShellSettings settings, boolean modal) {
        super(config, type, settings, modal);
    }

    @Override
    public @Nullable DialogConfig getConfig() {
        return (DialogConfig) super.getConfig();
    }
}
