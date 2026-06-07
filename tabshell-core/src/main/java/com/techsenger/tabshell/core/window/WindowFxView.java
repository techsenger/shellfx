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

package com.techsenger.tabshell.core.window;

import com.techsenger.annotations.Nullable;
import com.techsenger.annotations.Unmodifiable;
import com.techsenger.patternfx.mvp.ChildFxView;
import com.techsenger.patternfx.mvp.ParentFxView;
import com.techsenger.tabshell.material.style.Stylesheet;
import java.util.List;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.scene.layout.Region;
import javafx.stage.Stage;

/**
 *
 * @author Pavel Castornii
 */
public interface WindowFxView<P extends WindowPresenter<?>> extends ChildFxView<P>, WindowView {

    interface Composer extends ChildFxView.Composer, WindowView.Composer {

        /**
         * Defines the component that currently has the focus.
         *
         * <p>This method is intended for {@link WindowType#TOP_LEVEL} windows only.
         */
        ReadOnlyObjectProperty<ParentFxView<?>> focusedProperty();

        /**
         * Returns the value of {@link #focusedProperty()}.
         *
         * <p>This method is intended for {@link WindowType#TOP_LEVEL} windows only.
         *
         * @return
         */
        ParentFxView<?> getFocused();

        /**
         * {@inheritDoc}
         *
         * <p>This method is intended for {@link WindowType#NESTED} windows only.
         */
        @Override
        void close();

        @Nullable WindowContainerFxView<?> getContainer();
    }

    @Override
    Composer getComposer();

    /**
     * Returns the {@link Stage} that backs this window.
     *
     * <p>This method is intended for {@link WindowType#TOP_LEVEL} windows only.
     *
     * @return the {@link Stage} of this window
     */
    Stage getStage();

    /**
     * Adds stylesheets to this window.
     *
     * <p>This method is intended for {@link WindowType#TOP_LEVEL} windows only.
     *
     * @param sheets the stylesheets to add
     */
    void addStylesheets(List<Stylesheet> sheets);

    /**
     * Removes stylesheets from this window.
     *
     * <p>This method is intended for {@link WindowType#TOP_LEVEL} windows only.
     *
     * @param sheets the stylesheets to remove
     */
    void removeStylesheets(List<Stylesheet> sheets);

    /**
     * Returns an unmodifiable list of stylesheets applied to this window.
     *
     * <p>This method is intended for {@link WindowType#TOP_LEVEL} windows only.
     *
     * @return an unmodifiable list of stylesheets
     */
    @Unmodifiable List<Stylesheet> getStylesheets();

    @Override
    Region getNode();
}
