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

import atlantafx.base.theme.Styles;
import com.techsenger.annotations.Unmodifiable;
import com.techsenger.tabshell.core.page.PageFxView;
import com.techsenger.tabshell.core.page.PagePort;
import com.techsenger.tabshell.core.page.TreePageContainerFxView;
import com.techsenger.tabshell.core.page.TreePageDescriptor;
import com.techsenger.tabshell.core.page.TreePageItem;
import com.techsenger.tabshell.layout.style.LayoutIcons;
import com.techsenger.tabshell.material.icon.FontIconView;
import com.techsenger.tabshell.material.icon.IconViewBox;
import com.techsenger.tabshell.material.style.Spacing;
import com.techsenger.tabshell.material.style.StyleClasses;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
public class TreePageHostFxView<P extends TreePageHostPresenter<?>> extends AbstractPageHostFxView<P>
        implements TreePageContainerFxView<P>, TreePageHostView {

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


    public class Composer extends AbstractPageHostFxView<P>.Composer implements TreePageContainerFxView.Composer,
            TreePageHostView.Composer {

        private final TreePageHostFxView<P> view = TreePageHostFxView.this;

        /**
         * Created and initiliazed pages.
         */
        private final Map<TreePageItem, PageFxView<?>> pagesByItems = new HashMap<>();

        @Override
        public PagePort providePagePort(TreePageItem item) {
            var fxView = pagesByItems.get(item);
            if (fxView == null) {
                var treeItem = view.treeItemsByPageItem.get(item);
                fxView = treeItem.getValue().getFactory().createAndInitialize(item);
                getModifiableChildren().add(fxView);
                pagesByItems.put((TreePageDescriptor) item, fxView);
            }
            return fxView.getPresenter();
        }

        @Override
        public @Unmodifiable List<? extends PagePort> getPagePorts() {
            return pagesByItems.values().stream().map(c -> c.getPresenter()).toList();
        }

        @Override
        public void setPages(TreePageDescriptor root, boolean showRoot) {
            getModifiableChildren().removeAll(pagesByItems.values());
            pagesByItems.clear();
            getPresenter().setPages(root, showRoot);
        }
    }

    private final TreeView<TreePageDescriptor> pageTreeView = new TreeView<>();

    private final HBox breadcrumbsBox = new HBox();

    private final Map<TreePageItem, TreeItem<TreePageDescriptor>> treeItemsByPageItem = new HashMap<>();

    public TreePageHostFxView() {
        this(null);
    }

    public TreePageHostFxView(Callback<TreeView<TreePageDescriptor>, TreeCell<TreePageDescriptor>> clbck) {
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
    public void setMenu(TreePageItem root, boolean showRoot) {
        treeItemsByPageItem.clear();
        pageTreeView.setRoot(null);
        pageTreeView.setShowRoot(showRoot);
        if (root != null) {
            var treeRoot = buildTree(root, treeItemsByPageItem);
            pageTreeView.setRoot(treeRoot);
        }
    }

    @Override
    public void setMenu(FilteredTreePageItem root, boolean showRoot) {
        treeItemsByPageItem.clear();
        pageTreeView.setRoot(null);
        pageTreeView.setShowRoot(showRoot);
        if (root != null) {
            var treeRoot = buildTree(root, treeItemsByPageItem);
            pageTreeView.setRoot(treeRoot);
        }
    }

    @Override
    public void setPage(TreePageItem item) {
        var fxView = getComposer().pagesByItems.get(item);
        var treeItem = treeItemsByPageItem.get(item);
        setPage(fxView); // before selecting treeItem
        pageTreeView.getSelectionModel().select(treeItem); // it can be selected using breadcrumbs
        getContentBox().getChildren().clear();
        VBox.setVgrow(fxView.getNode(), Priority.ALWAYS);
        getContentBox().getChildren().add(fxView.getNode());
    }

    @Override
    public void setBreadcrumbs(List<PageBreadcrumb> breadcrumbs) {
        this.breadcrumbsBox.getChildren().clear();
        for (var i = 0; i < breadcrumbs.size(); i++) {
            var b = breadcrumbs.get(i);
            breadcrumbsBox.getChildren().add(createBreadcrumb(b));
            if (i + 1 < breadcrumbs.size()) {
                breadcrumbsBox.getChildren().add(createBreadcrumbDivider());
            }
        }
    }

    @Override
    protected Composer createComposer() {
        return new TreePageHostFxView.Composer();
    }

    @Override
    protected void build() {
        super.build();
        getLeftBox().getChildren().add(pageTreeView);
        pageTreeView.getStyleClass().addAll(Styles.DENSE, StyleClasses.NO_BORDER);
        pageTreeView.setShowRoot(false);
        VBox.setVgrow(pageTreeView, Priority.ALWAYS);

        getHeaderBox().getChildren().add(0, breadcrumbsBox);
        breadcrumbsBox.getStyleClass().add("breadcrumbs-box");
        breadcrumbsBox.setAlignment(Pos.CENTER_LEFT);
        breadcrumbsBox.setSpacing(Spacing.HORIZONTAL_THIRD);
        HBox.setHgrow(breadcrumbsBox, Priority.ALWAYS);
    }

    @Override
    protected void addListeners() {
        super.addListeners();
        pageTreeView.getSelectionModel().selectedItemProperty().addListener((ov, oldV, newV) -> {
            if (newV != null) {
                getPresenter().onPageRequested(newV.getValue());
            }
        });
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
            getPresenter().onPageRequested(breadcrumb);
        });
        return link;
    }

    protected Node createBreadcrumbDivider() {
        var node = new FontIconView(LayoutIcons.CHEVRON_RIGHT);
        node.getStyleClass().add(Styles.DENSE);
        return node;
    }
}
