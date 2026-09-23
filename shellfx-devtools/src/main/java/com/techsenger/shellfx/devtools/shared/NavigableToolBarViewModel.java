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

package com.techsenger.shellfx.devtools.shared;

import com.techsenger.patternfx.mvvm.ChildComposer;
import com.techsenger.shellfx.shared.find.AbstractNavigableFindViewModel;
import com.techsenger.shellfx.shared.find.FindTrigger;
import com.techsenger.shellfx.shared.find.NavigableFindResult;
import com.techsenger.shellfx.shared.find.TextMatcherFactory;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Matcher;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

/**
 * Like {@link ToolBarViewModel}, but for a find toolbar that can move between individual matches.
 *
 * @author Pavel Castornii
 */
public class NavigableToolBarViewModel<C extends ChildComposer>
        extends AbstractNavigableFindViewModel<C, NavigableFindResult> implements FullNavigableToolBarPort {

    private final StringProperty findPrompt = new SimpleStringProperty();

    private final NavigableToolBarAwarePort toolBarAware;

    public NavigableToolBarViewModel(NavigableToolBarParams params) {
        super(params, FindTrigger.ON_TYPE);
        this.findPrompt.set(params.getFindPrompt());
        this.toolBarAware = params.getToolBarAware();
        matchCaseSelectedProperty().addListener((obs, oldV, newV) -> {
            setNotFound(false);
            this.toolBarAware.onMatchCase(newV);
            runFind();
        });
    }

    @Override
    public String getFindPrompt() {
        return findPrompt.get();
    }

    @Override
    public StringProperty findPromptProperty() {
        return findPrompt;
    }

    @Override
    public void setFindPrompt(String prompt) {
        this.findPrompt.set(prompt);
    }

    @Override
    public Matcher createFindMatcher() {
        String text = getEditedFindText();
        if (text == null || text.isBlank()) {
            return null;
        }
        return TextMatcherFactory.create(text, isMatchCaseSelected());
    }

    @Override
    protected CompletableFuture<NavigableFindResult> onFind() {
        return this.toolBarAware.onFind();
    }

    @Override
    protected void onFindCleared() {
        this.toolBarAware.onFindCleared();
    }

    protected void onRefresh() {
        this.toolBarAware.onRefresh();
        runFind();
    }

    protected NavigableToolBarAwarePort getToolBarAware() {
        return toolBarAware;
    }
}
