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

import com.techsenger.shellfx.core.area.AbstractAreaViewModel;
import com.techsenger.toolkit.fx.value.ObservableSource;
import com.techsenger.toolkit.fx.value.SimpleObservableSource;
import javafx.beans.value.ChangeListener;
import javafx.geometry.Side;
import static javafx.geometry.Side.BOTTOM;
import static javafx.geometry.Side.LEFT;
import static javafx.geometry.Side.RIGHT;

/**
 *
 * @author Pavel Castornii
 */
public class TabPopupViewModel<C extends TabPopupComposer> extends AbstractAreaViewModel<C>
        implements FullTabPopupPort {

    private static final double DEFAULT_SIZE = 250.0;

    private final ObservableSource<Double> widthSource = new SimpleObservableSource<>();

    private final ObservableSource<Double> heightSource = new SimpleObservableSource<>();

    private final Side side;

    private final DockHostPort dockHost;

    private boolean closing;

    private final ChangeListener<Number> centerWidthListener =
            (ov, oldV, newV) -> setWidth(validateWidth(getWidth()));

    private final ChangeListener<Number> centerHeightListener =
            (ov, oldV, newV) -> setHeight(validateHeight(getHeight()));

    public TabPopupViewModel(TabPopupParams params) {
        super(params);
        this.side = params.getSide();
        this.dockHost = params.getDockHost();
    }

    @Override
    public Side getSide() {
        return side;
    }

    @Override
    public TabPopupPort.ComposerAccess getComposerAccess() {
        return getComposer();
    }

    @Override
    protected TabPopupHistory getHistory() {
        return (TabPopupHistory) super.getHistory();
    }

    @Override
    protected void applyPersistentState() {
        super.applyPersistentState();
        setWidth(validateWidth(DEFAULT_SIZE));
        setHeight(validateHeight(DEFAULT_SIZE));
    }

    @Override
    protected void restorePersistentState() {
        super.restorePersistentState();
        var h = getHistory();
        setWidth(validateWidth(h.getWidth()));
        setHeight(validateHeight(h.getHeight()));
    }

    @Override
    protected void savePersistentState() {
        super.savePersistentState();
        // If the user moves the mouse quickly, components may be created
        // and removed even before they have been rendered
        var h = getHistory();
        h.setWidth(validateWidth(getWidth() > 0.1 ? getWidth() : DEFAULT_SIZE));
        h.setHeight(validateHeight(getHeight() > 0.1 ? getHeight() : DEFAULT_SIZE));
    }

    @Override
    protected void postInitialize() {
        super.postInitialize();
        dockHost.centerWidthProperty().addListener(centerWidthListener);
        dockHost.centerHeightProperty().addListener(centerHeightListener);
    }

    @Override
    protected void postDeinitialize() {
        super.postDeinitialize();
        dockHost.centerWidthProperty().removeListener(centerWidthListener);
        dockHost.centerHeightProperty().removeListener(centerHeightListener);
    }

    protected void setWidth(double width) {
        widthSource.next(width);
    }

    protected void setHeight(double height) {
        heightSource.next(height);
    }

    boolean isClosing() {
        return closing;
    }

    void setClosing(boolean closing) {
        this.closing = closing;
    }

    ObservableSource<Double> widthSource() {
        return widthSource;
    }

    ObservableSource<Double> heightSource() {
        return heightSource;
    }

    private double validateWidth(double width) {
        if (side == RIGHT || side == LEFT) {
            return Math.min(dockHost.getCenterWidth(), width);
        } else {
            return dockHost.getCenterWidth();
        }
    }

    private double validateHeight(double height) {
        if (side == BOTTOM) {
            return Math.min(dockHost.getCenterHeight(), height);
        } else {
            return dockHost.getCenterHeight();
        }
    }
}
