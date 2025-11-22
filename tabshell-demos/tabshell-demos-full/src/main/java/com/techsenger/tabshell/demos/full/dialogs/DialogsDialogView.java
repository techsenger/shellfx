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

package com.techsenger.tabshell.demos.full.dialogs;

import atlantafx.base.theme.Styles;
import com.techsenger.tabshell.core.ShellView;
import com.techsenger.tabshell.core.style.SizeConstants;
import com.techsenger.tabshell.dialogs.AbstractSimpleDialogView;
import com.techsenger.toolkit.fx.utils.ButtonUtils;
import javafx.geometry.Insets;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

/**
 *
 * @author Pavel Castornii
 */
public class DialogsDialogView extends AbstractSimpleDialogView<DialogsDialogViewModel> {

    private final ShellView<?> shell;

    private final ListView<DialogItem> listView = new ListView<>();

    private final StackPane wrapper = new StackPane(listView);

    public DialogsDialogView(ShellView<?> shell, DialogsDialogViewModel viewModel) {
        super(viewModel);
        this.shell = shell;
        setDialogManager(shell.getDialogManager());
    }

    @Override
    protected void build(DialogsDialogViewModel viewModel) {
        super.build(viewModel);
        listView.getStyleClass().add(Styles.DENSE);
        listView.setItems(viewModel.getItems());
        listView.setCellFactory(lv -> new ListCell<DialogItem>() {
            @Override
            protected void updateItem(DialogItem item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.getDialog().toString());
                }
            }
        });
        wrapper.setPadding(new Insets(SizeConstants.INSET, SizeConstants.INSET, 0, SizeConstants.INSET));
        wrapper.getStyleClass().add(Styles.BORDERED);
        VBox.setVgrow(wrapper, Priority.ALWAYS);
        getButtonBox().getChildren().addAll(getCancelButton(), getOkButton());
        getContentPane().getChildren().addAll(wrapper, getButtonBox());
    }

    @Override
    protected void bind(DialogsDialogViewModel viewModel) {
        super.bind(viewModel);
        viewModel.itemWrapper().bind(listView.getSelectionModel().selectedItemProperty());
    }

    @Override
    protected void makeEqualButtons() {
        ButtonUtils.makeEqualWidthBySize(getCancelButton(), getOkButton());
    }

    @Override
    public void requestFocus() {
        listView.requestFocus();
    }
}
