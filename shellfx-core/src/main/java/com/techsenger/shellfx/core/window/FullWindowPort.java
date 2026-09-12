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
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;

/**
 * Provides full access to the component's client API.
 *
 * @author Pavel Castornii
 */
public interface FullWindowPort extends ClosableWindowPort, Titled, Closable, Iconed, Blockable {

    /**
     * Sets whether this window is always on top.
     *
     * @param alwaysOnTop {@code true} to keep the window above other windows; {@code false} otherwise
     */
    void setAlwaysOnTop(boolean alwaysOnTop);

    @Override
    BooleanProperty alwaysOnTopProperty();

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
    void setMaximized(boolean value);

    /**
     * Sets whether the window can be maximized by the user.
     *
     * @param maximizable {@code true} to allow maximizing, {@code false} to prevent it
     */
    void setMaximizable(boolean maximizable);

    @Override
    BooleanProperty maximizableProperty();

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
    void setMinimized(boolean minimized);

    /**
     * Sets whether the window can be minimized by the user.
     *
     * @param minimizable {@code true} to allow minimizing, {@code false} to prevent it
     */
    void setMinimizable(boolean minimizable);

    @Override
    BooleanProperty minimizableProperty();

    /**
     * Sets the width of the window. Using this method is optional because, by default, the window width is
     * based on the preferred width of its content.
     *
     * <p>An explicitly set width persists across sessions; an auto-computed width does not.
     *
     * <p>This is a request, not a guarantee — the platform may adjust or ignore it; observe
     * {@link #widthProperty()} for the value actually applied.
     *
     * @param value the width in pixels
     */
    void setWidth(double value);

    /**
     * Sets the height of the window. Using this method is optional because, by default, the window height is
     * based on the preferred height of its content.
     *
     * <p>An explicitly set height persists across sessions; an auto-computed height does not.
     *
     * <p>This is a request, not a guarantee — the platform may adjust or ignore it; observe
     * {@link #heightProperty()} for the value actually applied.
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

    @Override
    DoubleProperty minWidthProperty();

    /**
     * Sets the minimum height of the window.
     *
     * @param value the minimum height in pixels
     */
    void setMinHeight(double value);

    @Override
    DoubleProperty minHeightProperty();

    /**
     * Sets the maximum width of the window.
     *
     * @param value the maximum width in pixels
     */
    void setMaxWidth(double value);

    @Override
    DoubleProperty maxWidthProperty();

    /**
     * Sets the maximum height of the window.
     *
     * @param value the maximum height in pixels
     */
    void setMaxHeight(double value);

    DoubleProperty maxHeightProperty();

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

    @Override
    BooleanProperty outOfBoundsAllowedProperty();

    /**
     * Sets whether the window can be resized by the user.
     *
     * @param value {@code true} to make the window resizable, {@code false} to disable resizing
     */
    void setResizable(boolean value);

    @Override
    BooleanProperty resizableProperty();

    /**
     * Sets the x-coordinate of the window. This is a request, not a guarantee — the platform may adjust or
     * ignore it; observe {@link #xProperty()} for the value actually applied.
     *
     * @param x the x-coordinate of the window
     */
    void setX(double x);

    /**
     * Sets the y-coordinate of the window. This is a request, not a guarantee — the platform may adjust or
     * ignore it; observe {@link #yProperty()} for the value actually applied.
     *
     * @param y the y-coordinate of the window
     */
    void setY(double y);
}
