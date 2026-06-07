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

package com.techsenger.tabshell.demo.window;

import com.techsenger.tabshell.core.window.AbstractWindowFxView;
import com.techsenger.tabshell.demo.page.Text;
import com.techsenger.tabshell.material.style.Spacing;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.chart.AreaChart;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.BubbleChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.ScatterChart;
import javafx.scene.chart.StackedAreaChart;
import javafx.scene.chart.StackedBarChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TextArea;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

/**
 *
 * @author Pavel Castornii
 */
public class DemoWindowFxView extends AbstractWindowFxView<DemoWindowPresenter> implements DemoWindowView {

    private final int contentIndex;

    private final TextArea textArea = new TextArea(Text.INSTANCE);

    private final CheckBox alwaysOnTopCheckBox = new CheckBox("Always On Top");

    private final CheckBox resizableCheckBox = new CheckBox("Resizable");

    private final CheckBox maximizableCheckBox = new CheckBox("Maximizable");

    private final CheckBox closableCheckBox = new CheckBox("Closable");

    public DemoWindowFxView(int contentIndex) {
        this.contentIndex = contentIndex;
    }

    @Override
    public void requestFocus() {
        textArea.requestFocus();
    }

    @Override
    protected void build() {
        super.build();
        textArea.setWrapText(true);
        VBox.setVgrow(textArea, Priority.ALWAYS);
        closableCheckBox.setSelected(true);
        var hBox = new HBox(alwaysOnTopCheckBox, resizableCheckBox, maximizableCheckBox, closableCheckBox);
        hBox.setSpacing(Spacing.getHorizontal());
        getContentBox().getChildren().addAll(textArea, hBox);
        getContentBox().setPadding(new Insets(Spacing.getVertical(), Spacing.getHorizontal(),
                Spacing.getVertical(), Spacing.getHorizontal()));
        getContentBox().setSpacing(Spacing.getVertical());
//        switch (contentIndex) {
//            case 0 -> buildPieChart();
//            case 1 -> buildBarChart();
//            case 2 -> buildStackedBarChart();
//            case 3 -> buildLineChart();
//            case 4 -> buildAreaChart();
//            case 5 -> buildStackedAreaChart();
//            case 6 -> buildScatterChart();
//            case 7 -> buildBubbleChart();
//        }
    }

    @Override
    protected void addListeners() {
        super.addListeners();
        alwaysOnTopCheckBox.selectedProperty()
                .addListener((ov, oldV, newV) -> getPresenter().onAlwaysOnTopSelected(newV));
        maximizableCheckBox.selectedProperty()
                .addListener((ov, oldV, newV) -> getPresenter().onMaximizableSelected(newV));
        resizableCheckBox.selectedProperty()
                .addListener((ov, oldV, newV) -> getPresenter().onResizableSelected(newV));
        closableCheckBox.selectedProperty()
                .addListener((ov, oldV, newV) -> getPresenter().onClosableSelected(newV));
    }

    private void buildPieChart() {
        var data = FXCollections.observableArrayList(
                new PieChart.Data("Java", 40),
                new PieChart.Data("Python", 30),
                new PieChart.Data("C++", 20),
                new PieChart.Data("Other", 10));
        var chart = new PieChart(data);
        chart.setTitle("Languages");
        VBox.setVgrow(chart, Priority.ALWAYS);
        getContentBox().getChildren().add(chart);
    }

    private void buildBarChart() {
        var xAxis = new CategoryAxis();
        var yAxis = new NumberAxis();
        var chart = new BarChart<>(xAxis, yAxis);
        chart.setTitle("Sales");
        var series = new XYChart.Series<String, Number>();
        series.setName("2024");
        series.getData().addAll(
                new XYChart.Data<>("Q1", 120),
                new XYChart.Data<>("Q2", 200),
                new XYChart.Data<>("Q3", 150),
                new XYChart.Data<>("Q4", 180));
        chart.getData().add(series);
        VBox.setVgrow(chart, Priority.ALWAYS);
        getContentBox().getChildren().add(chart);
    }

