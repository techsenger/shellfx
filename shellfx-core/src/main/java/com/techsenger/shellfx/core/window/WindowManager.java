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
import com.techsenger.annotations.Unmodifiable;
import com.techsenger.shellfx.core.dialog.DialogView;
import com.techsenger.shellfx.material.Anchors;
import javafx.collections.ObservableList;

/**
 * The manager of the {@link WindowType#NESTED} windows, popups included.
 *
 * <p>A window passes through two independent phases. First it is added to or removed from the manager, which only
 * makes the window known to the manager. Then it is made visible or invisible through its own {@code visible}
 * property, and only a visible window takes part in z-order, focus and modality. Removing a visible window ends both
 * phases at once, so it does not have to be hidden first.
 *
 * @author Pavel Castornii
 */
public interface WindowManager {

    /**
     * Adds a window to the manager (the first phase). The window is added as it is: while it is invisible, it takes no
     * part in z-order, focus and modality, and it starts to as soon as it becomes visible.
     *
     * @param windowView the window to add
     */
    void addWindow(WindowView<?> windowView);

    /**
     * Removes a window from the manager, whether it is visible or not.
     *
     * @param windowView the window to remove
     */
    void removeWindow(WindowView<?> windowView);

    /**
     * Returns an unmodifiable list of windows.
     *
     * @return the list of windows, invisible ones included. If no windows are present, an empty list is returned.
     */
    @Unmodifiable ObservableList<WindowView<?>> getWindows();

    /**
     * Sets the anchors of a window: the distances from the edges of the container the window keeps while the
     * container is resized. A window without anchors is floating, which is the default, and sits where its
     * {@code x} and {@code y} say. It can be called before or after the window is added to the manager.
     *
     * <p>Any manual positioning of the window (setting {@code x} or {@code y}, aligning, moving or resizing it by the
     * user, arranging) makes it floating again. A maximized or minimized window is placed by the manager, and its
     * anchors are applied again when it is restored.
     *
     * @param window the window
     * @param anchors the anchors, or {@code null} to make the window floating
     */
    void setAnchors(WindowView<?> window, @Nullable Anchors anchors);

    /**
     * Returns the anchors of a window.
     *
     * @param window the window
     * @return the anchors, or {@code null} if the window is floating
     */
    @Nullable Anchors getAnchors(WindowView<?> window);

    /**
     * Returns whether the container that owns this manager is blocked because a modal component is shown.
     *
     * @return true if the container is blocked by a modal component, false otherwise
     */
    boolean isContainerBlocked();

    /**
     * Aligns the given window within the {@code StackPane} according to the given {@link WindowPosition}, adjusted
     * by the given offsets. Applies only to {@link WindowType#NESTED} windows.
     *
     * <p>This is a one-time positioning command, not a persisted constraint: once aligned, the window can be
     * freely moved by the user (e.g. via drag), and its position is not re-aligned afterward.
     *
     * @param window  the window to align
     * @param pos     the reference position within the {@code StackPane}
     * @param xOffset offset added to the computed x coordinate; positive values shift the window
     *                to the right, negative values shift it to the left
     * @param yOffset offset added to the computed y coordinate; positive values shift the window
     *                down, negative values shift it up
     */
    void alignWindow(WindowView<?> window, WindowPosition pos, double xOffset, double yOffset);

    /**
     * Aligns the given window within the {@code StackPane} according to the given {@link WindowPosition}, using the
     * top-level {@code Stage} bounds as the reference container, adjusted by the given offsets. Applies only to
     * {@link WindowType#NESTED} windows.
     *
     * <p>This is best-effort — if the {@code Stage} bounds are not available, the {@code StackPane} is used as a
     * fallback. Primarily useful for dialogs that should appear centered within the application window rather than
     * within the MDI area.
     *
     * <p>This is a one-time positioning command, not a persisted constraint: once aligned, the window can be freely
     * moved by the user (e.g. via drag), and its position is not re-aligned afterward.
     *
     * @param window  the window to align
     * @param pos     the reference position within the {@code Stage}
     * @param xOffset offset added to the computed x coordinate; positive values shift the window to the right,
     *                negative values shift it to the left
     * @param yOffset offset added to the computed y coordinate; positive values shift the window down,
     *                negative values shift it up
     */
    void alignWindowToStage(WindowView<?> window, WindowPosition pos, double xOffset, double yOffset);

    /**
     * Convenience method that adds the given dialog to the component tree and aligns it slightly above the center
     * of the top-level {@code Stage}, which is the conventional position for dialogs. Equivalent to calling
     * {@link #addWindow(WindowFxView)} followed by {@link #alignWindowToStage(WindowFxView, WindowPosition)} with
     * {@link WindowPosition#CENTER} and a small upward offset.
     *
     * <p>If the {@code Stage} bounds are not available, the {@code StackPane} center is used as a fallback.
     *
     * <p>An invisible dialog is aligned once, when it becomes visible for the first time, because only then its
     * content, and therefore its size, is final. Hiding and showing it again keeps its current position.
     *
     * <p>For precise control over dialog positioning, use {@link #addWindow(WindowFxView)} combined with one of
     * the {@link #alignWindow(WindowFxView, WindowPosition, double, double) }
     * or {@link #alignWindowToStage(WindowFxView, WindowPosition, double, double) } methods.
     *
     * @param dialog the dialog to add and align
     */
    void addDialog(DialogView<?> dialog);

    /**
     * Arranges the visible floating windows according to the specified arrangement strategy. Anchored windows are
     * placed by the container and are left as they are.
     * <p>
     * This operation repositions and resizes windows within the available container area based on the given
     * arrangement mode.
     *
     * @param arrangement the arrangement strategy to apply to the floating windows
     */
    void arrangeWindows(WindowArrangement arrangement);

    /**
     * Maximizes the specified window to fill the available container area.
     *
     * @param window the window component to maximize
     */
    void maximizeWindow(WindowView<?> window);

    /**
     * Minimizes the specified window.
     *
     * @param window the window component to maximize
     */
    void minimizeWindow(WindowView<?> window);

    /**
     * Restores the specified window to its previous size and position before it was maximized or minimized.
     *
     * @param window the window component to restore
     */
    void restoreWindow(WindowView<?> window);

    /**
     * Updates the state of a window in the manager.
     * <p>
     * This should be called when properties of the window that affect its visual representation or layout have changed
     * (e.g. size, position, visibility).
     *
     * @param windowView the window to update
     */
    void updateWindow(WindowView<?> windowView);
}
