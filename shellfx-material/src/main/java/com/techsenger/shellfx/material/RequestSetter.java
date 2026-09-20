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

package com.techsenger.shellfx.material;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a setter whose value is decided by the underlying platform or widget rather than the ViewModel itself,
 * so calling it only issues a request and never guarantees the requested value takes effect.
 *
 * <p>Such a setter exists because JavaFX itself exposes the corresponding state as read-only (for example,
 * {@code Window#widthProperty()}), with a separate best-effort setter the platform is free to adjust, clamp, or
 * reject outright. A ViewModel following the same {@code wrapper}/{@code source} split forwards the request to
 * the View, which performs the real platform call; the outcome flows back independently through the paired
 * read-only property.
 *
 * <p>Two typical shapes: platform-owned geometry, such as a window's width, height, x, y, or its maximized/
 * minimized state, which the platform may adjust or ignore outright; and a control's selection, such as a
 * {@code ComboBox}, where the requested value may not be present among the available choices, or may be
 * rejected for another widget-specific reason, leaving the selection unchanged. Either way, the property this
 * setter is paired with — not the setter's argument — is the source of truth for what is actually in effect.
 *
 * @author Pavel Castornii
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface RequestSetter {

}
