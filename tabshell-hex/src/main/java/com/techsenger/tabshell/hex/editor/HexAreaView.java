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

import com.techsenger.tabshell.core.area.AbstractAreaView;
import static com.techsenger.tabshell.hex.editor.CaretByteLocation.FIRST;
import static com.techsenger.tabshell.hex.editor.CaretByteLocation.SECOND;
import static com.techsenger.tabshell.hex.editor.CaretByteLocation.THIRD;
import com.techsenger.tabshell.hex.model.ByteRange;
import com.techsenger.tabshell.material.style.StyleClasses;
import com.techsenger.toolkit.fx.pulse.LayoutPhase;
import com.techsenger.toolkit.fx.utils.NodeUtils;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import static javafx.scene.input.KeyCode.DOWN;
import static javafx.scene.input.KeyCode.END;
import static javafx.scene.input.KeyCode.HOME;
import static javafx.scene.input.KeyCode.LEFT;
import static javafx.scene.input.KeyCode.PAGE_DOWN;
import static javafx.scene.input.KeyCode.PAGE_UP;
import static javafx.scene.input.KeyCode.RIGHT;
import static javafx.scene.input.KeyCode.UP;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseDragEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import org.fxmisc.flowless.Cell;
import org.fxmisc.flowless.VirtualFlow;
import org.fxmisc.flowless.VirtualizedScrollPane;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The hex editor uses JavaFX nodes instead of rendering text directly on a canvas to achieve a balance between
 * performance, flexibility, and maintainability. By leveraging Text nodes inside a virtualized flow, only the visible
 * rows and bytes are created and rendered at any given time, which keeps memory usage and layout computation efficient
 * even for large files. This node-based approach allows the editor to remain responsive while preserving the benefits
 * of JavaFX’s scene graph and hardware acceleration.
 *
 * <p>Using nodes also enables full integration with JavaFX styling and layout mechanisms. Each byte is represented as
 * an independent Text element, which allows fine-grained control through CSS—such as coloring, spacing, and font
 * adjustments—without manual coordinate calculations. This makes it possible to highlight selections, draw the caret,
 * and apply visual effects in a declarative and consistent way.
 *
 * <p>Finally, a node-based architecture maintains clean separation between view and logic layers, aligning with the
 * MVVM pattern used throughout the application. Unlike a canvas-based approach that would require manual hit testing
 * and repainting for every interaction, the current design allows for clear data binding, modular updates, and
 * seamless scalability as features evolve.
 *
 * <p>The node-based rendering model is efficient enough for a hex editor of this scale. Each visible row in the
 * virtualized flow contains both hexadecimal and ASCII text representations—typically up to 64 bytes per row, which
 * corresponds to 128 Text nodes (two per byte). Even on a large monitor that can display around 100 visible rows,
 * the total number of active nodes is about 12,800. This number is well within JavaFX’s optimal rendering range.
 * Modern JavaFX versions easily handle between 10,000 and 20,000 nodes at 60 FPS when GPU acceleration is enabled.
 * Since the editor uses VirtualFlow, only visible rows are kept in memory, and off-screen rows are reused rather
 * than recreated. In practical terms, this means the scene graph remains lightweight, layout passes are bounded,
 * and scrolling performance stays smooth even with maximum density.
 *
 * <p>Therefore, the node-based approach not only preserves clarity and styling flexibility but also stays comfortably
 * within JavaFX’s performance limits, ensuring consistent rendering speed and responsiveness for realistic file
 * sizes and monitor resolutions.
 *
 * @author Pavel Castornii
 */
public class HexAreaView<T extends HexAreaViewModel<?>, S extends HexAreaComponent<?>> extends AbstractAreaView<T, S> {

    /**
     * This class represents the text for a single byte. We use {@link Text} instead of {@link Label} because the
     * latter is significantly slower.
     *
     * @author Pavel Castornii
     */
    private static class ByteText extends Text {

        private int index;

        ByteText() {
            setSmooth(false);
        }

        public int getIndex() {
            return index;
        }

        public void setIndex(int index) {
            this.index = index;
        }
    }

    private static class RowPane extends StackPane {

        private final EditorPanel panel;

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

        /**
         * Only texts that represent bytes. Its size is always equal to max rowByteCount.
         */
        private final List<ByteText> texts = new ArrayList<>();

        RowPane(EditorPanel panel) {
            this.panel = panel;
            this.getChildren().addAll(this.canvas, this.caretPane, this.contentBox);
            this.caretPane.setMouseTransparent(true);
            this.caretPane.getStyleClass().add("caret-pane");
            this.contentBox.getStyleClass().add("content-box");
        }

        public EditorPanel getPanel() {
            return panel;
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

        public List<ByteText> getTexts() {
            return texts;
        }

        @Override
        protected void layoutChildren() {
            super.layoutChildren();
            this.canvas.setWidth(getWidth());
            this.canvas.setHeight(getHeight());
        }

        void clearCanvas() {
            var gc = this.canvas.getGraphicsContext2D();
            gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
        }

        void clear() {
            this.texts.clear();
            this.caretPane.getChildren().clear();
            this.contentBox.getChildren().clear();
        }
    }

    private abstract static class AbstractRow {

        private final HexAreaView<?, ?> area;

        /**
         * This flow has only one text node.
         */
        private final Label infoLabel = new Label();

        /**
         * Stack pane for hex panel.
         */
        private final RowPane hexPane = new RowPane(EditorPanel.HEX);

        /**
         * Stack pane for ascii panel.
         */
        private final RowPane asciiPane = new RowPane(EditorPanel.ASCII);

        /**
         * The root node of the row.
         */
        private final HBox node = new HBox(infoLabel, hexPane, asciiPane);

        AbstractRow(HexAreaView<?, ?> area) {
            this.area = area;
            this.infoLabel.setMinWidth(Region.USE_PREF_SIZE);
            this.infoLabel.getStyleClass().add("info-label");
            this.hexPane.getStyleClass().add("hex-pane");
            this.asciiPane.getStyleClass().add("ascii-pane");
            this.node.getStyleClass().addAll(StyleClasses.MONOSPACE);
        }

        public HBox getNode() {
            return node;
        }

        public abstract void rebuild();

        protected Label getInfoLabel() {
            return infoLabel;
        }

        protected RowPane getHexPane() {
            return hexPane;
        }

