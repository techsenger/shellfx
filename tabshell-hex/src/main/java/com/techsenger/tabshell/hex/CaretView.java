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

import com.techsenger.tabshell.core.node.AbstractNodeView;
import static com.techsenger.tabshell.hex.CaretShape.BAR;
import static com.techsenger.tabshell.hex.CaretShape.BLOCK;
import static com.techsenger.tabshell.hex.CaretShape.UNDERSCORE;
import com.techsenger.toolkit.fx.value.ValueUtils;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;

/**
 * The caret is responsible only for rendering and performing movement. The coordinates and movement commands
 * are determined by the editor and sometimes a row.
 *
 * @author Pavel Castornii
 */
public final class CaretView extends AbstractNodeView<CaretViewModel> {

    /**
     * Region is not use used because it can't always have 1 px width, but we don't need as the height is set via timer.
     */
    private final Rectangle caret = new Rectangle();

    private final Rectangle indicator = new Rectangle();

    private final Timeline timeline = new Timeline();

    private BodyRowView row;

    CaretView(CaretViewModel viewModel) {
        super(viewModel);
    }

    @Override
    public Rectangle getNode() {
        return caret;
    }

    public Rectangle getIndicator() {
        return indicator;
    }

    @Override
    public void requestFocus() {

    }

    @Override
    protected void build(CaretViewModel viewModel) {
        super.build(viewModel);
        this.caret.getStyleClass().add("caret");
        this.caret.setSmooth(false);
        this.caret.setManaged(false);
        this.indicator.getStyleClass().add("indicator");
        this.indicator.setSmooth(false);
        this.indicator.setManaged(false);
        this.timeline.setCycleCount(Timeline.INDEFINITE);
    }

    @Override
    protected void bind(CaretViewModel viewModel) {
        super.bind(viewModel);
        this.caret.widthProperty().bind(viewModel.widthProperty());
        this.indicator.widthProperty().bind(viewModel.indicatorWidthProperty());
        this.caret.translateXProperty().bind(viewModel.xProperty());
        this.indicator.translateXProperty().bind(viewModel.indicatorXProperty());
    }

    @Override
    protected void addHandlers(CaretViewModel viewModel) {
        super.addHandlers(viewModel);
        this.timeline.getKeyFrames().add(new KeyFrame(Duration.millis(500), e -> {
            this.caret.setVisible(!this.caret.isVisible());
            if (this.row != null && this.caret.isVisible()) {
                //setting caret width and height, for example, when font changes,
                //while we can calculate char width we can't calculate row width
                switch (viewModel.getShape()) {
                    case BAR:
                        this.caret.setHeight(this.row.getNode().getHeight());
                        break;
                    case BLOCK:
                        this.caret.setHeight(this.row.getNode().getHeight());
                        break;
                    case UNDERSCORE:
                        this.caret.setTranslateY(this.row.getNode().getHeight() - 1);
                        break;
                    default:
                        throw new AssertionError();
                }

                //setting indicator width, height, x
                this.indicator.setHeight(this.row.getNode().getHeight());
            }
        }));
    }

    @Override
    protected void addListeners(CaretViewModel viewModel) {
        super.addListeners(viewModel);
        viewModel.disabledProperty().addListener((ov, oldV, newV) -> {
            if (newV) {
                this.timeline.pause();
                this.caret.setVisible(false);
                this.indicator.setVisible(false);
            } else {
                this.caret.setVisible(true);
                this.indicator.setVisible(true);
                this.timeline.play();
            }
        });
        ValueUtils.callAndAddListener(viewModel.shapeProperty(), (ov, oldV, newV) -> {
                this.caret.getStyleClass().clear();
            switch (newV) {
                case BAR:
                    VBox.setVgrow(caret, Priority.ALWAYS);
                    this.caret.setTranslateY(0);
                    this.caret.getStyleClass().addAll("caret", "bar");
                    break;
                case BLOCK:
                    this.caret.setTranslateY(0);
                    this.caret.getStyleClass().addAll("caret", "block");
                    break;
                case UNDERSCORE:
                    this.caret.setHeight(1);
                    this.caret.getStyleClass().addAll("caret", "underscore");
                    break;
                default:
                    throw new AssertionError();
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

    BodyRowView getRow() {
        return row;
    }

    /**
     * Moves the caret to a new position in the view, based on the model state.
     *
     * @param position the new position of the caret
     * @param newRow the target row, or {@code null} if the caret remains on the same row
     */
    void moveTo(CaretPosition position, BodyRowView newRow) {
        var viewModel = getViewModel();
        //if the position is null, it indicates that the caret has not moved,
        //and only the view's caret needs to be updated.
        if (position == null) {
            position = viewModel.getPosition();
        }
        
        if (newRow == null) {
            newRow = this.row;
        }
        viewModel.setRow(newRow.getViewModel());
        var oldRow = this.row;
        this.row = newRow;

        if (oldRow != newRow) {
            if (oldRow != null) {
                oldRow.removeCaret();
                oldRow.getViewModel().setFocused(false);
            }
        }
        // The caret is added to the row in two cases: when this method is called and when row.updateItem(..) is called.
        // That's why we always remove the caret first to avoid a 'duplicate children added' exception.
        newRow.removeCaret();
        newRow.addCaret(position);
        newRow.getViewModel().setFocused(true);
        //when cursor is moved it must always be visible
        if (!getViewModel().isDisabled()) {
            this.caret.setVisible(true);
        }
        //and only now we change position
        viewModel.setPosition(position);
    }
}
