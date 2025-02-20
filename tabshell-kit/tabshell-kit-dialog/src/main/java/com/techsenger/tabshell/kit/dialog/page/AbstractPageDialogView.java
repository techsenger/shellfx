/*
 * Copyright 2024-2025 Pavel Castornii.
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

package com.techsenger.tabshell.kit.dialog.page;

import atlantafx.base.theme.Styles;
import com.techsenger.tabshell.core.page.PageView;
import com.techsenger.tabshell.kit.dialog.AbstractSimpleDialogView;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.layout.VBox;

/**
 *
 * @author Pavel Castornii
 */
public abstract class AbstractPageDialogView<S extends PageView<?>, T extends AbstractPageDialogViewModel>
        extends AbstractSimpleDialogView<T> {

    private final ListView<S> pageListView = new ListView<>();

    private final VBox pageContainer = new VBox();

    public AbstractPageDialogView(T viewModel) {
        super(viewModel);
    }

    protected ListView<S> getPageListView() {
        return pageListView;
    }

    protected VBox getPageContainer() {
        return pageContainer;
    }

    @Override
    protected void build(T viewModel) {
        super.build(viewModel);
        pageListView.getStyleClass().add(Styles.DENSE);
        pageListView.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(S page, boolean empty) {
                super.updateItem(page, empty);
                if (empty || page == null) {
                    setText(null);
                } else {
                    setText(page.getViewModel().getTitle());
                }
            }
        });
    }

    @Override
    protected void addListeners(T viewModel) {
        super.addListeners(viewModel);
        pageListView.getSelectionModel().selectedItemProperty().addListener((ov, oldV, newV) -> {
            getFocusTrap().deactivate();
            this.pageContainer.getChildren().clear();
            if (oldV != null) {
                oldV.doOnDeselected();
            }
            if (newV != null) {
                this.pageContainer.getChildren().add(newV.getNode());
                newV.doOnSelected();
                getFocusTrap().activate();
            }
        });
    }

    @Override
    protected void preInitialize(T viewModel) {
        super.preInitialize(viewModel);
        this.pageListView.getItems().forEach(p -> p.initialize());
    }

    @Override
    protected void postInitialize(T viewModel) {
        super.postInitialize(viewModel);
        this.pageListView.getSelectionModel().select(0);
    }

    @Override
    protected void postDeinitialize(T viewModel) {
        super.postDeinitialize(viewModel);
        this.pageListView.getItems().forEach(p -> p.deinitialize());
    }
}
