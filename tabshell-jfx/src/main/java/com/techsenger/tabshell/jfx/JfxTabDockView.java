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

package com.techsenger.tabshell.jfx;

import atlantafx.base.theme.Styles;
import com.techsenger.tabshell.jfx.style.JfxIcons;
import com.techsenger.tabshell.layout.dock.TabDockView;
import com.techsenger.tabshell.material.icon.FontIconView;
import javafx.scene.control.Button;

/**
 *
 * @author Pavel Castornii
 */
public class JfxTabDockView<T extends JfxTabDockViewModel<?>, S extends JfxTabDockComponent<?>>
        extends TabDockView<T, S> {

    private final Button selectButton = new Button(null, new FontIconView(JfxIcons.SELECT));

    public JfxTabDockView(T viewModel) {
        super(viewModel);
    }

    @Override
    protected void build() {
        super.build();
        selectButton.getStyleClass().addAll(Styles.FLAT, "select-button");
        getTabHeaderFirstBox().getChildren().add(selectButton);
        var styles = JfxTabDockView.class.getResource("jfx-tab-dock.css").toExternalForm();
        getNode().getStylesheets().add(styles);
        getNode().setTabDragEnabled(false);
        getNode().setTabDropEnabled(false);
    }

    @Override
    protected void addHandlers() {
        super.addHandlers();
        selectButton.setOnAction(e -> getViewModel().updateInspectMode());
    }
}
