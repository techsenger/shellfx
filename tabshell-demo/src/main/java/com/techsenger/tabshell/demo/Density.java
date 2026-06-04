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

package com.techsenger.tabshell.demo;

import com.techsenger.tabshell.material.style.StyleClasses;
import javafx.scene.Scene;

/**
 * In a real application, a density style class should only be added to the root node of the {@link Scene}.
 * However, in the demo application, we need to test multiple density style classes (including the default one)
 * within the same application. For this reason, the density style class is applied to the root container of each
 * demo section.
 *
 * @author Pavel Castornii
 */
public final class Density {

    public static final String STYLE_CLASS = StyleClasses.DENSITY_S;

    private Density() {
        // empty
    }
}
