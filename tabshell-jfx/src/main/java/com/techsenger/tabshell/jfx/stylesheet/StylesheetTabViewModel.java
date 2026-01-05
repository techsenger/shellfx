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

package com.techsenger.tabshell.jfx.stylesheet;

import com.techsenger.tabshell.core.CloseCheckResult;
import com.techsenger.tabshell.core.ClosePreparationResult;
import com.techsenger.tabshell.core.tab.AbstractTabViewModel;
import com.techsenger.tabshell.jfx.ElementUtils;
import devtoolsfx.connector.Connector;
import devtoolsfx.scenegraph.Element;
import devtoolsfx.scenegraph.WindowProperties;
import static devtoolsfx.scenegraph.WindowProperties.WindowType.ALERT;
import static devtoolsfx.scenegraph.WindowProperties.WindowType.MODAL;
import static devtoolsfx.scenegraph.WindowProperties.WindowType.POPUP;
import static devtoolsfx.scenegraph.WindowProperties.WindowType.STAGE;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

/**
 *
 * @author Pavel Castornii
 */
public class StylesheetTabViewModel<T extends StylesheetTabMediator> extends AbstractTabViewModel<T> {

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

    private final Connector connector;

    private final int windowUid;

    /**
    * Flat list of items representing a tree structure. The hierarchy is encoded via {@link StylesheetItem#depth()}
    * and the actual TreeItems are rebuilt in the View on each refresh.
    */
    private final ObservableList<StylesheetItem> items = FXCollections.observableArrayList();

    public StylesheetTabViewModel(Connector connector, int stageUid) {
        this.connector = connector;
        this.windowUid = stageUid;
    }

    public void refresh() {
        rebuildTree();
    }

    @Override
    public CloseCheckResult isReadyToClose() {
        return CloseCheckResult.READY;
    }

    @Override
    public void prepareToClose(Consumer<ClosePreparationResult> resultCallback) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    protected Connector getConnector() {
        return connector;
    }

    protected int getWindowUid() {
        return windowUid;
    }

    @Override
    protected void initialize() {
        super.initialize();
        setTitle("Stylesheets");
        setClosable(false);
        var searchPanel = getMediator().getSearchPanel();
        searchPanel.caseSensitiveProperty().addListener((ov, oldV, newV) -> {
            if (searchPanel.getSearchText() != null && !searchPanel.getSearchText().isBlank()) {
                rebuildTree();
            }
        });
        rebuildTree();
    }

    protected void rebuildTree() {
        var entry = connector.getStyledElements(windowUid);
        Matcher matcher = getMediator().getSearchPanel().createMatcher();

        List<StylesheetItem> tempItems = new ArrayList<>();
        var item = new StylesheetItem(StylesheetItem.APPLICATION_DEPTH,
                "Application [" + connector.getUserAgentStylesheet() + "]", true);
        tempItems.add(item);
        item = new StylesheetItem(StylesheetItem.WINDOW_DEPTH, formatWindowType(windowUid, entry.getKey()), true);
        tempItems.add(item);

        var sceneStylesheets = entry.getKey().sceneStylesheets();
        if (sceneStylesheets != null && !sceneStylesheets.isEmpty()) {
            item = filterAndCreateItem(null, matcher);
            if (item != null) {
                tempItems.add(item);
                for (var s : sceneStylesheets) {
                    tempItems.add(new StylesheetItem(3, s, false));
                }
            }
        }

        for (var e : entry.getValue()) {
            item = filterAndCreateItem(e, matcher);
            if (item != null) {
                tempItems.add(item);
                for (var s : e.getNodeProperties().stylesheets()) {
                    tempItems.add(new StylesheetItem(3, s, false));
                }
            }
        }
        this.items.clear();
        this.items.addAll(tempItems);
    }

    protected StylesheetItem filterAndCreateItem(Element el, Matcher matcher) {
        StylesheetItem item = null;
        if (el == null) {
            if (matcher == null) {
                item = new StylesheetItem(StylesheetItem.NODE_DEPTH, "Scene", false);
            } else {
                if (matcher.reset("Scene").find()) {
                    item = new StylesheetItem(StylesheetItem.NODE_DEPTH, "Scene", false);
                }
            }
        } else {
            if (matcher == null) {
                item = new StylesheetItem(StylesheetItem.NODE_DEPTH, ElementUtils.getTitle(el), false);
            } else {
                var id = el.getNodeProperties().id();
                var styleClasses = el.getNodeProperties().styleClass();
                if (matcher.reset(el.getClassInfo().simpleClassName()).find()
                        || (id != null && matcher.reset(id).find())
                        || (styleClasses != null && styleClasses.stream()
                                .filter(s -> matcher.reset(s).find()).anyMatch(e -> true))) {
                    item = new StylesheetItem(StylesheetItem.NODE_DEPTH, ElementUtils.getTitle(el), false);
                }
            }
        }
        return item;
    }

    protected ObservableList<StylesheetItem> getItems() {
        return items;
    }
}
