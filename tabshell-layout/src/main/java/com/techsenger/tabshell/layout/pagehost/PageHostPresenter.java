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

package com.techsenger.tabshell.layout.pagehost;

import com.techsenger.patternfx.core.ComponentName;
import com.techsenger.patternfx.core.HistoryPolicy;
import com.techsenger.patternfx.core.HistoryProvider;
import com.techsenger.patternfx.mvp.Descriptor;
import com.techsenger.tabshell.core.area.AbstractAreaPresenter;
import com.techsenger.tabshell.core.page.PageContainerPresenter;
import com.techsenger.tabshell.core.page.PagePort;
import com.techsenger.tabshell.layout.LayoutComponents;

/**
 *
 * @author Pavel Castornii
 */
public class PageHostPresenter<V extends PageHostView, C extends PageHostComposer>
        extends AbstractAreaPresenter<V, C> implements PageContainerPresenter<V, C> {

    protected class Port extends AbstractAreaPresenter.Port implements PageHostPort {

        @Override
        public PagePort getSelectedPage() {
            return getComposer().getSelectedPage();
        }

        @Override
        public void selectPage(ComponentName page) {
            getComposer().selectPage(page);
        }
    }

    public PageHostPresenter(V view, HistoryProvider<PageHostHistory> historyProvider) {
        super(view);
        setHistoryPolicy(HistoryPolicy.APPEARANCE);
        setHistoryProvider(historyProvider);
    }

    @Override
    protected Descriptor createDescriptor() {
        return new Descriptor(LayoutComponents.PAGE_HOST);
    }

    @Override
    public PageHostPort getPort() {
        return (PageHostPort) super.getPort();
    }

    @Override
    protected Port createPort() {
        return new PageHostPresenter.Port();
    }

    protected void onPageSelected(Page page) {
        getComposer().selectPage(page.getName());
    }

    @Override
    protected void saveAppearance() {
        super.saveAppearance();
        var history = getHistory();
        var view = getView();
        history.setDividerPosition(view.getDividerPosition());
    }

    @Override
    protected void restoreAppearance() {
        super.restoreAppearance();
        var history = getHistory();
        var view = getView();
        view.setDividerPosition(history.getDividerPosition());
    }

    @Override
    protected PageHostHistory getHistory() {
        return (PageHostHistory) super.getHistory();
    }

    @Override
    protected void postInitialize() {
        super.postInitialize();
        var history = getHistory();
        if (history.isNew()) {
            getView().setDividerPosition(0.2);
        }
    }


}
