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

import com.techsenger.patternfx.mvp.ComponentDescriptor;
import com.techsenger.shellfx.core.area.AbstractAreaPresenter;
import com.techsenger.shellfx.layout.LayoutComponents;
import javafx.geometry.Side;
import static javafx.geometry.Side.BOTTOM;
import static javafx.geometry.Side.LEFT;
import static javafx.geometry.Side.RIGHT;

/**
 *
 * @author Pavel Castornii
 */
public class TabPopupPresenter<V extends TabPopupView> extends AbstractAreaPresenter<V> implements TabPopupPort {

    private static final double DEFAULT_SIZE = 250.0;

    private boolean closing;

    private final Side side;

    private double centerWidth;

    private double centerHeight;

    public TabPopupPresenter(V view, TabPopupParams params) {
        super(view, params);
        this.side = params.getSide();
        this.centerWidth = params.getCenterWidth();
        this.centerHeight = params.getCenterHeight();
    }

    @Override
    public Side getSide() {
        return side;
    }

    @Override
    public ViewAccess getViewAccess() {
        return getView();
    }

    @Override
    public void onCenterWidthChanged(double width) {
        this.centerWidth = width;
        setWidth(validateWidth(getWidth()));
    }

    @Override
    public void onCenterHeightChanged(double height) {
        this.centerHeight = height;
        setHeight(validateHeight(getHeight()));
    }

    @Override
    protected TabPopupHistory getHistory() {
        return (TabPopupHistory) super.getHistory();
    }

    @Override
    protected void applyAppearance() {
        super.applyAppearance();
        setWidth(validateWidth(DEFAULT_SIZE));
        setHeight(validateHeight(DEFAULT_SIZE));
    }

    @Override
    protected void restoreAppearance() {
        super.restoreAppearance();
        var h = getHistory();
        setWidth(validateWidth(h.getWidth()));
        setHeight(validateHeight(h.getHeight()));
    }

    @Override
    protected void saveAppearance() {
        super.saveAppearance();
        // If the user moves the mouse quickly, components may be created
        // and removed even before they have been rendered
        var h = getHistory();
        h.setWidth(validateWidth(getWidth() > 0.1 ? getWidth() : DEFAULT_SIZE));
        h.setHeight(validateHeight(getHeight() > 0.1 ? getHeight() : DEFAULT_SIZE));
    }

    @Override
    protected void setWidth(double width) {
        if (getWidth() == width) {
            return;
        }
        super.setWidth(width);
        getView().setWidth(width);
    }

    @Override
    protected void setHeight(double height) {
        if (getHeight() == height) {
            return;
        }
        super.setHeight(height);
        getView().setHeight(height);
    }

    @Override
    protected ComponentDescriptor createDescriptor() {
        return new ComponentDescriptor(LayoutComponents.TAB_POPUP);
    }

    boolean isClosing() {
        return closing;
    }

    void setClosing(boolean closing) {
        this.closing = closing;
    }

    private double validateWidth(double width) {
        if (side == RIGHT || side == LEFT) {
            return Math.min(centerWidth, width);
        } else {
            return centerWidth;
        }
    }

    private double validateHeight(double height) {
        if (side == BOTTOM) {
            return Math.min(centerHeight, height);
        } else {
            return centerHeight;
        }
    }
}
