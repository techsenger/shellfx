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

package com.techsenger.shellfx.core.registry;

import com.techsenger.patternfx.mvvm.ParentView;
import java.util.List;

/**
 * The result of a build: the root control and the providers of all the controls created for it, initialized, from
 * the root down. The owner of the root deinitializes the providers once, when it is done with the controls; a
 * provider is one-shot and cannot be initialized or deinitialized again.
 *
 * @param <V> the type of the view the controls were built for
 * @param <R> the type of the root control
 * @author Pavel Castornii
 */
public final class Controls<V extends ParentView<?>, R> {

    private final R root;

    private final List<ControlProvider<? super V, ?>> providers;

    /**
     * Creates the result of a build.
     *
     * @param root the control the build started from (a menu bar, a menu, a context menu or a tool bar)
     * @param providers the providers of all created controls, the provider of the root first; each of them was
     *     registered for the view's type or one of its supertypes
     */
    public Controls(R root, List<ControlProvider<? super V, ?>> providers) {
        this.root = root;
        this.providers = providers;
    }

    public R root() {
        return root;
    }

    public List<ControlProvider<? super V, ?>> providers() {
        return providers;
    }

    /**
     * Deinitializes all providers from the last to the first. The providers are one-shot, so this can be done only
     * once.
     *
     * @param view the view the controls were built for
     */
    public void deinitializeAll(V view) {
        providers.reversed().forEach(provider -> provider.deinitialize(view));
    }
}
