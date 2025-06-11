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
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;

/**
 *
 * @author Pavel Castornii
 */
class HeaderRowView extends AbstractRowView<HeaderRowViewModel> {

    private final HBox node = new HBox();

    private final HBox scrollableBox = super.getNode();

    private final List<Label> contentLabels = new ArrayList<>();

    HeaderRowView(HeaderRowViewModel viewModel, AbstractHexEditorTabView<?> editor) {
        super(viewModel, editor);
    }

    @Override
    public void rebuild() {
        var editorVM = getViewModel().getEditor();
        var charWidth = editorVM.getCharSize().getWidth();

        getInfoLabel().setPadding(new Insets(0, charWidth, 0, charWidth));

        getHexPane().clear();
        //canvas width prevents resetting the panel width
        getHexPane().getCanvas().setWidth(0);
        getAsciiPane().clear();
        getAsciiPane().getCanvas().setWidth(0);

        var labelText = "x".repeat(editorVM.getOffsetLength());
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

        for (byte i = 0; i < editorVM.getRowByteCount(); i++) {
            if (editorVM.areColumnsEnabled() && i != 0 && i % editorVM.getColumnByteCount() == 0) {
                if (editorVM.getColumnSeparator() == ColumnSeparator.SPACE) {
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
            var contentLabel = new Label(NumberBaseUtils.convert(i, editorVM.getOffsetNumberBase(), 2));
            contentLabel.getStyleClass().add("content");
            contentLabel.setLineSpacing(0);
            children.add(contentLabel);
            contentLabels.add(contentLabel);
        }
    }

    @Override
    protected void build(HeaderRowViewModel viewModel) {
        super.build(viewModel);
        this.scrollableBox.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(this.scrollableBox, Priority.ALWAYS);
        this.scrollableBox.getStyleClass().addAll("scrollable-box", StyleClasses.MONOSPACE);

        this.node.setMaxWidth(Double.MAX_VALUE);
        this.node.setMinWidth(0);
        this.node.getChildren().add(this.scrollableBox);
        this.node.getStyleClass().add("header-row");

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
     * padding to labels and using a background color and insets that simulate padding — without actually applying any.
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
