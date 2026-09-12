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
import com.techsenger.shellfx.core.page.PageView;
import com.techsenger.shellfx.core.page.TreePageContainerView;
import com.techsenger.shellfx.core.page.TreePageDescriptor;
import com.techsenger.shellfx.core.page.TreePageItem;
import com.techsenger.shellfx.layout.style.LayoutIcons;
import com.techsenger.shellfx.material.icon.FontIconView;
import com.techsenger.shellfx.material.icon.IconViewBox;
import com.techsenger.shellfx.material.style.Spacing;
import com.techsenger.shellfx.material.style.StyleClasses;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javafx.collections.ListChangeListener;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.util.Callback;

/**
 *
 * @author Pavel Castornii
 */
public class TreePageHostView<VM extends TreePageHostViewModel<?>> extends AbstractPageHostView<VM>
        implements TreePageContainerView<VM> {

    private static TreeItem<TreePageDescriptor> buildTree(TreePageItem root,
            Map<TreePageItem, TreeItem<TreePageDescriptor>> treeItemsByItems) {
        var treeItem = new TreeItem<TreePageDescriptor>((TreePageDescriptor) root);
        treeItemsByItems.put(root, treeItem);
        if (root.getChildren() != null && !root.getChildren().isEmpty()) {
            root.getChildren().stream()
                .map(child -> buildTree(child, treeItemsByItems))
                .forEach(treeItem.getChildren()::add);
        }
        return treeItem;
    }

    private static TreeItem<TreePageDescriptor> buildTree(FilteredTreePageItem root,
            Map<TreePageItem, TreeItem<TreePageDescriptor>> treeItemsByItems) {
        var treeItem = new TreeItem<TreePageDescriptor>((TreePageDescriptor) root.getOriginal());
        if (!root.getChildren().isEmpty()) {
            treeItem.setExpanded(true);
        }
        treeItemsByItems.put(root.getOriginal(), treeItem);
        if (root.getChildren() != null && !root.getChildren().isEmpty()) {
            root.getChildren().stream()
                .map(child -> buildTree(child, treeItemsByItems))
                .forEach(treeItem.getChildren()::add);
        }
        return treeItem;
    }

    public class Composer extends AbstractPageHostView<VM>.Composer implements TreePageContainerView.Composer,
            TreePageHostComposer {

        private final TreePageHostView<VM> view = TreePageHostView.this;

        /**
         * Created and initiliazed pages.
         */
        private final Map<TreePageItem, PageView<?>> pagesByItems = new HashMap<>();

        @Override
        public ContainerPagePort providePagePort(TreePageItem item) {
            var fxView = pagesByItems.get(item);
            if (fxView == null) {
                var treeItem = view.treeItemsByPageItem.get(item);
                fxView = treeItem.getValue().getFactory().create(item);
                getModifiableChildren().add(fxView);
                pagesByItems.put((TreePageDescriptor) item, fxView);
            }
            return fxView.getViewModel();
        }

        @Override
        public @Unmodifiable List<? extends ContainerPagePort> getPagePorts() {
            return pagesByItems.values().stream().map(c -> c.getViewModel()).toList();
        }

        @Override
        public void setPages(TreePageDescriptor root, boolean showRoot) {
            getModifiableChildren().removeAll(pagesByItems.values());
            pagesByItems.clear();
            getViewModel().setPages(root, showRoot);
        }
    }

    private final TreeView<TreePageDescriptor> pageTreeView = new TreeView<>();

    private final HBox breadcrumbsBox = new HBox();

    private final Map<TreePageItem, TreeItem<TreePageDescriptor>> treeItemsByPageItem = new HashMap<>();

    public TreePageHostView(VM viewModel) {
        this(viewModel, null);
    }

    public TreePageHostView(VM viewModel,
            Callback<TreeView<TreePageDescriptor>, TreeCell<TreePageDescriptor>> clbck) {
        super(viewModel);
        if (clbck != null) {
            pageTreeView.setCellFactory(clbck);
        } else {
            pageTreeView.setCellFactory(tv -> new TreeCell<>() {
                @Override
                protected void updateItem(TreePageDescriptor item, boolean empty) {
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
        return new TreePageHostView.Composer();
    }

    @Override
    protected void build() {
        super.build();
        getLeftBox().getChildren().add(pageTreeView);
        pageTreeView.getStyleClass().add(StyleClasses.NO_BORDER);
        pageTreeView.setShowRoot(false);
        VBox.setVgrow(pageTreeView, Priority.ALWAYS);

        getHeaderBox().getChildren().add(0, breadcrumbsBox);
        breadcrumbsBox.getStyleClass().add("breadcrumbs-box");
        breadcrumbsBox.setAlignment(Pos.CENTER_LEFT);
        breadcrumbsBox.setSpacing(Spacing.getHorizontalThird());
        HBox.setHgrow(breadcrumbsBox, Priority.ALWAYS);
    }

    @Override
    protected void addListeners() {
        super.addListeners();
        var viewModel = getViewModel();
        pageTreeView.getSelectionModel().selectedItemProperty().addListener((ov, oldV, newV) -> {
            if (newV != null) {
                viewModel.onPageRequested(newV.getValue());
            }
        });
        viewModel.updateMenuSource().addListener((root) -> updateMenu(root, viewModel.isShowRoot()));
        viewModel.refreshMenuSource().addListener((root) -> updateFilteredMenu(root, viewModel.isShowRoot()));
        updateBreadcrumbs(viewModel.getBreadcrumbs());
        viewModel.getBreadcrumbs().addListener(
                (ListChangeListener<PageBreadcrumb>) change -> updateBreadcrumbs(viewModel.getBreadcrumbs()));
        viewModel.selectPageSource().addListener((item) -> updateSelectedPage(item));
    }

    protected TreeView<TreePageDescriptor> getPageTreeView() {
        return pageTreeView;
    }

    protected HBox getBreadcrumbsBox() {
        return breadcrumbsBox;
    }

    protected Node createBreadcrumb(PageBreadcrumb breadcrumb) {
        var item = breadcrumb.getItem();
        var link = new Hyperlink(item.getText());
        if (breadcrumb.getItem().getIcon() != null) {
            link.setGraphic(new IconViewBox(item.getIcon()));
        }
        link.setOnAction(e -> {
            getViewModel().onPageRequested(breadcrumb);
        });
        return link;
    }

    protected Node createBreadcrumbDivider() {
        var node = new FontIconView(LayoutIcons.CHEVRON_RIGHT);
        node.getStyleClass().add(StyleClasses.SIZE_S);
        return node;
    }

    private void updateSelectedPage(TreePageItem item) {
        if (!treeItemsByPageItem.containsKey(item)) {
            return;
        }
        getComposer().providePagePort(item);
        var fxView = getComposer().pagesByItems.get(item);
        var treeItem = treeItemsByPageItem.get(item);
        var previous = getComposer().getPage();
        if (previous != null) {
            previous.getViewModel().setSelected(false);
        }
        getComposer().setPage(fxView); // before selecting treeItem
        fxView.getViewModel().setSelected(true);
        pageTreeView.getSelectionModel().select(treeItem); // it can be selected using breadcrumbs
        getContentBox().getChildren().clear();
        VBox.setVgrow(fxView.getNode(), Priority.ALWAYS);
        getContentBox().getChildren().add(fxView.getNode());
    }

    private void updateMenu(TreePageItem root, boolean showRoot) {
        treeItemsByPageItem.clear();
        pageTreeView.setRoot(null);
        pageTreeView.setShowRoot(showRoot);
        if (root != null) {
            var treeRoot = buildTree(root, treeItemsByPageItem);
            pageTreeView.setRoot(treeRoot);
        }
    }

    private void updateFilteredMenu(FilteredTreePageItem root, boolean showRoot) {
        treeItemsByPageItem.clear();
        pageTreeView.setRoot(null);
        pageTreeView.setShowRoot(showRoot);
        if (root != null) {
            var treeRoot = buildTree(root, treeItemsByPageItem);
            pageTreeView.setRoot(treeRoot);
        }
    }

    private void updateBreadcrumbs(List<PageBreadcrumb> breadcrumbs) {
        this.breadcrumbsBox.getChildren().clear();
        for (var i = 0; i < breadcrumbs.size(); i++) {
            var b = breadcrumbs.get(i);
            breadcrumbsBox.getChildren().add(createBreadcrumb(b));
            if (i + 1 < breadcrumbs.size()) {
                breadcrumbsBox.getChildren().add(createBreadcrumbDivider());
            }
        }
    }
}
