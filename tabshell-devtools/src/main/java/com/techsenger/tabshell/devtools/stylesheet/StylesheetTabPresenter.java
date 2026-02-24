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

package com.techsenger.tabshell.devtools.stylesheet;

import com.techsenger.connectorfx.Connector;
import com.techsenger.connectorfx.scenegraph.Element;
import com.techsenger.connectorfx.scenegraph.WindowProperties;
import static com.techsenger.connectorfx.scenegraph.WindowProperties.WindowType.ALERT;
import static com.techsenger.connectorfx.scenegraph.WindowProperties.WindowType.MODAL;
import static com.techsenger.connectorfx.scenegraph.WindowProperties.WindowType.POPUP;
import static com.techsenger.connectorfx.scenegraph.WindowProperties.WindowType.STAGE;
import com.techsenger.patternfx.mvp.Descriptor;
import com.techsenger.tabshell.core.AddablePresenter;
import com.techsenger.tabshell.core.CloseCheckResult;
import com.techsenger.tabshell.core.ClosePreparationResult;
import com.techsenger.tabshell.core.tab.AbstractTabPresenter;
import com.techsenger.tabshell.devtools.DevToolsComponents;
import com.techsenger.tabshell.devtools.DevToolsTabDockPort;
import com.techsenger.tabshell.devtools.ElementUtils;
import com.techsenger.tabshell.devtools.ToolBarAwarePort;
import com.techsenger.tabshell.devtools.node.NodeTabPort;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.regex.Matcher;

/**
 *
 * @author Pavel Castornii
 */
public class StylesheetTabPresenter<V extends StylesheetTabView, C extends StylesheetTabComposer>
        extends AbstractTabPresenter<V, C> implements AddablePresenter {

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
            rebuildTree();
        }

        @Override
        public void onFindCleared() {
            rebuildTree();
        }
    }

    private final Connector connector;

    private final DevToolsTabDockPort dock;

    private final NodeTabPort nodeTab;

    public StylesheetTabPresenter(V view, Connector connector, DevToolsTabDockPort dock, NodeTabPort nodeTab) {
        super(view);
        this.connector = connector;
        this.dock = dock;
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

    public void refresh() {
        rebuildTree();
    }

    @Override
    public void onAdded() {
        rebuildTree();
    }

    @Override
    protected Descriptor createDescriptor() {
        return new Descriptor(DevToolsComponents.STYLESHEET_TAB);
    }

    protected Connector getConnector() {
        return connector;
    }

    protected void onStylesheetSelected(StylesheetItem s) {
        if (s.type() == StylesheetItemType.APPLICATION || s.type() == StylesheetItemType.STYLESHEET) {
            return;
        }
        if (s.node() != null) {
            nodeTab.selectNode(s.node());
        } else {
            nodeTab.selectRoot();
        }
    }

    protected void rebuildTree() {
        var entry = connector.getStyledElements(dock.getWindowUid());
        Matcher matcher = getComposer().getToolBar().createFindMatcher();

        List<StylesheetItem> items = new ArrayList<>();
        var item = new StylesheetItem(StylesheetItemType.APPLICATION,
                "Application [" + connector.getUserAgentStylesheet() + "]", true, null);
        items.add(item);
        item = new StylesheetItem(StylesheetItemType.WINDOW,
                formatWindowType(dock.getWindowUid(), entry.getKey()), true, null);
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
        if (matcher != null) {
            getComposer().getToolBar().showFindResultInfo(found);
        } else {
            getComposer().getToolBar().hideFindResultInfo();
        }
        getView().setItems(items);
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
