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

package com.techsenger.shellfx.shared.find;

import com.techsenger.shellfx.core.area.AreaView;
import java.util.List;

/**
 *
 * @author Pavel Castornii
 */
public interface FindBaseView extends AreaView {

    void updateFindText(String text);

    void updateFindTexts(List<String> texts);

    void updateNotFound(boolean value);

    void updateMatchCaseSelected(boolean value);

    void updateMatchCaseDisabled(boolean value);

    void updateMatchesText(String text);

    void updateMatchesVisible(boolean visible);

    void updateClearVisible(boolean visible);

    void updateFindNextDisabled(boolean value);

    void updateFindPreviousDisabled(boolean value);

    void updateWholeWordSelected(boolean value);

    void updateWholeWordDisabled(boolean value);

    void updateRegExpSelected(boolean value);

    void updateRegExpDisabled(boolean value);

    void updateHighlightSelected(boolean value);

    void updateHighlightDisabled(boolean value);
}
