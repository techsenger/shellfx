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

package com.techsenger.tabshell.devtools.component;

import com.techsenger.patternfx.mvp.Descriptor;
import com.techsenger.tabshell.core.CloseCheckResult;
import com.techsenger.tabshell.core.ClosePreparationResult;
import com.techsenger.tabshell.core.tab.AbstractTabPresenter;
import com.techsenger.tabshell.devtools.DevToolsComponents;
import com.techsenger.tabshell.devtools.ToolBarAwarePort;
import com.techsenger.tabshell.shared.find.FindNavigationAwarePort;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.regex.Matcher;

/**
 *
 * @author Pavel Castornii
 */
public class ComponentTabPresenter<V extends ComponentTabView, C extends ComponentTabComposer>
        extends AbstractTabPresenter<V, C> {

    private record FindMatch(ComponentItem item, List<Integer> path) { }

    /**
     * Traverses the tree and finds all items whose text matches the given matcher.
     */
    private static List<FindMatch> findMatches(ComponentItem root, Matcher matcher) {
        List<FindMatch> results = new ArrayList<>();
        BiFunction<ComponentItem, List<Integer>, Boolean> func = (item, path) -> {
            if (matcher.reset(item.getName().getText()).find() || matcher.reset(item.getUuid().toString()).find()) {
                results.add(new FindMatch(item, new ArrayList<>(path)));
            }
            return true;
        };
        List<Integer> path = new ArrayList();
        path.add(0); // root
        traverse(root, path, func);
        return results;
    }

    private static List<Integer> getPath(ComponentItem root, UUID itemUuid) {
        List<Integer> foundPath = new ArrayList<>();
        BiFunction<ComponentItem, List<Integer>, Boolean> func = (item, path) -> {
            if (item.getUuid().equals(itemUuid)) {
                foundPath.addAll(path);
                return false;
            } else {
                return true;
            }
        };
        List<Integer> path = new ArrayList();
        path.add(0); // root
        traverse(root, path, func);
        return foundPath;
    }

    private static boolean traverse(ComponentItem current, List<Integer> currentPath,
            BiFunction<ComponentItem, List<Integer>, Boolean> consumer) {
        var result = consumer.apply(current, currentPath);
        if (!result) {
            return false;
        }
        // Recursively traverse all children
        List<ComponentItem> children = current.getChildren();
        if (children != null) {
            for (int i = 0; i < children.size(); i++) {
                ComponentItem child = children.get(i);
                // Add the index to the current path
                currentPath.add(i);
                result = traverse(child, currentPath, consumer);
                if (!result) {
                    return false;
                }
                // Remove the index after returning from recursion (backtracking)
                currentPath.removeLast();
            }
        }
        return true;
    }

    protected class ToolBarAwarePortImpl implements ToolBarAwarePort, FindNavigationAwarePort {

        @Override
        public void onMatchCase(boolean selected) {
            clearMatches();
            findMatches();
        }

        @Override
        public void onRefresh() {
            refresh();
        }

        @Override
        public void onFind() {
            // It is necessary to refresh the tree on every find because we work directly
            // with the live component tree, not with a snapshot/copy of it
            refresh();
        }

        @Override
        public void onFindCleared() {
            clearMatches();
        }

        @Override
        public void onFindNext() {
            findNext();
        }

        @Override
        public void onFindPrevious() {
            findPrevious();
        }
    }

    private final ComponentService service;

    private ComponentItem rootComponent;

    private List<FindMatch> matches = Collections.EMPTY_LIST;

    private int currentMatchIndex = -1;

    public ComponentTabPresenter(V view, ComponentService service) {
        super(view);
        this.service = service;
    }

    @Override
    public CloseCheckResult isReadyToClose() {
        return CloseCheckResult.READY;
    }

    @Override
    public void prepareToClose(Consumer<ClosePreparationResult> resultCallback) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    protected Descriptor createDescriptor() {
        return new Descriptor(DevToolsComponents.COMPONENT_TAB);
    }

    @Override
    protected void postInitialize() {
        super.postInitialize();
        var toolBar = getComposer().getToolBar();
        refresh();
    }

    protected void refresh() {
        var selectedItem = getView().getSelectedItem();
        this.rootComponent = this.service.getRootComponent();
        getView().setRootItem(rootComponent);
        clearMatches();
        findMatches();
        if (selectedItem != null && this.matches.isEmpty()) { // restoring selected item
            var path = getPath(rootComponent, selectedItem.getUuid());
            if (!path.isEmpty()) {
                getView().selectItem(path);
            }
        }
    }

    private void findMatches() {
        var findMatcher = getComposer().getToolBar().createFindMatcher();
        if (findMatcher != null) {
            this.matches = findMatches(rootComponent, findMatcher);
            if (!this.matches.isEmpty()) {
                this.currentMatchIndex = 0;
                getView().selectItem(this.matches.get(this.currentMatchIndex).path());
            }
            updateMatchesInfo();
        }
    }

    private void findNext() {
        if (!this.matches.isEmpty()) {
            this.currentMatchIndex++;
            if (this.currentMatchIndex >= this.matches.size()) {
                this.currentMatchIndex = 0;
            }
            updateMatchesInfo();
            getView().selectItem(this.matches.get(currentMatchIndex).path());
        }
    }

    private void findPrevious() {
        if (!this.matches.isEmpty()) {
            this.currentMatchIndex--;
            if (this.currentMatchIndex < 0) {
                this.currentMatchIndex = this.matches.size() - 1;
            }
            updateMatchesInfo();
            getView().selectItem(this.matches.get(currentMatchIndex).path());
        }
    }

    private void clearMatches() {
        this.matches = Collections.EMPTY_LIST;
        this.currentMatchIndex = -1;
        getComposer().getToolBar().hideFindResultInfo();
    }

    private void updateMatchesInfo() {
        getComposer().getToolBar().showFindResultInfo(currentMatchIndex + 1, this.matches.size());
    }
}
