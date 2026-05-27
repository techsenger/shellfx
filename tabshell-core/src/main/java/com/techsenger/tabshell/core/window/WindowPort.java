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

import com.techsenger.patternfx.mvp.ParentPort;
import com.techsenger.tabshell.core.CloseAwarePort;
import com.techsenger.tabshell.core.traits.Blockable;
import com.techsenger.tabshell.core.traits.Closable;
import com.techsenger.tabshell.core.traits.Iconed;
import com.techsenger.tabshell.core.traits.Titled;
import com.techsenger.tabshell.material.icon.Icon;

/**
 *
 * @author Pavel Castornii
 */
public interface WindowPort extends ParentPort, WindowShared, CloseAwarePort, Titled, Closable, Iconed, Blockable {

    /**
     * Returns the title of the window.
     *
     * @return the title, or {@code null} if not set
     */
    String getTitle();

    /**
     * Returns the icon of the window.
     *
     * @return the icon, or {@code null} if not set
     */
    Icon<?> getIcon();

    /**
     * Returns whether the window is currently maximized.
     *
     * @return {@code true} if the window is maximized, {@code false} otherwise
     */
    boolean isMaximized();

    /**
     * Returns whether the window can be maximized.
     *
     * @return {@code true} if the window is maximizable, {@code false} otherwise
     */
    boolean isMaximizable();

    /**
     * Returns whether the window is currently minimized.
     *
     * @return {@code true} if the window is minimized, {@code false} otherwise
     */
    boolean isMinimized();

    /**
     * Returns whether the window can be minimized.
     *
     * @return {@code true} if the window is minimizable, {@code false} otherwise
     */
    boolean isMinimizable();

    /**
     * Returns whether the window can be closed by the user.
     *
     * @return {@code true} if the window is closable, {@code false} otherwise
     */
    boolean isClosable();

    /**
     * Returns the width of the window.
     *
     * @return the width in pixels
     */
    double getWidth();

    /**
     * Returns the height of the window.
     *
     * @return the height in pixels
     */
    double getHeight();

    /**
     * Returns whether the window is currently blocked, preventing user interaction.
     *
     * @return {@code true} if the window is blocked, {@code false} otherwise
     */
    boolean isBlocked();
}
