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
import com.techsenger.shellfx.material.icon.Icon;
import com.techsenger.shellfx.material.style.Density;
import com.techsenger.shellfx.material.theme.Theme;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyStringProperty;
import javafx.scene.text.Font;

/**
 * Provides minimal, read-only access to the component's client API.
 *
 * @author Pavel Castornii
 */
public interface WindowPort extends ChildPort {

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

    String getTitle();

    ReadOnlyStringProperty titleProperty();

    boolean isClosable();

    ReadOnlyBooleanProperty closableProperty();

    Icon<?> getIcon();

    ReadOnlyObjectProperty<Icon<?>> iconProperty();

    boolean isBlocked();

    ReadOnlyBooleanProperty blockedProperty();

    /**
     * Returns whether this window is always on top.
     *
     * @return {@code true} if this window is always on top; {@code false} otherwise
     */
    boolean isAlwaysOnTop();

    ReadOnlyBooleanProperty alwaysOnTopProperty();

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

    ReadOnlyBooleanProperty maximizedProperty();

    /**
     * Returns whether the window can be maximized.
     *
     * @return {@code true} if the window is maximizable, {@code false} otherwise
     */
    boolean isMaximizable();

    ReadOnlyBooleanProperty maximizableProperty();

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

    ReadOnlyBooleanProperty minimizedProperty();

    /**
     * Returns whether the window can be minimized.
     *
     * @return {@code true} if the window is minimizable, {@code false} otherwise
     */
    boolean isMinimizable();

    ReadOnlyBooleanProperty minimizableProperty();

    /**
     * Returns the width of the window.
     *
     * @return the width in pixels
     */
    double getWidth();

    ReadOnlyDoubleProperty widthProperty();

    /**
     * Returns the height of the window.
     *
     * @return the height in pixels
     */
    double getHeight();

    ReadOnlyDoubleProperty heightProperty();

    /**
     * Returns the minimum width of the window.
     *
     * @return the minimum width in pixels
     */
    double getMinWidth();

    ReadOnlyDoubleProperty minWidthProperty();

    /**
     * Returns the minimum height of the window.
     *
     * @return the minimum height in pixels
     */
    double getMinHeight();

    ReadOnlyDoubleProperty minHeightProperty();

    /**
     * Returns the maximum width of the window.
     *
     * @return the maximum width in pixels
     */
    double getMaxWidth();

    ReadOnlyDoubleProperty maxWidthProperty();

    /**
     * Returns the maximum height of the window.
     *
     * @return the maximum height in pixels
     */
    double getMaxHeight();

    ReadOnlyDoubleProperty maxHeightProperty();

    /**
     * Returns whether moving the dialog outside the bounds of its parent container is allowed.
     *
     * <p>This method is intended for {@link WindowType#NESTED} windows only.
     *
     * @return {@code true} if the dialog may be moved beyond the parent bounds,
     *         {@code false} if movement is restricted to the parent area
     */
    boolean isOutOfBoundsAllowed();

    ReadOnlyBooleanProperty outOfBoundsAllowedProperty();

    /**
     * Returns whether the window can be resized by the user.
     *
     * @return {@code true} if the window is resizable, {@code false} otherwise
     */
    boolean isResizable();

    ReadOnlyBooleanProperty resizableProperty();

    /**
     * Returns the x-coordinate of the window.
     *
     * @return the x-coordinate of the window
     */
    double getX();

    ReadOnlyDoubleProperty xProperty();

    /**
     * Returns the y-coordinate of the window.
     *
     * @return the y-coordinate of the window
     */
    double getY();

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
