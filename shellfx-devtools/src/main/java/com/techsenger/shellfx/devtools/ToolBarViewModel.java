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

package com.techsenger.shellfx.devtools;

import com.techsenger.patternfx.mvvm.ChildComposer;
import com.techsenger.shellfx.shared.find.AbstractFindBaseViewModel;
import com.techsenger.shellfx.shared.find.FindNavigationAwarePort;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author Pavel Castornii
 */
public class ToolBarViewModel<C extends ChildComposer> extends AbstractFindBaseViewModel<C> implements FindToolBarPort {

    private final ToolBarAwarePort toolBarAware;

    public ToolBarViewModel(ToolBarParams params) {
        super(params);
        this.toolBarAware = params.getToolBarAware();
        matchCaseSelectedProperty().addListener((obs, oldV, newV) -> {
            setNotFound(false);
            this.toolBarAware.onMatchCase(newV);
        });
    }

    @Override
    public Matcher createFindMatcher() {
        String text = getFindText();
        if (text == null || text.isBlank()) {
            return null;
        }

        int flags = isMatchCaseSelected() ? 0 : Pattern.CASE_INSENSITIVE;
        String patternText = Pattern.quote(text);
        return Pattern.compile(patternText, flags).matcher("");
    }

    @Override
    public void hideFindResultInfo() {
        super.hideFindResultInfo();
    }

    @Override
    public void showFindResultInfo(int currentMatch, int totalMatches) {
        super.showFindResultInfo(currentMatch, totalMatches);
    }

    @Override
    public void showFindResultInfo(int totalMatches) {
        super.showFindResultInfo(totalMatches);
    }

    @Override
    protected void onFind() {
        this.toolBarAware.onFind();
    }

    @Override
    protected void onFindCleared() {
        this.toolBarAware.onFindCleared();
    }

    @Override
    protected void onFindPrevious() {
        super.onFindPrevious();
        if (this.toolBarAware instanceof FindNavigationAwarePort p) {
            p.onFindPrevious();
        }
    }

    @Override
    protected void onFindNext() {
        super.onFindNext();
        if (this.toolBarAware instanceof FindNavigationAwarePort p) {
            p.onFindNext();
        }
    }

    protected void onRefresh() {
        this.toolBarAware.onRefresh();
    }

    protected ToolBarAwarePort getToolBarAware() {
        return toolBarAware;
    }
}