        protected RowPane getAsciiPane() {
            return asciiPane;
        }

        HexAreaView<?, ?> getArea() {
            return area;
        }
    }

    private static class BodyRow extends AbstractRow implements Cell<Integer, Node> {

        private static final double SELECTION_BORDER_SIZE = 1.0;

        private RowData data;

        private boolean focused;

        BodyRow(RowData data, HexAreaView<?, ?> area) {
            super(area);
            this.data = data;
            getNode().getStyleClass().addAll("body-row", StyleClasses.MONOSPACE);
            addRowSelectionHandlers();
            rebuild();
            updateItem(this.data.getOffset());
        }

        /**
         * Due to virtualization and for performance reasons, we do not bind the values of text nodes to the model,
         * but instead set the values explicitly.
         */
        @Override
        public void updateItem(Integer offset) {
            var areaVM = getArea().getViewModel();
            var row = areaVM.createRowData(offset);
            this.data = row;
            removeCaret();
            if (offset != null) {
                getNode().setVisible(true);
                var offsetStr = NumberBaseUtils.convert(row.getOffset(),
                        areaVM.getMediator().getToolBar().getOffsetNumberBase(),
                        areaVM.getOffsetLength());
                getInfoLabel().setText(offsetStr);
                var hexTexts = getHexPane().getTexts();
                var asciiTexts = getAsciiPane().getTexts();
                for (var i = 0; i < hexTexts.size(); i++) {
                    var hexText = hexTexts.get(i);
                    hexText.setIndex(i);
                    var asciiText = asciiTexts.get(i);
                    asciiText.setIndex(i);
                    if (i < row.getHexes().size()) {
                        hexText.setText(row.getHexes().get(i));
                        asciiText.setText(row.getAsciis().get(i));
                    } else {
                        hexText.setText("  ");
                        asciiText.setText(" ");
                    }
                }
                var caretPos = getArea().getComponent().getCaret().getView().getViewModel().getPosition();
                if (caretPos != null && caretPos.getRowOffset() == offset) {
                    this.focused = true;
                    addCaret(caretPos);
                } else {
                    this.focused = false;
                }
                drawSelection();
            } else {
                this.focused = false;
                getNode().setVisible(false);
            }
        }

        @Override
        public boolean isReusable() {
            return true;
        }

        @Override
        public void rebuild() {
            var areaViewModel = getArea().getViewModel();
            var charWidth = areaViewModel.getCharSize().getWidth();

            getInfoLabel().setPadding(new Insets(0, charWidth, 0, charWidth));

            getHexPane().clear();
            //canvas width prevents resetting the panel width
            getHexPane().getCanvas().setWidth(0);
            getAsciiPane().clear();
            getAsciiPane().getCanvas().setWidth(0);

            var hexContentBox = getHexPane().getContentBox();
            hexContentBox.setPadding(new Insets(0, charWidth, 0, charWidth));
            hexContentBox.setSpacing(charWidth);
            var toolBarVM = areaViewModel.getMediator().getToolBar();
            for (var i = 0; i < toolBarVM.getRowByteCount(); i++) {
                if (toolBarVM.areColumnsEnabled() && i != 0
                        && i % toolBarVM.getColumnByteCount() == 0) {
                    if (areaViewModel.getColumnSeparator() == ColumnSeparator.SPACE) {
                        //we use regions as they stretch
                        var separator = new Region();
                        separator.getStyleClass().add("space");
                        hexContentBox.getChildren().add(separator);
                    } else {
                        var separator = new Region();
                        separator.getStyleClass().add("line");
                        hexContentBox.getChildren().add(separator);
                    }
                }

                var hexText = createHexText();
                addTextSelectionHandlers(hexText);
                getHexPane().getTexts().add(hexText);
                hexContentBox.getChildren().add(hexText);
                var asciiText = createAsciiText();
                addTextSelectionHandlers(asciiText);
                getAsciiPane().getTexts().add(asciiText);
            }

            var asciiContentBox = getAsciiPane().getContentBox();
            asciiContentBox.setPadding(new Insets(0, charWidth, 0, charWidth));
            asciiContentBox.getChildren().addAll(getAsciiPane().getTexts());
        }

        /**
         * Returns true if this row is the first one among all visible and non-visible rows.
         *
         * @return true if this is the first row; false otherwise.
         */
        public boolean isFirst() {
            return this.data.getOffset() == 0;
        }

//        protected void addListeners(BodyRowViewModel viewModel) {
//            super.addListeners(viewModel);
//            //when new row is created on scrolling it is necessary to update caret x
//            var caret = this.editor.getCaret();
//            if (caret.getRow() != null
//                    && caret.getRow().getViewModel().getOffset() == caret.getViewModel().getRowOffset()) {
//                addLayoutPulseListener(PulseListenerTiming.AFTER, () -> {
//                    setLinesX();
//                    getEditor().getCaret().updateX();
//                    removeCaret();
//                    addCaret();
//                    return false;
//                });
//            }
//        }

        RowData getData() {
            return data;
        }

        void removeCaret() {
            getHexPane().getCaretPane().getChildren().clear();
            getAsciiPane().getCaretPane().getChildren().clear();
        }

        void addCaret(CaretPosition position) {
            calculateCaretX(position);
            var caretV = getArea().getComponent().getCaret().getView();
            if (position.getPanel() == EditorPanel.HEX) {
                getHexPane().getCaretPane().getChildren().add(caretV.getNode());
                getAsciiPane().getCaretPane().getChildren().add(caretV.getIndicator());
            } else {
                getHexPane().getCaretPane().getChildren().add(caretV.getIndicator());
                getAsciiPane().getCaretPane().getChildren().add(caretV.getNode());
            }
        }

        ByteText getText(EditorPanel panel, int byteIndex) {
            if (panel == EditorPanel.HEX) {
                return getHexPane().getTexts().get(byteIndex);
            } else {
                return getAsciiPane().getTexts().get(byteIndex);
            }
        }

        boolean isFocused() {
            return focused;
        }

        void setFocused(boolean focused) {
            this.focused = focused;
        }

