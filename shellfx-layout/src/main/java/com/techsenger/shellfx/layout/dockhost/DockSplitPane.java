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
 * A {@link SplitPane} used internally by the docking layout ({@code DockHostFxView}) to arrange
 * {@code Area}-based components (TabDocks and the main area) into a nested tree of horizontal/vertical splits.
 * <p>
 * Beyond what {@code SplitPane} already provides, this class adds two things:
 * <ol>
 *     <li><b>Logical vs. live children.</b> {@link #getItems()} (inherited from {@code SplitPane}) only ever
 *     holds children currently visible in the tree. This class additionally tracks {@link #logicalItems}, the
 *     canonical order of every child that logically belongs here — including ones temporarily minimized into a
 *     SideBar and therefore absent from {@code getItems()}. The logical order never changes on minimize/restore,
 *     only on genuine structural changes (insert, permanent removal, wrap, unwrap) — see {@link #minimize} and
 *     {@link #restore}.</li>
 *     <li><b>Divider-position arithmetic.</b> Every {@code updateDividersOn*} method recomputes this SplitPane's
 *     divider positions after some structural change (a child added, removed, or a passive resize such as a
 *     SideBar appearing or disappearing), so that the change reads as a deliberate, proportionate redistribution
 *     of space rather than JavaFX's own default (usually an equal split, or leaving the change unaccounted for).
 *     All such methods share one closing step: {@link #snapPositions}, which pixel-snaps the freshly computed
 *     positions before they are stored — see that method's Javadoc for why skipping this step causes a slow
 *     drift in divider positions across repeated operations.</li>
 * </ol>
 * This is deliberately a low-level, arithmetic-heavy class: the {@code Transformer} in {@code DockHostFxView}
 * owns all decisions about *what* structural change is happening and *who* participates as donor/receiver of
 * space; this class only owns *how* the resulting pixel/fraction math is carried out.
 *
 * @author Pavel Castornii
 */
class DockSplitPane extends SplitPane {

    private static final Logger logger = LoggerFactory.getLogger(DockSplitPane.class);

    /**
     * Maps a pre-removal item index to its corresponding index in the post-removal array, where the removed item
     * at {@code removedIndex} is gone and every index after it shifts back by one.
     *
     * @param oldIndex an index into the pre-removal array
     * @param removedIndex the index of the item being removed, also in the pre-removal array
     * @return the corresponding index into the post-removal array
     */
    private static int mapToPostRemovalIndex(int oldIndex, int removedIndex) {
        return oldIndex < removedIndex ? oldIndex : oldIndex - 1;
    }

    /**
     * Applies {@link #mapToPostRemovalIndex} to every index in {@code oldIndices}.
     *
     * @param oldIndices indices into the pre-removal array
     * @param removedIndex the index of the item being removed, also in the pre-removal array
     * @return the corresponding indices into the post-removal array
     */
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
     * @param oldSizes absolute sizes of every item before the resize, in order
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
     *
     * @param oldSizes absolute sizes of every item, indexed as before the structural change
     * @param participantIndices indices into {@code oldSizes} of the items that donate/receive space; must be
     *         non-empty
     * @param amount the total space to distribute
     * @param isDonation {@code true} if the participants are giving up space (subject to {@code MIN_SIZE}),
     *         {@code false} if they are receiving it (no floor applies)
     * @return a map from participant index to the absolute amount that participant contributes/receives; keys
     *         are exactly {@code participantIndices}
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

    /**
     * Maps a pre-insertion item index to its corresponding index in the post-insertion array, where a new item
     * is inserted at {@code insertedAt} and every index at or after it shifts forward by one.
     *
     * @param oldIndex an index into the pre-insertion array
     * @param insertedAt the index the new item is inserted at, in the post-insertion array
     * @return the corresponding index into the post-insertion array
     */
    private static int mapToNewIndex(int oldIndex, int insertedAt) {
        return oldIndex < insertedAt ? oldIndex : oldIndex + 1;
    }

    /**
     * Converts divider fractions into absolute item sizes.
     *
     * @param oldSize the SplitPane's total size (in its orientation), excluding dividers
     * @param oldPositions divider fractions (0.0 to 1.0), in order; may be empty (a single-item SplitPane)
     * @return the absolute size of each item, in order; one more entry than {@code oldPositions}
     */
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

    /**
     * Creates a new, empty DockSplitPane.
     *
     * @param logPrefix prefix prepended to every log message this instance emits, identifying the owning
     *         {@code DockHostFxView} (e.g. its own log prefix), since a single window may contain many
     *         DockSplitPane instances
     */
    DockSplitPane(String logPrefix) {
        this.logPrefix = logPrefix;
        this.shortUuid = uuid.toString().substring(0, 8);
        this.fullName = getClass().getSimpleName() + "@" + shortUuid;
    }

    /**
     * Returns an unmodifiable view of all logical children — live and minimized alike, in canonical order.
     *
     * @return the logical children, in order
     */
    @Unmodifiable List<Node> getLogicalItems() {
        return Collections.unmodifiableList(logicalItems);
    }

    /**
     * Returns whether {@code node} is currently live — present in {@link #getItems()} — as opposed to logically
     * present but minimized.
     *
     * @param node the node to check
     * @return {@code true} if {@code node} is currently in {@link #getItems()}
     */
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

    /**
     * Removes a child entirely, from both live items and canonical order. Used when a child is permanently
     * removed (e.g. a TabDock is closed), never when it is only minimized.
     *
     * @param node the child to remove
     */
    void removePermanently(Node node) {
        getItems().remove(node);
        logicalItems.remove(node);
    }

    /**
     * Removes a child from the live items only, keeping it in the canonical logical order so it can later be
     * restored at the correct position. Used when minimizing a TabDock to the SideBar.
     *
     * @param node the child to minimize; must already be a logical child of this split
     * @throws IllegalArgumentException if {@code node} is not a logical child of this split
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
     *
     * @param node the child to restore; must already be a logical child of this split, and not currently live
     * @throws IllegalArgumentException if {@code node} is not a logical child of this split
     * @throws IllegalStateException if {@code node} is already live
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
     *
     * @return {@code true} if this split should be collapsed via unwrap
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

    /**
     * Logs this split's current live child sizes and divider positions at debug level, tagged with {@code note}.
     *
     * @param note a short label identifying the calling context, included in the log line
     */
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
        snapPositions(newPositions);
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
        snapPositions(newPositions);
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
        snapPositions(newPositions);
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
        snapPositions(newPositions);
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
     * @param oldSize the SplitPane's total size (in its orientation) before this change, including dividers
     * @param oldPositions divider positions (0.0 to 1.0) before this change
     * @param dividerSize the pixel width/height of a single divider, used to convert between total size and
     *         size-excluding-dividers; negative if not yet measurable, in which case it is recomputed after the
     *         structural change has already happened
     * @param flexibleChildIndex index (in the post-insertion array) of the main area
     * @param newChildIndex index (in the post-insertion array) where the new node is inserted
     * @param newChildSize the desired size for the new node
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
            snapPositions(newPositions);
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
        snapPositions(newPositions);
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
     *
     * @param oldSize the SplitPane's total size (in its orientation) before this change, including dividers
     * @param oldPositions divider positions (0.0 to 1.0) before this change
     * @param dividerSize the pixel width/height of a single divider; negative if not yet measurable
     * @param newChildIndex index (in the post-insertion array) where the new node is inserted
     * @param newChildSize the desired size for the new node
     * @param donorIndices pre-insertion indices of the donor(s); empty only when there is nothing to donate from
     */
    void updateDividersOnAddWithoutMain(double oldSize, double[] oldPositions, double dividerSize, int newChildIndex,
            double newChildSize, Set<Integer> donorIndices) {
        oldSize = computeOldSize(oldSize, oldPositions, dividerSize);
        var newSize = computeNewSize(dividerSize);
        if (oldSize <= 0) {
            double[] newPositions = new double[1];
            newPositions[0] = newChildIndex == 0 ? newChildSize / newSize : (newSize - newChildSize) / newSize;
            newPositions[0] = Math.max(0.0, Math.min(1.0, newPositions[0]));
            snapPositions(newPositions);
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
        snapPositions(newPositions);
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
     * @param oldSize the SplitPane's total size (in its orientation) before this change, including dividers
     * @param oldPositions divider positions (0.0 to 1.0) before this change
     * @param dividerSize the pixel width/height of a single divider; negative if not yet measurable
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
        snapPositions(newPositions);
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
     *
     * @param oldSize the SplitPane's total size (in its orientation) before this change, including dividers
     * @param oldPositions divider positions (0.0 to 1.0) before this change
     * @param dividerSize the pixel width/height of a single divider; negative if not yet measurable
     * @param removedChildIndex index (in the pre-removal array) of the node being removed
     * @param receiverIndices pre-removal indices of the receiver(s) — mapped internally to the post-removal array
     *         before use
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
        snapPositions(newPositions);
        setDividerPositions(newPositions);
        logger.debug("{} Updated dividers on remove without main; receiverIndices: {}, oldPositions: {}, "
                + "newPositions: {}", logPrefix, receiverIndices, oldPositions, newPositions);
    }

    /**
     * Computes SplitPane divider size - width for vertical dividers, height for horizontal dividers.
     * <p>
     * Derived by measuring the current live layout: the total pane size minus the summed bounds of every live
     * item, divided evenly across the gaps between them. Returns {@code -1} (unmeasurable) if there are no
     * dividers yet, or if this SplitPane has not yet been laid out (its size is not yet known).
     *
     * @return the measured divider size in pixels, or {@code -1} if not currently measurable
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

    /**
     * Resolves the live index a logical child currently occupies (or would occupy, for a minimized child, once
     * restored) among only the currently-live items.
     *
     * @param node a logical child of this split
     * @return the live index corresponding to {@code node}'s position in the logical order
     * @throws IllegalStateException if {@code node} is not a logical child of this split
     */
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

    /**
     * Resolves the position in {@link #logicalItems} a newly-inserted child should occupy, given the live index
     * it is being inserted at.
     *
     * @param liveIndex the live index the new child is being inserted at
     * @return the corresponding index into {@link #logicalItems}
     */
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
     * @return the current live size (width if horizontal, height if vertical) of each live item, in order
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

    /**
     * Strips the space taken up by dividers out of this SplitPane's total pre-change size, yielding the combined
     * size of its items alone.
     *
     * @param oldSize the SplitPane's total size (in its orientation) before this change, including dividers
     * @param oldPositions divider positions (0.0 to 1.0) before this change; only its length (the divider count)
     *         is used here
     * @param dividerSize the pixel width/height of a single divider
     * @return {@code oldSize}, minus the combined width/height of all dividers
     */
    private double computeOldSize(double oldSize, double[] oldPositions, double dividerSize) {
        var dividersCount = oldPositions.length;
        var oldSizeWithoutDividers = oldSize - (dividersCount * dividerSize);
        logger.debug("{} SplitPane total old size: {}, without dividers: {}", logPrefix,
                oldSize, oldSizeWithoutDividers);
        return oldSizeWithoutDividers;
    }

    /**
     * Strips the space taken up by dividers out of this SplitPane's current (post-change) total size, yielding
     * the combined size its items should occupy after the change.
     * <p>
     * Reads the pane's current live width/height directly — this is always called after the structural mutation
     * (insertion/removal) has already happened, so it reflects the pane's true, current size, not a stale one.
     *
     * @param dividerSize the pixel width/height of a single divider
     * @return the SplitPane's current total size (in its orientation), minus the combined width/height of all
     *         dividers implied by its current item count
     */
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

    /**
     * Snaps each divider fraction in {@code positions} to the pixel grid, using this SplitPane's current size in
     * its own orientation.
     * <p>
     * Divider fractions computed purely in floating-point arithmetic virtually never land exactly on a whole
     * pixel. When {@link #setDividerPositions} is applied, {@code SplitPaneSkin} snaps each divider to the
     * nearest whole pixel anyway (subject to {@link #isSnapToPixel()}) — but only once this pane is actually laid
     * out on screen, which can happen well after this method returns. Every method in this class that reads
     * {@code oldPositions} on a subsequent call sees whatever was last stored via {@link #setDividerPositions} —
     * if that was an un-snapped fraction, it silently diverges from the pixel grid the user actually sees, and
     * that divergence compounds across repeated add/remove/minimize/restore cycles on the same SplitPane (verified
     * empirically: without this step, two equal siblings around a repeatedly minimized/restored middle TabDock
     * visibly drift apart by a few pixels after a handful of cycles). Snapping here, before the fraction is ever
     * stored, keeps what this class computes and what the skin will render in agreement, eliminating that drift.
     * <p>
     * Uses {@link #snapSize(double)} (ceiling semantics, matching what {@code SplitPaneSkin} itself uses), so
     * this is a no-op when {@link #isSnapToPixel()} is {@code false}. Also a no-op if this pane's current size in
     * its orientation is not yet known (0 or unset) — dividing by an unknown size would be meaningless.
     *
     * @param positions divider fractions (0.0 to 1.0), in order
     * @return {@code positions}, each snapped to the nearest whole pixel; the same array instance, mutated in
     *         place, returned only for call-site convenience
     */
    private double[] snapPositions(double[] positions) {
        double totalSize = getOrientation() == Orientation.HORIZONTAL ? getWidth() : getHeight();
        if (totalSize <= 0) {
            return positions;
        }
        for (int i = 0; i < positions.length; i++) {
            positions[i] = snapSize(positions[i] * totalSize) / totalSize;
        }
        return positions;
    }
}
