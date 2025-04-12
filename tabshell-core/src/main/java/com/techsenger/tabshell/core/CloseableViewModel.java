/*
 * Copyright 2024-2025 Pavel Castornii.
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
public interface CloseableViewModel {

    /**
     * Initiates the component's closure process.
     *
     * <p>When called, this method will trigger the {@link CloseableView#close()} method
     * on the corresponding View through a registered listener. The actual closure
     * is delegated to the parent component (like {@code TabShell} or {@code DialogManager})
     * if the View is not a top-level component.
     */
    void requestClose();
}
