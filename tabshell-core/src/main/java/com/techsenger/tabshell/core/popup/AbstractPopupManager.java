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

package com.techsenger.tabshell.core.popup;

import com.techsenger.annotations.Nullable;
import com.techsenger.annotations.Unmodifiable;
import com.techsenger.patternfx.mvp.ChildFxView;
import com.techsenger.tabshell.material.Anchors;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;
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

        private PopupPane(Node... nodes) {
            super(nodes);
        }
    }

    private static @Nullable PopupPane getPopupPane(PopupFxView<?> popup) {
        if (popup.getNode() != null) {
            return (PopupPane) popup.getNode().getParent();
        } else {
            return null;
        }
    }

    private final ObservableList<PopupFxView<?>> modifiablePopups = FXCollections.observableArrayList();

    private final @Unmodifiable ObservableList<PopupFxView<?>> popups =
            FXCollections.unmodifiableObservableList(modifiablePopups);

    private final Supplier<StackPane> stackPane;

    private int modalCount = 0;

    private boolean containerBlocked;

    // The last popup in z-order.
    private PopupFxView<?> lastPopup;

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
    public void addPopup(PopupFxView<?> view, Anchors anchors) {
        modifiablePopups.add(view);
        doAdd(view, view.getPresenter().isModal(), anchors);
        reorderPopups();
        focusLast();
    }

    @Override
    public void removePopup(PopupFxView<?> view) {
        if (modifiablePopups.remove(view)) {
            doRemove(view, view.getPresenter().isModal());
            reorderPopups();
            focusLast();
        }
    }

    @Override
    public @Unmodifiable ObservableList<PopupFxView<?>> getPopups() {
        return this.popups;
    }

    protected void focusLast() {
        if (this.lastPopup != null) {
            this.lastPopup.requestFocus();
        }
    }

    protected PopupFxView<?> getLastPopup() {
        return lastPopup;
    }

    protected Supplier<StackPane> getStackPane() {
        return stackPane;
    }

    protected ObservableList<PopupFxView<?>> getModifiablePopups() {
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

    protected void doAdd(ChildFxView<?> view, boolean modal, Anchors anchors) {
        Pane node = (Pane) view.getNode();
        var bgPane = new PopupPane(node);
        AnchorPane.setTopAnchor(node, anchors.getTop());
        AnchorPane.setRightAnchor(node, anchors.getRight());
        AnchorPane.setBottomAnchor(node, anchors.getBottom());
        AnchorPane.setLeftAnchor(node, anchors.getLeft());
        if (modal) {
            onModalAdded();
        } else {
            // Do not use setMouseTransparent(true) here — it propagates to all children, making them unresponsive to
            // mouse events regardless of their own settings. Instead, disable bounds-based picking so the pane itself
            // does not consume mouse events, while children remain fully interactive.
            bgPane.setPickOnBounds(false);
        }
        this.stackPane.get().getChildren().add(bgPane);
    }

    protected void doRemove(ChildFxView<?> view, boolean modal) {
        Pane node = (Pane) view.getNode();
        this.stackPane.get().getChildren().remove(node.getParent());
        if (modal) {
            onModalRemoved();
        }
    }

    protected void reorderPopups() {
        this.lastPopup = null;
        List<PopupFxView<?>> modalPopups = new ArrayList<>();
        for (var popup : popups) {
            this.lastPopup = popup;
            var pane = getPopupPane(popup);
            if (pane != null) {
                pane.toFront();
                if (popup.getPresenter().isModal()) {
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

    protected @Nullable ChildFxView<?> getLastModal() {
        if (this.lastPopup != null && this.lastPopup.getPresenter().isModal()) {
            return this.lastPopup;
        } else {
            return null;
        }
    }

    private boolean isEventTargetInsideModal(Event event) {
        ChildFxView<?> modalComponent = getLastModal();
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
