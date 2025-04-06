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

package com.techsenger.tabshell.material.textarea;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import javafx.scene.control.IndexRange;
import javafx.scene.text.TextFlow;
import org.fxmisc.richtext.CaretSelectionBind;
import org.fxmisc.richtext.CodeArea;
import org.fxmisc.richtext.StyledTextArea;
import org.fxmisc.richtext.TextExt;
import org.fxmisc.richtext.model.Codec;
import org.fxmisc.richtext.model.RichTextChange;
import org.fxmisc.richtext.util.UndoUtils;
import org.reactfx.SuspendableYes;

/**
 * This code area is the CodeArea that works with TextAreaStyle. As no StyleClassesTextArea no CodeArea support generics
 * code from these two classes was copied to this class.
 *
 * Generics:
 * <ol>
 *  <li>{@code Collection<String>} is the paragraph style type (paragraph styles are used for
 *      folding/unfolding (+ -));</li>
 *  <li>{@code ExtendedStyle} - text style.</li>
 * </ol>
 *
 * <p>Important. Only \n line terminators are used inside all RichTextFX text areas,
 * see this <a href="https://github.com/FXMisc/RichTextFX/issues/211#issuecomment-158661921">comment</a>
 * So, don't use {@link System#lineSeparator()} and its length, because you will get wrong results.
 *
 * @author Pavel Castornii
 */
public class ExtendedTextArea extends StyledTextArea<Collection<String>, Collection<TextAreaStyle>> {

    /**
     * This suspendUndo is required when it is necessary to modify text area without saving modifications in undo list.
     */
    private final SuspendableYes suspendUndo = new SuspendableYes();

    private IndexRange savedSelection;

    private int saveCaretPosition;

    /**
     * Codec methods are used for (de)serialization during copy, paste, saving, and load.
     */
    private static class TextAreaStyleCodec implements Codec<Collection<TextAreaStyle>> {

        private final Codec<Collection<String>> backend;

        TextAreaStyleCodec() {
            this.backend = Codec.collectionCodec(Codec.STRING_CODEC);
        }

        @Override
        public String getName() {
            return "richtextfx/codestyle";
        }

        @Override
        public void encode(DataOutputStream os, Collection<TextAreaStyle> t) throws IOException {
            var styleClasses = t.stream().map(s -> s.getClassName()).collect(Collectors.toList());
            this.backend.encode(os, styleClasses);
        }

        @Override
        public Collection<TextAreaStyle> decode(DataInputStream is) throws IOException {
            return backend.decode(is).stream().map(s -> new TextAreaStyle(s)).collect(Collectors.toList());
        }
    }

    public ExtendedTextArea() {
        super(Collections.<String>emptyList(), ExtendedTextArea::styleParagraph,
                TextAreaStyle.EMPTY, ExtendedTextArea::styleText);
        // Style codecs are used for (de)serialization during copy, paste, save, and load
        setStyleCodecs(
                Codec.collectionCodec(Codec.STRING_CODEC),
                Codec.styledTextCodec(new TextAreaStyleCodec()));
        // position the caret at the beginning
        selectRange(0, 0);
        this.setUndoManager(UndoUtils.richTextSuspendableUndoManager(this, suspendUndo));
    }

    public ExtendedTextArea(String text) {
        super(Collections.<String>emptyList(), ExtendedTextArea::styleParagraph,
                TextAreaStyle.EMPTY, ExtendedTextArea::styleText);
        // Style codecs are used for (de)serialization during copy, paste, save, and load
        setStyleCodecs(
                Codec.collectionCodec(Codec.STRING_CODEC),
                Codec.styledTextCodec(new TextAreaStyleCodec()));

        appendText(text);
        getUndoManager().forgetHistory();
        getUndoManager().mark();

        // position the caret at the beginning
        selectRange(0, 0);
        this.setUndoManager(UndoUtils.richTextSuspendableUndoManager(this, suspendUndo));
    }

    /**
     * On text appending RichTextFX resets selection, see https://github.com/FXMisc/RichTextFX/issues/1237 .
     * To keep selection use {@link saveSelection()} and  {@link restoreSelection() }
     */
    public void saveSelection() {
        this.savedSelection = getSelection();
        this.saveCaretPosition = getCaretPosition();
    }

    public IndexRange getSavedSelection() {
        return savedSelection;
    }

