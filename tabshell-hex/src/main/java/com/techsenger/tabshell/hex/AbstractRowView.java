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

package com.techsenger.tabshell.hex;

import com.techsenger.tabshell.core.pane.AbstractPaneView;
import com.techsenger.tabshell.core.style.StyleClasses;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;

/**
 *
 * @author Pavel Castornii
 */
abstract class AbstractRowView<T extends AbstractRowViewModel> extends AbstractPaneView<T> {

    private final AbstractHexEditorTabView<?> editor;

    /**
     * This flow has only one text node.
     */
    private final Label infoLabel = new Label();

    /**
     * Stack pane for hex panel.
     */
    private final PanelRowPane hexPane = new PanelRowPane();

    /**
     * Stack pane for ascii panel.
     */
    private final PanelRowPane asciiPane = new PanelRowPane();

    /**
     * The root node of the row.
     */
    private final HBox node = new HBox(infoLabel, hexPane, asciiPane);

    AbstractRowView(T viewModel, AbstractHexEditorTabView<?> editor) {
        super(viewModel);
        this.editor = editor;
    }

    @Override
    public HBox getNode() {
        return node;
    }

    @Override
    public void requestFocus() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public abstract void rebuild();

    @Override
    protected void build(T viewModel) {
        super.build(viewModel);
        this.infoLabel.setMinWidth(Region.USE_PREF_SIZE);
        this.infoLabel.getStyleClass().add("info-label");
        this.hexPane.getStyleClass().add("hex-pane");
        this.asciiPane.getStyleClass().add("ascii-pane");
        this.node.getStyleClass().addAll(StyleClasses.MONOSPACE);
    }

    protected Label getInfoLabel() {
        return infoLabel;
    }

    protected PanelRowPane getHexPane() {
        return hexPane;
    }

    protected PanelRowPane getAsciiPane() {
        return asciiPane;
    }

    AbstractHexEditorTabView<?> getEditor() {
        return editor;
    }
}
