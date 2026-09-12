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

package com.techsenger.shellfx.core.window;

import com.techsenger.annotations.Nullable;
import com.techsenger.annotations.Unmodifiable;
import com.techsenger.patternfx.mvvm.ChildView;
import com.techsenger.patternfx.mvvm.ParentView;
import com.techsenger.shellfx.material.style.Stylesheet;
import java.util.List;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.scene.layout.Region;
import javafx.stage.Stage;

/**
 *
 * @author Pavel Castornii
 */
public interface WindowView<VM extends WindowViewModel<?>> extends ChildView<VM> {

    interface Composer extends ChildView.Composer, WindowComposer {

        /**
         * Defines the component that currently has the focus.
         *
         * <p>When the window loses focus and then regains it, the focused component does not change,
         * because JavaFX preserves and restores the focus automatically.
         * <p>This method is intended for {@link WindowType#TOP_LEVEL} windows only.
         */
        ReadOnlyObjectProperty<@Nullable ParentView<?>> focusedProperty();

        /**
         * Returns the value of {@link #focusedProperty()}.
         *
         * <p>When the window loses focus and then regains it, the focused component does not change,
         * because JavaFX preserves and restores the focus automatically.
         * <p>This method is intended for {@link WindowType#TOP_LEVEL} windows only.
         *
         * @return
         */
        @Nullable ParentView<?> getFocused();

        /**
         * {@inheritDoc}
         *
         * <p>This method is intended for {@link WindowType#NESTED} windows only.
         */
        @Override
        void close();

        /**
         * {@inheritDoc}
         *
         * <p>This method is intended for {@link WindowType#NESTED} windows only.
         */
        @Override
        @Nullable WindowContainerView<?> getParent();
    }

    @Override
    Composer getComposer();

    /**
     * Adds stylesheets to this window.
     *
     * @param sheets the stylesheets to add
     */
    void addStylesheets(List<Stylesheet> sheets);

    /**
     * Removes stylesheets from this window.
     *
     * @param sheets the stylesheets to remove
     */
    void removeStylesheets(List<Stylesheet> sheets);

    /**
     * Returns an unmodifiable list of stylesheets applied to this window.
     *
     * @return an unmodifiable list of stylesheets
     */
    @Unmodifiable List<Stylesheet> getStylesheets();

    /**
     * Returns the {@link Stage} that backs this window.
     *
     * <p>This method is intended for {@link WindowType#TOP_LEVEL} windows only.
     *
     * @return the {@link Stage} of this window
     */
    Stage getStage();

    @Override
    Region getNode();
}
