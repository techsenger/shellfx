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

package com.techsenger.shellfx.core.window;

import com.techsenger.shellfx.core.traits.Blockable;
import com.techsenger.shellfx.core.traits.Closable;
import com.techsenger.shellfx.core.traits.Iconed;
import com.techsenger.shellfx.core.traits.Titled;
import com.techsenger.shellfx.material.RequestSetter;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ReadOnlyBooleanProperty;

/**
 * Provides full access to the client API of a window with a title bar: it adds the title, the icon and the buttons
 * for closing, minimizing and maximizing, and lets the user resize the window.
 *
 * @author Pavel Castornii
 */
public interface FrameWindowPort extends WindowPort, Titled, Closable, Iconed, Blockable {

    /**
     * Returns whether this window is modal: it blocks the window that owns it while it is visible. Only a window with
     * a title bar can be modal, as a top-level window is made modal by its JavaFX stage.
     *
     * @return {@code true} if this window is modal; {@code false} otherwise
     */
    boolean isModal();

    /**
     * Returns whether the window is currently maximized.
     *
     * <p>Important: the {@code maximized} and {@code minimized} states are orthogonal and may be combined freely for
     * {@link WindowType#TOP_LEVEL} windows. For {@link WindowType#NESTED} windows, however, these states are mutually
     * exclusive - setting one to {@code true} resets the other to {@code false}.
     *
     * @return {@code true} if the window is maximized, {@code false} otherwise
     */
    boolean isMaximized();

    /**
     * Sets whether the window is maximized.
     *
     * <p>Important: the {@code maximized} and {@code minimized} states are orthogonal and may be combined freely for
     * {@link WindowType#TOP_LEVEL} windows. For {@link WindowType#NESTED} windows, however, these states are mutually
     * exclusive - setting one to {@code true} resets the other to {@code false}.
     *
     * <p>This is a request, not a guarantee — the platform may adjust or ignore it; observe
     * {@link #maximizedProperty()} for the value actually applied.
     *
     * @param value {@code true} to maximize the window, {@code false} to restore it
     */
    @RequestSetter
    void setMaximized(boolean value);

    ReadOnlyBooleanProperty maximizedProperty();

    /**
     * Returns whether the window can be maximized.
     *
     * @return {@code true} if the window is maximizable, {@code false} otherwise
     */
    boolean isMaximizable();

    /**
     * Sets whether the window can be maximized by the user.
     *
     * @param maximizable {@code true} to allow maximizing, {@code false} to prevent it
     */
    void setMaximizable(boolean maximizable);

    BooleanProperty maximizableProperty();

    /**
     * Returns whether the window is currently minimized.
     *
     * <p>Important: the {@code maximized} and {@code minimized} states are orthogonal and may be combined freely for
     * {@link WindowType#TOP_LEVEL} windows. For {@link WindowType#NESTED} windows, however, these states are mutually
     * exclusive - setting one to {@code true} resets the other to {@code false}.
     *
     * @return {@code true} if the window is minimized, {@code false} otherwise
     */
    boolean isMinimized();

    /**
     * Sets whether the window is minimized.
     *
     * <p>Important: the {@code maximized} and {@code minimized} states are orthogonal and may be combined freely for
     * {@link WindowType#TOP_LEVEL} windows. For {@link WindowType#NESTED} windows, however, these states are mutually
     * exclusive - setting one to {@code true} resets the other to {@code false}.
     *
     * <p>This is a request, not a guarantee — the platform may adjust or ignore it; observe
     * {@link #minimizedProperty()} for the value actually applied.
     *
     * @param minimized {@code true} to minimize the window, {@code false} to restore it
     */
    @RequestSetter
    void setMinimized(boolean minimized);

    ReadOnlyBooleanProperty minimizedProperty();

    /**
     * Returns whether the window can be minimized.
     *
     * @return {@code true} if the window is minimizable, {@code false} otherwise
     */
    boolean isMinimizable();

    /**
     * Sets whether the window can be minimized by the user.
     *
     * @param minimizable {@code true} to allow minimizing, {@code false} to prevent it
     */
    void setMinimizable(boolean minimizable);

    BooleanProperty minimizableProperty();

    /**
     * Returns whether the window can be resized by the user.
     *
     * @return {@code true} if the window is resizable, {@code false} otherwise
     */
    boolean isResizable();

    /**
     * Sets whether the window can be resized by the user.
     *
     * @param value {@code true} to make the window resizable, {@code false} to disable resizing
     */
    void setResizable(boolean value);

    BooleanProperty resizableProperty();

    /**
     * Returns whether moving the dialog outside the bounds of its parent container is allowed.
     *
     * <p>This method is intended for {@link WindowType#NESTED} windows only.
     *
     * @return {@code true} if the dialog may be moved beyond the parent bounds,
     *         {@code false} if movement is restricted to the parent area
     */
    boolean isOutOfBoundsAllowed();

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

    BooleanProperty outOfBoundsAllowedProperty();
}
