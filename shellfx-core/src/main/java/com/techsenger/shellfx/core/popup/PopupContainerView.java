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

package com.techsenger.shellfx.core.popup;

import com.techsenger.annotations.Unmodifiable;
import com.techsenger.patternfx.mvvm.ChildView;
import com.techsenger.shellfx.material.Anchors;
import java.util.List;

/**
 *
 * @author Pavel Castornii
 */
public interface PopupContainerView<VM extends PopupContainerViewModel<?>> extends ChildView<VM> {

    interface Composer extends ChildView.Composer, PopupContainerComposer {

        /**
         * Adds the specified popup component to the component tree.
         *
         * @param popup the popup component to add
         */
        void addPopup(PopupView<?> popup, Anchors anchors);

        /**
         * Removes the specified popup component from the component tree.
         *
         * @param popup the popup component to remove
         */
        void removePopup(PopupView<?> popup);

        /**
         * Removes the specified popup component from the component tree and deinitializes it.
         *
         * @param popup the popup component to close
         */
        void closePopup(PopupView<?> popup);

        /**
         * Returns an an modifiable list of popups.
         */
        @Unmodifiable List<? extends PopupView<?>> getPopups();
    }

    @Override
    Composer getComposer();

}
