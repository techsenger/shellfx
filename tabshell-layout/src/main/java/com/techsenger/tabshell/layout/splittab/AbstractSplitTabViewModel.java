/*
 * Copyright 2024-2025 Pavel Castornii.
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

package com.techsenger.tabshell.layout.splittab;

import com.techsenger.tabshell.core.tab.AbstractShellTabViewModel;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.geometry.Insets;

/**
 *
 * @author Pavel Castornii
 */
public abstract class AbstractSplitTabViewModel<T extends SplitTabMediator> extends AbstractShellTabViewModel<T> {

    class Divider {

        private final BooleanProperty paneVisible = new SimpleBooleanProperty(false);

        /**
         * Real current position.
         */
        private final DoubleProperty position = new SimpleDoubleProperty();

        /**
        * The size of the divider on the screen is defined by its padding.
        */
        private final ObjectProperty<Insets> padding = new SimpleObjectProperty<>();

        private Insets visiblePadding;

        private double closedPosition;

        private double historyPosition;

        Divider(double closedPosition) {
            this.closedPosition = closedPosition;
            position.addListener((ov, oldV, newV) -> {
                if (paneVisible.get()) {
                    //saving to settings
                    historyPosition = newV.doubleValue();
                }
            });
            paneVisible.addListener((ov, oldV, newV) -> {
                if (newV) {
                    position.set(historyPosition);
                    this.padding.set(visiblePadding);
                } else {
                    position.set(this.closedPosition);
                    this.padding.set(Insets.EMPTY);
                }
            });
        }

        ObjectProperty<Insets> paddingProperty() {
            return this.padding;
        }

        Insets getPadding() {
            return this.padding.get();
        }

        void setPadding(Insets padding) {
            this.padding.set(padding);
        }

        DoubleProperty positionProperty() {
            return this.position;
        }

        double getPosition() {
            return this.position.get();
        }

        void setPosition(double value) {
            this.position.set(value);
        }

        void setHistoryPosition(double historyPostion) {
            this.historyPosition = historyPostion;
        }

        double getHistoryPosition() {
            return historyPosition;
        }

        /**
         * This method is called after restoring the history.
         */
        private void init() {
            this.visiblePadding = this.padding.get();
            this.padding.set(Insets.EMPTY);
            //Only when divider has been hidden, it is necessary to set close position - divider has its own size.
            if (paneVisible.get()) {
                this.position.set(historyPosition);
            } else {
                this.position.set(closedPosition);
            }
        }
    }

    private final Divider leftDivider;

    private final Divider rightDivider;

    private final Divider bottomDivider;

    public AbstractSplitTabViewModel() {
        this.leftDivider = new Divider(0);
        this.leftDivider.setHistoryPosition(0.25);
        this.rightDivider = new Divider(1.0);
        this.rightDivider.setHistoryPosition(0.75);
        this.bottomDivider = new Divider(1.0);
        this.bottomDivider.setHistoryPosition(0.75);
    }

    public BooleanProperty leftPaneVisibleProperty() {
        return this.leftDivider.paneVisible;
    }

    public boolean isLeftPaneVisible() {
        return leftDivider.paneVisible.get();
    }

    public void setLeftPaneVisible(boolean visible) {
        this.leftDivider.paneVisible.set(visible);
    }

    public BooleanProperty rightPaneVisibleProperty() {
        return this.rightDivider.paneVisible;
    }

    public boolean isRightPaneVisible() {
        return this.rightDivider.paneVisible.get();
    }

    public void setRightPaneVisible(boolean visible) {
        this.rightDivider.paneVisible.set(visible);
    }

    public BooleanProperty bottomPaneVisibleProperty() {
        return bottomDivider.paneVisible;
    }

    public boolean isBottomPaneVisible() {
        return bottomDivider.paneVisible.get();
    }

    public void setBottomPaneVisible(boolean visible) {
        bottomDivider.paneVisible.set(visible);
    }

    Divider getLeftDivider() {
        return leftDivider;
    }

    Divider getRightDivider() {
        return rightDivider;
    }

    Divider getBottomDivider() {
        return bottomDivider;
    }

    void initDividers() {
        this.leftDivider.init();
        this.rightDivider.init();
        this.bottomDivider.init();
    }
}
