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
import com.techsenger.shellfx.material.RequestSetter;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyDoubleProperty;

/**
 * Provides full access to the client API of a bare window: a surface with a position, a size, a visibility and a
 * focus, but without a title bar, buttons or border.
 *
 * @author Pavel Castornii
 */
public interface WindowPort extends ChildPort, CloseAwarePort {

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
     * Returns whether the window is visible. A window is created invisible, so its content can be filled in before it
     * is shown. An invisible nested window is a part of the component tree, but takes no part in z-order, focus and
     * modality; an invisible top-level window is not shown on the screen.
     *
     * @return {@code true} if the window is visible, {@code false} otherwise
     */
    boolean isVisible();

    /**
     * Shows or hides the window. For {@link WindowType#TOP_LEVEL} windows this is {@code show()} and {@code hide()} of
     * the stage; a minimized window is still visible. Hiding is not closing: the window stays in the component tree,
     * and {@link #close()} still has to be called to remove it.
     *
     * <p>Showing a window gives it the focus. A top-level window gets it from the platform, so a nested window behaves
     * the same way. {@code requestFocus()} brings the focus back to the window later.
     *
     * <p>The visibility can also be changed by the user or the platform, for example when the owner of a top-level
     * window is closed; observe {@link #visibleProperty()} for the actual value.
     *
     * @param value {@code true} to show the window, {@code false} to hide it
     */
    void setVisible(boolean value);

    BooleanProperty visibleProperty();

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
}
