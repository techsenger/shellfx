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

import com.techsenger.shellfx.core.close.CloseCheckResult;
import com.techsenger.shellfx.core.close.ClosePreparationResult;
import com.techsenger.shellfx.core.dialog.AbstractDialogViewModel;
import com.techsenger.shellfx.core.dialog.DialogParams;
import com.techsenger.shellfx.core.history.HistoryManager;
import com.techsenger.shellfx.core.settings.AppearanceSettings;
import com.techsenger.shellfx.core.window.WindowType;
import com.techsenger.shellfx.demo.page.PageDialogParams;
import com.techsenger.shellfx.demo.page.PageMenuType;
import com.techsenger.shellfx.dialogs.alert.AlertDialogParams;
import com.techsenger.shellfx.dialogs.alert.AlertDialogType;
import com.techsenger.shellfx.dialogs.file.FileChooserDialogButtons;
import com.techsenger.shellfx.dialogs.file.FileChooserDialogParams;
import com.techsenger.shellfx.dialogs.file.FileChooserType;
import com.techsenger.shellfx.dialogs.namevalue.NameValueButtons;
import com.techsenger.shellfx.storage.DefaultGenericFile;
import com.techsenger.shellfx.storage.FileStorage;
import com.techsenger.shellfx.storage.GenericFile;
import com.techsenger.shellfx.storage.UnixFileStorage;
import com.techsenger.shellfx.storage.WindowsFileStorage;
import com.techsenger.toolkit.core.os.OsUtils;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;

/**
 *
 * @author Pavel Castornii
 */
public class DialogsDialogViewModel<C extends DialogsDialogComposer> extends AbstractDialogViewModel<C> {

    private final ObjectProperty<WindowType> selectedWindowType = new SimpleObjectProperty<>();

    private final AppearanceSettings settings;

    private final HistoryManager historyManager;

    private final List<? extends FileStorage<GenericFile>> storages;

    private final Map<DialogType, Runnable> dialogActionsByType = Map.ofEntries(
            Map.entry(DialogType.INFO, () -> {
                var params = new AlertDialogParams(selectedWindowType.get(), getAppearanceSettings(),
                        AlertDialogType.INFO);
                var dialog = getComposer().openAlertDialog(params);
                dialog.setMessage("All done! Time for coffee.");
            }),
            Map.entry(DialogType.WARNING, () -> {
                var params = new AlertDialogParams(selectedWindowType.get(), getAppearanceSettings(),
                        AlertDialogType.WARNING);
                var dialog = getComposer().openAlertDialog(params);
                dialog.setMessage("Attention! You shouldn't do it!");
            }),
            Map.entry(DialogType.ERROR, () -> {
                var params = new AlertDialogParams(selectedWindowType.get(), getAppearanceSettings(),
                        AlertDialogType.ERROR);
                var dialog = getComposer().openAlertDialog(params);
                dialog.setMessage("Oops! That didn’t work.\nTwice!");
            }),
            Map.entry(DialogType.YES_NO, () -> {
                var params = new AlertDialogParams(selectedWindowType.get(), getAppearanceSettings(),
                        AlertDialogType.CONFIRMATION);
                var dialog = getComposer().openAlertDialog(params);
                dialog.setMessage("Are you really sure?");
            }),
            Map.entry(DialogType.NAME_VALUE, () -> {
                var params = new DialogParams(selectedWindowType.get(), getSettings());
                var dialog = getComposer().openNameValueDialog(params);
                dialog.setTitle("Name & Value");
                dialog.setName("Some Name");
                dialog.setValue("Some Value");
                dialog.setRightButtons(NameValueButtons.OK);
            }),
            Map.entry(DialogType.PROGRESS, () -> {
                var params = new DialogParams(selectedWindowType.get(), getSettings());
                var dialog = getComposer().openProgressDialog(params);
                dialog.setMessage("I am working...");
                dialog.setStepsVisible(true);
                dialog.setStepCount(10);
                dialog.setCurrentStep(3);
                dialog.setProgress(0.3);
            }),
            Map.entry(DialogType.OPEN_FILE, () -> showFileChooserDialog(FileChooserType.OPEN)),
            Map.entry(DialogType.SAVE_FILE, () -> showFileChooserDialog(FileChooserType.SAVE_AS)),
            Map.entry(DialogType.PAGE, () -> showPagedDialog(PageMenuType.FLAT)),
            Map.entry(DialogType.TREE_PAGE, () -> showPagedDialog(PageMenuType.TREE))
    );

    public DialogsDialogViewModel(DialogsDialogParams params) {
        super(params);
        this.settings = params.getSettings();
        this.historyManager = params.getManager();
        if (OsUtils.isWindows()) {
            this.storages = WindowsFileStorage.createSystemStorages(DefaultGenericFile::new);
        } else {
            this.storages = UnixFileStorage.createSystemStorages(DefaultGenericFile::new);
        }
    }

    @Override
    public CloseCheckResult isReadyToClose() {
        return CloseCheckResult.READY;
    }

    @Override
    public void prepareToClose(Consumer<ClosePreparationResult> resultCallback) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public ObjectProperty<WindowType> selectedWindowTypeProperty() {
        return selectedWindowType;
    }

    @Override
    protected void postInitialize() {
        super.postInitialize();
        setTitle("Dialogs");
        setOnResult((result) -> closeSafely());
        setRightButtons(DialogsDialogButtons.CLOSE);
    }

    protected void onDialogClick(DialogType type) {
        var action = this.dialogActionsByType.get(type);
        action.run();
    }

    protected AppearanceSettings getSettings() {
        return settings;
    }

    private void showFileChooserDialog(FileChooserType type) {
        var params = new FileChooserDialogParams<GenericFile>(selectedWindowType.get(), settings,
                type, this.storages, historyManager);
        var dialog = getComposer().openFileChooserDialog(params);
        dialog.setOnResult((buttonName) -> {
            if (buttonName == FileChooserDialogButtons.OK) {
                var result = dialog.getResult();
                System.out.println("Result: " + result.getUri());
                dialog.closeSafely();
            } else {
                dialog.closeSafely();
            }
        });
    }

    private void showPagedDialog(PageMenuType menuType) {
        var params = new PageDialogParams(selectedWindowType.get(), settings, menuType, historyManager);
        getComposer().openPagedDialog(params);
    }
}
