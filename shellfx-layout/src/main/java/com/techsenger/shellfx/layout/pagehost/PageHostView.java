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

package com.techsenger.shellfx.layout.pagehost;

import com.techsenger.annotations.Unmodifiable;
import com.techsenger.shellfx.core.page.ContainerPagePort;
import com.techsenger.shellfx.core.page.PageDescriptor;
import com.techsenger.shellfx.core.page.PageItem;
import com.techsenger.shellfx.core.page.PageView;
import com.techsenger.shellfx.material.icon.IconViewBox;
import com.techsenger.shellfx.material.style.StyleClasses;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.util.Callback;
import com.techsenger.shellfx.core.page.PageContainerView;

/**
 *
 * @author Pavel Castornii
 */
public class PageHostView<VM extends PageHostViewModel<?>> extends AbstractPageHostView<VM>
        implements PageContainerView<VM> {

    public class Composer extends AbstractPageHostView<VM>.Composer implements PageContainerView.Composer,
            PageHostComposer {

        private final PageHostView<VM> view = PageHostView.this;

        /**
         * Created and initiliazed pages.
         */
        private final Map<PageItem, PageView<?>> pagesByItems = new HashMap<>();

        @Override
        public ContainerPagePort providePagePort(int index) {
            return providePage(index).getViewModel();
        }

        @Override
        public void setPages(List<PageDescriptor> pages) {
            getModifiableChildren().removeAll(pagesByItems.values());
            pagesByItems.clear();
            getViewModel().setPages((List<PageItem>) (List<?>) pages);
        }

        @Override
        public @Unmodifiable List<? extends ContainerPagePort> getPagePorts() {
            return pagesByItems.values().stream().map(c -> c.getViewModel()).toList();
        }

        PageView<?> providePage(int index) {
            var pageDescriptor = view.pageListView.getItems().get(index);
            var fxView = pagesByItems.get(pageDescriptor);
            if (fxView == null) {
                fxView = pageDescriptor.getFactory().create(pageDescriptor);
                getModifiableChildren().add(fxView);
                pagesByItems.put(pageDescriptor, fxView);
            }
            return fxView;
        }
    }

    private final ListView<PageDescriptor> pageListView = new ListView<>();

    private final Label titleLabel = new Label();

    private final HBox titleBox = new HBox(titleLabel);

    public PageHostView(VM viewModel) {
        this(viewModel, null);
    }

    public PageHostView(VM viewModel, Callback<ListView<PageDescriptor>, ListCell<PageDescriptor>> clbck) {
        super(viewModel);
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
    protected Composer createComposer() {
        return new PageHostView.Composer();
    }

    @Override
    protected void build() {
        super.build();
        getLeftBox().getChildren().add(pageListView);
        pageListView.getStyleClass().add(StyleClasses.NO_BORDER);
        VBox.setVgrow(pageListView, Priority.ALWAYS);
        pageListView.setItems((ObservableList<PageDescriptor>) (ObservableList<?>) getViewModel().getMenu());

        getHeaderBox().getChildren().add(0, titleBox);
        this.titleBox.getStyleClass().add("title-box");
        titleBox.setAlignment(Pos.CENTER_LEFT);
    }

    @Override
    protected void addListeners() {
        super.addListeners();
        var viewModel = getViewModel();
        pageListView.getSelectionModel().selectedIndexProperty().addListener((ov, oldV, newV) -> {
            if (newV != null) {
                viewModel.onPageRequested(newV.intValue());
            }
        });
        viewModel.selectPageSource().addListener((index) -> updateSelectedPage(index));
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

    private void updateSelectedPage(int index) {
        var fxView = getComposer().providePage(index);
        var descriptor = this.pageListView.getItems().get(index);
        var previous = getComposer().getPage();
        if (previous != null) {
            previous.getViewModel().setSelected(false);
        }
        getComposer().setPage(fxView); // before selecting item
        fxView.getViewModel().setSelected(true);
        pageListView.getSelectionModel().select(index); // it can be selected using breadcrumbs
        getContentBox().getChildren().clear();
        VBox.setVgrow(fxView.getNode(), Priority.ALWAYS);
        getContentBox().getChildren().add(fxView.getNode());
        this.titleLabel.setText(descriptor.getText());
    }
}
