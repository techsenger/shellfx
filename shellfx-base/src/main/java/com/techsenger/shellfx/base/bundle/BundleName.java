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

package com.techsenger.shellfx.base.bundle;

import com.techsenger.patternfx.core.Name;

/**
 * Identifies a bundle of texts, such as the bundle of a module or of a plugin. The bundle registry finds a bundle by
 * this name. Implementations are, by convention, enum constants.
 *
 * @author Pavel Castornii
 */
public interface BundleName extends Name {

}
