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

import com.techsenger.annotations.Nullable;
import com.techsenger.annotations.Unmodifiable;
import com.techsenger.patternfx.mvp.ChildFxView;
import com.techsenger.patternfx.mvp.FxViewUtils;
import com.techsenger.patternfx.mvp.ParentFxView;
import com.techsenger.tabshell.core.popup.AbstractPopupManager;
import com.techsenger.tabshell.core.popup.PopupFxView;
import static com.techsenger.tabshell.core.window.WindowArrangement.CASCADE;
import static com.techsenger.tabshell.core.window.WindowArrangement.TILE_HORIZONTAL;
import static com.techsenger.tabshell.core.window.WindowArrangement.TILE_VERTICAL;
import com.techsenger.tabshell.material.Anchors;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;
import javafx.animation.Interpolator;
import javafx.animation.Transition;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.util.Duration;

/**
 *
 * @author Pavel Castornii
 */
public abstract class AbstractWindowManager extends AbstractPopupManager implements WindowManager {

    private enum SpecialState {
        MAXIMIZED, MINIMIZED
    }

    private record WindowBounds(double x, double y, double width, double height) { }

    private static final class RestoreInfo {

        private SpecialState state;

        private final WindowBounds bounds;

        private RestoreInfo(SpecialState state, WindowBounds bounds) {
            this.state = state;
            this.bounds = bounds;
        }
    }

    private static final class WindowPane extends AnchorPane {

        private WindowPane(Node... nodes) {
            super(nodes);
        }
    }

    /**
     * To align window to the center of the StackPane its necessary to know window sizes. However, these sizes
     * will be available only after layout pulse. So, window aligner class that is used as a pulse listener.
     */
    private static final class WindowAligner implements Runnable {

        private final StackPane stackPane;

        private final WindowFxView<?> windowView;

        WindowAligner(StackPane stackPane, WindowFxView<?> windowView) {
            this.stackPane = stackPane;
            this.windowView = windowView;
        }

        @Override
        public void run() {
            var window = windowView.getNode();
            var x = (stackPane.getWidth() / 2) - (window.getWidth() / 2);
            var y = (stackPane.getHeight() / 2) - (window.getHeight() / 2);
            //move a little bit up
            y -= 50;
            y = Math.max(0, y);
            /* if values are left as a double, the dialog border might appear blurry when the values are decimal */
            window.setLayoutX((int) x);
            window.setLayoutY((int) y);
            stackPane.getScene().removePostLayoutPulseListener(this);
            windowView.requestFocus();
        }
    }

    private enum MinimizeDirection {

        /**
         * Window collapses into the slot: shrink height first (anchored at bottom), then fly to the slot.
         */
        TO_SLOT,

        /**
         * Window expands from the slot: fly from the slot first, then expand height (anchored at bottom).
         */
        FROM_SLOT
    }

    private static final double MINIMIZED_WINDOW_WIDTH = 250;

    private static final Duration ANIMATION_DURATION = Duration.millis(250);

    private static @Nullable WindowPane getWindowPane(WindowFxView<?> window) {
        if (window.getNode() != null) {
            return (WindowPane) window.getNode().getParent();
        } else {
            return null;
        }
    }

    /**
     * Calculates cascading layout positions for windows within a container.
     * Windows are ordered from bottom to top — index 0 is the bottommost window.
     *
     * @param containerWidth  the width of the container StackPane
     * @param containerHeight the height of the container StackPane
     * @param windowCount     the number of windows to arrange
     * @return list of window bounds ordered from bottom (index 0) to top (index n-1)
     */
    private static List<WindowBounds> cascade(double containerWidth, double containerHeight, int windowCount) {
        if (windowCount <= 0) {
            return List.of();
        }

        // Window occupies ~65% of the container
        double windowWidth  = Math.round(containerWidth  * 0.65);
        double windowHeight = Math.round(containerHeight * 0.65);

        // Step between windows; title bar height is a good natural offset (~4.5% of container height)
        double step = Math.max(Math.round(containerHeight * 0.045), 24);

        // If windows would overflow the container, shrink the step to fit
        if (windowWidth + step * (windowCount - 1) > containerWidth
                || windowHeight + step * (windowCount - 1) > containerHeight) {
            step = Math.max((int) Math.min(
                    (containerWidth  - windowWidth)  / Math.max(windowCount - 1, 1),
                    (containerHeight - windowHeight) / Math.max(windowCount - 1, 1)
            ), 1);
        }

        var result = new ArrayList<WindowBounds>(windowCount);
        for (int i = 0; i < windowCount; i++) {
            result.add(new WindowBounds(step * i, step * i, windowWidth, windowHeight));
        }
        return result;
    }