        /**
         * The selection won't be visible if it is drawn on the row before the canvas is rendered, because at that
         * moment the canvas size is still 0x0.
         */
        private void drawSelection() {
            clearSelection();
            var info = getArea().getViewModel().getSelectionInfo();
            if (info == null) {
                return;
            }
            if (info.getRowCount() == 1) {
                if (info.getFirstRow().getIndex() == this.data.getIndex()) {
                    drawSimpleSelection(info.getFirstRow());
                }
            } else {
                if ((info.getFirstRow().getIndex() <= this.data.getIndex())
                        && (info.getLastRow().getIndex() >= this.data.getIndex())) {
                    if (info.getType() == SelectionInfo.SelectionType.SIMPLE) {
                        var rowInfo = info.getFirstRow();
                        if (info.getLastRow().getIndex() == this.data.getIndex()) {
                            rowInfo = info.getLastRow();
                        }
                        drawSimpleSelection(rowInfo);
                    } else {
                        drawJoinedSelection(info);
                    }
                }
            }
        }

        private void drawSimpleSelection(SelectionInfo.RowInfo rowInfo) {
            drawSimpleSelection(rowInfo, getHexPane());
            drawSimpleSelection(rowInfo, getAsciiPane());
        }

        private void drawSimpleSelection(SelectionInfo.RowInfo rowInfo, RowPane pane) {
            var startText = getStartText(pane, rowInfo);
            var endText = getEndText(pane, rowInfo);
            var leftX = startText.getBoundsInParent().getMinX();
            var rightX = endText.getBoundsInParent().getMaxX();
            var width = rightX - leftX;

            GraphicsContext gc = pane.getCanvas().getGraphicsContext2D();
            gc.setFill(getArea().getViewModel().getSelectionBgColor());
            drawSelectionBg(gc, leftX, width);
            gc.setFill(getArea().getViewModel().getSelectionBorderColor());
            drawSelectionTopBorder(gc, leftX, width);
            drawSelectionBottomBorder(gc, leftX, width);
            drawSelectionLeftBorder(gc, leftX);
            drawSelectionRightBorder(gc, rightX);
        }

        private void drawJoinedSelection(SelectionInfo info) {
            if (info.getFirstRow().getIndex() == this.data.getIndex()) {
                drawJoinedSelectionFirstRow(getHexPane(), info.getFirstRow());
                drawJoinedSelectionFirstRow(getAsciiPane(), info.getFirstRow());
            } else if (info.getLastRow().getIndex() == this.data.getIndex()) {
                drawJoinedSelectionLastRow(getHexPane(), info.getLastRow());
                drawJoinedSelectionLastRow(getAsciiPane(), info.getLastRow());
            } else {
                drawJoinedSelectionMiddleRow(getHexPane());
                drawJoinedSelectionMiddleRow(getAsciiPane());
            }
        }

        private void drawJoinedSelectionFirstRow(RowPane pane, SelectionInfo.RowInfo rowInfo) {
            var startText = getStartText(pane, rowInfo);
            var endText = getEndText(pane, rowInfo);
            var leftX = startText.getBoundsInParent().getMinX();
            var rightX = endText.getBoundsInParent().getMaxX();
            var width = rightX - leftX;

            GraphicsContext gc = pane.getCanvas().getGraphicsContext2D();
            gc.setFill(getArea().getViewModel().getSelectionBgColor());
            drawSelectionBg(gc, leftX, width);
            gc.setFill(getArea().getViewModel().getSelectionBorderColor());
            drawSelectionTopBorder(gc, leftX, width);
            drawSelectionLeftBorder(gc, leftX);
            drawSelectionRightBorder(gc, rightX);
            if (rowInfo.getByteIndex() > 0) {
                var firstTextX = pane.getTexts().get(0).getBoundsInParent().getMinX();
                drawSelectionBottomBorder(gc, firstTextX - SELECTION_BORDER_SIZE, leftX - firstTextX);
            }
        }

        private void drawJoinedSelectionMiddleRow(RowPane pane) {
            var startIndex = 0;
            var endIndex = pane.getTexts().size() - 1;
            var startText = pane.getTexts().get(startIndex);
            var endText = pane.getTexts().get(endIndex);
            var leftX = startText.getBoundsInParent().getMinX();
            var rightX = endText.getBoundsInParent().getMaxX();

            GraphicsContext gc = pane.getCanvas().getGraphicsContext2D();
            gc.setFill(getArea().getViewModel().getSelectionBgColor());
            drawSelectionBg(gc, leftX, rightX - leftX);
            gc.setFill(getArea().getViewModel().getSelectionBorderColor());
            drawSelectionLeftBorder(gc, leftX);
            drawSelectionRightBorder(gc, rightX);
        }

        private void drawJoinedSelectionLastRow(RowPane pane, SelectionInfo.RowInfo rowInfo) {
            var startText = getStartText(pane, rowInfo);
            var endText = getEndText(pane, rowInfo);
            var leftX = startText.getBoundsInParent().getMinX();
            var rightX = endText.getBoundsInParent().getMaxX();
            var width = rightX - leftX;

            GraphicsContext gc = pane.getCanvas().getGraphicsContext2D();
            gc.setFill(getArea().getViewModel().getSelectionBgColor());
            drawSelectionBg(gc, leftX, rightX - leftX);
            gc.setFill(getArea().getViewModel().getSelectionBorderColor());
            drawSelectionLeftBorder(gc, leftX);
            drawSelectionRightBorder(gc, rightX);
            drawSelectionBottomBorder(gc, leftX, width);
            if (rowInfo.getByteCount() != pane.getTexts().size()) {
                var lastTextX = pane.getTexts().get(pane.getTexts().size() - 1).getBoundsInParent().getMaxX();
                drawSelectionTopBorder(gc, rightX, lastTextX - rightX + SELECTION_BORDER_SIZE);
            }
        }

        private ByteText getStartText(RowPane pane, SelectionInfo.RowInfo rowInfo) {
            var startIndex = rowInfo.getByteIndex();
            var startText = pane.getTexts().get(startIndex);
            return startText;
        }

        private ByteText getEndText(RowPane pane, SelectionInfo.RowInfo rowInfo) {
            var endIndex = rowInfo.getByteIndex() + rowInfo.getByteCount() - 1;
            var endText = pane.getTexts().get(endIndex);
            return endText;
        }

        private void drawSelectionTopBorder(GraphicsContext gc, double x, double width) {
            var node = getNode();
            x = node.snapPositionX(x);
            width = node.snapSizeX(width);
            gc.fillRect(x, 0, width, SELECTION_BORDER_SIZE);
        }

