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

package com.techsenger.tabshell.demo.dialogs;

import com.techsenger.patternfx.mvp.ComponentDescriptor;
import com.techsenger.tabshell.core.CloseCheckResult;
import com.techsenger.tabshell.core.ClosePreparationResult;
import com.techsenger.tabshell.core.dialog.AbstractDialogPresenter;
import com.techsenger.tabshell.core.history.HistoryManager;
import com.techsenger.tabshell.core.settings.AppearanceSettings;
import com.techsenger.tabshell.demo.DemoComponents;
import com.techsenger.tabshell.demo.page.PageDialogParams;
import com.techsenger.tabshell.demo.page.PageMenuType;
import com.techsenger.tabshell.dialogs.alert.AlertDialogParams;
import com.techsenger.tabshell.dialogs.alert.AlertDialogType;
import com.techsenger.tabshell.dialogs.file.FileChooserDialogParams;
import com.techsenger.tabshell.dialogs.file.FileChooserType;
import com.techsenger.tabshell.dialogs.namevalue.NameValueButtons;
import com.techsenger.tabshell.storage.DefaultFileStorageRegistry;
import com.techsenger.tabshell.storage.FileStorageRegistry;
import java.util.Arrays;
import java.util.Map;
import java.util.function.Consumer;
import com.techsenger.tabshell.dialogs.file.FileChooserDialogButtons;

/**
 *
 * @author Pavel Castornii
 */
public class DialogsDialogPresenter extends AbstractDialogPresenter<DialogsDialogView> {

    private final Map<DialogType, Runnable> dialogActionsByType = Map.ofEntries(
            Map.entry(DialogType.INFO, () -> {
                var params = new AlertDialogParams(AlertDialogType.INFO);
                var dialog = getView().getComposer().addAlertDialog(params, "All done! Time for coffee.");
            }),
            Map.entry(DialogType.WARNING, () -> {
                var params = new AlertDialogParams(AlertDialogType.WARNING);
                var dialog = getView().getComposer().addAlertDialog(params, "Attention! You shouldn't do it!");
            }),
            Map.entry(DialogType.ERROR, () -> {
                var params = new AlertDialogParams(AlertDialogType.ERROR);
                var dialog = getView().getComposer().addAlertDialog(params, "Oops! That didn’t work.\nTwice!");
            }),
            Map.entry(DialogType.YES_NO, () -> {
                var params = new AlertDialogParams(AlertDialogType.CONFIRMATION);
                var dialog = getView().getComposer().addAlertDialog(params, "Are you really sure?");
            }),
            Map.entry(DialogType.NAME_VALUE, () -> {
                var dialog = getView().getComposer().addNameValueDialog();
                dialog.setTitle("Name & Value");
                dialog.setName("Some Name");
                dialog.setValue("Some Value");
                dialog.setRightButtons(NameValueButtons.OK);
            }),
            Map.entry(DialogType.OPEN_FILE, () -> showFileChooserDialog(FileChooserType.OPEN)),
            Map.entry(DialogType.SAVE_FILE, () -> showFileChooserDialog(FileChooserType.SAVE_AS)),
            Map.entry(DialogType.PAGE, () -> showPagedDialog(PageMenuType.FLAT)),
            Map.entry(DialogType.TREE_PAGE, () -> showPagedDialog(PageMenuType.TREE))
    );

    private final AppearanceSettings settings;

    private final HistoryManager historyManager;

    private final FileStorageRegistry storageRegistry = new DefaultFileStorageRegistry();

    public DialogsDialogPresenter(DialogsDialogView view, DialogsDialogParams params) {
        super(view, params);
        this.settings = params.getSettings();
        this.historyManager = params.getManager();
    }

    @Override
    protected ComponentDescriptor createDescriptor() {
        return new ComponentDescriptor(DemoComponents.DIALOGS_DIALOG);
    }

    @Override
    public CloseCheckResult isReadyToClose() {
        return CloseCheckResult.READY;
    }

    @Override
    public void prepareToClose(Consumer<ClosePreparationResult> resultCallback) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    protected void postInitialize() {
        super.postInitialize();
        var view = getView();
        setResizable(true);
        setTitle("Dialogs");
        view.setDialogTypes(Arrays.asList(DialogType.values()));
        setResultAction((result) -> {
            requestClose();
        });
        setRightButtons(DialogsDialogButtons.CLOSE);
        setMinWidth(400);
        setMinHeight(200);
    }

    @Override
    protected void applyAppearance() {
        super.applyAppearance();
        setPrefWidth(600);
        setPrefHeight(300);
    }

    protected void onDialogClick(DialogType type) {
        var action = this.dialogActionsByType.get(type);
        action.run();
    }

    private void showFileChooserDialog(FileChooserType type) {
        this.storageRegistry.refreshDefaultStorages();
        var params = new FileChooserDialogParams(type, this.storageRegistry.getAllStorages(), settings, historyManager);
        var port = getView().getComposer().addFileChooserDialog(params);
        port.setResultAction((buttonName) -> {
            if (buttonName == FileChooserDialogButtons.OK) {
                var result = port.getResult();
                System.out.println("Result: " + result.getUri());
                port.requestClose();
            } else {
                port.requestClose();
            }
        });
    }

    private void showPagedDialog(PageMenuType menuType) {
        var params = new PageDialogParams(menuType, historyManager);
        getView().getComposer().addPagedDialog(params);
    }
}
