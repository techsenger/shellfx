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
     * Sets whether this window is always on top.
     *
     * @param alwaysOnTop {@code true} to keep the window above other windows; {@code false} otherwise
     */
    void setAlwaysOnTop(boolean alwaysOnTop);

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
     * Sets the width of the window. Using this method is optional because, by default, the window width is
     * based on the preferred width of its content.
     *
     * @param value the width in pixels
     */
    void setWidth(double value);

    /**
     * Sets the height of the window. Using this method is optional because, by default, the window height is
     * based on the preferred height of its content.
     *
     * @param value the height in pixels
     */
    void setHeight(double value);

    /**
     * Sets the minimum width of the window.
     *
     * @param value the minimum width in pixels
     */
    void setMinWidth(double value);

    /**
     * Sets the minimum height of the window.
     *
     * @param value the minimum height in pixels
     */
    void setMinHeight(double value);

    /**
     * Sets the maximum width of the window.
     *
     * @param value the maximum width in pixels
     */
    void setMaxWidth(double value);

    /**
     * Sets the maximum height of the window.
     *
     * @param value the maximum height in pixels
     */
    void setMaxHeight(double value);

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

    /**
     * Enables or disables the ability to move the dialog outside the bounds of its parent container.
     *
     * <p>This method is intended for {@link WindowType#NESTED} windows only.
     *
     * <p>
     * When enabled, only a minimum top constraint may be applied.
     * When disabled, dialog movement is fully constrained to the parent bounds.
     *
     * @param outOfBoundsAllowed {@code true} to allow moving outside parent bounds,
     *                           {@code false} to restrict movement to the parent area
     */
    void setOutOfBoundsAllowed(boolean outOfBoundsAllowed);

    /**
     * Sets whether the window can be resized by the user.
     *
     * @param value {@code true} to make the window resizable, {@code false} to disable resizing
     */
    void setResizable(boolean value);
}
