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

/**
 *
 * @author Pavel Castornii
 */
public abstract class AbstractPagePresenter<V extends PageView, C extends PageComposer>
        extends AbstractAreaPresenter<V, C> implements PagePresenter<V, C> {

    protected class Port extends AbstractAreaPresenter.Port implements PagePort {

        public Port() {
            // empty
        }

        @Override
        public boolean isSelected() {
            return getView().isSelected();
        }

        @Override
        public String getTitle() {
            return getView().getTitle();
        }

        @Override
        public Icon<?> getIcon() {
            return getView().getIcon();
        }

    }

    public AbstractPagePresenter(V view) {
        super(view);
    }

    @Override
    public void handleSelected(boolean selected) {
        getView().requestFocus();
    }

    @Override
    public PagePort getPort() {
        return (PagePort) super.getPort();
    }

    @Override
    protected PageHistory getHistory() {
        return (PageHistory) super.getHistory();
    }

    @Override
    protected Port createPort() {
        return new AbstractPagePresenter.Port();
    }
}
