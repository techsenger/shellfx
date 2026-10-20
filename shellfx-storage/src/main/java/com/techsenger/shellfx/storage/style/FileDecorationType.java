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

/**
 * Tells whether the decoration a client asks for is the last one the file will get, or the second loading phase is
 * still to refine the file and decorate it again.
 *
 * @author Pavel Castornii
 */
public enum FileDecorationType {

    /**
     * The file is still to be refined, so it is decorated provisionally: it is shown with a pending icon instead of
     * its own.
     */
    TEMP,

    /**
     * The file needs no refining, or it has been refined, so what it is decorated with is final.
     */
    FINAL
}
