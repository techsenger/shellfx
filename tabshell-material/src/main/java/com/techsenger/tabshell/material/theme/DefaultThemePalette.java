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

package com.techsenger.tabshell.material.theme;

import java.util.Map;

/**
 *
 * @author Pavel Castornii
 */
class DefaultThemePalette implements ThemePalette {

    private final int darkColor;

    private final int lightColor;

    private final int base0Color;

    private final int base1Color;

    private final int base2Color;

    private final int base3Color;

    private final int base4Color;

    private final int base5Color;

    private final int base6Color;

    private final int base7Color;

    private final int base8Color;

    private final int base9Color;

    private final int accent0Color;

    private final int accent1Color;

    private final int accent2Color;

    private final int accent3Color;

    private final int accent4Color;

    private final int accent5Color;

    private final int accent6Color;

    private final int accent7Color;

    private final int accent8Color;

    private final int accent9Color;

    private final int success0Color;

    private final int success1Color;

    private final int success2Color;

    private final int success3Color;

    private final int success4Color;

    private final int success5Color;

    private final int success6Color;

    private final int success7Color;

    private final int success8Color;

    private final int success9Color;

    private final int warning0Color;

    private final int warning1Color;

    private final int warning2Color;

    private final int warning3Color;

    private final int warning4Color;

    private final int warning5Color;

    private final int warning6Color;

    private final int warning7Color;

    private final int warning8Color;

    private final int warning9Color;

    private final int danger0Color;

    private final int danger1Color;

    private final int danger2Color;

    private final int danger3Color;

    private final int danger4Color;

    private final int danger5Color;

    private final int danger6Color;

    private final int danger7Color;

    private final int danger8Color;

    private final int danger9Color;

    private final int defaultFgColor;

    private final int mutedFgColor;

    private final int subtleFgColor;

    private final int emphasisFgColor;

    private final int defaultBgColor;

    private final int overlayBgColor;

    private final int subtleBgColor;

    private final int insetBgColor;

    private final int extraBgColor;

    private final int defaultBorderColor;

    private final int mutedBorderColor;

    private final int subtleBorderColor;

    private final int defaultShadowColor;

    private final int emphasisPlusNeutralColor;

    private final int emphasisNeutralColor;

    private final int mutedNeutralColor;

    private final int subtleNeutralColor;

    private final int fgAccentColor;

    private final int emphasisAccentColor;

    private final int mutedAccentColor;

    private final int subtleAccentColor;

    private final int fgWarningColor;

    private final int emphasisWarningColor;

    private final int mutedWarningColor;

    private final int subtleWarningColor;

    private final int fgSuccessColor;

    private final int emphasisSuccessColor;

    private final int mutedSuccessColor;

    private final int subtleSuccessColor;

    private final int fgDangerColor;

    private final int emphasisDangerColor;

    private final int mutedDangerColor;

    private final int subtleDangerColor;

    private final int chart1Color;

    private final int chart2Color;

    private final int chart3Color;

    private final int chart4Color;

    private final int chart5Color;

    private final int chart6Color;

    private final int chart7Color;

    private final int chart8Color;

    private final int chart1Alpha70Color;

    private final int chart2Alpha70Color;

    private final int chart3Alpha70Color;

    private final int chart4Alpha70Color;

    private final int chart5Alpha70Color;

    private final int chart6Alpha70Color;

    private final int chart7Alpha70Color;

    private final int chart8Alpha70Color;

    private final int chart1Alpha20Color;

    private final int chart2Alpha20Color;

    private final int chart3Alpha20Color;

    private final int chart4Alpha20Color;

    private final int chart5Alpha20Color;

    private final int chart6Alpha20Color;

    private final int chart7Alpha20Color;

    private final int chart8Alpha20Color;

    private final int selectionFgColor;

    private final int selectionBgColor;

    private final int selectionBorderColor;

    private final int foundFgColor;

    private final int foundBgColor;

    private final int hyperlinkFgColor;

    private final int hyperlinkBgColor;

