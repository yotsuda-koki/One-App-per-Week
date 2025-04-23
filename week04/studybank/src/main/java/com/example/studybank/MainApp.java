package com.example.studybank;



import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.media.AudioClip;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.*;
import java.net.URL;
import java.time.LocalDate;
import java.time.temporal.WeekFields;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import org.json.JSONObject;

public class MainApp extends Application {
	
	private int seconds = 0;
	private Timeline timeline;

    @Override
    public void start(Stage primaryStage) {
    	StackPane contentArea = new StackPane();
    	
    	VBox timerView = createTimerView();
//    	VBox countdownView = createCountdownTimerView();
//    	VBox summary = createSummaryView();
//    	VBox graphView = createGraphView();
    	
    	Button toStopwatchButton = new Button("Stop watch");
    	Button toCountdownButton = new Button("Timer");
    	Button toGraphButton = new Button("Graph");
    	Button toSummaryButton = new Button("Summary");
    	
    	toStopwatchButton.setOnAction(_ -> {
    		contentArea.getChildren().setAll(createTimerView());
    	});
    	
    	toCountdownButton.setOnAction(_ -> {
    		contentArea.getChildren().setAll(createCountdownTimerView());
    	});
    	
    	toGraphButton.setOnAction(_ -> {
    		contentArea.getChildren().setAll(createGraphView());
    	});
    	
    	toSummaryButton.setOnAction(_ -> {
    		contentArea.getChildren().setAll(createSummaryView());
    	});
    	
    	HBox navBar = new HBox(10, toStopwatchButton, toCountdownButton, toGraphButton, toSummaryButton);
    	navBar.setStyle("-fx-alignment: center; -fx-padding: 10;");
    	
    	VBox root = new VBox(navBar, contentArea);
    	root.setStyle("-fx-padding: 20;");
    	
    	contentArea.getChildren().add(timerView);
    	
    	Scene scene = new Scene(root, 500, 400);
    	primaryStage.setScene(scene);
    	primaryStage.setTitle("StudyBank");
    	primaryStage.show();
    }
    
    private String formatTime(int totalSeconds) {
    	int hours = totalSeconds / 3600;
    	int minutes = (totalSeconds % 3600) / 60;
    	int seconds = totalSeconds % 60;
    	return String.format("%02d:%02d:%02d", hours, minutes, seconds);
    }
    
    private void saveTodayStudyTime(int seconds) {
    	try {
    		File file = new File("study-log.json");
    		JSONObject json;
    		
    		if(file.exists()) {
    			BufferedReader reader = new BufferedReader(new FileReader(file));
    			StringBuilder sb = new StringBuilder();
    			String line;
    			while ((line = reader.readLine()) != null) sb.append(line);
    			reader.close();
    			json = new JSONObject(sb.toString());
    		} else {
    			json = new JSONObject();
    		}
    		
    		String today = LocalDate.now().toString();
    		int prev = json.optInt(today, 0);
    		json.put(today, prev + seconds);
    		
    		BufferedWriter writer = new BufferedWriter(new FileWriter(file));
    		writer.write(json.toString(2));
    		writer.close();
    	} catch (IOException e) {
    		e.printStackTrace();
    	}
    }
    
