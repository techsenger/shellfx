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

package com.techsenger.shellfx.storage.style;

import com.techsenger.annotations.Nullable;
import com.techsenger.shellfx.material.icon.FontIcon;

/**
 * The icon and the style a file is shown with. A file gets both in one step, so the elements that show it are told
 * once. A decoration without a style is outdated in its style only: the icon is kept and the style is resolved again.
 *
 * @author Pavel Castornii
 * @param icon the icon of the file
 * @param style the style of the icon and the texts, or {@code null} if it has to be resolved again
 */
public record FileDecoration(FontIcon<?> icon, @Nullable IconTextStyle style) {

}
