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

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import javafx.beans.property.ReadOnlyIntegerWrapper;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.scene.input.KeyEvent;
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
    private static final class WindowAligner implements Runnable {

        private final StackPane stackPane;

        private final AbstractDialogView<?, ?> dialogView;

        WindowAligner(StackPane stackPane, AbstractDialogView<?, ?> dialogView) {
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

    private final StackPane stackPane;

    private final VBox mainPane;

    private final LinkedList<DialogView<?, ?>> modifiableDialogs = new LinkedList<>();

    private final List<DialogView<?, ?>> dialogs = Collections.unmodifiableList(modifiableDialogs);

    private final LinkedList<Pane> bgPanes = new LinkedList<>();

    private final ReadOnlyIntegerWrapper dialogCount;

    public DefaultDialogManager(StackPane stackPane, VBox mainPane, ReadOnlyIntegerWrapper dialogCount) {
        this.stackPane = stackPane;
        this.mainPane = mainPane;
        this.dialogCount = dialogCount;
    }

    @Override
    public void showDialog(DialogView<?, ?> dialogView) {
        if (modifiableDialogs.isEmpty()) {
            //event consumer added only once for all dialogs
            mainPane.addEventFilter(KeyEvent.ANY, eventConsumer);
        } else {
            var last = modifiableDialogs.peekLast();
            last.getViewModel().setActive(true);
        }
        var d = (AbstractDialogView) dialogView;
        var window = d.getNode();
        //for every dialog window a bg pane is created
        var bgPane = new Pane(window);
        bgPane.setMouseTransparent(false);
        stackPane.getChildren().add(bgPane);
        modifiableDialogs.addLast(d);
        bgPanes.addLast(bgPane);
        var aligner = new WindowAligner(stackPane, d);
        stackPane.getScene().addPostLayoutPulseListener(aligner);
        this.dialogCount.set(this.modifiableDialogs.size());
    }

    @Override
    public void hideDialog(DialogView<?, ?> dialogView) {
        var dialog = modifiableDialogs.pollLast();
        if (dialog == null) {
            return;
        }
        var bgPane = bgPanes.pollLast();
        stackPane.getChildren().remove(bgPane);
        if (modifiableDialogs.isEmpty()) {
            mainPane.removeEventFilter(KeyEvent.ANY, eventConsumer);
        } else {
            var last = modifiableDialogs.peekLast();
            last.getViewModel().setActive(false);
        }
        this.dialogCount.set(this.modifiableDialogs.size());
    }

    @Override
    public List<DialogView<?, ?>> getDialogs() {
        return dialogs;
    }

    protected int getDialogCount() {
        return modifiableDialogs.size();
    }
}