    private VBox createTimerView() {
    	Label title = new Label("Stopwatch");
    	title.setStyle("""
    	        -fx-font-size: 22px;
    	        -fx-font-weight: bold;
    	        -fx-text-fill: #333;
    	        -fx-font-family: 'Segoe UI', 'Roboto', 'Helvetica Neue', sans-serif;
    	    """);
    	
    	Font digitalFont = Font.loadFont(
    			getClass().getResourceAsStream("/font/digital-7 (mono).ttf"), 48
    			);
    	
    	Label timeLabel = new Label(formatTime(seconds));
    	timeLabel.setFont(digitalFont);

    	timeLabel.setStyle("-fx-text-fill: #4caf50; -fx-padding: 10;");

        
    	Button startButton = new Button("Start");
    	Button stopButton = new Button("Stop");
    	Button resetButton = new Button("Reset");
    	
        String btnStyle = """
                -fx-background-color: #4caf50;
                -fx-text-fill: white;
                -fx-font-size: 14px;
                -fx-font-weight: bold;
                -fx-pref-width: 100px;
                -fx-font-family: 'Segoe UI', 'Roboto', 'Helvetica Neue', sans-serif;
            """;
        
        startButton.setStyle(btnStyle);
        stopButton.setStyle(btnStyle);
        resetButton.setStyle(btnStyle);
    	
    	timeline = new Timeline(new KeyFrame(Duration.seconds(1), _ -> {
    		seconds++;
    		timeLabel.setText(formatTime(seconds));
    	}));
    	timeline.setCycleCount(Timeline.INDEFINITE);
    	
    	startButton.setOnAction(_ -> timeline.play());
    	stopButton.setOnAction(_ -> timeline.pause());
    	resetButton.setOnAction(_ -> {
    		timeline.stop();
    		saveTodayStudyTime(seconds);
    		seconds = 0;
    		timeLabel.setText(formatTime(seconds));
    	});
    	
    	HBox buttonRow = new HBox(10, timeLabel, startButton, stopButton, resetButton);
    	buttonRow.setStyle("-fx-alignment: center;");
    	
    	VBox timerView = new VBox(20, title, timeLabel, buttonRow);
        timerView.setStyle("""
                -fx-padding: 40;
                -fx-alignment: center;
                -fx-background-color: #f5f5f5;
            """);
    	return timerView;
    }
    
    private VBox createCountdownTimerView() {
    	Label title = new Label("Timer");
    	title.setStyle("""
    	        -fx-font-size: 22px;
    	        -fx-font-weight: bold;
    	        -fx-text-fill: #333;
    	        -fx-font-family: 'Segoe UI', 'Roboto', 'Helvetica Neue', sans-serif;
    	    """);
    	
    	final IntegerProperty remainingSeconds = new SimpleIntegerProperty(25 * 60);
    	
    	Label timeLabel = new Label(formatTime(remainingSeconds.get()));
    	timeLabel.setStyle("""
    	        -fx-text-fill: #4caf50;
    	        -fx-padding: 10;
    	    """);
    	
    	Font degitalFont = Font.loadFont(getClass().getResourceAsStream("/font/digital-7 (mono).ttf"), 36);
    	timeLabel.setFont(degitalFont);
    	
    	Timeline countdown = new Timeline();
    	countdown.setCycleCount(Timeline.INDEFINITE);
    	countdown.getKeyFrames().add(new KeyFrame(Duration.seconds(1), _ -> {
    		int current = remainingSeconds.get();
    		if(current <= 1) {
    			countdown.stop();
    			remainingSeconds.set(0);
    			timeLabel.setText("Finish!");
    			
    			URL soundUrl = getClass().getResource("/sound/alarm.mp3");
    			if(soundUrl != null) {
    				AudioClip clip = new AudioClip(soundUrl.toString());
    				clip.play();
    			}
    			
    			Alert alert = new Alert(AlertType.INFORMATION);
    			alert.setTitle("Timer finish");
    			alert.setHeaderText(null);
    			alert.setContentText("Good job for your tasks");
    			alert.showAndWait();
    		} else {
    			remainingSeconds.set(current - 1);
    			timeLabel.setText(formatTime(current - 1));
    		}
    	}));
    	
    	Label timeSelectLabel = new Label("Set Time (min)");
    	timeSelectLabel.setStyle("""
    			-fx-font-size: 14px;
    			-fx-font-family: 'Segoe UI', 'Roboto', 'Helvetica Neue', sans-serif;
    			-fx-text-fill: #333;
    			""");
    	
    	ComboBox<Integer> timeSelect = new ComboBox<>();
    	for(int i = 5; i <= 180; i += 5) {
    		timeSelect.getItems().add(i);
    	}
    	timeSelect.setValue(25);
        timeSelect.setStyle("""
		    -fx-font-size: 14px;
		    -fx-font-family: 'Segoe UI', 'Roboto', 'Helvetica Neue', sans-serif;
		    -fx-pref-width: 150px;
		    -fx-padding: 5 10 5 10;
            """);
    	
    	timeSelect.setOnAction(_ -> {
    		int selectedMinutes = timeSelect.getValue();
    		countdown.stop();
    		remainingSeconds.set(selectedMinutes * 60);
    		timeLabel.setText(formatTime(remainingSeconds.get()));
    	});
    	
    	Button start = new Button("Start");
    	Button pause = new Button("Pause");
    	Button reset = new Button("Reset");
    	
    	String btnStyle = """
    			-fx-background-color: #4caf50;
    			-fx-text-fill: white;
    			-fx-font-size: 14px;
    			-fx-font-weight: bold;
    			-fx-pref-width: 100px;
    			-fx-font-family: 'Segoe UI', 'Roboto', 'Helvetica Neue', sans-serif;
    			""";
    	
    	start.setStyle(btnStyle);
    	pause.setStyle(btnStyle);
    	reset.setStyle(btnStyle);
    	
    	start.setOnAction(_ -> countdown.play());
    	pause.setOnAction(_ -> countdown.pause());
    	reset.setOnAction(_ -> {
    		countdown.stop();
    		int selectedMinutes = timeSelect.getValue();
    		remainingSeconds.set(selectedMinutes * 60);
    		timeLabel.setText(formatTime(remainingSeconds.get()));
    	});
    	
    	HBox buttons = new HBox(10, start, pause, reset);
    	buttons.setAlignment(Pos.CENTER);
    	
    	VBox timeSelectGroup = new VBox(5, timeSelectLabel, timeSelect);
    	timeSelectGroup.setAlignment(Pos.CENTER);

    	
    	VBox layout = new VBox(20,
    			title,
    			timeLabel,
    			timeSelectGroup,
    			buttons);
    	layout.setAlignment(Pos.CENTER);
    	layout.setStyle("-fx-padding: 40; -fx-background-color: #f5f5f5;");
    	return layout;
    }
    
