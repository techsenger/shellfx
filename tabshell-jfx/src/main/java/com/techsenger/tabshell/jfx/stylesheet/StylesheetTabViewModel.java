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
import com.techsenger.tabshell.core.tab.AbstractTabViewModel;
import com.techsenger.tabshell.core.tab.TabMediator;
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
import java.util.regex.Pattern;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

/**
 *
 * @author Pavel Castornii
 */
public class StylesheetTabViewModel<T extends TabMediator> extends AbstractTabViewModel<T> {

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

    private final ReadOnlyObjectWrapper<StylesheetNode> root = new ReadOnlyObjectWrapper<>();

    private final StringProperty searchText = new SimpleStringProperty();

    private final BooleanProperty caseSensitive = new SimpleBooleanProperty(false);

    public StylesheetTabViewModel(Connector connector, int stageUid) {
        this.connector = connector;
        this.windowUid = stageUid;
    }

    public StylesheetNode getRoot() {
        return root.get();
    }

    public ReadOnlyObjectProperty<StylesheetNode> rootProperty() {
        return root.getReadOnlyProperty();
    }

    public String getSearchText() {
        return searchText.get();
    }

    public void setSearchText(String value) {
        searchText.set(value);
    }

    public StringProperty searchTextProperty() {
        return searchText;
    }

    public boolean isCaseSensitive() {
        return caseSensitive.get();
    }

    public void setCaseSensitive(boolean value) {
        caseSensitive.set(value);
    }

    public BooleanProperty caseSensitiveProperty() {
        return caseSensitive;
    }

    public void refresh() {
        rebuildTree();
    }

    @Override
    public CloseCheckResult canClose() {
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
        caseSensitive.addListener((ov, oldV, newV) -> {
            if (getSearchText() != null && !getSearchText().isBlank()) {
                rebuildTree();
            }
        });
        rebuildTree();
    }

    protected void rebuildTree() {
        var entry = connector.getStyledElements(windowUid);
        var root = new StylesheetNode("Application [" + connector.getUserAgentStylesheet() + "]");
        var window = new StylesheetNode(formatWindowType(windowUid, entry.getKey()));
        root.setChildren(List.of(window));
        List<StylesheetNode> nodes = new ArrayList<>();
        window.setChildren(nodes);

        Matcher matcher = null;
        if (getSearchText() != null && !getSearchText().isBlank()) {
             int flags = isCaseSensitive() ? Pattern.LITERAL
                                  : Pattern.CASE_INSENSITIVE | Pattern.LITERAL;
            var pattern = Pattern.compile(getSearchText().trim(), flags);
            matcher = pattern.matcher("");
        }

        var sceneStylesheets = entry.getKey().sceneStylesheets();
        if (sceneStylesheets != null && !sceneStylesheets.isEmpty()) {
            var node = filterAndCreateNode(null, entry.getKey().sceneStylesheets(), matcher);
            if (node != null) {
                nodes.add(node);
            }
        }

        for (var e : entry.getValue()) {
            var element = filterAndCreateNode(e, e.getNodeProperties().stylesheets(), matcher);
            if (element != null) {
                nodes.add(element);
            }
        }
        setRoot(root);
    }

    protected void setRoot(StylesheetNode item) {
        root.set(item);
    }

    protected StylesheetNode filterAndCreateNode(Element el, List<String> stylesheets, Matcher matcher) {
        StylesheetNode node = null;
        if (el == null) {
            if (matcher == null) {
                node = new StylesheetNode("Scene");
            } else {
                if (matcher.reset("Scene").find()) {
                    node = new StylesheetNode("Scene");
                }
            }
        } else {
            if (matcher == null) {
                node = new StylesheetNode(ElementUtils.getTitle(el));
            } else {
                var id = el.getNodeProperties().id();
                var styleClasses = el.getNodeProperties().styleClass();
                if (matcher.reset(el.getClassInfo().simpleClassName()).find()
                        || (id != null && matcher.reset(id).find())
                        || (styleClasses != null && styleClasses.stream()
                                .filter(s -> matcher.reset(s).find()).anyMatch(e -> true))) {
                    node = new StylesheetNode(ElementUtils.getTitle(el));
                }
            }
        }

        if (node != null && stylesheets != null) {
            node.setStylesheets(stylesheets);
        }
        return node;
    }
}
