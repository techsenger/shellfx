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
import com.techsenger.tabshell.hex.row.BodyRowView;
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

    private final AbstractHexEditorTabView<?> editor;

    private BodyRowView row;

    CaretView(AbstractHexEditorTabView<?> editor, CaretViewModel viewModel) {
        super(viewModel);
        this.editor = editor;
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
     * @param row the target row, or {@code null} if the caret remains on the same row
     */
    //todo: package level
    public void move(BodyRowView row) {
        var viewModel = getViewModel();

        if (row == null) {
            row = this.row;
        }
        viewModel.setRow(row.getViewModel());
        var oldRow = this.row;
        this.row = row;

        //when file is opened the position of the caret is calculated by char width as there can be no bytes
        if (viewModel.getByteIndex() == 0 && viewModel.getBytePosition() == CaretBytePosition.FIRST
                && viewModel.getRowIndex() == 0) {
            var charWidth = this.editor.getViewModel().getCharWidth();
            viewModel.setX(charWidth);
            viewModel.setIndicatorX(charWidth);
            return;
        }
        var bytePair = this.row.getByteTextPairs().get(viewModel.getByteIndex());
        if (viewModel.getPanel() == EditorPanel.HEX) {
            //caret
            var text = bytePair.getHexText();
            switch (viewModel.getBytePosition()) {
                case FIRST:
                    viewModel.setX(text.getBoundsInParent().getMinX());
                    break;
                case SECOND:
                    double textWidth = text.getLayoutBounds().getWidth();
                    double widthHalf = textWidth / 2;
                    viewModel.setX(text.getBoundsInParent().getMinX() + widthHalf);
                    break;
                case THIRD:
                    viewModel.setX(text.getBoundsInParent().getMaxX());
                    break;
                default:
                    throw new AssertionError();
            }
            //indicator
            text = bytePair.getAsciiText();
            viewModel.setIndicatorX(text.getBoundsInParent().getMinX());
        } else {
            //caret
            var text = bytePair.getAsciiText();
            if (viewModel.getBytePosition() == CaretBytePosition.THIRD) {
                viewModel.setX(text.getBoundsInParent().getMaxX());
            } else {
                viewModel.setX(text.getBoundsInParent().getMinX());
            }
            //indicator
            text = bytePair.getHexText();
            viewModel.setIndicatorX(text.getBoundsInParent().getMinX());
        }
        updateRow(oldRow, this.row);
    }

    private void updateRow(BodyRowView oldRow, BodyRowView newRow) {
        if (oldRow != newRow) {
            if (oldRow != null) {
                oldRow.removeCaret();
                oldRow.getViewModel().setFocused(false);
            }
            newRow.getViewModel().setFocused(true);
        }
        // The caret is added to the row in two cases: when this method is called and when row.updateItem(..) is called.
        // That's why we always remove the caret first to avoid a 'duplicate children added' exception.
        newRow.removeCaret();
        newRow.addCaret();
        //when cursor is moved it must always be visible
        if (!getViewModel().isDisabled()) {
            this.caret.setVisible(true);
        }
    }
}
