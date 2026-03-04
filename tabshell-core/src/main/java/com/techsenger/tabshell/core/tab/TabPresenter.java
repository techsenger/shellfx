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

package com.techsenger.tabshell.core.tab;

import com.techsenger.patternfx.mvp.ChildPresenter;
import com.techsenger.tabshell.core.traits.Closable;
import com.techsenger.tabshell.core.CloseAwarePresenter;
import com.techsenger.tabshell.core.traits.Iconed;
import com.techsenger.tabshell.core.SelectablePresenter;
import com.techsenger.tabshell.core.traits.Titled;
import com.techsenger.tabshell.core.traits.Tooltiped;
import com.techsenger.tabshell.core.traits.Waitable;

/**
 *
 * @author Pavel Castornii
 */
public interface TabPresenter<V extends TabView, C extends TabComposer> extends ChildPresenter<V, C>,
        CloseAwarePresenter<V, C>, SelectablePresenter, Closable, Waitable, Iconed, Titled, Tooltiped, TabPort {

}
