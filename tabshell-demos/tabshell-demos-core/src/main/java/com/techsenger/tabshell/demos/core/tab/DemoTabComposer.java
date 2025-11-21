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

import com.techsenger.tabshell.core.tab.AbstractShellTabComposer;
import com.techsenger.tabshell.demos.core.dialog.DemoDialogView;
import com.techsenger.tabshell.demos.core.dialog.DemoDialogViewModel;

/**
 *
 * @author Pavel Castornii
 */
public class DemoTabComposer extends AbstractShellTabComposer<DemoTabView> implements DemoTabView.Composer {

    protected class ViewModelComposer extends AbstractShellTabComposer.ViewModelComposer
            implements DemoTabViewModel.Composer {

        @Override
        public void openDemoDialog(DemoDialogViewModel dialog) {
            DemoTabComposer.this.openDemoDialog(dialog); // magic
        }
    }

    public DemoTabComposer(DemoTabView view) {
        super(view);
    }

    @Override
    public ViewModelComposer getViewModelComposer() {
        return (ViewModelComposer) super.getViewModelComposer();
    }

    @Override
    public void openDemoDialog(DemoDialogViewModel dialogVM) {
        var dialogV = new DemoDialogView(dialogVM);
        dialogV.initialize(); //dont' forget!
        openDialog(dialogV);
    }

    @Override
    protected ViewModelComposer createViewModelComposer() {
        return new DemoTabComposer.ViewModelComposer();
    }
}