    /**
     * On text appending RichTextFX resets selection, see https://github.com/FXMisc/RichTextFX/issues/1237 .
     * To keep selection use {@link saveSelection()} and  {@link restoreSelection() }
     */
    public void restoreSelection() {
        //when caret is at position two, then selection.start = 2, selection.end = 2
        if (this.savedSelection != null && !(this.savedSelection.getStart() == this.saveCaretPosition
                && this.savedSelection.getEnd() == this.saveCaretPosition)) {
            moveTo(this.saveCaretPosition);
            selectRange(this.savedSelection.getStart(), this.savedSelection.getEnd());
        }
    }

    private static void styleParagraph(TextFlow paragraph, Collection<String> styles) {
        paragraph.getStyleClass().addAll(styles);
        //paragraph.setStyle( style );
    }

    private static void styleText(TextExt text, Collection<TextAreaStyle> styles) {
        var styleClasses = styles.stream().map(s -> s.getClassName()).collect(Collectors.toList());
        text.getStyleClass().addAll(styleClasses);
        // or
        // text.setStyle( style.getCss() );
    }

    /**
     * In RichTextFX returned style range is relative to specific paragraph/line. This method returns absolute
     * range. For details see https://github.com/FXMisc/RichTextFX/issues/1231
     *
     * @param position
     * @return
     */
    public IndexRange getAbsoluteStyleRangeAt(int position) {
        var ss = this.getStyleSpans(0, this.getLength());  // From beginning to end
        var range = ss.getStyleRange(position);  // So range is from beginning
        return range;
    }

    public SuspendableYes getSuspendUndo() {
        return suspendUndo;
    }

    /**
     * Checks if this rich change modifies only styles, but text after it will be the same.
     * @param c
     * @return
     */
    public boolean isStyleOnlyChange(RichTextChange<Collection<String>, String, Collection<TextAreaStyle>> c) {
        return c.getRemoved().getText().equals(c.getInserted().getText());
    }

    /*

        Code copied from RichTextFX CodeArea:

    */

    {
        getStyleClass().add("code-area");

        // load the default style that defines a fixed-width font
        getStylesheets().add(CodeArea.class.getResource("code-area.css").toExternalForm());

        // don't apply preceding style to typed text
        setUseInitialStyleForInsertion(true);
    }

    protected static final Pattern WORD_PATTERN = Pattern.compile("\\w+", Pattern.UNICODE_CHARACTER_CLASS);

    protected static final Pattern WORD_OR_SYMBOL = Pattern.compile(
            "([\\W&&[^\\h]]{2}"    // Any two non-word characters (excluding white spaces), matches like:
                                   // !=  <=  >=  ==  +=  -=  *=  --  ++  ()  []  <>  &&  ||  //  /*  */
            + "|\\w*)"              // Zero or more word characters [a-zA-Z_0-9]
            + "\\h*",                // Both cases above include any trailing white space
            Pattern.UNICODE_CHARACTER_CLASS
        );

    /**
     * Skips ONLY 1 number of word boundaries backwards.
     * @param n is ignored !
     */
    @Override
    public void wordBreaksBackwards(int n, SelectionPolicy selectionPolicy) {
        if (getLength() == 0) {
            return;
        }

        CaretSelectionBind<?, ?, ?> csb = getCaretSelectionBind();
        int paragraph = csb.getParagraphIndex();
        int position = csb.getColumnPosition();
        int prevWord = 0;

        if (position == 0) {
            prevWord = getParagraph(--paragraph).length();
            moveTo(paragraph, prevWord, selectionPolicy);
            return;
        }

        Matcher m = WORD_OR_SYMBOL.matcher(getText(paragraph));

        while (m.find()) {
            if (m.start() == position) {
                moveTo(paragraph, prevWord, selectionPolicy);
                break;
            }
            prevWord = m.end();
            if (prevWord >= position) {
                moveTo(paragraph, m.start(), selectionPolicy);
                break;
            }
        }
    }

