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

import com.techsenger.tabshell.core.style.StyleClasses;
import static com.techsenger.tabshell.hex.CaretByteLocation.FIRST;
import static com.techsenger.tabshell.hex.CaretByteLocation.SECOND;
import static com.techsenger.tabshell.hex.CaretByteLocation.THIRD;
import java.util.ArrayList;
import java.util.List;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.layout.Region;
import org.fxmisc.flowless.Cell;

/**
 *
 * @author Pavel Castornii
 */
class BodyRowView extends AbstractRowView<BodyRowViewModel> implements Cell<Integer, Node> {

    /**
     * Only texts that represent bytes. Its size is always equal to max rowByteCount.
     */
    private final List<ByteTextPair> byteTextPairs = new ArrayList<>();

    BodyRowView(BodyRowViewModel viewModel, AbstractHexEditorTabView<?> editor) {
        super(viewModel, editor);
    }

    @Override
    public void requestFocus() {

    }

    /**
     * Due to virtualization and for performance reasons, we do not bind the values of text nodes to the model,
     * but instead set the values explicitly.
     */
    @Override
    public void updateItem(Integer offset) {
        var vm = getViewModel();
        var row = RowModel.create(offset, vm.getEditor());
        vm.setModel(row);
        removeCaret();
        if (offset != null) {
            getNode().setVisible(true);
            var offsetStr = NumberBaseUtils.convert(row.getOffset(), vm.getEditor().getOffsetNumberBase(),
                    vm.getEditor().getOffsetLength());
            getInfoLabel().setText(offsetStr);
            for (var i = 0; i < vm.getEditor().getRowByteCount(); i++) {
                var bytePair = byteTextPairs.get(i);
                bytePair.setIndex(i);
                if (i < row.getHexes().size()) {
                    bytePair.setEmpty(false);
                    bytePair.getHexText().setText(row.getHexes().get(i));
                    bytePair.getAsciiText().setText(row.getAsciis().get(i));
                } else {
                    bytePair.setEmpty(true);
                    bytePair.getHexText().setText("  ");
                    bytePair.getAsciiText().setText(" ");
                }
            }
            var caretPos = getEditor().getCaret().getViewModel().getPosition();
            if (caretPos != null && caretPos.getRowOffset() == offset) {
                vm.setFocused(true);
                addCaret(caretPos);
            } else {
                vm.setFocused(false);
            }
        } else {
            vm.setFocused(false);
            getNode().setVisible(false);
        }
    }

    @Override
    public boolean isReusable() {
        return true;
    }

