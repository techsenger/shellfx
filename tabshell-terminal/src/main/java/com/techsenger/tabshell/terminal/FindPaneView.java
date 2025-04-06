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

package com.techsenger.tabshell.terminal;

import com.techsenger.tabshell.core.find.AbstractFindPaneView;
import javafx.scene.control.ComboBox;
import javafx.scene.input.KeyCode;

/**
 *
 * @author Pavel Castornii
 */
public class FindPaneView extends AbstractFindPaneView<FindPaneViewModel> {

    private final KitJediTermFxWidget widget;

    public FindPaneView(KitJediTermFxWidget widget, FindPaneViewModel viewModel) {
        super(viewModel);
        this.widget = widget;
        viewModel.setTextBuffer(this.widget.getTerminalTextBuffer());
    }

    @Override
    protected ComboBox<String> getFindComboBox() {
        return super.getFindComboBox();
    }

    @Override
    protected void bind(FindPaneViewModel viewModel) {
        super.bind(viewModel);
        widget.getTerminalPanel().findResultHighlightedProperty().bind(viewModel.highlightSelectedProperty());
    }

    @Override
    protected void addListeners(FindPaneViewModel viewModel) {
        super.addListeners(viewModel);
        viewModel.resultProperty().addListener((ov, oldV, newV) -> this.widget.getTerminalPanel().setFindResult(newV));
        getHighlightButton().selectedProperty().addListener((ov, oldV, newV) -> widget.getTerminalPanel().repaint());
    }

    @Override
    protected void addHandlers(FindPaneViewModel viewModel) {
        super.addHandlers(viewModel);
        getFindComboBox().setOnKeyPressed((event) -> {
            if (event.getCode() == KeyCode.ENTER) {
                if (viewModel.resultProperty().get() == null) {
                    viewModel.find();
                } else {
                    widget.getTerminalPanel().selectNextFindResultItem();
                    viewModel.updateResultText();
                }
            }
        });
        getFindPreviousButton().setOnAction(e -> {
            widget.getTerminalPanel().selectPrevFindResultItem();
            viewModel.updateResultText();
        });
        getFindNextButton().setOnAction(e -> {
            widget.getTerminalPanel().selectNextFindResultItem();
            viewModel.updateResultText();
        });
    }

    @Override
    protected void preDeinitialize(FindPaneViewModel viewModel) {
        super.preDeinitialize(viewModel);
        widget.getTerminalPanel().setFindResult(null);
    }

    @Override
    protected void unbind(FindPaneViewModel viewModel) {
        super.unbind(viewModel);
        widget.getTerminalPanel().findResultHighlightedProperty().unbind();
    }
}
