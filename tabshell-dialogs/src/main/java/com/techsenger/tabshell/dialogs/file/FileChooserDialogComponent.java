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

package com.techsenger.tabshell.dialogs.file;

import com.techsenger.patternfx.core.ComponentName;
import com.techsenger.patternfx.core.HistoryPolicy;
import com.techsenger.tabshell.core.dialog.DialogContainerComponent;
import com.techsenger.tabshell.core.history.HistoryManager;
import com.techsenger.tabshell.core.settings.AppearanceSettings;
import com.techsenger.tabshell.dialogs.DialogComponentNames;
import com.techsenger.tabshell.dialogs.alert.AlertDialogComponent;
import com.techsenger.tabshell.dialogs.alert.AlertDialogView;
import com.techsenger.tabshell.dialogs.alert.AlertDialogViewModel;
import com.techsenger.tabshell.dialogs.simple.AbstractSimpleDialogComponent;

/**
 *
 * @author Pavel Castornii
 */
public class FileChooserDialogComponent<T extends FileChooserDialogView<?, ?>>
        extends AbstractSimpleDialogComponent<T> {

    protected class Mediator extends AbstractSimpleDialogComponent.Mediator implements FileChooserDialogMediator {

        @Override
        public void addAlertDialog(AlertDialogViewModel vm) {
            var view = new AlertDialogView<>(vm);
            var component = new AlertDialogComponent<>(view);
            component.initialize();
            dialogContainer.addDialog(component);
        }
    }

    private final AppearanceSettings settings;

    private final DialogContainerComponent<?> dialogContainer;

    public FileChooserDialogComponent(T view, AppearanceSettings settings, HistoryManager historyManager,
            DialogContainerComponent<?> dc) {
        super(view);
        this.settings = settings;
        setHistoryPolicy(HistoryPolicy.APPEARANCE);
        setHistoryProvider(() -> historyManager.getOrCreateHistory(FileChooserDialogHistory.class,
                FileChooserDialogHistory::new));
        this.dialogContainer = dc;
    }

    @Override
    public ComponentName getName() {
        return DialogComponentNames.FILE_CHOOSER_DIALOG;
    }

    @Override
    protected Mediator createMediator() {
        return new Mediator();
    }

    AppearanceSettings getSettings() {
        return settings;
    }
}
