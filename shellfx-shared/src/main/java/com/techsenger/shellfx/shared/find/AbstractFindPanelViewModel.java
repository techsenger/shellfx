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

package com.techsenger.shellfx.shared.find;

import com.techsenger.patternfx.mvvm.ChildComposer;
import com.techsenger.shellfx.core.area.AreaParams;
import com.techsenger.shellfx.core.close.ForceClosePort;

/**
 *
 * @author Pavel Castornii
 */
public abstract class AbstractFindPanelViewModel<C extends ChildComposer> extends AbstractFindBaseViewModel<C>
        implements ForceClosePort {

    private Runnable onCloseRequest = () -> close();

    public AbstractFindPanelViewModel(AreaParams params) {
        super(params);
    }

    @Override
    public Runnable getOnCloseRequest() {
        return this.onCloseRequest;
    }

    @Override
    public void setOnCloseRequest(Runnable runnable) {
        this.onCloseRequest = runnable;
    }

    @Override
    protected FindPanelHistory getHistory() {
        return (FindPanelHistory) super.getHistory();
    }

    protected void onCloseRequest() {
        if (this.onCloseRequest != null) {
            this.onCloseRequest.run();
        }
    }
}
