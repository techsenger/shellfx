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

package com.techsenger.tabshell.web.toolbar;

import com.techsenger.patternfx.mvp.Descriptor;
import com.techsenger.tabshell.core.area.AbstractAreaPresenter;
import com.techsenger.tabshell.core.area.AreaComposer;
import com.techsenger.tabshell.web.WebComponents;

/**
 *
 * @author Pavel Castornii
 */
public class ToolBarPresenter<V extends ToolBarView, C extends AreaComposer>
        extends AbstractAreaPresenter<V, C> {

    protected class Port extends AbstractAreaPresenter.Port implements ToolBarPort {

        private final ToolBarPresenter<V, C> presenter = ToolBarPresenter.this;

        @Override
        public void setBackDisable(boolean value) {
            getView().setBackDisabled(value);
        }

        @Override
        public void setForwardDisable(boolean value) {
            getView().setForwardDisabled(value);
        }

        @Override
        public void setReloadDisable(boolean value) {
            getView().setReloadDisabled(value);
        }

        @Override
        public void setUrl(String value) {
            getView().setUrl(value);
        }

        @Override
        public void setListener(ToolBarListener listener) {
            presenter.listener = listener;
        }
    }

    private ToolBarListener listener;

    public ToolBarPresenter(V view) {
        super(view);
    }

    @Override
    public ToolBarPort getPort() {
        return (ToolBarPort) super.getPort();
    }

    @Override
    protected Descriptor createDescriptor() {
        return new Descriptor(WebComponents.WEB_TOOL_BAR);
    }

    protected void onBack() {
        listener.onNavigateBack();
    }

    protected void onForward() {
        listener.onNavigateForward();
    }

    protected void onReload() {
        listener.onReload();
    }

    protected void onLoad(String url) {
        listener.onLoad(url);
    }

    @Override
    protected Port createPort() {
        return new ToolBarPresenter.Port();
    }
}
