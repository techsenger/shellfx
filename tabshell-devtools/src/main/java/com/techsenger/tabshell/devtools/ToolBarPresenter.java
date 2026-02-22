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

import com.techsenger.patternfx.mvp.Descriptor;
import com.techsenger.tabshell.core.area.AreaComposer;
import com.techsenger.tabshell.shared.find.AbstractFindBasePresenter;
import com.techsenger.tabshell.shared.find.FindFeature;
import com.techsenger.tabshell.shared.find.FindNavigationAwarePort;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author Pavel Castornii
 */
public class ToolBarPresenter<V extends ToolBarView, C extends AreaComposer> extends AbstractFindBasePresenter<V, C> {

    protected class Port extends AbstractFindBasePresenter<V, C>.Port implements ToolBarPort {

        private final ToolBarPresenter<V, C> presenter = ToolBarPresenter.this;

        @Override
        public void showFindResultInfo(int totalMatches) {
            presenter.showFindResultInfo(totalMatches);
        }

        @Override
        public void showFindResultInfo(int currentMatch, int totalMatches) {
            presenter.showFindResultInfo(currentMatch, totalMatches);
        }

        @Override
        public void hideFindResultInfo() {
            presenter.hideFindResultInfo();
        }

        @Override
        public Matcher createFindMatcher() {
            var v = presenter.getView();
            if (v.getFindText() == null || v.getFindText().isBlank()) {
                return null;
            }

            String text = v.getFindText();
            int flags = v.isMatchCaseSelected() ? 0 : Pattern.CASE_INSENSITIVE;
            String patternText = Pattern.quote(text);
            return Pattern.compile(patternText, flags).matcher("");
        }
    }

    private final ToolBarAwarePort toolBarAware;

    public ToolBarPresenter(V view, ToolBarAwarePort toolBarAware, FindFeature... features) {
        super(view, features);
        this.toolBarAware = toolBarAware;
    }

    @Override
    protected void handleFindTextEdited(String text) {
        super.handleFindTextEdited(text);
        getView().setNotFound(false);
    }

    @Override
    protected void handleFind() {
        this.toolBarAware.onFind();
    }

    @Override
    protected void handleFindCleared() {
        this.toolBarAware.onFindCleared();
    }

    @Override
    protected void handleFindPrevious() {
        super.handleFindPrevious();
        if (this.toolBarAware instanceof FindNavigationAwarePort p) {
            p.onFindPrevious();
        }
    }

    @Override
    protected void handleFindNext() {
        super.handleFindNext();
        if (this.toolBarAware instanceof FindNavigationAwarePort p) {
            p.onFindNext();
        }
    }

    @Override
    protected void handleMatchCase() {
        super.handleMatchCase();
        getView().setNotFound(false);
        this.toolBarAware.onMatchCase(getView().isMatchCaseSelected());
    }

    protected void handleRefresh() {
        this.toolBarAware.onRefresh();
    }

    @Override
    protected Descriptor createDescriptor() {
        return new Descriptor(DevToolsComponents.TOOL_BAR);
    }

    @Override
    public Port getPort() {
        return (Port) super.getPort();
    }

    @Override
    protected Port createPort() {
        return new ToolBarPresenter.Port();
    }

    protected ToolBarAwarePort getToolBarAware() {
        return toolBarAware;
    }
}
