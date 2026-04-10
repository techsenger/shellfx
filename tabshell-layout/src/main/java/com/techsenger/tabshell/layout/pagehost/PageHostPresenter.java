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
import com.techsenger.tabshell.core.page.PageContainerPresenter;
import com.techsenger.tabshell.core.page.PageItem;
import com.techsenger.tabshell.core.page.PagePort;
import com.techsenger.tabshell.layout.LayoutComponents;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 *
 * @author Pavel Castornii
 */
public class PageHostPresenter<V extends PageHostView, C extends PageHostComposer>
        extends AbstractPageHostPresenter<V, C> implements PageContainerPresenter<V, C>,
        PageHostPort {

    static List<PageItem> match(List<PageItem> items, Matcher matcher, FindStatistics statistics) {
        List<PageItem> matchedItems = items.stream()
                .filter(i -> matcher.reset(i.getText()).find())
                .collect(Collectors.toList());
        statistics.setMatches(matchedItems.size());
        statistics.setTotal(items.size());
        return matchedItems;
    }

    private List<PageItem> items;

    private List<PageItem> matchedItems;


    public PageHostPresenter(V view, HistoryProvider<BasePageHostHistory> historyProvider) {
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
        return getComposer().getSelectedPagePort();
    }

    @Override
    public void selectPage(PageItem item) {
        Objects.requireNonNull(item, "Item can't be null");
        int index;
        if (isFindMode()) {
            index = this.matchedItems.indexOf(item);
        } else {
            index = this.items.indexOf(item);
        }
        if (index >= 0) {
            selectPage(item, index);
        }
    }

    public void selectPage(int index) {
        PageItem item;
        if (isFindMode()) {
            item = this.matchedItems.get(index);
        } else {
            item = this.items.get(index);
        }
        selectPage(item, index);
    }

    @Override
    public void onFind(String text) {
        setFindMode(true);
        updateHistoryNavigation();
        var matcher = Pattern.compile(Pattern.quote(text), Pattern.CASE_INSENSITIVE).matcher("");
        var statistics = new FindStatistics();
        this.matchedItems = match(items, matcher, statistics);
        var findPanel = getComposer().getFindPanelPort();
        findPanel.showFindResultInfo(statistics.getMatches());
        getView().setMenu(matchedItems);
        if (!matchedItems.isEmpty()) {
            if (!isCurrentPage(matchedItems.get(0))) {
                selectPage(0);
            }
        }
    }

    @Override
    public void onFindCleared() {
        var findPanel = getComposer().getFindPanelPort();
        findPanel.hideFindResultInfo();
        setFindMode(false);
        var item = getComposer().getSelectedPagePort().getItem();
        addPageHistory(item);
        updateHistoryNavigation();

        getView().setMenu(items);
        getView().setPage(this.items.indexOf(item)); // just to select item in the menu
    }

    protected List<PageItem> getItems() {
        return items;
    }

    protected void onPageRequested(int index) {
        if (index == -1) {
            return;
        }
        var item = this.items.get(index);
        if (!isCurrentPage(item)) {
            doSelectPage(index);
            addPageHistory(item);
            updateHistoryNavigation();
        }
    }

    void setPages(List<PageItem> items) {
        this.items = items;
        getView().setMenu(items);
    }

    @Override
    void navigateHistory(int newIndex) {
        var pageIndex = getPageHistory().get(newIndex);
        selectPage(pageIndex);
        setPageHistoryIndex(newIndex);
        updateHistoryNavigation();
    }

    private void selectPage(PageItem item, int index) {
        if (!isCurrentPage(item) && index >= 0) {
            if (isFindMode()) {
                doSelectPage(index);
            } else {
                doSelectPage(index);
                addPageHistory(item);
                updateHistoryNavigation();
            }
        }
    }

    private void doSelectPage(int index) {
        var currentPage = getComposer().getSelectedPagePort();
        if (currentPage != null) {
            currentPage.setSelected(false);
        }
        getComposer().providePagePort(index);
        getView().setPage(index);
        currentPage = getComposer().getSelectedPagePort();
        currentPage.setSelected(true);
        if (!isFindMode()) {
            currentPage.requestFocus();
        }
    }
}
