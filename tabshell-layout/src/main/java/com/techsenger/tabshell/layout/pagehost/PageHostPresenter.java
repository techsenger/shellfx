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
import java.util.Collections;
import java.util.List;

/**
 *
 * @author Pavel Castornii
 */
public class PageHostPresenter<V extends PageHostView, C extends PageHostComposer>
        extends AbstractAreaPresenter<V, C> implements PageContainerPresenter<V, C>, PageHostPort {

    private List<PageBreadcrumb> breadcrumbs;

    private double dividerPosition;

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
    public PagePort getSelectedPage() {
        return getComposer().getSelectedPage();
    }

    @Override
    public void selectPage(ComponentName page) {
        getComposer().selectPage(page);
    }

    public void setDividerPosition(double pos) {
        this.dividerPosition = pos;
        getView().setDividerPosition(pos);
    }

    public double getDividerPosition() {
        return this.dividerPosition;
    }

    @Override
    public List<PageBreadcrumb> getBreadcrumbs() {
        return Collections.unmodifiableList(breadcrumbs);
    }

    protected void onDividerPositionChanged(double pos) {
        this.dividerPosition = pos;
    }

    /**
     * This method is called when the user clicks on a page menu item.
     *
     * @param pageName
     * @param breadcrumbs a list of breadcrumbs or an empty list
     */
    protected void onPageRequested(ComponentName pageName, List<PageBreadcrumb> breadcrumbs) {
        var currentPage = getComposer().getSelectedPage();
        if (currentPage != null) {
            if (currentPage.getDescriptor().getName() == pageName) {
                return;
            }
            currentPage.setSelected(false);
        }
        this.breadcrumbs = breadcrumbs;
        getView().setBreadcrumbs(breadcrumbs);
        getComposer().selectPage(pageName);
        currentPage = getComposer().getSelectedPage();
        currentPage.setSelected(true);
    }

    @Override
    protected void saveAppearance() {
        super.saveAppearance();
        var history = getHistory();
        history.setDividerPosition(getDividerPosition());
    }

    @Override
    protected void restoreAppearance() {
        super.restoreAppearance();
        var history = getHistory();
        setDividerPosition(history.getDividerPosition());
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
