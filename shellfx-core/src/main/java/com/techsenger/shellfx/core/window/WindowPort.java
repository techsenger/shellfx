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

import com.techsenger.annotations.Nullable;
import com.techsenger.patternfx.core.ChildPort;
import com.techsenger.shellfx.core.close.CloseAwarePort;
import com.techsenger.shellfx.core.traits.Blockable;
import com.techsenger.shellfx.core.traits.Closable;
import com.techsenger.shellfx.core.traits.Iconed;
import com.techsenger.shellfx.core.traits.Titled;
import com.techsenger.shellfx.material.RequestSetter;
import com.techsenger.shellfx.material.style.Density;
import com.techsenger.shellfx.material.theme.Theme;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.scene.text.Font;

/**
 * Provides full access to the component's client API.
 *
 * @author Pavel Castornii
 */
public interface WindowPort extends ChildPort, CloseAwarePort, Titled, Closable, Iconed, Blockable {

    interface ComposerAccess extends ChildPort.ComposerAccess {

        @Override
        @Nullable WindowContainerPort getParentPort();
    }

    @Override
    ComposerAccess getComposerAccess();

    /**
     * Returns the type of this window.
     *
     * @return the window type
     */
    WindowType getWindowType();

    /**
     * Returns whether this window is modal.
     *
     * @return {@code true} if this window is modal; {@code false} otherwise
     */
    boolean isModal();

    /**
     * Returns whether this window is always on top.
     *
     * @return {@code true} if this window is always on top; {@code false} otherwise
     */
    boolean isAlwaysOnTop();

    /**
     * Sets whether this window is always on top.
     *
     * @param alwaysOnTop {@code true} to keep the window above other windows; {@code false} otherwise
     */
    void setAlwaysOnTop(boolean alwaysOnTop);

    BooleanProperty alwaysOnTopProperty();

    /**
     * Returns whether this window is currently active. For {@link WindowType#TOP_LEVEL} windows, this indicates that
     * the window has OS focus. For {@link WindowType#NESTED} windows, this indicates that the window is the most
     * recently selected window in the window manager.
     */
    boolean isActive();

    ReadOnlyBooleanProperty activeProperty();

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
     * Returns the width of the window.
     *
     * @return the width in pixels
     */
    double getWidth();

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
    @RequestSetter
    void setWidth(double value);

    ReadOnlyDoubleProperty widthProperty();

    /**
     * Returns the height of the window.
     *
     * @return the height in pixels
     */
    double getHeight();

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
    @RequestSetter
    void setHeight(double value);

    ReadOnlyDoubleProperty heightProperty();

    /**
     * Returns the minimum width of the window.
     *
     * @return the minimum width in pixels
     */
    double getMinWidth();

    /**
     * Sets the minimum width of the window.
     *
     * @param value the minimum width in pixels
     */
    void setMinWidth(double value);

    DoubleProperty minWidthProperty();

    /**
     * Returns the minimum height of the window.
     *
     * @return the minimum height in pixels
     */
    double getMinHeight();

    /**
     * Sets the minimum height of the window.
     *
     * @param value the minimum height in pixels
     */
    void setMinHeight(double value);

    DoubleProperty minHeightProperty();

    /**
     * Returns the maximum width of the window.
     *
     * @return the maximum width in pixels
     */
    double getMaxWidth();

    /**
     * Sets the maximum width of the window.
     *
     * @param value the maximum width in pixels
     */
    void setMaxWidth(double value);

    DoubleProperty maxWidthProperty();

    /**
     * Returns the maximum height of the window.
     *
     * @return the maximum height in pixels
     */
    double getMaxHeight();

    /**
     * Sets the maximum height of the window.
     *
     * @param value the maximum height in pixels
     */
    void setMaxHeight(double value);

    DoubleProperty maxHeightProperty();

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
     * Returns the x-coordinate of the window.
     *
     * @return the x-coordinate of the window
     */
    double getX();

    /**
     * Sets the x-coordinate of the window. This is a request, not a guarantee — the platform may adjust or
     * ignore it; observe {@link #xProperty()} for the value actually applied.
     *
     * @param x the x-coordinate of the window
     */
    @RequestSetter
    void setX(double x);

    ReadOnlyDoubleProperty xProperty();

    /**
     * Returns the y-coordinate of the window.
     *
     * @return the y-coordinate of the window
     */
    double getY();

    /**
     * Sets the y-coordinate of the window. This is a request, not a guarantee — the platform may adjust or
     * ignore it; observe {@link #yProperty()} for the value actually applied.
     *
     * @param y the y-coordinate of the window
     */
    @RequestSetter
    void setY(double y);

    ReadOnlyDoubleProperty yProperty();

    /**
     * Returns the density applied to the window.
     *
     * <p>This state is intended for {@link WindowType#TOP_LEVEL} windows only.
     *
     * @return the density, or {@code null} if not set
     */
    @Nullable Density getDensity();

    ReadOnlyObjectProperty<@Nullable Density> densityProperty();

    /**
     * Returns the theme applied to the window.
     *
     * <p>This state is intended for {@link WindowType#TOP_LEVEL} windows only.
     *
     * @return the theme
     */
    Theme getTheme();

    ReadOnlyObjectProperty<Theme> themeProperty();

    /**
     * Returns the regular font applied to the window.
     *
     * <p>This state is intended for {@link WindowType#TOP_LEVEL} windows only.
     *
     * @return the regular font
     */
    Font getRegularFont();

    ReadOnlyObjectProperty<Font> regularFontProperty();

    /**
     * Returns the monospace font applied to the window.
     *
     * <p>This state is intended for {@link WindowType#TOP_LEVEL} windows only.
     *
     * @return the monospace font
     */
    Font getMonospaceFont();

    ReadOnlyObjectProperty<Font> monospaceFontProperty();
}
