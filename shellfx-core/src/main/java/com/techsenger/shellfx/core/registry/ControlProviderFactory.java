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

/**
 * Creates a new {@link ControlProvider} for every build of the controls: a provider keeps its control, so one
 * provider can serve only one component view.
 *
 * @param <V> the view type of the component the controls are built for
 * @param <C> the type of the control of the created provider; the provider may hold a control of a narrower type
 * @author Pavel Castornii
 */
@FunctionalInterface
public interface ControlProviderFactory<V extends ParentView<?>, C> {

    ControlProvider<V, ? extends C> create();
}
