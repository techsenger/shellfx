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

package com.techsenger.tabshell.devtools.component;

import com.techsenger.annotations.Nullable;
import com.techsenger.patternfx.mvp.FxViewUtils;
import com.techsenger.patternfx.mvp.ParentFxView;
import com.techsenger.tabshell.core.ShellFxView;
import javafx.stage.Window;

/**
 *
 * @author Pavel Castornii
 */
public class JfxComponentService implements ComponentService {

    private final ShellFxView<?> shell;

    public JfxComponentService(ShellFxView<?> shell) {
        this.shell = shell;
    }

    @Override
    public ComponentItem getShellComponent() {
        return new JfxComponentItem(shell);
    }

    @Override
    public @Nullable ComponentItem getComponent(int windowUid) {
        for (Window window : Window.getWindows()) {
            if (window.hashCode() == windowUid) {
                return new JfxComponentItem((ParentFxView<?>) FxViewUtils.getView(window.getScene()));
            }
        }
        return null;
    }


}