    private void buildStackedBarChart() {
        var xAxis = new CategoryAxis();
        var yAxis = new NumberAxis();
        var chart = new StackedBarChart<>(xAxis, yAxis);
        chart.setTitle("Stacked Sales");
        var s1 = new XYChart.Series<String, Number>();
        s1.setName("Product A");
        s1.getData().addAll(
                new XYChart.Data<>("Q1", 80),
                new XYChart.Data<>("Q2", 120),
                new XYChart.Data<>("Q3", 90),
                new XYChart.Data<>("Q4", 110));
        var s2 = new XYChart.Series<String, Number>();
        s2.setName("Product B");
        s2.getData().addAll(
                new XYChart.Data<>("Q1", 40),
                new XYChart.Data<>("Q2", 80),
                new XYChart.Data<>("Q3", 60),
                new XYChart.Data<>("Q4", 70));
        chart.getData().addAll(s1, s2);
        VBox.setVgrow(chart, Priority.ALWAYS);
        getContentBox().getChildren().add(chart);
    }

    private void buildLineChart() {
        var xAxis = new NumberAxis();
        var yAxis = new NumberAxis();
        var chart = new LineChart<>(xAxis, yAxis);
        chart.setTitle("Temperature");
        var series = new XYChart.Series<Number, Number>();
        series.setName("2024");
        series.getData().addAll(
                new XYChart.Data<>(1, 5),
                new XYChart.Data<>(2, 8),
                new XYChart.Data<>(3, 15),
                new XYChart.Data<>(4, 20),
                new XYChart.Data<>(5, 25));
        chart.getData().add(series);
        VBox.setVgrow(chart, Priority.ALWAYS);
        getContentBox().getChildren().add(chart);
    }

    private void buildAreaChart() {
        var xAxis = new NumberAxis();
        var yAxis = new NumberAxis();
        var chart = new AreaChart<>(xAxis, yAxis);
        chart.setTitle("Revenue");
        var series = new XYChart.Series<Number, Number>();
        series.setName("2024");
        series.getData().addAll(
                new XYChart.Data<>(1, 300),
                new XYChart.Data<>(2, 450),
                new XYChart.Data<>(3, 400),
                new XYChart.Data<>(4, 600),
                new XYChart.Data<>(5, 550));
        chart.getData().add(series);
        VBox.setVgrow(chart, Priority.ALWAYS);
        getContentBox().getChildren().add(chart);
    }

    private void buildStackedAreaChart() {
        var xAxis = new NumberAxis();
        var yAxis = new NumberAxis();
        var chart = new StackedAreaChart<>(xAxis, yAxis);
        chart.setTitle("Stacked Revenue");
        var s1 = new XYChart.Series<Number, Number>();
        s1.setName("Region A");
        s1.getData().addAll(
                new XYChart.Data<>(1, 200),
                new XYChart.Data<>(2, 300),
                new XYChart.Data<>(3, 250));
        var s2 = new XYChart.Series<Number, Number>();
        s2.setName("Region B");
        s2.getData().addAll(
                new XYChart.Data<>(1, 100),
                new XYChart.Data<>(2, 150),
                new XYChart.Data<>(3, 200));
        chart.getData().addAll(s1, s2);
        VBox.setVgrow(chart, Priority.ALWAYS);
        getContentBox().getChildren().add(chart);
    }

    private void buildScatterChart() {
        var xAxis = new NumberAxis();
        var yAxis = new NumberAxis();
        var chart = new ScatterChart<>(xAxis, yAxis);
        chart.setTitle("Distribution");
        var series = new XYChart.Series<Number, Number>();
        series.setName("Group A");
        series.getData().addAll(
                new XYChart.Data<>(1, 4),
                new XYChart.Data<>(2, 7),
                new XYChart.Data<>(3, 3),
                new XYChart.Data<>(4, 9),
                new XYChart.Data<>(5, 6));
        chart.getData().add(series);
        VBox.setVgrow(chart, Priority.ALWAYS);
        getContentBox().getChildren().add(chart);
    }

    private void buildBubbleChart() {
        var xAxis = new NumberAxis();
        var yAxis = new NumberAxis();
        var chart = new BubbleChart<>(xAxis, yAxis);
        chart.setTitle("Market Size");
        var series = new XYChart.Series<Number, Number>();
        series.setName("Companies");
        // Data: x, y, radius (третий параметр через extraValue)
        series.getData().addAll(
                new XYChart.Data<>(2, 5, 3),
                new XYChart.Data<>(4, 8, 5),
                new XYChart.Data<>(6, 3, 2),
                new XYChart.Data<>(8, 7, 4));
        chart.getData().add(series);
        VBox.setVgrow(chart, Priority.ALWAYS);
        getContentBox().getChildren().add(chart);
    }
}
