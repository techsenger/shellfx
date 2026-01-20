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

package com.techsenger.tabshell.terminal.find;

import com.techsenger.jeditermfx.ui.FindResult;
import com.techsenger.tabshell.shared.find.AbstractFullFindPanelFxView;
import com.techsenger.tabshell.terminal.area.TabJediTermFxWidget;
import com.techsenger.toolkit.fx.value.ValueUtils;
import javafx.scene.control.ComboBox;
import javafx.scene.input.KeyCode;

/**
 *
 * @author Pavel Castornii
 */
public class FindPanelFxView<P extends FindPanelPresenter<?, ?>> extends AbstractFullFindPanelFxView<P>
        implements FindPanelView {

    private final TabJediTermFxWidget widget;

    public FindPanelFxView(TabJediTermFxWidget widget) {
        this.widget = widget;
    }

    @Override
    public void setResult(FindResult result) {
        this.widget.getTerminalPanel().setFindResult(result);
    }

    @Override
    public FindResult getResult() {
        return this.widget.getTerminalPanel().getFindResult();
    }

    @Override
    public void selectNextMatch() {
        widget.getTerminalPanel().selectNextFindMatch();
    }

    @Override
    public void selectPrevMatch() {
        widget.getTerminalPanel().selectPrevFindMatch();
    }



    @Override
    protected ComboBox<String> getFindComboBox() {
        return super.getFindComboBox();
    }

    @Override
    protected void addListeners() {
        super.addListeners();
        ValueUtils.callAndAddListener(getHighlightButton().selectedProperty(), (ov, oldV, newV) -> {
            widget.getTerminalPanel().setFindMatchHighlighted(newV);
            widget.getTerminalPanel().repaint();
        });
    }

    @Override
    protected void addHandlers() {
        super.addHandlers();
        getFindComboBox().setOnKeyPressed((event) -> {
            if (event.getCode() == KeyCode.ENTER) {
                getPresenter().handleTextInput(getFindComboBox().getEditor().getText());
            }
        });
    }

    @Override
    protected void unbuild() {
        super.unbuild();
        widget.getTerminalPanel().setFindResult(null);
    }
}