    DefaultThemePalette(Map<String, Integer> colorsByName) {
        this.darkColor = ThemeProvider.colorFromMap(colorsByName, "dark");
        this.lightColor = ThemeProvider.colorFromMap(colorsByName, "light");

        this.base0Color = ThemeProvider.colorFromMap(colorsByName, "base-0");
        this.base1Color = ThemeProvider.colorFromMap(colorsByName, "base-1");
        this.base2Color = ThemeProvider.colorFromMap(colorsByName, "base-2");
        this.base3Color = ThemeProvider.colorFromMap(colorsByName, "base-3");
        this.base4Color = ThemeProvider.colorFromMap(colorsByName, "base-4");
        this.base5Color = ThemeProvider.colorFromMap(colorsByName, "base-5");
        this.base6Color = ThemeProvider.colorFromMap(colorsByName, "base-6");
        this.base7Color = ThemeProvider.colorFromMap(colorsByName, "base-7");
        this.base8Color = ThemeProvider.colorFromMap(colorsByName, "base-8");
        this.base9Color = ThemeProvider.colorFromMap(colorsByName, "base-9");

        this.accent0Color = ThemeProvider.colorFromMap(colorsByName, "accent-0");
        this.accent1Color = ThemeProvider.colorFromMap(colorsByName, "accent-1");
        this.accent2Color = ThemeProvider.colorFromMap(colorsByName, "accent-2");
        this.accent3Color = ThemeProvider.colorFromMap(colorsByName, "accent-3");
        this.accent4Color = ThemeProvider.colorFromMap(colorsByName, "accent-4");
        this.accent5Color = ThemeProvider.colorFromMap(colorsByName, "accent-5");
        this.accent6Color = ThemeProvider.colorFromMap(colorsByName, "accent-6");
        this.accent7Color = ThemeProvider.colorFromMap(colorsByName, "accent-7");
        this.accent8Color = ThemeProvider.colorFromMap(colorsByName, "accent-8");
        this.accent9Color = ThemeProvider.colorFromMap(colorsByName, "accent-9");

        this.success0Color = ThemeProvider.colorFromMap(colorsByName, "success-0");
        this.success1Color = ThemeProvider.colorFromMap(colorsByName, "success-1");
        this.success2Color = ThemeProvider.colorFromMap(colorsByName, "success-2");
        this.success3Color = ThemeProvider.colorFromMap(colorsByName, "success-3");
        this.success4Color = ThemeProvider.colorFromMap(colorsByName, "success-4");
        this.success5Color = ThemeProvider.colorFromMap(colorsByName, "success-5");
        this.success6Color = ThemeProvider.colorFromMap(colorsByName, "success-6");
        this.success7Color = ThemeProvider.colorFromMap(colorsByName, "success-7");
        this.success8Color = ThemeProvider.colorFromMap(colorsByName, "success-8");
        this.success9Color = ThemeProvider.colorFromMap(colorsByName, "success-9");

        this.warning0Color = ThemeProvider.colorFromMap(colorsByName, "warning-0");
        this.warning1Color = ThemeProvider.colorFromMap(colorsByName, "warning-1");
        this.warning2Color = ThemeProvider.colorFromMap(colorsByName, "warning-2");
        this.warning3Color = ThemeProvider.colorFromMap(colorsByName, "warning-3");
        this.warning4Color = ThemeProvider.colorFromMap(colorsByName, "warning-4");
        this.warning5Color = ThemeProvider.colorFromMap(colorsByName, "warning-5");
        this.warning6Color = ThemeProvider.colorFromMap(colorsByName, "warning-6");
        this.warning7Color = ThemeProvider.colorFromMap(colorsByName, "warning-7");
        this.warning8Color = ThemeProvider.colorFromMap(colorsByName, "warning-8");
        this.warning9Color = ThemeProvider.colorFromMap(colorsByName, "warning-9");

        this.danger0Color = ThemeProvider.colorFromMap(colorsByName, "danger-0");
        this.danger1Color = ThemeProvider.colorFromMap(colorsByName, "danger-1");
        this.danger2Color = ThemeProvider.colorFromMap(colorsByName, "danger-2");
        this.danger3Color = ThemeProvider.colorFromMap(colorsByName, "danger-3");
        this.danger4Color = ThemeProvider.colorFromMap(colorsByName, "danger-4");
        this.danger5Color = ThemeProvider.colorFromMap(colorsByName, "danger-5");
        this.danger6Color = ThemeProvider.colorFromMap(colorsByName, "danger-6");
        this.danger7Color = ThemeProvider.colorFromMap(colorsByName, "danger-7");
        this.danger8Color = ThemeProvider.colorFromMap(colorsByName, "danger-8");
        this.danger9Color = ThemeProvider.colorFromMap(colorsByName, "danger-9");

        this.defaultFgColor = ThemeProvider.colorFromMap(colorsByName, "fg-default");
        this.mutedFgColor = ThemeProvider.colorFromMap(colorsByName, "fg-muted");
        this.subtleFgColor = ThemeProvider.colorFromMap(colorsByName, "fg-subtle");
        this.emphasisFgColor = ThemeProvider.colorFromMap(colorsByName, "fg-emphasis");

        this.defaultBgColor = ThemeProvider.colorFromMap(colorsByName, "bg-default");
        this.overlayBgColor = ThemeProvider.colorFromMap(colorsByName, "bg-overlay");
        this.subtleBgColor = ThemeProvider.colorFromMap(colorsByName, "bg-subtle");
        this.insetBgColor = ThemeProvider.colorFromMap(colorsByName, "bg-inset");
        this.extraBgColor = ThemeProvider.colorFromMap(colorsByName, "bg-extra");

        this.defaultBorderColor = ThemeProvider.colorFromMap(colorsByName, "border-default");
        this.mutedBorderColor = ThemeProvider.colorFromMap(colorsByName, "border-muted");
        this.subtleBorderColor = ThemeProvider.colorFromMap(colorsByName, "border-subtle");

        this.defaultShadowColor = ThemeProvider.colorFromMap(colorsByName, "shadow-default");
        this.emphasisPlusNeutralColor = ThemeProvider.colorFromMap(colorsByName, "neutral-emphasis-plus");
        this.emphasisNeutralColor = ThemeProvider.colorFromMap(colorsByName, "neutral-emphasis");
        this.mutedNeutralColor = ThemeProvider.colorFromMap(colorsByName, "neutral-muted");
        this.subtleNeutralColor = ThemeProvider.colorFromMap(colorsByName, "neutral-subtle");

        this.fgAccentColor = ThemeProvider.colorFromMap(colorsByName, "accent-fg");
        this.emphasisAccentColor = ThemeProvider.colorFromMap(colorsByName, "accent-emphasis");
        this.mutedAccentColor = ThemeProvider.colorFromMap(colorsByName, "accent-muted");
        this.subtleAccentColor = ThemeProvider.colorFromMap(colorsByName, "accent-subtle");

        this.fgWarningColor = ThemeProvider.colorFromMap(colorsByName, "warning-fg");
        this.emphasisWarningColor = ThemeProvider.colorFromMap(colorsByName, "warning-emphasis");
        this.mutedWarningColor = ThemeProvider.colorFromMap(colorsByName, "warning-muted");
        this.subtleWarningColor = ThemeProvider.colorFromMap(colorsByName, "warning-subtle");

        this.fgSuccessColor = ThemeProvider.colorFromMap(colorsByName, "success-fg");
        this.emphasisSuccessColor = ThemeProvider.colorFromMap(colorsByName, "success-emphasis");
        this.mutedSuccessColor = ThemeProvider.colorFromMap(colorsByName, "success-muted");
        this.subtleSuccessColor = ThemeProvider.colorFromMap(colorsByName, "success-subtle");

        this.fgDangerColor = ThemeProvider.colorFromMap(colorsByName, "danger-fg");
        this.emphasisDangerColor = ThemeProvider.colorFromMap(colorsByName, "danger-emphasis");
        this.mutedDangerColor = ThemeProvider.colorFromMap(colorsByName, "danger-muted");
        this.subtleDangerColor = ThemeProvider.colorFromMap(colorsByName, "danger-subtle");

        this.chart1Color = ThemeProvider.colorFromMap(colorsByName, "chart-1");
        this.chart2Color = ThemeProvider.colorFromMap(colorsByName, "chart-2");
        this.chart3Color = ThemeProvider.colorFromMap(colorsByName, "chart-3");
        this.chart4Color = ThemeProvider.colorFromMap(colorsByName, "chart-4");
        this.chart5Color = ThemeProvider.colorFromMap(colorsByName, "chart-5");
        this.chart6Color = ThemeProvider.colorFromMap(colorsByName, "chart-6");
        this.chart7Color = ThemeProvider.colorFromMap(colorsByName, "chart-7");
        this.chart8Color = ThemeProvider.colorFromMap(colorsByName, "chart-8");
        this.chart1Alpha70Color = ThemeProvider.colorFromMap(colorsByName, "chart-1-alpha70");
        this.chart2Alpha70Color = ThemeProvider.colorFromMap(colorsByName, "chart-2-alpha70");
        this.chart3Alpha70Color = ThemeProvider.colorFromMap(colorsByName, "chart-3-alpha70");
        this.chart4Alpha70Color = ThemeProvider.colorFromMap(colorsByName, "chart-4-alpha70");
        this.chart5Alpha70Color = ThemeProvider.colorFromMap(colorsByName, "chart-5-alpha70");
        this.chart6Alpha70Color = ThemeProvider.colorFromMap(colorsByName, "chart-6-alpha70");
        this.chart7Alpha70Color = ThemeProvider.colorFromMap(colorsByName, "chart-7-alpha70");
        this.chart8Alpha70Color = ThemeProvider.colorFromMap(colorsByName, "chart-8-alpha70");
        this.chart1Alpha20Color = ThemeProvider.colorFromMap(colorsByName, "chart-1-alpha20");
        this.chart2Alpha20Color = ThemeProvider.colorFromMap(colorsByName, "chart-2-alpha20");
        this.chart3Alpha20Color = ThemeProvider.colorFromMap(colorsByName, "chart-3-alpha20");
        this.chart4Alpha20Color = ThemeProvider.colorFromMap(colorsByName, "chart-4-alpha20");
        this.chart5Alpha20Color = ThemeProvider.colorFromMap(colorsByName, "chart-5-alpha20");
        this.chart6Alpha20Color = ThemeProvider.colorFromMap(colorsByName, "chart-6-alpha20");
        this.chart7Alpha20Color = ThemeProvider.colorFromMap(colorsByName, "chart-7-alpha20");
        this.chart8Alpha20Color = ThemeProvider.colorFromMap(colorsByName, "chart-8-alpha20");

        this.selectionFgColor = ThemeProvider.colorFromMap(colorsByName, "fg-selection");
        this.selectionBgColor = ThemeProvider.colorFromMap(colorsByName, "bg-selection");
        this.selectionBorderColor = ThemeProvider.colorFromMap(colorsByName, "border-selection");
        this.foundFgColor = ThemeProvider.colorFromMap(colorsByName, "fg-found");
        this.foundBgColor = ThemeProvider.colorFromMap(colorsByName, "bg-found");
        this.hyperlinkFgColor = ThemeProvider.colorFromMap(colorsByName, "fg-link");
        this.hyperlinkBgColor = ThemeProvider.colorFromMap(colorsByName, "bg-link");
    }

