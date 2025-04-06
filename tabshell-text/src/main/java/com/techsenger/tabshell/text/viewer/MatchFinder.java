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

package com.techsenger.tabshell.text.viewer;

import com.techsenger.tabshell.material.textarea.TextAreaStyle;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.regex.Pattern;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.control.IndexRange;
import org.fxmisc.richtext.model.RichTextChange;

/**
 *
 * @author Pavel Castornii
 */
public class MatchFinder {

    private final BooleanProperty matchesReset = new SimpleBooleanProperty(true);

    private final List<MatchRange> matchRanges = new ArrayList<>();

    private final ObjectProperty<MatchRange> matchRange = new SimpleObjectProperty<>();

    private final FindMatchesResetPolicy resetPolicy;

    public MatchFinder(FindMatchesResetPolicy resetPolicy) {
        this.resetPolicy = resetPolicy;
    }

    public List<MatchRange> getMatchRanges() {
        return matchRanges;
    }

    public ObjectProperty<MatchRange> matchRangeProperty() {
        return matchRange;
    }

    public BooleanProperty matchesResetProperty() {
        return matchesReset;
    }

    public boolean hasNextMatch() {
        var range = this.matchRange.get();
        if (this.matchesReset.get() || range == null) {
            return false;
        }
        var has = false;
        for (var i = range.getIndex() + 1; i < this.matchRanges.size(); i++) {
            if (!this.matchRanges.get(i).isReplaced()) {
                has = true;
                break;
            }
        }
        return has;
    }

    public boolean hasPreviousMatch() {
        var range = this.matchRange.get();
        if (this.matchesReset.get() || range == null) {
            return false;
        }
        var has = false;
        for (var i = range.getIndex() - 1; i >= 0; i--) {
            if (!this.matchRanges.get(i).isReplaced()) {
                has = true;
                break;
            }
        }
        return has;
    }

    public boolean resetMatches() {
        if (!this.matchesReset.get()) {
            this.matchesReset.set(true);
            this.matchRanges.clear();
            this.matchRange.set(null);
            return true;
        }
        return false;
    }

    /**
     * Method contains main logic for finding text.
     * @param text
     * @param wholeText
     */
    void find(String wholeText, String findText, boolean regExpEnabled, boolean wholeWordEnabled, boolean caseEnabled) {
        this.matchesReset.set(false);
        String regExp;
        if (regExpEnabled) {
            regExp = findText;
        } else {
            regExp = Pattern.quote(findText);
            if (wholeWordEnabled) {
                regExp = "\\b" + regExp + "\\b";
            }
        }
        Pattern pattern;
        if (!caseEnabled) {
            pattern = Pattern.compile(regExp, Pattern.CASE_INSENSITIVE);
        } else {
            pattern = Pattern.compile(regExp);
        }
        var matcher = pattern.matcher(wholeText);
        var start = 0;
        var index = 0;
        while (true) {
            var found = matcher.find(start);
            if (found) {
                matchRanges.add(new MatchRange(index++, matcher.start(), matcher.end()));
                start = matcher.end();
            } else {
                break;
            }
        }
    }

    MatchRange resolveNextRange(IndexRange s, int caretPosition) {
        MatchRange resolvedRange = null;
        boolean ignoreCurrent = false;
        if (s != null && caretPosition == s.getStart()) {
            ignoreCurrent = true;
        }
        for (var range : matchRanges) {
            if (range.isReplaced()) {
                continue;
            }
            if (ignoreCurrent && range == this.matchRange.get()) {
                continue;
            }
            if (caretPosition <= range.getStart()) {
                resolvedRange = range;
                break;
            }
        }
        return resolvedRange;
    }

    MatchRange resolvePreviousRange(int caretPosition) {
        MatchRange resolvedRange = null;
        for (var i = matchRanges.size() - 1; i >= 0; i--) {
            var range = matchRanges.get(i);
            if (range.isReplaced()) {
                continue;
            }
            //when caret is right after selection then we need to move to the previous
            if (caretPosition == range.getEnd() && this.matchRange.get() == range) {
                continue;
            }
            //important >= is here
            if (caretPosition >= range.getEnd()) {
                resolvedRange = range;
                break;
            }
        }
        return resolvedRange;
    }

    void updateNextRangesOnReplace(String replaceText) {
        //As for search regex can be used we get ranges from current range;
        var currentRange = this.matchRange.get();
        //as we replace only one, then all others we must update to replaced match difference
        var diff = currentRange.lengthDifference(replaceText);
        //update ranges
        for (var i = currentRange.getIndex() + 1; i < this.matchRanges.size(); i++) {
            var pos = this.matchRanges.get(i);
            //difference should be calculated for every range because of regex usage
            pos.addToStart(diff);
            pos.addToEnd(diff);
        }
    }

    boolean updateMatchesOnTextChange(RichTextChange<Collection<String>, String, Collection<TextAreaStyle>> c,
            boolean replacingIsDone) {
        if (this.matchesReset.get() || this.matchRanges.isEmpty() || replacingIsDone
                || this.resetPolicy == FindMatchesResetPolicy.MANUAL) {
            return false;
        }
        var iterator = this.matchRanges.iterator();
        while (iterator.hasNext()) {
            var range = iterator.next();
            if ((c.getRemoved().length() != 0 && range.intersects(c.getPosition(), c.getRemovalEnd()))
                    || (c.getInserted().length() != 0 && range.hasInside(c.getPosition()))) {
                iterator.remove();
                if (range == matchRange.get()) {
                    matchRange.set(null);
                }
            }
        }
        //correction
        var index = 0;
        for (var r : matchRanges) {
            r.setIndex(index++);
            if (r.getStart() >= c.getPosition()) {
                r.addToStart(c.getNetLength());
                r.addToEnd(c.getNetLength());
            }
        }
        return true;
    }
}
