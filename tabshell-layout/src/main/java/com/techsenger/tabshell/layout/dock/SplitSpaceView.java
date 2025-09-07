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
        this.splitPane.getDividers().addListener((ListChangeListener<SplitPane.Divider>) (change) -> {
            while (change.next()) {
                if (change.wasAdded()) {
                    for (var d : change.getAddedSubList()) {
                        d.positionProperty().addListener((ov, oldV, newV) -> {
                            try {
                                throw new Exception(String.valueOf(newV));
                            } catch (Exception ex) {
                                ex.printStackTrace();
                            }
                        });
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

    /**
     * Updates divider positions in a horizontal SplitPane after inserting a new node at the specified index.
     *

     *
     * @param splitPane The SplitPane instance (must be horizontal).
     * @param insertIndex The index at which the new node will be inserted.
     * @param nodeWidth The width of the new node, in pixels.
     * @param oldDividerPositions The divider positions before the new node is inserted.
     */
    public void updateDividerPositionsAfterInsert(
            SplitPane splitPane,
            int insertIndex,
            double nodeWidth,
            double[] oldDividerPositions
    ) {

        List<Node> items = splitPane.getItems();
        int count = items.size();

        if (count < 2) {
            return;
        }

        double totalWidth = splitPane.getWidth();
        if (totalWidth <= 0) {
            return; // Can't proceed if SplitPane width is not known
        }

        // Get actual widths of all children (including the new one)
        double[] widths = new double[count];
        for (int i = 0; i < count; i++) {
            widths[i] = items.get(i).getBoundsInParent().getWidth();
        }

        // Prepare adjustment
        if (insertIndex == 0 || insertIndex == count - 1) {
            // New node at the edge
            int neighborIdx = (insertIndex == 0) ? 1 : count - 2;
            double neighborWidth = widths[neighborIdx];

            double allocate = nodeWidth;
            if (neighborWidth < nodeWidth) {
                allocate = neighborWidth / 3.0;
            }

            widths[insertIndex] = allocate;
            widths[neighborIdx] = neighborWidth - allocate;
        } else {
            // New node between two nodes
            int leftIdx = insertIndex - 1;
            int rightIdx = insertIndex + 1;
            double leftWidth = widths[leftIdx];
            double rightWidth = widths[rightIdx];
            double totalNeighbors = leftWidth + rightWidth;

            double leftGive = nodeWidth * (leftWidth / totalNeighbors);
            double rightGive = nodeWidth * (rightWidth / totalNeighbors);

            boolean leftOk = leftGive <= leftWidth;
            boolean rightOk = rightGive <= rightWidth;

            if (leftOk && rightOk) {
                widths[leftIdx] = leftWidth - leftGive;
                widths[rightIdx] = rightWidth - rightGive;
                widths[insertIndex] = leftGive + rightGive;
            } else {
                leftGive = leftWidth / 3.0;
                rightGive = rightWidth / 3.0;
                widths[leftIdx] = leftWidth - leftGive;
                widths[rightIdx] = rightWidth - rightGive;
                widths[insertIndex] = leftGive + rightGive;
            }
        }

        // Пересчитываем divider positions
        double total = 0.0;
        for (int i = 0; i < count; i++) {
            total += widths[i];
        }
        double[] newDividers = new double[count - 1];
        double accum = 0.0;
        for (int i = 0; i < count - 1; i++) {
            accum += widths[i];
            newDividers[i] = accum / total;
        }
        splitPane.setDividerPositions(newDividers);
    }

}
