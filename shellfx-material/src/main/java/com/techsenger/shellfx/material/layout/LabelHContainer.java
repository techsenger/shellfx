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

package com.techsenger.shellfx.material.layout;

import java.util.function.BinaryOperator;
import java.util.function.ToDoubleFunction;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.scene.control.Label;
import javafx.scene.layout.Region;

/**
 * Horizontal container for labels that intelligently truncates labels to maximize information.
 * When not all labels fit, it ensures that the maximum number of labels is visible,
 * with the last visible label always truncated.
 *
 * <p><b>Behavior:</b></p>
 * <ul>
 *   <li><b>All labels fit</b> → all labels are shown fully</li>
 *   <li><b>Not all labels fit</b> → show as many labels as possible, but the last visible label is always truncated.
 *       Only ONE label is truncated - the last visible one. All subsequent labels are hidden.</li>
 * </ul>
 *
 * <p><b>Examples with 3 labels (A, B, C):</b></p>
 * <ul>
 *   <li>Space for 3 labels → A B C (all full)</li>
 *   <li>Space for 2.5 labels → A B C (truncated)</li>
 *   <li>Space for 1.5 labels → A B (truncated), C hidden</li>
 * </ul>
 *
 * <p><b>Limitations:</b></p>
 * <ul>
 *   <li>Labels must have sufficiently wide text. Very short labels (e.g. a single character) may not display the
 *       truncation ellipsis correctly, since forced truncation relies on shrinking the label by
 *       {@value #FORCED_TRUNCATION_PX}px — which may be larger than the label itself.</li>
 * </ul>
 */
public class LabelHContainer extends Region {

    /**
     * The number of pixels by which the last visible label is forcibly shrunk to ensure it always appears
     * truncated when there are hidden labels after it.
     */
    private static final int FORCED_TRUNCATION_PX = 5;

    private final ObservableList<Label> labels = FXCollections.observableArrayList();

    private final DoubleProperty spacing = new SimpleDoubleProperty(0);

    public LabelHContainer() {
        this(0, null);
    }

    public LabelHContainer(double spacing) {
        this(spacing, null);
    }

    public LabelHContainer(Label... labels) {
        this(0, labels);
    }

    public LabelHContainer(double spacing, Label... labels) {
        this.labels.addListener((ListChangeListener<Label>) observable -> {
            getChildren().setAll(this.labels);
            requestLayout();
        });
        this.spacing.addListener(observable -> requestLayout());
        setSpacing(spacing);
        if (labels != null) {
            this.labels.addAll(labels);
        }
    }

    public ObservableList<Label> getLabels() {
        return labels;
    }

    public DoubleProperty spacingProperty() {
        return spacing;
    }

    public final void setSpacing(double spacing) {
        spacingProperty().set(spacing);
    }

    public final double getSpacing() {
        return spacingProperty().get();
    }

    @Override
    protected double computeMinWidth(double height) {
        return computeTotalWidth(label -> label.minWidth(height));
    }

    @Override
    protected double computePrefWidth(double height) {
        return computeTotalWidth(label -> label.prefWidth(height));
    }

    @Override
    protected double computeMaxWidth(double height) {
        return computeTotalWidth(label -> label.maxWidth(height));
    }

    @Override
    protected double computeMinHeight(double width) {
        return computeHeight(label -> label.minHeight(width), Double::max);
    }

    @Override
    protected double computePrefHeight(double width) {
        return computeHeight(label -> label.prefHeight(width), Double::max);
    }

    @Override
    protected double computeMaxHeight(double width) {
        return computeHeight(label -> label.maxHeight(width), Math::min);
    }

