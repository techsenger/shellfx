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

import com.techsenger.patternfx.core.HistoryPolicy;
import com.techsenger.patternfx.core.HistoryProvider;
import com.techsenger.patternfx.mvp.Descriptor;
import com.techsenger.tabshell.core.area.AbstractAreaPresenter;
import com.techsenger.tabshell.core.page.PageContainerPresenter;
import com.techsenger.tabshell.core.page.PageItem;
import com.techsenger.tabshell.core.page.PagePort;
import com.techsenger.tabshell.layout.LayoutComponents;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 *
 * @author Pavel Castornii
 */
public class PageHostPresenter<V extends PageHostView, C extends PageHostComposer>
        extends AbstractAreaPresenter<V, C> implements PageContainerPresenter<V, C>, PageHostPort {

    private static List<PageBreadcrumb> getBreadcrumbs(PageItem<?> item) {
        List<PageBreadcrumb> breadcrumbs = new ArrayList<>();
        if (item == null) {
            return breadcrumbs;
        }
        PageItem<?> current = item;
        DefaultPageBreadcrumb previous = null;
        while (current != null) {
            if (current.getText() != null) { // it can be not shown root
                var breadcrumb = new DefaultPageBreadcrumb(current);
                breadcrumbs.add(breadcrumb);
                if (previous != null) {
                    previous.setPrevious(breadcrumb);
                }
                previous = breadcrumb;
            }
            current = current.getParent();
        }
        Collections.reverse(breadcrumbs);
        return breadcrumbs;
    }

    private static List<PageBreadcrumb> getBreadcrumbs(PageBreadcrumb breadcrumb) {
        List<PageBreadcrumb> breadcrumbs = new ArrayList<>();
        PageBreadcrumb current = breadcrumb;
        while (current != null) {
            if (current.getItem().getText() != null) { // it can be not shown root
                breadcrumbs.add(current);
            }
            current = current.getPrevious();
        }
        Collections.reverse(breadcrumbs);
        return breadcrumbs;
    }

    private PageItem<?> rootItem;

    private List<PageBreadcrumb> breadcrumbs;

    private double dividerPosition;

    private boolean forwardDisabled;

    private boolean backDisabled;

    private final List<PageItem<?>> pageHistory = new ArrayList<>();

    private int pageHistoryIndex;

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
    public void selectPage(PageItem<?> item) {
        Objects.requireNonNull(item, "Item can't be null");
        if (!isCurrentPage(item)) {
            var breadcrumbs = getBreadcrumbs(item);
            selectPage(item, breadcrumbs);
            addPageHistory(item);
        }
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

    public boolean isForwardDisabled() {
        return forwardDisabled;
    }

    public boolean isBackDisabled() {
        return backDisabled;
    }

    /**
     * Returns an unmodifiable list of history entries.
     * @return
     */
    public List<PageItem<?>> getPageHistory() {
        return Collections.unmodifiableList(pageHistory);
    }

    public int getPageHistoryIndex() {
        return pageHistoryIndex;
    }

    protected PageItem<?> getRootItem() {
        return rootItem;
    }

    protected void setForwardDisabled(boolean forwardDisabled) {
        this.forwardDisabled = forwardDisabled;
        getView().setForwardDisabled(forwardDisabled);
    }

    protected void setBackDisabled(boolean backDisabled) {
        this.backDisabled = backDisabled;
        getView().setBackDisabled(backDisabled);
    }

    protected void onDividerPositionChanged(double pos) {
        this.dividerPosition = pos;
    }

    protected void onPageRequested(PageItem<?> item) {
        if (!isCurrentPage(item)) {
            var breadcrumbs = getBreadcrumbs(item);
            selectPage(item, breadcrumbs);
            addPageHistory(item);
        }
    }

    protected void onPageRequested(PageBreadcrumb breadcrumb) {
        var item = breadcrumb.getItem();
        if (!isCurrentPage(item)) {
            var breadcrumbs = getBreadcrumbs(breadcrumb);
            selectPage(item, breadcrumbs);
            addPageHistory(item);
        }
    }

    protected void onBack() {
        if (this.pageHistoryIndex > 0) {
            var newIndex = pageHistoryIndex - 1;
            navigateHistory(newIndex);
        }
    }

    protected void onForward() {
        if (this.pageHistoryIndex + 1 < this.pageHistory.size()) {
            var newIndex = pageHistoryIndex + 1;
            navigateHistory(newIndex);
        }
    }

    protected boolean isCurrentPage(PageItem<?> item) {
        var currentPage = getComposer().getSelectedPage();
        return currentPage != null && Objects.equals(currentPage.getItem(), item);
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
        setBackDisabled(true);
        setForwardDisabled(true);
    }

    void setRootItem(PageItem<?> rootItem) {
        this.rootItem = rootItem;
    }

    private void navigateHistory(int newIndex) {
        var page = this.pageHistory.get(newIndex);
        var breadcrumbs = getBreadcrumbs(page);
        selectPage(page, breadcrumbs);
        setPageHistoryIndex(newIndex);
    }

    private void selectPage(PageItem<?> item, List<PageBreadcrumb> breadcrumbs) {
        var currentPage = getComposer().getSelectedPage();
        if (currentPage != null) {
            currentPage.setSelected(false);
        }
        getComposer().providePage(item);
        this.breadcrumbs = breadcrumbs;
        getView().setBreadcrumbs(breadcrumbs);
        getView().showPage(item);
        currentPage = getComposer().getSelectedPage();
        currentPage.setSelected(true);
    }

    private void addPageHistory(PageItem<?> item) {
        if (this.pageHistoryIndex + 1 < this.pageHistory.size()) {
            this.pageHistory.subList(this.pageHistoryIndex + 1, this.pageHistory.size()).clear();
        }
        this.pageHistory.add(item);
        setPageHistoryIndex(this.pageHistory.size() - 1);
    }

    private void setPageHistoryIndex(int index) {
        this.pageHistoryIndex = index;
        setBackDisabled(index == 0);
        setForwardDisabled(index + 1 == this.pageHistory.size());
    }
}
