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

package com.techsenger.shellfx.devtools.component;

import com.techsenger.connectorfx.scenegraph.Element;
import com.techsenger.patternfx.mvvm.ParentView;
import com.techsenger.patternfx.mvvm.View;
import com.techsenger.patternfx.mvvm.ViewModel;
import com.techsenger.shellfx.core.UiExecutor;
import com.techsenger.shellfx.core.close.CloseCheckResult;
import com.techsenger.shellfx.core.close.ClosePreparationResult;
import com.techsenger.shellfx.core.dialog.DialogParams;
import com.techsenger.shellfx.core.tab.AbstractTabViewModel;
import com.techsenger.shellfx.core.window.WindowType;
import com.techsenger.shellfx.devtools.DevToolsHostType;
import com.techsenger.shellfx.devtools.DevToolsTabDockPort;
import com.techsenger.shellfx.devtools.shared.IndexedFindResult;
import com.techsenger.shellfx.devtools.shared.NavigableToolBarAwarePort;
import com.techsenger.shellfx.devtools.shared.ToolBarAwarePort;
import com.techsenger.shellfx.devtools.shared.TotalFindResult;
import com.techsenger.shellfx.dialogs.namevalue.FullNameValueDialogPort;
import com.techsenger.shellfx.dialogs.namevalue.NameValueButtons;
import com.techsenger.shellfx.shared.find.FindResult;
import com.techsenger.shellfx.shared.find.NavigableFindResult;
import com.techsenger.toolkit.fx.value.ObservableSource;
import com.techsenger.toolkit.fx.value.SimpleObservableSource;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringJoiner;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.regex.Matcher;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;

/**
 *
 * @author Pavel Castornii
 */
public class ComponentTabViewModel<C extends ComponentTabComposer> extends AbstractTabViewModel<C> {

    private record FindMatch(ComponentItem item) { }

    /**
     * Traverses the tree and finds all items whose text matches the given matcher.
     */
    private static List<FindMatch> findMatches(ComponentItem root, Matcher matcher) {
        List<FindMatch> results = new ArrayList<>();
        traverse(root, item -> {
            if (matcher.reset(item.getName().getText()).find() || matcher.reset(item.getUuid().toString()).find()) {
                results.add(new FindMatch(item));
            }
            return true;
        });
        return results;
    }

    private static boolean traverse(ComponentItem current, Function<ComponentItem, Boolean> consumer) {
        var result = consumer.apply(current);
        if (!result) {
            return false;
        }
        // Recursively traverse all children
        List<ComponentItem> children = current.getChildren();
        if (children != null) {
            for (var child : children) {
                result = traverse(child, consumer);
                if (!result) {
                    return false;
                }
            }
        }
        return true;
    }

    private record InspectorMatchResult(List<InspectorItem> items, int totalMatches) { }

