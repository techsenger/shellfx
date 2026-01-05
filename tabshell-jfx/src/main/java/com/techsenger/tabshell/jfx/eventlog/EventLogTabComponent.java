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

package com.techsenger.tabshell.jfx.eventlog;

import com.techsenger.patternfx.core.Name;
import com.techsenger.tabshell.core.tab.AbstractTabComponent;
import com.techsenger.tabshell.jfx.JfxComponentNames;
import com.techsenger.tabshell.jfx.SearchPanelComponent;
import com.techsenger.tabshell.jfx.SearchPanelView;
import com.techsenger.tabshell.jfx.SearchPanelViewModel;

/**
 *
 * @author Pavel Castornii
 */
public class EventLogTabComponent<T extends EventLogTabView<?, ?>> extends AbstractTabComponent<T> {

    protected class Mediator extends AbstractTabComponent.Mediator implements EventLogTabMediator {

        private final EventLogTabComponent<?> component = EventLogTabComponent.this;

        @Override
        public SearchPanelViewModel<?> getSearchPanel() {
            return component.searchPanel.getView().getViewModel();
        }
    }

    private final SearchPanelComponent<?> searchPanel;

    public EventLogTabComponent(T view) {
        super(view);
        this.searchPanel = createSearchPanel();
        getModifiableChildren().add(this.searchPanel);
    }

    @Override
    public Name getName() {
        return JfxComponentNames.EVENT_LOG_TAB;
    }

    @Override
    protected Mediator createMediator() {
        return new Mediator();
    }

    @Override
    protected void preInitialize() {
        super.preInitialize();
        this.searchPanel.initialize();
    }

    protected SearchPanelComponent<?> createSearchPanel() {
        var vm = new SearchPanelViewModel<>();
        var v = new SearchPanelView<>(vm);
        var c = new SearchPanelComponent<>(v);
        return c;
    }

    protected SearchPanelComponent<?> getSearchPanel() {
        return searchPanel;
    }
}
