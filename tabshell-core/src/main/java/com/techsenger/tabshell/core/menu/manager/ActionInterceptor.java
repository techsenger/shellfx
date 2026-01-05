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

package com.techsenger.tabshell.core.menu.manager;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;

/**
 * This interface is a wrapper for menu/menu item actions. We need it because when menu items are not shown to user
 * they are NOT disabled and their actions can be called using accelerator keys. So, it intercepts the event and
 * decides to call origin action or not.
 *
 * @author Pavel Castornii
 */
public interface ActionInterceptor extends EventHandler<ActionEvent> {

    EventHandler<ActionEvent> getAction();
}
