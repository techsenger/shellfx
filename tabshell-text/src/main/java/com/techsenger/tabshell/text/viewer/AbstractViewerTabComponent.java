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

package com.techsenger.tabshell.text.viewer;

import com.techsenger.patternfx.core.HistoryPolicy;
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
            var c = new GoToLineDialogComponent<>(view);
            c.initialize();
            // todo: refactor after fixing JDK-8333275
            viewModel.getOk().setAction(() -> {
                try {
                    viewModel.setHistoryPolicy(HistoryPolicy.DATA); //before closing
                    viewModel.requestClose();
                    var line = viewModel.getLine();
                    if (line == null) {
                        line = 0;
                    }
                    if (line > 0) {
                        line--;
                    }
                    var column = viewModel.getColumn();
                    if (column == null) {
                        column = 0;
                    }
                    if (column > 0) {
                        column--;
                    }
                    tabView.getTextArea().moveTo(line, column);
                    tabView.getTextArea().requestFollowCaret();
                    tabView.getTextArea().requestFocus();
                    logger.debug("Moved caret to line: {}, column: {}", line, column);
                } catch (Exception ex) {
                    logger.error("e", ex);
                }
            });
            addDialog(c);
        }

        @Override
        public void removeFindPane() {
            component.removeFindPane();
            getModifiableChildren().remove(findPane);
        }

        @Override
        public void addFindPane(DefaultFindPaneViewModel viewModel) {
            var shell = component.getShell();
            var v = new DefaultFindPaneView<>(viewModel, getView().getTextArea());
            findPane = new DefaultFindPaneComponent<>(v);
            findPane.initialize();
            getModifiableChildren().add(findPane);
            v.getNode().addEventHandler(KeyEvent.KEY_PRESSED, (e) -> {
                if (e.getCode() == KeyCode.ESCAPE) {
                    getView().getViewModel().removeFindPane();
                }
            });
            if (!viewModel.replaceModeProperty().get()) {
                v.setSelectionToFindText();
            }
            getView().addFindPane(v);
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

    private DefaultFindPaneComponent<?> findPane;

    public AbstractViewerTabComponent(T view, ShellComponent<?> shell) {
        super(view, shell);
    }

    @Override
    protected abstract Mediator createMediator();

    private void removeFindPane() {
        getView().removeFindPane();
    }
}
