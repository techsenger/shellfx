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
import com.techsenger.tabshell.core.area.AbstractAreaFxView;
import com.techsenger.tabshell.core.page.PageContainerFxView;
import com.techsenger.tabshell.core.page.PageDescriptor;
import com.techsenger.tabshell.core.page.PageFxView;
import com.techsenger.tabshell.core.page.PageItem;
import com.techsenger.tabshell.core.page.PagePort;
import com.techsenger.tabshell.layout.style.LayoutIcons;
import com.techsenger.tabshell.material.icon.FontIconView;
import com.techsenger.tabshell.material.icon.IconViewBox;
import com.techsenger.tabshell.material.style.Spacing;
import com.techsenger.tabshell.material.style.StyleClasses;
import com.techsenger.tabshell.shared.find.ResultFindPort;
import com.techsenger.toolkit.fx.Spacer;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.SplitPane;
import javafx.scene.control.Tooltip;
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

    private static TreeItem<PageDescriptor> buildTree(PageItem<?> root,
            Map<PageItem<?>, TreeItem<PageDescriptor>> treeItemsByItems) {
        var treeItem = new TreeItem<PageDescriptor>((PageDescriptor) root);
        treeItemsByItems.put(root, treeItem);
        if (root.getChildren() != null && !root.getChildren().isEmpty()) {
            root.getChildren().stream()
                .map(child -> buildTree(child, treeItemsByItems))
                .forEach(treeItem.getChildren()::add);
        }
        return treeItem;
    }

    private static TreeItem<PageDescriptor> buildTree(FilteredPageItem root,
            Map<PageItem<?>, TreeItem<PageDescriptor>> treeItemsByItems) {
        var treeItem = new TreeItem<PageDescriptor>((PageDescriptor) root.getOriginal());
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


    public class Composer extends AbstractAreaFxView<P>.Composer implements PageContainerFxView.Composer,
            PageHostComposer {

        private final PageHostFxView<P> view = PageHostFxView.this;

        private FindPanelFxView<?> findPanel;

        /**
         * Created and initiliazed pages.
         */
        private final Map<PageItem<?>, PageFxView<?>> pagesByItems = new HashMap<>();

        @Override
        public void compose() {
            super.compose();
            this.findPanel = createFindPanel();
            this.findPanel.getPresenter().initialize();
            getModifiableChildren().add(this.findPanel);
            addFindPanel(this.findPanel.getNode());
        }

        @Override
        public ResultFindPort getFindPanelPort() {
            return this.findPanel.getPresenter();
        }

        @Override
        public PagePort providePagePort(PageItem<?> item) {
            var fxView = pagesByItems.get(item);
            if (fxView == null) {
                var treeItem = view.treeItemsByItems.get(item);
                fxView = treeItem.getValue().getFactory().create(item);
                fxView.getPresenter().initialize();
                getModifiableChildren().add(fxView);
                pagesByItems.put((PageDescriptor) item, fxView);
            }
            return fxView.getPresenter();
        }

        @Override
        public PagePort getSelectedPagePort() {
            return view.page == null ? null : view.page.getPresenter();
        }

        @Override
        public void setPages(PageDescriptor root, boolean showRoot) {
            getModifiableChildren().removeAll(pagesByItems.values());
            pagesByItems.clear();
            getPresenter().setPages(root, showRoot);
        }

        protected FindPanelFxView<?> createFindPanel() {
            var view = new FindPanelFxView<>();
            var presenter = new FindPanelPresenter<>(view, getPresenter());
            return view;
        }
    }

    private final TreeView<PageDescriptor> pageTreeView = new TreeView<>();

    private final VBox leftBox = new VBox(pageTreeView);

    private final HBox breadcrumbsBox = new HBox();

    private final Button backButton = new Button(null, new FontIconView(LayoutIcons.CHEVRON_LEFT));

    private final Button forwardButton = new Button(null, new FontIconView(LayoutIcons.CHEVRON_RIGHT));

    private final HBox historyBox = new HBox(backButton, forwardButton);

    private final HBox headerBox = new HBox(breadcrumbsBox, new Spacer(Orientation.HORIZONTAL), historyBox);

    private final VBox contentBox = new VBox();

    private final VBox rightBox = new VBox(headerBox, contentBox);

    private final SplitPane splitPane = new SplitPane(leftBox, rightBox);


    private final Map<PageItem<?>, TreeItem<PageDescriptor>> treeItemsByItems = new HashMap<>();

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
        // empty
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

    @Override
    public void setMenu(PageItem<?> root, boolean showRoot) {
        treeItemsByItems.clear();
        pageTreeView.setRoot(null);
        pageTreeView.setShowRoot(showRoot);
        if (root != null) {
            var treeRoot = buildTree(root, treeItemsByItems);
            pageTreeView.setRoot(treeRoot);
        }
    }

    @Override
    public void setMenu(FilteredPageItem root, boolean showRoot) {
        treeItemsByItems.clear();
        pageTreeView.setRoot(null);
        pageTreeView.setShowRoot(showRoot);
        if (root != null) {
            var treeRoot = buildTree(root, treeItemsByItems);
            pageTreeView.setRoot(treeRoot);
        }
    }

    public void setContentPadding(Insets padding) {
        this.contentBox.setPadding(padding);
    }

    @Override
    public void setPage(PageItem<?> item) {
        var fxView = getComposer().pagesByItems.get(item);
        var treeItem = treeItemsByItems.get(item);
        this.page = fxView; // before selecting treeItem
        pageTreeView.getSelectionModel().select(treeItem); // it can be selected using breadcrumbs
        contentBox.getChildren().clear();
        VBox.setVgrow(fxView.getNode(), Priority.ALWAYS);
        contentBox.getChildren().add(fxView.getNode());
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
    public void setForwardDisabled(boolean disabled) {
        this.forwardButton.setDisable(disabled);
    }

    @Override
    public void setBackDisabled(boolean disabled) {
        this.backButton.setDisable(disabled);
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
        pageTreeView.getStyleClass().addAll(StyleClasses.COMPACT, StyleClasses.NO_BORDER);
        pageTreeView.setShowRoot(false);
        VBox.setVgrow(pageTreeView, Priority.ALWAYS);

        this.headerBox.getStyleClass().add("header-box");
        this.headerBox.setPadding(new Insets(0, Spacing.HORIZONTAL, 0, Spacing.HORIZONTAL));
        breadcrumbsBox.getStyleClass().add("breadcrumbs-box");
        breadcrumbsBox.setAlignment(Pos.CENTER_LEFT);
        breadcrumbsBox.setSpacing(Spacing.HORIZONTAL_THIRD);
        HBox.setHgrow(breadcrumbsBox, Priority.ALWAYS);

        backButton.getStyleClass().addAll(StyleClasses.ICON_BUTTON, Styles.FLAT, StyleClasses.DENSE);
        backButton.setTooltip(new Tooltip("Back"));
        forwardButton.getStyleClass().addAll(StyleClasses.ICON_BUTTON, Styles.FLAT, StyleClasses.DENSE);
        forwardButton.setTooltip(new Tooltip("Forward"));

        historyBox.setSpacing(Spacing.HORIZONTAL);
        historyBox.getStyleClass().add("history-box");
        historyBox.setAlignment(Pos.CENTER_RIGHT);

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
            if (newV != null) {
                getPresenter().onPageRequested(newV.getValue());
            }
        });
        splitPane.getDividers().getFirst().positionProperty()
                .addListener((ov, oldV, newV) -> getPresenter().onDividerPositionChanged(newV.doubleValue()));
    }

    @Override
    protected void addHandlers() {
        super.addHandlers();
        this.forwardButton.setOnAction(e -> getPresenter().onForward());
        this.backButton.setOnAction(e -> getPresenter().onBack());
    }

    protected TreeView<PageDescriptor> getPageTreeView() {
        return pageTreeView;
    }

    protected VBox getLeftBox() {
        return leftBox;
    }

    protected VBox getContentBox() {
        return contentBox;
    }

    protected Button getBackButton() {
        return backButton;
    }

    protected Button getForwardButton() {
        return forwardButton;
    }

    protected HBox getHistoryBox() {
        return historyBox;
    }

    protected HBox getHeaderBox() {
        return headerBox;
    }

    protected PageFxView<?> getPage() {
        return page;
    }

    protected HBox getBreadcrumbsBox() {
        return breadcrumbsBox;
    }

    protected VBox getRightBox() {
        return rightBox;
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

    protected void addFindPanel(HBox node) {
        node.heightProperty().addListener((ov, oldV, newV) -> {
            // to have one base line for text in the left and right panels
            this.headerBox.setPrefHeight(newV.doubleValue());
            this.headerBox.setMinHeight(newV.doubleValue());
            this.headerBox.setMaxHeight(newV.doubleValue());
        });
        this.leftBox.getChildren().add(0, node);
    }
}
