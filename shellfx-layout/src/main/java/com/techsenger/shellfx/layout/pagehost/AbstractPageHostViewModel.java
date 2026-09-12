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

package com.techsenger.shellfx.layout.pagehost;

import com.techsenger.annotations.Unmodifiable;
import com.techsenger.shellfx.core.area.AbstractAreaViewModel;
import com.techsenger.shellfx.core.area.AreaParams;
import com.techsenger.shellfx.core.page.PageItem;
import com.techsenger.toolkit.fx.value.ObservableSource;
import com.techsenger.toolkit.fx.value.SimpleObservableSource;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyBooleanWrapper;
import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.beans.property.ReadOnlyDoubleWrapper;

/**
 *
 * @author Pavel Castornii
 */
public abstract class AbstractPageHostViewModel<C extends BasePageHostComposer> extends AbstractAreaViewModel<C>
        implements FindPageHostPort {

    private final ReadOnlyDoubleWrapper dividerPosition = new ReadOnlyDoubleWrapper();

    private final ObservableSource<Double> dividerPositionSource = new SimpleObservableSource<>();

    private final ReadOnlyBooleanWrapper forwardDisabled = new ReadOnlyBooleanWrapper();

    private final ReadOnlyBooleanWrapper backDisabled = new ReadOnlyBooleanWrapper();

    /**
     * When {@link #findMode} is true history is disabled. We save items not indexes because dynamic menu will be
     * supported.
     */
    private final List<PageItem> pageHistory = new ArrayList<>();

    private int pageHistoryIndex;

    private final ReadOnlyBooleanWrapper findMode = new ReadOnlyBooleanWrapper();

    public AbstractPageHostViewModel(AreaParams params) {
        super(params);
    }

    public double getDividerPosition() {
        return this.dividerPosition.get();
    }

    /**
     * Sets the position of the divider between this page host's content and its find panel. This is a request,
     * not a guarantee — the underlying {@code SplitPane} may adjust or ignore it; observe
     * {@link #dividerPositionProperty()} for the value actually applied.
     *
     * @param pos the requested divider position, between {@code 0} and {@code 1}
     */
    public void setDividerPosition(double pos) {
        dividerPositionSource.next(pos);
    }

    public ReadOnlyDoubleProperty dividerPositionProperty() {
        return dividerPosition.getReadOnlyProperty();
    }

    public boolean isForwardDisabled() {
        return forwardDisabled.get();
    }

    public ReadOnlyBooleanProperty forwardDisabledProperty() {
        return forwardDisabled.getReadOnlyProperty();
    }

    public boolean isBackDisabled() {
        return backDisabled.get();
    }

    public ReadOnlyBooleanProperty backDisabledProperty() {
        return backDisabled.getReadOnlyProperty();
    }

    /**
     * Returns an unmodifiable list of history entries.
     * @return
     */
    public @Unmodifiable List<? extends PageItem> getPageHistory() {
        return Collections.unmodifiableList(pageHistory);
    }

    public int getPageHistoryIndex() {
        return pageHistoryIndex;
    }

    public boolean isFindMode() {
        return findMode.get();
    }

    public ReadOnlyBooleanProperty findModeProperty() {
        return findMode.getReadOnlyProperty();
    }

    protected void setForwardDisabled(boolean forwardDisabled) {
        this.forwardDisabled.set(forwardDisabled);
    }

    protected void setBackDisabled(boolean backDisabled) {
        this.backDisabled.set(backDisabled);
    }

    protected void onBack() {
        if (this.pageHistoryIndex > 0) {
            var newIndex = pageHistoryIndex - 1;
            navigateHistory(newIndex);
        }
    }

    protected void onForward() {
        if (this.pageHistoryIndex + 1 < this.pageHistory.size()) {
            var newIndex = pageHistoryIndex + 1;
            navigateHistory(newIndex);
        }
    }

    protected boolean isCurrentPage(PageItem item) {
        var currentPage = getComposer().getSelectedPagePort();
        return currentPage != null && Objects.equals(currentPage.getItem(), item);
    }

    @Override
    protected void applyPersistentState() {
        super.applyPersistentState();
        setDividerPosition(0.2);
    }

    @Override
    protected void savePersistentState() {
        super.savePersistentState();
        var history = getHistory();
        history.setDividerPosition(getDividerPosition());
    }

    @Override
    protected void restorePersistentState() {
        super.restorePersistentState();
        var history = getHistory();
        setDividerPosition(history.getDividerPosition());
    }

    @Override
    protected PageHostHistoryBase getHistory() {
        return (PageHostHistoryBase) super.getHistory();
    }

    @Override
    protected void postInitialize() {
        super.postInitialize();
        setBackDisabled(true);
        setForwardDisabled(true);
    }

    /**
     * Framework contract: written directly by the View to report the actual divider position, intended
     * exclusively for {@code AbstractPageHostFxView}. Direct invocation by user code results in undefined
     * behavior.
     */
    ReadOnlyDoubleWrapper dividerPositionWrapper() {
        return dividerPosition;
    }

    /**
     * Framework contract: emits a requested divider position for the View to apply to the underlying widget,
     * intended exclusively for {@code AbstractPageHostFxView}. Direct invocation by user code results in
     * undefined behavior.
     */
    ObservableSource<Double> dividerPositionSource() {
        return dividerPositionSource;
    }

    void addPageHistory(PageItem item) {
        if (this.findMode.get()) {
            return;
        }
        if (!this.pageHistory.isEmpty() && this.pageHistory.getLast().equals(item)) {
            return;
        }
        if (this.pageHistoryIndex + 1 < this.pageHistory.size()) {
            this.pageHistory.subList(this.pageHistoryIndex + 1, this.pageHistory.size()).clear();
        }
        this.pageHistory.add(item);
        setPageHistoryIndex(this.pageHistory.size() - 1);
    }

    void setPageHistoryIndex(int index) {
        this.pageHistoryIndex = index;
    }

    void updateHistoryNavigation() {
        if (findMode.get()) {
            setBackDisabled(true);
            setForwardDisabled(true);
        } else {
            setBackDisabled(this.pageHistoryIndex == 0);
            setForwardDisabled(this.pageHistoryIndex + 1 == this.pageHistory.size());
        }
    }

    void setFindMode(boolean findMode) {
        this.findMode.set(findMode);
    }

    abstract void navigateHistory(int newIndex);
}
