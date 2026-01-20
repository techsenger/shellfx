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
import com.techsenger.tabshell.web.WebComponentNames;
import com.techsenger.tabshell.web.area.WebAreaPort;
import java.util.function.Supplier;

/**
 *
 * @author Pavel Castornii
 */
public class WebToolBarPresenter<V extends WebToolBarView, C extends AreaComposer>
        extends AbstractAreaPresenter<V, C> {

    protected class Port extends AbstractAreaPresenter.Port implements WebToolBarPort {

        @Override
        public void setBackDisable(boolean value) {
            getView().setBackDisable(value);
        }

        @Override
        public void setForwardDisable(boolean value) {
            getView().setForwardDisable(value);
        }

        @Override
        public void setReloadDisable(boolean value) {
            getView().setReloadDisable(value);
        }

        @Override
        public void setUrl(String value) {
            getView().setUrl(value);
        }
    }

    private final Supplier<WebAreaPort> area;

    public WebToolBarPresenter(V view, Supplier<WebAreaPort> area) {
        super(view);
        this.area = area;
    }

    @Override
    public WebToolBarPort getPort() {
        return (WebToolBarPort) super.getPort();
    }

    @Override
    protected Descriptor createDescriptor() {
        return new Descriptor(WebComponentNames.WEB_TOOL_BAR);
    }

    protected void handleBackAction() {
        area.get().navigateBack();
    }

    protected void handleForwardAction() {
        area.get().navigateForward();
    }

    protected void handleReloadAction() {
        area.get().reload();
    }

    protected void handleUrlInput(String url) {
        area.get().load(url);
    }

    @Override
    protected Port createPort() {
        return new WebToolBarPresenter.Port();
    }
}
