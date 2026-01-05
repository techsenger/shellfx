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

package com.techsenger.tabshell.layout.splittab;

import com.techsenger.tabshell.core.tab.AbstractShellTabView;
import com.techsenger.toolkit.fx.pulse.LayoutPhase;
import java.util.ArrayList;
import java.util.List;
import javafx.geometry.Orientation;
import javafx.scene.control.SplitPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

/**
 * View for tab component with top and bottom sections.
 *
 * @author Pavel Castornii
 */
public abstract class AbstractSplitTabView<T extends AbstractSplitTabViewModel<?>,
        S extends AbstractSplitTabComponent<?>> extends AbstractShellTabView<T, S> {

    private final SplitPane horizontalSplitPane = new SplitPane();

    private final SplitPane verticalSplitPane = new SplitPane();

    /**
     * See {@link javafx.scene.control.skin.SplitPaneSkin.ContentDivider}.
     */
    private StackPane leftDivider;

    private StackPane rightDivider;

    private StackPane bottomDivider;

    private final VBox leftPane = new VBox();

    private final VBox topPane = new VBox();

    private final VBox bottomPane = new VBox();

    private final VBox rightPane = new VBox();

    public AbstractSplitTabView(T viewModel) {
        super(viewModel);
    }

    protected VBox getLeftPane() {
        return leftPane;
    }

    protected VBox getTopPane() {
        return topPane;
    }

    protected VBox getBottomPane() {
        return bottomPane;
    }

    protected VBox getRightPane() {
        return rightPane;
    }

    @Override
    protected void build() {
        super.build();
        var viewModel = getViewModel();
        //to avoid SplitPane to resize one of the panes when the window resizes
        SplitPane.setResizableWithParent(this.leftPane, Boolean.FALSE);
        SplitPane.setResizableWithParent(this.topPane, Boolean.FALSE);
        SplitPane.setResizableWithParent(this.rightPane, Boolean.FALSE);
        SplitPane.setResizableWithParent(this.bottomPane, Boolean.FALSE);

        this.horizontalSplitPane.getItems().addAll(this.leftPane, this.verticalSplitPane, this.rightPane);
        this.horizontalSplitPane.setOrientation(Orientation.HORIZONTAL);
        this.horizontalSplitPane.getDividers().get(0).positionProperty()
                .bindBidirectional(viewModel.getLeftDivider().positionProperty());
        this.horizontalSplitPane.getDividers().get(1).positionProperty()
                .bindBidirectional(viewModel.getRightDivider().positionProperty());

        this.verticalSplitPane.getItems().addAll(this.topPane, this.bottomPane);
        this.verticalSplitPane.setOrientation(Orientation.VERTICAL);
        this.verticalSplitPane.getDividers().get(0).positionProperty()
                .bindBidirectional(viewModel.getBottomDivider().positionProperty());
        this.getContentPane().getChildren().add(horizontalSplitPane);
        VBox.setVgrow(horizontalSplitPane, Priority.ALWAYS);
    }

    @Override
    protected void addListeners() {
        super.addListeners();
        var viewModel = getViewModel();
        getPulseListenerManager().addListener(LayoutPhase.POST, () -> {
            List<StackPane> dividers = new ArrayList<>();
            //we need only the dividers of these two split panes, so, we check every divider parent
            this.horizontalSplitPane.lookupAll(".split-pane-divider").forEach(d -> {
                var divider = (StackPane) d;
                if (divider.getParent() == this.horizontalSplitPane) {
                    dividers.add(divider);
                } else if (divider.getParent() == this.verticalSplitPane) {
                    this.bottomDivider = divider;
                    this.bottomDivider.visibleProperty().bindBidirectional(viewModel.bottomPaneVisibleProperty());
                    viewModel.getBottomDivider().paddingProperty()
                            .bindBidirectional(this.bottomDivider.paddingProperty());
                }
            });
            this.leftDivider = dividers.get(0);
            this.leftDivider.visibleProperty().bindBidirectional(viewModel.leftPaneVisibleProperty());
            viewModel.getLeftDivider().paddingProperty().bindBidirectional(this.leftDivider.paddingProperty());

            this.rightDivider = dividers.get(1);
            this.rightDivider.visibleProperty().bindBidirectional(viewModel.rightPaneVisibleProperty());
            viewModel.getRightDivider().paddingProperty().bindBidirectional(this.rightDivider.paddingProperty());

            viewModel.initDividers();
            return false;
        });
    }
}
