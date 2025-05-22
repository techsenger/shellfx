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
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.text.Text;
import org.fxmisc.flowless.Cell;

/**
 *
 * @author Pavel Castornii
 */
class RowView extends AbstractNodeView<RowViewModel> implements Cell<Integer, Node> {

    private final HexEditorTabView editor;

    /**
     * This flow has only one text node.
     */
    private final Label offsetLabel = new Label();

    /**
     * This pane is used for showing mouse position.
     */
    private final Pane mousePane = new Pane();

    /**
     * For caret and its indicator one pane is used.
     */
    private final Pane caretPane = new Pane();

    /**
     * This box has text nodes (with spaces and with byte values) and lines.
     */
    private final HBox hexBox = new HBox();

    /**
     * Contains {@link #hexBox} and ASCII text nodes.
     */
    private final HBox contentBox = new HBox();

    /**
     * Contains all panes for content and working with it.
     */
    private final StackPane stackPane = new StackPane(mousePane, caretPane, contentBox);

    /**
     * Only texts that represent bytes.
     */
    private final List<ByteTextPair> byteTextPairs = new ArrayList<>();

    /**
     * The root node of the row.
     */
    private final HBox root = new HBox(offsetLabel, stackPane);

    RowView(RowViewModel viewModel, HexEditorTabView editor) {
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
        this.caretPane.setMouseTransparent(true);
        this.caretPane.getStyleClass().add("caret-pane");
        HBox.setHgrow(this.hexBox, Priority.ALWAYS);
        this.hexBox.getStyleClass().add("hex-box");
        this.contentBox.getStyleClass().add("content-box");
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
        this.caretPane.getChildren().clear();
    }

    void addCaret() {
        if (getViewModel().isFocused()) {
            var caret = editor.getCaret();
            var caretVM = caret.getViewModel();
            caret.getNode().setTranslateX(caret.getViewModel().getX());
            this.caretPane.getChildren().add(caret.getNode());
            this.caretPane.getChildren().add(caret.getIndicator());
            caret.getIndicator().setHeight(this.root.getHeight());
        }
    }

    void removeMousePairHighlighter() {
        this.mousePane.getChildren().clear();
    }

    void addMousePairHighlighter() {
//        if (getViewModel().isFocused()) {
//            this.caretHightlightPane.getChildren().add(getEditor().ge)
//        }
    }

    HexEditorTabView getEditor() {
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
        getViewModel().setColumnCount(getViewModel().getEditor().getColumnCount());
        this.hexBox.getChildren().clear();
        this.contentBox.getChildren().clear();
        this.mousePane.getChildren().clear();
        this.caretPane.getChildren().clear();

        this.byteTextPairs.clear();
        var charWidth = getViewModel().getEditor().getCharWidth();
        hexBox.setPadding(new Insets(0, charWidth, 0, charWidth));
        hexBox.setSpacing(charWidth);
        List<Text> asciiTexts = new ArrayList<>();
        for (var c = 0; c < getViewModel().getColumnCount(); c++) {
            for (var i = 0; i < HexEditorTabViewModel.COLUMN_BYTE_COUNT; i++) {
                var hexText = createByteHexText();
                hexBox.getChildren().add(hexText);
                var asciiText = createByteAsciiText();
                asciiTexts.add(asciiText);
                this.byteTextPairs.add(new ByteTextPair(this, hexText, asciiText));
                if (i + 1 == HexEditorTabViewModel.COLUMN_BYTE_COUNT) {
                    //we use regions as they stretch
                    var line = new Region();
                    line.getStyleClass().add("line");
                    hexBox.getChildren().add(line);
                }
            }
        }
        this.contentBox.getChildren().add(this.hexBox);
        this.contentBox.getChildren().addAll(asciiTexts);
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
