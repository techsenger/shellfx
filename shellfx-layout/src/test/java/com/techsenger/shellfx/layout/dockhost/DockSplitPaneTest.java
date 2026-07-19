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

import java.util.Set;
import javafx.application.Platform;
import javafx.geometry.Orientation;
import javafx.geometry.Side;
import javafx.scene.Node;
import javafx.scene.control.SplitPane;
import javafx.scene.layout.Region;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.assertj.core.api.Assertions.assertThatIllegalStateException;
import org.assertj.core.data.Offset;
import static org.assertj.core.data.Offset.offset;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Tests for {@link DockSplitPane}'s two responsibilities: tracking live vs. logical children across
 * minimize/restore, and the divider-position arithmetic in the {@code updateDividersOn*} methods.
 * <p>
 * Every {@code updateDividersOn*} test pre-populates the pane with exactly the number of live items the
 * post-change state should have, via {@link #newChild()}/{@code insertNew}, before invoking the method under
 * test. This matters mechanically, not just for realism: JavaFX's {@code SplitPane#setDividerPositions} assigns
 * values to already-existing {@code Divider} objects, and the number of those objects is derived from the
 * current item count ({@code items.size() - 1}) — it does not create dividers on demand. Calling an
 * {@code updateDividersOn*} method against an empty pane leaves {@code getDividerPositions()} at {@code []}
 * regardless of what was computed internally, which silently defeats the assertion rather than failing loudly at
 * the point of the actual mismatch.
 * <p>
 * Most arithmetic tests deliberately pass {@code dividerSize = 0}, which makes {@code computeOldSize} and
 * {@code computeNewSize} pass their inputs straight through — this isolates the tests from divider-width
 * bookkeeping so the numbers reflect only the redistribution logic under test. Sizes are chosen, wherever
 * possible, to already be whole pixels so that {@code snapPositions}'s largest-remainder rounding is a verified
 * no-op rather than an extra source of expected-value uncertainty.
 * <p>
 * A JavaFX toolkit is required for {@link Region#resize(double, double)} and {@link SplitPane#getItems()} to
 * behave correctly even without a live {@code Scene}; {@link #initJavaFxToolkit()} starts it once per JVM.
 *
 * @author Pavel Castornii
 */
class DockSplitPaneTest {

    private static final Offset<Double> EXACT = offset(1e-9);

    private static final Offset<Double> LOOSE = offset(1e-4);

    private static final double ONE_THIRD = 1.0 / 3.0;

    @BeforeAll
    static void initJavaFxToolkit() {
        try {
            System.setProperty("glass.platform", "Headless");
            Platform.startup(() -> { });
        } catch (IllegalStateException alreadyStarted) {
            // toolkit already running in this JVM (e.g. started by another test class); nothing to do
        }
    }

    private static Region newChild() {
        return new Region();
    }

    /**
     * Inserts {@code count} freshly-created children into {@code pane}, live and in canonical order — the
     * minimum setup every {@code updateDividersOn*} test needs so {@code SplitPane} has the {@code Divider}
     * objects its assertions depend on.
     */
    private static void populate(DockSplitPane pane, int count) {
        for (int i = 0; i < count; i++) {
            pane.insertNew(i, newChild());
        }
    }

    private DockSplitPane pane;

    @BeforeEach
    void createPane() {
        pane = new DockSplitPane("test");
        pane.setOrientation(Orientation.HORIZONTAL);
    }

    @Test
    void insertNew_intoEmptyPane_addsToLiveAndLogicalItems() {
        Node a = newChild();
        Node b = newChild();

        pane.insertNew(0, a);
        pane.insertNew(1, b);

        assertThat(pane.getItems()).containsExactly(a, b);
        assertThat(pane.getLogicalItems()).containsExactly(a, b);
    }

    @Test
    void insertNew_atMiddleLiveIndex_insertsAtSameLogicalPosition() {
        Node a = newChild();
        Node b = newChild();
        Node c = newChild();
        pane.insertNew(0, a);
        pane.insertNew(1, b);

        pane.insertNew(1, c);

        assertThat(pane.getItems()).containsExactly(a, c, b);
        assertThat(pane.getLogicalItems()).containsExactly(a, c, b);
    }

    @Test
    void replace_existingDividerPositions_preservesThemAcrossItemsSet() {
        Node a = newChild();
        Node b = newChild();
        Node c = newChild();
        pane.insertNew(0, a);
        pane.insertNew(1, b);
        pane.insertNew(2, c);
        pane.setDividerPositions(0.3, 0.7);
        Node d = newChild();

        pane.replace(1, d);

        assertThat(pane.getItems()).containsExactly(a, d, c);
        assertThat(pane.getLogicalItems()).containsExactly(a, d, c);
        assertThat(pane.getDividerPositions()).containsExactly(new double[] {0.3, 0.7}, EXACT);
    }

    @Test
    void removePermanently_logicalChild_removesFromBothLiveAndLogicalItems() {
        Node a = newChild();
        Node b = newChild();
        pane.insertNew(0, a);
        pane.insertNew(1, b);

        pane.removePermanently(a);

        assertThat(pane.getItems()).containsExactly(b);
        assertThat(pane.getLogicalItems()).containsExactly(b);
    }

    @Test
    void minimize_logicalChild_removesFromLiveButKeepsInLogicalItems() {
        Node a = newChild();
        Node b = newChild();
        pane.insertNew(0, a);
        pane.insertNew(1, b);

        pane.minimize(a);

        assertThat(pane.getItems()).containsExactly(b);
        assertThat(pane.getLogicalItems()).containsExactly(a, b);
        assertThat(pane.isLive(a)).isFalse();
    }

    @Test
    void minimize_nodeNotLogicalChild_throwsIllegalArgumentException() {
        Node stranger = newChild();

        assertThatIllegalArgumentException().isThrownBy(() -> pane.minimize(stranger));
    }

    @Test
    void restore_previouslyMinimizedChild_reinsertsAtResolvedLiveIndex() {
        Node a = newChild();
        Node b = newChild();
        Node c = newChild();
        pane.insertNew(0, a);
        pane.insertNew(1, b);
        pane.insertNew(2, c);
        pane.minimize(b);

        pane.restore(b);

        assertThat(pane.getItems()).containsExactly(a, b, c);
        assertThat(pane.getLogicalItems()).containsExactly(a, b, c);
    }

    @Test
    void restore_nodeNotLogicalChild_throwsIllegalArgumentException() {
        Node stranger = newChild();

        assertThatIllegalArgumentException().isThrownBy(() -> pane.restore(stranger));
    }

    @Test
    void restore_nodeAlreadyLive_throwsIllegalStateException() {
        Node a = newChild();
        pane.insertNew(0, a);

        assertThatIllegalStateException().isThrownBy(() -> pane.restore(a));
    }

    @Test
    void resolveLiveInsertIndex_multipleMinimizedSiblingsBefore_skipsThemInLiveCount() {
        Node a = newChild();
        Node b = newChild();
        Node c = newChild();
        Node d = newChild();
        pane.insertNew(0, a);
        pane.insertNew(1, b);
        pane.insertNew(2, c);
        pane.insertNew(3, d);
        pane.minimize(b);
        pane.minimize(c);

        assertThat(pane.resolveLiveInsertIndex(b)).isEqualTo(1);
        assertThat(pane.resolveLiveInsertIndex(c)).isEqualTo(1);
        assertThat(pane.resolveLiveInsertIndex(d)).isEqualTo(1);
    }

    @Test
    void shouldBeNormalized_zeroOrOneLogicalItems_returnsTrue() {
        assertThat(pane.shouldBeNormalized()).isTrue();

        pane.insertNew(0, newChild());

        assertThat(pane.shouldBeNormalized()).isTrue();
    }

    @Test
    void shouldBeNormalized_twoOrMoreLogicalItems_returnsFalse() {
        pane.insertNew(0, newChild());
        pane.insertNew(1, newChild());

        assertThat(pane.shouldBeNormalized()).isFalse();
    }

    @Test
    void updateDividersOnHalfSplit_noPriorDividers_splitsAtMidpoint() {
        populate(pane, 2);

        pane.updateDividersOnHalfSplit(0, new double[0]);

        assertThat(pane.getDividerPositions()).containsExactly(new double[] {0.5}, EXACT);
    }

    @Test
    void updateDividersOnHalfSplit_splittingFirstSegment_insertsAtItsMidpoint() {
        populate(pane, 3);

        pane.updateDividersOnHalfSplit(0, new double[] {0.4});

        assertThat(pane.getDividerPositions()).containsExactly(new double[] {0.2, 0.4}, EXACT);
    }

    @Test
    void updateDividersOnHalfSplit_splittingLastSegment_insertsAtItsMidpoint() {
        populate(pane, 4);

        pane.updateDividersOnHalfSplit(2, new double[] {0.3, 0.6});

        assertThat(pane.getDividerPositions()).containsExactly(new double[] {0.3, 0.6, 0.8}, EXACT);
    }

    @Test
    void updateDividersOnThirdSplit_sideLeft_givesOneThirdToNewElement() {
        populate(pane, 2);

        pane.updateDividersOnThirdSplit(0, new double[0], Side.LEFT);

        assertThat(pane.getDividerPositions()).containsExactly(new double[] {ONE_THIRD}, LOOSE);
    }

    @Test
    void updateDividersOnThirdSplit_sideRight_givesTwoThirdsToExistingElement() {
        populate(pane, 2);

        pane.updateDividersOnThirdSplit(0, new double[0], Side.RIGHT);

        assertThat(pane.getDividerPositions()).containsExactly(new double[] {1 - ONE_THIRD}, LOOSE);
    }

    @Test
    void updateDividersOnInsertBetween_equalSiblings_takesEqualProportionalShareFromEach() {
        populate(pane, 3);

        pane.updateDividersOnInsertBetween(1, 0.5, 0.5, new double[] {0.5});

        assertThat(pane.getDividerPositions()).containsExactly(new double[] {ONE_THIRD, 1 - ONE_THIRD}, LOOSE);
    }

    @Test
    void updateDividersOnUnwrap_singleGrandchild_leavesParentPositionsUnchanged() {
        populate(pane, 3);

        pane.updateDividersOnUnwrap(1, new double[] {0.3, 0.7}, new double[0]);

        assertThat(pane.getDividerPositions()).containsExactly(new double[] {0.3, 0.7}, EXACT);
    }

    @Test
    void updateDividersOnUnwrap_multipleGrandchildren_mapsChildPositionsIntoParentRange() {
        populate(pane, 3);

        pane.updateDividersOnUnwrap(1, new double[] {0.5}, new double[] {0.5});

        assertThat(pane.getDividerPositions()).containsExactly(new double[] {0.5, 0.75}, EXACT);
    }

    @Test
    void updateDividersOnAddWithMain_emptyPane_newChildFirst_splitsProportionallyToRequestedSize() {
        populate(pane, 2);
        pane.resize(500, 600);

        pane.updateDividersOnAddWithMain(0, new double[0], 0, 0, 0, 200, Set.of());

        assertThat(pane.getDividerPositions()).containsExactly(new double[] {0.4}, EXACT);
    }

    @Test
    void updateDividersOnAddWithMain_emptyPane_newChildLast_splitsProportionallyToRequestedSize() {
        populate(pane, 2);
        pane.resize(500, 600);

        pane.updateDividersOnAddWithMain(0, new double[0], 0, 0, 1, 200, Set.of());

        assertThat(pane.getDividerPositions()).containsExactly(new double[] {0.6}, EXACT);
    }

    @Test
    void updateDividersOnAddWithMain_flexibleMainAbsorbsResizeDelta_donorGivesRequestedSize() {
        populate(pane, 3);
        pane.resize(1200, 600);

        pane.updateDividersOnAddWithMain(1000, new double[] {0.5}, 0, 0, 2, 100, Set.of(1));

        assertThat(pane.getDividerPositions()).containsExactly(new double[] {700.0 / 1200, 1100.0 / 1200}, EXACT);
    }

    @Test
    void updateDividersOnAddWithoutMain_emptyPane_newChildFirst_splitsProportionallyToRequestedSize() {
        populate(pane, 2);
        pane.resize(500, 600);

        pane.updateDividersOnAddWithoutMain(0, new double[0], 0, 0, 200, Set.of());

        assertThat(pane.getDividerPositions()).containsExactly(new double[] {0.4}, EXACT);
    }

    @Test
    void updateDividersOnAddWithoutMain_emptyPane_newChildLast_splitsProportionallyToRequestedSize() {
        populate(pane, 2);
        pane.resize(500, 600);

        pane.updateDividersOnAddWithoutMain(0, new double[0], 0, 1, 200, Set.of());

        assertThat(pane.getDividerPositions()).containsExactly(new double[] {0.6}, EXACT);
    }

    @Test
    void updateDividersOnAddWithoutMain_singleDonor_takesFullRequestedSizeFromIt() {
        populate(pane, 3);
        pane.resize(1000, 600);

        pane.updateDividersOnAddWithoutMain(1000, new double[] {0.5}, 0, 1, 200, Set.of(1));

        assertThat(pane.getDividerPositions()).containsExactly(new double[] {0.5, 0.7}, EXACT);
    }

    @Test
    void updateDividersOnRemoveWithMain_singleReceiver_getsFullFreedSpace() {
        populate(pane, 2);
        pane.resize(2000, 600);

        pane.updateDividersOnRemoveWithMain(2000, new double[] {0.5, 0.75}, 0, 0, 2, Set.of(1));

        assertThat(pane.getDividerPositions()).containsExactly(new double[] {0.5}, EXACT);
    }

    @Test
    void updateDividersOnRemoveWithMain_lastRemainingItemRemoved_clearsDividerPositions() {
        pane.updateDividersOnRemoveWithMain(500, new double[0], 0, 0, 0, Set.of());

        assertThat(pane.getDividerPositions()).isEmpty();
    }

    @Test
    void updateDividersOnRemoveWithoutMain_singleReceiver_getsFullFreedSpaceAsymmetrically() {
        populate(pane, 2);
        pane.resize(1980, 600);

        pane.updateDividersOnRemoveWithoutMain(2000, new double[] {0.45, 0.55}, 0, 1, Set.of(2));

        assertThat(pane.getDividerPositions()).containsExactly(new double[] {0.45}, EXACT);
    }

    @Test
    void updateDividersOnRemoveWithoutMain_resizeDeltaSpreadAcrossFullSetBeforeRemoval_keepsSiblingsSymmetric() {
        populate(pane, 2);
        pane.resize(1980, 600);

        pane.updateDividersOnRemoveWithoutMain(2000, new double[] {0.45, 0.55}, 0, 1, Set.of(0, 2));

        assertThat(pane.getDividerPositions()).containsExactly(new double[] {0.5}, EXACT);
    }

    @Test
    void updateDividersOnRemoveWithoutMain_noLiveItemsRemain_doesNothing() {
        assertThatCode(() -> pane.updateDividersOnRemoveWithoutMain(500, new double[] {0.5}, 0, 0, Set.of(0)))
                .doesNotThrowAnyException();
    }

    @Test
    void computeDividerSize_noDividers_returnsNegativeOne() {
        pane.resize(1000, 600);

        assertThat(pane.computeDividerSize()).isEqualTo(-1);
    }

    @Test
    void computeDividerSize_paneNotYetSized_returnsNegativeOne() {
        pane.insertNew(0, newChild());
        pane.insertNew(1, newChild());

        assertThat(pane.computeDividerSize()).isEqualTo(-1);
    }

    @Test
    void computeDividerSize_measuredFromLiveItemBounds_returnsRemainingSpacePerDivider() {
        Region a = newChild();
        a.resize(500, 600);
        Region b = newChild();
        b.resize(500, 600);
        pane.insertNew(0, a);
        pane.insertNew(1, b);
        pane.setDividerPositions(0.5);
        pane.resize(1010, 600);

        assertThat(pane.computeDividerSize()).isCloseTo(10.0, LOOSE);
    }
}
