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

package com.techsenger.tabshell.core.dialog;

import com.techsenger.annotations.Unmodifiable;
import com.techsenger.patternfx.mvp.ParentPresenter;
import com.techsenger.tabshell.core.popup.PopupContainerFxView;
import java.util.List;

/**
 *
 * @author Pavel Castornii
 */
public interface DialogContainerFxView<P extends ParentPresenter<?, ?>> extends PopupContainerFxView<P> {

    interface Composer extends PopupContainerFxView.Composer, DialogContainerComposer {

        /**
         * Adds the specified dialog component to the component tree.
         *
         * @param dialog the dialog component to add
         */
        void addDialog(DialogFxView<?> dialog);

        /**
         * Removes the specified dialog component from the component tree.
         *
         * @param dialog the dialog component to remove
         */
        void removeDialog(DialogFxView<?> dialog);

        /**
         * Returns an unmodifiable list of dialogs.
         * @return
         */
        @Unmodifiable List<? extends DialogFxView<?>> getDialogs();

    }

    @Override
    Composer getComposer();
}
