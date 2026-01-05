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

/**
 *
 * @author Pavel Castornii
 */
public interface SelectableView {

    /**
     * Called when the component is selected (also called when a new component is created even if there are no
     * other ones).
     */
    void doOnSelected();

    /**
     * Called when the component is deselected.
     */
    void doOnDeselected();

}
