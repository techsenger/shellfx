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

import com.techsenger.tabshell.core.pane.AbstractPaneViewModel;
import com.techsenger.tabshell.core.pane.PaneKey;
import com.techsenger.tabshell.layout.LayoutComponentKeys;
import static com.techsenger.tabshell.layout.dock.DockConstants.ONE_THIRD;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.ReadOnlyProperty;
import javafx.geometry.Side;
import static javafx.geometry.Side.LEFT;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Pavel Castornii
 */
public class SplitSpaceViewModel extends AbstractPaneViewModel {

    private static final Logger logger = LoggerFactory.getLogger(SplitSpaceViewModel.class);

    private final ReadOnlyObjectWrapper<SpaceReceiver> spaceReceiver = new ReadOnlyObjectWrapper<>();

    protected SplitSpaceViewModel() {

    }

    @Override
    public PaneKey getKey() {
        return LayoutComponentKeys.SPLIT_SPACE;
    }

    /**
     * Returns the value of {@link #spaceReceiverProperty()}.
     *
     * @return the current {@link SpaceReceiver} of this component
     */
    public final SpaceReceiver getSpaceReceiver() {
        return spaceReceiver.get();
    }

    /**
     * The property that defines which neighboring component will receive this component's space if it is removed
     * from the layout.
     *
     * @return the {@link ReadOnlyProperty} of the {@link SpaceReceiver}
     */
    public final ReadOnlyProperty<SpaceReceiver> spaceReceiverProperty() {
        return spaceReceiver.getReadOnlyProperty();
    }

    /**
     * Sets the value of {@link #spaceReceiverProperty()}.
     *
     * @param value the new {@link SpaceReceiver} for this component
     */
    public final void setSpaceReceiver(SpaceReceiver value) {
        spaceReceiver.set(value);
    }

    @Override
    public SplitSpaceHelper getComponentHelper() {
        return (SplitSpaceHelper) super.getComponentHelper();
    }

    void updateHalfDividersOnAdd(int anchorContainerIndex, double[] oldPositions) {
        int newDividerCount = oldPositions.length + 1;
        double[] newPositions = new double[newDividerCount];

        if (oldPositions.length == 0) {
            newPositions[0] = 0.5;
        } else {
            double leftBound = (anchorContainerIndex == 0) ? 0.0 : oldPositions[anchorContainerIndex - 1];
            double rightBound =
                    (anchorContainerIndex == oldPositions.length) ? 1.0 : oldPositions[anchorContainerIndex];
            double middle = (leftBound + rightBound) / 2;
            for (int i = 0; i < newDividerCount; i++) {
                if (i < anchorContainerIndex) {
                    newPositions[i] = oldPositions[i];
                } else if (i == anchorContainerIndex) {
                    newPositions[i] = middle;
                } else {
                    newPositions[i] = oldPositions[i - 1];
                }
            }
        }
        getComponentHelper().setDividerPositions(newPositions);
        logger.debug("Updated half dividers on add; oldPositions: {}, newPositions: {}", oldPositions, newPositions);
    }

    void updateThirdDividersOnAdd(int anchorContainerIndex, double[] oldPositions, Side side) {
        double firstFraction = 1 - ONE_THIRD;
        double secondFraction = ONE_THIRD;
        if (side == Side.TOP || side == LEFT) {
            firstFraction = ONE_THIRD;
            secondFraction = 1 - ONE_THIRD;
        }
        int newDividerCount = oldPositions.length + 1;
        double[] newPositions = new double[newDividerCount];

        if (oldPositions.length == 0) {
            newPositions[0] = firstFraction;
        } else {
            double leftBound = (anchorContainerIndex == 0) ? 0.0 : oldPositions[anchorContainerIndex - 1];
            double rightBound =
                    (anchorContainerIndex == oldPositions.length) ? 1.0 : oldPositions[anchorContainerIndex];
            double firstPart = leftBound + (rightBound - leftBound) * firstFraction;
            for (int i = 0; i < newDividerCount; i++) {
                if (i < anchorContainerIndex) {
                    newPositions[i] = oldPositions[i];
                } else if (i == anchorContainerIndex) {
                    newPositions[i] = firstPart;
                } else {
                    newPositions[i] = oldPositions[i - 1];
                }
            }
        }
        getComponentHelper().setDividerPositions(newPositions);
        logger.debug("Updated third dividers on add; oldPositions: {}, newPositions: {}", oldPositions, newPositions);
    }

