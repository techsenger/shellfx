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
import com.techsenger.tabshell.dialogs.namevalue.NameValueDialogViewModel;
import com.techsenger.tabshell.jfx.AbstractSearchableTabViewModel;
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
public class EnvironmentTabViewModel<T extends EnvironmentTabMediator> extends AbstractSearchableTabViewModel<T> {

    private final Connector connector;

    private final ObservableList<EnvironmentDataItem> items = FXCollections.observableArrayList();

    private final Map<EnvironmentCategory, BooleanProperty> expandedByCategory;

    private final ReadOnlyObjectWrapper<EnvironmentItem> selectedItem = new ReadOnlyObjectWrapper<>();

    public EnvironmentTabViewModel(Connector connector) {
        this.connector = connector;
        expandedByCategory = Arrays.stream(EnvironmentCategory.values())
                .collect(Collectors.toMap(e -> e, e -> new SimpleBooleanProperty()));
    }

    @Override
    public CloseCheckResult canClose() {
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
        caseSensitiveProperty().addListener((ov, oldV, newV) -> refresh());
    }

    ObservableList<EnvironmentDataItem> getItems() {
        return items;
    }

    Map<EnvironmentCategory, BooleanProperty> getExpandedByCategory() {
        return expandedByCategory;
    }

    ReadOnlyObjectWrapper<EnvironmentItem> getSelectedItemWrapper() {
        return selectedItem;
    }

    void refresh() {
        items.clear();
        var e = this.connector.getEnv();
        var allItems = new ArrayList<EnvironmentDataItem>();
        var matcher = createMatcher();
        addItems(allItems, matcher, EnvironmentCategory.PLATFORM,
                e.getPlatformPreferences(), e.getOtherPlatformProperties(), e.getConditionalFeatures());
        addItems(allItems, matcher, EnvironmentCategory.SYSTEM_PROPERTY, e.getSystemProperties());
        addItems(allItems, matcher, EnvironmentCategory.ENVIRONMENT_VARIABLE, e.getEnvVariables());
        items.addAll(allItems);
    }

    void handleItemClick() {
        if (getSelectedItem() instanceof EnvironmentDataItem i) {
            var vm = new NameValueDialogViewModel<>(DialogScope.TAB, true);
            vm.setTitle("Property Dialog");
            vm.setName(i.name());
            vm.setValue(i.value());
            getMediator().addNameValueDialog(vm);
        }
    }

    private void addItems(List<EnvironmentDataItem> allItems, Matcher matcher, EnvironmentCategory cat,
            List<KeyValue>... lists) {
        var tempList = new ArrayList<EnvironmentDataItem>();
        for (var l : lists) {
            for (var kv : l) {
                if (matcher != null && !matcher.reset(kv.key()).find()) {
                    continue;
                }
                tempList.add(new EnvironmentDataItem(cat, kv.key(), kv.value()));
            }
        }
        tempList.sort(Comparator.comparing(EnvironmentDataItem::name));
        allItems.addAll(tempList);
    }
}
