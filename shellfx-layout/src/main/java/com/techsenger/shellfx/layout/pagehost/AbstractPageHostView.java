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

import atlantafx.base.theme.Styles;
import com.techsenger.shellfx.core.area.AbstractAreaView;
import com.techsenger.shellfx.core.page.ContainerPagePort;
import com.techsenger.shellfx.core.page.PageView;
import com.techsenger.shellfx.layout.style.LayoutIcons;
import com.techsenger.shellfx.material.icon.FontIconView;
import com.techsenger.shellfx.material.style.Spacing;
import com.techsenger.shellfx.material.style.StyleClasses;
import com.techsenger.toolkit.fx.Spacer;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.SplitPane;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

/**
 *
 * @author Pavel Castornii
 */
public abstract class AbstractPageHostView<VM extends AbstractPageHostViewModel<?>> extends AbstractAreaView<VM>  {

    public class Composer extends AbstractAreaView<VM>.Composer implements BasePageHostComposer {

        private final AbstractPageHostView<VM> view = AbstractPageHostView.this;

        private FindPanelView<?> findPanel;

        private PageView<?> page;

        @Override
        public void compose() {
            super.compose();
            this.findPanel = createFindPanel();
            getModifiableChildren().add(this.findPanel);
            addFindPanel(this.findPanel.getNode());
        }

        @Override
        public ContainerPagePort getSelectedPagePort() {
            return page == null ? null : page.getViewModel();
        }

        protected FindPanelView<?> getFindPanel() {
            return findPanel;
        }

        protected FindPanelView<?> createFindPanel() {
            var viewModel = new FindPanelViewModel<>(new FindPanelParams(getViewModel()));
            var view = new FindPanelView<>(viewModel);
            view.initialize();
            return view;
        }

        protected PageView<?> getPage() {
            return page;
        }

        void setPage(PageView<?> page) {
            this.page = page;
        }
    }

    private final VBox leftBox = new VBox();

    private final Button backButton = new Button(null, new FontIconView(LayoutIcons.CHEVRON_LEFT));

    private final Button forwardButton = new Button(null, new FontIconView(LayoutIcons.CHEVRON_RIGHT));

    private final HBox historyBox = new HBox(backButton, forwardButton);

    private final HBox headerBox = new HBox(new Spacer(Orientation.HORIZONTAL), historyBox);

    private final VBox contentBox = new VBox();

    private final VBox rightBox = new VBox(headerBox, contentBox);

    private final SplitPane splitPane = new SplitPane(leftBox, rightBox);

    public AbstractPageHostView(VM viewModel) {
        super(viewModel);
    }

    public void setContentPadding(Insets padding) {
        this.contentBox.setPadding(padding);
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
    protected void build() {
        super.build();
        this.leftBox.getStyleClass().add("left-box");
        SplitPane.setResizableWithParent(leftBox, false);

        this.headerBox.getStyleClass().add("header-box");
        this.headerBox.setPadding(new Insets(0, Spacing.getHorizontal(), 0, Spacing.getHorizontal()));

        backButton.getStyleClass().addAll(StyleClasses.SQUARE, Styles.FLAT, StyleClasses.SIZE_S);
        backButton.setTooltip(new Tooltip("Back"));
        forwardButton.getStyleClass().addAll(StyleClasses.SQUARE, Styles.FLAT, StyleClasses.SIZE_S);
        forwardButton.setTooltip(new Tooltip("Forward"));

        historyBox.setSpacing(Spacing.getHorizontal());
        historyBox.getStyleClass().add("history-box");
        historyBox.setAlignment(Pos.CENTER_RIGHT);

        VBox.setVgrow(contentBox, Priority.ALWAYS);
        this.contentBox.getStyleClass().add("content-box");
        this.rightBox.getStyleClass().add("right-box");

        var css = PageHostView.class.getResource("page-host.css").toExternalForm();
        this.splitPane.getStylesheets().add(css);
    }

    @Override
    protected void bind() {
        super.bind();
        var viewModel = getViewModel();
        forwardButton.disableProperty().bind(viewModel.forwardDisabledProperty());
        backButton.disableProperty().bind(viewModel.backDisabledProperty());
    }

    @Override
    protected void addListeners() {
        super.addListeners();
        var viewModel = getViewModel();
        viewModel.dividerPositionWrapper().bind(splitPane.getDividers().getFirst().positionProperty());
        viewModel.dividerPositionSource().addListener((value) -> splitPane.getDividers().getFirst().setPosition(value));
    }

    @Override
    protected void addHandlers() {
        super.addHandlers();
        var viewModel = getViewModel();
        this.forwardButton.setOnAction(e -> viewModel.onForward());
        this.backButton.setOnAction(e -> viewModel.onBack());
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

    protected VBox getRightBox() {
        return rightBox;
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
