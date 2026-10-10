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

import com.techsenger.annotations.Nullable;
import com.techsenger.shellfx.core.close.CloseCheckResult;
import com.techsenger.shellfx.core.close.ClosePreparationResult;
import com.techsenger.shellfx.core.config.ConfigManager;
import com.techsenger.shellfx.core.dialog.AbstractDialogViewModel;
import com.techsenger.shellfx.core.dialog.DialogParams;
import com.techsenger.shellfx.core.settings.AppearanceSettings;
import com.techsenger.shellfx.core.window.WindowType;
import com.techsenger.shellfx.demo.page.PageDialogConfig;
import com.techsenger.shellfx.demo.page.PageDialogParams;
import com.techsenger.shellfx.demo.page.PageMenuType;
import com.techsenger.shellfx.dialogs.alert.AlertDialogConfig;
import com.techsenger.shellfx.dialogs.alert.AlertDialogParams;
import com.techsenger.shellfx.dialogs.alert.AlertDialogType;
import com.techsenger.shellfx.dialogs.file.FileChooserDialogButtons;
import com.techsenger.shellfx.dialogs.file.FileChooserDialogConfig;
import com.techsenger.shellfx.dialogs.file.FileChooserDialogParams;
import com.techsenger.shellfx.dialogs.file.FileChooserType;
import com.techsenger.shellfx.dialogs.namevalue.NameValueButtons;
import com.techsenger.shellfx.dialogs.progress.ProgressDialogConfig;
import com.techsenger.shellfx.dialogs.progress.ProgressDialogParams;
import com.techsenger.shellfx.dialogs.text.LongTextDialogConfig;
import com.techsenger.shellfx.dialogs.text.LongTextDialogParams;
import com.techsenger.shellfx.dialogs.text.TextChoiceDialogConfig;
import com.techsenger.shellfx.dialogs.text.TextChoiceDialogParams;
import com.techsenger.shellfx.dialogs.text.TextDialogConfig;
import com.techsenger.shellfx.dialogs.text.TextDialogParams;
import com.techsenger.shellfx.dialogs.text.TextsDialogConfig;
import com.techsenger.shellfx.dialogs.text.TextsDialogParams;
import com.techsenger.shellfx.material.theme.Theme;
import com.techsenger.shellfx.storage.DefaultStorageFile;
import com.techsenger.shellfx.storage.FileStorage;
import com.techsenger.shellfx.storage.FileStyle;
import com.techsenger.shellfx.storage.FileStyleResolver;
import com.techsenger.shellfx.storage.StorageFile;
import com.techsenger.shellfx.storage.UnixFileStorage;
import com.techsenger.shellfx.storage.WindowsFileStorage;
import com.techsenger.toolkit.core.os.OsUtils;
import com.techsenger.toolkit.fx.color.ColorUtils;
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

    private static @Nullable FileStyle resolveDemoStyle(Theme theme, StorageFile file) {
        if (file.isRoot()) {
            return file.getStorage().createRootStyle(theme);
        }
        var palette = theme.getPalette();
        var iconColor = palette.getDefaultFgColor();
        var textColor = palette.getDefaultFgColor();
        if (file.isDirectory()) {
            if (theme.isDark()) {
                iconColor = palette.getWarning2Color();
            } else {
                iconColor = palette.getWarning6Color();
            }
        }
        if (file.isHidden()) {
            iconColor = ColorUtils.toArgb(180, iconColor);
            textColor = ColorUtils.toArgb(180, textColor);
        }
        return new FileStyle(iconStyle(iconColor), textStyle(textColor));
    }

    private static String iconStyle(int color) {
        return "-fx-fill: " + ColorUtils.toCssRgba(color) + ";";
    }

    private static String textStyle(int color) {
        return "-fx-text-fill: " + ColorUtils.toCssRgba(color) + ";";
    }

    private final ObjectProperty<WindowType> selectedWindowType = new SimpleObjectProperty<>();

    private final AppearanceSettings settings;

    private final ConfigManager configManager;

    private final List<? extends FileStorage<StorageFile>> storages;

    private final Map<DialogType, Runnable> dialogActionsByType = Map.ofEntries(
            Map.entry(DialogType.INFO, () -> {
                var params = new AlertDialogParams(new AlertDialogConfig(), selectedWindowType.get(),
                        getAppearanceSettings(), AlertDialogType.INFO);
                var dialog = getComposer().openAlertDialog(params);
                dialog.setMessage("All done! Time for coffee.");
            }),
            Map.entry(DialogType.WARNING, () -> {
                var params = new AlertDialogParams(new AlertDialogConfig(), selectedWindowType.get(),
                        getAppearanceSettings(), AlertDialogType.WARNING);
                var dialog = getComposer().openAlertDialog(params);
                dialog.setMessage("Attention! You shouldn't do it!");
            }),
            Map.entry(DialogType.ERROR, () -> {
                var params = new AlertDialogParams(new AlertDialogConfig(), selectedWindowType.get(),
                        getAppearanceSettings(), AlertDialogType.ERROR);
                var dialog = getComposer().openAlertDialog(params);
                dialog.setMessage("Oops! That didn’t work.\nTwice!");
            }),
            Map.entry(DialogType.YES_NO, () -> {
                var params = new AlertDialogParams(new AlertDialogConfig(), selectedWindowType.get(),
                        getAppearanceSettings(), AlertDialogType.CONFIRMATION);
                var dialog = getComposer().openAlertDialog(params);
                dialog.setMessage("Are you really sure?");
            }),
            Map.entry(DialogType.NAME_VALUE, () -> {
                var params = new DialogParams(null, selectedWindowType.get(), getSettings());
                var dialog = getComposer().openNameValueDialog(params);
                dialog.setTitle("Name & Value");
                dialog.setName("Some Name");
                dialog.setValue("Some Value");
                dialog.setRightButtons(NameValueButtons.OK);
            }),
            Map.entry(DialogType.TEXT, () -> {
                var params = new TextDialogParams(new TextDialogConfig(), selectedWindowType.get(),
                        getSettings());
                var dialog = getComposer().openTextDialog(params);
                dialog.setTitle("Text");
                dialog.setText("Some text");
            }),
            Map.entry(DialogType.TEXTS, () -> {
                var params = new TextsDialogParams(new TextsDialogConfig(), selectedWindowType.get(),
                        getSettings());
                var dialog = getComposer().openTextsDialog(params);
                dialog.setTitle("Texts");
                dialog.getTexts().setAll(List.of("First", "Second", "Third"));
                dialog.setEditable(false);
                dialog.setText("Second");
            }),
            Map.entry(DialogType.TEXT_CHOICE, () -> {
                var params = new TextChoiceDialogParams(new TextChoiceDialogConfig(), selectedWindowType.get(),
                        getSettings());
                var dialog = getComposer().openTextChoiceDialog(params);
                dialog.setTitle("Text Choice");
                dialog.getTexts().setAll(List.of("First", "Second", "Third"));
                dialog.setText("Second");
            }),
            Map.entry(DialogType.LONG_TEXT, () -> {
                var params = new LongTextDialogParams(new LongTextDialogConfig(), selectedWindowType.get(),
                        getSettings());
                var dialog = getComposer().openLongTextDialog(params);
                dialog.setTitle("Long Text");
                dialog.setText("First line\nSecond line");
            }),
            Map.entry(DialogType.PROGRESS, () -> {
                var params = new ProgressDialogParams(new ProgressDialogConfig(), selectedWindowType.get(),
                        getSettings());
                var dialog = getComposer().openProgressDialog(params);
                dialog.setMessage("I am working...");
                dialog.setStepsVisible(true);
                dialog.setStepCount(10);
                dialog.setCurrentStep(3);
                dialog.setProgress(0.3);
            }),
            Map.entry(DialogType.OPEN_FILE, () -> showFileChooserDialog(FileChooserType.OPEN, null)),
            Map.entry(DialogType.SAVE_FILE, () -> showFileChooserDialog(FileChooserType.SAVE_AS, null)),
            Map.entry(DialogType.OPEN_FILE_HIGHLIGHTED, () -> showFileChooserDialog(FileChooserType.OPEN,
                    (f) -> resolveDemoStyle(getSettings().getTheme(), f))),
            Map.entry(DialogType.PAGE, () -> showPagedDialog(PageMenuType.FLAT)),
            Map.entry(DialogType.TREE_PAGE, () -> showPagedDialog(PageMenuType.TREE))
    );

    public DialogsDialogViewModel(DialogsDialogParams params) {
        super(params);
        this.settings = params.getSettings();
        this.configManager = params.getManager();
        if (OsUtils.isWindows()) {
            this.storages = WindowsFileStorage.createSystemStorages(DefaultStorageFile::new);
        } else {
            this.storages = UnixFileStorage.createSystemStorages(DefaultStorageFile::new);
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

    private void showFileChooserDialog(FileChooserType type, @Nullable FileStyleResolver<StorageFile> resolver) {
        var config = configManager.getOrCreateConfig(FileChooserDialogConfig.class, FileChooserDialogConfig::new);
        var params = new FileChooserDialogParams<StorageFile>(config, selectedWindowType.get(), settings,
                type, this.storages);
        var dialog = getComposer().openFileChooserDialog(params);
        dialog.setStyleResolver(resolver);
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
        var config = configManager.getOrCreateConfig(PageDialogConfig.class, PageDialogConfig::new);
        var params = new PageDialogParams(config, selectedWindowType.get(), settings, menuType);
        getComposer().openPagedDialog(params);
    }
}
