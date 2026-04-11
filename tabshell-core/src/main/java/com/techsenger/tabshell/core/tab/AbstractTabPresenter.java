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

package com.techsenger.tabshell.core.tab;

import com.techsenger.patternfx.mvp.AbstractChildPresenter;
import com.techsenger.tabshell.core.ShellPort;
import com.techsenger.tabshell.material.icon.Icon;

/**
 *
 * @author Pavel Castornii
 */
public abstract class AbstractTabPresenter<V extends TabView, C extends TabComposer>
        extends AbstractChildPresenter<V, C> implements TabPresenter<V, C> {

    private boolean closable = true;

    private boolean waiting;

    private String tooltip;

    private Icon<?> icon;

    private String title;

    private boolean selected;

    public AbstractTabPresenter(V view) {
        super(view);
    }

    @Override
    public void onSelected(boolean selected) {
        this.selected = selected;
    }

    @Override
    public void close() {
        getComposer().remove();
    }

    @Override
    public boolean isClosable() {
        return closable;
    }

    @Override
    public void setClosable(boolean closable) {
        this.closable = closable;
        getView().setClosable(closable);
    }

    @Override
    public boolean isWaiting() {
        return waiting;
    }

    @Override
    public void setWaiting(boolean waiting) {
        this.waiting = waiting;
        getView().setWaiting(waiting);
    }

    @Override
    public String getTooltip() {
        return tooltip;
    }

    @Override
    public void setTooltip(String tooltip) {
        this.tooltip = tooltip;
        getView().setTooltip(tooltip);
    }

    @Override
    public String getTitle() {
        return this.title;
    }

    @Override
    public void setTitle(String title) {
        this.title = title;
        getView().setTitle(title);
    }

    @Override
    public boolean isSelected() {
        return this.selected;
    }

    @Override
    public Icon<?> getIcon() {
        return this.icon;
    }

    @Override
    public void setIcon(Icon<?> icon) {
        this.icon = icon;
        getView().setIcon(icon);
    }

    @Override
    public ShellPort getShell() {
        return getComposer().getShellPort();
    }

    @Override
    protected TabHistory getHistory() {
        return (TabHistory) super.getHistory();
    }
}
