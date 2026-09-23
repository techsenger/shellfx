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

package com.techsenger.shellfx.devtools.component;

import com.techsenger.shellfx.core.dialog.DialogParams;
import com.techsenger.shellfx.core.tab.TabComposer;
import com.techsenger.shellfx.devtools.shared.NavigableToolBarPort;
import com.techsenger.shellfx.devtools.shared.ToolBarPort;
import com.techsenger.shellfx.dialogs.namevalue.FullNameValueDialogPort;

/**
 * The Port-level facet of a ComponentTab's Composer &mdash; the type a {@link ComponentTabViewModel} is
 * parametrized with.
 *
 * @author Pavel Castornii
 */
public interface ComponentTabComposer extends TabComposer {

    NavigableToolBarPort getComponentToolBarPort();

    ToolBarPort getInspectorToolBarPort();

    FullNameValueDialogPort addNameValueDialog(String nameCaption, String valueCaption, DialogParams params);
}