    /**
     * Calculates vertical tile layout — windows are placed side by side in columns.
     * All windows get equal width and full container height. The last window absorbs
     * any remainder from integer division, so it may be slightly larger or smaller
     * than the others. There are no gaps between windows.
     *
     * @param containerWidth  the width of the container StackPane
     * @param containerHeight the height of the container StackPane
     * @param windowCount     the number of windows to arrange
     * @return list of window bounds ordered from left (index 0) to right (index n-1)
     */
    private static List<WindowBounds> tileVertical(double containerWidth, double containerHeight, int windowCount) {
        if (windowCount <= 0) {
            return List.of();
        }

        int totalWidth = (int) containerWidth;
        int winWidth = totalWidth / windowCount;

        var result = new ArrayList<WindowBounds>(windowCount);
        for (int i = 0; i < windowCount; i++) {
            int x = winWidth * i;
            int w = (i == windowCount - 1) ? totalWidth - x : winWidth;
            result.add(new WindowBounds(x, 0, w, containerHeight));
        }
        return result;
    }

    /**
     * Calculates horizontal tile layout — windows are stacked in rows.
     * All windows get full container width and equal height. The last window absorbs
     * any remainder from integer division, so it may be slightly larger or smaller
     * than the others. There are no gaps between windows.
     *
     * @param containerWidth  the width of the container StackPane
     * @param containerHeight the height of the container StackPane
     * @param windowCount     the number of windows to arrange
     * @return list of window bounds ordered from top (index 0) to bottom (index n-1)
     */
    private static List<WindowBounds> tileHorizontal(double containerWidth, double containerHeight, int windowCount) {
        if (windowCount <= 0) {
            return List.of();
        }

        int totalHeight = (int) containerHeight;
        int winHeight = totalHeight / windowCount;

        var result = new ArrayList<WindowBounds>(windowCount);
        for (int i = 0; i < windowCount; i++) {
            int y = winHeight * i;
            int h = (i == windowCount - 1) ? totalHeight - y : winHeight;
            result.add(new WindowBounds(0, y, containerWidth, h));
        }
        return result;
    }

    /**
     * Calculates grid layout positions for windows within a container.
     * The number of columns and rows is derived from the container's aspect ratio —
     * wider containers get more columns, taller containers get more rows.
     * If the last row is incomplete, its windows are stretched to fill the full width.
     * The last window in an incomplete row absorbs any remainder from integer division.
     *
     * @param containerWidth  the width of the container StackPane
     * @param containerHeight the height of the container StackPane
     * @param windowCount     the number of windows to arrange
     * @return list of window bounds ordered left-to-right, top-to-bottom (index 0 is top-left)
     */
    private static List<WindowBounds> tileGrid(double containerWidth, double containerHeight, int windowCount) {
        if (windowCount <= 0) {
            return List.of();
        }
        if (windowCount == 1) {
            return List.of(new WindowBounds(0, 0, containerWidth, containerHeight));
        }

        double aspectRatio = containerWidth / containerHeight;

        // derive columns from aspect ratio, then rows from columns
        int cols = (int) Math.round(Math.sqrt(windowCount * aspectRatio));
        cols = Math.max(1, Math.min(cols, windowCount));
        int rows = (int) Math.ceil((double) windowCount / cols);

        int totalWidth  = (int) containerWidth;
        int totalHeight = (int) containerHeight;
        int winWidth    = totalWidth  / cols;
        int winHeight   = totalHeight / rows;

        int lastRowCount = windowCount % cols == 0 ? cols : windowCount % cols;
        int lastRowWinWidth = totalWidth / lastRowCount;

        var result = new ArrayList<WindowBounds>(windowCount);
        for (int i = 0; i < windowCount; i++) {
            int row = i / cols;
            int col = i % cols;
            boolean isLastRow = (row == rows - 1);

            int x;
            int w;
            if (isLastRow) {
                int posInRow = i - (rows - 1) * cols;
                x = lastRowWinWidth * posInRow;
                w = (posInRow == lastRowCount - 1) ? totalWidth - x : lastRowWinWidth;
            } else {
                x = winWidth * col;
                w = (col == cols - 1) ? totalWidth - x : winWidth;
            }

            int y = winHeight * row;
            int h = (row == rows - 1) ? totalHeight - y : winHeight;

            result.add(new WindowBounds(x, y, w, h));
        }
        return result;
    }

