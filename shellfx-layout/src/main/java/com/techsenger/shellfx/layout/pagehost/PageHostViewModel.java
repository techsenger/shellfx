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

package com.techsenger.shellfx.layout.pagehost;

import com.techsenger.annotations.Unmodifiable;
import com.techsenger.shellfx.core.page.PageContainerViewModel;
import com.techsenger.shellfx.core.page.PageItem;
import com.techsenger.toolkit.fx.value.ObservableSource;
import com.techsenger.toolkit.fx.value.SimpleObservableSource;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

/**
 *
 * @author Pavel Castornii
 */
public class PageHostViewModel<C extends PageHostComposer> extends AbstractPageHostViewModel<C>
        implements PageContainerViewModel<C>, FullPageHostPort {

    static List<PageItem> match(List<PageItem> items, Matcher matcher, FindStatistics statistics) {
        List<PageItem> matchedItems = items.stream()
                .filter(i -> matcher.reset(i.getText()).find())
                .collect(Collectors.toList());
        statistics.setMatches(matchedItems.size());
        statistics.setTotal(items.size());
        return matchedItems;
    }

    private final ObservableList<PageItem> modifiableMenu = FXCollections.observableArrayList();

    private final ObservableList<PageItem> menu = FXCollections.unmodifiableObservableList(modifiableMenu);

    private final ObservableSource<Integer> selectPageSource = new SimpleObservableSource<>();

    private List<PageItem> items;

    private List<PageItem> matchedItems;

    public PageHostViewModel(PageHostParams params) {
        super(params);
    }

    @Override
    public PageHostPort.ComposerAccess getComposerAccess() {
        return getComposer();
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

    @Override
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
        modifiableMenu.setAll(matchedItems);
        if (!matchedItems.isEmpty()) {
            if (!isCurrentPage(matchedItems.get(0))) {
                doSelectPage(0);
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

        modifiableMenu.setAll(items);
        doSelectPage(this.items.indexOf(item)); // just to select item in the menu
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
        modifiableMenu.setAll(items);
    }

    @Override
    void navigateHistory(int newIndex) {
        var pageIndex = getPageHistory().get(newIndex);
        selectPage(pageIndex);
        setPageHistoryIndex(newIndex);
        updateHistoryNavigation();
    }

    @Unmodifiable ObservableList<PageItem> getMenu() {
        return menu;
    }

    ObservableSource<Integer> selectPageSource() {
        return selectPageSource;
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
        selectPageSource.next(index);
    }
}
