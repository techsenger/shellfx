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

package com.techsenger.tabshell.core.dialog;

import com.techsenger.tabshell.core.pane.AbstractPaneViewModel;
import com.techsenger.tabshell.material.icon.Icon;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.beans.property.ReadOnlyDoubleWrapper;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.layout.VBox;

/**
 *
 * @author Pavel Castornii
 */
public abstract class AbstractDialogViewModel extends AbstractPaneViewModel implements DialogViewModel {

    private final DialogScope scope;

    private final ReadOnlyDoubleWrapper width = new ReadOnlyDoubleWrapper();

    private final ReadOnlyDoubleWrapper height = new ReadOnlyDoubleWrapper();

    private final DoubleProperty prefWidth = new SimpleDoubleProperty(VBox.USE_COMPUTED_SIZE);

    private final DoubleProperty prefHeight = new SimpleDoubleProperty(VBox.USE_COMPUTED_SIZE);

    private final DoubleProperty minWidth = new SimpleDoubleProperty();

    private final DoubleProperty minHeight = new SimpleDoubleProperty();

    private final DoubleProperty maxWidth = new SimpleDoubleProperty();

    private final DoubleProperty maxHeight = new SimpleDoubleProperty();

    private final BooleanProperty resizable = new SimpleBooleanProperty();

    private final StringProperty title = new SimpleStringProperty();

    private final ObjectProperty<Icon<?>> icon = new SimpleObjectProperty<>();

    private final BooleanProperty waiting = new SimpleBooleanProperty(false);

    /**
     * If it is necessary to close a dialog then dialog helper should be used. Default implementation uses window
     * closer set from view.
     */
    private final ObjectProperty<Runnable> closeAction =
            new SimpleObjectProperty<>(() -> close());

    private Runnable windowCloser;

    /**
     * If it is true user can move dialog only with minimum top constrain. If this value is false user
     * can only move the dialog within the bounds of the parent Pane.
     */
    private boolean outOfBoundsAllowed = true;

    public AbstractDialogViewModel(DialogScope scope, boolean resizable) {
        super();
        this.scope = scope;
        this.resizable.set(resizable);
    }

    @Override
    public DialogScope getScope() {
        return scope;
    }

    public ReadOnlyDoubleProperty widthProperty() {
        return width.getReadOnlyProperty();
    }

    public ReadOnlyDoubleProperty heightProperty() {
        return height.getReadOnlyProperty();
    }

    public DoubleProperty prefWidthProperty() {
        return prefWidth;
    }

    public double getPrefWidth() {
        return this.prefWidth.get();
    }

    public void setPrefWidth(double value) {
        this.prefWidth.set(value);
    }

    public DoubleProperty prefHeightProperty() {
        return prefHeight;
    }

    public double getPrefHeight() {
        return this.prefHeight.get();
    }

    public void setPrefHeight(double value) {
        this.prefHeight.set(value);
    }

    public DoubleProperty minWidthProperty() {
        return minWidth;
    }

    public double getMinWidth() {
        return this.minWidth.get();
    }

    public void setMinWidth(double value) {
        this.minWidth.set(value);
    }

    public DoubleProperty minHeightProperty() {
        return minHeight;
    }

    public double getMinHeight() {
        return this.minHeight.get();
    }

    public void setMinHeight(double value) {
        this.minHeight.set(value);
    }

    public DoubleProperty maxWidthProperty() {
        return maxWidth;
    }

    public double getMaxWidth() {
        return this.maxWidth.get();
    }

    public void setMaxWidth(double value) {
        this.maxWidth.set(value);
    }

    public DoubleProperty maxHeightProperty() {
        return maxHeight;
    }

    public double getMaxHeight() {
        return this.maxHeight.get();
    }

    public void setMaxHeight(double value) {
        this.maxHeight.set(value);
    }

    public BooleanProperty resizableProperty() {
        return resizable;
    }

    public boolean isResizable() {
        return this.resizable.get();
    }

    public void setResizable(boolean value) {
        this.resizable.set(value);
    }

    @Override
    public StringProperty titleProperty() {
        return title;
    }

    @Override
    public String getTitle() {
        return this.title.get();
    }

    @Override
    public void setTitle(String value) {
        this.title.set(value);
    }

    @Override
    public ObjectProperty<Icon<?>> iconProperty() {
        return icon;
    }

    @Override
    public Icon<?> getIcon() {
        return this.icon.get();
    }

    @Override
    public void setIcon(Icon<?> value) {
        this.icon.set(value);
    }

    public ObjectProperty<Runnable> closeActionProperty() {
        return closeAction;
    }

    public boolean isOutOfBoundsAllowed() {
        return outOfBoundsAllowed;
    }

    public void setOutOfBoundsAllowed(boolean outOfBoundsAllowed) {
        this.outOfBoundsAllowed = outOfBoundsAllowed;
    }

    public BooleanProperty waitingProperty() {
        return waiting;
    }

    public void setWaiting(boolean waiting) {
        this.waiting.set(waiting);
    }

    public boolean getWaiting() {
        return waiting.get();
    }

    @Override
    public void close() {
        this.windowCloser.run();
    }

    void setWindowCloser(Runnable closer) {
        this.windowCloser = closer;
    }

    ReadOnlyDoubleWrapper widthWrapper() {
        return width;
    }

    ReadOnlyDoubleWrapper heightWrapper() {
        return width;
    }
}
