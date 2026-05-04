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

package com.techsenger.tabshell.core;

import com.techsenger.patternfx.core.HistoryPolicy;
import com.techsenger.patternfx.mvp.Descriptor;

/**
 *
 * @author Pavel Castornii
 */
public class DefaultShellPresenter<V extends ShellView> extends DefaultWindowPresenter<V> implements ShellPresenter<V> {

    private final ShellContext context;

    private Runnable onClose;

    public DefaultShellPresenter(V view, ShellContext context) {
        super(view, context.getSettings().getAppearance());
        this.context = context;
        setHistoryPolicy(HistoryPolicy.APPEARANCE);
        setHistoryProvider(() -> context.getHistoryManager().getOrCreateHistory(ShellHistory.class, ShellHistory::new));
    }

    @Override
    public ShellContext getContext() {
        return this.context;
    }

    @Override
    public <T extends ShellContext> T getContext(Class<T> contextClass) {
        return (T) this.context;
    }

    @Override
    public void close() {
        super.close();
        if (this.onClose != null) {
            this.onClose.run();
        }
    }

    @Override
    public Runnable getOnClose() {
        return this.onClose;
    }

    @Override
    public void setOnClose(Runnable onClose) {
        this.onClose = onClose;
    }

    @Override
    protected ShellHistory getHistory() {
        return (ShellHistory) super.getHistory();
    }

    @Override
    protected Descriptor createDescriptor() {
        return new Descriptor(CoreComponents.SHELL);
    }
}
