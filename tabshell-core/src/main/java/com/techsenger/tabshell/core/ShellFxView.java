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

package com.techsenger.tabshell.core;

import com.techsenger.patternfx.mvp.ParentFxView;
import com.techsenger.tabshell.core.dialog.DialogContainerFxView;
import com.techsenger.tabshell.core.shelltab.ShellTabFxView;
import com.techsenger.tabshell.core.tab.TabContainerFxView;
import javafx.application.HostServices;
import javafx.stage.Stage;

/**
 * There can be only one instance of Shell in VirtualMachine.
 *
 * @author Pavel Castornii
 */
public interface ShellFxView<P extends ShellPresenter<?, ?>> extends ParentFxView<P>,
        TabContainerFxView<ShellTabFxView<?>>, DialogContainerFxView, ShellView {

    interface Composer extends ParentFxView.Composer, DialogContainerFxView.Composer,
            TabContainerFxView.Composer<ShellTabFxView<?>>, ShellComposer {

    }

    /**
     * Returns application host services.
     *
     * @return
     */
    HostServices getHostServices();

    /**
     * Returns primary stage.
     *
     * @return
     */
    Stage getStage();

    @Override
    Composer getComposer();
}
