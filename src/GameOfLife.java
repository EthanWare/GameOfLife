import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.Slider;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.Stage;
import javafx.util.Duration;

/*
Ethan Ware
CSC 360-002
3/22/17

This program runs the Game of Life. The Game of Life is based on cells becoming
alive, or staying alive based on neighboring cells. It also allows for load
and save feature, allowing you to come back to fully restore previous game.

Rules:
	For a cell that is 'populated':
		-Each cell with one or no neighbors dies, as if by solitude.
		-Each cell with four or more neighbors dies, as if by overpopulation.
		-Each cell with two or three neighbors survives.
	For a cell that is 'empty' or 'unpopulated':
		-Each cell with three neighbors becomes populated.
*/

public class GameOfLife extends Application {
	//Initialize the number of cells and the dimensions of each cell
	private static final int DIM = 32;
	private static final int CELL_DIM = 22;
	//Initialize cells, the next state place holders, and the high life boolean
	private Cell[][] cell = new Cell[DIM][DIM];
	private boolean[][] nextState = new boolean[DIM][DIM];
	private boolean highLife = false;
	//Initialize all of the buttons, animation, slider, and radio buttons
	private Button step = new Button("Step");
	private Button play = new Button("Play");
	private boolean seePlay = true;
	private Timeline animation;
	private Label rate = new Label("Rate:");
	private Slider slider = new Slider(1.0,10.0,0);
	private Button clear = new Button("Clear");
	private Button save = new Button("Save Game");
	private Button load = new Button("Load Game");
	private RadioButton life = new RadioButton("Life");
	private RadioButton hLife = new RadioButton("High Life");
	
	public void start(Stage stage){
		//create grid, and populate it with cells
		GridPane gridPane = new GridPane();
		gridPane.setGridLinesVisible(true);
		for (int i = 0; i < DIM; i++)
			for (int j = 0; j < DIM; j++)
				gridPane.add(cell[i][j] = new Cell(i,j), j, i);
		//create VBox and add life and high life buttons, making life the default
		VBox lifeButtons = new VBox();
		lifeButtons.getChildren().addAll(life,hLife);
		ToggleGroup group = new ToggleGroup();
	    life.setToggleGroup(group);
	    hLife.setToggleGroup(group);
	    life.setSelected(true);
	    
		//Action taken when clicked
		step.setOnAction(e ->{
			step();
		});
		
		//set animation
		animation = new Timeline(new KeyFrame(Duration.millis(1000), e ->{
			step();
		}));
		animation.setCycleCount(Timeline.INDEFINITE);
		//bind the animation speed to the slider
		animation.rateProperty().bind(slider.valueProperty());
		
		//Action taken when clicked
		play.setOnAction(e ->{
			if(seePlay == true)
				play();
			else
				stop();
		});
		//Action taken when clicked
		clear.setOnAction(e ->{
			stop();
			for (int i = 0; i < DIM; i++)
				for (int j = 0; j < DIM; j++)
					cell[i][j].setState(false);
		});
		//Action taken when clicked
		save.setOnAction(e ->{
			stop();
			saveGame(stage);
		});	
		//Action taken when clicked
		load.setOnAction(e ->{
			stop();
			loadGame(stage);
		});
		//Action taken when clicked
		life.setOnAction(e ->{
			highLife = false;
		});
		//Action taken when clicked
		hLife.setOnAction(e ->{
			highLife = true;
		});
		
		//create the HBox, border pane, and scene, adding the grid pane and HBox
		//to the the border pane, adding the border pane to the scene, and
		//displaying the scene through the stage
		HBox hBox = new HBox(5,step,play,rate,slider,clear,save,load,lifeButtons);
		hBox.setAlignment(Pos.CENTER);
		BorderPane borderPane = new BorderPane();
	    borderPane.setCenter(gridPane);
	    borderPane.setBottom(hBox);
	    Scene scene = new Scene(borderPane);
	    stage.setTitle("Game of Life");
	    stage.setScene(scene);
	    stage.show();
	}
	