        private void drawSelectionBottomBorder(GraphicsContext gc, double x, double width) {
            var node = getNode();
            x = node.snapPositionX(x);
            width = node.snapSizeX(width);
            var height = node.snapSizeY(getNode().getHeight());
            gc.fillRect(x, height - SELECTION_BORDER_SIZE, width, height);
        }

        private void drawSelectionLeftBorder(GraphicsContext gc, double x) {
            var node = getNode();
            x = node.snapPositionX(x);
            var height = node.snapSizeY(getNode().getHeight());
            gc.fillRect(x - SELECTION_BORDER_SIZE, 0, SELECTION_BORDER_SIZE, height);
        }

        private void drawSelectionRightBorder(GraphicsContext gc, double x) {
            var node = getNode();
            x = node.snapPositionX(x);
            var height = node.snapSizeY(getNode().getHeight());
            gc.fillRect(x, 0, SELECTION_BORDER_SIZE, height);
        }

        private void drawSelectionBg(GraphicsContext gc, double x, double width) {
            var node = getNode();
            x = node.snapPositionX(x);
            width = node.snapSizeX(width);
            var height = node.snapSizeY(getNode().getHeight());
            gc.fillRect(x, 0, width, height);
        }

        private void clearSelection() {
            getHexPane().clearCanvas();
            getAsciiPane().clearCanvas();
        }

        private ByteText createHexText() {
            var text = createByteText();
            text.setOnMouseClicked(e -> {
                var areaVM = getArea().getViewModel();
                var caretV = getArea().getComponent().getCaret().getView();
                var location = resolveHexLocation(text, e.getX(), caretV.getViewModel().getShape());
                var rowIndex = this.data.getIndex();
                var newPos = CaretPosition.create(EditorPanel.HEX, rowIndex, text.getIndex(),
                        location, areaVM);
                getArea().moveCaretTo(newPos, this);
            });
            return text;
        }

        private void addTextSelectionHandlers(ByteText text) {
            text.setOnDragDetected(e -> {
                text.startFullDrag();
                var offset = this.data.getOffset() + text.getIndex();
                if (getArea().getViewModel().getSelection() != null) {
                    getArea().clearSelection();
                }
                getArea().getViewModel().setSelection(new ByteRange(offset, offset + 1));
                getArea().handleSelectionStarted(this, offset);
                e.consume();
            });
            text.addEventFilter(MouseDragEvent.MOUSE_DRAG_OVER, e -> {
                var offset = this.data.getOffset() + text.getIndex();
                var currentSelection = getArea().getViewModel().getSelection();
                boolean selectionChanged = false;
                boolean specialCase = false;
                if (currentSelection.getEnd() - currentSelection.getStart() == 2) { // two bytes selected - special case
                    if (offset == getArea().selectionStartOffset) {
                        selectionChanged = true;
                        specialCase = true;
                    }
                }
                if (offset != currentSelection.getStart() && offset != currentSelection.getEnd() - 1) {
                    selectionChanged = true;
                }

                if (selectionChanged) {
                    ByteRange range = null;
                    if (offset < currentSelection.getStart()) { // moving left or up
                        range = new ByteRange(offset, currentSelection.getEnd());
                    } else { // moving right or down
                        if (offset < getArea().selectionStartOffset) {
                            range = new ByteRange(offset, currentSelection.getEnd());
                        } else if (offset == getArea().selectionStartOffset && specialCase) {
                            range = new ByteRange(offset, offset + 1);
                        } else {
                            range = new ByteRange(currentSelection.getStart(), offset + 1);
                        }
                    }
                    getArea().getViewModel().setSelection(range);
                    getArea().handleTextSelectionHovered(this);
                }
                e.consume();
            });
        }

        private void addRowSelectionHandlers() {
            getNode().addEventFilter(MouseDragEvent.MOUSE_DRAG_RELEASED, e -> {
                getArea().handleSelectionFinished(this);
            });
            getNode().addEventFilter(MouseDragEvent.MOUSE_DRAG_ENTERED, e -> {
                getArea().handleRowSelectionEntered(this);
                e.consume();
            });
            getNode().addEventFilter(MouseDragEvent.MOUSE_DRAG_EXITED, e -> {
                getArea().handleRowSelectionExited(this);
                e.consume();
            });
        }

        private CaretByteLocation resolveHexLocation(ByteText text, double x, CaretShape shape) {
            double textWidth = text.getLayoutBounds().getWidth();
            double widthHalf = textWidth / 2;
            if (x < widthHalf) {
                return CaretByteLocation.FIRST;
            } else {
                if (shape != CaretShape.BAR) {
                    return CaretByteLocation.SECOND;
                }
                if (x < widthHalf + (widthHalf / 2)) {
                    return CaretByteLocation.SECOND;
                } else {
                    return CaretByteLocation.THIRD;
                }
            }
        }

        private ByteText createAsciiText() {
            var text = createByteText();
            text.setOnMouseClicked(e -> {
                var areaVM = getArea().getViewModel();

                var caretV = getArea().getComponent().getCaret().getView();
                var caretVM = caretV.getViewModel();
                var location = resolveAsciiLocation(text, e.getX(), caretVM.getShape(), caretVM.isAtRowEnd());
                var rowIndex = this.data.getIndex();
                var newPos = CaretPosition.create(EditorPanel.ASCII, rowIndex, text.getIndex(), location,
                        areaVM);
                getArea().moveCaretTo(newPos, this);

            });
            return text;
        }

        private CaretByteLocation resolveAsciiLocation(ByteText text, double x, CaretShape shape, boolean lastByte) {
            if (shape != CaretShape.BAR) {
                return CaretByteLocation.FIRST;
            }
            double textWidth = text.getLayoutBounds().getWidth();
            double widthHalf = textWidth / 2;
            if (lastByte) {
                if (x < widthHalf) {
                    return CaretByteLocation.FIRST;
                } else {
                    return CaretByteLocation.THIRD;
                }
            } else {
                return CaretByteLocation.FIRST;
            }
        }

        private ByteText createByteText() {
            var text = new ByteText();
            text.getStyleClass().add("content");
            return text;
        }

