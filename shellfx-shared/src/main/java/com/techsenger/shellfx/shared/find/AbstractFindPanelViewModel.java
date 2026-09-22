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

package com.techsenger.shellfx.shared.find;

import com.techsenger.patternfx.mvvm.ChildComposer;
import com.techsenger.shellfx.core.area.AreaParams;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;

/**
 *
 * @author Pavel Castornii
 */
public abstract class AbstractFindPanelViewModel<C extends ChildComposer> extends AbstractFindBaseViewModel<C>
        implements FullFindPanelPort {

    private final BooleanProperty wholeWordSelected = new SimpleBooleanProperty();

    private final BooleanProperty wholeWordDisabled = new SimpleBooleanProperty();

    private final BooleanProperty regExpSelected = new SimpleBooleanProperty();

    private final BooleanProperty regExpDisabled = new SimpleBooleanProperty();

    private final BooleanProperty highlightSelected = new SimpleBooleanProperty();

    private final BooleanProperty highlightDisabled = new SimpleBooleanProperty();

    private Runnable onCloseRequest;

    public AbstractFindPanelViewModel(AreaParams params) {
        super(params);
    }

    @Override
    public boolean isWholeWordSelected() {
        return wholeWordSelected.get();
    }

    @Override
    public void setWholeWordSelected(boolean wholeWordSelected) {
        this.wholeWordSelected.set(wholeWordSelected);
    }

    @Override
    public BooleanProperty wholeWordSelectedProperty() {
        return wholeWordSelected;
    }

    @Override
    public boolean isWholeWordDisabled() {
        return wholeWordDisabled.get();
    }

    @Override
    public void setWholeWordDisabled(boolean wholeWordDisabled) {
        this.wholeWordDisabled.set(wholeWordDisabled);
    }

    @Override
    public BooleanProperty wholeWordDisabledProperty() {
        return wholeWordDisabled;
    }

    @Override
    public boolean isRegExpSelected() {
        return regExpSelected.get();
    }

    @Override
    public void setRegExpSelected(boolean regExpSelected) {
        this.regExpSelected.set(regExpSelected);
    }

    @Override
    public BooleanProperty regExpSelectedProperty() {
        return regExpSelected;
    }

    @Override
    public boolean isRegExpDisabled() {
        return regExpDisabled.get();
    }

    @Override
    public void setRegExpDisabled(boolean regExpDisabled) {
        this.regExpDisabled.set(regExpDisabled);
    }

    @Override
    public BooleanProperty regExpDisabledProperty() {
        return regExpDisabled;
    }

    @Override
    public boolean isHighlightSelected() {
        return highlightSelected.get();
    }

    @Override
    public void setHighlightSelected(boolean highlightSelected) {
        this.highlightSelected.set(highlightSelected);
    }

    @Override
    public BooleanProperty highlightSelectedProperty() {
        return highlightSelected;
    }

    @Override
    public boolean isHighlightDisabled() {
        return highlightDisabled.get();
    }

    @Override
    public void setHighlightDisabled(boolean highlightDisabled) {
        this.highlightDisabled.set(highlightDisabled);
    }

    @Override
    public BooleanProperty highlightDisabledProperty() {
        return highlightDisabled;
    }

    @Override
    public Runnable getOnCloseRequest() {
        return this.onCloseRequest;
    }

    @Override
    public void setOnCloseRequest(Runnable runnable) {
        this.onCloseRequest = runnable;
    }

    protected void onWholeWord() {

    }

    protected void onRegExp() {

    }

    protected void onHighlight() {

    }

    @Override
    protected void restorePersistentState() {
        super.restorePersistentState();
        var h = getHistory();
        setWholeWordSelected(h.isWholeWordSelected());
        setRegExpSelected(h.isRegExpSelected());
        setHighlightSelected(h.isHighlightSelected());
    }

    @Override
    protected void savePersistentState() {
        super.savePersistentState();
        var h = getHistory();
        h.setWholeWordSelected(isWholeWordSelected());
        h.setRegExpSelected(isRegExpSelected());
        h.setHighlightSelected(isHighlightSelected());
    }

    @Override
    protected FindPanelHistory getHistory() {
        return (FindPanelHistory) super.getHistory();
    }

    protected void onCloseRequest() {
        if (this.onCloseRequest != null) {
            this.onCloseRequest.run();
        }
    }
}
