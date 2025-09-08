/*
 * Copyright 2024-2025 Pavel Castornii.
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

package com.techsenger.tabshell.layout.dock;

import com.techsenger.mvvm4fx.core.ChildView;
import com.techsenger.mvvm4fx.core.ComponentHelper;
import com.techsenger.tabshell.core.pane.AbstractPaneView;
import java.util.List;
import javafx.collections.ListChangeListener;
import javafx.scene.Node;
import javafx.scene.control.SplitPane;

/**
 *
 * @author Pavel Castornii
 */
public class SplitSpaceView<T extends SplitSpaceViewModel> extends AbstractPaneView<T> {

    private final DockLayoutView<?> layout;

    private final SplitPane splitPane = new SplitPane();

    protected SplitSpaceView(DockLayoutView<?> layout, T viewModel) {
        super(viewModel);
        this.layout = layout;
    }

    @Override
    public void requestFocus() {

    }

    @Override
    public SplitPane getNode() {
        return splitPane;
    }

    @Override
    protected void build(T viewModel) {
        super.build(viewModel);
        this.splitPane.setOrientation(viewModel.getOrientation());
    }

    @Override
    protected void addListeners(T viewModel) {
        super.addListeners(viewModel);
        getChildren().addListener((ListChangeListener<ChildView<?>>) (change) -> {
            while (change.next()) {
                if (change.wasAdded()) {
                    int startIndex = change.getFrom();
                    int endIndex = change.getTo();
                    List<? extends ChildView<?>> addedElements = change.getAddedSubList();
                    for (int i = 0; i < addedElements.size(); i++) {
                        int actualIndex = startIndex + i;
                        AbstractPaneView<?> child = (AbstractPaneView<?>) addedElements.get(i);
                        addChild(child, actualIndex);
                    }
                }
                if (change.wasRemoved()) {
                    int removedFrom = change.getFrom();
                    List<? extends ChildView<?>> removedItems = change.getRemoved();
                    for (int i = 0; i < removedItems.size(); i++) {
                        int oldIndex = removedFrom + i;
                        removeChild(oldIndex);
                    }
                }
            }
        });
    }

    @Override
    protected ComponentHelper<?> createComponentHelper() {
        return new SplitSpaceHelper(this);
    }

    private void addChild(AbstractPaneView<?> child, int index) {
        Node container = this.layout.createContainerFor(child);
        splitPane.getItems().add(index, container);
    }

    private void removeChild(int childIndex) {
        splitPane.getItems().remove(childIndex); // removing container
    }
}