    private static InspectorMatchResult matchInspectorItems(Class<? extends View> fxViewClass,
            Class<? extends ParentView.Composer> fxComposerClass, ViewModel viewModel, Matcher matcher) {
        var descriptor = viewModel.getDescriptor();
        var totalMatches = 0;
        var items = new ArrayList<InspectorItem>();
        // properties
        var tempItems = new ArrayList<InspectorItem>();
        items.add(new InspectorItem(InspectorCategory.PROPERTY, "Property", null, List.of("Value"), null));
        var savedSize = items.size();
        tempItems.add(new InspectorItem(null, "Name", null, List.of(descriptor.getName().getText()), null));
        tempItems.add(new InspectorItem(null, "UUID", null, List.of(descriptor.getUuid().toString()), null));
        tempItems.add(new InspectorItem(null, "State", null, List.of(descriptor.getState().toString()), null));
        for (var item : tempItems) {
            if (matcher == null || matcher.reset(item.name()).find()) {
                items.add(item);
            }
        }
        totalMatches += items.size() - savedSize;
        removeCategoryIfRequired(savedSize, items);

        var  categoryItem = new InspectorItem(InspectorCategory.VIEW, "View", null, List.of("Interfaces"), null);
        totalMatches += matchInspectorItems(fxViewClass, categoryItem, items, matcher);

        categoryItem = new InspectorItem(InspectorCategory.VIEW_MODEL, "ViewModel", null, List.of("Interfaces"), null);
        totalMatches += matchInspectorItems(viewModel.getClass(), categoryItem, items, matcher);

        if (fxComposerClass != null) {
            categoryItem = new InspectorItem(InspectorCategory.COMPOSER, "Composer", null, List.of("Interfaces"), null);
            totalMatches += matchInspectorItems(fxComposerClass, categoryItem, items, matcher);
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
        var seenInterfaces = new LinkedHashSet<Class<?>>();
        for (var c : classes) {
            var matched = false;
            String componentClass = getSimpleName(c);
            if (matcher == null || matcher.reset(componentClass).find()) {
                matched = true;
            }
            // interfaces implemented directly by this class, plus everything they extend transitively
            var ownInterfaces = new LinkedHashSet<Class<?>>();
            collectInterfaces(c.getInterfaces(), ownInterfaces);

            var iSimpleNames = new ArrayList<String>();
            var iFullNames = new ArrayList<String>();
            for (var i : ownInterfaces) {
                // skip interfaces already shown for a class higher up in the hierarchy
                if (seenInterfaces.contains(i)) {
                    continue;
                }
                var interfaceClass = getSimpleName(i);
                if (!matched) {
                    matched = matcher.reset(interfaceClass).find();
                }
                iSimpleNames.add(interfaceClass);
                iFullNames.add(i.getName());
            }
            seenInterfaces.addAll(ownInterfaces);
            if (matched) {
                items.add(new InspectorItem(null, componentClass, c.getName(), iSimpleNames, iFullNames));
            }
        }
    }

    private static void collectInterfaces(Class<?>[] interfaces, Set<Class<?>> result) {
        for (var i : interfaces) {
            if (result.add(i)) {
                collectInterfaces(i.getInterfaces(), result);
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

    protected class ComponentToolBarAwarePort implements NavigableToolBarAwarePort {

        @Override
        public void onMatchCase(boolean selected) {
            // find result is refreshed by the runFind() triggered right after this returns
        }

        @Override
        public void onRefresh() {
            refreshComponents();
        }

        @Override
        public CompletableFuture<NavigableFindResult> onFind() {
            // It is necessary to refresh the tree on every find because we work directly
            // with the live component tree, not with a snapshot/copy of it
            refreshComponents();
            return CompletableFuture.completedFuture(buildFoundComponentInfo());
        }

        @Override
        public void onFindCleared() {
            clearFoundComponents();
        }
    }

    protected class InspectorToolBarAwarePort implements ToolBarAwarePort {

        @Override
        public void onMatchCase(boolean selected) {
            // find result is refreshed by the runFind() triggered right after this returns
        }

        @Override
        public void onRefresh() {
            refreshInspector();
        }

        @Override
        public CompletableFuture<FindResult> onFind() {
            return CompletableFuture.completedFuture(refreshInspector());
        }

        @Override
        public void onFindCleared() {
            refreshInspector();
        }

    }

    private final ObjectProperty<ComponentItem> rootComponent = new SimpleObjectProperty<>();

    private final ObservableSource<UUID> selectComponentByUuidSource = new SimpleObservableSource<>();

    private final ObservableSource<Element> selectComponentByElementSource = new SimpleObservableSource<>();

    private final ObservableSource<Void> selectRootComponentSource = new SimpleObservableSource<>();

    private final ObservableSource<InspectorRefreshData> refreshInspectorSource = new SimpleObservableSource<>();

    private final ComponentService service;

    private final DevToolsTabDockPort tabDock;

    private List<FindMatch> componentMatches = Collections.emptyList();

    private int currentMatchIndex = -1;

    private final Map<InspectorCategory, Boolean> expandedByCategory = new HashMap<>();

    private Class<? extends View> componentFxViewClass;

    private Class<? extends ParentView.Composer> componentFxComposerClass;

    private ViewModel componentViewModel;

    private ComponentItem selectedComponent;

    private boolean selectNode = true;

    public ComponentTabViewModel(ComponentTabParams params) {
        super(params);
        this.service = params.getService();
        this.tabDock = params.getTabDock();
    }

    @Override
    public CloseCheckResult isReadyToClose() {
        return CloseCheckResult.READY;
    }

    @Override
    public void prepareToClose(Consumer<ClosePreparationResult> resultCallback) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public ComponentItem getSelectedComponent() {
        return selectedComponent;
    }

    public ComponentItem getRootComponent() {
        return rootComponent.get();
    }

    public ObjectProperty<ComponentItem> rootComponentProperty() {
        return rootComponent;
    }

    @Override
    protected void postInitialize() {
        super.postInitialize();
        setTitle("Components");
        setClosable(false);
        this.tabDock.getSelector().addPostListener((oldUid, newUid, oldNode, newNode) -> {
            if (oldUid != newUid) {
                var component = service.getComponent(newUid);
                if (component != null) {
                    rootComponent.set(component);
                }
            }
            if (newNode != null) {
                this.selectNode = false;
                selectComponentByElementSource.next(newNode);
            } else {
                selectRootComponentSource.next(null);
            }
        });
        UiExecutor.execute(() -> {
            refreshComponents();
            Arrays.stream(InspectorCategory.values()).forEach((v) -> expandedByCategory.put(v, Boolean.FALSE));
        });
    }

    protected void onComponentSelected(ComponentItem component, Class<? extends View> fxViewClass,
            Class<? extends ParentView.Composer> fxComposerClass, ViewModel viewModel,
            Element componentNode) {
        this.selectedComponent = component;
        if (componentNode != null && this.selectNode) {
            this.tabDock.getSelector().selectNode(tabDock.getSelector().getSelectedWindowUid(), componentNode);
        }
        this.selectNode = true;
        this.componentFxViewClass = fxViewClass;
        this.componentFxComposerClass = fxComposerClass;
        this.componentViewModel = viewModel;
        refreshInspector();
    }

    protected void onInspectorItemRequested(InspectorItem parent, InspectorItem item) {
        if (item.category() != null) {
            return;
        }
        WindowType type = WindowType.NESTED;
        if (tabDock.getHostType() == DevToolsHostType.WINDOW) {
            type = WindowType.TOP_LEVEL;
        }
        var params = new DialogParams(type, getShellContext().getSettings().getAppearance());
        FullNameValueDialogPort dialog;
        if (parent.category() == InspectorCategory.PROPERTY) {
            dialog = getComposer().addNameValueDialog("Property", "Value", params);
        } else {
            dialog = getComposer().addNameValueDialog("Class", "Interfaces", params);
        }
        dialog.setWidth(600);
        dialog.setHeight(350);
        dialog.setRightButtons(NameValueButtons.OK);
        dialog.setTitle("Inspector Dialog");
        dialog.setName(item.name());
        var joiner = new StringJoiner(", ");
        item.values().forEach(s -> joiner.add(s));
        dialog.setValue(joiner.toString());
    }

    protected void onCategoryExpanded(InspectorCategory category, boolean expanded) {
        expandedByCategory.put(category, expanded);
    }

    protected void refreshComponents() {
        var root = this.service.getShellComponent();
        this.rootComponent.set(root);
        clearFoundComponents();
        findComponents();
        if (selectedComponent != null && this.componentMatches.isEmpty()) { // restoring selected item
            selectComponentByUuidSource.next(selectedComponent.getUuid());
        }
    }

    ObservableSource<UUID> selectComponentByUuidSource() {
        return selectComponentByUuidSource;
    }

    ObservableSource<Element> selectComponentByElementSource() {
        return selectComponentByElementSource;
    }

    ObservableSource<Void> selectRootComponentSource() {
        return selectRootComponentSource;
    }

    ObservableSource<InspectorRefreshData> refreshInspectorSource() {
        return refreshInspectorSource;
    }

    private void findComponents() {
        var findMatcher = getComposer().getComponentToolBarPort().createFindMatcher();
        if (findMatcher != null) {
            var root = getRootComponent();
            this.componentMatches = findMatches(root, findMatcher);
            if (!this.componentMatches.isEmpty()) {
                this.currentMatchIndex = 0;
                selectComponentByUuidSource.next(this.componentMatches.get(this.currentMatchIndex).item().getUuid());
            }
        }
    }

    private void findNextComponent() {
        if (!this.componentMatches.isEmpty()) {
            this.currentMatchIndex++;
            if (this.currentMatchIndex >= this.componentMatches.size()) {
                this.currentMatchIndex = 0;
            }
            selectComponentByUuidSource.next(this.componentMatches.get(currentMatchIndex).item().getUuid());
        }
    }

    private void findPreviousComponent() {
        if (!this.componentMatches.isEmpty()) {
            this.currentMatchIndex--;
            if (this.currentMatchIndex < 0) {
                this.currentMatchIndex = this.componentMatches.size() - 1;
            }
            selectComponentByUuidSource.next(this.componentMatches.get(currentMatchIndex).item().getUuid());
        }
    }

    private void clearFoundComponents() {
        this.componentMatches = Collections.emptyList();
        this.currentMatchIndex = -1;
    }

    private NavigableFindResult buildFoundComponentInfo() {
        return new IndexedFindResult(() -> this.componentMatches.size(), () -> this.currentMatchIndex,
                this::findNextComponent, this::findPreviousComponent);
    }

    private FindResult refreshInspector() {
        var composer = getComposer();
        if (this.componentFxViewClass != null) {
            var matcher = composer.getInspectorToolBarPort().createFindMatcher();
            var result = matchInspectorItems(
                    this.componentFxViewClass,
                    this.componentFxComposerClass,
                    this.componentViewModel,
                    matcher);
            refreshInspectorSource.next(new InspectorRefreshData(result.items, expandedByCategory));
            return matcher != null ? new TotalFindResult(result.totalMatches) : null;
        } else {
            refreshInspectorSource.next(new InspectorRefreshData(Collections.emptyList(), expandedByCategory));
            return null;
        }
    }
}
