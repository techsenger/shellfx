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

package com.techsenger.tabshell.layout.dockhost;

import com.techsenger.patternfx.mvp.ComponentDescriptor;
import com.techsenger.tabshell.core.area.AbstractAreaPresenter;
import com.techsenger.tabshell.core.area.AreaParams;
import com.techsenger.tabshell.layout.LayoutComponents;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import javafx.geometry.Orientation;

/**
 *
 * @author Pavel Castornii
 */
public class SplitSpacePresenter<V extends SplitSpaceView> extends AbstractAreaPresenter<V> implements SplitSpacePort {

    private Orientation orientation = Orientation.HORIZONTAL;

    private double[] dividerPositions = new double[0];

    public SplitSpacePresenter(V view, AreaParams params) {
        super(view, params);
    }

    @Override
    public Orientation getOrientation() {
        return this.orientation;
    }

    @Override
    public void setOrientation(Orientation orientation) {
        if (Objects.equals(this.orientation, orientation)) {
            return;
        }
        Objects.requireNonNull(orientation, "Orientation can't be null");
        this.orientation = orientation;
        getView().setOrientation(orientation);
    }

    @Override
    public List<Double> getDividerPositions() {
        return Arrays.stream(dividerPositions).boxed().toList();
    }

    @Override
    public void setDividerPositions(List<Double> positions) {
        getView().setDividerPositions(positions);
    }

    @Override
    protected ComponentDescriptor createDescriptor() {
        return new ComponentDescriptor(LayoutComponents.SPLIT_SPACE);
    }

    protected void onDividerPositionsChanged(double[] pos) {
        this.dividerPositions = pos;
    }
}
