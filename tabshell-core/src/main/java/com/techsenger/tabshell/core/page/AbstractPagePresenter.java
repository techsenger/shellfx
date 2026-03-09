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
import com.techsenger.tabshell.material.icon.Icon;
import java.util.Collections;
import java.util.List;

/**
 *
 * @author Pavel Castornii
 */
public abstract class AbstractPagePresenter<V extends PageView, C extends PageComposer>
        extends AbstractAreaPresenter<V, C> implements PagePresenter<V, C> {

    private boolean selected;

    private List<PageBreadcrumb> breadcrumbs;

    private Icon<?> icon;

    public AbstractPagePresenter(V view) {
        super(view);
    }

    @Override
    public boolean isSelected() {
        return selected;
    }

    @Override
    public Icon<?> getIcon() {
        return icon;
    }

    @Override
    public void setBreadcrumbs(List<PageBreadcrumb> breadcrumbs) {
        this.breadcrumbs = breadcrumbs;
        getView().setBreadcrumbs(breadcrumbs);
    }

    @Override
    public List<PageBreadcrumb> getBreadcrumbs() {
        return Collections.unmodifiableList(breadcrumbs);
    }

    @Override
    public void onSelected(boolean selected) {
        this.selected = selected;
        getView().requestFocus();
    }

    @Override
    public void setIcon(Icon<?> icon) {
        this.icon = icon;
        getView().setIcon(icon);
    }

    @Override
    protected PageHistory getHistory() {
        return (PageHistory) super.getHistory();
    }
}
