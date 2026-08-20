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
import com.techsenger.patternfx.mvp.ChildPort;
import com.techsenger.shellfx.core.CloseAwarePort;
import com.techsenger.shellfx.core.traits.Blockable;
import com.techsenger.shellfx.core.traits.Closable;
import com.techsenger.shellfx.core.traits.Iconed;
import com.techsenger.shellfx.core.traits.Titled;
import com.techsenger.shellfx.material.icon.Icon;
import com.techsenger.shellfx.material.style.Density;
import com.techsenger.shellfx.material.theme.Theme;
import javafx.scene.text.Font;

/**
 *
 * @author Pavel Castornii
 */
public interface WindowPort extends ChildPort, CloseAwarePort, Titled, Closable, Iconed, Blockable {

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
     * Returns whether this window is currently active. For {@link WindowType#TOP_LEVEL} windows, this indicates that
     * the window has OS focus. For {@link WindowType#NESTED} windows, this indicates that the window is the most
     * recently selected window in the window manager.
     */
    boolean isActive();

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
     * <p>Important: the {@code maximized} and {@code minimized} states are orthogonal and may be combined freely for
     * {@link WindowType#TOP_LEVEL} windows. For {@link WindowType#NESTED} windows, however, these states are mutually
     * exclusive - setting one to {@code true} resets the other to {@code false}.
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
     * <p>Important: the {@code maximized} and {@code minimized} states are orthogonal and may be combined freely for
     * {@link WindowType#TOP_LEVEL} windows. For {@link WindowType#NESTED} windows, however, these states are mutually
     * exclusive - setting one to {@code true} resets the other to {@code false}.
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
     * Returns the minimum width of the window.
     *
     * @return the minimum width in pixels
     */
    double getMinWidth();

    /**
     * Returns the minimum height of the window.
     *
     * @return the minimum height in pixels
     */
    double getMinHeight();

    /**
     * Returns the maximum width of the window.
     *
     * @return the maximum width in pixels
     */
    double getMaxWidth();

    /**
     * Returns the maximum height of the window.
     *
     * @return the maximum height in pixels
     */
    double getMaxHeight();

    /**
     * Returns whether the window is currently blocked, preventing user interaction.
     *
     * @return {@code true} if the window is blocked, {@code false} otherwise
     */
    boolean isBlocked();

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
     * Returns whether the window can be resized by the user.
     *
     * @return {@code true} if the window is resizable, {@code false} otherwise
     */
    boolean isResizable();

    /**
     * Returns the x-coordinate of the window.
     *
     * @return the x-coordinate of the window
     */
    double getX();

    /**
     * Returns the y-coordinate of the window.
     *
     * @return the y-coordinate of the window
     */
    double getY();

    /**
     * Returns the density applied to the window.
     *
     * <p>This state is intended for {@link WindowType#TOP_LEVEL} windows only.
     *
     * @return the density, or {@code null} if not set
     */
    @Nullable Density getDensity();

    /**
     * Returns the theme applied to the window.
     *
     * <p>This state is intended for {@link WindowType#TOP_LEVEL} windows only.
     *
     * @return the theme
     */
    Theme getTheme();

    /**
     * Returns the regular font applied to the window.
     *
     * <p>This state is intended for {@link WindowType#TOP_LEVEL} windows only.
     *
     * @return the regular font
     */
    Font getRegularFont();

    /**
     * Returns the monospace font applied to the window.
     *
     * <p>This state is intended for {@link WindowType#TOP_LEVEL} windows only.
     *
     * @return the monospace font
     */
    Font getMonospaceFont();

    /**
     * Sets whether this window is always on top.
     *
     * @param alwaysOnTop {@code true} to keep the window above other windows; {@code false} otherwise
     */
    void setAlwaysOnTop(boolean alwaysOnTop);

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
     * <p>Important: the {@code maximized} and {@code minimized} states are orthogonal and may be combined freely for
     * {@link WindowType#TOP_LEVEL} windows. For {@link WindowType#NESTED} windows, however, these states are mutually
     * exclusive - setting one to {@code true} resets the other to {@code false}.
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
     * <p>Important: the {@code maximized} and {@code minimized} states are orthogonal and may be combined freely for
     * {@link WindowType#TOP_LEVEL} windows. For {@link WindowType#NESTED} windows, however, these states are mutually
     * exclusive - setting one to {@code true} resets the other to {@code false}.
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

    /**
     * Sets the x-coordinate of the window.
     *
     * @param x the x-coordinate of the window
     */
    void setX(double x);

    /**
     * Sets the y-coordinate of the window.
     *
     * @param y the y-coordinate of the window
     */
    void setY(double y);
}
