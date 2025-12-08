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

package com.techsenger.tabshell.material.theme;

/**
 * This palette32 contains 16 colors for foreground and 16 colors for background.
 *
 * However, such approach may contain not very good results because when some application was developed
 * without knowing anything about themes, then rendered colors can be wrong for this application. It terminal were
 * used only for running commands, then it wouldn't be a big problem. But if someone starts a TUI application in
 * this terminal, for example, mc, then it will become a problem.
 *
 * @author Pavel Castornii
 */
public interface ThemePalette32 {

    int[] getFgColors();

    int[] getBgColors();
}