    private VBox createGraphView() {
        Label title = new Label("Study Time Chart");
        title.setStyle("""
            -fx-font-size: 22px;
            -fx-font-weight: bold;
            -fx-font-family: 'Segoe UI', 'Roboto', 'Helvetica Neue', sans-serif;
            -fx-text-fill: #333;
        """);
        
        CategoryAxis xAxis = new CategoryAxis();
        NumberAxis yAxis = new NumberAxis();
        xAxis.setLabel("Date");
        yAxis.setLabel("Study Time (min)");
        xAxis.setTickLabelRotation(45);

        BarChart<String, Number> barChart = new BarChart<>(xAxis, yAxis);
        barChart.setLegendVisible(false);
        barChart.setTitle("Study Time per Day");
        barChart.setPrefHeight(300);
        barChart.setCategoryGap(10);
        barChart.setBarGap(3);

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Study Time");
        
        LineChart<String, Number> lineChart = new LineChart<>(xAxis, yAxis);
        lineChart.setLegendVisible(false);
        lineChart.setCreateSymbols(false);
        lineChart.setAlternativeRowFillVisible(false);
        lineChart.setAlternativeColumnFillVisible(false);
        lineChart.setHorizontalGridLinesVisible(false);
        lineChart.setVerticalGridLinesVisible(false);
        lineChart.setAnimated(false);
        lineChart.setVerticalZeroLineVisible(false);
        lineChart.setHorizontalZeroLineVisible(false);
        lineChart.setTitle(null);
        
        XYChart.Series<String, Number> avgLine = new XYChart.Series<>();

        double totalMinutes = 0;
        int dataCount = 0;
        List<String> orderedDateStrings = new ArrayList<>();
        
        try {
        	File file = new File("study-log.json");
        	if(file.exists()) {
        		BufferedReader reader = new BufferedReader(new FileReader(file));
        		StringBuilder sb = new StringBuilder();
        		String line;
        		while((line = reader.readLine()) != null) sb.append(line);
        		reader.close();
        		
        		JSONObject json = new JSONObject(sb.toString());
        		
        		List<LocalDate> sortedDates = new ArrayList<>();
        		for (String dateStr : json.keySet()) {
        		    sortedDates.add(LocalDate.parse(dateStr));
        		}
        		Collections.sort(sortedDates);
                
                for (LocalDate date : sortedDates) {
                    String dateStr = date.toString();
                    orderedDateStrings.add(dateStr);
                    int seconds = json.getInt(dateStr);
                    double minutes = seconds / 60.0;
                    series.getData().add(new XYChart.Data<>(dateStr, minutes));
                    totalMinutes += minutes;
                    dataCount++;
                }

                xAxis.setCategories(javafx.collections.FXCollections.observableArrayList(orderedDateStrings));

        		
        		if(dataCount > 0) {
        			double avg = totalMinutes / dataCount;
        			for(LocalDate date : sortedDates) {
        		        String dateStr = date.toString();
        		        avgLine.getData().add(new XYChart.Data<>(dateStr, avg));
        			}
        		}
        	}
        }catch(Exception e) {
        	e.printStackTrace();
        }
        
        VBox graphView;
        
        if(dataCount == 0) {
        	Label noDataLabel = new Label("No data");
            noDataLabel.setStyle("""
                    -fx-font-size: 16px;
                    -fx-text-fill: #999;
                    -fx-font-family: 'Segoe UI', 'Roboto', 'Helvetica Neue', sans-serif;
                """);
            graphView = new VBox(20, title, noDataLabel);
        } else {
        	barChart.getData().add(series);
        	lineChart.getData().add(avgLine);
        	
            StackPane chartStack = new StackPane(barChart, lineChart);
            graphView = new VBox(20, title, chartStack);
        }
        
        graphView.setStyle("""
            -fx-alignment: center;
            -fx-padding: 40;
            -fx-background-color: #f5f5f5;
        """);
        
        return graphView;
    }
    
