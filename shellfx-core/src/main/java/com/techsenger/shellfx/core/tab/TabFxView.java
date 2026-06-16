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

package com.techsenger.shellfx.core.tab;

import com.techsenger.annotations.Nullable;
import com.techsenger.patternfx.mvp.ChildFxView;
import com.techsenger.shellfx.core.ShellFxView;
import javafx.scene.control.Tab;

/**
 * A view for components that has a root with JavaFX Tab class.
 *
 * @author Pavel Castornii
 */
public interface TabFxView<P extends TabPresenter<?>> extends ChildFxView<P>, TabView {

    interface Composer extends ChildFxView.Composer, TabView.Composer {

        ShellFxView<?> getShell();

        @Nullable TabContainerFxView<?> getContainer();
    }

    @Override
    Tab getNode();

    @Override
    Composer getComposer();
}