    private static Optional<WindowFxView<?>> findComponentWindow(ParentFxView<?> component, StackPane windowPane) {
        while (component != null) {
            if (component instanceof WindowFxView<?> window) {
                var bgPane = getWindowPane(window);
                if (bgPane != null && bgPane.getParent() == windowPane) {
                    return Optional.of(window);
                }
            }
            component = component instanceof ChildFxView<?> child ? child.getComposer().getParent() : null;
        }
        return Optional.empty();
    }

    private static void animateMaximize(Region windowNode, WindowBounds from, WindowBounds to, Runnable onFinished) {
        windowNode.setLayoutX(from.x());
        windowNode.setLayoutY(from.y());
        windowNode.setMinSize(from.width(), from.height());
        windowNode.setMaxSize(from.width(), from.height());

        Transition t = new Transition() {
            {
                setCycleDuration(ANIMATION_DURATION);
                setInterpolator(Interpolator.EASE_BOTH);
            }
            @Override
            protected void interpolate(double frac) {
                // round to whole pixels to avoid layout jitter from sub-pixel size/position changes
                double w = Math.round(from.width()  + (to.width()  - from.width())  * frac);
                double h = Math.round(from.height() + (to.height() - from.height()) * frac);
                double x = Math.round(from.x() + (to.x() - from.x()) * frac);
                double y = Math.round(from.y() + (to.y() - from.y()) * frac);

                windowNode.setMinSize(w, h);
                windowNode.setMaxSize(w, h);
                windowNode.setLayoutX(x);
                windowNode.setLayoutY(y);
            }
        };

        t.setOnFinished(e -> {
            windowNode.setMinSize(to.width(), to.height());
            windowNode.setMaxSize(to.width(), to.height());
            if (onFinished != null) {
                onFinished.run();
            }
        });

        t.play();
    }

