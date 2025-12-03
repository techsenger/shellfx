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

package com.techsenger.tabshell.core.tab;

import com.techsenger.tabshell.core.ShellViewModel;
import javafx.beans.property.ReadOnlyIntegerProperty;
import javafx.beans.property.ReadOnlyIntegerWrapper;

/**
 *
 * @author Pavel Castornii
 */
public abstract class AbstractShellTabViewModel extends AbstractTabViewModel implements ShellTabViewModel {

    private final ShellViewModel shell;

    private ReadOnlyIntegerWrapper dialogCount = new ReadOnlyIntegerWrapper();

    public AbstractShellTabViewModel(ShellViewModel shell) {
        super();
        this.shell = shell;
    }

    @Override
    public ShellViewModel getShell() {
        return this.shell;
    }

    @Override
    public ReadOnlyIntegerProperty dialogCountProperty() {
        return this.dialogCount.getReadOnlyProperty();
    }

    @Override
    public int getDialogCount() {
        return this.dialogCount.get();
    }

    @Override
    public ShellTabMediator getMediator() {
        return (ShellTabMediator) super.getMediator();
    }

    ReadOnlyIntegerWrapper dialogCountWrapper() {
        return dialogCount;
    }
 }