        private void calculateCaretX(CaretPosition position) {
            var caretVM = getArea().getComponent().getCaret().getView().getViewModel();
            //when file is opened the position of the caret is calculated by char width as there can be no bytes
            if (position.getByteIndex() == 0 && position.getByteLocation() == CaretByteLocation.FIRST
                    && position.getRowIndex() == 0) {
                var charWidth = getArea().getViewModel().getCharSize().getWidth();
                caretVM.setX(charWidth);
                caretVM.setIndicatorX(charWidth);
            }
            double x;
            double indicatorX;
            var hexText = getHexPane().getTexts().get(position.getByteIndex());
            var asciiText = getAsciiPane().getTexts().get(position.getByteIndex());
            if (position.getPanel() == EditorPanel.HEX) {
                //caret
                switch (position.getByteLocation()) {
                    case FIRST:
                        x = hexText.getBoundsInParent().getMinX() - SELECTION_BORDER_SIZE;
                        break;
                    case SECOND:
                        double textWidth = hexText.getLayoutBounds().getWidth();
                        double widthHalf = textWidth / 2;
                        x = hexText.getBoundsInParent().getMinX() + widthHalf;
                        break;
                    case THIRD:
                        x = hexText.getBoundsInParent().getMaxX();
                        break;
                    default:
                        throw new AssertionError();
                }
                //indicator
                indicatorX = asciiText.getBoundsInParent().getMinX();
            } else {
                //caret
                if (position.getByteLocation() == CaretByteLocation.THIRD) {
                    x = asciiText.getBoundsInParent().getMaxX();
                } else {
                    x = asciiText.getBoundsInParent().getMinX() - SELECTION_BORDER_SIZE;
                }
                //indicator
                indicatorX = hexText.getBoundsInParent().getMinX();
            }
            caretVM.setX(x);
            caretVM.setIndicatorX(indicatorX);
        }
    }

    private static class HeaderRow extends AbstractRow {

        private final HBox node = new HBox();

        private final HBox scrollableBox = super.getNode();

        private final List<Label> contentLabels = new ArrayList<>();

        HeaderRow(HexAreaView<?, ?> area) {
            super(area);
            this.scrollableBox.setMaxWidth(Double.MAX_VALUE);
            HBox.setHgrow(this.scrollableBox, Priority.ALWAYS);
            this.scrollableBox.getStyleClass().addAll("scrollable-box", StyleClasses.MONOSPACE);

            this.node.setMaxWidth(Double.MAX_VALUE);
            this.node.setMinWidth(0);
            this.node.getChildren().add(this.scrollableBox);
            this.node.getStyleClass().add("header-row");
        }

        @Override
        public void rebuild() {
            var areaVM = getArea().getViewModel();
            var charWidth = areaVM.getCharSize().getWidth();

            getInfoLabel().setPadding(new Insets(0, charWidth, 0, charWidth));

            getHexPane().clear();
            //canvas width prevents resetting the panel width
            getHexPane().getCanvas().setWidth(0);
            getAsciiPane().clear();
            getAsciiPane().getCanvas().setWidth(0);

            var labelText = "x".repeat(areaVM.getOffsetLength());
            getInfoLabel().setText(labelText);


            var hexContentBox = getHexPane().getContentBox();
            hexContentBox.setSpacing(charWidth);
            var boxPadding = new Insets(0, charWidth, 0, charWidth);
            hexContentBox.setPadding(boxPadding);
            var children = hexContentBox.getChildren();
            children.clear();
            this.contentLabels.clear();
            var asciiContentBox = getAsciiPane().getContentBox();
            asciiContentBox.setPadding(boxPadding);
            asciiContentBox.getChildren().clear();
            var toolBarVM = areaVM.getMediator().getToolBar();
            for (byte i = 0; i < toolBarVM.getRowByteCount(); i++) {
                if (toolBarVM.areColumnsEnabled() && i != 0 && i % toolBarVM.getColumnByteCount() == 0) {
                    if (areaVM.getColumnSeparator() == ColumnSeparator.SPACE) {
                        //we use regions as they stretch
                        var separator = new Region();
                        separator.getStyleClass().add("space");
                        children.add(separator);
                    } else {
                        var separator = new Region();
                        separator.getStyleClass().add("line");
                        children.add(separator);
                    }
                }
                var contentLabel = new Label(NumberBaseUtils.convert(i, toolBarVM.getOffsetNumberBase(), 2));
                contentLabel.getStyleClass().add("content");
                contentLabel.setLineSpacing(0);
                children.add(contentLabel);
                contentLabels.add(contentLabel);
            }
        }

        @Override
        public HBox getNode() {
            return this.node;
        }

        HBox getScrollableBox() {
            return scrollableBox;
        }

        /**
         * Depending on the {@link ColumnSeparator} settings, a row may contain lines. Therefore, we cannot simply add
         * padding to any of the panes, as this would result in gaps in those lines. This issue is resolved by adding
         * padding to labels and using a background color and insets that simulate padding — without actually applying
         * any.
         */
        void setPanelPanesBackground() {
            var lineExtraSpaceHalf = contentLabels.get(0).getHeight() - contentLabels.get(0).getBaselineOffset();
            DecimalFormat df = new DecimalFormat("0", DecimalFormatSymbols.getInstance(Locale.US));
            df.setMaximumFractionDigits(10);
            String lineExtraSpaceHalfStr = df.format(lineExtraSpaceHalf);
            //note that the bottom padding is twice as large as the top padding; this is real padding
            var labelPadding = new Insets(lineExtraSpaceHalf, 0, lineExtraSpaceHalf * 2, 0);
            for (var label : contentLabels) {
                label.setPadding(labelPadding);
            }
            var style = "-fx-background-insets: 0, 0 0 " + lineExtraSpaceHalfStr + " 0;"; //this is fake padding
            getHexPane().setStyle(style);
            getAsciiPane().setStyle(style);
        }

        /**
        * Adjusts the width of the asciiPane to account for horizontal translation.
        * <p>
        * When scrollable container is translated horizontally (via {@code translateX}), the visual position changes but
        * the layout system remains unaware of this offset. As a result, the default HBox layout will not stretch child
        * elements to fill the apparent available space created by the translation. This method manually compensates
        * by recalculating the effective width required for the asciiPane to visually fill the translated space.
        *
        * @param mainPaneWidth the width of the main pane in the editor. We don't use this component node width because
        * it changes in a listener, so there can situations when it won't contain a new value when node.getWidth()
        * will be called.
        */
        void updateAsciiPaneWidth(double mainPaneWidth) {
            var scrollBarVisibleWidth = (getInfoLabel().getWidth() + getHexPane().getWidth())
                    - Math.abs(this.scrollableBox.getTranslateX());
            var asciiPaneWidth =  mainPaneWidth - scrollBarVisibleWidth;
            if (asciiPaneWidth >= 0) {
                getAsciiPane().setMinWidth(asciiPaneWidth);
                getAsciiPane().setPrefWidth(asciiPaneWidth);
            }
        }
    }

