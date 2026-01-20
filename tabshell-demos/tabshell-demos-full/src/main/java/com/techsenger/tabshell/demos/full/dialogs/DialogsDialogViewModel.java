///*
// * Copyright 2024-2026 Pavel Castornii.
// *
// * Licensed under the Apache License, Version 2.0 (the "License");
// * you may not use this file except in compliance with the License.
// * You may obtain a copy of the License at
// *
// *     http://www.apache.org/licenses/LICENSE-2.0
// *
// * Unless required by applicable law or agreed to in writing, software
// * distributed under the License is distributed on an "AS IS" BASIS,
// * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// * See the License for the specific language governing permissions and
// * limitations under the License.
// */
//
//package com.techsenger.tabshell.demos.full.dialogs;
//
//import com.techsenger.tabshell.core.CloseCheckResult;
//import com.techsenger.tabshell.core.ClosePreparationResult;
//import com.techsenger.tabshell.core.dialog.DialogScope;
//import com.techsenger.tabshell.dialogs.alert.AlertDialogType;
//import com.techsenger.tabshell.dialogs.alert.AlertDialogViewModel;
//import com.techsenger.tabshell.dialogs.file.FileChooserDialogViewModel;
//import com.techsenger.tabshell.dialogs.file.FileChooserType;
//import com.techsenger.tabshell.dialogs.simple.AbstractSimpleDialogViewModel;
//import com.techsenger.tabshell.dialogs.yesno.YesNoDialogViewModel;
//import java.util.function.Consumer;
//import javafx.beans.property.ReadOnlyObjectProperty;
//import javafx.beans.property.ReadOnlyObjectWrapper;
//import javafx.collections.FXCollections;
//import javafx.collections.ObservableList;
//
///**
// *
// * @author Pavel Castornii
// */
//public class DialogsDialogViewModel extends AbstractSimpleDialogViewModel<DialogsDialogMediator> {
//
//    private final ObservableList<DialogItem> items = FXCollections.observableArrayList(
//            new DialogItem(Dialog.INFO, () -> showInfoDialog()),
//            new DialogItem(Dialog.WARNING, () -> showWarningDialog()),
//            new DialogItem(Dialog.ERROR, () -> showErrorDialog()),
//            new DialogItem(Dialog.YES_NO, () -> showYesNoDialog()),
//            new DialogItem(Dialog.OPEN_FILE, () -> showOpenFileDialog()),
//            new DialogItem(Dialog.SAVE_FILE, () -> showSaveFileDialog()));
//
//
//    private final ReadOnlyObjectWrapper<DialogItem> item = new ReadOnlyObjectWrapper<>();
//
//    public DialogsDialogViewModel() {
//        super(DialogScope.SHELL, true);
//        setPrefWidth(600);
//        setPrefHeight(300);
//        setTitle("Dialogs");
//        setButtonWidthEqual(true);
//        getOk().setText("Show");
//        getOk().setAction(() -> {
//            var item = getItem();
//            if (item != null) {
//                item.getRunnable().run();
//            }
//        });
//    }
//
//    @Override
//    public CloseCheckResult isReadyToClose() {
//        return CloseCheckResult.READY;
//    }
//
//    @Override
//    public void prepareToClose(Consumer<ClosePreparationResult> resultCallback) {
//        throw new UnsupportedOperationException("Not supported yet.");
//    }
//
//    ObservableList<DialogItem> getItems() {
//        return items;
//    }
//
//    ReadOnlyObjectWrapper<DialogItem> itemWrapper() {
//        return item;
//    }
//
//    private DialogItem getItem() {
//        return item.get();
//    }
//
//    private ReadOnlyObjectProperty<DialogItem> itemProperty() {
//        return item.getReadOnlyProperty();
//    }
//
//    private void setItem(DialogItem newItem) {
//        item.set(newItem);
//    }
//
//    private void showInfoDialog() {
//        var viewModel = new AlertDialogViewModel(DialogScope.SHELL, AlertDialogType.INFO,
//                "All done! Time for coffee.");
//        viewModel.setPrefWidth(400);
//        getMediator().addAlertDialog(viewModel);
//    }
//
//    private void showWarningDialog() {
//        var viewModel = new AlertDialogViewModel(DialogScope.SHELL, AlertDialogType.WARNING,
//                "Attention! You shouldn't do it!");
//        viewModel.setPrefWidth(400);
//        getMediator().addAlertDialog(viewModel);
//    }
//
//    private void showErrorDialog() {
//        var viewModel = new AlertDialogViewModel(DialogScope.SHELL, AlertDialogType.ERROR,
//                "Oops! That didn’t work.");
//        viewModel.setPrefWidth(400);
//        getMediator().addAlertDialog(viewModel);
//    }
//
//    private void showYesNoDialog() {
//        var viewModel = new YesNoDialogViewModel(DialogScope.SHELL,
//                "Are you really sure?");
//        viewModel.setTitle("Confirm");
//        viewModel.setPrefWidth(400);
//        getMediator().addYesNoDialog(viewModel);
//    }
//
//    private void showOpenFileDialog() {
//        var shell = getMediator().getShell();
//        var viewModel = new FileChooserDialogViewModel(DialogScope.SHELL, FileChooserType.OPEN,
//                shell.getSettings().getAppearance(), shell.getHistoryManager());
//        viewModel.setPrefWidth(700);
//        getMediator().addFileChooserDialog(viewModel);
//    }
//
//    private void showSaveFileDialog() {
//        var shell = getMediator().getShell();
//        var viewModel = new FileChooserDialogViewModel(DialogScope.SHELL, FileChooserType.SAVE_AS,
//                shell.getSettings().getAppearance(), shell.getHistoryManager());
//        viewModel.setPrefWidth(700);
//        getMediator().addFileChooserDialog(viewModel);
//    }
//
//}
