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
 * @author Pavel Castornii
 */
class DefaultThemePalette32 implements ThemePalette32 {

    private int[] fgColors;

    private int[] bgColors;

    DefaultThemePalette32(int[] fgColors, int[] bgColors) {
        this.fgColors = fgColors;
        this.bgColors = bgColors;
    }

    @Override
    public int[] getFgColors() {
        return fgColors;
    }

    @Override
    public int[] getBgColors() {
        return bgColors;
    }
}
