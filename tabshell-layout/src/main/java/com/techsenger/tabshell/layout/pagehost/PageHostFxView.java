/*
 * Copyright 2024-2026 Pavel Castornii.
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

package com.techsenger.tabshell.layout.pagehost;

import com.techsenger.annotations.Unmodifiable;
import com.techsenger.tabshell.core.page.PageContainerFxView;
import com.techsenger.tabshell.core.page.PageDescriptor;
import com.techsenger.tabshell.core.page.PageFxView;
import com.techsenger.tabshell.core.page.PageItem;
import com.techsenger.tabshell.core.page.PagePort;
import com.techsenger.tabshell.material.icon.IconViewBox;
import com.techsenger.tabshell.material.style.StyleClasses;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javafx.collections.FXCollections;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.util.Callback;

/**
 *
 * @author Pavel Castornii
 */
public class PageHostFxView<P extends PageHostPresenter<?>> extends AbstractPageHostFxView<P>
        implements PageContainerFxView<P>, PageHostView {

    public class Composer extends AbstractPageHostFxView<P>.Composer implements PageContainerFxView.Composer,
            PageHostView.Composer {

        private final PageHostFxView<P> view = PageHostFxView.this;

        /**
         * Created and initiliazed pages.
         */
        private final Map<PageItem, PageFxView<?>> pagesByItems = new HashMap<>();

        @Override
        public PagePort providePagePort(int index) {
            var pageDescriptor = view.pageListView.getItems().get(index);
            var fxView = pagesByItems.get(pageDescriptor);
            if (fxView == null) {
                fxView = pageDescriptor.getFactory().createAndInitialize(pageDescriptor);
                getModifiableChildren().add(fxView);
                pagesByItems.put(pageDescriptor, fxView);
            }
            return fxView.getPresenter();
        }

        @Override
        public void setPages(List<PageDescriptor> pages) {
            getModifiableChildren().removeAll(pagesByItems.values());
            pagesByItems.clear();
            getPresenter().setPages((List<PageItem>) (List<?>) pages);
        }

        @Override
        public @Unmodifiable List<? extends PagePort> getPagePorts() {
            return pagesByItems.values().stream().map(c -> c.getPresenter()).toList();
        }
    }

    private final ListView<PageDescriptor> pageListView = new ListView<>();

    private final Label titleLabel = new Label();

    private final HBox titleBox = new HBox(titleLabel);

    public PageHostFxView() {
        this(null);
    }

    public PageHostFxView(Callback<ListView<PageDescriptor>, ListCell<PageDescriptor>> clbck) {
        if (clbck != null) {
            pageListView.setCellFactory(clbck);
        } else {
            pageListView.setCellFactory(tv -> new ListCell<>() {
                @Override
                protected void updateItem(PageDescriptor item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText(null);
                        setGraphic(null);
                    } else {
                        setText(item.getText());
                        if (item.getIcon() != null) {
                            setGraphic(new IconViewBox(item.getIcon()));
                        } else {
                            setGraphic(null);
                        }
                    }
                }
            });
        }
    }

    @Override
    public Composer getComposer() {
        return (Composer) super.getComposer();
    }

    @Override
    public void setMenu(List<PageItem> items) {
        pageListView.setItems(FXCollections.observableArrayList((List<PageDescriptor>) (List<?>) items));
    }

    @Override
    public void setPage(int index) {
        var descriptor = this.pageListView.getItems().get(index);
        var fxView = getComposer().pagesByItems.get(descriptor);
        setPage(fxView); // before selecting item
        pageListView.getSelectionModel().select(index); // it can be selected using breadcrumbs
        getContentBox().getChildren().clear();
        VBox.setVgrow(fxView.getNode(), Priority.ALWAYS);
        getContentBox().getChildren().add(fxView.getNode());
        this.titleLabel.setText(descriptor.getText());
    }

    @Override
    protected Composer createComposer() {
        return new PageHostFxView.Composer();
    }

    @Override
    protected void build() {
        super.build();
        getLeftBox().getChildren().add(pageListView);
        pageListView.getStyleClass().add(StyleClasses.NO_BORDER);
        VBox.setVgrow(pageListView, Priority.ALWAYS);

        getHeaderBox().getChildren().add(0, titleBox);
        this.titleBox.getStyleClass().add("title-box");
        titleBox.setAlignment(Pos.CENTER_LEFT);
    }

    @Override
    protected void addListeners() {
        super.addListeners();
        pageListView.getSelectionModel().selectedIndexProperty().addListener((ov, oldV, newV) -> {
            if (newV != null) {
                getPresenter().onPageRequested(newV.intValue());
            }
        });
    }

    protected ListView<PageDescriptor> getPageListView() {
        return pageListView;
    }

    public Label getTitleLabel() {
        return titleLabel;
    }

    protected HBox getTitleBox() {
        return titleBox;
    }
}
