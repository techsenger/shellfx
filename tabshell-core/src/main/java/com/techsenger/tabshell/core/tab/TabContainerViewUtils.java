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

package com.techsenger.tabshell.core.tab;

import java.lang.ref.WeakReference;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;

/**
 * This class has logic that can't be added via default interface method because it can't be public.
 *
 * @author Pavel Castornii
 */
public final class TabContainerViewUtils {

    private static final Object TAB_KEY = new Object();

    /**
     * Tab can be closed via close button and via model. In both cases doOnCloseRequest is called.
     */
    public static void initTabPane(TabPane tabPane, TabContainerView<?> view) {
        tabPane.getTabs().addListener((ListChangeListener<? super Tab>) (change)  -> {
            while (change.next()) {
                if (change.wasAdded()) {
                    for (var t : change.getAddedSubList()) {
                        ComponentTab tab = (ComponentTab) t;
                        tab.setOnCloseRequest((e) -> {
                            view.closeTab(tab);
                            e.consume();
                        });
                        //tabs can be added only by one
                        tabPane.getSelectionModel().select(t);
                        tab.setContextMenu(createTabContextMenu((ObservableList) tabPane.getTabs(),
                                tab, view));
                    }
                } else if (change.wasRemoved()) {
                    for (var t : change.getRemoved()) {
                        t.setOnCloseRequest(null);
                        t.setContextMenu(null);
                    }
                }
            }
        });
    }

    private static ContextMenu createTabContextMenu(ObservableList<ComponentTab> tabs, ComponentTab tab,
            TabContainerView<?> view) {
        ContextMenu contextMenu = new ContextMenu();
        // we use a weak reference as a workaround for JDK-8283449
        contextMenu.getProperties().put(TAB_KEY, new WeakReference<ComponentTab>(tab));

        MenuItem close = new MenuItem("Close", new Label(" "));
        close.setOnAction((e) -> {
            var t = getTab(contextMenu);
            if (t != null) {
                view.closeTab(t);
            }
        });
        MenuItem closeAll = new MenuItem("Close All");
        closeAll.setOnAction((e) -> {
            view.closeAllTabs(tabs);
        });
        MenuItem closeOther = new MenuItem("Close Other");
        closeOther.setOnAction((e) -> {
            var t = getTab(contextMenu);
            if (t != null) {
                view.closeOtherTabs(tabs, t);
            }
        });
        MenuItem closeRight = new MenuItem("Close to the Right");
        closeRight.setOnAction((e) -> {
            var t = getTab(contextMenu);
            if (t != null) {
                view.closeRightTabs(tabs, t);
            }
        });
        MenuItem closeLeft = new MenuItem("Close to the Left");
        closeLeft.setOnAction((e) -> {
            var t = getTab(contextMenu);
            if (t != null) {
                view.closeLeftTabs(tabs, t);
            }
        });

        contextMenu.getItems().addAll(close, closeAll, closeOther, closeRight, closeLeft);
        contextMenu.setOnShowing((e) -> {
            if (tabs.size() == 0) {
                return;
            }
            if (tabs.size() == 1) {
                closeAll.setDisable(true);
                closeOther.setDisable(true);
                closeLeft.setDisable(true);
                closeRight.setDisable(true);
            } else {
                closeAll.setDisable(false);
                closeOther.setDisable(false);
                closeLeft.setDisable(false);
                closeRight.setDisable(false);
            }
            var t = getTab(contextMenu);
            if (t != null) {
                if (tabs.get(tabs.size() - 1) == t) {
                    closeRight.setDisable(true);
                } else {
                    closeRight.setDisable(false);
                }
                if (tabs.get(0) == t) {
                    closeLeft.setDisable(true);
                } else {
                    closeLeft.setDisable(false);
                }
            }
        });
        return contextMenu;
    }

    private static ComponentTab getTab(ContextMenu menu) {
        WeakReference<ComponentTab> ref = (WeakReference<ComponentTab>) menu.getProperties().get(TAB_KEY);
        return ref.get();
    }

    private TabContainerViewUtils() {
        //empty
    }
}
