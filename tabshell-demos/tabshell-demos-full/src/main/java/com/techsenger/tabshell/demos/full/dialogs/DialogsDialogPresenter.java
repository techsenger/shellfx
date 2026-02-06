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

import com.techsenger.patternfx.mvp.Descriptor;
import com.techsenger.tabshell.core.CloseCheckResult;
import com.techsenger.tabshell.core.ClosePreparationResult;
import com.techsenger.tabshell.core.dialog.AbstractDialogPresenter;
import com.techsenger.tabshell.core.history.HistoryManager;
import com.techsenger.tabshell.core.popup.OverlayScope;
import com.techsenger.tabshell.core.settings.AppearanceSettings;
import com.techsenger.tabshell.demos.full.DemoComponents;
import com.techsenger.tabshell.dialogs.alert.AlertDialogType;
import com.techsenger.tabshell.dialogs.file.FileChooserButtons;
import com.techsenger.tabshell.dialogs.file.FileChooserType;
import java.util.Arrays;
import java.util.Map;
import java.util.function.Consumer;

/**
 *
 * @author Pavel Castornii
 */
public class DialogsDialogPresenter extends AbstractDialogPresenter<DialogsDialogView, DialogsDialogComposer> {

    private final Map<DialogType, Runnable> dialogActionsByType = Map.ofEntries(
            Map.entry(DialogType.INFO, () -> {
                var dialog = getComposer().addAlertDialog(AlertDialogType.INFO, "All done! Time for coffee.");
            }),
            Map.entry(DialogType.WARNING, () -> {
                var dialog = getComposer().addAlertDialog(AlertDialogType.WARNING, "Attention! You shouldn't do it!");
            }),
            Map.entry(DialogType.ERROR, () -> {
                var dialog = getComposer().addAlertDialog(AlertDialogType.ERROR, "Oops! That didn’t work.");
            }),
            Map.entry(DialogType.YES_NO, () -> {
                var dialog = getComposer().addAlertDialog(AlertDialogType.CONFIRMATION, "Are you really sure?");
            }),
            Map.entry(DialogType.NAME_VALUE, () -> {
                var dialog = getComposer().addNameValueDialog();
                dialog.setTitle("Name & Value");
                dialog.setName("Some Name");
                dialog.setValue("Some Value");
                dialog.setCancelVisible(false);
            }),
            Map.entry(DialogType.OPEN_FILE, () -> showFileChooserDialog(FileChooserType.OPEN)),
            Map.entry(DialogType.SAVE_FILE, () -> showFileChooserDialog(FileChooserType.SAVE_AS))
    );

    private final AppearanceSettings settings;

    private final HistoryManager historyManager;

    public DialogsDialogPresenter(DialogsDialogView view, AppearanceSettings settings, HistoryManager manager) {
        super(view, OverlayScope.SHELL);
        this.settings = settings;
        this.historyManager = manager;
    }

    @Override
    protected Descriptor createDescriptor() {
        return new Descriptor(DemoComponents.DEMO_DIALOGS_DIALOG);
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
        view.setPrefWidth(600);
        view.setPrefHeight(300);
        view.setTitle("Dialogs");
        view.setButtonWidthEqual(true);
        view.setDialogTypes(Arrays.asList(DialogType.values()));
        setResultAction((result) -> {
            requestClose();
        });
    }

    protected void handleDialogClick(DialogType type) {
        var action = this.dialogActionsByType.get(type);
        action.run();
    }

    private void showFileChooserDialog(FileChooserType type) {
        var port = getComposer().addFileChooserDialog(type, settings, historyManager);
        port.setResultAction((buttonName) -> {
            if (buttonName == FileChooserButtons.OK) {
                var result = port.getResult();
                System.out.println("Result: " + result.getUri());
                port.requestClose();
            } else {
                port.requestClose();
            }
        });
    }
}
