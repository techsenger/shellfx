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

package com.techsenger.tabshell.demo.window;

import com.techsenger.patternfx.mvp.ComponentDescriptor;
import com.techsenger.tabshell.core.CloseCheckResult;
import com.techsenger.tabshell.core.ClosePreparationResult;
import com.techsenger.tabshell.core.window.AbstractWindowPresenter;
import com.techsenger.tabshell.core.window.WindowParams;
import com.techsenger.tabshell.demo.DemoComponents;
import java.util.function.Consumer;

/**
 *
 * @author Pavel Castornii
 */
public class DemoWindowPresenter extends AbstractWindowPresenter<DemoWindowView> {

    public DemoWindowPresenter(DemoWindowView view, WindowParams params) {
        super(view, params);
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
    protected ComponentDescriptor createDescriptor() {
        return new ComponentDescriptor(DemoComponents.DEMO_WINDOW);
    }

    protected void onAlwaysOnTopSelected(boolean value) {
        setAlwaysOnTop(value);
    }

    protected void onMinimizableSelected(boolean value) {
        setMinimizable(value);
    }

    protected void onMaximizableSelected(boolean value) {
        setMaximizable(value);
    }

    protected void onResizableSelected(boolean value) {
        setResizable(value);
    }

    protected void onClosableSelected(boolean value) {
        setClosable(value);
    }
}
