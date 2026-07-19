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

    private static final Logger logger = LoggerFactory.getLogger(DockSplitPane.class);

    /**
     * Maps a pre-removal item index to its corresponding index in the post-removal array, where the removed item
     * at {@code removedIndex} is gone and every index after it shifts back by one.
     */
    private static int mapToPostRemovalIndex(int oldIndex, int removedIndex) {
        return oldIndex < removedIndex ? oldIndex : oldIndex - 1;
    }

    private static Set<Integer> toPostRemovalIndices(Set<Integer> oldIndices, int removedIndex) {
        var result = new HashSet<Integer>(oldIndices.size());
        for (int i : oldIndices) {
            result.add(mapToPostRemovalIndex(i, removedIndex));
        }
        return result;
    }

    /**
     * Distributes {@code delta} — the SplitPane's own resize (e.g. a SideBar appearing or disappearing, shrinking
     * or growing every sibling in this SplitPane regardless of which one is about to donate or receive space) —
     * proportionally across every item in {@code oldSizes}, weighted by each item's own size.
     * <p>
     * Unlike {@link #distributeProportionally}, this always touches every item, not just a resolved donor/receiver
     * subset — because JavaFX shrinks/grows every live sibling by its fractional share when the SplitPane's own
     * width or height changes, independently of any donation logic layered on top.
     *
     * @param delta may be negative (the SplitPane got narrower) or positive (it got wider)
     * @return each item's size after absorbing its proportional share of {@code delta}
     */
    private static double[] distributeResizeDelta(double[] oldSizes, double delta) {
        double total = Arrays.stream(oldSizes).sum();
        double[] adjusted = new double[oldSizes.length];
        if (total <= 0) {
            System.arraycopy(oldSizes, 0, adjusted, 0, oldSizes.length);
            if (adjusted.length > 0) {
                adjusted[adjusted.length - 1] += delta;
            }
            return adjusted;
        }
        for (int i = 0; i < oldSizes.length; i++) {
            adjusted[i] = oldSizes[i] + delta * (oldSizes[i] / total);
        }
        return adjusted;
    }

    /**
     * Splits {@code amount} among the given indices, proportionally to each one's current size relative to the
     * combined size of all indices in {@code participantIndices}. For example, given sizes 1000 and 500 and an
     * amount of 300, the first receives/gives 200 and the second 100 — proportional to their 2:1 size ratio.
     * <p>
     * When {@code isDonation} is {@code true}, no participant is reduced below {@link TabDockFxView#MIN_SIZE};
     * if a participant's proportional share would push it below that floor, its contribution is capped there
     * and the shortfall is redistributed among the remaining participants, repeating until either the full
     * amount is distributed or every participant has hit the floor.
     */
    private static Map<Integer, Double> distributeProportionally(double[] oldSizes, Set<Integer> participantIndices,
            double amount, boolean isDonation) {
        var remaining = new HashSet<>(participantIndices);
        var contribution = new HashMap<Integer, Double>();
        for (int i : participantIndices) {
            contribution.put(i, 0.0);
        }
        double toDistribute = amount;
        for (int pass = 0; pass < participantIndices.size() && toDistribute > 0 && !remaining.isEmpty(); pass++) {
            double totalRemainingSize = remaining.stream().mapToDouble(i -> oldSizes[i]).sum();
            if (totalRemainingSize <= 0) {
                break;
            }
            var floored = new HashSet<Integer>();
            double distributedThisPass = 0;
            for (int i : remaining) {
                double share = toDistribute * (oldSizes[i] / totalRemainingSize);
                double taken = share;
                if (isDonation) {
                    double available = Math.max(oldSizes[i] - TabDockFxView.MIN_SIZE, 0) - contribution.get(i);
                    taken = Math.max(Math.min(share, available), 0);
                    if (taken < share) {
                        floored.add(i);
                    }
                }
                contribution.merge(i, taken, Double::sum);
                distributedThisPass += taken;
            }
            toDistribute -= distributedThisPass;
            remaining.removeAll(floored);
            if (floored.isEmpty()) {
                break;
            }
        }
        return contribution;
    }

    private static int mapToNewIndex(int oldIndex, int insertedAt) {
        return oldIndex < insertedAt ? oldIndex : oldIndex + 1;
    }

    private static double[] computeOldSizes(double oldSize, double[] oldPositions) {
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
        return oldSizes;
    }

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

    @Unmodifiable List<Node> getLogicalItems() {
        return Collections.unmodifiableList(logicalItems);
    }

    boolean isLive(Node node) {
        return getItems().contains(node);
    }

    void insertNew(int liveIndex, Node node) {
        getItems().add(liveIndex, node);
        logicalItems.add(resolveLogicalIndexForLiveIndex(liveIndex), node);
    }

    /**
     * Replaces a brand-new child into both the live items and the canonical logical order.
     * <p>
     * Divider positions are explicitly saved before and restored after the underlying {@code items.set(...)}
     * call. Unlike {@link #insertNew} and {@link #removePermanently}, which change the item count and are
     * handled correctly by {@code SplitPaneSkin}, replacing an item in place via {@link List#set} does not
     * reliably preserve existing divider positions — this is the step used when a dragged TabDock is dropped in
     * place of the placeholder that reserved its target position.
     *
     * @param liveIndex the index among the currently-live items to insert at
     * @param node the child to replace
     */
    void replace(int liveIndex, Node node) {
        double[] positions = getDividerPositions();
        getItems().set(liveIndex, node);
        logicalItems.set(resolveLogicalIndexForLiveIndex(liveIndex), node);
        if (positions.length > 0) {
            setDividerPositions(positions);
        }
    }

    void removePermanently(Node node) {
        getItems().remove(node);
        logicalItems.remove(node);
    }

    void minimize(Node node) {
        if (!logicalItems.contains(node)) {
            throw new IllegalArgumentException("Node is not a logical child of this split");
        }
        getItems().remove(node);
    }

    void restore(Node node) {
        if (!logicalItems.contains(node)) {
            throw new IllegalArgumentException("Node is not a logical child of this split");
        }
        if (isLive(node)) {
            throw new IllegalStateException("Node is already live");
        }
        getItems().add(resolveLiveInsertIndex(node), node);
    }

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

    void updateDividersOnInsertBetween(int newContainerIndex, double beforeProportion, double afterProportion,
            double[] oldPositions) {
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
        double total = 0;
        for (double s : newSizes) {
            total += s;
        }
        for (int i = 0; i < newSizes.length; i++) {
            newSizes[i] /= total;
        }
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

    void updateDividersOnUnwrap(int unwrapIndex, double[] oldPositions, double[] childPositions) {
        double[] newPositions;
        if (childPositions == null || childPositions.length == 0) {
            newPositions = Arrays.copyOf(oldPositions, oldPositions.length);
        } else {
            newPositions = new double[oldPositions.length + childPositions.length];
            int pos = 0;
            for (int i = 0; i < unwrapIndex; i++) {
                newPositions[pos++] = oldPositions[i];
            }
            double left = unwrapIndex == 0 ? 0.0 : oldPositions[unwrapIndex - 1];
            double right = unwrapIndex == oldPositions.length ? 1.0 : oldPositions[unwrapIndex];
            for (double p : childPositions) {
                newPositions[pos++] = left + (right - left) * p;
            }
            for (int i = unwrapIndex; i < oldPositions.length; i++) {
                newPositions[pos++] = oldPositions[i];
            }
        }
        setDividerPositions(newPositions);
        logger.debug("{} Updated dividers on unwrap; oldPositions: {}, childPositions: {}, newPositions: {}",
                logPrefix, oldPositions, childPositions, newPositions);
    }

    /**
     * Updates divider positions after SplitPane resize and node insertion, when this SplitPane has a main area.
     * <p>
     * The SplitPane's own resize delta is always absorbed entirely by {@code flexibleChildIndex} (the main
     * area), independently of {@code donorIndices}. The space for the new child ({@code newChildSize}) is taken
     * from {@code donorIndices} (pre-insertion indices), proportionally to their current size — see
     * {@link #distributeProportionally}.
     *
     * @param flexibleChildIndex index (in the post-insertion array) of the main area
     * @param newChildIndex index (in the post-insertion array) where the new node is inserted
     * @param donorIndices pre-insertion indices of the donor(s); empty only when there is nothing to donate from
     *         (a brand-new, single-item SplitPane), in which case this method returns without touching them
     */
    void updateDividersOnAddWithMain(double oldSize, double[] oldPositions, double dividerSize,
            int flexibleChildIndex, int newChildIndex, double newChildSize, Set<Integer> donorIndices) {
        oldSize = computeOldSize(oldSize, oldPositions, dividerSize);
        var newSize = computeNewSize(dividerSize);
        if (oldSize <= 0) {
            double[] newPositions = new double[1];
            newPositions[0] = newChildIndex == 0 ? newChildSize / newSize : (newSize - newChildSize) / newSize;
            newPositions[0] = Math.max(0.0, Math.min(1.0, newPositions[0]));
            setDividerPositions(newPositions);
            logger.debug("{} Initial dividers setup with main; newPositions: {}", logPrefix, newPositions);
            return;
        }
        double[] oldSizes = computeOldSizes(oldSize, oldPositions);
        int newNodeCount = oldSizes.length + 1;
        double[] newSizes = new double[newNodeCount];
        for (int i = 0, j = 0; i < newNodeCount; i++) {
            newSizes[i] = (i == newChildIndex) ? 0 : oldSizes[j++];
        }
        newSizes[flexibleChildIndex] += (newSize - oldSize);
        var contribution = distributeProportionally(oldSizes, donorIndices, newChildSize, true);
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
        setDividerPositions(newPositions);
        logger.debug("{} Updated dividers on add with main; donorIndices: {}, oldPositions: {}, newPositions: {}",
                logPrefix, donorIndices, oldPositions, newPositions);
    }

    /**
     * Updates divider positions after SplitPane resize and node insertion, when this SplitPane has no main area.
     * <p>
     * The resize delta (e.g. a SideBar appearing/disappearing) is absorbed proportionally by every existing item —
     * see {@link #distributeResizeDelta} — since JavaFX shrinks/grows all of them together regardless of donation.
     * Only after that, {@code newChildSize} is taken from {@code donorIndices}, proportionally to their
     * (already resize-adjusted) size — see {@link #distributeProportionally}.
     */
    void updateDividersOnAddWithoutMain(double oldSize, double[] oldPositions, double dividerSize, int newChildIndex,
            double newChildSize, Set<Integer> donorIndices) {
        oldSize = computeOldSize(oldSize, oldPositions, dividerSize);
        var newSize = computeNewSize(dividerSize);
        if (oldSize <= 0) {
            double[] newPositions = new double[1];
            newPositions[0] = newChildIndex == 0 ? newChildSize / newSize : (newSize - newChildSize) / newSize;
            newPositions[0] = Math.max(0.0, Math.min(1.0, newPositions[0]));
            setDividerPositions(newPositions);
            logger.debug("{} Initial dividers setup; newPositions: {}", logPrefix, newPositions);
            return;
        }
        double[] oldSizes = computeOldSizes(oldSize, oldPositions);
        double sizeChange = newSize - oldSize;
        double[] adjustedSizes = distributeResizeDelta(oldSizes, sizeChange);
        int newNodeCount = oldSizes.length + 1;
        double[] newSizes = new double[newNodeCount];
        for (int i = 0, j = 0; i < newNodeCount; i++) {
            newSizes[i] = (i == newChildIndex) ? 0 : adjustedSizes[j++];
        }
        var contribution = distributeProportionally(adjustedSizes, donorIndices, newChildSize, true);
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
        setDividerPositions(newPositions);
        logger.debug("{} Updated dividers on add without main; donorIndices: {}, oldPositions: {}, "
                + "newPositions: {}", logPrefix, donorIndices, oldPositions, newPositions);
    }

    /**
     * Updates divider positions for a parent SplitPane that has a main area, after a node is removed.
     * <p>
     * The resize delta is always absorbed entirely by {@code flexibleChildIndex} (the main area). The removed
     * node's freed space is given to {@code receiverIndices}, proportionally.
     *
     * @param flexibleChildIndex index (in the post-removal array) of the main area
     * @param removedChildIndex index (in the pre-removal array) of the node being removed
     * @param receiverIndices pre-removal indices of the receiver(s) — mapped internally to the post-removal array
     *         before use; empty only when the SplitPane had a single item before removal
     */
    void updateDividersOnRemoveWithMain(double oldSize, double[] oldPositions, double dividerSize,
            int flexibleChildIndex, int removedChildIndex, Set<Integer> receiverIndices) {
        oldSize = computeOldSize(oldSize, oldPositions, dividerSize);
        var newSize = computeNewSize(dividerSize);
        double[] oldSizes = computeOldSizes(oldSize, oldPositions);
        int newNodeCount = oldSizes.length - 1;
        if (newNodeCount <= 0) {
            setDividerPositions();
            logger.debug("{} Updated dividers on remove with main; no items remain", logPrefix);
            return;
        }
        double[] newSizes = new double[newNodeCount];
        for (int i = 0, j = 0; i < oldSizes.length; i++) {
            if (i != removedChildIndex) {
                newSizes[j++] = oldSizes[i];
            }
        }
        newSizes[flexibleChildIndex] += (newSize - oldSize);
        double removedNodeSize = oldSizes[removedChildIndex];
        var postRemovalReceiverIndices = toPostRemovalIndices(receiverIndices, removedChildIndex);
        var contribution = distributeProportionally(newSizes, postRemovalReceiverIndices, removedNodeSize, false);
        for (var entry : contribution.entrySet()) {
            newSizes[entry.getKey()] += entry.getValue();
        }
        double[] newPositions = new double[newNodeCount - 1];
        double cumulativeSize = 0.0;
        for (int i = 0; i < newNodeCount - 1; i++) {
            cumulativeSize += newSizes[i];
            newPositions[i] = cumulativeSize / newSize;
        }
        setDividerPositions(newPositions);
        logger.debug("{} Updated dividers on remove with main; receiverIndices: {} (post-removal: {}), "
                + "oldPositions: {}, newPositions: {}", logPrefix, receiverIndices, postRemovalReceiverIndices,
                oldPositions, newPositions);
    }

    /**
     * Updates divider positions for a parent SplitPane with no main area, after a node is removed.
     * <p>
     * The resize delta is distributed proportionally across the FULL pre-removal set of sizes — including the item
     * being removed — before that item's (now delta-adjusted) share is peeled off and handed to
     * {@code receiverIndices}. Distributing the delta on the full set first, rather than on the already-reduced
     * remaining set, is what keeps the split symmetric: two equally-sized siblings around a removed middle item
     * stay exactly equal after the removed item's space is returned to one of them, instead of drifting apart by a
     * few pixels on every minimize/restore cycle.
     */
    void updateDividersOnRemoveWithoutMain(double oldSize, double[] oldPositions, double dividerSize,
            int removedChildIndex, Set<Integer> receiverIndices) {
        if (getItems().isEmpty()) {
            return;
        }
        oldSize = computeOldSize(oldSize, oldPositions, dividerSize);
        var newSize = computeNewSize(dividerSize);
        double[] oldSizes = computeOldSizes(oldSize, oldPositions);
        double sizeChange = newSize - oldSize;
        double[] adjustedOldSizes = distributeResizeDelta(oldSizes, sizeChange);
        double removedNodeAdjustedSize = adjustedOldSizes[removedChildIndex];
        int newNodeCount = oldSizes.length - 1;
        if (newNodeCount <= 0) {
            logger.debug("{} Updated dividers on remove without main; no items remain", logPrefix);
            return;
        }
        double[] newSizes = new double[newNodeCount];
        for (int i = 0, j = 0; i < adjustedOldSizes.length; i++) {
            if (i != removedChildIndex) {
                newSizes[j++] = adjustedOldSizes[i];
            }
        }
        var postRemovalReceiverIndices = toPostRemovalIndices(receiverIndices, removedChildIndex);
        var contribution = distributeProportionally(newSizes, postRemovalReceiverIndices, removedNodeAdjustedSize,
                false);
        for (var entry : contribution.entrySet()) {
            newSizes[entry.getKey()] += entry.getValue();
        }
        double[] newPositions = new double[newNodeCount - 1];
        double cumulativeSize = 0.0;
        for (int i = 0; i < newNodeCount - 1; i++) {
            cumulativeSize += newSizes[i];
            newPositions[i] = cumulativeSize / newSize;
        }
        setDividerPositions(newPositions);
        logger.debug("{} Updated dividers on remove without main; receiverIndices: {}, oldPositions: {}, "
                + "newPositions: {}", logPrefix, receiverIndices, oldPositions, newPositions);
    }

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
        var dividersCount = oldPositions.length;
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