    /**
     * Two-phase minimize/restore animation between a window's normal bounds and its minimized slot.
     *
     * @param from      starting bounds ({@code TO_SLOT}: normal bounds; {@code FROM_SLOT}: slot bounds)
     * @param to        target bounds ({@code TO_SLOT}: slot bounds; {@code FROM_SLOT}: normal bounds)
     * @param direction whether the window is collapsing into the slot or expanding from it
     */
    private static void animateMinimize(Region windowNode, WindowBounds from, WindowBounds to,
            MinimizeDirection direction, Runnable onFinished) {

        windowNode.setLayoutX(from.x());
        windowNode.setLayoutY(from.y());
        windowNode.setMinSize(from.width(), from.height());
        windowNode.setMaxSize(from.width(), from.height());

        // pick anchor corner of 'from' closest to 'to' direction
        boolean anchorLeft = to.x() <= from.x();
        boolean anchorTop  = to.y() <= from.y();

        double anchorX = anchorLeft ? from.x() : from.x() + from.width();
        double anchorY = anchorTop  ? from.y() : from.y() + from.height();

        double targetAnchorX = anchorLeft ? to.x() : to.x() + to.width();
        double targetAnchorY = anchorTop  ? to.y() : to.y() + to.height();

        Transition t = new Transition() {
            {
                setCycleDuration(ANIMATION_DURATION);
                setInterpolator(Interpolator.EASE_BOTH);
            }
            @Override
            protected void interpolate(double frac) {
                // round to whole pixels to avoid layout jitter from sub-pixel size/position changes
                double w = Math.round(from.width()  + (to.width()  - from.width())  * frac);
                double h = Math.round(from.height() + (to.height() - from.height()) * frac);
                double curAnchorX = Math.round(anchorX + (targetAnchorX - anchorX) * frac);
                double curAnchorY = Math.round(anchorY + (targetAnchorY - anchorY) * frac);
                double x = anchorLeft ? curAnchorX : curAnchorX - w;
                double y = anchorTop  ? curAnchorY : curAnchorY - h;

                windowNode.setMinSize(w, h);
                windowNode.setMaxSize(w, h);
                windowNode.setLayoutX(x);
                windowNode.setLayoutY(y);
            }
        };

        t.setOnFinished(e -> {
            windowNode.setMinSize(to.width(), to.height());
            windowNode.setMaxSize(to.width(), to.height());
            if (onFinished != null) {
                onFinished.run();
            }
        });

        t.play();
    }

    /**
     * Finds the first free slot for a minimized window, scanning row by row starting from the bottom-left corner and
     * moving right, then upward.
     *
     * <p>A slot is considered free if its rectangle does not intersect (even partially) with any of the given
     * {@code minimizedWindowBounds}.
     *
     * @param minimizedWindowBounds bounds of windows already minimized (in {@code StackPane} coordinates)
     * @param containerWidth        width of the {@code StackPane}
     * @param containerHeight       height of the {@code StackPane}
     * @param slotWidth              width of a minimized window slot
     * @param slotHeight             height of a minimized window slot
     * @return bounds of the first free slot
     */
    private static WindowBounds findFirstFreeSlot(List<WindowBounds> minimizedWindowBounds, double containerWidth,
            double containerHeight, double slotWidth, double slotHeight) {

        int cols = Math.max(1, (int) Math.floor(containerWidth / slotWidth));
        int rows = Math.max(1, (int) Math.floor(containerHeight / slotHeight));

        for (int row = 0; row < rows; row++) {
            double y = containerHeight - (row + 1) * slotHeight;
            for (int col = 0; col < cols; col++) {
                double x = col * slotWidth;

                WindowBounds candidate = new WindowBounds(x, y, slotWidth, slotHeight);

                WindowBounds blocker = null;
                for (WindowBounds existing : minimizedWindowBounds) {
                    if (intersects(candidate, existing)) {
                        blocker = existing;
                        break;
                    }
                }

                if (blocker == null) {
                    return candidate;
                }
            }
        }

        double y = containerHeight - (rows + 1) * slotHeight;
        WindowBounds fallback = new WindowBounds(0, y, slotWidth, slotHeight);
        return fallback;
    }

    private static boolean intersects(WindowBounds a, WindowBounds b) {
        return a.x() < b.x() + b.width()
            && a.x() + a.width() > b.x()
            && a.y() < b.y() + b.height()
            && a.y() + a.height() > b.y();
    }


    private final ObservableList<WindowFxView<?>> modifiableWindows = FXCollections.observableArrayList();

    private final @Unmodifiable ObservableList<WindowFxView<?>> windows =
            FXCollections.unmodifiableObservableList(modifiableWindows);

    // The last window in z-order.
    private WindowFxView<?> lastWindow;

    private final Map<WindowFxView<?>, RestoreInfo> restoreInfosByWindow = new HashMap<>();

    private boolean animationEnabled = true;

    public AbstractWindowManager(Supplier<StackPane> stackPane) {
        super(stackPane);
    }

