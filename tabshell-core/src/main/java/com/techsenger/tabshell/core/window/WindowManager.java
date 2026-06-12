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

import com.techsenger.annotations.Unmodifiable;
import com.techsenger.tabshell.core.popup.PopupManager;
import javafx.collections.ObservableList;

/**
 * The manager of the {@link WindowType#NESTED} windows.
 *
 * @author Pavel Castornii
 */
public interface WindowManager extends PopupManager {

    /**
     * Adds a window to the manager.
     *
     * @param windowView the window to add
     */
    void addWindow(WindowFxView<?> windowView);

    /**
     * Removes a window from the manager.
     *
     * @param windowView the window to remove
     */
    void removeWindow(WindowFxView<?> windowView);

    /**
     * Returns an unmodifiable list of windows.
     *
     * @return the list of windows. If no windows are present, an empty list is returned.
     */
    @Unmodifiable ObservableList<WindowFxView<?>> getWindows();

    /**
     * Arranges all managed windows according to the specified arrangement strategy.
     * <p>
     * This operation repositions and resizes windows within the available container area based on the given
     * arrangement mode.
     *
     * @param arrangement the arrangement strategy to apply to all windows
     */
    void arrangeWindows(WindowArrangement arrangement);

    /**
     * Maximizes the specified window to fill the available container area.
     *
     * @param window the window component to maximize
     */
    void maximizeWindow(WindowFxView<?> window);

    /**
     * Minimizes the specified window.
     *
     * @param window the window component to maximize
     */
    void minimizeWindow(WindowFxView<?> window);

    /**
     * Restores the specified window to its previous size and position before it was maximized or minimized.
     *
     * @param window the window component to restore
     */
    void restoreWindow(WindowFxView<?> window);

    /**
     * Updates the state of a window in the manager.
     * <p>
     * This should be called when properties of the window that affect its visual representation or layout have changed
     * (e.g. size, position, visibility).
     *
     * @param windowView the window to update
     */
    void updateWindow(WindowFxView<?> windowView);
}
