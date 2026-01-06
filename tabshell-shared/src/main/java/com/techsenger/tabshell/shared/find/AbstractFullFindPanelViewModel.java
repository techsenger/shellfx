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

package com.techsenger.tabshell.shared.find;

import com.techsenger.tabshell.material.button.ToggleButtonViewModel;
import com.techsenger.tabshell.core.area.AreaMediator;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;

/**
 *
 * @author Pavel Castornii
 */
public abstract class AbstractFullFindPanelViewModel<T extends AreaMediator>
        extends AbstractNavigableFindPanelViewModel<T> {

    private final ToggleButtonViewModel wholeWord = new ToggleButtonViewModel();

    private final ToggleButtonViewModel regExp = new ToggleButtonViewModel();

    private final ToggleButtonViewModel highlight = new ToggleButtonViewModel();

    private final ObjectProperty<Runnable> closeAction = new SimpleObjectProperty<>();

    public AbstractFullFindPanelViewModel() {

    }

    public ToggleButtonViewModel getWholeWord() {
        return wholeWord;
    }

    public ToggleButtonViewModel getRegExp() {
        return regExp;
    }

    public ToggleButtonViewModel getHighlight() {
        return highlight;
    }

    public ObjectProperty<Runnable> closeActionProperty() {
        return closeAction;
    }

    public Runnable getCloseAction() {
        return closeAction.get();
    }

    public void setCloseAction(Runnable value) {
        this.closeAction.set(value);
    }

    @Override
    protected AbstractFullFindPanelHistory getHistory() {
        return (AbstractFullFindPanelHistory) super.getHistory();
    }

    @Override
    protected void restoreAppearance() {
        super.restoreAppearance();
        var h = getHistory();
        wholeWord.setSelected(h.getWholeWordButton().isSelected());
        regExp.setSelected(h.getRegExpButton().isSelected());
        highlight.setSelected(h.getHighlightButton().isSelected());
    }

    @Override
    protected void saveAppearance() {
        super.saveAppearance();
        var h = getHistory();
        h.getWholeWordButton().setSelected(wholeWord.isSelected());
        h.getRegExpButton().setSelected(regExp.isSelected());
        h.getHighlightButton().setSelected(highlight.isSelected());
    }
}
