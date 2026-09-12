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
import com.techsenger.shellfx.core.page.TreePageContainerViewModel;
import com.techsenger.shellfx.core.page.TreePageItem;
import com.techsenger.toolkit.fx.value.ObservableSource;
import com.techsenger.toolkit.fx.value.SimpleObservableSource;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyBooleanWrapper;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

/**
 *
 * @author Pavel Castornii
 */
public class TreePageHostViewModel<C extends TreePageHostComposer> extends AbstractPageHostViewModel<C>
        implements TreePageContainerViewModel<C>, FullTreePageHostPort {

    static FilteredTreePageItem match(TreePageItem node, Matcher matcher, FindStatistics statistics) {
        List<FilteredTreePageItem> matchingChildren = node.getChildren().stream()
                .map(child -> match(child, matcher, statistics))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        FilteredTreePageItem filtered = null;
        statistics.incrementTotal();
        if (node.getText() == null) {
            if (!matchingChildren.isEmpty()) {
                filtered = new FilteredTreePageItem(node, false);
                filtered.getChildren().addAll(matchingChildren);
            }
        } else {
            boolean matches = matcher.reset(node.getText()).find();
            if (matches) {
                statistics.incrementMatches();
            }
            if (matches || !matchingChildren.isEmpty()) {
                filtered = new FilteredTreePageItem(node, matches);
                filtered.getChildren().addAll(matchingChildren);
            }
        }
        return filtered;
    }

    private static FilteredTreePageItem findFirstMatched(FilteredTreePageItem node) {
        if (node.isMatched()) {
            return node;
        }
        for (FilteredTreePageItem child : node.getChildren()) {
            FilteredTreePageItem found = findFirstMatched(child);
            if (found != null) {
                return found;
            }
        }

        return null;
    }

    private static List<PageBreadcrumb> getBreadcrumbs(TreePageItem item) {
        List<PageBreadcrumb> breadcrumbs = new ArrayList<>();
        if (item == null) {
            return breadcrumbs;
        }
        TreePageItem current = item;
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

    private final ObservableSource<TreePageItem> updateMenuSource = new SimpleObservableSource<>();

    private final ObservableSource<FilteredTreePageItem> refreshMenuSource = new SimpleObservableSource<>();

    private final ObservableSource<TreePageItem> selectPageSource = new SimpleObservableSource<>();

    private final ObservableList<PageBreadcrumb> modifiableBreadcrumbs = FXCollections.observableArrayList();

    private final ObservableList<PageBreadcrumb> breadcrumbs =
            FXCollections.unmodifiableObservableList(modifiableBreadcrumbs);

    private final ReadOnlyObjectWrapper<TreePageItem> rootItem = new ReadOnlyObjectWrapper<>();

    private final ReadOnlyBooleanWrapper showRoot = new ReadOnlyBooleanWrapper();

    public TreePageHostViewModel(TreePageHostParams params) {
        super(params);
    }

    @Override
    public TreePageHostPort.ComposerAccess getComposerAccess() {
        return getComposer();
    }

    @Override
    public void selectPage(TreePageItem item) {
        Objects.requireNonNull(item, "Item can't be null");
        if (!isCurrentPage(item)) {
            var breadcrumbs = getBreadcrumbs(item);
            selectPage(item, breadcrumbs);
            addPageHistory(item);
            updateHistoryNavigation();
        }
    }

    @Override
    public @Unmodifiable ObservableList<PageBreadcrumb> getBreadcrumbs() {
        return breadcrumbs;
    }

    @Override
    public boolean isShowRoot() {
        return showRoot.get();
    }

    @Override
    public ReadOnlyBooleanProperty showRootProperty() {
        return showRoot.getReadOnlyProperty();
    }

    @Override
    public void onFind(String text) {
        setFindMode(true);
        updateHistoryNavigation();
        var matcher = Pattern.compile(Pattern.quote(text), Pattern.CASE_INSENSITIVE).matcher("");
        var statistics = new FindStatistics();
        var matchedItem = match(rootItem.get(), matcher, statistics);
        var findPanel = getComposer().getFindPanelPort();
        findPanel.showFindResultInfo(statistics.getMatches());
        refreshMenuSource.next(matchedItem);
        if (matchedItem != null) {
            var item = findFirstMatched(matchedItem).getOriginal();
            if (!isCurrentPage(item)) {
                var breadcrumbs = getBreadcrumbs(item);
                selectPage(item, breadcrumbs);
            }
        }
    }

    @Override
    public void onFindCleared() {
        var findPanel = getComposer().getFindPanelPort();
        findPanel.hideFindResultInfo();
        setFindMode(false);
        var pageItem = (TreePageItem) getComposer().getSelectedPagePort().getItem();
        addPageHistory(pageItem);
        updateHistoryNavigation();

        updateMenuSource.next(rootItem.get());
        selectPageSource.next(pageItem); // just to select item in the menu
    }

    @Override
    public TreePageItem getRootItem() {
        return rootItem.get();
    }

    @Override
    public ReadOnlyObjectProperty<TreePageItem> rootItemProperty() {
        return rootItem.getReadOnlyProperty();
    }

    @Override
    protected void postInitialize() {
        super.postInitialize();
        updateMenuSource.next(rootItem.get());
    }

    protected void onPageRequested(TreePageItem item) {
        if (item == null) {
            return;
        }
        if (!isCurrentPage(item)) {
            var breadcrumbs = getBreadcrumbs(item);
            selectPage(item, breadcrumbs);
            addPageHistory(item);
            updateHistoryNavigation();
        }
    }

    protected void onPageRequested(PageBreadcrumb breadcrumb) {
        var item = breadcrumb.getItem();
        if (!isCurrentPage(item)) {
            var breadcrumbs = getBreadcrumbs(breadcrumb);
            selectPage(item, breadcrumbs);
            addPageHistory(item);
            updateHistoryNavigation();
        }
    }

    void setPages(TreePageItem rootItem, boolean showRoot) {
        if (Objects.equals(this.rootItem.get(), rootItem) && this.showRoot.get() == showRoot) {
            return;
        }
        this.rootItem.set(rootItem);
        this.showRoot.set(showRoot);
        updateMenuSource.next(rootItem);
    }

    @Override
    void navigateHistory(int newIndex) {
        TreePageItem page = (TreePageItem) getPageHistory().get(newIndex);
        var breadcrumbs = getBreadcrumbs(page);
        selectPage(page, breadcrumbs);
        setPageHistoryIndex(newIndex);
        updateHistoryNavigation();
    }

    ObservableSource<TreePageItem> updateMenuSource() {
        return updateMenuSource;
    }

    ObservableSource<FilteredTreePageItem> refreshMenuSource() {
        return refreshMenuSource;
    }

    ObservableSource<TreePageItem> selectPageSource() {
        return selectPageSource;
    }

    private void selectPage(TreePageItem item, List<PageBreadcrumb> breadcrumbs) {
        modifiableBreadcrumbs.setAll(breadcrumbs);
        selectPageSource.next(item);
    }
}
