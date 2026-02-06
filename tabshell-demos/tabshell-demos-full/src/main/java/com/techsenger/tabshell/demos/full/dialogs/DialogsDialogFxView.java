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

package com.techsenger.tabshell.demos.full.dialogs;

import atlantafx.base.theme.Styles;
import com.techsenger.tabshell.core.dialog.AbstractDialogFxView;
import com.techsenger.tabshell.core.dialog.DialogPort;
import com.techsenger.tabshell.core.history.HistoryManager;
import com.techsenger.tabshell.core.popup.OverlayScope;
import com.techsenger.tabshell.core.settings.AppearanceSettings;
import com.techsenger.tabshell.dialogs.alert.AlertDialogFxView;
import com.techsenger.tabshell.dialogs.alert.AlertDialogPresenter;
import com.techsenger.tabshell.dialogs.alert.AlertDialogType;
import com.techsenger.tabshell.dialogs.file.FileChooserDialogFxView;
import com.techsenger.tabshell.dialogs.file.FileChooserDialogPort;
import com.techsenger.tabshell.dialogs.file.FileChooserDialogPresenter;
import com.techsenger.tabshell.dialogs.file.FileChooserType;
import com.techsenger.tabshell.dialogs.namevalue.NameValueDialogFxView;
import com.techsenger.tabshell.dialogs.namevalue.NameValueDialogPort;
import com.techsenger.tabshell.dialogs.namevalue.NameValueDialogPresenter;
import com.techsenger.tabshell.material.button.ResultButton;
import java.util.List;
import javafx.collections.FXCollections;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

/**
 *
 * @author Pavel Castornii
 */
public class DialogsDialogFxView extends AbstractDialogFxView<DialogsDialogPresenter> implements DialogsDialogView {

    public class Composer extends AbstractDialogFxView.Composer implements DialogsDialogComposer {

        @Override
        public DialogPort addAlertDialog(AlertDialogType type, String message) {
            var view = new AlertDialogFxView<>(false);
            var presenter = new AlertDialogPresenter<>(view, OverlayScope.SHELL, type, message);
            presenter.initialize();
            getContainer().getComposer().addDialog(view);
            return presenter.getPort();
        }

        @Override
        public NameValueDialogPort addNameValueDialog() {
            var view = new NameValueDialogFxView<>(true);
            var presenter = new NameValueDialogPresenter<>(view, OverlayScope.SHELL);
            presenter.initialize();
            getContainer().getComposer().addDialog(view);
            return presenter.getPort();
        }

        @Override
        public FileChooserDialogPort addFileChooserDialog(FileChooserType type, AppearanceSettings settings,
                HistoryManager manager) {
            var view = new FileChooserDialogFxView<>(true);
            var presenter = new FileChooserDialogPresenter<>(view, OverlayScope.SHELL, type, settings, manager);
            presenter.initialize();
            getContainer().getComposer().addDialog(view);
            return presenter.getPort();
        }
    }

    private final ListView<DialogType> listView = new ListView<>();

    private final StackPane wrapper = new StackPane(listView);

    private final ResultButton closeButton = new ResultButton(DialogsDialogButtons.CLOSE, "Close");

    public DialogsDialogFxView() {
        super(true);
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
    protected Composer createComposer() {
        return new DialogsDialogFxView.Composer();
    }

    @Override
    protected void build() {
        super.build();
        listView.getStyleClass().add(Styles.DENSE);
        listView.setCellFactory(lv -> new ListCell<DialogType>() {

            {
                setOnMouseClicked(event -> {
                    if (event.getClickCount() == 2 && !isEmpty()) {
                        getPresenter().handleDialogClick(getItem());
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
        getContentBox().getChildren().add(wrapper);
        registerButtons(closeButton);
        addRightButtons(DialogsDialogButtons.CLOSE);
    }
}
