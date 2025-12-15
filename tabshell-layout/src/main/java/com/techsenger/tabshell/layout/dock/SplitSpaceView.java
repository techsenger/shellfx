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

import com.techsenger.tabshell.core.area.AbstractAreaView;
import static com.techsenger.tabshell.layout.dock.DockConstants.ONE_THIRD;
import com.techsenger.tabshell.material.pane.SplitPaneDividerBinder;
import com.techsenger.tabshell.core.area.AreaView;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javafx.geometry.Orientation;
import javafx.geometry.Side;
import static javafx.geometry.Side.LEFT;
import javafx.scene.Node;
import javafx.scene.control.SplitPane;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Pavel Castornii
 */
public class SplitSpaceView<T extends SplitSpaceViewModel, S extends SplitSpaceComponent<?>>
        extends AbstractAreaView<T, S> {

    private static final Logger logger = LoggerFactory.getLogger(SplitSpaceView.class);

    private final SplitPane splitPane = new SplitPane();

    protected SplitSpaceView(T viewModel) {
        super(viewModel);
    }

    @Override
    public void requestFocus() {

    }

    @Override
    public SplitPane getNode() {
        return splitPane;
    }

    @Override
    protected void build() {
        super.build();
        this.splitPane.setOrientation(getViewModel().getOrientation());
    }

    @Override
    protected void bind() {
        super.bind();
        new SplitPaneDividerBinder(splitPane, getViewModel().getDividerPositions());
    }

    void logState(String note) {
        logger.debug("{} {} child sizes: {}, dividers: {}", getComponent().getLogPrefix(), note,
                getChildSizes(), this.splitPane.getDividerPositions());
    }

    /**
     * Updates divider positions after a container is split in half. Adjusts dividers to create equal space for the
     * new container.
     *
     * @param splitContainerIndex the index of the container that was split
     * @param oldPositions divider positions before the split
     */
    void updateDividersOnHalfSplit(int splitContainerIndex, double[] oldPositions) {
        int newDividerCount = oldPositions.length + 1;
        double[] newPositions = new double[newDividerCount];

        if (oldPositions.length == 0) {
            newPositions[0] = 0.5;
        } else {
            double leftBound = (splitContainerIndex == 0) ? 0.0 : oldPositions[splitContainerIndex - 1];
            double rightBound =
                    (splitContainerIndex == oldPositions.length) ? 1.0 : oldPositions[splitContainerIndex];
            double middle = (leftBound + rightBound) / 2;
            for (int i = 0; i < newDividerCount; i++) {
                if (i < splitContainerIndex) {
                    newPositions[i] = oldPositions[i];
                } else if (i == splitContainerIndex) {
                    newPositions[i] = middle;
                } else {
                    newPositions[i] = oldPositions[i - 1];
                }
            }
        }
        getNode().setDividerPositions(newPositions);
        logger.debug("{} Updated dividers on half split; oldPositions: {}, newPositions: {}",
                getComponent().getLogPrefix(), oldPositions, newPositions);
    }

    /**
     * Updates divider positions after a container is split into thirds. Adjusts dividers to allocate one third for
     * the new container and two thirds for the existing container.
     *
     * @param splitContainerIndex the index of the container that was split
     * @param oldPositions divider positions before the split
     * @param side the side the new element occupies (LEFT, RIGHT, TOP, BOTTOM)
     */
    void updateDividersOnThirdSplit(int splitContainerIndex, double[] oldPositions, Side side) {
        double firstFraction = 1 - ONE_THIRD;
        if (side == Side.TOP || side == LEFT) {
            firstFraction = ONE_THIRD;
        }
        int newDividerCount = oldPositions.length + 1;
        double[] newPositions = new double[newDividerCount];

        if (oldPositions.length == 0) {
            newPositions[0] = firstFraction;
        } else {
            double leftBound = (splitContainerIndex == 0) ? 0.0 : oldPositions[splitContainerIndex - 1];
            double rightBound =
                    (splitContainerIndex == oldPositions.length) ? 1.0 : oldPositions[splitContainerIndex];
            double firstPart = leftBound + (rightBound - leftBound) * firstFraction;
            for (int i = 0; i < newDividerCount; i++) {
                if (i < splitContainerIndex) {
                    newPositions[i] = oldPositions[i];
                } else if (i == splitContainerIndex) {
                    newPositions[i] = firstPart;
                } else {
                    newPositions[i] = oldPositions[i - 1];
                }
            }
        }
        getNode().setDividerPositions(newPositions);
        logger.debug("{} Updated dividers on third split; oldPositions: {}, newPositions: {}",
                getComponent().getLogPrefix(), oldPositions, newPositions);
    }

    /**
     * Updates divider positions when inserting between existing containers. Takes space from both neighboring
     * containers according to specified proportions.
     *
     * <p>When a new dock is inserted between two existing docks, the available space is redistributed proportionally
     * to their current sizes. For example, if the adjacent docks have sizes of 100 and 200, the total combined space
     * is 300. The new dock receives one-third of this space (100), with the first dock giving up 33 and the second
     * dock giving up 66, preserving their original proportions.
     *
     * @param newContainerIndex the index where new container was inserted
     * @param beforeProportion proportion taken from the container before insertion point
     * @param afterProportion proportion taken from the container after insertion point
     * @param oldPositions divider positions before the insertion
     */
    void updateDividersOnInsertBetween(int newContainerIndex,
            double beforeProportion, double afterProportion, double[] oldPositions) {
        int oldCount = oldPositions.length + 1;
        double[] oldSizes = new double[oldCount];
        oldSizes[0] = (oldPositions.length > 0) ? oldPositions[0] : 1.0;
        for (int i = 1; i < oldPositions.length; i++) {
            oldSizes[i] = oldPositions[i] - oldPositions[i - 1];
        }
        if (oldPositions.length > 0) {
            oldSizes[oldSizes.length - 1] = 1.0 - oldPositions[oldPositions.length - 1];
        }

        int left = newContainerIndex - 1;
        int right = newContainerIndex;
        double leftWidth = oldSizes[left];
        double rightWidth = oldSizes[right];

        double newWidth = (leftWidth + rightWidth) * ONE_THIRD;
        double takenFromLeft = newWidth * beforeProportion;
        double takenFromRight = newWidth * afterProportion;

        double[] newSizes = new double[oldSizes.length + 1];
        int j = 0;
        for (int i = 0; i < newSizes.length; i++) {
            if (i == newContainerIndex) {
                newSizes[i] = newWidth;
            } else if (i == left) {
                newSizes[i] = oldSizes[j++] - takenFromLeft;
            } else if (i == right + 1) {
                newSizes[i] = oldSizes[j++] - takenFromRight;
            } else {
                newSizes[i] = oldSizes[j++];
            }
        }

        // adjust newSizes so that the sum is 1.0
        double total = 0;
        for (double s : newSizes) {
            total += s;
        }
        for (int i = 0; i < newSizes.length; i++) {
            newSizes[i] /= total;
        }

        // convert to divider positions
        double[] newPositions = new double[newSizes.length - 1];
        double sum = 0;
        for (int i = 0; i < newPositions.length; i++) {
            sum += newSizes[i];
            newPositions[i] = sum;
        }
        getNode().setDividerPositions(newPositions);
        logger.debug("{} Updated dividers on insert between; oldPositions: {}, newPositions: {}",
                getComponent().getLogPrefix(), oldPositions, newPositions);
    }

    /**
     * Updates divider positions for a parent SplitPane after unwrapping a child SplitPane.
     *
     * @param unwrapIndex the index in the parent SplitPane where the unwrapped SplitPane was located
     * @param oldPositions the divider positions (from 0 to 1) of the parent SplitPane BEFORE unwrapping.
     * @param childPositions the divider positions (from 0 to 1) of the removed child SplitPane.
     */
    void updateDividersOnUnwrap(int unwrapIndex, double[] oldPositions, double[] childPositions) {
        double[] newPositions;

        if (childPositions == null || childPositions.length == 0) {
            // Case 1: Child SplitPane had only one element
            // We're replacing one node with another node at the same position
            // Number of dividers remains the same, positions remain the same
            newPositions = Arrays.copyOf(oldPositions, oldPositions.length);
        } else {
            // Case 2: Child SplitPane had multiple elements with dividers
            // We're replacing one node with multiple nodes
            newPositions = new double[oldPositions.length + childPositions.length];

            int pos = 0;
            // Copy old dividers before unwrapIndex
            for (int i = 0; i < unwrapIndex; i++) {
                newPositions[pos++] = oldPositions[i];
            }

            // Calculate bounds for childPositions mapping
            double left = unwrapIndex == 0 ? 0.0 : oldPositions[unwrapIndex - 1];
            double right = unwrapIndex == oldPositions.length ? 1.0 : oldPositions[unwrapIndex];

            // Insert mapped child dividers
            for (double p : childPositions) {
                newPositions[pos++] = left + (right - left) * p;
            }

            // Copy old dividers after unwrapIndex
            for (int i = unwrapIndex; i < oldPositions.length; i++) {
                newPositions[pos++] = oldPositions[i];
            }
        }

        getNode().setDividerPositions(newPositions);
        logger.debug("{} Updated dividers on unwrap; oldPositions: {}, childPositions: {}, newPositions: {}",
                getComponent().getLogPrefix(), oldPositions, childPositions, newPositions);
    }

    /**
     * Updates divider positions after SplitPane resize and node insertion.
     *
     * <p>This function calculates new divider positions when the SplitPane size changes (width for horizontal,
     * height for vertical) and a new node is inserted.
     *
     * <p>The algorithm ensures that all nodes maintain their original size except for one flexible node, which
     * sacrifices part of its size to accommodate the new node and absorbs the remaining size change.
     *
     * @param oldSize the original size of the SplitPane before changes
     * @param oldPositions array of divider positions (0.0 to 1.0) before changes
     * @param flexibleChildIndex index of the flexible node that will absorb size changes
     * @param newChildIndex index where the new node will be inserted
     * @param newChildSize the desired size for the new node
     */
    void updateDividersOnAddWithMain(double oldSize, double[] oldPositions, double dividerSize, int flexibleChildIndex,
            int newChildIndex, double newChildSize) {
        oldSize = computeOldSize(oldSize, oldPositions, dividerSize);
        var newSize = computeNewSize(dividerSize);

        // If oldSize is 0 (initial state), use simple distribution
        if (oldSize <= 0) {
            // Initial setup with two nodes
            double[] newPositions = new double[1];

            if (newChildIndex == 0) {
                // New node at beginning, flexible node takes remaining space
                newPositions[0] = newChildSize / newSize;
            } else {
                // New node at end, flexible node takes remaining space
                newPositions[0] = (newSize - newChildSize) / newSize;
            }

            // Ensure valid range
            newPositions[0] = Math.max(0.0, Math.min(1.0, newPositions[0]));

            logger.debug("{} Initial dividers setup with main; newSize: {}, flexibleChildIndex: {}, "
                    + "newChildIndex: {}, newChildSize: {}, newPositions: {}", getComponent().getLogPrefix(),
                    newSize, flexibleChildIndex, newChildIndex, newChildSize, newPositions);
            splitPane.setDividerPositions(newPositions);
            return;
        }

        // Calculate original node sizes in absolute units
        int oldNodeCount = oldPositions.length + 1;
        double[] oldSizes = new double[oldNodeCount];

        if (oldPositions.length == 0) {
            // Only one node exists - it takes the entire space
            oldSizes[0] = oldSize;
        } else {
            // First node
            oldSizes[0] = oldPositions[0] * oldSize;

            // Middle nodes
            for (int i = 1; i < oldNodeCount - 1; i++) {
                oldSizes[i] = (oldPositions[i] - oldPositions[i - 1]) * oldSize;
            }

            // Last node
            oldSizes[oldNodeCount - 1] = (1.0 - oldPositions[oldPositions.length - 1]) * oldSize;
        }

        // Create new sizes array with space for the new node
        int newNodeCount = oldNodeCount + 1;
        double[] newSizes = new double[newNodeCount];

        // Copy old sizes to new array, inserting the new node at the specified position
        for (int i = 0, j = 0; i < newNodeCount; i++) {
            if (i == newChildIndex) {
                newSizes[i] = newChildSize;
            } else {
                newSizes[i] = oldSizes[j++];
            }
        }

        // Apply SplitPane size change directly to flexible node
        double sizeChange = newSize - oldSize;
        newSizes[flexibleChildIndex] += sizeChange;

        // Now flexible node sacrifices space for the new node
        double sacrificeNeeded = newChildSize;
        double availableForSacrifice = newSizes[flexibleChildIndex] - TabDockView.MIN_SIZE;

        if (availableForSacrifice >= sacrificeNeeded) {
            // Flexible node can provide all space needed for new node
            newSizes[flexibleChildIndex] -= sacrificeNeeded;
        } else {
            // Flexible node gives all it can, reduce new node size
            newSizes[flexibleChildIndex] = TabDockView.MIN_SIZE;
            newSizes[newChildIndex] = availableForSacrifice;
        }

        // Calculate new divider positions
        double[] newPositions = new double[newNodeCount - 1];
        double cumulativeSize = 0.0;

        for (int i = 0; i < newNodeCount - 1; i++) {
            cumulativeSize += newSizes[i];
            newPositions[i] = cumulativeSize / newSize;
        }

        logger.debug("{} Updated dividers on add with main; oldSize: {}, newSize: {}, flexibleChildIndex: {}, "
                + "newChildIndex: {}, newChildSize: {}, oldPositions: {}, newPositions: {}",
                getComponent().getLogPrefix(), oldSize, newSize, flexibleChildIndex, newChildIndex, newChildSize,
                oldPositions, newPositions);

        splitPane.setDividerPositions(newPositions);
    }

    /**
     * Updates divider positions after SplitPane resize and node removal.
     *
     * <p>This function calculates new divider positions when the SplitPane size changes (width for horizontal,
     * height for vertical) and a node is removed.
     *
     * <p>The algorithm ensures that the flexible node absorbs both the size change from the SplitPane resize
     * and the space freed up by the removed node.
     *
     * @param oldSize the original size of the SplitPane before changes
     * @param oldPositions array of divider positions (0.0 to 1.0) before changes
     * @param flexibleChildIndex index of the flexible node that will absorb size changes
     * @param removedChildIndex index of the node that will be removed
     */
    void updateDividersOnRemoveWithMain(double oldSize, double[] oldPositions, double dividerSize,
            int flexibleChildIndex, int removedChildIndex) {
        // Calculate original node sizes in absolute units
        int oldNodeCount = oldPositions.length + 1;
        double[] oldSizes = new double[oldNodeCount];

        oldSize = computeOldSize(oldSize, oldPositions, dividerSize);
        var newSize = computeNewSize(dividerSize);

        // First node
        oldSizes[0] = oldPositions[0] * oldSize;

        // Middle nodes
        for (int i = 1; i < oldNodeCount - 1; i++) {
            oldSizes[i] = (oldPositions[i] - oldPositions[i - 1]) * oldSize;
        }

        // Last node
        oldSizes[oldNodeCount - 1] = (1.0 - oldPositions[oldPositions.length - 1]) * oldSize;

        // Create new sizes array without the removed node
        int newNodeCount = oldNodeCount - 1;
        double[] newSizes = new double[newNodeCount];

        // Copy old sizes to new array, skipping the removed node
        for (int i = 0, j = 0; i < oldNodeCount; i++) {
            if (i != removedChildIndex) {
                newSizes[j++] = oldSizes[i];
            }
        }

        // Apply SplitPane size change and removed node space to flexible node
        double sizeChange = newSize - oldSize;
        double removedNodeSize = oldSizes[removedChildIndex];

        // Flexible node absorbs both the size change and the removed node's space
        newSizes[flexibleChildIndex] += sizeChange + removedNodeSize;

        // Calculate new divider positions
        double[] newPositions = new double[newNodeCount - 1];
        double cumulativeSize = 0.0;

        for (int i = 0; i < newNodeCount - 1; i++) {
            cumulativeSize += newSizes[i];
            newPositions[i] = cumulativeSize / newSize;
        }

        logger.debug("{} Updated dividers on remove with main; oldSize: {}, newSize: {}, flexibleChildIndex: {}, "
                + "removedChildIndex: {}, oldPositions: {}, newPositions: {}", getComponent().getLogPrefix(),
                oldSize, newSize, flexibleChildIndex, removedChildIndex, oldPositions, newPositions);
        splitPane.setDividerPositions(newPositions);
    }

    /**
     * Updates divider positions after SplitPane resize and node insertion.
     *
     * <p>This function calculates new divider positions when the SplitPane size changes (width for horizontal,
     * height for vertical) and a new node is inserted.
     *
     * <p>The redistribution logic depends on the insertion position of the new node:
     * <ul>
     *   <li>If the new node is inserted at the first or last position, it takes the requested size from
     *       the immediate neighbor. The neighbor also absorbs the SplitPane size change.</li>
     *   <li>If the new node is inserted in the middle, it takes the requested size proportionally from
     *       its left and right neighbors, who also absorb the SplitPane size change proportionally.</li>
     * </ul>
     *
     * <p>Nodes cannot be reduced below MINIMAL_NODE_SIZE. If the requested size cannot be fully allocated,
     * neighbors contribute what they can until reaching MINIMAL_NODE_SIZE, and the new node receives
     * the maximum possible size.
     *
     * @param oldSize the original size of the SplitPane before changes
     * @param oldPositions array of divider positions (0.0 to 1.0) before changes
     * @param newChildIndex index where the new node will be inserted
     * @param newChildSize the desired size for the new node
     */
    void updateDividersOnAddWithoutMain(double oldSize, double[] oldPositions, double dividerSize, int newChildIndex,
            double newChildSize) {
        oldSize = computeOldSize(oldSize, oldPositions, dividerSize);
        var newSize = computeNewSize(dividerSize);

        // If oldSize is 0 (initial state), use equal distribution
        if (oldSize <= 0) {
            // Initial setup with two nodes
            double[] newPositions = new double[1];
            if (newChildIndex == 0) {
                // New node at beginning, existing node takes remaining space
                newPositions[0] = newChildSize / newSize;
            } else {
                // New node at end, existing node takes remaining space
                newPositions[0] = (newSize - newChildSize) / newSize;
            }

            // Ensure valid range
            newPositions[0] = Math.max(0.0, Math.min(1.0, newPositions[0]));

            logger.debug("{} Initial dividers setup; newSize: {}, newChildIndex: {}, newChildSize: {}, "
                    + "newPositions: {}", getComponent().getLogPrefix(), newSize, newChildIndex, newChildSize,
                    newPositions);

            splitPane.setDividerPositions(newPositions);
            return;
        }

        // Calculate original node sizes in absolute units
        int oldNodeCount = oldPositions.length + 1;
        double[] oldSizes = new double[oldNodeCount];

        if (oldPositions.length == 0) {
            // Only one node exists - it takes the entire space
            oldSizes[0] = oldSize;
        } else {
            // First node
            oldSizes[0] = oldPositions[0] * oldSize;

            // Middle nodes
            for (int i = 1; i < oldNodeCount - 1; i++) {
                oldSizes[i] = (oldPositions[i] - oldPositions[i - 1]) * oldSize;
            }

            // Last node
            oldSizes[oldNodeCount - 1] = (1.0 - oldPositions[oldPositions.length - 1]) * oldSize;
        }

        double sizeChange = newSize - oldSize;

        // Create new sizes array with space for the new node
        int newNodeCount = oldNodeCount + 1;
        double[] newSizes = new double[newNodeCount];

        // Copy old sizes to new array, inserting 0 for the new node
        for (int i = 0, j = 0; i < newNodeCount; i++) {
            if (i == newChildIndex) {
                newSizes[i] = 0;
            } else {
                newSizes[i] = oldSizes[j++];
            }
        }

        // Calculate space allocation
        if (newChildIndex == 0) {
            // Insert at beginning
            double neighborSize = newSizes[1];
            double availableFromNeighbor = Math.max(neighborSize - TabDockView.MIN_SIZE, 0);
            double actualSpace = Math.min(newChildSize, availableFromNeighbor);

            newSizes[1] = neighborSize - actualSpace + sizeChange;
            newSizes[0] = actualSpace;

        } else if (newChildIndex == newNodeCount - 1) {
            // Insert at end
            double neighborSize = newSizes[newNodeCount - 2];
            double availableFromNeighbor = Math.max(neighborSize - TabDockView.MIN_SIZE, 0);
            double actualSpace = Math.min(newChildSize, availableFromNeighbor);

            newSizes[newNodeCount - 2] = neighborSize - actualSpace + sizeChange;
            newSizes[newNodeCount - 1] = actualSpace;

        } else {
            // Insert in middle
            int leftNeighborIndex = newChildIndex - 1;
            int rightNeighborIndex = newChildIndex + 1;

            double leftNeighborSize = newSizes[leftNeighborIndex];
            double rightNeighborSize = newSizes[rightNeighborIndex];

            double availableFromLeft = Math.max(leftNeighborSize - TabDockView.MIN_SIZE, 0);
            double availableFromRight = Math.max(rightNeighborSize - TabDockView.MIN_SIZE, 0);
            double totalAvailable = availableFromLeft + availableFromRight;

            double actualSpace = Math.min(newChildSize, totalAvailable);

            if (actualSpace > 0) {
                // Distribute proportionally based on available space
                double leftRatio = availableFromLeft / totalAvailable;
                double rightRatio = availableFromRight / totalAvailable;

                double leftContribution = actualSpace * leftRatio;
                double rightContribution = actualSpace * rightRatio;

                newSizes[leftNeighborIndex] = leftNeighborSize - leftContribution;
                newSizes[rightNeighborIndex] = rightNeighborSize - rightContribution;
                newSizes[newChildIndex] = actualSpace;
            }

            // Distribute size change proportionally
            double totalNeighborSize = newSizes[leftNeighborIndex] + newSizes[rightNeighborIndex];
            if (totalNeighborSize > 0) {
                double leftRatio = newSizes[leftNeighborIndex] / totalNeighborSize;
                double rightRatio = newSizes[rightNeighborIndex] / totalNeighborSize;

                newSizes[leftNeighborIndex] += sizeChange * leftRatio;
                newSizes[rightNeighborIndex] += sizeChange * rightRatio;
            } else {
                newSizes[leftNeighborIndex] += sizeChange / 2;
                newSizes[rightNeighborIndex] += sizeChange / 2;
            }
        }

        // Calculate new divider positions
        double[] newPositions = new double[newNodeCount - 1];
        double cumulativeSize = 0.0;

        for (int i = 0; i < newNodeCount - 1; i++) {
            cumulativeSize += newSizes[i];
            newPositions[i] = cumulativeSize / newSize;
        }

        logger.debug("{} Updated dividers on resize and add without main; oldSize: {}, newSize: {}, "
                + "newChildIndex: {}, newChildSize: {}, oldPositions: {}, newPositions: {}",
                getComponent().getLogPrefix(), oldSize, newSize, newChildIndex, newChildSize, oldPositions,
                newPositions);

        splitPane.setDividerPositions(newPositions);
    }

    /**
     * Updates divider positions after SplitPane resize and node removal.
     *
     * <p>This function calculates new divider positions when the SplitPane size changes (width for horizontal,
     * height for vertical) and a node is removed.
     *
     * <p>The redistribution logic depends on the position of the removed node:
     * <ul>
     *   <li>If the removed node was the first or last node, its immediate neighbor absorbs both the
     *       SplitPane size change and the removed node's space. Other nodes maintain their sizes.</li>
     *   <li>If the removed node was in the middle, its left and right neighbors absorb the SplitPane
     *       size change and the removed node's space proportionally to their original sizes.
     *       Other nodes maintain their sizes.</li>
     * </ul>
     *
     * @param oldSize the original size of the SplitPane before changes
     * @param oldPositions array of divider positions (0.0 to 1.0) before changes
     * @param removedChildIndex index of the node that will be removed
     */
    void updateDividersOnRemoveWithoutMain(double oldSize, double[] oldPositions, double dividerSize,
            int removedChildIndex) {
        // Calculate original node sizes in absolute units
        int oldNodeCount = oldPositions.length + 1;
        double[] oldSizes = new double[oldNodeCount];

        oldSize = computeOldSize(oldSize, oldPositions, dividerSize);
        var newSize = computeNewSize(dividerSize);

        // First node
        oldSizes[0] = oldPositions[0] * oldSize;

        // Middle nodes
        for (int i = 1; i < oldNodeCount - 1; i++) {
            oldSizes[i] = (oldPositions[i] - oldPositions[i - 1]) * oldSize;
        }

        // Last node
        oldSizes[oldNodeCount - 1] = (1.0 - oldPositions[oldPositions.length - 1]) * oldSize;

        double sizeChange = newSize - oldSize;
        double removedNodeSize = oldSizes[removedChildIndex];

        // Create new sizes array without the removed node
        int newNodeCount = oldNodeCount - 1;
        double[] newSizes = new double[newNodeCount];

        // Copy old sizes to new array, skipping the removed node
        for (int i = 0, j = 0; i < oldNodeCount; i++) {
            if (i != removedChildIndex) {
                newSizes[j++] = oldSizes[i];
            }
        }

        // Apply redistribution based on removed node position
        if (removedChildIndex == 0) {
            // Removed first node - first neighbor (now at index 0) absorbs everything
            newSizes[0] += sizeChange + removedNodeSize;
        } else if (removedChildIndex == oldNodeCount - 1) {
            // Removed last node - last neighbor (now at index newNodeCount-1) absorbs everything
            newSizes[newNodeCount - 1] += sizeChange + removedNodeSize;
        } else {
            // Removed middle node - distribute between left and right neighbors proportionally
            int leftNeighborIndex = removedChildIndex - 1;
            int rightNeighborIndex = removedChildIndex; // After removal, right neighbor shifts left

            double leftNeighborOldSize = oldSizes[leftNeighborIndex];
            double rightNeighborOldSize = oldSizes[removedChildIndex + 1]; // Original right neighbor

            double totalNeighborsSize = leftNeighborOldSize + rightNeighborOldSize;

            // Calculate distribution ratios
            double leftRatio = leftNeighborOldSize / totalNeighborsSize;
            double rightRatio = rightNeighborOldSize / totalNeighborsSize;

            // Distribute both the size change and removed node space proportionally
            double totalToDistribute = sizeChange + removedNodeSize;
            double leftShare = totalToDistribute * leftRatio;
            double rightShare = totalToDistribute * rightRatio;

            newSizes[leftNeighborIndex] += leftShare;
            newSizes[rightNeighborIndex] += rightShare;
        }

        // Calculate new divider positions
        double[] newPositions = new double[newNodeCount - 1];
        double cumulativeSize = 0.0;

        for (int i = 0; i < newNodeCount - 1; i++) {
            cumulativeSize += newSizes[i];
            newPositions[i] = cumulativeSize / newSize;
        }

        logger.debug("{} Updated dividers on remove without main; oldSize: {}, newSize: {}, "
                + "removedChildIndex: {}, oldPositions: {}, newPositions: {}", getComponent().getLogPrefix(),
                oldSize, newSize, removedChildIndex, oldPositions, newPositions);
        splitPane.setDividerPositions(newPositions);
    }

    /**
     * Computes SplitPane divider size - width for vertical dividers, height for horizontal dividers.
     *
     * @return
     */
    double computeDividerSize() {
        var dividers = splitPane.getDividerPositions();
        if (dividers.length == 0) {
            return -1;
        }

        double paneSize = splitPane.getOrientation() == Orientation.HORIZONTAL
                ? splitPane.getWidth()
                : splitPane.getHeight();

        if (paneSize <= 0) {
            return -1;
        }
        double totalItemsSize = 0;
        for (var item : splitPane.getItems()) {
            double itemSize = splitPane.getOrientation() == Orientation.HORIZONTAL
                    ? item.getBoundsInParent().getWidth()
                    : item.getBoundsInParent().getHeight();
            totalItemsSize += itemSize;
        }
        double totalDividersSize = paneSize - totalItemsSize;
        double dividerSize = totalDividersSize / (splitPane.getItems().size() - 1);
        logger.debug("{} Computed dividerSize: {}", getComponent().getLogPrefix(), dividerSize);
        return dividerSize;
    }

    void addChild(AreaView<?, ?> child) {
        Node container = getComponent().getLayout().getView().createContainer(child);
        splitPane.getItems().add(container);
    }

    void addChild(int index, AreaView<?, ?> child) {
        Node container = getComponent().getLayout().getView().createContainer(child);
        splitPane.getItems().add(index, container);
    }

    void removeChild(int childIndex) {
        StackPane container = (StackPane) splitPane.getItems().remove(childIndex);
        getComponent().getLayout().getView().destroyContainer(container);
    }

    /**
     * Returns the widths/heights of the children for horizontal/vertical split space.
     *
     * @return
     */
    private List<Double> getChildSizes() {
        var sizes = new ArrayList<Double>();
        for (var item : splitPane.getItems()) {
            if (splitPane.getOrientation() == Orientation.HORIZONTAL) {
                sizes.add(((Region) item).getWidth());
            } else {
                sizes.add(((Region) item).getHeight());
            }
        }
        return sizes;
    }

    private double computeOldSize(double oldSize, double[] oldPositions, double dividerSize) {
        var dividersCount = 0;
        if (oldPositions.length > 0) {
            dividersCount = splitPane.getItems().size() - 1;
        }
        var oldSizeWithoutDividers = oldSize - (dividersCount * dividerSize);
        logger.debug("{} SplitPane total old size: {}, without dividers: {}", getComponent().getLogPrefix(),
                oldSize, oldSizeWithoutDividers);
        return oldSizeWithoutDividers;
    }

    private double computeNewSize(double dividerSize) {
        double newSize = splitPane.getWidth();
        if (splitPane.getOrientation() == Orientation.VERTICAL) {
            newSize = splitPane.getHeight();
        }
        var dividersCount = 0;
        if (!splitPane.getItems().isEmpty()) {
            dividersCount = splitPane.getItems().size() - 1;
        }
        var newSizeWithoutDividers = newSize - (dividersCount * dividerSize);
        logger.debug("{} SplitPane total new size: {}, without dividers: {}", getComponent().getLogPrefix(),
                newSize, newSizeWithoutDividers);
        return newSizeWithoutDividers;
    }
}
