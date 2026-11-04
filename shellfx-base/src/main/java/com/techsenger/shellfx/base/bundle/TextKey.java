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

/**
 * The address of a text: the bundle it is in and its key in that bundle. A key is unique only within its bundle, so
 * bundles of different modules do not clash.
 *
 * @param bundleName the name of the bundle
 * @param key the key of the text in the bundle
 * @author Pavel Castornii
 */
public record TextKey(BundleName bundleName, String key) {

}
