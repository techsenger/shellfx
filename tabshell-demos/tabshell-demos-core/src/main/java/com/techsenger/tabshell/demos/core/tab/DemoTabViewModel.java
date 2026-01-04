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

package com.techsenger.tabshell.demos.core.tab;

import com.techsenger.tabshell.core.CloseCheckResult;
import com.techsenger.tabshell.core.ClosePreparationResult;
import com.techsenger.tabshell.core.dialog.DialogScope;
import com.techsenger.tabshell.core.menu.SimpleMenuItemHelper;
import com.techsenger.tabshell.core.tab.AbstractShellTabViewModel;
import com.techsenger.tabshell.demos.core.dialog.DemoDialogViewModel;
import com.techsenger.tabshell.demos.core.menu.DemoMenuNames;
import java.util.function.Consumer;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;

/**
 *
 * @author Pavel Castornii
 */
public class DemoTabViewModel<T extends DemoTabMediator> extends AbstractShellTabViewModel<T> {

    private final BooleanProperty newValid = new SimpleBooleanProperty();

    private final BooleanProperty exitIncluded = new SimpleBooleanProperty();

    private final BooleanProperty exitValid = new SimpleBooleanProperty();

    public DemoTabViewModel() {
        super();
        setTitle("Tab");
        addMenuItemHelpers(new SimpleMenuItemHelper(DemoMenuNames.NEW) {
                @Override
                public Boolean getItemValid() {
                    return newValid.get();
                }
            },
            new SimpleMenuItemHelper(DemoMenuNames.EXIT) {
                @Override
                public Boolean getItemIncluded() {
                    return exitIncluded.get();
                }

                @Override
                public Boolean getItemValid() {
                    return exitValid.get();
                }
            }
        );
    }

    public BooleanProperty newValidProperty() {
        return newValid;
    }

    public boolean isNewValid() {
        return newValid.get();
    }

    public void setNewValid(boolean value) {
        this.newValid.set(value);
    }

    public BooleanProperty exitIncludedProperty() {
        return exitIncluded;
    }

    public boolean isExitIncluded() {
        return exitIncluded.get();
    }

    public void setExitIncluded(boolean value) {
        this.exitIncluded.set(value);
    }

    public BooleanProperty exitValidProperty() {
        return exitValid;
    }

    public boolean isExitValid() {
        return exitValid.get();
    }

    public void setExitValid(boolean value) {
        this.exitValid.set(value);
    }

    @Override
    public CloseCheckResult isReadyToClose() {
        return CloseCheckResult.READY;
    }

    @Override
    public void prepareToClose(Consumer<ClosePreparationResult> resultCallback) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    void openDialog(DialogScope scope) {
        var dialogViewModel = new DemoDialogViewModel(scope, true);
        getMediator().addDemoDialog(dialogViewModel);
    }
}
