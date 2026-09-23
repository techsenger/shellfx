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

package com.techsenger.shellfx.devtools.stylesheet;

import com.techsenger.annotations.Unmodifiable;
import com.techsenger.connectorfx.scenegraph.Element;
import com.techsenger.connectorfx.scenegraph.WindowProperties;
import static com.techsenger.connectorfx.scenegraph.WindowProperties.WindowType.ALERT;
import static com.techsenger.connectorfx.scenegraph.WindowProperties.WindowType.MODAL;
import static com.techsenger.connectorfx.scenegraph.WindowProperties.WindowType.POPUP;
import static com.techsenger.connectorfx.scenegraph.WindowProperties.WindowType.STAGE;
import com.techsenger.shellfx.core.UiExecutor;
import com.techsenger.shellfx.core.close.CloseCheckResult;
import com.techsenger.shellfx.core.close.ClosePreparationResult;
import com.techsenger.shellfx.core.tab.AbstractTabViewModel;
import com.techsenger.shellfx.devtools.DevToolsTabDockPort;
import com.techsenger.shellfx.devtools.ElementUtils;
import com.techsenger.shellfx.devtools.shared.ToolBarAwarePort;
import com.techsenger.shellfx.devtools.shared.TotalFindResult;
import com.techsenger.shellfx.shared.find.FindResult;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

/**
 *
 * @author Pavel Castornii
 */
public class StylesheetTabViewModel<C extends StylesheetTabComposer> extends AbstractTabViewModel<C>
        implements ToolBarAwarePort {

    private static String formatWindowType(int uid, WindowProperties props) {
        String text;
        if (props.isPrimaryStage()) {
            text = "Primary Stage";
        } else {
            text = switch (props.windowType()) {
                case STAGE -> formatWindowText("Stage", props.windowTitle(), uid);
                case MODAL -> formatWindowText("Modal", props.windowTitle(), uid);
                case ALERT -> formatWindowText("Alert", props.windowTitle(), uid);
                case POPUP -> formatWindowText("Popup", props.ownerClassName(), uid, "owner");
            };
        }
        return text;
    }

    private static String formatWindowText(String type, String property, int uid) {
        return formatWindowText(type, property, uid, "title");
    }

    private static String formatWindowText(String type, String property, int uid, String propName) {
        if (property != null) {
            return type + " [" + propName + "=\"" + property + "\"" + "]";
        } else {
            return type + "@" + uid;
        }
    }

    private final ObservableList<StylesheetItem> modifiableItems = FXCollections.observableArrayList();

    private final ObservableList<StylesheetItem> items = FXCollections.unmodifiableObservableList(modifiableItems);

    private final DevToolsTabDockPort tabDock;

    public StylesheetTabViewModel(StylesheetTabParams params) {
        super(params);
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

    public @Unmodifiable ObservableList<StylesheetItem> getItems() {
        return items;
    }

    @Override
    public void onMatchCase(boolean selected) {
        // find result is refreshed by the runFind() triggered right after this returns
    }

    @Override
    public void onRefresh() {
        refresh();
    }

    @Override
    public CompletableFuture<FindResult> onFind() {
        return CompletableFuture.completedFuture(rebuildTree());
    }

    @Override
    public void onFindCleared() {
        rebuildTree();
    }

    @Override
    protected void postInitialize() {
        super.postInitialize();
        setTitle("Stylesheets");
        setClosable(false);
        UiExecutor.execute(() -> rebuildTree());
    }

    protected void refresh() {
        rebuildTree();
    }

    protected void onStylesheetSelected(StylesheetItem s) {
        if (s.type() == StylesheetItemType.APPLICATION || s.type() == StylesheetItemType.STYLESHEET) {
            return;
        }
        if (s.node() != null) {
            this.tabDock.getSelector().selectNode(tabDock.getSelector().getSelectedWindowUid(), s.node());
        } else {
            this.tabDock.getSelector().selectWindow(tabDock.getSelector().getSelectedWindowUid());
        }
    }

    protected FindResult rebuildTree() {
        var connector = this.tabDock.getConnector();
        var entry = connector.getStyledElements(tabDock.getSelector().getSelectedWindowUid());
        Matcher matcher = getComposer().getToolBarPort().createFindMatcher();

        List<StylesheetItem> items = new ArrayList<>();
        var item = new StylesheetItem(StylesheetItemType.APPLICATION,
                "Application [" + connector.getUserAgentStylesheet() + "]", true, null);
        items.add(item);
        item = new StylesheetItem(StylesheetItemType.WINDOW,
                formatWindowType(tabDock.getSelector().getSelectedWindowUid(), entry.getKey()), true, null);
        items.add(item);

        var found = 0;
        var sceneStylesheets = entry.getKey().sceneStylesheets();
        if (sceneStylesheets != null && !sceneStylesheets.isEmpty()) {
            item = filterAndCreateStylesheet(null, matcher);
            if (item != null) {
                found++;
                items.add(item);
                for (var s : sceneStylesheets) {
                    items.add(new StylesheetItem(StylesheetItemType.STYLESHEET, s, false, null));
                }
            }
        }

        for (var e : entry.getValue()) {
            item = filterAndCreateStylesheet(e, matcher);
            if (item != null) {
                found++;
                items.add(item);
                for (var s : e.getNodeProperties().stylesheets()) {
                    items.add(new StylesheetItem(StylesheetItemType.STYLESHEET, s, false, null));
                }
            }
        }
        setItems(items);
        return matcher != null ? new TotalFindResult(found) : null;
    }

    protected void setItems(List<StylesheetItem> items) {
        modifiableItems.setAll(items);
    }

    protected StylesheetItem filterAndCreateStylesheet(Element el, Matcher matcher) {
        StylesheetItem item = null;
        if (el == null) {
            if (matcher == null) {
                item = new StylesheetItem(StylesheetItemType.NODE, "Scene", false, el);
            } else {
                if (matcher.reset("Scene").find()) {
                    item = new StylesheetItem(StylesheetItemType.NODE, "Scene", false, el);
                }
            }
        } else {
            if (matcher == null) {
                item = new StylesheetItem(StylesheetItemType.NODE, ElementUtils.getTitle(el), false, el);
            } else {
                var id = el.getNodeProperties().id();
                var styleClasses = el.getNodeProperties().styleClass();
                if (matcher.reset(el.getClassInfo().simpleClassName()).find()
                        || (id != null && matcher.reset(id).find())
                        || (styleClasses != null && styleClasses.stream()
                                .filter(s -> matcher.reset(s).find()).anyMatch(e -> true))) {
                    item = new StylesheetItem(StylesheetItemType.NODE, ElementUtils.getTitle(el), false, el);
                }
            }
        }
        return item;
    }
}
