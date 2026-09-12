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

package com.techsenger.shellfx.demo.dialogs;

import atlantafx.base.theme.Styles;
import com.techsenger.shellfx.core.dialog.AbstractDialogView;
import com.techsenger.shellfx.core.dialog.DialogParams;
import com.techsenger.shellfx.core.dialog.DialogPort;
import com.techsenger.shellfx.core.dialog.DialogView;
import com.techsenger.shellfx.core.window.AbstractWindowView;
import com.techsenger.shellfx.core.window.WindowType;
import com.techsenger.shellfx.demo.page.PageDialogParams;
import com.techsenger.shellfx.demo.page.PageDialogView;
import com.techsenger.shellfx.demo.page.PageDialogViewModel;
import com.techsenger.shellfx.dialogs.alert.AlertDialogParams;
import com.techsenger.shellfx.dialogs.alert.AlertDialogView;
import com.techsenger.shellfx.dialogs.alert.AlertDialogViewModel;
import com.techsenger.shellfx.dialogs.alert.FullAlertDialogPort;
import com.techsenger.shellfx.dialogs.file.FileChooserDialogParams;
import com.techsenger.shellfx.dialogs.file.FileChooserDialogView;
import com.techsenger.shellfx.dialogs.file.FileChooserDialogViewModel;
import com.techsenger.shellfx.dialogs.file.FullFileChooserDialogPort;
import com.techsenger.shellfx.dialogs.namevalue.FullNameValueDialogPort;
import com.techsenger.shellfx.dialogs.namevalue.NameValueDialogView;
import com.techsenger.shellfx.dialogs.namevalue.NameValueDialogViewModel;
import com.techsenger.shellfx.dialogs.progress.FullProgressDialogPort;
import com.techsenger.shellfx.dialogs.progress.ProgressDialogView;
import com.techsenger.shellfx.dialogs.progress.ProgressDialogViewModel;
import com.techsenger.shellfx.material.button.ResultButton;
import com.techsenger.shellfx.storage.GenericFile;
import java.util.Arrays;
import javafx.collections.FXCollections;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

/**
 *
 * @author Pavel Castornii
 */
public class DialogsDialogView<VM extends DialogsDialogViewModel<?>> extends AbstractDialogView<VM> {

    public class Composer extends AbstractWindowView<VM>.Composer implements DialogsDialogComposer {

        @Override
        public FullAlertDialogPort openAlertDialog(AlertDialogParams params) {
            var viewModel = new AlertDialogViewModel<>(params);
            var dialogView = new AlertDialogView<>(viewModel);
            dialogView.initialize();
            showDialog(dialogView);
            return viewModel;
        }

        @Override
        public FullNameValueDialogPort openNameValueDialog(DialogParams params) {
            var viewModel = new NameValueDialogViewModel<>(params);
            var dialogView = new NameValueDialogView<>(viewModel);
            dialogView.initialize();
            showDialog(dialogView);
            return viewModel;
        }

        @Override
        public FullProgressDialogPort openProgressDialog(DialogParams params) {
            var viewModel = new ProgressDialogViewModel<>(params);
            var dialogView = new ProgressDialogView<>(viewModel);
            dialogView.initialize();
            showDialog(dialogView);
            return viewModel;
        }

        @Override
        public FullFileChooserDialogPort<GenericFile> openFileChooserDialog(
                FileChooserDialogParams<GenericFile> params) {
            var viewModel = new FileChooserDialogViewModel<>(params);
            var dialogView = new FileChooserDialogView<>(viewModel);
            dialogView.initialize();
            showDialog(dialogView);
            return viewModel;
        }

        @Override
        public DialogPort openPagedDialog(PageDialogParams params) {
            var viewModel = new PageDialogViewModel<>(params);
            var dialogView = new PageDialogView<>(viewModel);
            dialogView.initialize();
            showDialog(dialogView);
            return viewModel;
        }

        private void showDialog(DialogView<?> dialog) {
            if (dialog.getViewModel().getWindowType() == WindowType.NESTED) {
                getParent().getComposer().addDialog(dialog);
            } else {
                dialog.getStage().initOwner(getNode().getScene().getWindow());
                dialog.getStage().show();
            }
            dialog.requestFocus();
        }
    }

    private final ListView<DialogType> listView = new ListView<>();

    private final VBox wrapper = new VBox(listView);

    private final ResultButton closeButton = new ResultButton(DialogsDialogButtons.CLOSE, "Close");

    private final ComboBox<WindowType> windowTypeComboBox =
            new ComboBox(FXCollections.observableArrayList(WindowType.values()));

    public DialogsDialogView(VM viewModel) {
        super(viewModel);
    }

    @Override
    public void requestFocus() {
        listView.requestFocus();
    }

    @Override
    public Composer getComposer() {
        return (Composer) super.getComposer();
    }

    @Override
    protected Composer createComposer() {
        return new DialogsDialogView.Composer();
    }

    @Override
    protected void build() {
        super.build();
        listView.setItems(FXCollections.observableArrayList(Arrays.asList(DialogType.values())));
        listView.setCellFactory(lv -> new ListCell<DialogType>() {

            {
                setOnMouseClicked(event -> {
                    if (event.getClickCount() == 2 && !isEmpty()) {
                        getViewModel().onDialogClick(getItem());
                    }
                });
            }

            @Override
            protected void updateItem(DialogType item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.toString());
                }
            }
        });
        wrapper.getStyleClass().add(Styles.BORDERED);
        VBox.setVgrow(wrapper, Priority.ALWAYS);
        VBox.setVgrow(listView, Priority.ALWAYS);
        getContentBox().getChildren().add(wrapper);
        getLeftBottomBox().getChildren().add(windowTypeComboBox);
        registerButtons(closeButton);
    }

    @Override
    protected void addListeners() {
        super.addListeners();
        windowTypeComboBox.getSelectionModel().selectedItemProperty()
                .addListener((ov, oldV, newV) -> getViewModel().selectedWindowTypeProperty().set(newV));
        windowTypeComboBox.getSelectionModel().select(0);
    }
}
