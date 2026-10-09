/*
 * Copyright 2026 Pavel Castornii.
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

package com.techsenger.shellfx.dialogs.text;

import com.techsenger.shellfx.core.dialog.DialogParams;
import com.techsenger.shellfx.core.settings.AppearanceSettings;
import com.techsenger.shellfx.core.window.WindowType;
import java.util.Objects;

/**
 *
 * @author Pavel Castornii
 */
public class TextDialogParams extends DialogParams {

    public TextDialogParams(TextDialogConfig config, WindowType type, AppearanceSettings settings) {
        super(config, type, settings);
    }

    @Override
    public TextDialogConfig getConfig() {
        return (TextDialogConfig) super.getConfig();
    }

    @Override
    public void validate() {
        super.validate();
        Objects.requireNonNull(getConfig());
    }
}
