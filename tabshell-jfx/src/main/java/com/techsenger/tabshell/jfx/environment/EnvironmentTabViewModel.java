/*
 * Copyright 2024-2025 Pavel Castornii.
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

package com.techsenger.tabshell.jfx.environment;

import com.techsenger.patternfx.core.State;
import com.techsenger.tabshell.core.CloseCheckResult;
import com.techsenger.tabshell.core.ClosePreparationResult;
import com.techsenger.tabshell.core.dialog.DialogScope;
import com.techsenger.tabshell.core.tab.AbstractTabViewModel;
import com.techsenger.tabshell.dialogs.namevalue.NameValueDialogViewModel;
import static com.techsenger.tabshell.jfx.environment.EnvironmentCategory.ENVIRONMENT_VARIABLE;
import static com.techsenger.tabshell.jfx.environment.EnvironmentCategory.PLATFORM;
import static com.techsenger.tabshell.jfx.environment.EnvironmentCategory.SYSTEM_PROPERTY;
import devtoolsfx.connector.Connector;
import devtoolsfx.connector.KeyValue;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.stream.Collectors;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

/**
 *
 * @author Pavel Castornii
 */
public class EnvironmentTabViewModel<T extends EnvironmentTabMediator> extends AbstractTabViewModel<T> {

    protected static String getText(EnvironmentCategory cat) {
        return switch (cat) {
            case ENVIRONMENT_VARIABLE -> "Environment Variables";
            case PLATFORM -> "Platform";
            case SYSTEM_PROPERTY -> "System Properties";
            default -> throw new AssertionError();
        };
    }

    private final Connector connector;

    /**
    * Flat list of items representing a tree structure. The hierarchy is encoded via {@link EnvironmentItem#getDepth()}
    * and the actual TreeItems are rebuilt in the View on each refresh.
    */
    private final ObservableList<EnvironmentItem> items = FXCollections.observableArrayList();

    private final Map<EnvironmentCategory, BooleanProperty> expandedByCategory;

    private final ReadOnlyObjectWrapper<EnvironmentItem> selectedItem = new ReadOnlyObjectWrapper<>();

    public EnvironmentTabViewModel(Connector connector) {
        this.connector = connector;
        expandedByCategory = Arrays.stream(EnvironmentCategory.values())
                .collect(Collectors.toMap(e -> e, e -> new SimpleBooleanProperty()));
    }

    @Override
    public CloseCheckResult isReadyToClose() {
        return CloseCheckResult.READY;
    }

    @Override
    public void prepareToClose(Consumer<ClosePreparationResult> resultCallback) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public final ReadOnlyObjectProperty<EnvironmentItem> selectedItemProperty() {
        return selectedItem.getReadOnlyProperty();
    }

    public final EnvironmentItem getSelectedItem() {
        return selectedItem.get();
    }

    @Override
    protected void initialize() {
        super.initialize();
        setTitle("Environment");
        setClosable(false);

        getMediator().stateProperty().addListener((ov, oldV, newV) -> {
            if (newV == State.INITIALIZED) {
                refresh();
            }
        });
        getMediator().getSearchPanel().caseSensitiveProperty().addListener((ov, oldV, newV) -> refresh());
    }

    ObservableList<EnvironmentItem> getItems() {
        return items;
    }

    ReadOnlyObjectWrapper<EnvironmentItem> getSelectedItemWrapper() {
        return selectedItem;
    }

    void refresh() {
        items.clear();
        var e = this.connector.getEnv();
        var allItems = new ArrayList<EnvironmentItem>();
        allItems.add(new DefaultEnvironmentItem(EnvironmentItem.ROOT_DEPTH, "", null, true));
        var matcher = getMediator().getSearchPanel().createMatcher();
        addItems(allItems, matcher, EnvironmentCategory.PLATFORM,
                e.getPlatformPreferences(), e.getOtherPlatformProperties(), e.getConditionalFeatures());
        addItems(allItems, matcher, EnvironmentCategory.SYSTEM_PROPERTY, e.getSystemProperties());
        addItems(allItems, matcher, EnvironmentCategory.ENVIRONMENT_VARIABLE, e.getEnvVariables());
        items.addAll(allItems);
    }

    void handleItemClick() {
        var i = getSelectedItem();
        if (i.getDepth() == EnvironmentItem.PROPERTY_DEPTH) {
            var vm = new NameValueDialogViewModel<>(DialogScope.TAB, true);
            vm.setTitle("Property Dialog");
            vm.setName(i.getName());
            vm.setValue(i.getValue());
            getMediator().addNameValueDialog(vm);
        }
    }

    private void addItems(List<EnvironmentItem> allItems, Matcher matcher, EnvironmentCategory cat,
            List<KeyValue>... lists) {
        var tempList = new ArrayList<EnvironmentItem>();
        for (var l : lists) {
            for (var kv : l) {
                if (matcher != null && !matcher.reset(kv.key()).find()) {
                    continue;
                }
                tempList.add(new DefaultEnvironmentItem(EnvironmentItem.PROPERTY_DEPTH, kv.key(), kv.value(), false));
            }
        }
        if (!tempList.isEmpty()) {
            tempList.sort(Comparator.comparing(EnvironmentItem::getName));
            var expandedProperty = expandedByCategory.get(cat);
            allItems.add(new DefaultEnvironmentItem(EnvironmentItem.CATEGORY_DEPTH, getText(cat), null,
                    expandedProperty.get()) {

                @Override
                public void setExpanded(boolean expanded) {
                    expandedProperty.set(expanded);
                }
            });
            allItems.addAll(tempList);
        }
    }
}
