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
import com.techsenger.shellfx.core.dialog.AbstractDialogFxView;
import com.techsenger.shellfx.core.dialog.DialogFxView;
import com.techsenger.shellfx.core.dialog.DialogParams;
import com.techsenger.shellfx.core.dialog.DialogPort;
import com.techsenger.shellfx.core.window.WindowType;
import com.techsenger.shellfx.demo.page.PageDialogFxView;
import com.techsenger.shellfx.demo.page.PageDialogParams;
import com.techsenger.shellfx.demo.page.PageDialogPresenter;
import com.techsenger.shellfx.dialogs.alert.AlertDialogFxView;
import com.techsenger.shellfx.dialogs.alert.AlertDialogParams;
import com.techsenger.shellfx.dialogs.alert.AlertDialogPort;
import com.techsenger.shellfx.dialogs.alert.AlertDialogPresenter;
import com.techsenger.shellfx.dialogs.file.FileChooserDialogFxView;
import com.techsenger.shellfx.dialogs.file.FileChooserDialogParams;
import com.techsenger.shellfx.dialogs.file.FileChooserDialogPort;
import com.techsenger.shellfx.dialogs.file.FileChooserDialogPresenter;
import com.techsenger.shellfx.dialogs.namevalue.NameValueDialogFxView;
import com.techsenger.shellfx.dialogs.namevalue.NameValueDialogPort;
import com.techsenger.shellfx.dialogs.namevalue.NameValueDialogPresenter;
import com.techsenger.shellfx.material.button.ResultButton;
import com.techsenger.shellfx.material.button.ResultButtonName;
import java.util.List;
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
public class DialogsDialogFxView extends AbstractDialogFxView<DialogsDialogPresenter>
        implements DialogsDialogView {

    public class Composer extends AbstractDialogFxView<DialogsDialogPresenter>.Composer
            implements DialogsDialogView.Composer {

        @Override
        public AlertDialogPort openAlertDialog(AlertDialogParams params) {
            var view = new AlertDialogFxView<>();
            var presenter = new AlertDialogPresenter<>(view, params);
            presenter.initialize();
            showDialog(view);
            return presenter;
        }

        @Override
        public NameValueDialogPort openNameValueDialog(DialogParams params) {
            var view = new NameValueDialogFxView<>();
            var presenter = new NameValueDialogPresenter<>(view, params);
            presenter.initialize();
            showDialog(view);
            return presenter;
        }

        @Override
        public FileChooserDialogPort openFileChooserDialog(FileChooserDialogParams params) {
            var view = new FileChooserDialogFxView<>();
            var presenter = new FileChooserDialogPresenter<>(view, params);
            presenter.initialize();
            showDialog(view);
            return presenter;
        }

        @Override
        public DialogPort openPagedDialog(PageDialogParams params) {
            var view = new PageDialogFxView();
            var presenter = new PageDialogPresenter(view, params);
            presenter.initialize();
            showDialog(view);
            return presenter;
        }

        private void showDialog(DialogFxView<?> dialog) {
            if (dialog.getPresenter().getWindowType() == WindowType.NESTED) {
                getContainer().getComposer().addDialog(dialog);
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

    public DialogsDialogFxView() {
        super();
    }

    @Override
    public void setDialogTypes(List<DialogType> types) {
        this.listView.setItems(FXCollections.observableArrayList(types));
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
    public void setRightButtons(ResultButtonName... names) {
        super.setRightButtons(names);
        makeEqualWidth(getRightButtons(true));
    }

    @Override
    protected Composer createComposer() {
        return new DialogsDialogFxView.Composer();
    }

    @Override
    protected void build() {
        super.build();
        listView.setCellFactory(lv -> new ListCell<DialogType>() {

            {
                setOnMouseClicked(event -> {
                    if (event.getClickCount() == 2 && !isEmpty()) {
                        getPresenter().onDialogClick(getItem());
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
                .addListener((ov, oldV, newV) -> getPresenter().onWindowTypeSelected(newV));
    }

    @Override
    protected void initialize() {
        super.initialize();
        windowTypeComboBox.getSelectionModel().select(0);
    }
}
