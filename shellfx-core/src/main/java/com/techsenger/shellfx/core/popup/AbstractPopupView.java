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
import com.techsenger.shellfx.core.area.AbstractAreaView;
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
public abstract class AbstractPopupView<VM extends AbstractPopupViewModel<?>>
        extends AbstractAreaView<VM> implements PopupView<VM> {

    public class Composer extends AbstractAreaView<VM>.Composer implements PopupView.Composer {

        private final AbstractPopupView<VM> view = AbstractPopupView.this;

        @Override
        public void close() {
            var parent = getParent();
            if (parent != null) {
                parent.getComposer().closePopup(view);
            }
        }

        @Override
        public @Nullable PopupContainerView<?> getParent() {
            return (PopupContainerView<?>) super.getParent();
        }

        @Override
        public @Nullable PopupContainerPort getParentPort() {
            var container = getParent();
            return container == null ? null : container.getViewModel();
        }
    }

    private final VBox contentBox = new VBox();

    /**
     * This is internal pane that is required for waiting mode.
     */
    private final StackPane stackPane = new StackPane(contentBox);

    private final Pane waitingPane = new Pane();

    public AbstractPopupView(VM viewModel) {
        super(viewModel);
    }

    @Override
    public Pane getNode() {
        return this.stackPane;
    }

    @Override
    public Composer getComposer() {
        return (Composer) super.getComposer();
    }

    @Override
    protected Composer createComposer() {
        return new AbstractPopupView<VM>.Composer();
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

    @Override
    protected void addListeners() {
        super.addListeners();
        getViewModel().waitingProperty().addListener((ov, oldV, newV) -> {
            if (newV) {
                stackPane.getChildren().add(waitingPane);
            } else {
                stackPane.getChildren().remove(waitingPane);
            }
        });
        getViewModel().widthSource().addListener((value) -> stackPane.setPrefWidth(value));
        getViewModel().heightSource().addListener((value) -> stackPane.setPrefHeight(value));
    }
}
