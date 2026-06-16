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
import com.techsenger.shellfx.core.area.AbstractAreaFxView;
import javafx.scene.Cursor;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

/**
 *
 * @author Pavel Castornii
 */
public abstract class AbstractPopupFxView<P extends AbstractPopupPresenter<?>>
        extends AbstractAreaFxView<P> implements PopupFxView<P> {

    public class Composer extends AbstractAreaFxView<P>.Composer implements PopupFxView.Composer {

        private final AbstractPopupFxView<P> view = AbstractPopupFxView.this;

        @Override
        public void close() {
            var parent = getParent();
            if (parent != null) {
                ((PopupContainerFxView.Composer) parent.getComposer()).closePopup(view);
            }
        }

        @Override
        public @Nullable PopupContainerFxView<?> getContainer() {
            return getParent(PopupContainerFxView.class);
        }

        @Override
        public @Nullable PopupContainerPort getContainerPort() {
            var container = getContainer();
            return container == null ? null : container.getPresenter();
        }
    }

    private final VBox contentBox = new VBox();

    /**
     * This is internal pane that is required for waiting mode.
     */
    private final StackPane stackPane = new StackPane(contentBox);

    private final Pane waitingPane = new Pane();

    public AbstractPopupFxView() {
        super();
    }

    @Override
    public Pane getNode() {
        return this.stackPane;
    }

    @Override
    public void setPrefWidth(double value) {
        stackPane.setPrefWidth(value);
    }

    @Override
    public void setPrefHeight(double value) {
        stackPane.setPrefHeight(value);
    }

    @Override
    public void setWaiting(boolean waiting) {
        if (waiting) {
            stackPane.getChildren().add(waitingPane);
        } else {
            stackPane.getChildren().remove(waitingPane);
        }
    }

    @Override
    public Composer getComposer() {
        return (Composer) super.getComposer();
    }

    @Override
    protected Composer createComposer() {
        return new AbstractPopupFxView.Composer();
    }

    protected VBox getContentBox() {
        return contentBox;
    }

    @Override
    protected void build() {
        waitingPane.setMouseTransparent(false);
        waitingPane.setCursor(Cursor.WAIT);

        contentBox.setMinSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE);
        VBox.setVgrow(contentBox, Priority.ALWAYS);
        VBox.setVgrow(stackPane, Priority.ALWAYS);
    }
}
