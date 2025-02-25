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

package com.techsenger.tabshell.demo.tab;

import com.techsenger.tabshell.core.TabShellViewModel;
import com.techsenger.tabshell.core.dialog.DialogScope;
import com.techsenger.tabshell.core.menu.SimpleMenuItemHelper;
import com.techsenger.tabshell.core.tab.AbstractShellTabViewModel;
import com.techsenger.tabshell.core.tab.ShellTabKey;
import com.techsenger.tabshell.demo.dialog.DemoDialogViewModel;
import com.techsenger.tabshell.demo.menu.DemoMenuKeys;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;

/**
 *
 * @author Pavel Castornii
 */
public class DemoTabViewModel extends AbstractShellTabViewModel {

    private static final ShellTabKey DEMO_TAB = new ShellTabKey("Demo Tab");

    private final BooleanProperty newValid = new SimpleBooleanProperty();

    private final BooleanProperty exitIncluded = new SimpleBooleanProperty();

    private final BooleanProperty exitValid = new SimpleBooleanProperty();

    public DemoTabViewModel(TabShellViewModel tabShell) {
        super(tabShell);
        setTitle("Tab");
        addMenuItemHelpers(
            new SimpleMenuItemHelper(DemoMenuKeys.NEW) {
                @Override
                public Boolean getItemValid() {
                    return newValid.get();
                }
            },
            new SimpleMenuItemHelper(DemoMenuKeys.EXIT) {
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

    @Override
    public ShellTabKey getKey() {
        return DEMO_TAB;
    }

    public BooleanProperty newValidProperty() {
        return newValid;
    }

    public BooleanProperty exitIncludedProperty() {
        return exitIncluded;
    }

    public BooleanProperty exitValidProperty() {
        return exitValid;
    }

    void openDialog(DialogScope scope) {
        var dialogViewModel = new DemoDialogViewModel(scope, true);
        getComponentHelper().openDemoDialog(dialogViewModel);
    }

    @Override
    public DemoTabHelper getComponentHelper() {
        return (DemoTabHelper) super.getComponentHelper();
    }
}
