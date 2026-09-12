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

package com.techsenger.shellfx.core.page;

/**
 * Provides full access to the component's client API.
 *
 * @author Pavel Castornii
 */
public interface FullPageContainerPort extends PageContainerPort {

    /**
     * Selects the given page, unless it is already the current page or isn't part of what is currently displayed
     * (e.g. filtered out by an active find) — in which case this call has no effect.
     *
     * @param item the page to select
     */
    void selectPage(PageItem item);

    /**
     * Selects the page at {@code index} within whatever is currently displayed (the full page list, or a
     * find-filtered subset), unless it is already the current page.
     *
     * @param index position within the currently displayed pages
     */
    void selectPage(int index);
}
