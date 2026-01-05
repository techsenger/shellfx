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

package com.techsenger.tabshell.jfx.stylesheet;

/**
 *
 * @author Pavel Castornii
 */
public record StylesheetItem(int depth, String name, boolean expanded) {

    public static final int APPLICATION_DEPTH = 0;

    public static final int WINDOW_DEPTH = 1;

    public static final int NODE_DEPTH = 2;

    public static final int STYLESHEET_DEPTH = 3;
}
