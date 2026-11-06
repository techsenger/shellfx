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

package com.techsenger.shellfx.demo.ide;

import com.techsenger.shellfx.core.close.CloseCheckResult;
import com.techsenger.shellfx.core.close.ClosePreparationResult;
import com.techsenger.shellfx.core.popup.PopupContainerViewModel;
import com.techsenger.shellfx.core.tab.AbstractTabViewModel;
import com.techsenger.shellfx.core.tab.TabParams;
import com.techsenger.shellfx.demo.styles.DemoIcons;
import java.util.function.Consumer;

/**
 *
 * @author Pavel Castornii
 */
public class IdeMainTabViewModel<C extends IdeMainTabComposer> extends AbstractTabViewModel<C>
        implements PopupContainerViewModel<C>, IdeMainTabPort {

    public IdeMainTabViewModel() {
        super(new TabParams(null));
    }

    @Override
    public CloseCheckResult isReadyToClose() {
        return CloseCheckResult.READY;
    }

    @Override
    public void prepareToClose(Consumer<ClosePreparationResult> resultCallback) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public IdeMainTabPort.ComposerAccess getComposerAccess() {
        return getComposer();
    }

    @Override
    protected void postInitialize() {
        super.postInitialize();
        setTitle("Main Tab");
        setIcon(DemoIcons.MAIN_TAB);
    }
}