    @Override
    protected void layoutChildren() {
        if (labels.isEmpty()) {
            return;
        }

        double startX = snappedLeftInset();
        double y = snappedTopInset();
        double availableHeight = getHeight() - y - snappedBottomInset();
        double availableWidth = getWidth() - startX - snappedRightInset();

        if (availableWidth <= 0) {
            for (Label label : labels) {
                label.setVisible(false);
            }
            return;
        }

        // Reset maxWidth and visibility for ALL labels BEFORE measuring
        for (Label label : labels) {
            label.setMaxWidth(Double.MAX_VALUE);
            label.setVisible(true);
        }

        int labelCount = labels.size();

        // Measure preferred and min widths
        double[] prefWidths = new double[labelCount];
        double[] minWidths = new double[labelCount];
        for (int i = 0; i < labelCount; i++) {
            prefWidths[i] = labels.get(i).prefWidth(availableHeight);
            minWidths[i] = labels.get(i).minWidth(availableHeight);
        }

        // Case 1: all labels fit fully — show all, no truncation needed
        if (allFitFully(prefWidths, availableWidth)) {
            layoutLabels(startX, y, availableHeight, prefWidths, labelCount, -1, 0);
            return;
        }

        // Count how many labels fit fully
        int fullCount = 0;
        double usedWidth = 0;
        for (int i = 0; i < labelCount; i++) {
            double added = prefWidths[i] + (i > 0 ? getSpacing() : 0);
            if (usedWidth + added <= availableWidth) {
                usedWidth += added;
                fullCount++;
            } else {
                break;
            }
        }

        // Try to fit one more label truncated after the full ones
        if (fullCount < labelCount) {
            int truncIdx = fullCount;
            double spacingBefore = (fullCount > 0) ? getSpacing() : 0;
            double remainingForTruncated = availableWidth - usedWidth - spacingBefore;
            double truncatedWidth = Math.min(remainingForTruncated, prefWidths[truncIdx] - FORCED_TRUNCATION_PX);
            truncatedWidth = Math.max(truncatedWidth, minWidths[truncIdx]);

            if (truncatedWidth >= minWidths[truncIdx]) {
                layoutLabels(startX, y, availableHeight, prefWidths,
                        fullCount + 1, truncIdx, truncatedWidth);
                return;
            }
        }

        // Next truncated label doesn't fit — truncate the last full
        if (fullCount > 1) {
            int truncIdx = fullCount - 1;
            double usedWithoutLast = 0;
            for (int i = 0; i < truncIdx; i++) {
                usedWithoutLast += prefWidths[i] + (i > 0 ? getSpacing() : 0);
            }
            double remainingForLast = availableWidth - usedWithoutLast - getSpacing();
            double truncatedWidth = Math.min(remainingForLast, prefWidths[truncIdx] - FORCED_TRUNCATION_PX);
            truncatedWidth = Math.max(truncatedWidth, minWidths[truncIdx]);
            layoutLabels(startX, y, availableHeight, prefWidths,
                    fullCount, truncIdx, truncatedWidth);
        } else if (fullCount == 1) {
            double truncatedWidth = Math.min(availableWidth, prefWidths[0] - FORCED_TRUNCATION_PX);
            truncatedWidth = Math.max(truncatedWidth, minWidths[0]);
            layoutLabels(startX, y, availableHeight, prefWidths,
                    1, 0, truncatedWidth);
        } else {
            for (Label label : labels) {
                label.setVisible(false);
            }
        }
    }

    private double computeTotalWidth(ToDoubleFunction<Label> widthFunction) {
        double labelsWidth = labels.stream()
                .mapToDouble(widthFunction)
                .sum();
        double totalSpacing = labels.isEmpty() ? 0 : (labels.size() - 1) * getSpacing();
        return labelsWidth + totalSpacing + snappedLeftInset() + snappedRightInset();
    }

    private double computeHeight(ToDoubleFunction<Label> heightFunction,
                                 BinaryOperator<Double> reducer) {
        if (labels.isEmpty()) {
            return snappedTopInset() + snappedBottomInset();
        }
        double height = labels.stream()
                .mapToDouble(heightFunction)
                .boxed()
                .reduce(reducer)
                .orElse(0.0);
        return height + snappedTopInset() + snappedBottomInset();
    }

    /**
     * Lays out the first {@code visibleCount} labels and hides the rest.
     *
     * @param truncatedIndex index of the label to truncate, or -1 if no truncation
     * @param truncatedWidth available width for the truncated label (used only when truncatedIndex >= 0)
     */
    private void layoutLabels(double startX, double y, double availableHeight,
                               double[] prefWidths, int visibleCount,
                               int truncatedIndex, double truncatedWidth) {
        double x = startX;
        for (int i = 0; i < visibleCount; i++) {
            Label label = labels.get(i);
            label.setVisible(true);
            double w;
            if (i == truncatedIndex) {
                w = truncatedWidth;
                label.setMaxWidth(w);
            } else {
                w = prefWidths[i];
                label.setMaxWidth(Double.MAX_VALUE);
            }
            double h = Math.min(label.prefHeight(w), availableHeight);
            label.resizeRelocate(x, y, w, h);
            x += w;
            if (i < visibleCount - 1) {
                x += getSpacing();
            }
        }

        // Explicitly hide all labels beyond visibleCount
        for (int i = visibleCount; i < labels.size(); i++) {
            labels.get(i).setVisible(false);
        }
    }

    private boolean allFitFully(double[] prefWidths, double availableWidth) {
        double total = 0;
        for (int i = 0; i < prefWidths.length; i++) {
            total += prefWidths[i];
            if (i < prefWidths.length - 1) {
                total += getSpacing();
            }
        }
        return total <= availableWidth;
    }
}
