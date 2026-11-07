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

package com.techsenger.shellfx.core.popup;

import com.techsenger.annotations.Nullable;
import com.techsenger.annotations.Unmodifiable;
import com.techsenger.patternfx.mvvm.ChildView;
import com.techsenger.shellfx.material.Anchors;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;
import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;

/**
 *
 * @author Pavel Castornii
 */
public abstract class AbstractPopupManager implements PopupManager {

    protected static final class PopupPane extends AnchorPane {

        // Reacts to the popup becoming visible or invisible; kept here to be removed together with the popup.
        private @Nullable ChangeListener<Boolean> visibilityListener;

        private PopupPane(Node... nodes) {
            super(nodes);
        }
    }

    private static @Nullable PopupPane getPopupPane(PopupView<?> popup) {
        if (popup.getNode() != null) {
            return (PopupPane) popup.getNode().getParent();
        } else {
            return null;
        }
    }

    private final ObservableList<PopupView<?>> modifiablePopups = FXCollections.observableArrayList();

    private final @Unmodifiable ObservableList<PopupView<?>> popups =
            FXCollections.unmodifiableObservableList(modifiablePopups);

    private final Supplier<StackPane> stackPane;

    private int modalCount = 0;

    private boolean containerBlocked;

    // The last popup in z-order.
    private PopupView<?> lastPopup;

    private final EventHandler<Event> eventBlocker = event -> {
        if (!isEventTargetInsideModal(event)) {
            event.consume();
        }
    };

    public AbstractPopupManager(Supplier<StackPane> stackPane) {
        this.stackPane = stackPane;
    }

    @Override
    public boolean isContainerBlocked() {
        return containerBlocked;
    }

    @Override
    public void addPopup(PopupView<?> view, Anchors anchors) {
        modifiablePopups.add(view);
        doAdd(view, view.getViewModel().isModal(), anchors);
    }

    @Override
    public void removePopup(PopupView<?> view) {
        if (modifiablePopups.remove(view)) {
            // an invisible popup took no part in z-order and focus, so its removal changes none of them
            doRemove(view, view.getViewModel().isModal());
            if (view.getViewModel().isVisible()) {
                reorderPopups();
            }
        }
    }

    @Override
    public @Unmodifiable ObservableList<PopupView<?>> getPopups() {
        return this.popups;
    }

    protected Supplier<StackPane> getStackPane() {
        return stackPane;
    }

    protected ObservableList<PopupView<?>> getModifiablePopups() {
        return modifiablePopups;
    }

    protected EventHandler<Event> getEventBlocker() {
        return eventBlocker;
    }

    protected abstract void onContainerBlocked(boolean blocked);

    protected void onModalAdded() {
        if (modalCount == 0) {
            this.containerBlocked = true;
            onContainerBlocked(this.containerBlocked);
        }
        modalCount++;
    }

    protected void onModalRemoved() {
        modalCount--;
        if (modalCount == 0) {
            this.containerBlocked = false;
            onContainerBlocked(this.containerBlocked);
        }
    }

    protected void doAdd(ChildView<?> view, boolean modal, Anchors anchors) {
        Pane node = (Pane) view.getNode();
        var popupView = (PopupView<?>) view;
        var bgPane = new PopupPane(node);
        bgPane.visibilityListener = (ov, oldV, newV) -> {
            if (newV) {
                onPopupShown(popupView);
            } else {
                onPopupHidden(popupView);
            }
        };
        popupView.getViewModel().visibleProperty().addListener(bgPane.visibilityListener);
        // the pane is shown by onPopupShown(), the same way as when the popup becomes visible later
        bgPane.setVisible(false);
        bgPane.setManaged(false);
        AnchorPane.setTopAnchor(node, anchors.getTop());
        AnchorPane.setRightAnchor(node, anchors.getRight());
        AnchorPane.setBottomAnchor(node, anchors.getBottom());
        AnchorPane.setLeftAnchor(node, anchors.getLeft());
        if (!modal) {
            // Do not use setMouseTransparent(true) here — it propagates to all children, making them unresponsive to
            // mouse events regardless of their own settings. Instead, disable bounds-based picking so the pane itself
            // does not consume mouse events, while children remain fully interactive.
            bgPane.setPickOnBounds(false);
        }
        this.stackPane.get().getChildren().add(bgPane);
        if (popupView.getViewModel().isVisible()) {
            onPopupShown(popupView);
        }
    }

    protected void doRemove(ChildView<?> view, boolean modal) {
        Pane node = (Pane) view.getNode();
        var modalRemoved = modal;
        if (view instanceof PopupView<?> popup) {
            var bgPane = getPopupPane(popup);
            if (bgPane != null && bgPane.visibilityListener != null) {
                popup.getViewModel().visibleProperty().removeListener(bgPane.visibilityListener);
            }
            // an invisible modal popup never blocked the container
            modalRemoved = modal && popup.getViewModel().isVisible();
        }
        this.stackPane.get().getChildren().remove(node.getParent());
        if (modalRemoved) {
            onModalRemoved();
        }
    }

    protected void reorderPopups() {
        this.lastPopup = null;
        List<PopupView<?>> modalPopups = new ArrayList<>();
        for (var popup : popups) {
            if (!popup.getViewModel().isVisible()) {
                continue;
            }
            this.lastPopup = popup;
            var pane = getPopupPane(popup);
            if (pane != null) {
                pane.toFront();
                if (popup.getViewModel().isModal()) {
                    modalPopups.add(popup);
                }
            }
        }

        for (var popup : modalPopups) {
            var pane = getPopupPane(popup);
            if (pane != null) {
                pane.toFront();
            }
        }
        if (!modalPopups.isEmpty()) {
            this.lastPopup = modalPopups.getLast();
        }
    }

    protected @Nullable ChildView<?> getLastModal() {
        if (this.lastPopup != null && this.lastPopup.getViewModel().isModal()) {
            return this.lastPopup;
        } else {
            return null;
        }
    }

    private void onPopupShown(PopupView<?> popup) {
        var bgPane = getPopupPane(popup);
        if (bgPane != null) {
            bgPane.setVisible(true);
            bgPane.setManaged(true);
        }
        reorderPopups();
        // A popup never takes the focus itself: the client calls requestFocus() or the user clicks it.
        if (popup.getViewModel().isModal()) {
            onModalAdded();
        }
    }

    private void onPopupHidden(PopupView<?> popup) {
        var bgPane = getPopupPane(popup);
        if (bgPane != null) {
            bgPane.setVisible(false);
            bgPane.setManaged(false);
        }
        reorderPopups();
        if (popup.getViewModel().isModal()) {
            onModalRemoved();
        }
    }

    private boolean isEventTargetInsideModal(Event event) {
        ChildView<?> modalComponent = getLastModal();
        if (modalComponent == null) {
            return false;
        }
        if (event.getTarget() instanceof Node target) {
            Node current = target;
            while (current != null) {
                if (current == modalComponent.getNode()) {
                    return true;
                }
                current = current.getParent();
            }
        }
        return false;
    }
}
