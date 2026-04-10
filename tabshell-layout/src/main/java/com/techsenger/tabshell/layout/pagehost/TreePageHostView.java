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

package com.techsenger.tabshell.layout.pagehost;

import com.techsenger.tabshell.core.page.TreePageContainerView;
import com.techsenger.tabshell.core.page.TreePageItem;
import java.util.List;

/**
 *
 * @author Pavel Castornii
 */
public interface TreePageHostView extends BasePageHostView, TreePageContainerView {

    void setMenu(TreePageItem root, boolean showRoot);

    void setMenu(FilteredTreePageItem root, boolean showRoot);

    void setPage(TreePageItem item);

    void setBreadcrumbs(List<PageBreadcrumb> breadcrumbs);
}
