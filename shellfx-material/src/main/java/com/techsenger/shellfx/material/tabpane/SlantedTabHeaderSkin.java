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

package com.techsenger.shellfx.material.tabpane;

import com.techsenger.tabpanepro.core.skin.TabPaneProSkin;
import java.util.List;
import java.util.function.DoubleConsumer;
import javafx.beans.value.ObservableValue;
import javafx.css.CssMetaData;
import javafx.css.Styleable;
import javafx.css.StyleableProperty;
import javafx.css.StyleablePropertyFactory;
import javafx.geometry.Insets;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.LineTo;
import javafx.scene.shape.MoveTo;
import javafx.scene.shape.Path;
import javafx.scene.shape.Polygon;

/**
 * The shape of a slanted tab header. Its geometry is set by CSS: {@code -sfx-slant-left} and
 * {@code -sfx-slant-right} (how far the bottom point of the left or right side is moved outward; a negative value
 * moves it inward), {@code -sfx-top-offset} (the distance from the top of the tab to the top edge of its shape;
 * whatever is above it is clipped) and {@code -sfx-padding} (the padding between the edges of the tab and its
 * content). The slant does not widen the tab, so a larger slant needs a larger padding. The defaults are the values
 * for the font size 14 in pixels.
 * <p>
 * {@code -sfx-tab-gap} is the distance between two neighbouring tabs (negative if they overlap). It belongs to the
 * whole tab header area, so the skin only reports its value to the tab pane, which applies it.
 *
 * @author Pavel Castornii
 */
class SlantedTabHeaderSkin extends TabPaneProSkin.TabHeaderSkin {

    private static final double DEFAULT_SLANT = 14.0;

    private static final double DEFAULT_TOP_OFFSET = 5.0;

    private static final double BOTTOM_OVERLAP = 2.0;

    private static final Insets DEFAULT_PADDING = new Insets(2, 8, 0, 8);

    private static final StyleablePropertyFactory<SlantedTabHeaderSkin> FACTORY =
            new StyleablePropertyFactory<>(StackPane.getClassCssMetaData());

    public static List<CssMetaData<? extends Styleable, ?>> getClassCssMetaData() {
        return FACTORY.getCssMetaData();
    }

    private final StyleableProperty<Number> slantLeft = FACTORY.createStyleableNumberProperty(this, "slantLeft",
            "-sfx-slant-left", s -> s.slantLeft, DEFAULT_SLANT);

    private final StyleableProperty<Number> slantRight = FACTORY.createStyleableNumberProperty(this, "slantRight",
            "-sfx-slant-right", s -> s.slantRight, DEFAULT_SLANT);

    private final StyleableProperty<Number> topOffset = FACTORY.createStyleableNumberProperty(this, "topOffset",
            "-sfx-top-offset", s -> s.topOffset, DEFAULT_TOP_OFFSET);

    private final StyleableProperty<Insets> padding = FACTORY.createStyleableInsetsProperty(this, "padding",
            "-sfx-padding", s -> s.padding, DEFAULT_PADDING);

    private final StyleableProperty<Number> tabGap =
            FACTORY.createStyleableNumberProperty(this, "tabGap", "-sfx-tab-gap", s -> s.tabGap, 0.0);

    private final Path borderPath = new Path();

    private final Path selectedPath = new Path();

    private final Polygon clipPolygon = new Polygon();

    SlantedTabHeaderSkin(TabPaneProSkin.TabHeaderContext context, DoubleConsumer tabGapListener) {
        super(context);
        borderPath.getStyleClass().add("border-path");
        borderPath.setManaged(false);
        selectedPath.getStyleClass().add("selected-path");
        selectedPath.setManaged(false);

        setClip(clipPolygon);
        getChildren().addAll(0, List.of(borderPath, selectedPath));
        ((ObservableValue<?>) slantLeft).addListener((ov, oldV, newV) -> requestLayout());
        ((ObservableValue<?>) slantRight).addListener((ov, oldV, newV) -> requestLayout());
        ((ObservableValue<?>) topOffset).addListener((ov, oldV, newV) -> requestLayout());
        ((ObservableValue<?>) padding).addListener((ov, oldV, newV) -> requestLayout());
        ((ObservableValue<?>) tabGap).addListener((ov, oldV, newV) ->
                tabGapListener.accept(tabGap.getValue().doubleValue()));
    }

    @Override
    public List<CssMetaData<? extends Styleable, ?>> getCssMetaData() {
        return getClassCssMetaData();
    }

    @Override
    protected double computePrefWidth(double height) {
        var extraPadding = padding.getValue();
        // the width is snapped here, because the header area snaps the sum of the widths but the tabs one by one, and
        // the clip of the headers then ends before the last tab does
        return snapSizeX(super.computePrefWidth(height) + extraPadding.getLeft() + extraPadding.getRight());
    }

    @Override
    protected void layoutChildren() {
        super.layoutChildren(padding.getValue());

        double left = slantLeft.getValue().doubleValue();
        double right = slantRight.getValue().doubleValue();
        double topY = topOffset.getValue().doubleValue();
        double width = snapSizeX(getWidth());
        double height = snapSizeY(getHeight());
        // a negative slant moves the top point of the side outward instead of the bottom one
        double topLeftX = snapSizeX(Math.max(0.0, left));
        double bottomLeftX = snapSizeX(Math.max(0.0, -left));
        double topRightX = width - snapSizeX(Math.max(0.0, right));
        double bottomRightX = width - snapSizeX(Math.max(0.0, -right));

        // the sides go a little below the tab, so that they reach the line under the tab headers
        double extraY = BOTTOM_OVERLAP;
        double sideHeight = Math.max(1.0, height - topY);
        double extraLeftX = (bottomLeftX - topLeftX) * extraY / sideHeight;
        double extraRightX = (bottomRightX - topRightX) * extraY / sideHeight;
        double extendedLeftX = bottomLeftX + extraLeftX;
        double extendedRightX = bottomRightX + extraRightX;
        double extendedHeight = height + extraY;

        Double[] points = {topLeftX, topY, topRightX, topY, extendedRightX, extendedHeight,
            extendedLeftX, extendedHeight};
        clipPolygon.getPoints().clear();
        clipPolygon.getPoints().addAll(points);

        borderPath.getElements().clear();
        borderPath.getElements().addAll(
            new MoveTo(topLeftX, topY),
            new LineTo(topRightX, topY),
            new LineTo(extendedRightX, extendedHeight),
            new MoveTo(extendedLeftX, extendedHeight),
            new LineTo(topLeftX, topY)
        );
        selectedPath.getElements().clear();
        if (getContext().getTab().isSelected()) {
            selectedPath.getElements().addAll(new MoveTo(topLeftX, topY + 1.0),
                    new LineTo(topRightX, topY + 1.0));
        }
    }
}
