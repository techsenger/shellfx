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

package com.techsenger.tabshell.core.window;

import com.techsenger.patternfx.mvp.ComponentParams;
import com.techsenger.tabshell.core.settings.AppearanceSettings;
import java.util.Objects;

/**
 *
 * @author Pavel Castornii
 */
public class WindowParams extends ComponentParams {

    private final WindowType windowType;

    private final boolean modal;

    private final AppearanceSettings settings;

    public WindowParams(WindowType type, boolean modal, AppearanceSettings settings) {
        this.windowType = type;
        this.modal = modal;
        this.settings = settings;
    }

    public WindowType getWindowType() {
        return windowType;
    }

    public boolean isModal() {
        return modal;
    }

    public AppearanceSettings getSettings() {
        return settings;
    }

    @Override
    protected void validate() {
        super.validate();
        if (this.windowType == WindowType.TOP_LEVEL) {
            Objects.requireNonNull(settings);
        }
    }
}
