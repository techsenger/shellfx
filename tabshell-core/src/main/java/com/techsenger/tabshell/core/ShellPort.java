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

package com.techsenger.tabshell.core;

import com.techsenger.patternfx.mvp.ParentPort;
import com.techsenger.tabshell.core.dialog.DialogContainerPort;
import com.techsenger.tabshell.core.history.HistoryManager;
import com.techsenger.tabshell.core.settings.Settings;
import com.techsenger.tabshell.material.icon.Icon;

/**
 *
 * @author Pavel Castornii
 */
public interface ShellPort extends ParentPort, DialogContainerPort, CloseablePort, MenuAwarePort {

    /**
     * Returns the history manager.
     *
     * @return
     */
    HistoryManager getHistoryManager();

    /**
     * Returns the settings of the shell.
     *
     * @return
     */
    Settings getSettings();

    /**
     * Returns the settings of the shell as an instance of the specified class using type casting.
     *
     * @return
     */
    <T extends Settings> T getSettings(Class<T> settingsClass);

    /**
     * Returns shell current width.
     *
     * @return
     */
    double getWidth();

    /**
     * Returns shell current height.
     *
     * @return
     */
    double getHeight();

    /**
     * Returns true if the shell stage is maximized, otherwise false.
     *
     * @return
     */
    boolean isMaximized();

    String getTitle();

    Icon<?> getIcon();

}
