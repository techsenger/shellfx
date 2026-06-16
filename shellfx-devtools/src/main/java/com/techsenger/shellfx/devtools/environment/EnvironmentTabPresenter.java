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

package com.techsenger.shellfx.devtools.environment;

import com.techsenger.connectorfx.KeyValue;
import com.techsenger.patternfx.mvp.ComponentDescriptor;
import com.techsenger.shellfx.core.CloseCheckResult;
import com.techsenger.shellfx.core.ClosePreparationResult;
import com.techsenger.shellfx.core.dialog.DialogParams;
import com.techsenger.shellfx.core.tab.AbstractTabPresenter;
import com.techsenger.shellfx.core.window.WindowType;
import com.techsenger.shellfx.devtools.DevToolsComponents;
import com.techsenger.shellfx.devtools.DevToolsHostType;
import com.techsenger.shellfx.devtools.DevToolsTabDockPort;
import com.techsenger.shellfx.devtools.ToolBarAwarePort;
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
public class EnvironmentTabPresenter<V extends EnvironmentTabView> extends AbstractTabPresenter<V> {

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

    private final DevToolsTabDockPort tabDock;

    private final Map<EnvironmentCategory, BooleanProperty> expandedByCategory;

    public EnvironmentTabPresenter(V view, EnvironmentTabParams params) {
        super(view, params);
        this.tabDock = params.getTabDock();
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
    protected ComponentDescriptor createDescriptor() {
        return new ComponentDescriptor(DevToolsComponents.ENVIRONMENT_TAB);
    }

    @Override
    protected void postInitialize() {
        super.postInitialize();
        setTitle("Environment");
        setClosable(false);
        var toolBar = getView().getComposer().getToolBarPort();
        refresh();
    }

    protected void refresh() {
        var composer = getView().getComposer();
        var e = this.tabDock.getConnector().getEnv();
        var items = new ArrayList<EnvironmentItem>();
        items.add(new DefaultEnvironmentItem(EnvironmentItemType.ROOT, "", null, true));
        var matcher = composer.getToolBarPort().createFindMatcher();
        var savedSize = items.size();
        addItems(items, matcher, EnvironmentCategory.PLATFORM,
                e.getPlatformPreferences(), e.getOtherPlatformProperties(), e.getConditionalFeatures());
        addItems(items, matcher, EnvironmentCategory.SYSTEM_PROPERTY, e.getSystemProperties());
        addItems(items, matcher, EnvironmentCategory.ENVIRONMENT_VARIABLE, e.getEnvVariables());
        if (matcher != null) {
            composer.getToolBarPort().showFindResultInfo(items.size() - savedSize - 1); // -1 is the root
        } else {
            composer.getToolBarPort().hideFindResultInfo();
        }
        getView().setItems(items);
    }

    protected void onItemRequested(EnvironmentItem i) {
        if (i.getType() == EnvironmentItemType.PROPERTY) {
            WindowType windowType = WindowType.NESTED;
            if (tabDock.getHostType() == DevToolsHostType.WINDOW) {
                windowType = WindowType.TOP_LEVEL;
            }
            var params = new DialogParams(windowType, getShellContext().getSettings().getAppearance());
            var dialog = getView().getComposer().openNameValueDialog(params);
            dialog.setResizable(true);
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
