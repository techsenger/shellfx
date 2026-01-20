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

package com.techsenger.tabshell.layout.dock;

import com.techsenger.patternfx.mvp.Descriptor;
import com.techsenger.tabshell.core.area.AbstractAreaPresenter;
import com.techsenger.tabshell.core.area.AreaComposer;
import com.techsenger.tabshell.layout.LayoutComponentNames;
import java.util.List;
import javafx.geometry.Orientation;

/**
 *
 * @author Pavel Castornii
 */
public class SplitSpacePresenter<V extends SplitSpaceView, C extends AreaComposer>
        extends AbstractAreaPresenter<V, C> {

    protected class Port extends AbstractAreaPresenter.Port implements SplitSpacePort {

        private final SplitSpacePresenter<?, ?> presenter = SplitSpacePresenter.this;

        @Override
        public Orientation getOrientation() {
            return presenter.getView().getOrientation();
        }

        @Override
        public List<Double> getDividerPositions() {
            return presenter.getView().getDividerPositions();
        }

        @Override
        public void setDividerPositions(List<Double> positions) {
            presenter.getView().setDividerPositions(positions);
        }

    }

    public SplitSpacePresenter(V view) {
        super(view);
    }

    @Override
    public Port getPort() {
        return (Port) super.getPort();
    }

    @Override
    protected Port createPort() {
        return new SplitSpacePresenter.Port();
    }

    @Override
    protected Descriptor createDescriptor() {
        return new Descriptor(LayoutComponentNames.SPLIT_SPACE);
    }
}
