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

import com.techsenger.patternfx.mvp.ChildView;
import com.techsenger.tabshell.core.ShellPort;
import com.techsenger.tabshell.core.dialog.DialogContainerView;
import com.techsenger.tabshell.material.icon.Icon;

/**
 *
 * @author Pavel Castornii
 */
public interface TabView extends ChildView, DialogContainerView {

    interface Composer extends ChildView.Composer,  DialogContainerView.Composer {

        ShellPort getShellPort();

        void close();
    }

    @Override
    Composer getComposer();

    /**
     * Sets whether the component can be closed.
     *
     * @param closable true to allow closing the tab, false to prevent it
     */
    void setClosable(boolean closable);

    void setWaiting(boolean waiting);

    void setIcon(Icon<?> icon);

    void setTitle(String title);

    void setTooltip(String tooltip);

    boolean isSelected();
}