    @Override
    public void addWindow(WindowFxView<?> windowView) {
        if (windowView instanceof AbstractWindowFxView<?> fxView) {
            fxView.setWindowManager(this);
        }
        modifiableWindows.add(windowView);
        doAdd(windowView, windowView.getPresenter().isModal(), null);
        var aligner = new WindowAligner(getStackPane().get(), windowView);
        getStackPane().get().getScene().addPostLayoutPulseListener(aligner);
        reorderAll();
        focusLast();
    }

    @Override
    public void removeWindow(WindowFxView<?> windowView) {
        if (modifiableWindows.remove(windowView)) {
            this.restoreInfosByWindow.remove(windowView);
            doRemove(windowView, windowView.getPresenter().isModal());
            if (windowView instanceof AbstractWindowFxView<?> fxView) {
                fxView.setWindowManager(null);
            }
            reorderAll();
            focusLast();
        }
    }

    @Override
    public @Unmodifiable ObservableList<WindowFxView<?>> getWindows() {
        return windows;
    }

    @Override
    public void arrangeWindows(WindowArrangement arrangement) {
        if (this.windows.isEmpty()) {
            return;
        }

        List<WindowFxView<?>> windowsByZOder = getWindowsByZOrder();

        var stackPane = getStackPane().get();
        List<WindowBounds> bounds = switch (arrangement) {
            case CASCADE -> cascade(stackPane.getWidth(), stackPane.getHeight(), this.windows.size());
            case TILE_VERTICAL -> tileVertical(stackPane.getWidth(), stackPane.getHeight(), this.windows.size());
            case TILE_HORIZONTAL -> tileHorizontal(stackPane.getWidth(), stackPane.getHeight(), this.windows.size());
            case TILE_GRID -> tileGrid(stackPane.getWidth(), stackPane.getHeight(), this.windows.size());
            default -> throw new AssertionError();
        };

        this.animationEnabled = false;
        for (var i = 0; i < bounds.size(); i++) {
            var bound = bounds.get(i);
            var window = windowsByZOder.get(i);
            if (window.getPresenter().isMaximized()) {
                window.getPresenter().setMaximized(false);
            }
            if (window.getPresenter().isMinimized()) {
                window.getPresenter().setMinimized(false);
            }
            window.getNode().setLayoutX(bound.x);
            window.getNode().setLayoutY(bound.y);
            window.getPresenter().setWidth(bound.width);
            window.getPresenter().setHeight(bound.height);
        }
        this.animationEnabled = true;
    }

    @Override
    public void updateWindow(WindowFxView<?> windowView) {
        reorderAll();
    }

    @Override
    public void maximizeWindow(WindowFxView<?> window) {
        var restoreInfo = restoreInfosByWindow.get(window);
        if (restoreInfo != null) { // from minimized
            restoreInfo.state = SpecialState.MAXIMIZED;
        } else {
            createAndSaveRestoreInfo(SpecialState.MAXIMIZED, window);
        }
        var n = window.getNode();
        var from = new WindowBounds(n.getLayoutX(), n.getLayoutY(), n.getWidth(), n.getHeight());
        var sp = getStackPane().get();
        var width = sp.getWidth() - sp.getPadding().getRight() - sp.getPadding().getLeft();
        var height = sp.getHeight() - sp.getPadding().getTop() - sp.getPadding().getBottom();
        var to = new WindowBounds(0, 0, width, height);
        Runnable onFinished = () -> {
            n.setMaxSize(Region.USE_COMPUTED_SIZE, Region.USE_COMPUTED_SIZE);
            n.setMinSize(Region.USE_COMPUTED_SIZE, Region.USE_COMPUTED_SIZE);
            AnchorPane.setTopAnchor(n, 0.0);
            AnchorPane.setRightAnchor(n, 0.0);
            AnchorPane.setBottomAnchor(n, 0.0);
            AnchorPane.setLeftAnchor(n, 0.0);
        };
        if (animationEnabled) {
            animateMaximize(n, from, to, onFinished);
        } else {
            n.setLayoutX(to.x);
            n.setLayoutX(to.y);
            onFinished.run();
        }
    }

