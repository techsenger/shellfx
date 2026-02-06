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
//package com.techsenger.tabshell.dialogs.page;
//
//import atlantafx.base.theme.Styles;
//import com.techsenger.tabshell.core.page.PageView;
//import com.techsenger.tabshell.dialogs.simple.AbstractSimpleDialogView;
//import javafx.scene.control.ListCell;
//import javafx.scene.control.ListView;
//import javafx.scene.layout.VBox;
//
///**
// *
// * @author Pavel Castornii
// */
//public abstract class AbstractPageDialogView<T extends AbstractPageDialogViewModel<?>,
//        S extends AbstractPageDialogComponent<?>> extends AbstractSimpleDialogView<T, S> {
//
//    private final ListView<PageView<?, ?>> pageListView = new ListView<>();
//
//    private final VBox pageContainer = new VBox();
//
//    public AbstractPageDialogView(T viewModel) {
//        super(viewModel);
//    }
//
//    protected ListView<PageView<?, ?>> getPageListView() {
//        return pageListView;
//    }
//
//    protected VBox getPageContainer() {
//        return pageContainer;
//    }
//
//    @Override
//    protected void build() {
//        super.build();
//        pageListView.getStyleClass().add(Styles.DENSE);
//        pageListView.setCellFactory(lv -> new ListCell<>() {
//            @Override
//            protected void updateItem(PageView<?, ?> page, boolean empty) {
//                super.updateItem(page, empty);
//                if (empty || page == null) {
//                    setText(null);
//                } else {
//                    setText(page.getViewModel().getTitle());
//                }
//            }
//        });
//    }
//
//    @Override
//    protected void addListeners() {
//        super.addListeners();
//        pageListView.getSelectionModel().selectedItemProperty().addListener((ov, oldV, newV) -> {
//            getFocusTrap().deactivate();
//            this.pageContainer.getChildren().clear();
//            if (oldV != null) {
//                oldV.doOnDeselected();
//            }
//            if (newV != null) {
//                this.pageContainer.getChildren().add(newV.getNode());
//                newV.doOnSelected();
//                getFocusTrap().activate();
//            }
//        });
//    }
//}
