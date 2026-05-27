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

import com.techsenger.annotations.Unmodifiable;
import com.techsenger.patternfx.mvp.ComponentDescriptor;
import com.techsenger.tabshell.core.page.TreePageContainerPresenter;
import com.techsenger.tabshell.core.page.TreePageItem;
import com.techsenger.tabshell.layout.LayoutComponents;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 *
 * @author Pavel Castornii
 */
public class TreePageHostPresenter<V extends TreePageHostView> extends AbstractPageHostPresenter<V>
        implements TreePageContainerPresenter<V>, TreePageHostPort {

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

    private TreePageItem rootItem;

    private boolean showRoot;

    private List<PageBreadcrumb> breadcrumbs;

    public TreePageHostPresenter(V view, TreePageHostParams params) {
        super(view, params);
    }

    @Override
    public ViewAccess getViewAccess() {
        return getView();
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
    public @Unmodifiable List<PageBreadcrumb> getBreadcrumbs() {
        return Collections.unmodifiableList(breadcrumbs);
    }

    public boolean isShowRoot() {
        return showRoot;
    }

    @Override
    public void onFind(String text) {
        setFindMode(true);
        updateHistoryNavigation();
        var matcher = Pattern.compile(Pattern.quote(text), Pattern.CASE_INSENSITIVE).matcher("");
        var statistics = new FindStatistics();
        var matchedItem = match(rootItem, matcher, statistics);
        var findPanel = getView().getComposer().getFindPanelPort();
        findPanel.showFindResultInfo(statistics.getMatches());
        getView().setMenu(matchedItem, showRoot);
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
        var findPanel = getView().getComposer().getFindPanelPort();
        findPanel.hideFindResultInfo();
        setFindMode(false);
        var pageItem = (TreePageItem) getView().getComposer().getSelectedPagePort().getItem();
        addPageHistory(pageItem);
        updateHistoryNavigation();

        getView().setMenu(rootItem, showRoot);
        getView().setPage(pageItem); // just to select item in the menu
    }

    @Override
    protected ComponentDescriptor createDescriptor() {
        return new ComponentDescriptor(LayoutComponents.TREE_PAGE_HOST);
    }

    protected TreePageItem getRootItem() {
        return rootItem;
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
        this.rootItem = rootItem;
        this.showRoot = false;
        getView().setMenu(rootItem, showRoot);
    }

    @Override
    void navigateHistory(int newIndex) {
        TreePageItem page = (TreePageItem) getPageHistory().get(newIndex);
        var breadcrumbs = getBreadcrumbs(page);
        selectPage(page, breadcrumbs);
        setPageHistoryIndex(newIndex);
        updateHistoryNavigation();
    }

    private void selectPage(TreePageItem item, List<PageBreadcrumb> breadcrumbs) {
        var composer = getView().getComposer();
        var currentPage = composer.getSelectedPagePort();
        if (currentPage != null) {
            currentPage.setSelected(false);
        }
        composer.providePagePort(item);
        this.breadcrumbs = breadcrumbs;
        getView().setBreadcrumbs(breadcrumbs);
        getView().setPage(item);
        currentPage = composer.getSelectedPagePort();
        currentPage.setSelected(true);
    }
}
