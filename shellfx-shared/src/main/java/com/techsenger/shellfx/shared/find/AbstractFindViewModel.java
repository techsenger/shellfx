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

import com.techsenger.annotations.Nullable;
import com.techsenger.annotations.Unmodifiable;
import com.techsenger.patternfx.mvvm.ChildComposer;
import com.techsenger.shellfx.core.Debouncer;
import com.techsenger.shellfx.core.UiExecutor;
import com.techsenger.shellfx.core.area.AbstractAreaViewModel;
import com.techsenger.shellfx.core.area.AreaParams;
import com.techsenger.shellfx.core.config.ConfigListener;
import com.techsenger.shellfx.core.config.ConfigUtils;
import com.techsenger.toolkit.fx.value.ObservableSource;
import com.techsenger.toolkit.fx.value.SimpleObservableSource;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyBooleanWrapper;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.ReadOnlyStringProperty;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @param <C> the composer type
 * @param <R> the kind of {@link FindResult} this component reports
 * @author Pavel Castornii
 */
public abstract class AbstractFindViewModel<C extends ChildComposer, R extends FindResult>
        extends AbstractAreaViewModel<C> implements FindPort<R> {

    /**
     * The hint of a config notification telling that the find texts were replaced. Finds sharing one config react
     * only to it, so they show the same list of earlier find texts.
     */
    protected static final Object FIND_TEXTS_REPLACED_HINT = new Object();

    private static final Logger logger = LoggerFactory.getLogger(AbstractFindViewModel.class);

    private final StringProperty editedFindText = new SimpleStringProperty();

    private final ObservableList<String> modifiableFindTexts = FXCollections.observableArrayList();

    private final ObservableList<String> findTexts =
            FXCollections.unmodifiableObservableList(modifiableFindTexts);

    private final BooleanProperty showClear = new SimpleBooleanProperty(true);

    private final ReadOnlyBooleanWrapper clearVisible = new ReadOnlyBooleanWrapper();

    private final BooleanProperty showMatches = new SimpleBooleanProperty(true);

    private final ReadOnlyStringWrapper matchesText = new ReadOnlyStringWrapper();

    private final ReadOnlyBooleanWrapper matchesVisible = new ReadOnlyBooleanWrapper();

    private final ReadOnlyBooleanWrapper notFound = new ReadOnlyBooleanWrapper();

    private final ReadOnlyObjectWrapper<@Nullable R> findResult = new ReadOnlyObjectWrapper<>();

    private final BooleanProperty matchCaseSelected = new SimpleBooleanProperty();

    private final BooleanProperty matchCaseDisabled = new SimpleBooleanProperty();

    private final FindTrigger findTrigger;

    private final @Nullable Debouncer debouncer;

    private final @Nullable Debouncer findTextDebouncer;

    private final ObservableSource<String> valueSource = new SimpleObservableSource<>();

    private final ConfigListener configListener = this::onConfigChanged;

    /**
     * Whether the find texts are being replaced in the drop-down list. The ComboBox may change the edited text by
     * itself meanwhile, which is not a user edit and must neither start a search nor clear the result.
     */
    private boolean replacingTexts;

    /**
     * Whether the find texts are being replaced with the ones from the config. A change of the list made meanwhile
     * is not written back to the config, otherwise components sharing it would keep notifying each other.
     */
    private boolean applyingConfigTexts;

    /**
     * Whether this component is notifying the config listeners that the find texts were replaced. The notification
     * is synchronous and reaches this component as well, which must ignore it.
     */
    private boolean notifyingConfigListeners;

    private @Nullable CompletableFuture<R> currentFind;

    public AbstractFindViewModel(AreaParams params, FindTrigger findTrigger) {
        super(params);
        this.findTrigger = findTrigger;
        this.debouncer = findTrigger == FindTrigger.ON_TYPE ? new Debouncer(getDebounceMillis()) : null;
        this.findTextDebouncer = findTrigger == FindTrigger.ON_TYPE
                ? new Debouncer(getFindTextDebounceMillis()) : null;
    }

    @Override
    public String getEditedFindText() {
        return editedFindText.get();
    }

    @Override
    public void setEditedFindText(String editedFindText) {
        this.editedFindText.set(editedFindText);
    }

    @Override
    public StringProperty editedFindTextProperty() {
        return editedFindText;
    }

    public FindTrigger getFindTrigger() {
        return findTrigger;
    }

    @Override
    public @Unmodifiable ObservableList<String> getFindTexts() {
        return findTexts;
    }

    @Override
    public boolean isShowClear() {
        return showClear.get();
    }

    @Override
    public void setShowClear(boolean showClear) {
        this.showClear.set(showClear);
    }

    @Override
    public BooleanProperty showClearProperty() {
        return showClear;
    }

    @Override
    public boolean isClearVisible() {
        return clearVisible.get();
    }

    @Override
    public ReadOnlyBooleanProperty clearVisibleProperty() {
        return clearVisible.getReadOnlyProperty();
    }

    @Override
    public boolean isShowMatches() {
        return showMatches.get();
    }

    @Override
    public void setShowMatches(boolean showMatches) {
        this.showMatches.set(showMatches);
    }

    @Override
    public BooleanProperty showMatchesProperty() {
        return showMatches;
    }

    @Override
    public String getMatchesText() {
        return matchesText.get();
    }

    @Override
    public ReadOnlyStringProperty matchesTextProperty() {
        return matchesText.getReadOnlyProperty();
    }

    @Override
    public boolean isMatchesVisible() {
        return matchesVisible.get();
    }

    @Override
    public ReadOnlyBooleanProperty matchesVisibleProperty() {
        return matchesVisible.getReadOnlyProperty();
    }

    @Override
    public boolean isNotFound() {
        return notFound.get();
    }

    @Override
    public ReadOnlyBooleanProperty notFoundProperty() {
        return notFound.getReadOnlyProperty();
    }

    @Override
    public @Nullable R getFindResult() {
        return findResult.get();
    }

    @Override
    public ReadOnlyObjectProperty<@Nullable R> findResultProperty() {
        return findResult.getReadOnlyProperty();
    }

    @Override
    public boolean isMatchCaseSelected() {
        return matchCaseSelected.get();
    }

    @Override
    public void setMatchCaseSelected(boolean matchCaseSelected) {
        this.matchCaseSelected.set(matchCaseSelected);
    }

    @Override
    public BooleanProperty matchCaseSelectedProperty() {
        return matchCaseSelected;
    }

    @Override
    public boolean isMatchCaseDisabled() {
        return matchCaseDisabled.get();
    }

    @Override
    public void setMatchCaseDisabled(boolean matchCaseDisabled) {
        this.matchCaseDisabled.set(matchCaseDisabled);
    }

    @Override
    public BooleanProperty matchCaseDisabledProperty() {
        return matchCaseDisabled;
    }

    protected void onFindNext() {

    }

    protected void onFindPrevious() {

    }

    protected void onMatchCase() {

    }

    /**
     * Adds {@code editedFindText} to {@code findTexts}, cancels any pending incremental search, and runs a find.
     * Called by the View when the user submits the find field, e.g. by pressing Enter. Always searches
     * immediately, regardless of the trigger mode or {@code minSearchLength}: an explicit submit is a deliberate
     * request, not an incidental keystroke, so those gates — which exist only to avoid firing {@code ON_TYPE}
     * incremental search too eagerly — do not apply here.
     */
    protected void onFindSubmitted() {
        var text = getEditedFindText();
        if (text == null || text.isEmpty()) {
            return;
        }
        if (debouncer != null) {
            debouncer.cancel();
        }
        addFindText(text);
        runFind();
    }

    /**
     * Runs a find using the current {@code editedFindText} and returns its eventual outcome, synchronously or from a
     * background thread. Called by {@link #runFind()}, which discards the outcome if a newer find has started by
     * the time it completes, so an implementation never has to cancel or otherwise account for its own staleness.
     * Never call this from the View: this class alone owns the trigger mode, debounce timing, and minimum-length
     * gating.
     */
    protected abstract CompletableFuture<R> onFind();

    protected abstract void onFindCleared();

    @Override
    protected void postInitialize() {
        super.postInitialize();
        editedFindText.addListener((obs, oldV, newV) -> {
            if (!replacingTexts) {
                applyFindTextChange(newV);
            }
        });
        findResult.addListener((obs, oldV, newV) -> applyFindResult(newV));
        showClear.addListener((obs, oldV, newV) -> applyFindTextChange(getEditedFindText()));
        showMatches.addListener((obs, oldV, newV) -> applyShowMatches());
        var config = getConfig();
        if (config != null) {
            config.addListener(configListener);
        }
    }

    @Override
    protected void preDeinitialize() {
        super.preDeinitialize();
        // cancel a pending debounced find so it cannot fire after the component is gone
        if (debouncer != null) {
            debouncer.cancel();
        }
        if (findTextDebouncer != null) {
            findTextDebouncer.cancel();
        }
        var config = getConfig();
        if (config != null) {
            config.removeListener(configListener);
        }
    }

    @Override
    protected void loadStateFromConfig() {
        super.loadStateFromConfig();
        var config = Objects.requireNonNull(getConfig());
        setMatchCaseSelected(config.isMatchCaseSelected());
        modifiableFindTexts.setAll(config.getFindTexts());
    }

    @Override
    protected void observeStateForConfig() {
        super.observeStateForConfig();
        var config = Objects.requireNonNull(getConfig());
        ConfigUtils.observe(matchCaseSelected, config, FindConfig::setMatchCaseSelected);
        modifiableFindTexts.addListener((ListChangeListener<String>) change -> {
            if (applyingConfigTexts) {
                return;
            }
            config.setFindTexts(new ArrayList<>(modifiableFindTexts));
            notifyTextsReplaced(config);
        });
    }

    protected void addFindText(String findText) {
        var updatedFindTexts = new ArrayList<>(modifiableFindTexts);
        ConfigUtils.addFirst(updatedFindTexts, findText);
        ConfigUtils.limit(updatedFindTexts);
        replaceFindTexts(updatedFindTexts);
    }

    @Override
    protected @Nullable FindConfig getConfig() {
        return (FindConfig) super.getConfig();
    }

    protected ObservableList<String> getModifiableFindTexts() {
        return modifiableFindTexts;
    }

    protected void onClearFindText() {
        setEditedFindText(""); // empty string!
    }

    /**
     * Returns the minimum number of characters {@code editedFindText} must reach before {@code ON_TYPE} incremental
     * search schedules a find. Not used for {@code ON_SUBMIT}.
     */
    protected int getMinSearchLength() {
        return 3;
    }

    /**
     * Returns the debounce delay, in milliseconds, {@code ON_TYPE} incremental search waits after the last
     * keystroke before running a find. Not used for {@code ON_SUBMIT}. Called from the constructor to build the
     * underlying {@link Debouncer} — an override must return a constant, since subclass instance state is not
     * yet initialized at that point.
     */
    protected int getDebounceMillis() {
        return 300;
    }

    /**
     * Returns the delay, in milliseconds, {@code applyFindResult} waits after the last incidental {@code ON_TYPE}
     * result before adding the find text to {@code findTexts}. Not used for {@code ON_SUBMIT}. Called from the
     * constructor to build the underlying {@link Debouncer} — an override must return a constant, since
     * subclass instance state is not yet initialized at that point.
     */
    protected int getFindTextDebounceMillis() {
        return 3000;
    }

    protected void setNotFound(boolean notFound) {
        this.notFound.set(notFound);
    }

    protected void setMatchesText(String matchesText) {
        this.matchesText.set(matchesText);
    }

    protected void setMatchesVisible(boolean matchesVisible) {
        this.matchesVisible.set(matchesVisible);
    }

    protected void setClearVisible(boolean clearVisible) {
        this.clearVisible.set(clearVisible);
    }

    /**
     * Reports a new {@code findResult}, or {@code null} to hide the result info. Written only from within this
     * class, by {@link #runFind()} applying an {@link #onFind()} outcome and by {@link #applyFindTextChange(String)}
     * clearing it — a component delegating its search to something external reports back by completing the
     * {@link CompletableFuture} it returned from {@code onFind()}, never by calling this directly.
     *
     * @param findResult the new search outcome, or {@code null} if there is nothing to search
     */
    protected void setFindResult(@Nullable R findResult) {
        this.findResult.set(findResult);
    }

    /**
     * Runs {@link #onFind()} against the current {@code editedFindText}, clearing {@code findResult} first so no
     * previous outcome is shown while the new find is in flight, and applies whatever it eventually completes with
     * — unless a further find has since started, in which case the now-stale outcome is silently discarded
     * instead. Does nothing if {@code editedFindText} is empty. Besides the triggers this class handles itself, a
     * subclass may call this directly for a condition it alone knows about — e.g. reapplying the find after a
     * refresh, or after toggling {@code matchCaseSelected} — instead of reporting a result on its own.
     */
    protected void runFind() {
        var text = getEditedFindText();
        if (text == null || text.isEmpty()) {
            return;
        }
        setFindResult(null);
        var future = onFind();
        currentFind = future;
        future.whenComplete((result, error) -> {
            UiExecutor.execute(() -> {
                if (future != currentFind) {
                    return;
                }
                if (error != null) {
                    logger.error("{} Find failed", getDescriptor().getLogPrefix(), error);
                } else {
                    setFindResult(result);
                }
            });
        });
    }

    /**
     * Shows search result information using total match count only.
     *
     * <p>The Find component displays the total number of matches (e.g. "[ 10 ]"). If {@code totalMatches} is
     * {@code 0}, the component reflects the "not found" state.</p>
     *
     * @param totalMatches the total number of matches found
     */
    protected void showFindResultInfo(int totalMatches) {
        setMatchesVisible(isShowMatches());
        if (isShowMatches()) {
            setMatchesText("[" + totalMatches + "]");
        }
        setNotFound(totalMatches == 0);
    }

    protected void hideFindResultInfo() {
        setMatchesVisible(false);
        setNotFound(false);
    }

    /**
     * Reports the current {@code findResult}. Does nothing if there is no current result. Overridden by
     * {@link AbstractNavigableFindViewModel} to also report the current match position.
     */
    protected void reportResultInfo() {
        var result = findResult.get();
        if (result != null) {
            showFindResultInfo(result.getTotalMatches());
        }
    }

    ObservableSource<String> valueSource() {
        return valueSource;
    }

    /**
     * Reacts to {@code findResult} changing by reporting the new outcome through {@link #reportResultInfo()}, or
     * hiding the result info if the new value is {@code null}. For {@code ON_TYPE} also adds
     * {@code editedFindText} to {@code findTexts} once at least one match was found and it has stayed unchanged for
     * {@code findTextDebounceMillis} — so live typing does not flood {@code findTexts} with intermediate texts.
     */
    private void applyFindResult(@Nullable R result) {
        if (result == null) {
            if (findTextDebouncer != null) {
                findTextDebouncer.cancel();
            }
            hideFindResultInfo();
            return;
        }
        if (findTextDebouncer != null) {
            if (result.getTotalMatches() > 0) {
                var text = getEditedFindText();
                findTextDebouncer.schedule(() -> addFindText(text));
            } else {
                findTextDebouncer.cancel();
            }
        }
        reportResultInfo();
    }

    private void onConfigChanged(@Nullable Object hint) {
        if (notifyingConfigListeners || !FIND_TEXTS_REPLACED_HINT.equals(hint)) {
            return;
        }
        applyingConfigTexts = true;
        try {
            replaceFindTexts(new ArrayList<>(Objects.requireNonNull(getConfig()).getFindTexts()));
        } finally {
            applyingConfigTexts = false;
        }
    }

    private void notifyTextsReplaced(FindConfig config) {
        notifyingConfigListeners = true;
        try {
            config.notifyListeners(FIND_TEXTS_REPLACED_HINT);
        } finally {
            notifyingConfigListeners = false;
        }
    }

    private void replaceFindTexts(List<String> texts) {
        // replacing the items makes the ComboBox move its selection to another item and change the edited text with
        // it, so the value is set again and the temporary changes are not treated as user edits
        var editedText = getEditedFindText();
        replacingTexts = true;
        try {
            modifiableFindTexts.setAll(texts);
            valueSource.next(editedText);
        } finally {
            replacingTexts = false;
        }
    }

    /**
     * Re-reports the current {@code findResult}, or hides the result info if there is none. Called when
     * {@code showMatches} changes so a client toggling it takes effect immediately, whether or not a search is
     * already showing a result.
     */
    private void applyShowMatches() {
        if (findResult.get() != null) {
            reportResultInfo();
        } else {
            hideFindResultInfo();
        }
    }

    private void applyFindTextChange(String text) {
        this.currentFind = null;
        if (text == null || text.isEmpty()) {
            if (debouncer != null) {
                debouncer.cancel();
            }
            if (findTextDebouncer != null) {
                findTextDebouncer.cancel();
            }
            setClearVisible(false);
            setFindResult(null);
            hideFindResultInfo();
            onFindCleared();
            return;
        }
        setClearVisible(isShowClear());
        if (findTrigger == FindTrigger.ON_TYPE) {
            if (text.length() >= getMinSearchLength()) {
                debouncer.schedule(this::runFind);
            } else {
                debouncer.cancel();
            }
        }
    }
}
