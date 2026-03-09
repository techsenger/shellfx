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
import com.techsenger.tabshell.core.page.PageBreadcrumb;
import com.techsenger.tabshell.core.page.PageContainerFxView;
import com.techsenger.tabshell.core.page.PageFxView;
import com.techsenger.tabshell.core.page.PagePort;
import com.techsenger.tabshell.material.icon.IconViewBox;
import com.techsenger.tabshell.material.style.StyleClasses;
import com.techsenger.tabshell.shared.style.SharedIcons;
import com.techsenger.toolkit.fx.utils.NodeUtils;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javafx.geometry.Insets;
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

    private static TreeItem<PageItem> buildTree(PageItem root) {
        var treeItem = new TreeItem<PageItem>(root);
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
            view.menuBox.getChildren().add(0, findV.getNode());
        }

        @Override
        public void selectPage(ComponentName page) {
            var fxView = view.pages.get(page);
            if (fxView == null) {
                var treeItem = view.itemRegister.getItem(page);
                if (treeItem == null) {
                    return;
                }
                List<PageBreadcrumb> breadcrumbs = new ArrayList<>();
                TreeItem<PageItem> current = treeItem;
                while (current != null) {
                    if (current.getValue().getText() != null) { // it can be not shown root
                        breadcrumbs.add(current.getValue());
                    }
                    current = current.getParent();
                }
                Collections.reverse(breadcrumbs);
                fxView = treeItem.getValue().getFactory().create();
                fxView.getPresenter().initialize();
                fxView.setBreadcrumbDivider(SharedIcons.CHEVRON_RIGHT);
                fxView.getPresenter().setBreadcrumbs(breadcrumbs);
                VBox.setVgrow(fxView.getNode(), Priority.ALWAYS);
                getModifiableChildren().add(fxView);
                view.pages.put(page, fxView);
                view.pageTreeView.getSelectionModel().select(treeItem);
            }
            view.showPage(fxView);
        }

        @Override
        public PagePort getSelectedPage() {
            return view.page == null ? null : view.page.getPresenter();
        }

        protected FindPanelFxView<?> createFindPanel() {
            var view = new FindPanelFxView<>();
            var presenter = new FindPanelPresenter<>(view);
            return view;
        }
    }

    private final TreeView<PageItem> pageTreeView = new TreeView<>();

    private final VBox menuBox = new VBox(pageTreeView);

    private final VBox contentBox = new VBox();

    private final SplitPane splitPane = new SplitPane(menuBox, contentBox);

    /**
     * Created and initiliazed pages.
     */
    private final Map<ComponentName, PageFxView<?>> pages = new HashMap<>();

    private final TreeItemRegister itemRegister;

    private PageFxView<?> page;

    public PageHostFxView(PageItem root, boolean showRoot) {
        this(root, showRoot, null);
    }

    public PageHostFxView(PageItem root, boolean showRoot, Callback<TreeView<PageItem>, TreeCell<PageItem>> clbck) {
        var treeRoot = buildTree(root);
        this.itemRegister = new TreeItemRegister(treeRoot);
        pageTreeView.setRoot(treeRoot);
        pageTreeView.setShowRoot(showRoot);
        if (clbck != null) {
            pageTreeView.setCellFactory(clbck);
        } else {
            pageTreeView.setCellFactory(tv -> new TreeCell<>() {
                @Override
                protected void updateItem(PageItem item, boolean empty) {
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
    protected Composer createComposer() {
        return new PageHostFxView.Composer();
    }

    @Override
    protected void build() {
        super.build();
        this.menuBox.getStyleClass().add("menu-box");
        pageTreeView.getStyleClass().addAll(StyleClasses.EXTRA_DENSE, StyleClasses.NO_BORDER);
        pageTreeView.setShowRoot(false);
        // VBox.setVgrow(pageTreeView, Priority.ALWAYS);
        HBox.setHgrow(contentBox, Priority.ALWAYS);
        this.contentBox.getStyleClass().add("content-box");

        var css = PageHostFxView.class.getResource("page-host.css").toExternalForm();
        this.splitPane.getStylesheets().add(css);

    }

    @Override
    protected void addListeners() {
        super.addListeners();
        pageTreeView.getSelectionModel().selectedItemProperty().addListener((ov, oldV, newV) -> {
            getPresenter().onPageSelected(newV.getValue().getName());
        });
        splitPane.getDividers().getFirst().positionProperty()
                .addListener((ov, oldV, newV) -> getPresenter().onDividerPositionChanged(newV.doubleValue()));
    }

    protected TreeView<? extends PageItem> getPageTreeView() {
        return pageTreeView;
    }

    protected VBox getMenuBox() {
        return menuBox;
    }

    protected VBox getContentBox() {
        return contentBox;
    }

    protected void showPage(PageFxView<?> page) {
        if (this.page != null) {
            contentBox.getChildren().remove(0);
            this.page.setSelected(false);
        }
        VBox.setVgrow(page.getNode(), Priority.ALWAYS);
        contentBox.getChildren().add(0, page.getNode());
        this.page = page;
        this.page.setSelected(true);
    }
}
