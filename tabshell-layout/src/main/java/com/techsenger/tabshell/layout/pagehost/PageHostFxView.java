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

import com.techsenger.patternfx.core.ComponentName;
import com.techsenger.tabshell.core.area.AbstractAreaFxView;
import com.techsenger.tabshell.core.page.PageContainerFxView;
import com.techsenger.tabshell.core.page.PageFxView;
import com.techsenger.tabshell.core.page.PagePort;
import com.techsenger.tabshell.material.icon.IconViewBox;
import com.techsenger.tabshell.material.style.SizeConstants;
import com.techsenger.tabshell.material.style.StyleClasses;
import com.techsenger.tabshell.shared.style.SharedIcons;
import com.techsenger.toolkit.fx.utils.NodeUtils;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.SplitPane;
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
public class PageHostFxView<P extends PageHostPresenter<?, ?>> extends AbstractAreaFxView<P>
        implements PageContainerFxView<P>, PageHostView {

    private static TreeItem<PageDescriptor> buildTree(PageDescriptor root) {
        var treeItem = new TreeItem<PageDescriptor>(root);
        if (root.getChildren() != null && !root.getChildren().isEmpty()) {
            root.getChildren().stream()
                .map(child -> buildTree(child))
                .forEach(treeItem.getChildren()::add);
        }
        return treeItem;
    }

    public class Composer extends AbstractAreaFxView<P>.Composer implements PageContainerFxView.Composer,
            PageHostComposer {

        private final PageHostFxView<P> view = PageHostFxView.this;

        @Override
        public void compose() {
            super.compose();

            var findV = createFindPanel();
            findV.getPresenter().initialize();
            getModifiableChildren().add(findV);
            view.leftBox.getChildren().add(0, findV.getNode());
        }

        @Override
        public void selectPage(ComponentName page) {
            var fxView = view.pagesByName.get(page);
            var treeItem = view.itemRegister.getItem(page);
            if (fxView == null) {
                fxView = treeItem.getValue().getFactory().create();
                fxView.getPresenter().initialize();
                getModifiableChildren().add(fxView);
                view.pagesByName.put(page, fxView);
            }
            view.pageTreeView.getSelectionModel().select(treeItem); // it can be selected using breadcrumbs
            view.showPage(fxView);
        }

        @Override
        public PagePort getSelectedPage() {
            return view.page == null ? null : view.page.getPresenter();
        }

        public void setPages(PageDescriptor root, boolean showRoot) {
            getModifiableChildren().removeAll(view.pagesByName.values());
            var treeRoot = buildTree(root);
            view.itemRegister = new TreeItemRegister(treeRoot);
            pageTreeView.setRoot(treeRoot);
            pageTreeView.setShowRoot(showRoot);
        }

        protected FindPanelFxView<?> createFindPanel() {
            var view = new FindPanelFxView<>();
            var presenter = new FindPanelPresenter<>(view);
            return view;
        }
    }

    private final TreeView<PageDescriptor> pageTreeView = new TreeView<>();

    private final VBox leftBox = new VBox(pageTreeView);

    private final HBox breadcrumbsBox = new HBox();

    private final HBox headerBox = new HBox(breadcrumbsBox);

    private final VBox contentBox = new VBox();

    private final VBox rightBox = new VBox(headerBox, contentBox);

    private final SplitPane splitPane = new SplitPane(leftBox, rightBox);

    /**
     * Created and initiliazed pages.
     */
    private final Map<ComponentName, PageFxView<?>> pagesByName = new HashMap<>();

    private TreeItemRegister itemRegister;

    private PageFxView<?> page;

    public PageHostFxView() {
        this(null);
    }

    public PageHostFxView(Callback<TreeView<PageDescriptor>, TreeCell<PageDescriptor>> clbck) {
        if (clbck != null) {
            pageTreeView.setCellFactory(clbck);
        } else {
            pageTreeView.setCellFactory(tv -> new TreeCell<>() {
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
    public void requestFocus() {
        NodeUtils.requestFocus(this.pageTreeView);
    }

    @Override
    public SplitPane getNode() {
        return this.splitPane;
    }

    @Override
    public Composer getComposer() {
        return (Composer) super.getComposer();
    }

    @Override
    public void setDividerPosition(double pos) {
        this.splitPane.getDividers().get(0).setPosition(pos);
    }

    public void setShowRoot(boolean value) {
        this.pageTreeView.setShowRoot(value);
    }

    public void setContentPadding(Insets padding) {
        this.contentBox.setPadding(padding);
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
        return new PageHostFxView.Composer();
    }

    @Override
    protected void build() {
        super.build();
        this.leftBox.getStyleClass().add("left-box");
        SplitPane.setResizableWithParent(leftBox, false);
        pageTreeView.getStyleClass().addAll(StyleClasses.EXTRA_DENSE, StyleClasses.NO_BORDER);
        pageTreeView.setShowRoot(false);
        VBox.setVgrow(pageTreeView, Priority.ALWAYS);

        headerBox.getStyleClass().add("header-box");
        headerBox.setPadding(new Insets(SizeConstants.INSET));
        breadcrumbsBox.getStyleClass().add("breadcrumbs-box");
        HBox.setHgrow(breadcrumbsBox, Priority.ALWAYS);
        VBox.setVgrow(contentBox, Priority.ALWAYS);
        this.contentBox.getStyleClass().add("content-box");
        this.rightBox.getStyleClass().add("right-box");

        var css = PageHostFxView.class.getResource("page-host.css").toExternalForm();
        this.splitPane.getStylesheets().add(css);
    }

    @Override
    protected void addListeners() {
        super.addListeners();
        pageTreeView.getSelectionModel().selectedItemProperty().addListener((ov, oldV, newV) -> {
            getPresenter().onPageRequested(newV.getValue().getName(), newV.getValue());
        });
        splitPane.getDividers().getFirst().positionProperty()
                .addListener((ov, oldV, newV) -> getPresenter().onDividerPositionChanged(newV.doubleValue()));
    }

    protected TreeView<? extends PageItem> getPageTreeView() {
        return pageTreeView;
    }

    protected VBox getLeftBox() {
        return leftBox;
    }

    protected VBox getContentBox() {
        return contentBox;
    }

    protected HBox getHeaderBox() {
        return headerBox;
    }

    protected HBox getBreadcrumbsBox() {
        return breadcrumbsBox;
    }

    protected VBox getRightBox() {
        return rightBox;
    }

    protected void showPage(PageFxView<?> page) {
        contentBox.getChildren().clear();
        VBox.setVgrow(page.getNode(), Priority.ALWAYS);
        contentBox.getChildren().add(page.getNode());
        this.page = page;
    }

    protected Node createBreadcrumb(PageBreadcrumb breadcrumb) {
        var link = new Hyperlink(breadcrumb.getText());
        if (breadcrumb.getIcon() != null) {
            link.setGraphic(new IconViewBox(breadcrumb.getIcon()));
        }
        link.setOnAction(e -> {
            getPresenter().onPageRequested(breadcrumb.getName(), breadcrumb);
        });
        return link;
    }

    protected Node createBreadcrumbDivider() {
        return new IconViewBox(SharedIcons.CHEVRON_RIGHT);
    }
}
