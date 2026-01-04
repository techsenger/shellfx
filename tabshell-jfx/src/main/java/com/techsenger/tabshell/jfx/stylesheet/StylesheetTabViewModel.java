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

package com.techsenger.tabshell.jfx.stylesheet;

import com.techsenger.tabshell.core.CloseCheckResult;
import com.techsenger.tabshell.core.ClosePreparationResult;
import com.techsenger.tabshell.core.tab.TabMediator;
import com.techsenger.tabshell.jfx.AbstractSearchableTabViewModel;
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
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;

/**
 *
 * @author Pavel Castornii
 */
public class StylesheetTabViewModel<T extends TabMediator> extends AbstractSearchableTabViewModel<T> {

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

    private final ReadOnlyObjectWrapper<StylesheetDataItem> root = new ReadOnlyObjectWrapper<>();

    public StylesheetTabViewModel(Connector connector, int stageUid) {
        this.connector = connector;
        this.windowUid = stageUid;
    }

    public StylesheetDataItem getRoot() {
        return root.get();
    }

    public ReadOnlyObjectProperty<StylesheetDataItem> rootProperty() {
        return root.getReadOnlyProperty();
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
        caseSensitiveProperty().addListener((ov, oldV, newV) -> {
            if (getSearchText() != null && !getSearchText().isBlank()) {
                rebuildTree();
            }
        });
        rebuildTree();
    }

    protected void rebuildTree() {
        var entry = connector.getStyledElements(windowUid);
        var root = new StylesheetDataItem("Application [" + connector.getUserAgentStylesheet() + "]");
        var window = new StylesheetDataItem(formatWindowType(windowUid, entry.getKey()));
        root.setChildren(List.of(window));
        List<StylesheetDataItem> items = new ArrayList<>();
        window.setChildren(items);

        Matcher matcher = createMatcher();

        var sceneStylesheets = entry.getKey().sceneStylesheets();
        if (sceneStylesheets != null && !sceneStylesheets.isEmpty()) {
            var item = filterAndCreateItem(null, entry.getKey().sceneStylesheets(), matcher);
            if (item != null) {
                items.add(item);
            }
        }

        for (var e : entry.getValue()) {
            var element = filterAndCreateItem(e, e.getNodeProperties().stylesheets(), matcher);
            if (element != null) {
                items.add(element);
            }
        }
        setRoot(root);
    }

    protected void setRoot(StylesheetDataItem item) {
        root.set(item);
    }

    protected StylesheetDataItem filterAndCreateItem(Element el, List<String> stylesheets, Matcher matcher) {
        StylesheetDataItem item = null;
        if (el == null) {
            if (matcher == null) {
                item = new StylesheetDataItem("Scene");
            } else {
                if (matcher.reset("Scene").find()) {
                    item = new StylesheetDataItem("Scene");
                }
            }
        } else {
            if (matcher == null) {
                item = new StylesheetDataItem(ElementUtils.getTitle(el));
            } else {
                var id = el.getNodeProperties().id();
                var styleClasses = el.getNodeProperties().styleClass();
                if (matcher.reset(el.getClassInfo().simpleClassName()).find()
                        || (id != null && matcher.reset(id).find())
                        || (styleClasses != null && styleClasses.stream()
                                .filter(s -> matcher.reset(s).find()).anyMatch(e -> true))) {
                    item = new StylesheetDataItem(ElementUtils.getTitle(el));
                }
            }
        }

        if (item != null && stylesheets != null) {
            item.setStylesheets(stylesheets);
        }
        return item;
    }
}