	//steps to the next generation
	public void step() {
		for (int i = 0; i < DIM; i++)
			for (int j = 0; j < DIM; j++)
				nextState[i][j] = cell[i][j].nextState();
		for (int i = 0; i < DIM; i++)
			for (int j = 0; j < DIM; j++)
				cell[i][j].setState(nextState[i][j]);
	}
	//plays the animation
	public void play() {
		play.setText("Stop");
		seePlay = false;
		step.setDisable(true);
		animation.play();
	}
	//stops the animation
	public void stop() {
		play.setText("Play");
		seePlay = true;
		step.setDisable(false);
		animation.pause();
	}
	//saves the game in the current directory
	private void saveGame(Stage primaryStage) {
		FileChooser fileChooser = new FileChooser();
		fileChooser.setInitialDirectory(new File("."));
		fileChooser.setTitle("Enter file name");
		fileChooser.getExtensionFilters().add(new ExtensionFilter("Game of Life Files (*.lif)", "*.lif"));
		File selectedFile = fileChooser.showSaveDialog(primaryStage);
		if (selectedFile != null) {
			try (ObjectOutputStream output = new ObjectOutputStream(new FileOutputStream(selectedFile));){
				boolean[][] cellState = new boolean[DIM][DIM];
				for (int i = 0; i < DIM; i++)
					for (int j = 0; j < DIM; j++)
						cellState[i][j] = cell[i][j].getState();
				output.writeObject(cellState);
				output.writeBoolean(highLife);
			}
			catch (IOException ex) {
				ex.printStackTrace();
			}
		}
	}
	//loads a game from the current directory
	private void loadGame(Stage primaryStage) {
		FileChooser fileChooser = new FileChooser();
		fileChooser.setInitialDirectory(new File("."));
		fileChooser.setTitle("Enter file name");
		fileChooser.getExtensionFilters().add(new ExtensionFilter("Game of Life Files (*.lif)", "*.lif"));
		File selectedFile = fileChooser.showOpenDialog(primaryStage);
		if (selectedFile != null) {
			try (ObjectInputStream input = new ObjectInputStream(new FileInputStream(selectedFile));){
			    boolean[][] cellState = (boolean[][])(input.readObject());
			    for (int i = 0; i < DIM; i++)
			    	for (int j = 0; j < DIM; j++) {
			    		cell[i][j].setState(cellState[i][j]);
			    	}
				highLife = input.readBoolean();
				if(highLife)
					hLife.setSelected(true);
				else
					life.setSelected(true);
			}
			catch (Exception ex) {
				ex.printStackTrace();
			}
		}
	}
	public class Cell extends Pane {
		//Declare/Initialize the instance data
		private boolean isAlive = false;
		private int row;
		private int col;
		
		//Constructor for creating an instance of cell
		public Cell(int row,int col) {
			//set instance data
			this.row = row;
			this.col = col;
			this.setPrefSize(CELL_DIM,CELL_DIM);
			this.setStyle("-fx-border-width: 0.25; -fx-border-color: white; -fx-background-color: black;");
			//listen and change alive state if clicked
			this.setOnMouseClicked(e -> {
				if(getState())
					setState(false);
				else
					setState(true);
			});
		}
		//takes what the next generation state should(alive or dead) as a boolean, and
		//makes it the current state
		public void setState(boolean nextState) {
			if(nextState){
				this.setStyle("-fx-border-width: 0.25; -fx-border-color: white; -fx-background-color: green;");
				isAlive = true;
			}
			else{
				this.setStyle("-fx-border-width: 0.25; -fx-border-color: white; -fx-background-color: black;");
				isAlive = false;
			}
		}
		//returns whether the cell is alive or not as a boolean
		public boolean getState() {
			return this.isAlive;
		}
		//returns what the next generation's state should be as a boolean
		public boolean nextState() {
			int liveCount = 0;
			for (int i = -1; i <= 1; i++)
				for (int j = -1; j <= 1; j++)
					if (cell[(row + i + DIM) % DIM][(col + j + DIM) % DIM].isAlive)
						liveCount++;
			if(highLife){
				if(isAlive){
					if(liveCount == 3 || liveCount == 4)
						return true;
				}
				else {
					if(liveCount == 3 || liveCount == 6)
						return true;
				}
			}
			else{
				if(isAlive){
					if(liveCount == 3 || liveCount == 4)
						return true;
				}
				else {
					if(liveCount == 3)
						return true;
				}
			}
			return false;
		}
	}

	public static void main(String[] args){
		launch(args);
	}
}