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
import com.techsenger.tabshell.core.window.WindowParams;
import com.techsenger.tabshell.core.window.WindowType;
import java.util.Objects;

/**
 *
 * @author Pavel Castornii
 */
public class DefaultShellParams extends WindowParams {

    private final ShellContext context;

    public DefaultShellParams(ShellContext context) {
        super(WindowType.TOP_LEVEL, false, context.getSettings().getAppearance());
        this.context = context;
        setHistoryPolicy(HistoryPolicy.APPEARANCE);
        setHistoryProvider(() -> context.getHistoryManager().getOrCreateHistory(ShellHistory.class, ShellHistory::new));
    }

    public ShellContext getContext() {
        return context;
    }

    @Override
    protected void validate() {
        super.validate();
        Objects.requireNonNull(context);
    }
}
