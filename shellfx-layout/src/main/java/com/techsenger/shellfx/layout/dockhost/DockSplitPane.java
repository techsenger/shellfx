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

package com.techsenger.shellfx.layout.dockhost;

import com.techsenger.annotations.Unmodifiable;
import static com.techsenger.shellfx.layout.dockhost.DockConstants.ONE_THIRD;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import javafx.geometry.Orientation;
import javafx.geometry.Side;
import static javafx.geometry.Side.LEFT;
import javafx.scene.Node;
import javafx.scene.control.SplitPane;
import javafx.scene.layout.Region;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Pavel Castornii
 */
class DockSplitPane extends SplitPane {

    /**
     * Splits {@code neededSize} among the given donors, proportionally to each donor's current size relative to
     * the combined size of all donors in {@code donorIndices}. For example, if two donors have sizes 1000 and 500
     * and {@code neededSize} is 300, the first donor contributes 200 and the second 100 — proportional to their
     * 2:1 size ratio.
     * <p>
     * No donor is reduced below {@link TabDockFxView#MIN_SIZE}. If a donor's proportional share would push it
     * below that floor, its contribution is capped there instead, and the shortfall is redistributed among the
     * remaining donors, proportionally to their own sizes, repeating until either the full {@code neededSize} has
     * been distributed or every donor has hit the floor — in which case the new child ends up smaller than
     * requested.
     *
     * @param oldSizes current absolute sizes of every item in the SplitPane, indexed as before insertion
     * @param donorIndices indices into {@code oldSizes} of the items that donate space; must be non-empty
     * @param neededSize the total space requested for the new child
     * @return a map from donor index to the absolute amount that donor contributes; keys are exactly
     *         {@code donorIndices}
     */
    private static Map<Integer, Double> distributeDonation(double[] oldSizes, Set<Integer> donorIndices,
            double neededSize) {
        var remaining = new HashSet<>(donorIndices);
        var contribution = new HashMap<Integer, Double>();
        for (int i : donorIndices) {
            contribution.put(i, 0.0);
        }
        double toDistribute = neededSize;
        for (int pass = 0; pass < donorIndices.size() && toDistribute > 0 && !remaining.isEmpty(); pass++) {
            double totalRemainingSize = remaining.stream().mapToDouble(i -> oldSizes[i]).sum();
            if (totalRemainingSize <= 0) {
                break;
            }
            var floored = new HashSet<Integer>();
            double distributedThisPass = 0;
            for (int i : remaining) {
                double share = toDistribute * (oldSizes[i] / totalRemainingSize);
                double available = Math.max(oldSizes[i] - TabDockFxView.MIN_SIZE, 0) - contribution.get(i);
                double taken = Math.max(Math.min(share, available), 0);
                if (taken < share) {
                    floored.add(i);
                }
                contribution.merge(i, taken, Double::sum);
                distributedThisPass += taken;
            }
            toDistribute -= distributedThisPass;
            remaining.removeAll(floored);
            if (floored.isEmpty()) {
                break; // everyone received their exact proportional share
            }
        }
        return contribution;
    }

    /**
     * Resolves {@code donor} into concrete old-space indices — indices into the pre-insertion {@code oldSizes}
     * array used by {@link #distributeDonation} — for a new child being inserted at {@code newChildIndex} into a
     * SplitPane that had {@code oldItemCount} items before insertion.
     *
     * @param donor the donor choice to resolve; the caller guarantees it is actually available at this insertion
     *         point (see {@code Transformer#resolveDonorOptions})
     * @param newChildIndex the index (in the post-insertion array) the new child will occupy
     * @param oldItemCount the number of items in the SplitPane before insertion
     * @return the old-space indices of the donor(s)
     */
    private static Set<Integer> resolveDonorIndices(SpaceDonor donor, int newChildIndex, int oldItemCount) {
        int previousIndex = newChildIndex - 1;
        int nextIndex = newChildIndex;
        return switch (donor) {
            case PREVIOUS_SIBLING -> Set.of(previousIndex);
            case NEXT_SIBLING -> Set.of(nextIndex);
            case NEAREST_SIBLINGS -> {
                var indices = new HashSet<Integer>();
                if (previousIndex >= 0) {
                    indices.add(previousIndex);
                }
                if (nextIndex < oldItemCount) {
                    indices.add(nextIndex);
                }
                yield indices;
            }
            case ALL_SIBLINGS -> IntStream.range(0, oldItemCount).boxed().collect(Collectors.toSet());
        };
    }

