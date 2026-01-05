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

package com.techsenger.tabshell.text.viewer;

import com.techsenger.tabshell.core.ShellComponent;
import com.techsenger.tabshell.dialogs.alert.AlertDialogComponent;
import com.techsenger.tabshell.dialogs.alert.AlertDialogView;
import com.techsenger.tabshell.dialogs.alert.AlertDialogViewModel;
import com.techsenger.tabshell.dialogs.file.FileChooserDialogComponent;
import com.techsenger.tabshell.dialogs.file.FileChooserDialogView;
import com.techsenger.tabshell.dialogs.file.FileChooserDialogViewModel;
import com.techsenger.tabshell.dialogs.yesno.YesNoDialogComponent;
import com.techsenger.tabshell.dialogs.yesno.YesNoDialogView;
import com.techsenger.tabshell.dialogs.yesno.YesNoDialogViewModel;
import com.techsenger.tabshell.layout.workertab.AbstractWorkerTabComponent;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Pavel Castornii
 */
public abstract class AbstractViewerTabComponent<T extends AbstractViewerTabView<?, ?>>
        extends AbstractWorkerTabComponent<T> {

    private static final Logger logger = LoggerFactory.getLogger(AbstractViewerTabComponent.class);

    protected class Mediator extends AbstractWorkerTabComponent.Mediator implements ViewerTabMediator {

        private final AbstractViewerTabComponent component = AbstractViewerTabComponent.this;

        @Override
        public void addGoToLineDialog(GoToLineDialogViewModel viewModel) {
            var tabView = getView();
            var shell = component.getShell();
            var view = new GoToLineDialogView<>(viewModel);
            var c = new GoToLineDialogComponent<>(view, component);
            c.initialize();
            addDialog(c);
        }

        @Override
        public void addFindPanel(DefaultFindPanelViewModel viewModel) {
            var shell = component.getShell();
            var v = new DefaultFindPanelView<>(viewModel, getView().getTextArea());
            findPanel = new DefaultFindPanelComponent<>(v);
            findPanel.initialize();
            getModifiableChildren().add(findPanel);
            v.getNode().addEventHandler(KeyEvent.KEY_PRESSED, (e) -> {
                if (e.getCode() == KeyCode.ESCAPE) {
                    getView().getViewModel().removeFindPanel();
                }
            });
            if (!viewModel.replaceModeProperty().get()) {
                v.setSelectionToFindText();
            }
            getView().addFindPanel(v);
        }

        @Override
        public void removeFindPanel() {
            getView().removeFindPanel();
            getModifiableChildren().remove(findPanel);
            findPanel.deinitialize();
        }

        @Override
        public void addAlertDialog(AlertDialogViewModel vm) {
            var v = new AlertDialogView<>(vm);
            var c = new AlertDialogComponent<>(v);
            c.initialize();
            addDialog(c);
        }

        @Override
        public void addYesNoDialog(YesNoDialogViewModel vm) {
            var v = new YesNoDialogView<>(vm);
            var c = new YesNoDialogComponent<>(v);
            c.initialize();
            addDialog(c);
        }

        @Override
        public void addFileChooserDialog(FileChooserDialogViewModel<?> viewModel) {
            var v = new FileChooserDialogView<>(viewModel);
            var shell = component.getShell();
            var c = new FileChooserDialogComponent<>(v, component);
            c.initialize();
            shell.addDialog(c);
        }
    }

    private DefaultFindPanelComponent<?> findPanel;

    public AbstractViewerTabComponent(T view, ShellComponent<?> shell) {
        super(view, shell);
    }

    @Override
    protected abstract Mediator createMediator();
}