    @Override
    public int getDarkColor() {
        return darkColor;
    }

    @Override
    public int getLightColor() {
        return lightColor;
    }

    @Override
    public int getBase0Color() {
        return base0Color;
    }

    @Override
    public int getBase1Color() {
        return base1Color;
    }

    @Override
    public int getBase2Color() {
        return base2Color;
    }

    @Override
    public int getBase3Color() {
        return base3Color;
    }

    @Override
    public int getBase4Color() {
        return base4Color;
    }

    @Override
    public int getBase5Color() {
        return base5Color;
    }

    @Override
    public int getBase6Color() {
        return base6Color;
    }

    @Override
    public int getBase7Color() {
        return base7Color;
    }

    @Override
    public int getBase8Color() {
        return base8Color;
    }

    @Override
    public int getBase9Color() {
        return base9Color;
    }

    @Override
    public int getAccent0Color() {
        return accent0Color;
    }

    @Override
    public int getAccent1Color() {
        return accent1Color;
    }

    @Override
    public int getAccent2Color() {
        return accent2Color;
    }

    @Override
    public int getAccent3Color() {
        return accent3Color;
    }

    @Override
    public int getAccent4Color() {
        return accent4Color;
    }

    @Override
    public int getAccent5Color() {
        return accent5Color;
    }

