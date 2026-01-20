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

import com.techsenger.tabshell.core.area.AbstractAreaPresenter;
import com.techsenger.tabshell.core.area.AreaComposer;
import com.techsenger.tabshell.core.history.HistoryUtils;
import java.util.ArrayList;

/**
 *
 * @author Pavel Castornii
 */
public abstract class AbstractFindPanelPresenter<V extends FindPanelView, C extends AreaComposer>
        extends AbstractAreaPresenter<V, C> {

    public AbstractFindPanelPresenter(V view) {
        super(view);
    }


    @Override
    protected void restoreAppearance() {
        super.restoreAppearance();
        var h = getHistory();
        var v = getView();
        v.setMatchCaseSelected(h.getMatchCaseButton().isSelected());
    }

    @Override
    protected void saveAppearance() {
        super.saveAppearance();
        var h = getHistory();
        var v = getView();
        h.getMatchCaseButton().setSelected(v.isMatchCaseSelected());
    }

    @Override
    protected void restoreData() {
        super.restoreData();
        var h = getHistory();
        var v = getView();
        v.setFindTexts(h.getFindTexts());
    }

    @Override
    protected void saveData() {
        super.saveData();
        var h = getHistory();
        var v = getView();
        h.setFindTexts(new ArrayList<>(v.getFindTexts()));
    }

    protected void saveFindTextToHistory() {
        var texts = getView().getFindTexts();
        HistoryUtils.addFirst(texts, getView().getFindText());
        getView().setFindTexts(texts);
    }

    @Override
    protected FindPanelHistory getHistory() {
        return (FindPanelHistory) super.getHistory();
    }

    protected abstract void handleResetMatches();

}