    @Override
    public void rebuild() {
        var viewModel = getViewModel();
        var editorViewModel = viewModel.getEditor();
        var charWidth = editorViewModel.getCharSize().getWidth();

        getInfoLabel().setPadding(new Insets(0, charWidth, 0, charWidth));

        getHexPane().clear();
        //canvas width prevents resetting the panel width
        getHexPane().getCanvas().setWidth(0);
        getAsciiPane().clear();
        getAsciiPane().getCanvas().setWidth(0);
        this.byteTextPairs.clear();

        var hexContentBox = getHexPane().getContentBox();
        hexContentBox.setPadding(new Insets(0, charWidth, 0, charWidth));
        hexContentBox.setSpacing(charWidth);

        List<ByteText> asciiTexts = new ArrayList<>();

        for (var i = 0; i < editorViewModel.getRowByteCount(); i++) {
            if (editorViewModel.areColumnsEnabled() && i != 0 && i % editorViewModel.getColumnByteCount() == 0) {
                if (editorViewModel.getColumnSeparator() == ColumnSeparator.SPACE) {
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

            var hexText = createByteHexText();
            hexContentBox.getChildren().add(hexText);
            var asciiText = createByteAsciiText();
            asciiTexts.add(asciiText);
            this.byteTextPairs.add(new ByteTextPair(this, hexText, asciiText));
        }

        var asciiContentBox = getAsciiPane().getContentBox();
        asciiContentBox.setPadding(new Insets(0, charWidth, 0, charWidth));
        asciiContentBox.getChildren().addAll(asciiTexts);
    }

    @Override
    protected void build(BodyRowViewModel viewModel) {
        super.build(viewModel);
        getNode().getStyleClass().addAll("body-row", StyleClasses.MONOSPACE);
        rebuild();
    }

    @Override
    protected void addListeners(BodyRowViewModel viewModel) {
        super.addListeners(viewModel);
//        //when new row is created on scrolling it is necessary to update caret x
//        var caret = this.editor.getCaret();
//        if (caret.getRow() != null
//                && caret.getRow().getViewModel().getOffset() == caret.getViewModel().getRowOffset()) {
//            addLayoutPulseListener(PulseListenerTiming.AFTER, () -> {
//                setLinesX();
//                getEditor().getCaret().updateX();
//                removeCaret();
//                addCaret();
//                return false;
//            });
//        }
    }

    @Override
    protected void postInitialize(BodyRowViewModel viewModel) {
        super.postInitialize(viewModel);
        updateItem(viewModel.getModel().getOffset());
    }

    List<ByteTextPair> getByteTextPairs() {
        return byteTextPairs;
    }

    void removeCaret() {
        getHexPane().getCaretPane().getChildren().clear();
        getAsciiPane().getCaretPane().getChildren().clear();
    }

    void addCaret(CaretPosition position) {
        calculateCaretX(position);
        var caret = getEditor().getCaret();
        if (position.getPanel() == EditorPanel.HEX) {
            getHexPane().getCaretPane().getChildren().add(caret.getNode());
            getAsciiPane().getCaretPane().getChildren().add(caret.getIndicator());
        } else {
            getHexPane().getCaretPane().getChildren().add(caret.getIndicator());
            getAsciiPane().getCaretPane().getChildren().add(caret.getNode());
        }
    }

    ByteText getText(EditorPanel panel, int byteIndex) {
        if (panel == EditorPanel.HEX) {
            return this.byteTextPairs.get(byteIndex).getHexText();
        } else {
            return this.byteTextPairs.get(byteIndex).getAsciiText();
        }
    }

    private ByteText createByteHexText() {
        var text = createByteText();
        text.setOnMouseClicked(e -> {
            var editorVM = getEditor().getViewModel();

            var caretV = getEditor().getCaret();
            var curPos = caretV.getViewModel().getPosition();

            var location = resolveHexLocation(text, e.getX(), caretV.getViewModel().getShape());
            var rowIndex = getViewModel().getModel().getIndex();
            var newPos = CaretPosition.create(EditorPanel.HEX, rowIndex, text.getPair().getIndex(),
                    location, editorVM);
            caretV.moveTo(newPos, this);
        });
        return text;
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

    private ByteText createByteAsciiText() {
        var text = createByteText();
        text.setOnMouseClicked(e -> {
            var editorVM = getEditor().getViewModel();

            var caretV = getEditor().getCaret();
            var caretVM = caretV.getViewModel();
            var curPos = caretV.getViewModel().getPosition();

            var location = resolveAsciiLocation(text, e.getX(), caretVM.getShape(), caretVM.isAtRowEnd());
            var rowIndex = getViewModel().getModel().getIndex();
            var newPos = CaretPosition.create(EditorPanel.ASCII, rowIndex, text.getPair().getIndex(), location,
                    editorVM);
            caretV.moveTo(newPos, this);

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
        var caretVM = getViewModel().getEditor().getCaret();
        //when file is opened the position of the caret is calculated by char width as there can be no bytes
        if (position.getByteIndex() == 0 && position.getByteLocation() == CaretByteLocation.FIRST
                && position.getRowIndex() == 0) {
            var charWidth = getEditor().getViewModel().getCharSize().getWidth();
            caretVM.setX(charWidth);
            caretVM.setIndicatorX(charWidth);
        }
        double x;
        double indicatorX;
        var bytePair = this.byteTextPairs.get(position.getByteIndex());
        if (position.getPanel() == EditorPanel.HEX) {
            //caret
            var text = bytePair.getHexText();
            switch (position.getByteLocation()) {
                case FIRST:
                    x = text.getBoundsInParent().getMinX();
                    break;
                case SECOND:
                    double textWidth = text.getLayoutBounds().getWidth();
                    double widthHalf = textWidth / 2;
                    x = text.getBoundsInParent().getMinX() + widthHalf;
                    break;
                case THIRD:
                    x = text.getBoundsInParent().getMaxX();
                    break;
                default:
                    throw new AssertionError();
            }
            //indicator
            text = bytePair.getAsciiText();
            indicatorX = text.getBoundsInParent().getMinX();
        } else {
            //caret
            var text = bytePair.getAsciiText();
            if (position.getByteLocation() == CaretByteLocation.THIRD) {
                x = text.getBoundsInParent().getMaxX();
            } else {
                x = text.getBoundsInParent().getMinX();
            }
            //indicator
            text = bytePair.getHexText();
            indicatorX = text.getBoundsInParent().getMinX();
        }
        caretVM.setX(x);
        caretVM.setIndicatorX(indicatorX);
    }
}
