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

package com.techsenger.shellfx.material.tabpane;

import atlantafx.base.theme.Styles;
import com.techsenger.tabpanepro.core.TabPanePro;
import com.techsenger.tabpanepro.core.skin.DragAndDropUtils;
import com.techsenger.tabpanepro.core.skin.TabPaneProSkin;
import javafx.geometry.Side;
import javafx.scene.Parent;
import javafx.scene.control.TabPane;

/**
 * A {@link TabPanePro} whose tabs are slanted, with a more saturated tab header. It is meant for the primary content
 * area of browser-like applications. The shape of the tabs is set only by CSS; their size is the size of the regular
 * tabs.
 *
 * @author Pavel Castornii
 */
public class SlantedTabPanePro extends TabPanePro {

    private static final String STYLE_CLASS = "slanted";

    private double tabGap;

    public SlantedTabPanePro() {
        getStyleClass().addAll(STYLE_CLASS, Styles.DENSE);
        var css = SlantedTabPanePro.class.getResource("slanted-tab-pane-pro.css").toExternalForm();
        getStylesheets().add(css);
        setTabClosingPolicy(TabPane.TabClosingPolicy.SELECTED_TAB);
        var tabHeaderArea = ((TabPaneProSkin) getSkin()).getTabHeaderArea();
        tabHeaderArea.setTabHeaderFactory(c -> new SlantedTabHeaderSkin(c, this::updateTabGap));
        tabHeaderArea.setTabDragContentFactory(h -> {
            var content = DragAndDropUtils.createTabDragContent(h);
            // the preview is not a child of the tab pane, so it gets the style sheet itself
            if (content instanceof Parent parent) {
                parent.getStylesheets().add(css);
            }
            return content;
        });
        // right corner is on top
        tabHeaderArea.setTabViewOrderResolver((tabHeader, index, tabCount, selected) -> {
            if (selected) {
                return tabCount * -1.0;
            } else {
                return (tabCount - 1 - index) * -1.0;
            }
        });
        sideProperty().addListener((ov, oldV, newV) -> updateTabGap(tabGap));
    }

    /**
     * Applies the gap between the tabs to the layout and moves the drop indicator into the middle of the gap, which
     * is where the notch between two slanted tabs is. The tabs are laid out in the opposite direction on the bottom
     * and left sides, so the shift changes its sign there.
     */
    private void updateTabGap(double gap) {
        this.tabGap = gap;
        var tabHeaderArea = ((TabPaneProSkin) getSkin()).getTabHeaderArea();
        tabHeaderArea.setTabGap(gap);
        var shift = gap / 2;
        if (getSide() == Side.BOTTOM || getSide() == Side.LEFT) {
            shift *= -1;
        }
        tabHeaderArea.getTabDropPosition().setOffset(shift);
    }
}
