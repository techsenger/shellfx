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

package com.techsenger.tabshell.hex.editor;

import javafx.scene.canvas.Canvas;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;

/**
 *
 * @author Pavel Castornii
 */
class PanelRowPane extends StackPane {

    /**
     * This canvas is used for background color, selection etc.
     */
    private final Canvas canvas = new Canvas();

    /**
     * For caret and its indicator one pane is used.
     */
    private final Pane caretPane = new Pane();

    /**
     * This box contains text nodes (with spaces and with byte values) and lines.
     */
    private final HBox contentBox = new HBox();

    PanelRowPane() {
        this.getChildren().addAll(this.canvas, this.caretPane, this.contentBox);
        this.caretPane.setMouseTransparent(true);
        this.caretPane.getStyleClass().add("caret-pane");
        this.contentBox.getStyleClass().add("content-box");
    }

    @Override
    protected void layoutChildren() {
        super.layoutChildren();
        this.canvas.setWidth(getWidth());
        this.canvas.setHeight(getHeight());
    }

    public Canvas getCanvas() {
        return canvas;
    }

    public Pane getCaretPane() {
        return caretPane;
    }

    public HBox getContentBox() {
        return contentBox;
    }

    void clearCanvas() {
        var gc = this.canvas.getGraphicsContext2D();
        gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
    }

    void clear() {
        clearCanvas();
        this.caretPane.getChildren().clear();
        this.contentBox.getChildren().clear();
    }
}
