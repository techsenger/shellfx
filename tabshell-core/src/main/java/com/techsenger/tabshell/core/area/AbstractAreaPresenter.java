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

package com.techsenger.tabshell.core.area;

import com.techsenger.patternfx.mvp.AbstractChildPresenter;

/**
 *
 * @author Pavel Castornii
 */
public abstract class AbstractAreaPresenter<V extends AreaView>
        extends AbstractChildPresenter<V> implements AreaPresenter<V> {

    private double width;

    private double height;

    public AbstractAreaPresenter(V view) {
        super(view);
    }

    @Override
    public double getWidth() {
        return width;
    }

    @Override
    public double getHeight() {
        return height;
    }

    @Override
    protected AreaHistory getHistory() {
        return (AreaHistory) super.getHistory();
    }

    protected void onWidthChanged(double width) {
        this.width = width;
    }

    protected void onHeightChanged(double width) {
        this.width = width;
    }
}