    @Override
    public void minimizeWindow(WindowFxView<?> window) {
        var restoreInfo = restoreInfosByWindow.get(window);
        if (restoreInfo != null) { // from maximized
            AnchorPane.clearConstraints(window.getNode());
            restoreInfo.state = SpecialState.MINIMIZED;
        } else {
            createAndSaveRestoreInfo(SpecialState.MINIMIZED, window);
        }
        var allMinimizedWindowBounds = this.windows.stream()
                .filter(w -> w.getPresenter().isMinimized() && w != window)
                .map(w -> {
                    var n = w.getNode();
                    return new WindowBounds(n.getLayoutX(), n.getLayoutY(), n.getWidth(), n.getHeight());
                })
                .toList();
        var width = getMinimizedWindowWidth();
        var height = getTitleBarHeight(window);
        var sp = getStackPane().get();
        var windowBounds = findFirstFreeSlot(allMinimizedWindowBounds, sp.getWidth(), sp.getHeight(), width, height);
        var n = window.getNode();
        if (this.animationEnabled) {
            var from = new WindowBounds(n.getLayoutX(), n.getLayoutY(), n.getWidth(), n.getHeight());
            animateMinimize(n, from, windowBounds, MinimizeDirection.TO_SLOT, null);
        } else {
            n.setLayoutX(windowBounds.x);
            n.setLayoutY(windowBounds.y);
            n.setMaxSize(windowBounds.width, windowBounds.height);
            n.setMinSize(windowBounds.width, windowBounds.height);
        }
    }

    @Override
    public void restoreWindow(WindowFxView<?> window) {
        var info = this.restoreInfosByWindow.remove(window);
        if (info != null) {
            if (info.state == SpecialState.MAXIMIZED) {
                AnchorPane.clearConstraints(window.getNode());
            }
            var restoredBounds = restoreBounds(info.bounds);
            if (this.animationEnabled) {
                var n = window.getNode();
                var from = new WindowBounds(n.getLayoutX(), n.getLayoutY(), n.getWidth(), n.getHeight());
                if (info.state == SpecialState.MAXIMIZED) {
                    animateMaximize(n, from, restoredBounds, null);
                } else {
                    animateMinimize(n, from, restoredBounds, MinimizeDirection.FROM_SLOT, null);
                }
            } else {
                window.getNode().setLayoutX(restoredBounds.x);
                window.getNode().setLayoutY(restoredBounds.y);
                window.setWidth(restoredBounds.width);
                window.setHeight(restoredBounds.height);
            }
        }
    }

    protected void deactivateAllWindows(@Nullable WindowFxView<?> exclude) {
        for (var window : windows) {
            if (window.getPresenter().isActive() && window != exclude) {
                if (window instanceof AbstractWindowFxView<?> windowView) {
                    windowView.setActive(false);
                }
                break;
            }
        }
    }

    protected void activateWindow(WindowFxView<?> window) {
        if (!window.getPresenter().isActive() && window instanceof AbstractWindowFxView<?> windowView) {
            if (!window.getPresenter().isActive()) {
                windowView.setActive(true);
            }
        }
    }

    protected double getTitleBarHeight(WindowFxView<?> window) {
        if (window instanceof AbstractWindowFxView<?> windowView) {
            return windowView.getTitleBar().getHeight() + windowView.getWindowBox().getPadding().getBottom()
                    + windowView.getWindowBox().getPadding().getTop();
        }
        return -1;
    }

    @Override
    protected void focusLast() {
        if (getLastPopup() != null) {
            super.focusLast();
        } else if (this.lastWindow != null) {
            // there is a time gap, if a window is activated by focus, so it looks very stange
            // that's why we do manual activation here
            deactivateAllWindows(this.lastWindow);
            activateWindow(this.lastWindow);
            this.lastWindow.requestFocus();
        }
    }

    @Override
    protected ChildFxView<?> getLastModal() {
        var popup = super.getLastModal();
        if (popup != null) {
            return popup;
        }
        if (this.lastWindow != null && this.lastWindow.getPresenter().isModal()) {
            return this.lastWindow;
        }
        return null;
    }

