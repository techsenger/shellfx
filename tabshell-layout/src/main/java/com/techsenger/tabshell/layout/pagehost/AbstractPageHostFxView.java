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
import com.techsenger.tabshell.core.page.PageFxView;
import com.techsenger.tabshell.core.page.PagePort;
import com.techsenger.tabshell.layout.style.LayoutIcons;
import com.techsenger.tabshell.material.icon.FontIconView;
import com.techsenger.tabshell.material.style.Spacing;
import com.techsenger.tabshell.material.style.StyleClasses;
import com.techsenger.tabshell.shared.find.ResultFindPort;
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
public abstract class AbstractPageHostFxView<P extends AbstractPageHostPresenter<?, ?>>
        extends AbstractAreaFxView<P> implements BasePageHostView {

    public class Composer extends AbstractAreaFxView<P>.Composer implements BasePageHostComposer {

        private final AbstractPageHostFxView<P> view = AbstractPageHostFxView.this;

        private FindPanelFxView<?> findPanel;

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
        public PagePort getSelectedPagePort() {
            return view.page == null ? null : view.page.getPresenter();
        }

        protected FindPanelFxView<?> getFindPanel() {
            return findPanel;
        }

        protected FindPanelFxView<?> createFindPanel() {
            var view = new FindPanelFxView<>();
            var presenter = new FindPanelPresenter<>(view, getPresenter());
            return view;
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

    private PageFxView<?> page;

    @Override
    public void setDividerPosition(double pos) {
        this.splitPane.getDividers().get(0).setPosition(pos);
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
    public void setForwardDisabled(boolean disabled) {
        this.forwardButton.setDisable(disabled);
    }

    @Override
    public void setBackDisabled(boolean disabled) {
        this.backButton.setDisable(disabled);
    }

    @Override
    protected void build() {
        super.build();
        this.leftBox.getStyleClass().add("left-box");
        SplitPane.setResizableWithParent(leftBox, false);

        this.headerBox.getStyleClass().add("header-box");
        this.headerBox.setPadding(new Insets(0, Spacing.HORIZONTAL, 0, Spacing.HORIZONTAL));

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
        splitPane.getDividers().getFirst().positionProperty()
            .addListener((ov, oldV, newV) -> getPresenter().onDividerPositionChanged(newV.doubleValue()));
    }

    @Override
    protected void addHandlers() {
        super.addHandlers();
        this.forwardButton.setOnAction(e -> getPresenter().onForward());
        this.backButton.setOnAction(e -> getPresenter().onBack());
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

    void setPage(PageFxView<?> page) {
        this.page = page;
    }
}
