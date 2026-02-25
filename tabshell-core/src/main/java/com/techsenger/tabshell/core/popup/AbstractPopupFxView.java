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

import com.techsenger.tabshell.core.area.AbstractAreaFxView;
import com.techsenger.toolkit.fx.value.ValueUtils;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.scene.Cursor;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

/**
 *
 * @author Pavel Castornii
 */
public abstract class AbstractPopupFxView<P extends PopupPresenter<?, ?>>
        extends AbstractAreaFxView<P> implements PopupFxView<P> {

    public class Composer extends AbstractAreaFxView<P>.Composer implements PopupFxView.Composer {

        private final AbstractPopupFxView<P> view = AbstractPopupFxView.this;

        @Override
        public void remove() {
            var parent = view.getParent();
            if (parent != null) {
                ((PopupContainerFxView.Composer) parent.getComposer()).removePopup(view);
            }
        }
    }

    private final VBox contentBox = new VBox();

    /**
     * This is internal pane that is required for waiting mode.
     */
    private final StackPane stackPane = new StackPane(contentBox);

    private final BooleanProperty waiting = new SimpleBooleanProperty(false);

    private final Pane waitingPane = new Pane();

    public AbstractPopupFxView() {
        super();
    }

    @Override
    public Pane getNode() {
        return this.stackPane;
    }

    @Override
    public double getPrefWidth() {
        return stackPane.getPrefWidth();
    }

    @Override
    public void setPrefWidth(double value) {
        stackPane.setPrefWidth(value);
    }

    @Override
    public double getPrefHeight() {
        return stackPane.getPrefHeight();
    }

    @Override
    public void setPrefHeight(double value) {
        stackPane.setPrefHeight(value);
    }

    @Override
    public void setWaiting(boolean waiting) {
        this.waiting.set(waiting);
    }

    @Override
    public boolean isWaiting() {
        return this.waiting.get();
    }

    @Override
    public Composer getComposer() {
        return (Composer) super.getComposer();
    }

    @Override
    protected Composer createComposer() {
        return new AbstractPopupFxView.Composer();
    }

    protected BooleanProperty waitingProperty() {
        return waiting;
    }

    protected VBox getContentBox() {
        return contentBox;
    }

    @Override
    protected void build() {
        waitingPane.setMouseTransparent(false);
        waitingPane.setCursor(Cursor.WAIT);

        VBox.setVgrow(contentBox, Priority.ALWAYS);
        VBox.setVgrow(stackPane, Priority.ALWAYS);
    }

    @Override
    protected void addListeners() {
        super.addListeners();
        ValueUtils.callAndAddListener(this.waiting, (ov, oldV, newV) -> {
            if (newV) {
                stackPane.getChildren().add(waitingPane);
            } else {
                stackPane.getChildren().remove(waitingPane);
            }
        });
    }
}
