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

package com.techsenger.tabshell.devtools.environment;

import com.techsenger.connectorfx.Connector;
import com.techsenger.connectorfx.KeyValue;
import com.techsenger.patternfx.mvp.Descriptor;
import com.techsenger.tabshell.core.CloseCheckResult;
import com.techsenger.tabshell.core.ClosePreparationResult;
import com.techsenger.tabshell.core.tab.AbstractTabPresenter;
import com.techsenger.tabshell.devtools.DevToolsComponents;
import com.techsenger.tabshell.devtools.ToolBarAwarePort;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.stream.Collectors;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;

/**
 *
 * @author Pavel Castornii
 */
public class EnvironmentTabPresenter<V extends EnvironmentTabView, C extends EnvironmentTabComposer>
        extends AbstractTabPresenter<V, C> {

    protected static String getText(EnvironmentCategory cat) {
        return switch (cat) {
            case ENVIRONMENT_VARIABLE -> "Environment Variables";
            case PLATFORM -> "Platform";
            case SYSTEM_PROPERTY -> "System Properties";
            default -> throw new AssertionError();
        };
    }

    protected class ToolBarAwarePortImpl implements ToolBarAwarePort {

        @Override
        public void onMatchCase(boolean selected) {
            refresh();
        }

        @Override
        public void onRefresh() {
            refresh();
        }

        @Override
        public void onFind() {
            refresh();
        }

        @Override
        public void onFindCleared() {
            refresh();
        }
    }

    private final Connector connector;

    private final Map<EnvironmentCategory, BooleanProperty> expandedByCategory;

    public EnvironmentTabPresenter(V view, Connector connector) {
        super(view);
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

    @Override
    protected Descriptor createDescriptor() {
        return new Descriptor(DevToolsComponents.ENVIRONMENT_TAB);
    }

    @Override
    protected void postInitialize() {
        super.postInitialize();
        setTitle("Environment");
        setClosable(false);
        var toolBar = getComposer().getToolBar();
        refresh();
    }

    protected void refresh() {
        var e = this.connector.getEnv();
        var items = new ArrayList<EnvironmentItem>();
        items.add(new DefaultEnvironmentItem(EnvironmentItemType.ROOT, "", null, true));
        var matcher = getComposer().getToolBar().createFindMatcher();
        var savedSize = items.size();
        addItems(items, matcher, EnvironmentCategory.PLATFORM,
                e.getPlatformPreferences(), e.getOtherPlatformProperties(), e.getConditionalFeatures());
        addItems(items, matcher, EnvironmentCategory.SYSTEM_PROPERTY, e.getSystemProperties());
        addItems(items, matcher, EnvironmentCategory.ENVIRONMENT_VARIABLE, e.getEnvVariables());
        if (matcher != null) {
            getComposer().getToolBar().showFindResultInfo(items.size() - savedSize - 1); // -1 is the root
        } else {
            getComposer().getToolBar().hideFindResultInfo();
        }
        getView().setItems(items);
    }

    protected void onItemRequested(EnvironmentItem i) {
        if (i.getType() == EnvironmentItemType.PROPERTY) {
            var dialog = getComposer().addNameValueDialog();
            dialog.setTitle("Property Dialog");
            dialog.setName(i.getName());
            dialog.setValue(i.getValue());
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
                tempList.add(new DefaultEnvironmentItem(EnvironmentItemType.PROPERTY, kv.key(), kv.value(), false));
            }
        }
        if (!tempList.isEmpty()) {
            tempList.sort(Comparator.comparing(EnvironmentItem::getName));
            var expandedProperty = expandedByCategory.get(cat);
            allItems.add(new DefaultEnvironmentItem(EnvironmentItemType.CATEGORY, getText(cat), null,
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
