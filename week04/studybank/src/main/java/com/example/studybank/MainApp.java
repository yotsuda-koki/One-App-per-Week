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
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.*;
import java.net.URL;
import java.time.LocalDate;
import java.time.temporal.WeekFields;
import java.util.Locale;

import org.json.JSONObject;

public class MainApp extends Application {
	
	private int seconds = 0;
	private Timeline timeline;

    @Override
    public void start(Stage primaryStage) {
    	StackPane contentArea = new StackPane();
    	
    	VBox timerView = createTimerView();
    	VBox countdownView = createCountdownTimerView();
//    	VBox graphView = createGraphView();
    	
    	Button toStopwatchButton = new Button("Stop watch");
    	Button toCountdownButton = new Button("Timer");
    	Button toGraphButton = new Button("Graph");
    	
    	toStopwatchButton.setOnAction(_ -> {
    		contentArea.getChildren().setAll(timerView);
    	});
    	
    	toCountdownButton.setOnAction(_ -> {
    		contentArea.getChildren().setAll(countdownView);
    	});
    	
    	toGraphButton.setOnAction(_ -> {
    		contentArea.getChildren().setAll(createGraphView());
    	});
    	
    	HBox navBar = new HBox(10, toStopwatchButton, toCountdownButton, toGraphButton);
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
    		File file = new File("studu-log.json");
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
    
    private BarChart<String, Number> createStudyBarChart() {
    	CategoryAxis xAxis = new CategoryAxis();
    	NumberAxis yAxis = new NumberAxis();
    	xAxis.setLabel("date(day)");
    	yAxis.setLabel("study time(min)");
    	
    	BarChart<String, Number> barChart = new BarChart<>(xAxis, yAxis);
    	barChart.setTitle("study time/date");
    	
    	XYChart.Series<String, Number> series = new XYChart.Series<>();
    	series.setName("total time");
    	
    	try {
    		File file = new File("study-log.json");
    		if(file.exists()) {
    			BufferedReader reader = new BufferedReader(new FileReader(file));
    			StringBuilder sb = new StringBuilder();
    			String line;
    			while((line = reader.readLine()) != null) sb.append(line);
    			reader.close();
    			
    			JSONObject json = new JSONObject(sb.toString());
    			
    			for(String key : json.keySet()) {
    				int seconds = json.getInt(key);
    				double minutes = seconds / 60.0;
    				series.getData().add(new XYChart.Data<>(key, minutes));
    			}
    		}
    	} catch (Exception e) {
    		e.printStackTrace();
    	}
    	
    	barChart.getData().add(series);
    	return barChart;
    }
    
    private VBox createTimerView() {
    	Label timeLabel = new Label(formatTime(seconds));
    	Button startButton = new Button("Start");
    	Button stopButton = new Button("Stop");
    	Button resetButton = new Button("Reset");
    	
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
    	
    	VBox timerView = new VBox(20, timeLabel, buttonRow);
    	timerView.setStyle("-fx-alignment: center;");
    	return timerView;
    }
    
    private VBox createCountdownTimerView() {
    	Label timeLabel = new Label();
    	timeLabel.setText(formatTime(25 * 60));
    	
    	final IntegerProperty remainingSeconds = new SimpleIntegerProperty(25 * 60);
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
    	
    	ComboBox<Integer> timeSelect = new ComboBox<>();
    	for(int i = 5; i <= 180; i += 5) {
    		timeSelect.getItems().add(i);
    	}
    	timeSelect.setValue(25);
    	
    	timeSelect.setOnAction(_ -> {
    		int selectedMinutes = timeSelect.getValue();
    		countdown.stop();
    		remainingSeconds.set(selectedMinutes * 60);
    		timeLabel.setText(formatTime(remainingSeconds.get()));
    	});
    	
    	Button start = new Button("Start");
    	Button pause = new Button("Pause");
    	Button reset = new Button("Reset");
    	
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
    	
    	VBox layout = new VBox(20,
    			timeLabel,
    			new Label("Time select(m)"), timeSelect,
    			buttons);
    	layout.setAlignment(Pos.CENTER);
    	return layout;
    }
    
    private VBox createGraphView() {
    	BarChart<String, Number> chart = createStudyBarChart();
    	VBox graphView = new VBox(chart);
    	graphView.setStyle("-fx-alignment: center;");
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
    	
    	todayLabel.setText("Today's time" + formatTime(todaySec));
    	weekLable.setText("This week's time" + formatTime(weekSec));
    	monthLabel.setText("This month's time" + formatTime(monthSec));
    	yearLabel.setText("This year's time" + formatTime(yearSec));
    	
    	VBox layout = new VBox(10, todayLabel, weekLable, monthLabel, yearLabel);
    	layout.setAlignment(Pos.CENTER);
    	return layout;
    }

    public static void main(String[] args) {
        launch(args);
    }
}
