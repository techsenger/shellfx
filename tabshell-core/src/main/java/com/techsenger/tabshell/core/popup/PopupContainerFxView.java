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

package com.techsenger.tabshell.core.popup;

import com.techsenger.annotations.Unmodifiable;
import com.techsenger.patternfx.mvp.ParentFxView;
import com.techsenger.patternfx.mvp.ParentPresenter;
import com.techsenger.tabshell.material.Anchors;
import java.util.List;

/**
 *
 * @author Pavel Castornii
 */
public interface PopupContainerFxView<P extends ParentPresenter<?>> extends ParentFxView<P>, PopupContainerView {

    interface Composer extends ParentFxView.Composer, PopupContainerView.Composer {

        /**
         * Adds the specified popup component to the component tree.
         *
         * @param popup the popup component to add
         */
        void addPopup(PopupFxView<?> popup, Anchors anchors);

        /**
         * Removes the specified popup component from the component tree.
         *
         * @param popup the popup component to remove
         */
        void removePopup(PopupFxView<?> popup);

        /**
         * Returns an an modifiable list of popups.
         */
        @Unmodifiable List<? extends PopupFxView<?>> getPopups();

    }

    @Override
    Composer getComposer();
}
