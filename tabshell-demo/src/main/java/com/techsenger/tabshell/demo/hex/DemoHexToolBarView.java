package com.techsenger.tabshell.demo.hex;

///*
// * Copyright 2024-2026 Pavel Castornii.
// *
// * Licensed under the Apache License, Version 2.0 (the "License");
// * you may not use this file except in compliance with the License.
// * You may obtain a copy of the License at
// *
// *     http://www.apache.org/licenses/LICENSE-2.0
// *
// * Unless required by applicable law or agreed to in writing, software
// * distributed under the License is distributed on an "AS IS" BASIS,
// * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// * See the License for the specific language governing permissions and
// * limitations under the License.
// */
//
//package com.techsenger.tabshell.demos.full.hex;
//
//import atlantafx.base.theme.Styles;
//import com.techsenger.tabshell.hex.editor.CaretShape;
//import com.techsenger.tabshell.hex.editor.ColumnSeparator;
//import com.techsenger.tabshell.hex.editor.HexToolBarView;
//import javafx.geometry.Orientation;
//import javafx.scene.control.ComboBox;
//import javafx.scene.control.Separator;
//import javafx.scene.control.Tooltip;
//
///**
// *
// * @author Pavel Castornii
// */
//public class DemoHexToolBarView extends HexToolBarView<DemoHexToolBarViewModel, DemoHexToolBarComponent> {
//
//    private final ComboBox<CaretShape> caretShapeComboBox = new ComboBox<>();
//
//    private final ComboBox<ColumnSeparator> columnSeparatorComboBox = new ComboBox<>();
//
//    public DemoHexToolBarView(DemoHexToolBarViewModel viewModel) {
//        super(viewModel);
//    }
//
//    public ComboBox<CaretShape> getCaretShapeComboBox() {
//        return caretShapeComboBox;
//    }
//
//    public ComboBox<ColumnSeparator> getColumnSeparatorComboBox() {
//        return columnSeparatorComboBox;
//    }
//
//    @Override
//    protected void build() {
//        super.build();
//        var viewModel = getViewModel();
//        this.caretShapeComboBox.setItems(viewModel.getCaretShapes());
//        this.caretShapeComboBox.getStyleClass().add(Styles.DENSE);
//        this.caretShapeComboBox.setTooltip(new Tooltip("Caret Shape"));
//
//        this.columnSeparatorComboBox.setItems(viewModel.getColumnSeparators());
//        this.columnSeparatorComboBox.getStyleClass().add(Styles.DENSE);
//        this.columnSeparatorComboBox.setTooltip(new Tooltip("Column Separator"));
//
//        getNode().getItems().addAll(
//            new Separator(Orientation.VERTICAL),
//            this.columnSeparatorComboBox,
//            this.caretShapeComboBox
//        );
//    }
//}
