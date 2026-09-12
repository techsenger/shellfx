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

package com.techsenger.shellfx.layout.tabhost;

import atlantafx.base.theme.Styles;
import com.techsenger.shellfx.material.style.StyleClasses;
import com.techsenger.toolkit.fx.value.ValueUtils;
import javafx.scene.control.TabPane;
import javafx.scene.text.Font;

/**
 * A {@link TabHostView} that stands out among other tab hosts through larger tabs and a more prominent tab header,
 * intended as the primary content area of browser-like applications.
 *
 * @author Pavel Castornii
 */
public class ProminentTabHostView<VM extends ProminentTabHostViewModel<?>> extends TabHostView<VM> {

    public ProminentTabHostView(VM viewModel) {
        super(viewModel);
    }

    @Override
    protected void build() {
        super.build();
        getNode().getStylesheets().add(
                ProminentTabHostView.class.getResource("prominent-tab-host.css").toExternalForm());
        getNode().setTabClosingPolicy(TabPane.TabClosingPolicy.SELECTED_TAB);
        getNode().getStyleClass().addAll(StyleClasses.PROMINENT, Styles.DENSE);
        var tabHeaderArea = getTabHeaderArea();
        tabHeaderArea.setTabHeaderFactory(c -> {
            var header = new SlantedTabHeaderSkin(c);
            header.getProperties().put(TAB_KEY, c.getTab());
            return header;
        });
        tabHeaderArea.setTabGap(-10.0);
        // right corner is on top
        tabHeaderArea.setTabViewOrderResolver((tabHeader, index, tabCount, selected) -> {
            if (selected) {
                return tabCount * -1.0;
            } else {
                return (tabCount - 1 - index) * -1.0;
            }
        });
    }

    @Override
    protected void addListeners() {
        super.addListeners();
        var viewModel = getViewModel();
        ValueUtils.callAndAddListener(viewModel.regularFontProperty(), (ov, oldV, newV) -> updateRegularFont(newV));
    }

    protected void updateRegularFont(Font font) {
        getNode().setTabMaxWidth(font.getSize() * 15);
    }
}