    @Override
    public int getAccent6Color() {
        return accent6Color;
    }

    @Override
    public int getAccent7Color() {
        return accent7Color;
    }

    @Override
    public int getAccent8Color() {
        return accent8Color;
    }

    @Override
    public int getAccent9Color() {
        return accent9Color;
    }

    @Override
    public int getSuccess0Color() {
        return success0Color;
    }

    @Override
    public int getSuccess1Color() {
        return success1Color;
    }

    @Override
    public int getSuccess2Color() {
        return success2Color;
    }

    @Override
    public int getSuccess3Color() {
        return success3Color;
    }

    @Override
    public int getSuccess4Color() {
        return success4Color;
    }

    @Override
    public int getSuccess5Color() {
        return success5Color;
    }

    @Override
    public int getSuccess6Color() {
        return success6Color;
    }

    @Override
    public int getSuccess7Color() {
        return success7Color;
    }

    @Override
    public int getSuccess8Color() {
        return success8Color;
    }

    @Override
    public int getSuccess9Color() {
        return success9Color;
    }

    @Override
    public int getWarning0Color() {
        return warning0Color;
    }

    @Override
    public int getWarning1Color() {
        return warning1Color;
    }

    @Override
    public int getWarning2Color() {
        return warning2Color;
    }

    @Override
    public int getWarning3Color() {
        return warning3Color;
    }

