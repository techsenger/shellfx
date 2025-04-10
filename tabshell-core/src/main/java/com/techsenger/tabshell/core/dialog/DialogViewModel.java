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

package com.techsenger.tabshell.core.dialog;

import com.techsenger.tabshell.core.IconedViewModel;
import com.techsenger.tabshell.core.TitledViewModel;
import com.techsenger.tabshell.core.pane.PaneViewModel;

/**
 *
 * @author Pavel Castornii
 */
public interface DialogViewModel extends PaneViewModel, TitledViewModel, IconedViewModel {

    @Override
    DialogKey getKey();

    DialogScope getScope();

    /**
     * Requests the View to close itself.
     */
    void requestClose();
}
