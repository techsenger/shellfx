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

package com.techsenger.tabshell.layout.pagehost;

import com.techsenger.annotations.Unmodifiable;
import com.techsenger.tabshell.core.area.AbstractAreaPresenter;
import com.techsenger.tabshell.core.page.PageItem;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 *
 * @author Pavel Castornii
 */
public abstract class AbstractPageHostPresenter<V extends PageHostViewBase>
        extends AbstractAreaPresenter<V> implements PageHostFindPort {

    private double dividerPosition;

    private boolean forwardDisabled;

    private boolean backDisabled;

    /**
     * When {@link findMode} is true history is disabled. We save items not indexes because dynamic menu will be
     * supported.
     */
    private final List<PageItem> pageHistory = new ArrayList<>();

    private int pageHistoryIndex;

    private boolean findMode = false;

    public AbstractPageHostPresenter(V view) {
        super(view);
    }

    public void setDividerPosition(double pos) {
        this.dividerPosition = pos;
        getView().setDividerPosition(pos);
    }

    public double getDividerPosition() {
        return this.dividerPosition;
    }

    public boolean isForwardDisabled() {
        return forwardDisabled;
    }

    public boolean isBackDisabled() {
        return backDisabled;
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
        return findMode;
    }

    protected void setForwardDisabled(boolean forwardDisabled) {
        this.forwardDisabled = forwardDisabled;
        getView().setForwardDisabled(forwardDisabled);
    }

    protected void setBackDisabled(boolean backDisabled) {
        this.backDisabled = backDisabled;
        getView().setBackDisabled(backDisabled);
    }

    protected void onDividerPositionChanged(double pos) {
        this.dividerPosition = pos;
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
        var currentPage = getView().getComposer().getSelectedPagePort();
        return currentPage != null && Objects.equals(currentPage.getItem(), item);
    }

    @Override
    protected void saveAppearance() {
        super.saveAppearance();
        var history = getHistory();
        history.setDividerPosition(getDividerPosition());
    }

    @Override
    protected void restoreAppearance() {
        super.restoreAppearance();
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
        var history = getHistory();
        if (history == null || history.isNew()) {
            getView().setDividerPosition(0.2);
        }
        setBackDisabled(true);
        setForwardDisabled(true);
    }

    void addPageHistory(PageItem item) {
        if (this.findMode) {
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
        if (findMode) {
            setBackDisabled(true);
            setForwardDisabled(true);
        } else {
            setBackDisabled(this.pageHistoryIndex == 0);
            setForwardDisabled(this.pageHistoryIndex + 1 == this.pageHistory.size());
        }
    }

    void setFindMode(boolean findMode) {
        this.findMode = findMode;
    }

    abstract void navigateHistory(int newIndex);
}