    @Override
    public int getWarning4Color() {
        return warning4Color;
    }

    @Override
    public int getWarning5Color() {
        return warning5Color;
    }

    @Override
    public int getWarning6Color() {
        return warning6Color;
    }

    @Override
    public int getWarning7Color() {
        return warning7Color;
    }

    @Override
    public int getWarning8Color() {
        return warning8Color;
    }

    @Override
    public int getWarning9Color() {
        return warning9Color;
    }

    @Override
    public int getDanger0Color() {
        return danger0Color;
    }

    @Override
    public int getDanger1Color() {
        return danger1Color;
    }

    @Override
    public int getDanger2Color() {
        return danger2Color;
    }

    @Override
    public int getDanger3Color() {
        return danger3Color;
    }

    @Override
    public int getDanger4Color() {
        return danger4Color;
    }

    @Override
    public int getDanger5Color() {
        return danger5Color;
    }

    @Override
    public int getDanger6Color() {
        return danger6Color;
    }

    @Override
    public int getDanger7Color() {
        return danger7Color;
    }

    @Override
    public int getDanger8Color() {
        return danger8Color;
    }

    @Override
    public int getDanger9Color() {
        return danger9Color;
    }

    @Override
    public int getDefaultFgColor() {
        return defaultFgColor;
    }

    @Override
    public int getMutedFgColor() {
        return mutedFgColor;
    }

    @Override
    public int getSubtleFgColor() {
        return subtleFgColor;
    }

    @Override
    public int getEmphasisFgColor() {
        return emphasisFgColor;
    }

    @Override
    public int getDefaultBgColor() {
        return defaultBgColor;
    }

    @Override
    public int getOverlayBgColor() {
        return overlayBgColor;
    }

    @Override
    public int getSubtleBgColor() {
        return subtleBgColor;
    }

    @Override
    public int getInsetBgColor() {
        return insetBgColor;
    }

    @Override
    public int getExtraBgColor() {
        return extraBgColor;
    }

    @Override
    public int getDefaultBorderColor() {
        return defaultBorderColor;
    }

    @Override
    public int getMutedBorderColor() {
        return mutedBorderColor;
    }

    @Override
    public int getSubtleBorderColor() {
        return subtleBorderColor;
    }

    @Override
    public int getDefaultShadowColor() {
        return defaultShadowColor;
    }

    @Override
    public int getEmphasisPlusNeutralColor() {
        return emphasisPlusNeutralColor;
    }

    @Override
    public int getEmphasisNeutralColor() {
        return emphasisNeutralColor;
    }

    @Override
    public int getMutedNeutralColor() {
        return mutedNeutralColor;
    }

    @Override
    public int getSubtleNeutralColor() {
        return subtleNeutralColor;
    }