    @Override
    protected void doAdd(ChildFxView<?> view, boolean modal, Anchors anchors) {
        if (view instanceof PopupFxView<?> popup) {
            super.doAdd(popup, modal, anchors);
            return;
        }
        //for every node a bg pane is created
        Pane bgPane = new WindowPane((Node) view.getNode());
        if (modal) {
            onModalAdded();
        } else {
            // Do not use setMouseTransparent(true) here — it propagates to all children, making them unresponsive to
            // mouse events regardless of their own settings. Instead, disable bounds-based picking so the pane itself
            // does not consume mouse events, while children remain fully interactive.
            bgPane.setPickOnBounds(false);
        }
        getStackPane().get().getChildren().add(bgPane);
    }

    double getMinimizedWindowWidth() {
        return MINIMIZED_WINDOW_WIDTH;
    }

    void onStageUnfocused() {
        deactivateAllWindows(null);
    }

    void onFocusedComponentChanged(ParentFxView<?> component) {
        findComponentWindow(component, getStackPane().get()).ifPresent(w -> {
            if (!w.getPresenter().isActive()) {
                deactivateAllWindows(null);
                activateWindow(w);
                w.getNode().getParent().toFront();
            }
            reorderAll();
        });
    }

    private void createAndSaveRestoreInfo(SpecialState state, WindowFxView<?> window) {
        var n = window.getNode();
        var bounds = new WindowBounds(n.getLayoutX(), n.getLayoutY(), n.getWidth(), n.getHeight());
        var info = new RestoreInfo(state, bounds);
        this.restoreInfosByWindow.put(window, info);
    }

    // Z-order:
    // Regular windows
    // Always-on-top windows
    // Modal windows
    // Regular popups (notifications, tooltips)
    // Modal popups — always on top of everything
    private void reorderAll() {
        reorderWindows();
        reorderPopups();
    }

    private void reorderWindows() {
        this.lastWindow = null;
        var windows = getWindowsByZOrder();
        List<WindowFxView<?>> modalWindows = new ArrayList<>();
        for (var w : windows) {
            this.lastWindow = w;
            var bgPane = getWindowPane(w);
            if (w.getPresenter().isAlwaysOnTop()) {
                if (bgPane != null) {
                    bgPane.toFront();
                }
            }
            if (w.getPresenter().isModal() && bgPane != null) {
                modalWindows.add(w);
            }
        }

        for (var w: modalWindows) {
            var bgPane = getWindowPane(w);
            if (bgPane != null) {
                bgPane.toFront();
            }
        }
        if (!modalWindows.isEmpty()) {
            this.lastWindow = modalWindows.getLast();
        }
    }

    private List<WindowFxView<?>> getWindowsByZOrder() {
        return getStackPane().get().getChildren().stream()
                .filter(n -> n.getClass() == WindowPane.class)
                .map(WindowPane.class::cast)
                .<WindowFxView<?>>map(n -> (WindowFxView<?>) FxViewUtils.getView(n.getChildren().get(0)))
                .toList();
    }

    /**
     * Restores window bounds after unmaximizing, adjusting position and size to fit within the current container
     * dimensions.
     *
     * <p>The algorithm:
     * <ol>
     *   <li>If the window fits at its saved position — restore as-is.</li>
     *   <li>If the window fits but is out of bounds — clamp position to container edges.</li>
     *   <li>If the window is larger than the container — clamp size to container, position to 0.</li>
     * </ol>
     *
     * @param saved     the window bounds saved before maximizing
     * @param container the current container dimensions
     * @return corrected bounds that fit within the container
     */
    private WindowBounds restoreBounds(WindowBounds saved) {
        double containerWidth  = getStackPane().get().getWidth();
        double containerHeight = getStackPane().get().getHeight();

        double width  = Math.min(saved.width(),  containerWidth);
        double height = Math.min(saved.height(), containerHeight);

        double x = Math.max(0, Math.min(saved.x(), containerWidth  - width));
        double y = Math.max(0, Math.min(saved.y(), containerHeight - height));

        return new WindowBounds(x, y, width, height);
    }
}