    /**
     * Maps an old-space index (into the pre-insertion items) to its corresponding index in the post-insertion
     * array, where the new child occupies {@code newChildIndex} and every old-space index at or after it shifts
     * by one.
     */
    private static int mapToNewIndex(int oldIndex, int newChildIndex) {
        return oldIndex < newChildIndex ? oldIndex : oldIndex + 1;
    }

    private static final Logger logger = LoggerFactory.getLogger(DockSplitPane.class);

    /**
     * The canonical, logical order of this split's children — both those currently live in {@link #getItems()}
     * and those temporarily minimized (removed from {@link #getItems()} but still logically belonging here).
     * This order never changes on minimize/restore, only on genuine structural changes (insert/remove/wrap/unwrap).
     */
    private final List<Node> logicalItems = new ArrayList<>();

    private final UUID uuid = UUID.randomUUID();

    private final String shortUuid;

    private final String logPrefix;

    private final String fullName;

    DockSplitPane(String logPrefix) {
        this.logPrefix = logPrefix;
        this.shortUuid = uuid.toString().substring(0, 8);
        this.fullName = getClass().getSimpleName() + "@" + shortUuid;
    }

    /**
     * Returns an unmodifiable view of all logical children — live and minimized alike, in canonical order.
     */
    @Unmodifiable List<Node> getLogicalItems() {
        return Collections.unmodifiableList(logicalItems);
    }

    boolean isLive(Node node) {
        return getItems().contains(node);
    }

    /**
     * Inserts a brand-new child into both the live items and the canonical logical order.
     *
     * @param liveIndex the index among the currently-live items to insert at
     * @param node the child to insert
     */
    void insertNew(int liveIndex, Node node) {
        getItems().add(liveIndex, node);
        logicalItems.add(resolveLogicalIndexForLiveIndex(liveIndex), node);
    }

    /**
     * Replaces a brand-new child into both the live items and the canonical logical order.
     *
     * @param liveIndex the index among the currently-live items to insert at
     * @param node the child to replace
     */
    void replace(int liveIndex, Node node) {
        getItems().set(liveIndex, node);
        logicalItems.set(resolveLogicalIndexForLiveIndex(liveIndex), node);
    }

    /**
     * Removes a child entirely, from both live items and canonical order. Used when a child is permanently
     * removed (e.g. a TabDock is closed), never when it is only minimized.
     */
    void removePermanently(Node node) {
        getItems().remove(node);
        logicalItems.remove(node);
    }

    /**
     * Removes a child from the live items only, keeping it in the canonical logical order so it can later be
     * restored at the correct position. Used when minimizing a TabDock to the SideBar.
     */
    void minimize(Node node) {
        if (!logicalItems.contains(node)) {
            throw new IllegalArgumentException("Node is not a logical child of this split");
        }
        getItems().remove(node);
    }

    /**
     * Re-inserts a previously minimized child back into the live items, at the position implied by its place in
     * the canonical logical order relative to the other currently-live children.
     */
    void restore(Node node) {
        if (!logicalItems.contains(node)) {
            throw new IllegalArgumentException("Node is not a logical child of this split");
        }
        if (isLive(node)) {
            throw new IllegalStateException("Node is already live");
        }
        getItems().add(resolveLiveInsertIndex(node), node);
    }

    /**
     * Returns true if this split has at most one logical child left — i.e. it should be unwrapped, whether or
     * not that one remaining child is currently live.
     */
    boolean shouldBeNormalized() {
        return logicalItems.size() <= 1;
    }

    UUID getUuid() {
        return uuid;
    }

    String getShortUuid() {
        return shortUuid;
    }

    String getFullName() {
        return fullName;
    }

