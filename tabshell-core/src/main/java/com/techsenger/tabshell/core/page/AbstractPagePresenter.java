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

package com.techsenger.tabshell.core.page;

import com.techsenger.tabshell.core.area.AbstractAreaPresenter;

/**
 *
 * @author Pavel Castornii
 */
public abstract class AbstractPagePresenter<V extends PageView, C extends PageComposer>
        extends AbstractAreaPresenter<V, C> implements PagePresenter<V, C> {

    private boolean selected;

    private final PageItem item;

    public AbstractPagePresenter(V view, PageItem item) {
        super(view);
        this.item = item;
    }

    @Override
    public boolean isSelected() {
        return selected;
    }

    @Override
    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    @Override
    protected PageHistory getHistory() {
        return (PageHistory) super.getHistory();
    }

    @Override
    public PageItem getItem() {
        return item;
    }
}
