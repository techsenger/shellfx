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

package com.techsenger.tabshell.core.traits;

/**
 *
 * @author Pavel Castornii
 */
public interface Closable {

    /**
     * Returns whether the component can be closed.
     *
     * @return true if the tab can be closed, false otherwise
     */
    boolean isClosable();

    /**
     * Sets whether the component can be closed.
     *
     * @param closable true to allow closing the tab, false to prevent it
     */
    void setClosable(boolean closable);
}