    /**
     * Skips ONLY 1 number of word boundaries forward.
     * @param n is ignored !
     */
    @Override
    public void wordBreaksForwards(int n, SelectionPolicy selectionPolicy) {
        if (getLength() == 0) {
            return;
        }

        CaretSelectionBind<?, ?, ?> csb = getCaretSelectionBind();
        int paragraph = csb.getParagraphIndex();
        int position = csb.getColumnPosition();

        Matcher m = WORD_OR_SYMBOL.matcher(getText(paragraph));

        while (m.find()) {
            if (m.start() > position) {
                moveTo(paragraph, m.start(), selectionPolicy);
                break;
            }
            if (m.hitEnd()) {
                moveTo(paragraph + 1, 0, selectionPolicy);
            }
        }
    }

    @Override
    public void selectWord() {
        if (getLength() == 0) {
            return;
        }

        CaretSelectionBind<?, ?, ?> csb = getCaretSelectionBind();
        int paragraph = csb.getParagraphIndex();
        int position = csb.getColumnPosition();

        Matcher m = WORD_PATTERN.matcher(getText(paragraph));

        while (m.find()) {
            if (m.end() > position) {
                csb.selectRange(paragraph, m.start(), paragraph, m.end());
                return;
            }
        }
    }

    /*

        Code copied from RichTextFX StyleClassedTextArea:

    */

    /**
     * Convenient method to append text together with a single style class.
     */
    public void append(String text, TextAreaStyle style) {

        insert(getLength(), text, style);
    }

    /**
     * Convenient method to insert text together with a single style class.
     */
    public void insert(int position, String text, TextAreaStyle style) {
        replace(position, position, text, style);
    }

    /**
     * Convenient method to replace text together with a single style class.
     */
    public void replace(int start, int end, String text, TextAreaStyle styleClass) {
        replace(start, end, text, Collections.singleton(styleClass));
    }


    /**
     * Convenient method to assign a single style class.
     */
    public void setStyle(int from, int to, TextAreaStyle style) {
        setStyle(from, to, style);
    }


    /**
     * Folds (hides/collapses) paragraphs from <code>startPar</code> to <code>
     * endPar</code>, "into" (i.e. excluding) the first paragraph of the range.
     */
    public void foldParagraphs(int startPar, int endPar) {
        foldParagraphs(startPar, endPar, getAddFoldStyle());
    }

    /**
     * Folds (hides/collapses) the currently selected paragraphs,
     * "into" (i.e. excluding) the first paragraph of the range.
     */
    public void foldSelectedParagraphs() {
        foldSelectedParagraphs(getAddFoldStyle());
    }

    /**
     * Folds (hides/collapses) paragraphs from character position <code>start</code>
     * to <code>end</code>, "into" (i.e. excluding) the first paragraph of the range.
     */
    public void foldText(int start, int end) {
        fold(start, end, getAddFoldStyle());
    }

    public boolean isFolded(int paragraph) {
        return getFoldStyleCheck().test(getParagraph(paragraph).getParagraphStyle());
    }

    /**
     * Unfolds paragraphs <code>startingFrom</code> onwards for the currently folded block.
     */
    public void unfoldParagraphs(int startingFromPar) {
        unfoldParagraphs(startingFromPar, getFoldStyleCheck(), getRemoveFoldStyle());
    }

    /**
     * Unfolds text <code>startingFromPos</code> onwards for the currently folded block.
     */
    public void unfoldText(int startingFromPos) {
        startingFromPos = offsetToPosition(startingFromPos, Bias.Backward).getMajor();
        unfoldParagraphs(startingFromPos, getFoldStyleCheck(), getRemoveFoldStyle());
    }


    /**
     * @return a Predicate that given a paragraph style, returns true if it includes folding.
     */
    protected Predicate<Collection<String>> getFoldStyleCheck() {
        return styleList -> styleList != null && styleList.contains("collapse");
    }

    /**
     * @return a UnaryOperator that given a paragraph style, returns a style that includes fold styling.
     */
    protected UnaryOperator<Collection<String>> getAddFoldStyle() {
        return styleList -> {
            styleList = new ArrayList<>(styleList);
            // "collapse" is in styled-text-area.css:
            // .collapse { visibility: false; }
            styleList.add("collapse");
            return styleList;
        };
    }

    /**
     * @return a UnaryOperator that given a paragraph style, returns a style that excludes fold styling.
     */
    protected UnaryOperator<Collection<String>> getRemoveFoldStyle() {
        return styleList -> {
            styleList = new ArrayList<>(styleList);
            styleList.remove("collapse");
            return styleList;
        };
    }
}
