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

package com.techsenger.tabshell.terminal;

import com.pty4j.PtyProcess;
import com.pty4j.PtyProcessBuilder;
import com.techsenger.jeditermfx.core.util.Platform;
import com.techsenger.jeditermfx.ui.settings.SettingsProvider;
import com.techsenger.mvvm4fx.core.ComponentDescriptor;
import com.techsenger.mvvm4fx.core.HistoryPolicy;
import com.techsenger.tabshell.core.ShellViewModel;
import com.techsenger.tabshell.core.tab.AbstractShellTabViewModel;
import com.techsenger.tabshell.core.theme.ShellTheme;
import com.techsenger.tabshell.terminal.style.TerminalIcons;
import com.techsenger.toolkit.fx.value.ObservableSource;
import com.techsenger.toolkit.fx.value.SimpleObservableSource;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyStringProperty;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

/**
 *
 * @author Pavel Castornii
 */
public class TerminalTabViewModel extends AbstractShellTabViewModel {

    private TerminalPalette terminalPalette;

    private final ObservableSource<Boolean> focusRequired = new SimpleObservableSource<>();

    private final ChangeListener<? super ShellTheme> themeListener = (ov, oldV, newV) -> {
        terminalPalette.setTheme(newV);
        focusRequired.next(true);
    };

    private final ReadOnlyStringWrapper selectedText = new ReadOnlyStringWrapper();

    private final BooleanProperty copyDisable = new SimpleBooleanProperty(true);

    private final BooleanProperty openUrlDisable = new SimpleBooleanProperty(true);

    private final ObservableList<TerminalPaletteType> paletteTypes = FXCollections.observableArrayList(
            Arrays.asList(TerminalPaletteType.values()));

    private final ObjectProperty<TerminalPaletteType> paletteType =
            new SimpleObjectProperty<>(TerminalPaletteType.THEME_32_LC);

    private final PtyProcessTtyConnector ttyConnector;

    private FindPaneViewModel find;

    public TerminalTabViewModel(ShellViewModel shell, String directory) {
        super(shell);
        this.ttyConnector = createTtyConnector(directory);
        this.setIcon(TerminalIcons.TERMINAL);
        this.setTitle("Terminal");
        getDescriptor().setHistoryPolicy(HistoryPolicy.ALL);
        setHistoryProvider(() -> shell.getHistoryManager().getOrCreateHistory(TerminalHistory.class,
                TerminalHistory::new));
    }

    public ObjectProperty<TerminalPaletteType> paletteTypeProperty() {
        return this.paletteType;
    }

    public TerminalPaletteType getPaletteType() {
        return this.paletteType.get();
    }

    public void setPaletteType(TerminalPaletteType type) {
        this.paletteType.set(type);
    }

    public ReadOnlyStringProperty selectedTextProperty() {
        return this.selectedText.getReadOnlyProperty();
    }

    public String getSelectedText() {
        return selectedTextProperty().get();
    }

    public BooleanProperty copyDisableProperty() {
        return copyDisable;
    }

    public boolean isCopyDisable() {
        return copyDisable.get();
    }

    public void setCopyDisable(boolean value) {
        this.copyDisable.set(value);
    }

    public BooleanProperty openUrlDisableProperty() {
        return openUrlDisable;
    }

    public boolean isOpenUrlDisable() {
        return openUrlDisable.get();
    }

    public void setOpenUrlDisable(boolean value) {
        this.openUrlDisable.set(value);
    }

    @Override
    public TerminalTabMediator getMediator() {
        return (TerminalTabMediator) super.getMediator();
    }

    @Override
    protected ComponentDescriptor createDescriptor() {
        return new ComponentDescriptor(TerminalComponentNames.TERMINAL_TAB);
    }

    protected SettingsProvider createSettingsProvider() {
        var settings = getShell().getSettings();
        this.terminalPalette = new TerminalPalette(getShell().getSettings().getAppearance().getTheme(),
                paletteType.get());
        return new TerminalSettingsProvider(settings.getAppearance().getMonospaceFont(), terminalPalette);
    }

    protected PtyProcessTtyConnector createTtyConnector(String directory) {
        try {
            Map<String, String> envs = System.getenv();
            String[] command;
            if (Platform.isWindows()) {
                command = new String[]{"cmd.exe"};
            } else {
                command = new String[]{"/bin/bash", "--login"};
                envs = new HashMap<>(System.getenv());
                envs.put("TERM", "xterm-256color");
            }
            PtyProcess process = new PtyProcessBuilder()
                    .setDirectory(directory)
                    .setCommand(command).setEnvironment(envs).start();
            return new PtyProcessTtyConnector(process, StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    public PtyProcessTtyConnector getTtyConnector() {
        return ttyConnector;
    }

    protected void showFind() {
        if (this.find != null) {
            return;
        }
        this.find = new FindPaneViewModel(getShell().getHistoryManager(), selectedText.get());
        this.find.closeActionProperty().set(() -> hideFind());
        getMediator().showFindPane(this.find);
    }

    protected void hideFind() {
        if (this.find == null) {
            return;
        }
        this.find = null;
        getMediator().hideFindPane();
    }

    protected void createNewTerminal() {
        //TODO
    }

    void addListeners() {
        getShell().getSettings().getAppearance().themeProperty().addListener(themeListener);
        this.paletteType.addListener((ov, oldV, newV) -> {
            this.terminalPalette.setPaletteType(newV);
            this.focusRequired.next(true);
        });
        this.selectedText.addListener((ov, oldV, newV) -> {
            this.copyDisable.set(newV == null);
            this.openUrlDisable.set(!isTextUrl(newV));
        });
    }

    void removeListeners() {
        getShell().getSettings().getAppearance().themeProperty().removeListener(themeListener);
    }

    ObservableList<TerminalPaletteType> getPaletteTypes() {
        return paletteTypes;
    }

    ReadOnlyStringWrapper selectedTextWrapper() {
        return selectedText;
    }

    ObservableSource<Boolean> focusRequiredSource() {
        return focusRequired;
    }

    private boolean isTextUrl(String text) {
        if (text != null) {
            try {
                URI uri = new URI(text);
                uri.toURL();
                return true;
            } catch (Exception e) {
                //do nothing
            }
        }
        return false;
    }
}
