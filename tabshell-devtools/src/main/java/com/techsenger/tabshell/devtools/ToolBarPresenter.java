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

package com.techsenger.tabshell.devtools;

import com.techsenger.patternfx.mvp.ComponentDescriptor;
import com.techsenger.tabshell.shared.find.AbstractFindBasePresenter;
import com.techsenger.tabshell.shared.find.FindNavigationAwarePort;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author Pavel Castornii
 */
public class ToolBarPresenter<V extends ToolBarView> extends AbstractFindBasePresenter<V> implements ToolBarPort {

    private final ToolBarAwarePort toolBarAware;

    public ToolBarPresenter(V view, ToolBarParams params) {
        super(view, params);
        this.toolBarAware = params.getToolBarAware();
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
    protected void onFindTextChanged(String text) {
        super.onFindTextChanged(text);
        getView().setNotFound(false);
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

    @Override
    protected void onMatchCase(boolean selected) {
        super.onMatchCase(selected);
        getView().setNotFound(false);
        this.toolBarAware.onMatchCase(selected);
    }

    protected void onRefresh() {
        this.toolBarAware.onRefresh();
    }

    @Override
    protected ComponentDescriptor createDescriptor() {
        return new ComponentDescriptor(DevToolsComponents.TOOL_BAR);
    }

    protected ToolBarAwarePort getToolBarAware() {
        return toolBarAware;
    }
}
