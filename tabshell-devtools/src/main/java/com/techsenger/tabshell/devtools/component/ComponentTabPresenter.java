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

import com.techsenger.connectorfx.scenegraph.Element;
import com.techsenger.patternfx.mvp.Descriptor;
import com.techsenger.patternfx.mvp.ParentComposer;
import com.techsenger.patternfx.mvp.ParentPort;
import com.techsenger.patternfx.mvp.ParentPresenter;
import com.techsenger.patternfx.mvp.ParentView;
import com.techsenger.patternfx.mvp.Presenter;
import com.techsenger.patternfx.mvp.View;
import com.techsenger.tabshell.core.AddablePresenter;
import com.techsenger.tabshell.core.CloseCheckResult;
import com.techsenger.tabshell.core.ClosePreparationResult;
import com.techsenger.tabshell.core.tab.AbstractTabPresenter;
import com.techsenger.tabshell.devtools.DevToolsComponents;
import com.techsenger.tabshell.devtools.ToolBarAwarePort;
import com.techsenger.tabshell.devtools.node.NodeTabPort;
import com.techsenger.tabshell.shared.find.FindNavigationAwarePort;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.regex.Matcher;

/**
 *
 * @author Pavel Castornii
 */
public class ComponentTabPresenter<V extends ComponentTabView, C extends ComponentTabComposer>
        extends AbstractTabPresenter<V, C> implements AddablePresenter {

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
        List<Integer> path = new ArrayList<>();
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
        List<Integer> path = new ArrayList<>();
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

    private record InspectorMatchResult(List<InspectorItem> items, int totalMatches) {

    }

    private static InspectorMatchResult matchInspectorItems(Class<? extends View> fxViewClass,
            Class<? extends ParentComposer> fxComposerClass, Presenter<?> presenter, Matcher matcher) {
        var descriptor = presenter.getDescriptor();
        ParentPort port = null;
        if (ParentView.class.isAssignableFrom(fxViewClass)) {
            var parentPresenter = (ParentPresenter<?, ?>) presenter;
            port = parentPresenter.getPort();
        }
        var totalMatches = 0;
        var items = new ArrayList<InspectorItem>();
        // properties
        var tempItems = new ArrayList<InspectorItem>();
        items.add(new InspectorItem(InspectorCategory.PROPERTY, "Property", null, List.of("Value"), null));
        var savedSize = items.size();
        tempItems.add(new InspectorItem(null, "Name", null, List.of(descriptor.getName().getText()), null));
        tempItems.add(new InspectorItem(null, "UUID", null, List.of(descriptor.getUuid().toString()), null));
        tempItems.add(new InspectorItem(null, "State", null, List.of(descriptor.getState().toString()), null));
        tempItems.add(new InspectorItem(null, "HistoryPolicy", null, List.of(presenter.getHistoryPolicy().toString()),
                null));
        for (var item : tempItems) {
            if (matcher == null || matcher.reset(item.name()).find()) {
                items.add(item);
            }
        }
        totalMatches += items.size() - savedSize;
        removeCategoryIfRequired(savedSize, items);

        var  categoryItem = new InspectorItem(InspectorCategory.FX_VIEW, "FX View", null, List.of("Interfaces"), null);
        totalMatches += matchInspectorItems(fxViewClass, categoryItem, items, matcher);

        categoryItem = new InspectorItem(InspectorCategory.PRESENTER, "Presenter", null, List.of("Interfaces"), null);
        totalMatches += matchInspectorItems(presenter.getClass(), categoryItem, items, matcher);

        if (fxComposerClass != null) {
            categoryItem = new InspectorItem(InspectorCategory.COMPOSER, "Composer", null, List.of("Interfaces"), null);
            totalMatches += matchInspectorItems(fxComposerClass, categoryItem, items, matcher);
        }
        if (port != null) {
            categoryItem = new InspectorItem(InspectorCategory.PORT, "Primary Port", null,
                    List.of("Interfaces"), null);
            totalMatches += matchInspectorItems(port.getClass(), categoryItem, items, matcher);
        }
        return new InspectorMatchResult(items, totalMatches);
    }

    private static int matchInspectorItems(Class<?> clazz, InspectorItem cat, List<InspectorItem> items,
            Matcher matcher) {
        items.add(cat);
        var savedSize = items.size();
        createInspectorItems(clazz, items, matcher);
        int totalMatches = items.size() - savedSize;
        removeCategoryIfRequired(savedSize, items);
        return totalMatches;
    }

    private static void removeCategoryIfRequired(int savedSize, List<InspectorItem> items) {
        if (savedSize == items.size()) {
            items.removeLast();
        }
    }

    private static List<Class<?>> getHierarchyFromObject(Class<?> clazz) {
        List<Class<?>> hierarchy = new ArrayList<>();
        Class<?> current = clazz;
        while (current != null) {
            hierarchy.add(current);
            current = current.getSuperclass();
        }
        Collections.reverse(hierarchy);
        return hierarchy;
    }

    private static void createInspectorItems(Class<?> clazz, List<InspectorItem> items, Matcher matcher) {
        var classes = getHierarchyFromObject(clazz);
        for (var c : classes) {
            var matched = false;
            String componentClass = getSimpleName(c);
            if (matcher == null || matcher.reset(componentClass).find()) {
                matched = true;
            }
            var iSimpleNames = new ArrayList<String>();
            var iFullNames = new ArrayList<String>();
            for (var i : c.getInterfaces()) {
                var interfaceClass = getSimpleName(i);
                if (!matched) {
                    matched = matcher.reset(interfaceClass).find();
                }
                iSimpleNames.add(interfaceClass);
                iFullNames.add(i.getName());
            }
            if (matched) {
                items.add(new InspectorItem(null, componentClass, c.getName(), iSimpleNames, iFullNames));
            }
        }
    }

    private static String getSimpleName(Class<?> clazz) {
        if (clazz.getEnclosingClass() != null) {
            String name = clazz.getName();
            int lastDot = name.lastIndexOf('.');
            String withoutPackage = name.substring(lastDot + 1);
            return withoutPackage.replace('$', '.');
        } else {
            return clazz.getSimpleName();
        }
    }

    protected class ComponentToolBarAwarePort implements ToolBarAwarePort, FindNavigationAwarePort {

        @Override
        public void onMatchCase(boolean selected) {
            clearFoundComponents();
            findComponents();
        }

        @Override
        public void onRefresh() {
            refreshComponents();
        }

        @Override
        public void onFind() {
            // It is necessary to refresh the tree on every find because we work directly
            // with the live component tree, not with a snapshot/copy of it
            refreshComponents();
        }

        @Override
        public void onFindCleared() {
            clearFoundComponents();
        }

        @Override
        public void onFindNext() {
            findNextComponent();
        }

        @Override
        public void onFindPrevious() {
            findPreviousComponent();
        }
    }

    protected class InspectorToolBarAwarePort implements ToolBarAwarePort {

        @Override
        public void onMatchCase(boolean selected) {
            refreshInspector();
        }

        @Override
        public void onRefresh() {
            refreshInspector();
        }

        @Override
        public void onFind() {
            refreshInspector();
        }

        @Override
        public void onFindCleared() {
            refreshInspector();
        }

    }

    private final ComponentService service;

    private final NodeTabPort nodeTab;

    private ComponentItem rootComponent;

    private List<FindMatch> componentMatches = Collections.emptyList();

    private int currentMatchIndex = -1;

    private final Map<InspectorCategory, Boolean> expandedByCategory = new HashMap<>();

    private Class<? extends View> componentFxViewClass;

    private Class<? extends ParentComposer> componentFxComposerClass;

    private Presenter<?> componentPresenter;

    public ComponentTabPresenter(V view, ComponentService service, NodeTabPort nodeTab) {
        super(view);
        this.service = service;
        this.nodeTab = nodeTab;
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
    public void handleAdded() {
        refreshComponents();
        Arrays.stream(InspectorCategory.values()).forEach((v) -> expandedByCategory.put(v, Boolean.FALSE));
    }

    @Override
    protected Descriptor createDescriptor() {
        return new Descriptor(DevToolsComponents.COMPONENT_TAB);
    }

    protected void handleComponentSelected(Class<? extends View> fxViewClass,
            Class<? extends ParentComposer> fxComposerClass, Presenter<?> presenter, Element componentNode) {
        if (componentNode != null) {
            this.nodeTab.selectNode(componentNode);
        }
        this.componentFxViewClass = fxViewClass;
        this.componentFxComposerClass = fxComposerClass;
        this.componentPresenter = presenter;
        refreshInspector();
    }

    protected void handleCategoryExpanded(InspectorCategory category, boolean expanded) {
        expandedByCategory.put(category, expanded);
    }

    protected void refreshComponents() {
        var selectedComponent = getView().getSelectedComponent();
        this.rootComponent = this.service.getRootComponent();
        getView().setRootComponent(rootComponent);
        clearFoundComponents();
        findComponents();
        if (selectedComponent != null && this.componentMatches.isEmpty()) { // restoring selected item
            var path = getPath(rootComponent, selectedComponent.getUuid());
            if (!path.isEmpty()) {
                getView().selectComponent(path);
            }
        }
    }

    private void findComponents() {
        var findMatcher = getComposer().getComponentToolBar().createFindMatcher();
        if (findMatcher != null) {
            this.componentMatches = findMatches(rootComponent, findMatcher);
            if (!this.componentMatches.isEmpty()) {
                this.currentMatchIndex = 0;
                getView().selectComponent(this.componentMatches.get(this.currentMatchIndex).path());
            }
            updateFoundComponentInfo();
        }
    }

    private void findNextComponent() {
        if (!this.componentMatches.isEmpty()) {
            this.currentMatchIndex++;
            if (this.currentMatchIndex >= this.componentMatches.size()) {
                this.currentMatchIndex = 0;
            }
            updateFoundComponentInfo();
            getView().selectComponent(this.componentMatches.get(currentMatchIndex).path());
        }
    }

    private void findPreviousComponent() {
        if (!this.componentMatches.isEmpty()) {
            this.currentMatchIndex--;
            if (this.currentMatchIndex < 0) {
                this.currentMatchIndex = this.componentMatches.size() - 1;
            }
            updateFoundComponentInfo();
            getView().selectComponent(this.componentMatches.get(currentMatchIndex).path());
        }
    }

    private void clearFoundComponents() {
        this.componentMatches = Collections.emptyList();
        this.currentMatchIndex = -1;
        getComposer().getComponentToolBar().hideFindResultInfo();
    }

    private void updateFoundComponentInfo() {
        getComposer().getComponentToolBar().showFindResultInfo(currentMatchIndex + 1, this.componentMatches.size());
    }

    private void refreshInspector() {
        if (this.componentFxViewClass != null) {
            var matcher = getComposer().getInspectorToolBar().createFindMatcher();
            var result = matchInspectorItems(
                    this.componentFxViewClass,
                    this.componentFxComposerClass,
                    this.componentPresenter,
                    matcher);
            getView().updateInspector(result.items, expandedByCategory);
            if (matcher != null) {
                getComposer().getInspectorToolBar().showFindResultInfo(result.totalMatches);
            } else {
                getComposer().getInspectorToolBar().hideFindResultInfo();
            }
        } else {
            getView().updateInspector(Collections.emptyList(), expandedByCategory);
            getComposer().getInspectorToolBar().hideFindResultInfo();
        }
    }
}
