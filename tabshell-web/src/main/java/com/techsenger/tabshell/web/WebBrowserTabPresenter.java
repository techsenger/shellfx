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

package com.techsenger.tabshell.web;

import com.techsenger.patternfx.mvp.Descriptor;
import com.techsenger.tabshell.core.CloseCheckResult;
import com.techsenger.tabshell.core.ClosePreparationResult;
import com.techsenger.tabshell.core.shelltab.AbstractShellTabPresenter;
import java.util.function.Consumer;

/**
 *
 * @author Pavel Castornii
 */
public class WebBrowserTabPresenter<V extends WebBrowserTabView, C extends WebBrowserTabComposer>
        extends AbstractShellTabPresenter<V, C> {

    public WebBrowserTabPresenter(V view, String url) {
        super(view);
        getParameters().setUrl(url);
    }

    @Override
    public CloseCheckResult isReadyToClose() {
        return CloseCheckResult.READY;
    }

    @Override
    public void prepareToClose(Consumer<ClosePreparationResult> resultCallback) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    protected Descriptor createDescriptor() {
        return new Descriptor(WebComponents.WEB_BROWSER_TAB);
    }

    @Override
    public void handleSelected(boolean selected) {
        super.handleSelected(selected);
        if (getComposer().getArea().getLocation() == null) {
            getComposer().getToolBar().requestFocus();
        }
    }

    @Override
    protected WebComposeParameters createParameters() {
        return new WebComposeParameters();
    }

    @Override
    protected WebComposeParameters getParameters() {
        return (WebComposeParameters) super.getParameters();
    }
}