    private static final double ROW_VISIBILITY_TOLERANCE = 2.0;

    private static final Logger logger = LoggerFactory.getLogger(HexAreaView.class);

    /**
     * Contains information for pageUp and pageDown scroll.
     */
    private record PageScroll(int firstRowIndex, double rowHeiht, int caretVisibleRowIndex, int scrollRowCount) { }

    private final HeaderRow headerRow;

    /**
     * The row that has a caret.
     */
    private BodyRow caretRow;

    /**
     * Reusable cells in a virtual flow remain in memory after creation and are only released when virtualFlow.dispose()
     * is explicitly invoked.
     */
    private final List<BodyRow> bodyRows = new ArrayList<>();

    /**
     * Integer is a row index.
     */
    private final VirtualFlow<Integer, BodyRow> virtualFlow;

    private final VirtualizedScrollPane<VirtualFlow<Integer, BodyRow>> virtualScrollPane;

    private final VBox mainPane = new VBox();

    private int pulseCounter;

    private int selectionStartOffset;

    private int previousSelectedRowIndex = -1;

    public HexAreaView(T viewModel) {
        super(viewModel);
        this.virtualFlow = VirtualFlow.createVertical(viewModel.getOffsets(), offset -> {
                var rowData = viewModel.createRowData(offset);
                var rowView = new BodyRow(rowData, this);
                this.bodyRows.add(rowView);
                return rowView;
        });
        this.virtualScrollPane = new VirtualizedScrollPane<>(virtualFlow);
        this.headerRow = new HeaderRow(this);
    }

    @Override
    public Region getNode() {
        return this.mainPane;
    }

    @Override
    public void requestFocus() {
        NodeUtils.requestFocus(virtualFlow);
    }

    @Override
    protected void build() {
        super.build();
        var css = HexAreaView.class.getResource("hex-area.css").toExternalForm();
        getNode().getStylesheets().add(css);
        this.virtualScrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        VBox.setVgrow(virtualScrollPane, Priority.ALWAYS);

        this.mainPane.getChildren().addAll(headerRow.getNode(), virtualScrollPane);
        VBox.setVgrow(this.mainPane, Priority.ALWAYS);
    }

    @Override
    protected void bind() {
        super.bind();
        this.headerRow.getNode().prefWidthProperty().bind(this.mainPane.widthProperty());
    }

    @Override
    protected void addHandlers() {
        super.addHandlers();
        var viewModel = getViewModel();
        virtualFlow.setOnMousePressed(e -> virtualFlow.requestFocus());
        virtualFlow.addEventFilter(KeyEvent.KEY_PRESSED, e -> {
            e.consume();
            if (viewModel.getOffsets().isEmpty()) {
                return;
            }
            switch (e.getCode()) {
                case UP: viewModel.moveCaretUp(); break;
                case DOWN: viewModel.moveCaretDown(); break;
                case LEFT: viewModel.moveCaretLeft(); break;
                case RIGHT: viewModel.moveCaretRight(); break;
                case HOME: viewModel.moveCaretHome(); break;
                case END: viewModel.moveCaretEnd(); break;
                case PAGE_UP: moveCaretPageUp(); break;
                case PAGE_DOWN: moveCaretPageDown(); break;
            }
        });
    }

    @Override
    protected void addListeners() {
        super.addListeners();
        var viewModel = getViewModel();
        viewModel.layoutUpdateSource().addListener((newPosition) -> updateLayout(newPosition));
        viewModel.caretPositionSource().addListener((position) -> updateCaretPosition(position));
        this.mainPane.widthProperty().addListener((ov, oldV, newV) ->
                this.headerRow.updateAsciiPaneWidth(newV.doubleValue()));
        this.virtualScrollPane.estimatedScrollXProperty().addListener((ov, oldV, newV) -> {
            this.headerRow.getScrollableBox().setTranslateX(newV * -1);
            this.headerRow.updateAsciiPaneWidth(this.mainPane.getWidth());
        });
    }

    protected VBox getMainPane() {
        return mainPane;
    }

    private void updateLayout(CaretPosition newPosition) {
        this.mainPane.setVisible(false);
        this.headerRow.rebuild();
        for (var r : bodyRows) {
            r.rebuild();
            r.updateItem(r.getData().getOffset());
        }
        BodyRow row = null;
        if (newPosition != null) {
            //after clearing and adding new items flow is scrolled to the end
            this.virtualFlow.showAsFirst(newPosition.getRowIndex());
            row = this.virtualFlow.getCell(newPosition.getRowIndex());
        }
        var finalRow = row;
        this.pulseCounter = 0;
        //When the layout changes, the text coordinates are updated as well; therefore, to correctly calculate
        //the caret position, it is necessary to use a pulse listener.

        //For precise padding calculation, exact dimensions are only available in the PulseListener. However,
        //when the required padding is set within this listener, the changes will only become visible in the
        //next pulse. To prevent flickering during layout adjustments, the mainPane is made invisible and only
        //becomes visible again after the second pulse.
        getPulseListenerManager().addListener(LayoutPhase.POST, () -> {
            if (this.pulseCounter == 0) {
                this.headerRow.setPanelPanesBackground();
                this.headerRow.updateAsciiPaneWidth(this.mainPane.getWidth());
                this.pulseCounter++;
                return true;
            } else {
                this.mainPane.setVisible(true);
                moveCaretTo(newPosition, finalRow);
                NodeUtils.requestFocus(virtualFlow);
                return false;
            }
        });
    }

    private void updateCaretPosition(CaretPosition newPos) {
        var curPos = getComponent().getCaret().getView().getViewModel().getPosition();
        if (newPos.getRowIndex() == curPos.getRowIndex()) {
            moveCaretTo(newPos, null);
        } else {
            BodyRow row;
            if (newPos.getRowIndex() > curPos.getRowIndex()) {
                row = scrollDownTo(newPos.getRowIndex());
            } else {
                row = scrollUpTo(newPos.getRowIndex());
            }
            moveCaretTo(newPos, row);
        }
    }

