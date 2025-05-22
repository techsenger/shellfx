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

import com.techsenger.tabshell.core.node.AbstractNodeView;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;

/**
 *
 * @author Pavel Castornii
 */
public class CaretView extends AbstractNodeView<CaretViewModel> {

    private RowView row;

    private final Rectangle bar = new Rectangle();

    private final Rectangle indicator = new Rectangle();

    private final Timeline timeline = new Timeline();

    CaretView(CaretViewModel viewModel) {
        super(viewModel);
    }

    @Override
    public Rectangle getNode() {
        return bar;
    }

    public Rectangle getIndicator() {
        return indicator;
    }

    @Override
    protected void build(CaretViewModel viewModel) {
        super.build(viewModel);
        this.bar.setWidth(1);
        this.bar.getStyleClass().add("caret");
        this.bar.setSmooth(true);
        this.bar.setManaged(false);

        this.indicator.getStyleClass().add("indicator");
        this.indicator.setHeight(10);
        this.indicator.setManaged(false);
        this.indicator.setSmooth(false);

        this.timeline.getKeyFrames().add(new KeyFrame(Duration.millis(500),
                e -> viewModel.setVisible(!viewModel.isVisible())));
        this.timeline.setCycleCount(Timeline.INDEFINITE);
    }

    @Override
    protected void bind(CaretViewModel viewModel) {
        super.bind(viewModel);
        this.bar.visibleProperty().bind(viewModel.visibleProperty());
    }

    @Override
    protected void addListeners(CaretViewModel viewModel) {
        super.addListeners(viewModel);
        viewModel.disabledProperty().addListener((ov, oldV, newV) -> {
            if (newV) {
                this.timeline.pause();
                viewModel.setVisible(false);
            } else {
                viewModel.setVisible(true);
                this.timeline.play();
            }
        });
    }

    @Override
    protected void preDeinitialize(CaretViewModel viewModel) {
        super.preDeinitialize(viewModel);
        if (!viewModel.isDisabled()) {
            this.timeline.stop();
        }
    }

    @Override
    public void requestFocus() {

    }

    RowView getRow() {
        return row;
    }

    void setRow(RowView row) {
        this.row = row;
    }
}
