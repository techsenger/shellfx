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

package com.techsenger.tabshell.core.dialog;

import com.techsenger.annotations.Unmodifiable;
import com.techsenger.tabshell.core.popup.PopupFxView;
import com.techsenger.tabshell.material.Anchors;
import java.util.HashSet;
import java.util.Set;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

/**
 *
 * @author Pavel Castornii
 */
public class DefaultDialogManager implements DialogManager {

    /**
     * To align window to the center of the StackPane its necessary to know window sizes. However, these sizes
     * will be available only after layout pulse. So, window aligner class that is used as a pulse listener.
     */
    private static final class DialogAligner implements Runnable {

        private final StackPane stackPane;

        private final DialogFxView<?> dialogView;

        DialogAligner(StackPane stackPane, DialogFxView<?> dialogView) {
            this.stackPane = stackPane;
            this.dialogView = dialogView;
        }

        @Override
        public void run() {
            var window = dialogView.getNode();
            var x = (stackPane.getWidth() / 2) - (window.getWidth() / 2);
            var y = (stackPane.getHeight() / 2) - (window.getHeight() / 2);
            //move a little bit up
            y -= 50;
            y = Math.max(0, y);
            /* if values are left as a double, the dialog border might appear blurry when the values are decimal */
            window.setLayoutX((int) x);
            window.setLayoutY((int) y);
            stackPane.getScene().removePostLayoutPulseListener(this);
            dialogView.requestFocus();
        }
    }

    /**
     * Provides keyboard event consumer to disable keyboard input.
     *
     * @return
     */
    private final EventHandler<? super Event> eventConsumer = event -> event.consume();

    private boolean eventConsumerAdded = false;

    private final StackPane stackPane;

    private final VBox mainPane;

    private final ObservableList<DialogFxView<?>> modifiableDialogs = FXCollections.observableArrayList();

    private final @Unmodifiable ObservableList<DialogFxView<?>> dialogs =
            FXCollections.unmodifiableObservableList(modifiableDialogs);

    private final ObservableList<PopupFxView<?>> modifiablePopups = FXCollections.observableArrayList();

    private final @Unmodifiable ObservableList<PopupFxView<?>> popups =
            FXCollections.unmodifiableObservableList(modifiablePopups);

    private final Set<PopupFxView<?>> allModalComponents = new HashSet<>();

    public DefaultDialogManager(StackPane stackPane, VBox mainPane) {
        this.stackPane = stackPane;
        this.mainPane = mainPane;
    }

    @Override
    public void showDialog(DialogFxView<?> dialogView) {
        if (!modifiableDialogs.isEmpty()) {
            var last = modifiableDialogs.getLast();
            last.setActive(false);
        }
        modifiableDialogs.add(dialogView);
        doShow(dialogView, null);
        dialogView.setActive(true);
        var aligner = new DialogAligner(stackPane, dialogView);
        stackPane.getScene().addPostLayoutPulseListener(aligner);
    }

    @Override
    public void hideDialog(DialogFxView<?> dialogView) {
        if (modifiableDialogs.remove(dialogView)) {
            doHide(dialogView);
            if (!modifiableDialogs.isEmpty()) {
                var last = modifiableDialogs.getLast();
                last.setActive(true);
            }
        }
    }

    @Override
    public ObservableList<DialogFxView<?>> getDialogs() {
        return dialogs;
    }

    @Override
    public void showPopup(PopupFxView<?> view, Anchors anchors) {
        modifiablePopups.add(view);
        doShow(view, anchors);
    }

    @Override
    public void hidePopup(PopupFxView<?> view) {
        if (modifiablePopups.remove(view)) {
            doHide(view);
        }
    }

    @Override
    public ObservableList<PopupFxView<?>> getPopups() {
        return popups;
    }

    protected int getDialogCount() {
        return modifiableDialogs.size();
    }

    private void doShow(PopupFxView<?> view, Anchors anchors) {
        var node = view.getNode();
        // node.setMouseTransparent(false);
        //for every node a bg pane is created
        Pane bgPane;
        if (anchors != null) {
            bgPane = new AnchorPane(node);
            AnchorPane.setTopAnchor(node, anchors.getTop());
            AnchorPane.setRightAnchor(node, anchors.getRight());
            AnchorPane.setBottomAnchor(node, anchors.getBottom());
            AnchorPane.setLeftAnchor(node, anchors.getLeft());
            // Allow clicks through empty areas; false = clicks pass through empty
            // areas to underlying controls, true = entire container blocks clicks
            bgPane.setPickOnBounds(view.getPresenter().isModal());
        } else {
            bgPane = new Pane(node);
        }
        bgPane.setMouseTransparent(false);
        if (view.getPresenter().isModal()) {
            allModalComponents.add(view);
            if (!eventConsumerAdded) {
                //event consumer added only once
                // mainPane.addEventFilter(KeyEvent.ANY, eventConsumer);
                eventConsumerAdded = true;
            }
        }
        stackPane.getChildren().add(bgPane);
    }

    private void doHide(PopupFxView<?> view) {
        stackPane.getChildren().remove(view.getNode().getParent());
        if (view.getPresenter().isModal()) {
            allModalComponents.remove(view);
        }
        if (allModalComponents.isEmpty()) {
            mainPane.removeEventFilter(KeyEvent.ANY, eventConsumer);
            eventConsumerAdded = false;
        }
    }
}