    private BodyRow scrollUpTo(int rowIndex) {
        var row = virtualFlow.getCellIfVisible(rowIndex).orElse(null);
        if (row == null) {
            virtualFlow.showAsFirst(rowIndex);
            row = virtualFlow.getCell(rowIndex);
        } else {
            //it can be visible but not entirely
            if (virtualFlow.getFirstVisibleIndex() == rowIndex) {
                virtualFlow.showAsFirst(rowIndex);
            }
        }
        return row;
    }

    private BodyRow scrollDownTo(int rowIndex) {
        var row = virtualFlow.getCellIfVisible(rowIndex).orElse(null);
        if (row == null) {
            virtualFlow.showAsLast(rowIndex);
            row = virtualFlow.getCell(rowIndex);
        } else {
            //it can be visible but not entirely
            if (virtualFlow.getLastVisibleIndex() == rowIndex) {
                virtualFlow.showAsLast(rowIndex);
            }
        }
        return row;
    }

    /**
     * Determines the first visible row after performing a PageUp scroll in a virtualized scroll flow. The
     * algorithm is based on two key principles:
     *
     * 1. After scrolling, virtual rows should appear in the same position within the viewport as they did before
     * scrolling. This ensures visual continuity.
     *
     * 2. No content should be skipped — all rows must be shown fully to the user.
     *
     * Based on the visibility of the first and last visible rows before scrolling, the first visible row after
     * scrolling is determined as follows:
     *
    * 1. If the first row is fully visible and the last row is fully visible, then the row immediately preceding the
    * first row becomes the last visible row after scrolling.
    *
    * 2. If the first row is fully visible and the last row is partially visible, then the first visible row
    * becomes the last visible row after scrolling.
    *
    * 3. If the first row is partially visible and the last row is fully visible, then the first visible
    * row becomes the last visible row after scrolling.
    *
    * 4. If the first row is partially visible and the last row is partially visible, then the second visible
    * row becomes the last visible row after scrolling.
     */
    private void moveCaretPageUp() {
        var pageScroll = createPageScroll();
        int calculatedNewFirstRowIndex = pageScroll.firstRowIndex() - pageScroll.scrollRowCount();
        this.virtualFlow.scrollYBy(pageScroll.scrollRowCount * pageScroll.rowHeiht() * -1);
        Platform.runLater(() -> {
            moveCaretOnPageScroll(calculatedNewFirstRowIndex, pageScroll.caretVisibleRowIndex, 0);
        });
    }

    /**
     * Determines the first visible row after performing a PageDown scroll in a virtualized scroll flow. The
     * algorithm is based on two key principles:
     *
     * 1. After scrolling, virtual rows should appear in the same position within the viewport as it did before
     * scrolling. This ensures visual continuity.
     *
     * 2. No content should be skipped — all rows must be shown fully to the user.
     *
     * Based on the visibility of the first and last visible rows before scrolling, the first visible row after
     * scrolling is determined as follows:
     *
     * 1. If the first row is fully visible and the last row is fully visible, then the row immediately following the
     * last row becomes the new first visible row.
     *
     * 2. If the first row is fully visible and the last row is partially visible, then the last visible row
     * becomes the new first visible row.
     *
     * 3. If the first row is partially visible and the last row is fully visible, then the last visible
     * row becomes the new first visible row after scrolling.
     *
     * 4. If the first row is partially visible and the last row is partially visible, then the second-to-last
     * visible row becomes the new first visible row after scrolling.
     */
    private void moveCaretPageDown() {
        var pageScroll = createPageScroll();
        int calculatedNewFirstRowIndex = pageScroll.firstRowIndex() + pageScroll.scrollRowCount();
        this.virtualFlow.scrollYBy(pageScroll.scrollRowCount * pageScroll.rowHeiht());
        Platform.runLater(() -> {
            moveCaretOnPageScroll(calculatedNewFirstRowIndex, pageScroll.caretVisibleRowIndex,
                    this.virtualFlow.visibleCells().size() - 1);
        });
    }

    /**
     * Moves the caret to a new position in the view, based on the model state.
     *
     * @param position the new position of the caret
     * @param newRow the target row, or {@code null} if the caret remains on the same row
     */
    private void moveCaretTo(CaretPosition position, BodyRow newRow) {
        var caretV = getComponent().getCaret().getView();
        var caretVM = caretV.getViewModel();
        //if the position is null, it indicates that the caret has not moved,
        //and only the view's caret needs to be updated.
        if (position == null) {
            position = caretVM.getPosition();
        }

        if (newRow == null) {
            newRow = this.caretRow;
        }
        var oldRow = this.caretRow;
        this.caretRow = newRow;

        if (oldRow != newRow) {
            if (oldRow != null) {
                oldRow.removeCaret();
                oldRow.setFocused(false);
            }
        }
        // The caret is added to the row in two cases: when this method is called and when row.updateItem(..) is called.
        // That's why we always remove the caret first to avoid a 'duplicate children added' exception.
        newRow.removeCaret();
        newRow.addCaret(position);
        newRow.setFocused(true);
        //when cursor is moved it must always be visible
        if (!caretVM.isDisabled()) {
            caretV.getNode().setVisible(true);
        }
        //and only now we change position
        caretVM.setPosition(position);
        caretV.setRowHeight(this.caretRow.getNode().getHeight());
    }

    private PageScroll createPageScroll() {
        var viewModel = getViewModel();

        //resolving which row is fully visible and which is not
        var firstRow = this.virtualFlow.visibleCells().get(0);
        var firstRowIndex = viewModel.calculateRowIndex(firstRow.getData());
        double firstRowFlowOffset = Math.max(0, -firstRow.getNode().getBoundsInParent().getMinY());
        boolean firstRowFullyVisible = ROW_VISIBILITY_TOLERANCE >= firstRowFlowOffset;

        var visibleRowTotalHeight = this.virtualFlow.visibleCells().size() * firstRow.getNode().getHeight();
        visibleRowTotalHeight -= firstRowFlowOffset;
        boolean lastRowFullyVisible = ROW_VISIBILITY_TOLERANCE >= visibleRowTotalHeight - this.virtualFlow.getHeight();

        //calculating caret row index diff
        var caretV = getComponent().getCaret().getView();
        var caretVM = caretV.getViewModel();
        var caretRowIndex = caretVM.getPosition().getRowIndex();
        //the index of the visible row owning the caret
        var caretVisibleRowIndex = caretRowIndex - firstRowIndex;

        int scrollRowCount;
        if (firstRowFullyVisible) {
            if (lastRowFullyVisible) {
                scrollRowCount = this.virtualFlow.visibleCells().size();
            } else {
                scrollRowCount = this.virtualFlow.visibleCells().size() - 1;
            }
        } else {
            if (lastRowFullyVisible) {
                scrollRowCount = this.virtualFlow.visibleCells().size() - 1;
            } else {
                if (this.virtualFlow.visibleCells().size() >= 2) {
                    scrollRowCount = this.virtualFlow.visibleCells().size() - 2;
                } else {
                    scrollRowCount = this.virtualFlow.visibleCells().size() - 1;
                }
            }
        }

        return new PageScroll(firstRowIndex, firstRow.getNode().getHeight(), caretVisibleRowIndex, scrollRowCount);
    }