    @Override
    public int getFgAccentColor() {
        return fgAccentColor;
    }

    @Override
    public int getEmphasisAccentColor() {
        return emphasisAccentColor;
    }

    @Override
    public int getMutedAccentColor() {
        return mutedAccentColor;
    }

    @Override
    public int getSubtleAccentColor() {
        return subtleAccentColor;
    }

    @Override
    public int getFgWarningColor() {
        return fgWarningColor;
    }

    @Override
    public int getEmphasisWarningColor() {
        return emphasisWarningColor;
    }

    @Override
    public int getMutedWarningColor() {
        return mutedWarningColor;
    }

    @Override
    public int getSubtleWarningColor() {
        return subtleWarningColor;
    }

    @Override
    public int getFgSuccessColor() {
        return fgSuccessColor;
    }

    @Override
    public int getEmphasisSuccessColor() {
        return emphasisSuccessColor;
    }

    @Override
    public int getMutedSuccessColor() {
        return mutedSuccessColor;
    }

    @Override
    public int getSubtleSuccessColor() {
        return subtleSuccessColor;
    }

    @Override
    public int getFgDangerColor() {
        return fgDangerColor;
    }

    @Override
    public int getEmphasisDangerColor() {
        return emphasisDangerColor;
    }

    @Override
    public int getMutedDangerColor() {
        return mutedDangerColor;
    }

    @Override
    public int getSubtleDangerColor() {
        return subtleDangerColor;
    }

    @Override
    public int getChart1Color() {
        return chart1Color;
    }

    @Override
    public int getChart2Color() {
        return chart2Color;
    }

    @Override
    public int getChart3Color() {
        return chart3Color;
    }

    @Override
    public int getChart4Color() {
        return chart4Color;
    }

    @Override
    public int getChart5Color() {
        return chart5Color;
    }

    @Override
    public int getChart6Color() {
        return chart6Color;
    }

    @Override
    public int getChart7Color() {
        return chart7Color;
    }

    @Override
    public int getChart8Color() {
        return chart8Color;
    }

    @Override
    public int getChart1Alpha70Color() {
        return chart1Alpha70Color;
    }

    @Override
    public int getChart2Alpha70Color() {
        return chart2Alpha70Color;
    }

    @Override
    public int getChart3Alpha70Color() {
        return chart3Alpha70Color;
    }

    @Override
    public int getChart4Alpha70Color() {
        return chart4Alpha70Color;
    }

    @Override
    public int getChart5Alpha70Color() {
        return chart5Alpha70Color;
    }

    @Override
    public int getChart6Alpha70Color() {
        return chart6Alpha70Color;
    }

    @Override
    public int getChart7Alpha70Color() {
        return chart7Alpha70Color;
    }

    @Override
    public int getChart8Alpha70Color() {
        return chart8Alpha70Color;
    }

    @Override
    public int getChart1Alpha20Color() {
        return chart1Alpha20Color;
    }

    @Override
    public int getChart2Alpha20Color() {
        return chart2Alpha20Color;
    }

    @Override
    public int getChart3Alpha20Color() {
        return chart3Alpha20Color;
    }

    @Override
    public int getChart4Alpha20Color() {
        return chart4Alpha20Color;
    }

    @Override
    public int getChart5Alpha20Color() {
        return chart5Alpha20Color;
    }

    @Override
    public int getChart6Alpha20Color() {
        return chart6Alpha20Color;
    }

    @Override
    public int getChart7Alpha20Color() {
        return chart7Alpha20Color;
    }

    @Override
    public int getChart8Alpha20Color() {
        return chart8Alpha20Color;
    }

    @Override
    public int getSelectionFgColor() {
        return selectionFgColor;
    }

    @Override
    public int getSelectionBgColor() {
        return selectionBgColor;
    }

    @Override
    public int getSelectionBorderColor() {
        return selectionBorderColor;
    }

    @Override
    public int getFoundFgColor() {
        return foundFgColor;
    }

    @Override
    public int getFoundBgColor() {
        return foundBgColor;
    }

    @Override
    public int getHyperlinkFgColor() {
        return hyperlinkFgColor;
    }

    @Override
    public int getHyperlinkBgColor() {
        return hyperlinkBgColor;
    }
}
