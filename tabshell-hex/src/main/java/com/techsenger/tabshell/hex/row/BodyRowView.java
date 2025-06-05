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

package com.techsenger.tabshell.hex.row;

import com.techsenger.tabshell.core.style.StyleClasses;
import com.techsenger.tabshell.hex.AbstractHexEditorTabView;
import com.techsenger.tabshell.hex.CaretBytePosition;
import com.techsenger.tabshell.hex.CaretShape;
import com.techsenger.tabshell.hex.ColumnSeparator;
import com.techsenger.tabshell.hex.EditorPanel;
import com.techsenger.tabshell.hex.NumberBaseUtils;
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
public class BodyRowView extends AbstractRowView<BodyRowViewModel> implements Cell<Integer, Node> {

    /**
     * Only texts that represent bytes. Its size is always equal to max rowByteCount.
     */
    private final List<ByteTextPair> byteTextPairs = new ArrayList<>();

    public BodyRowView(BodyRowViewModel viewModel, AbstractHexEditorTabView<?> editor) {
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
        var row = vm.getEditor().createRowModel(offset);
        vm.setModel(row);
        removeCaret();
        if (offset != null) {
            getNode().setVisible(true);
            var offsetStr = NumberBaseUtils.convert(row.getOffset(), vm.getEditor().getOffsetNumberBase(),
                    vm.getEditor().getOffsetLength());
            getOffsetLabel().setText(offsetStr);
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
            if (getEditor().getCaret().getViewModel().getRowOffset() == offset) {
                vm.setFocused(true);
                addCaret();
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
        getHexPane().clear();
        //canvas width prevents resetting the panel width
        getHexPane().getCanvas().setWidth(0);
        getAsciiPane().clear();
        getAsciiPane().getCanvas().setWidth(0);
        this.byteTextPairs.clear();

        var editorViewModel = viewModel.getEditor();
        var charWidth = editorViewModel.getCharWidth();

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

    //todo: not public
    public List<ByteTextPair> getByteTextPairs() {
        return byteTextPairs;
    }

    //todo: not public
    public void removeCaret() {
        getHexPane().getCaretPane().getChildren().clear();
        getAsciiPane().getCaretPane().getChildren().clear();
    }

    //todo: not public
    public void addCaret() {
        if (getViewModel().isFocused()) {
            var caret = getEditor().getCaret();
            if (caret.getViewModel().getPanel() == EditorPanel.HEX) {
                getHexPane().getCaretPane().getChildren().add(caret.getNode());
                getAsciiPane().getCaretPane().getChildren().add(caret.getIndicator());
            } else {
                getHexPane().getCaretPane().getChildren().add(caret.getIndicator());
                getAsciiPane().getCaretPane().getChildren().add(caret.getNode());
            }
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
            var caretV = getEditor().getCaret();
            var caretVM = caretV.getViewModel();
            caretVM.setByteIndex(text.getPair().getIndex());
            var position = resolveHexPosition(text, e.getX(), caretVM.getShape());
            caretVM.setBytePosition(position);
            caretVM.setPanel(EditorPanel.HEX);
            caretV.move(this);
        });
        return text;
    }

    private CaretBytePosition resolveHexPosition(ByteText text, double x, CaretShape shape) {
        double textWidth = text.getLayoutBounds().getWidth();
        double widthHalf = textWidth / 2;
        if (x < widthHalf) {
            return CaretBytePosition.FIRST;
        } else {
            if (shape != CaretShape.BAR) {
                return CaretBytePosition.SECOND;
            }
            if (x < widthHalf + (widthHalf / 2)) {
                return CaretBytePosition.SECOND;
            } else {
                return CaretBytePosition.THIRD;
            }
        }
    }

    private ByteText createByteAsciiText() {
        var text = createByteText();
        text.setOnMouseClicked(e -> {
            var caretV = getEditor().getCaret();
            var caretVM = caretV.getViewModel();
            caretVM.setByteIndex(text.getPair().getIndex());
            var position = resolveAsciiPosition(text, e.getX(), caretVM.getShape(),
                    caretVM.getByteIndex() == caretVM.getRow().getModel().getByteCount() - 1);
            caretVM.setBytePosition(position);
            caretVM.setPanel(EditorPanel.ASCII);
            caretV.move(this);
        });
        return text;
    }

    private CaretBytePosition resolveAsciiPosition(ByteText text, double x, CaretShape shape, boolean lastByte) {
        if (shape != CaretShape.BAR) {
            return CaretBytePosition.FIRST;
        }
        double textWidth = text.getLayoutBounds().getWidth();
        double widthHalf = textWidth / 2;
        if (lastByte) {
            if (x < widthHalf) {
                return CaretBytePosition.FIRST;
            } else {
                return CaretBytePosition.THIRD;
            }
        } else {
            return CaretBytePosition.FIRST;
        }
    }

    private ByteText createByteText() {
        var text = new ByteText();
        text.getStyleClass().add("text");
        return text;
    }
}
