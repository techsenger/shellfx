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

import com.techsenger.tabshell.material.icon.Icon;

/**
 *
 * @author Pavel Castornii
 */
public interface WindowShared {

    /**
     * Sets the title of the window.
     *
     * @param title the title to set, or {@code null} to clear it
     */
    void setTitle(String title);

    /**
     * Sets the icon of the window.
     *
     * @param icon the icon to set, or {@code null} to clear it
     */
    void setIcon(Icon<?> icon);

    /**
     * Sets the width of the window.
     *
     * @param value the width in pixels
     */
    void setWidth(double value);

    /**
     * Sets the height of the window.
     *
     * @param value the height in pixels
     */
    void setHeight(double value);

    /**
     * Sets whether the window is maximized.
     *
     * @param value {@code true} to maximize the window, {@code false} to restore it
     */
    void setMaximized(boolean value);

    /**
     * Sets whether the window can be maximized by the user.
     *
     * @param maximizable {@code true} to allow maximizing, {@code false} to prevent it
     */
    void setMaximizable(boolean maximizable);

    /**
     * Sets whether the window is minimized.
     *
     * @param minimized {@code true} to minimize the window, {@code false} to restore it
     */
    void setMinimized(boolean minimized);

    /**
     * Sets whether the window can be minimized by the user.
     *
     * @param minimizable {@code true} to allow minimizing, {@code false} to prevent it
     */
    void setMinimizable(boolean minimizable);

    /**
     * Sets whether the window can be closed by the user.
     *
     * @param closable {@code true} to allow closing, {@code false} to prevent it
     */
    void setClosable(boolean closable);

    /**
     * Sets whether the window is blocked, preventing user interaction.
     *
     * @param blocked {@code true} to block the window, {@code false} to unblock it
     */
    void setBlocked(boolean blocked);
}