    /**
     * Updates divider positions with custom proportions from neighbors.
     *
     * @param splitPane the SplitPane containing the containers
     * @param newContainerIndex the index where new container was inserted
     * @param beforeProportion proportion taken from the container before insertion point
     * @param afterProportion proportion taken from the container after insertion point
     * @param oldPositions divider positions before the insertion
     */
    void updateIntermediateDividersOnAdd(int newContainerIndex,
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
        getComponentHelper().setDividerPositions(newPositions);
        logger.debug("Updated intermediate dividers on add; oldPositions: {}, newPositions: {}", oldPositions,
                newPositions);
    }

    /**
     * Updates divider positions after removing a dock.
     *
     * @param splitPane        SplitPane where the removal occurs
     * @param oldPositions     divider positions before removal (length N-1, for N docks before removal)
     * @param removedIndex     index of the removed container (0-based)
     * @param spaceReceiver    strategy for redistributing freed space
     */
    void updateDividersOnRemove(double[] oldPositions, int removedIndex, SpaceReceiver spaceReceiver) {
        int oldCount = oldPositions.length + 1;
        int newCount = oldCount - 1;

        // Calculate initial dock sizes from divider positions
        double[] sizes = new double[oldCount];
        if (oldPositions.length > 0) {
            sizes[0] = oldPositions[0];
        } else {
            sizes[0] = 1.0;
        }
        for (int i = 1; i < oldCount - 1; i++) {
            sizes[i] = oldPositions[i] - oldPositions[i - 1];
        }
        if (oldPositions.length > 0) {
            sizes[oldCount - 1] = 1.0 - oldPositions[oldCount - 2];
        }

        double removedSize = sizes[removedIndex];

        // Redistribute freed space
        if (spaceReceiver == SpaceReceiver.PREVIOUS) {
            if (removedIndex > 0) {
                sizes[removedIndex - 1] += removedSize;
            }
        } else if (spaceReceiver == SpaceReceiver.NEXT) {
            if (removedIndex < sizes.length - 1) {
                sizes[removedIndex + 1] += removedSize;
            }
        } else if (spaceReceiver == SpaceReceiver.BOTH) {
            int left = removedIndex - 1;
            int right = removedIndex + 1;
            double total = 0.0;
            if (left >= 0) {
                total += sizes[left];
            }
            if (right < sizes.length) {
                total += sizes[right];
            }
            if (total > 0.0) {
                if (left >= 0) {
                    sizes[left] += removedSize * (sizes[left] / total);
                }
                if (right < sizes.length) {
                    sizes[right] += removedSize * (sizes[right] / total);
                }
            }
        }

        // Build new sizes array (skip removed dock)
        double[] newSizes = new double[newCount];
        int idx = 0;
        for (int i = 0; i < sizes.length; i++) {
            if (i != removedIndex) {
                newSizes[idx] = sizes[i];
                idx++;
            }
        }

        // Calculate new divider positions (cumulative sum)
        double[] newPositions = new double[newCount - 1];
        double acc = 0.0;
        for (int i = 0; i < newPositions.length; i++) {
            acc += newSizes[i];
            newPositions[i] = acc;
        }
        getComponentHelper().setDividerPositions(newPositions);
        logger.debug("Updated dividers on remove; spaceReceiver: {}, oldPositions: {}, newPositions: {}", spaceReceiver,
                oldPositions, newPositions);
    }

    /**
     * Updates divider positions for a parent SplitPane after unwrapping a child SplitPane.
     *
     * @param splitPane The SplitPane whose divider positions need to be updated.
     * @param oldPositions The divider positions (from 0 to 1) of the parent SplitPane BEFORE unwrapping.
     * @param unwrapIndex The index in the parent SplitPane where the unwrapped SplitPane was located
     * @param childPositions The divider positions (from 0 to 1) of the removed child SplitPane.
     */
    void updateDividersOnUnwrap(double[] oldPositions, int unwrapIndex, double[] childPositions) {
        double[] newPositions;

        if (childPositions == null || childPositions.length == 0) {
            // If childPositions is empty, the child SplitPane had only one dock.
            // After unwrap, the number of children in parent stays the same.
            // If parent has 2 children, divider should be kept.
            // If parent has 1 child, no divider.
            if (oldPositions.length == 1) {
                newPositions = new double[] {oldPositions[0]};
            } else {
                newPositions = new double[0];
            }
        } else {
            // Number of new dividers: oldPositions.length + childPositions.length
            newPositions = new double[oldPositions.length + childPositions.length];

            int pos = 0;
            // Copy old dividers before unwrapIndex
            for (int i = 0; i < unwrapIndex; i++) {
                newPositions[pos++] = oldPositions[i];
            }

            // Calculate bounds for childPositions
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
        getComponentHelper().setDividerPositions(newPositions);
        logger.debug("Updated dividers on unwrap; oldPositions: {}, childPositions: {}, newPositions: {}",
                oldPositions, childPositions, newPositions);
    }
}
