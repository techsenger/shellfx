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

package com.techsenger.shellfx.layout.tabhost;

import com.techsenger.tabpanepro.core.skin.TabPaneProSkin;
import java.util.List;
import java.util.function.BiFunction;
import javafx.geometry.Insets;
import javafx.geometry.Side;
import javafx.scene.shape.LineTo;
import javafx.scene.shape.MoveTo;
import javafx.scene.shape.Path;
import javafx.scene.shape.Polygon;

/**
 *
 * @author Pavel Castornii
 */
class SlantedTabHeaderSkin extends TabPaneProSkin.TabHeaderSkin {

    private static final Insets extraPadding = new Insets(2, 13, 0, 4);

    private static final BiFunction<Side, Double, Double> dropOffsetResolver = (side, gap) -> {
        var offset = gap / 2;
        if (side == Side.BOTTOM || side == Side.LEFT) {
            offset *= -1;
        }
        return offset;
    };

    public static BiFunction<Side, Double, Double> getDropOffsetResolver() {
        return dropOffsetResolver;
    }

    private final Path borderPath = new Path();

    private final Path selectedPath = new Path();

    private final Polygon clipPolygon = new Polygon();

    SlantedTabHeaderSkin(TabPaneProSkin.TabHeaderContext context) {
        super(context);
        borderPath.getStyleClass().add("border-path");
        borderPath.setManaged(false);
        selectedPath.getStyleClass().add("selected-path");
        selectedPath.setManaged(false);

        setClip(clipPolygon);
        getChildren().addAll(0, List.of(borderPath, selectedPath));
    }

    @Override
    protected double computePrefWidth(double height) {
        var width = super.computePrefWidth(height) + 28;
        return width;
    }

    @Override
    protected void layoutChildren() {
        super.layoutChildren(extraPadding);

        double widthBottom = snapSizeX(getWidth());
        double widthTop = snapSizeX(widthBottom - 28);
        double height = snapSizeY(getHeight());
        Double inset = snapSizeX((widthBottom - widthTop) / 2);

        Double[] points = {inset, 5.0, inset + widthTop, 5.0, widthBottom, height, 0.0, height};
        clipPolygon.getPoints().clear();
        clipPolygon.getPoints().addAll(points);

        borderPath.getElements().clear();
        borderPath.getElements().addAll(
            new MoveTo(inset, 5.0),
            new LineTo(inset + widthTop, 5.0),
            new LineTo(widthBottom, height),
            new MoveTo(0.0, height),
            new LineTo(inset, 5.0)
        );
        selectedPath.getElements().clear();
        if (getContext().getTab().isSelected()) {
            selectedPath.getElements().addAll(new MoveTo(inset, 6.0), new LineTo(inset + widthTop, 6.0));
        }
    }
}
