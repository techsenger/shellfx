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
import com.techsenger.tabshell.core.style.StyleClasses;
import java.util.ArrayList;
import java.util.List;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.text.Text;
import org.fxmisc.flowless.Cell;

/**
 *
 * @author Pavel Castornii
 */
public class RowView extends AbstractNodeView<RowViewModel> implements Cell<Integer, Node> {

    private final AbstractHexEditorTabView<?> editor;

    /**
     * This flow has only one text node.
     */
    private final Label offsetLabel = new Label();

    /**
     * Stack pane for hex panel.
     */
    private final PanelRowPane hexPane = new PanelRowPane();

    /**
     * Stack pane for ascii panel.
     */
    private final PanelRowPane asciiPane = new PanelRowPane();

    /**
     * Only texts that represent bytes. Its size is always equal to max rowByteCount.
     */
    private final List<ByteTextPair> byteTextPairs = new ArrayList<>();

    /**
     * The root node of the row.
     */
    private final HBox root = new HBox(offsetLabel, hexPane, asciiPane);

    RowView(RowViewModel viewModel, AbstractHexEditorTabView<?> editor) {
        super(viewModel);
        this.editor = editor;
    }

    @Override
    public void requestFocus() {

    }

    @Override
    public void updateItem(Integer offset) {
        var vm = getViewModel();
        var row = vm.getEditor().createRowModel(offset);
        vm.setModel(row);
        if (vm.shoulRebuilRow()) {
            this.updateRowPanes();
        }
        removeCaret();
        if (offset != null) {
            this.root.setVisible(true);
            offsetLabel.setText(row.getHexOffset());

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
            if (this.editor.getCaret().getViewModel().getRowOffset() == offset) {
                vm.setFocused(true);
                addCaret();
            } else {
                vm.setFocused(false);
            }
        } else {
            vm.setFocused(false);
            this.root.setVisible(false);
        }
    }

    @Override
    public HBox getNode() {
        return root;
    }

    @Override
    public boolean isReusable() {
        return true;
    }

    @Override
    protected void build(RowViewModel viewModel) {
        super.build(viewModel);
        this.offsetLabel.getStyleClass().add("offset-label");
        this.offsetLabel.setMinWidth(Region.USE_PREF_SIZE);

        this.hexPane.getStyleClass().add("hex-pane");
        this.asciiPane.getStyleClass().add("ascii-pane");
        HBox.setHgrow(this.asciiPane, Priority.ALWAYS);

        this.root.getStyleClass().add(StyleClasses.MONOSPACE);
    }

    @Override
    protected void addListeners(RowViewModel viewModel) {
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
    protected void postInitialize(RowViewModel viewModel) {
        super.postInitialize(viewModel);
        updateItem(viewModel.getModel().getOffset());
    }

    List<ByteTextPair> getByteTextPairs() {
        return byteTextPairs;
    }

    void removeCaret() {
        this.hexPane.getCaretPane().getChildren().clear();
        this.asciiPane.getCaretPane().getChildren().clear();
    }

    void addCaret() {
        if (getViewModel().isFocused()) {
            var caret = editor.getCaret();
            if (caret.getViewModel().getPanel() == EditorPanel.HEX) {
                this.hexPane.getCaretPane().getChildren().add(caret.getNode());
                this.asciiPane.getCaretPane().getChildren().add(caret.getIndicator());
            } else {
                this.hexPane.getCaretPane().getChildren().add(caret.getIndicator());
                this.asciiPane.getCaretPane().getChildren().add(caret.getNode());
            }
        }
    }

    AbstractHexEditorTabView<?> getEditor() {
        return editor;
    }

    ByteText getText(EditorPanel panel, int byteIndex) {
        if (panel == EditorPanel.HEX) {
            return this.byteTextPairs.get(byteIndex).getHexText();
        } else {
            return this.byteTextPairs.get(byteIndex).getAsciiText();
        }
    }

    private void updateRowPanes() {
        getViewModel().updateColumnCount();
        this.hexPane.clear();
        this.asciiPane.clear();
        this.byteTextPairs.clear();

        var hexContentBox = this.hexPane.getContentBox();
        var charWidth = getViewModel().getEditor().getCharWidth();
        hexContentBox.setPadding(new Insets(0, 0, 0, charWidth));
        hexContentBox.setSpacing(charWidth);
        List<Text> asciiTexts = new ArrayList<>();
        for (var c = 0; c < getViewModel().getColumnCount(); c++) {
            for (var i = 0; i < AbstractHexEditorTabViewModel.COLUMN_BYTE_COUNT; i++) {
                var hexText = createByteHexText();
                hexContentBox.getChildren().add(hexText);
                var asciiText = createByteAsciiText();
                asciiTexts.add(asciiText);
                this.byteTextPairs.add(new ByteTextPair(this, hexText, asciiText));
                if (i + 1 == AbstractHexEditorTabViewModel.COLUMN_BYTE_COUNT) {
                    //we use regions as they stretch
                    var line = new Region();
                    line.getStyleClass().add("line");
                    hexContentBox.getChildren().add(line);
                }
            }
        }

        var asciiContentBox = this.asciiPane.getContentBox();
        asciiContentBox.setPadding(new Insets(0, 0, 0, charWidth));
        asciiContentBox.getChildren().addAll(asciiTexts);
    }

    private ByteText createByteHexText() {
        var text = createByteText();
        text.setOnMouseClicked(e -> {
            var rowIndex = getViewModel().getEditor().calculateRowIndex(getViewModel().getModel().getOffset());
            this.editor.moveCaretTo(EditorPanel.HEX, this, rowIndex, text.getPair().getIndex(),
                    text.getPartAt(e.getX()));
        });
        return text;
    }

    private ByteText createByteAsciiText() {
        var text = createByteText();
        text.setOnMouseClicked(e -> {
            var x = text.getBoundsInParent().getMinX();
            var rowIndex = getViewModel().getEditor().calculateRowIndex(getViewModel().getModel().getOffset());
            this.editor.moveCaretTo(EditorPanel.ASCII, this, rowIndex, text.getPair().getIndex(), null);
        });
        return text;
    }

    private ByteText createByteText() {
        var text = new ByteText();
        text.getStyleClass().add("text");
        return text;
    }
}
