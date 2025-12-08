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

import com.techsenger.mvvm4fx.core.HistoryPolicy;
import com.techsenger.tabshell.dialogs.DialogShellTabComposer;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Pavel Castornii
 */
public class ViewerTabComposer<T extends AbstractViewerTabView<?>> extends DialogShellTabComposer<T> {

    private static final Logger logger = LoggerFactory.getLogger(ViewerTabComposer.class);

    protected class Mediator extends DialogShellTabComposer.Mediator implements ViewerTabMediator {

        @Override
        public void openGoToLineDialog(GoToLineDialogViewModel viewModel) {
            ViewerTabComposer.this.openGoToLineDialog(viewModel);
        }

        @Override
        public void removeFindPane() {
            ViewerTabComposer.this.removeFindPane();
        }

        @Override
        public void addFindPane(DefaultFindPaneViewModel viewModel) {
            ViewerTabComposer.this.addFindPane(viewModel);
        }

    }

    public ViewerTabComposer(T view) {
        super(view);
    }

    @Override
    public ViewerTabMediator createMediator() {
        return new ViewerTabComposer.Mediator();
    }

    private void openGoToLineDialog(GoToLineDialogViewModel viewModel) {
        // todo:
        var tabView = getView();
        var view = new GoToLineDialogView(viewModel);
        view.initialize();
        viewModel.okActionProperty().set(() -> {
            try {
                viewModel.getDescriptor().setHistoryPolicy(HistoryPolicy.DATA); //before closing
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
        tabView.getDialogManager().openDialog(view);
    }

    private void addFindPane(DefaultFindPaneViewModel viewModel) {
        var view = new DefaultFindPaneView(getView().getTextArea(), viewModel);
        view.initialize();
        view.getNode().addEventHandler(KeyEvent.KEY_PRESSED, (e) -> {
            if (e.getCode() == KeyCode.ESCAPE) {
                getView().getViewModel().removeFindPane();
            }
        });
        if (!viewModel.replaceModeProperty().get()) {
            view.setSelectionToFindText();
        }
        getView().addFindPane(view);
    }

    private void removeFindPane() {
        getView().removeFindPane();
    }
}