    void logState(String note) {
        logger.debug("{} {} child sizes: {}, dividers: {}", logPrefix, note,
                getChildSizes(), getDividerPositions());
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
        setDividerPositions(newPositions);
        logger.debug("{} Updated dividers on half split; oldPositions: {}, newPositions: {}", logPrefix,
                oldPositions, newPositions);
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
        setDividerPositions(newPositions);
        logger.debug("{} Updated dividers on third split; oldPositions: {}, newPositions: {}", logPrefix,
                oldPositions, newPositions);
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
        setDividerPositions(newPositions);
        logger.debug("{} Updated dividers on insert between; oldPositions: {}, newPositions: {}", logPrefix,
                oldPositions, newPositions);
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

        setDividerPositions(newPositions);
        logger.debug("{} Updated dividers on unwrap; oldPositions: {}, childPositions: {}, newPositions: {}", logPrefix,
                oldPositions, childPositions, newPositions);
    }

    /**
     * Updates divider positions after SplitPane resize and node insertion, when this SplitPane has a main area.
     * <p>
     * Two concerns are handled independently:
     * <ul>
     *     <li>The SplitPane's own resize delta (the difference between {@code oldSize} and its current size) is
     *     always absorbed entirely by {@code flexibleChildIndex} — the main area — regardless of {@code donor}.</li>
     *     <li>The space for the new child ({@code newChildSize}) is taken from the sibling(s) identified by
     *     {@code donor}, proportionally to their current size — see {@link #distributeDonation}. These donors may
     *     or may not include {@code flexibleChildIndex}; if they do, both effects apply on top of each other.</li>
     * </ul>
     *
     * @param oldSize the original size of the SplitPane before changes
     * @param oldPositions array of divider positions (0.0 to 1.0) before changes
     * @param flexibleChildIndex index (in the post-insertion array) of the main area, which absorbs the SplitPane's
     *         own resize delta
     * @param newChildIndex index (in the post-insertion array) where the new node is inserted
     * @param newChildSize the desired size for the new node
     * @param donor identifies which existing sibling(s) donate {@code newChildSize} to the new node
     */
    void updateDividersOnAddWithMain(double oldSize, double[] oldPositions, double dividerSize, int flexibleChildIndex,
            int newChildIndex, double newChildSize, SpaceDonor donor) {
        oldSize = computeOldSize(oldSize, oldPositions, dividerSize);
        var newSize = computeNewSize(dividerSize);
        if (oldSize <= 0) {
            double[] newPositions = new double[1];
            newPositions[0] = newChildIndex == 0 ? newChildSize / newSize : (newSize - newChildSize) / newSize;
            newPositions[0] = Math.max(0.0, Math.min(1.0, newPositions[0]));
            logger.debug("{} Initial dividers setup with main; newSize: {}, flexibleChildIndex: {}, "
                    + "newChildIndex: {}, newChildSize: {}, newPositions: {}", logPrefix,
                    newSize, flexibleChildIndex, newChildIndex, newChildSize, newPositions);
            setDividerPositions(newPositions);
            return;
        }
        int oldNodeCount = oldPositions.length + 1;
        double[] oldSizes = new double[oldNodeCount];
        if (oldPositions.length == 0) {
            oldSizes[0] = oldSize;
        } else {
            oldSizes[0] = oldPositions[0] * oldSize;
            for (int i = 1; i < oldNodeCount - 1; i++) {
                oldSizes[i] = (oldPositions[i] - oldPositions[i - 1]) * oldSize;
            }
            oldSizes[oldNodeCount - 1] = (1.0 - oldPositions[oldPositions.length - 1]) * oldSize;
        }
        int newNodeCount = oldNodeCount + 1;
        double[] newSizes = new double[newNodeCount];
        for (int i = 0, j = 0; i < newNodeCount; i++) {
            newSizes[i] = (i == newChildIndex) ? 0 : oldSizes[j++];
        }
        // resize delta is always absorbed by main alone, independently of the donor(s) below
        double sizeChange = newSize - oldSize;
        newSizes[flexibleChildIndex] += sizeChange;
        var donorIndices = resolveDonorIndices(donor, newChildIndex, oldNodeCount);
        var contribution = distributeDonation(oldSizes, donorIndices, newChildSize);
        double actualNewChildSize = 0;
        for (var entry : contribution.entrySet()) {
            int newIndex = mapToNewIndex(entry.getKey(), newChildIndex);
            newSizes[newIndex] -= entry.getValue();
            actualNewChildSize += entry.getValue();
        }
        newSizes[newChildIndex] = actualNewChildSize;
        double[] newPositions = new double[newNodeCount - 1];
        double cumulativeSize = 0.0;
        for (int i = 0; i < newNodeCount - 1; i++) {
            cumulativeSize += newSizes[i];
            newPositions[i] = cumulativeSize / newSize;
        }
        logger.debug("{} Updated dividers on add with main; oldSize: {}, newSize: {}, flexibleChildIndex: {}, "
                + "newChildIndex: {}, newChildSize: {}, donor: {}, oldPositions: {}, newPositions: {}",
                logPrefix, oldSize, newSize, flexibleChildIndex, newChildIndex, newChildSize, donor,
                oldPositions, newPositions);
        setDividerPositions(newPositions);
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
                + "removedChildIndex: {}, oldPositions: {}, newPositions: {}", logPrefix, oldSize, newSize,
                flexibleChildIndex, removedChildIndex, oldPositions, newPositions);
        setDividerPositions(newPositions);
    }

    /**
     * Updates divider positions after SplitPane resize and node insertion, when this SplitPane has no main area.
     * <p>
     * Both the SplitPane's own resize delta and the space for the new child ({@code newChildSize}) are taken from
     * the sibling(s) identified by {@code donor} — see {@link #distributeDonation}. Unlike
     * {@link #updateDividersOnAddWithMain}, there is no separate flexible node to absorb the resize delta on its
     * own; each donor absorbs a share of it proportional to its share of the donation.
     *
     * @param oldSize the original size of the SplitPane before changes
     * @param oldPositions array of divider positions (0.0 to 1.0) before changes
     * @param newChildIndex index (in the post-insertion array) where the new node is inserted
     * @param newChildSize the desired size for the new node
     * @param donor identifies which existing sibling(s) donate {@code newChildSize} (and the resize delta) to the
     *         new node
     */
    void updateDividersOnAddWithoutMain(double oldSize, double[] oldPositions, double dividerSize, int newChildIndex,
            double newChildSize, SpaceDonor donor) {
        oldSize = computeOldSize(oldSize, oldPositions, dividerSize);
        var newSize = computeNewSize(dividerSize);
        if (oldSize <= 0) {
            double[] newPositions = new double[1];
            newPositions[0] = newChildIndex == 0 ? newChildSize / newSize : (newSize - newChildSize) / newSize;
            newPositions[0] = Math.max(0.0, Math.min(1.0, newPositions[0]));
            logger.debug("{} Initial dividers setup; newSize: {}, newChildIndex: {}, newChildSize: {}, "
                    + "newPositions: {}", logPrefix, newSize, newChildIndex, newChildSize, newPositions);
            setDividerPositions(newPositions);
            return;
        }
        int oldNodeCount = oldPositions.length + 1;
        double[] oldSizes = new double[oldNodeCount];
        if (oldPositions.length == 0) {
            oldSizes[0] = oldSize;
        } else {
            oldSizes[0] = oldPositions[0] * oldSize;
            for (int i = 1; i < oldNodeCount - 1; i++) {
                oldSizes[i] = (oldPositions[i] - oldPositions[i - 1]) * oldSize;
            }
            oldSizes[oldNodeCount - 1] = (1.0 - oldPositions[oldPositions.length - 1]) * oldSize;
        }
        double sizeChange = newSize - oldSize;
        int newNodeCount = oldNodeCount + 1;
        double[] newSizes = new double[newNodeCount];
        for (int i = 0, j = 0; i < newNodeCount; i++) {
            newSizes[i] = (i == newChildIndex) ? 0 : oldSizes[j++];
        }
        var donorIndices = resolveDonorIndices(donor, newChildIndex, oldNodeCount);
        var contribution = distributeDonation(oldSizes, donorIndices, newChildSize);
        double totalDonorOldSize = donorIndices.stream().mapToDouble(i -> oldSizes[i]).sum();
        double actualNewChildSize = 0;
        for (int oldIndex : donorIndices) {
            int newIndex = mapToNewIndex(oldIndex, newChildIndex);
            double donated = contribution.get(oldIndex);
            // the resize delta follows the same proportions as the donation itself among the chosen donors —
            // mirrors the pre-donor-choice behavior, where the resize delta always followed whichever neighbor(s)
            // happened to donate
            double sizeChangeShare = totalDonorOldSize > 0 ? sizeChange * (oldSizes[oldIndex] / totalDonorOldSize) : 0;
            newSizes[newIndex] = newSizes[newIndex] - donated + sizeChangeShare;
            actualNewChildSize += donated;
        }
        newSizes[newChildIndex] = actualNewChildSize;
        double[] newPositions = new double[newNodeCount - 1];
        double cumulativeSize = 0.0;
        for (int i = 0; i < newNodeCount - 1; i++) {
            cumulativeSize += newSizes[i];
            newPositions[i] = cumulativeSize / newSize;
        }
        logger.debug("{} Updated dividers on add without main; oldSize: {}, newSize: {}, newChildIndex: {}, "
                + "newChildSize: {}, donor: {}, oldPositions: {}, newPositions: {}",
                logPrefix, oldSize, newSize, newChildIndex, newChildSize, donor, oldPositions, newPositions);
        setDividerPositions(newPositions);
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
        if (getItems().isEmpty()) {
            return;
        }
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
                + "removedChildIndex: {}, oldPositions: {}, newPositions: {}", logPrefix,
                oldSize, newSize, removedChildIndex, oldPositions, newPositions);
        setDividerPositions(newPositions);
    }

    /**
     * Computes SplitPane divider size - width for vertical dividers, height for horizontal dividers.
     *
     * @return
     */
    double computeDividerSize() {
        var dividers = getDividerPositions();
        if (dividers.length == 0) {
            return -1;
        }

        double paneSize = getOrientation() == Orientation.HORIZONTAL ? getWidth() : getHeight();

        if (paneSize <= 0) {
            return -1;
        }
        double totalItemsSize = 0;
        for (var item : getItems()) {
            double itemSize = getOrientation() == Orientation.HORIZONTAL
                    ? item.getBoundsInParent().getWidth()
                    : item.getBoundsInParent().getHeight();
            totalItemsSize += itemSize;
        }
        double totalDividersSize = paneSize - totalItemsSize;
        double dividerSize = totalDividersSize / (getItems().size() - 1);
        logger.debug("{} Computed dividerSize: {}", logPrefix, dividerSize);
        return dividerSize;
    }

    int resolveLiveInsertIndex(Node node) {
        int liveIndex = 0;
        for (var n : logicalItems) {
            if (n == node) {
                return liveIndex;
            }
            if (isLive(n)) {
                liveIndex++;
            }
        }
        throw new IllegalStateException("Node not found in logical items");
    }

    int resolveLogicalIndexForLiveIndex(int liveIndex) {
        int seenLive = 0;
        for (int i = 0; i < logicalItems.size(); i++) {
            if (seenLive == liveIndex) {
                return i;
            }
            if (isLive(logicalItems.get(i))) {
                seenLive++;
            }
        }
        return logicalItems.size();
    }

    /**
     * Returns the widths/heights of the children for horizontal/vertical split space.
     *
     * @return
     */
    private List<Double> getChildSizes() {
        var sizes = new ArrayList<Double>();
        for (var item : getItems()) {
            if (getOrientation() == Orientation.HORIZONTAL) {
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
            dividersCount = getItems().size() - 1;
        }
        var oldSizeWithoutDividers = oldSize - (dividersCount * dividerSize);
        logger.debug("{} SplitPane total old size: {}, without dividers: {}", logPrefix,
                oldSize, oldSizeWithoutDividers);
        return oldSizeWithoutDividers;
    }

    private double computeNewSize(double dividerSize) {
        double newSize = getWidth();
        if (getOrientation() == Orientation.VERTICAL) {
            newSize = getHeight();
        }
        var dividersCount = 0;
        if (!getItems().isEmpty()) {
            dividersCount = getItems().size() - 1;
        }
        var newSizeWithoutDividers = newSize - (dividersCount * dividerSize);
        logger.debug("{} SplitPane total new size: {}, without dividers: {}", logPrefix, newSize,
                newSizeWithoutDividers);
        return newSizeWithoutDividers;
    }
}
