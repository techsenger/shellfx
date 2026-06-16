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

package com.techsenger.shellfx.demo.page;

import com.techsenger.patternfx.core.HistoryPolicy;
import com.techsenger.patternfx.core.HistoryProvider;
import com.techsenger.shellfx.core.tab.TabParams;
import java.util.Objects;

/**
 *
 * @author Pavel Castornii
 */
public class PageTabParams extends TabParams {

    private final PageMenuType menuType;

    public PageTabParams(HistoryProvider<PageTabHistory> historyProvider, PageMenuType menuType) {
        this.menuType = menuType;
        setHistoryProvider(historyProvider);
        setHistoryPolicy(HistoryPolicy.ALL);
    }

    public PageMenuType getMenuType() {
        return menuType;
    }

    @Override
    protected void validate() {
        super.validate();
        Objects.requireNonNull(menuType);
    }
}
