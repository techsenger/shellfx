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

package com.techsenger.tabshell.shared.find;

import com.techsenger.tabshell.core.area.AreaView;
import java.util.List;

/**
 *
 * @author Pavel Castornii
 */
public interface FindBaseView extends AreaView {

    void setFindText(String text);

    String getFindText();

    void setFindTexts(List<String> texts);

    List<String> getFindTexts();

    void setNotFound(boolean value);

    boolean isNotFound();

    void setupMatchCase();

    void setMatchCaseSelected(boolean value);

    boolean isMatchCaseSelected();

    void setMatchCaseDisable(boolean value);

    boolean isMatchCaseDisable();

    void setMatchesText(String text);

    String getMatchesText();

    void setMatchesVisible(boolean visible);

    boolean isMatchesVisible();

    void setClearVisible(boolean visible);

    boolean isClearVisible();

    void setupFindNext();

    void setFindNextDisable(boolean value);

    boolean isFindNextDisable();

    void setupFindPrevious();

    void setFindPreviousDisable(boolean value);

    boolean isFindPreviousDisable();

    void setupWholeWord();

    void setWholeWordSelected(boolean value);

    boolean isWholeWordSelected();

    void setWholeWordDisable(boolean value);

    boolean isWholeWordDisable();

    void setupRegExp();

    void setRegExpSelected(boolean value);

    boolean isRegExpSelected();

    void setRegExpDisable(boolean value);

    boolean isRegExpDisable();

    void setupHighlight();

    void setHighlightSelected(boolean value);

    boolean isHighlightSelected();

    void setHighlightDisable(boolean value);

    boolean isHighlightDisable();
}