    private void moveCaretOnPageScroll(int calculatedNewFirstRowIndex, int caretVisibleRowIndex, int endCaretRowIndex) {
        var viewModel = getViewModel();
        BodyRow newCaretRow;

        //we don't know how many rows were actually scrolled
        var realNewFirstRow = this.virtualFlow.visibleCells().get(0);
        var realNewFirstRowIndex = viewModel.calculateRowIndex(realNewFirstRow.getData());
        //if fewer rows are scrolled than requested, the caret is placed on the first or last row.
        if (realNewFirstRowIndex == calculatedNewFirstRowIndex) {
            newCaretRow = this.virtualFlow.visibleCells().get(caretVisibleRowIndex);
        } else {
            newCaretRow = this.virtualFlow.visibleCells().get(endCaretRowIndex);
        }
        int newCaretRowIndex = viewModel.calculateRowIndex(newCaretRow.getData());
        var caretV = getComponent().getCaret().getView();
        var caretVM = caretV.getViewModel();
        var curPos = caretVM.getPosition();
        var newPos = CaretPosition.create(curPos.getPanel(),
                newCaretRowIndex, curPos.getByteIndex(), curPos.getByteLocation(), viewModel);
        moveCaretTo(newPos, newCaretRow);
    }

    private void handleSelectionStarted(BodyRow row, int offset) {
        this.selectionStartOffset = offset;
        this.previousSelectedRowIndex = -1;
        row.drawSelection();
    }

    private void handleRowSelectionEntered(BodyRow row) {
        ByteRange newByteRange = null;
        if (isPreviousSelectedRowVisible(row.getData().getIndex(), previousSelectedRowIndex)) {
            int prePreviousRowIndex;
            var previousRow = this.virtualFlow.getCell(previousSelectedRowIndex);
            var currentSelection = getViewModel().getSelection();
            var currentInfo = getViewModel().getSelectionInfo();
            var currentFirstRowIndex = currentInfo.getFirstRow().getIndex();
            var currentLastRowIndex = currentFirstRowIndex;
            if (currentInfo.getLastRow() != null) {
                currentLastRowIndex = currentInfo.getLastRow().getIndex();
            }
            if (row.getData().getIndex() < previousSelectedRowIndex) { // moving up
                prePreviousRowIndex = previousSelectedRowIndex + 1;
                if (previousRow.getData().getOffset() < currentSelection.getStart()) { // selecting
                    newByteRange = new ByteRange(previousRow.getData().getOffset(), currentSelection.getEnd());
                } else if (previousRow.getData().getOffset() < currentSelection.getEnd()) { // unselecting
                    if (row.getData().getIndex() == currentFirstRowIndex) {
                        newByteRange = new ByteRange(currentSelection.getStart(), currentSelection.getStart() + 1);
                    } else {
                        newByteRange = new ByteRange(currentSelection.getStart(), previousRow.getData().getOffset());
                    }
                }
            } else { // moving down
                prePreviousRowIndex = previousSelectedRowIndex - 1;
                var rowByteCount = getViewModel().getRowByteCount(row.getData().getIndex());
                if (row.getData().getOffset() <= currentSelection.getEnd()) { // unselecting
                    if (row.getData().getIndex() == currentLastRowIndex) {
                        newByteRange = new ByteRange(currentSelection.getEnd() - 1, currentSelection.getEnd());
                    } else {
                        newByteRange = new ByteRange(row.getData().getOffset() + rowByteCount,
                                currentSelection.getEnd());
                    }
                } else if (row.getData().getOffset() >= currentSelection.getEnd()) { // selecting
                    newByteRange = new ByteRange(currentSelection.getStart(), row.getData().getOffset());
                }
            }
            if (newByteRange != null) {
                getViewModel().setSelection(newByteRange);
                // it is necessary to redraw 3 rows
                previousRow.drawSelection();
                row.drawSelection();
                if (prePreviousRowIndex >= 0 && prePreviousRowIndex < getViewModel().getOffsets().size()) {
                    var prePreviousRow = this.virtualFlow.getCellIfVisible(prePreviousRowIndex);
                    if (prePreviousRow.isPresent()) {
                        prePreviousRow.get().drawSelection();
                    }
                }
            }
        }
    }

    private void handleRowSelectionExited(BodyRow row) {
        this.previousSelectedRowIndex = row.getData().getIndex();
    }

    private void handleTextSelectionHovered(BodyRow row) {
        row.drawSelection();
        var currentRowIndex = row.getData().getIndex();
        var previousRowIndex = previousSelectedRowIndex;
        previousSelectedRowIndex = row.data.getIndex();
        if (previousRowIndex != -1) {
            if (isPreviousSelectedRowVisible(currentRowIndex, previousRowIndex)) {
                this.virtualFlow.getCell(previousRowIndex).drawSelection();
            }
        }
    }

    private boolean isPreviousSelectedRowVisible(int currentRowIndex, int previousRowIndex) {
        return (previousRowIndex > currentRowIndex && virtualFlow.getLastVisibleIndex() >= previousRowIndex)
                    || (previousRowIndex < currentRowIndex && virtualFlow.getFirstVisibleIndex() <= previousRowIndex);
    }

    private void handleSelectionFinished(BodyRow row) {
        this.selectionStartOffset = -1;
    }

    private void clearSelection() {
        for (var row : this.virtualFlow.visibleCells()) {
            row.clearSelection();
        }
    }
}