    private VBox createSummaryView() {
    	Label todayLabel = new Label("Today's time");
    	Label weekLable = new Label("This week's time");
    	Label monthLabel = new Label("This month's time");
    	Label yearLabel = new Label("This year's time");
    	
    	int todaySec = 0, weekSec = 0, monthSec = 0, yearSec = 0;
    	
    	try {
    		File file = new File("study-log.json");
    		if(file.exists()) {
    			BufferedReader reader = new BufferedReader(new FileReader(file));
    			StringBuilder sb = new StringBuilder();
    			String line;
    			while((line = reader.readLine()) != null) sb.append(line);
    			reader.close();
    			
    			JSONObject json = new JSONObject(sb.toString());
    			
    			LocalDate now = LocalDate.now();
    			WeekFields weekFields = WeekFields.of(Locale.getDefault());
    			int currentWeek = now.get(weekFields.weekOfWeekBasedYear());
    			int currentMonth = now.getMonthValue();
    			int currentYear = now.getYear();
    			
    			for(String key : json.keySet()) {
    				LocalDate date = LocalDate.parse(key);
    				int seconds = json.getInt(key);
    				
    				if(date.equals(now)) todaySec += seconds;
    				if(date.getYear() == currentYear) {
    					yearSec += seconds;
    					if(date.getMonthValue() == currentMonth) {
    						monthSec += seconds;
    					}
    					if(date.get(weekFields.weekOfWeekBasedYear()) == currentWeek) {
    						weekSec += seconds;
    					}
    				}
    			}
    		}
		}catch(Exception e) {
    			e.printStackTrace();
    	}
    	
    	todayLabel.setText("Today's time : " + formatTime(todaySec));
    	weekLable.setText("This week's time : " + formatTime(weekSec));
    	monthLabel.setText("This month's time : " + formatTime(monthSec));
    	yearLabel.setText("This year's time : " + formatTime(yearSec));
    	
    	VBox layout = new VBox(10, todayLabel, weekLable, monthLabel, yearLabel);
    	layout.setAlignment(Pos.CENTER);
    	return layout;
    }

    public static void main(String[] args) {
        launch(args);
    }
}
