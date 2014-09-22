package application;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.Timer;
import java.util.TimerTask;

import Model.*;
import Model.RobotMonitorModel.RobotMonitorModelException;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.RowConstraints;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.FileChooser.ExtensionFilter;

public class RobotMonitorController implements Initializable {
	private static Color OBSTACLE_COLOR = Color.BLACK;
	private static Color EMPTY_COLOR = Color.WHITE;
	private static Color ROBOT_COLOR = Color.BLUE;
	private static Color DIRECTION_COLOR = Color.RED;
	private static Color PATH_COLOR = Color.AQUA;
	private static Color UNEXPLORED_COLOR = Color.GRAY;
	private static Color START_COLOR = Color.ALICEBLUE;
	private static Color GOAL_COLOR = Color.ANTIQUEWHITE;
	
	private int rowCount = GlobalUtil.rowCount;
	private int colCount = GlobalUtil.colCount;
	@FXML
	GridPane arena;

	@FXML
	Label msgLabel;

	@FXML
	Label rowIndexLabel;

	@FXML
	Label colIndexLabel;

	@FXML
	Label cellTypeLabel;

	@FXML
	Label stepCountLabel;

	@FXML
	Label turnCountLabel;

	@FXML
	Label coverageLabel;

	@FXML
	Rectangle demoCell;

	@FXML
	ToggleButton startpausedButton;

	@FXML
	Button resetButton;


	@FXML
	Button backwardButton;

	@FXML
	Button forwardButton;

	@FXML
	ChoiceBox<String> secondsPerStepChoiceBox;

	@FXML
	ChoiceBox<String> initRowChoiceBox;
	
	@FXML
	ChoiceBox<String> initColChoiceBox;
	
	@FXML
	ChoiceBox<String> initOrientationChoiceBox;
	
	@FXML
	TextField ipTextField ;
	
	@FXML
	TextField portTextField;
	
	@FXML
	Button connectionButton;
	


	private Stage stage;

	private Rectangle[][] recs;

	private RobotMonitorModel model;
	@Override
	public void initialize(URL location, ResourceBundle resources) {
		initControlWidgets();
		initConnectionWidgets();
		setConnectionWidgetsDisabled(false);
		addBlocks(rowCount, colCount, 12);
		
	}

	private void initConnectionWidgets() {
		for(int rowID = 1;rowID <= this.rowCount;rowID++){
			initRowChoiceBox.getItems().add("" + rowID);
		}
		initRowChoiceBox.setValue("10");
		
		for(int colID = 1;colID <= this.colCount;colID++){
			initColChoiceBox.getItems().add("" + colID);
		}
		initColChoiceBox.setValue("6");
		
		
		initOrientationChoiceBox.getItems().add(Orientation.NORTH.toString());
		initOrientationChoiceBox.getItems().add(Orientation.EAST.toString());
		initOrientationChoiceBox.getItems().add(Orientation.SOUTH.toString());
		initOrientationChoiceBox.getItems().add(Orientation.WEST.toString());
		initOrientationChoiceBox.setValue(Orientation.NORTH.toString());
	}
	
	private void setConnectionWidgetsDisabled(boolean value){
		this.initColChoiceBox.setDisable(value);
		this.initRowChoiceBox.setDisable(value);
		this.initOrientationChoiceBox.setDisable(value);
		this.ipTextField.setEditable(!value);
		this.portTextField.setEditable(!value);
		this.connectionButton.setDisable(value);


	}

	private void initControlWidgets() {
		startpausedButton.setDisable(true);
		forwardButton.setDisable(true);
		backwardButton.setDisable(true);
		resetButton.setDisable(true);
		secondsPerStepChoiceBox.setDisable(true);

		secondsPerStepChoiceBox.getItems().add("0.5");
		secondsPerStepChoiceBox.getItems().add("1");
		secondsPerStepChoiceBox.getItems().add("2");
		secondsPerStepChoiceBox.setValue("1");
	}

	private void addBlocks(int rowCount,int columnCount,int indexFont) {
		recs = new Rectangle[rowCount][columnCount];
		double blockWidth = arena.getPrefWidth() / (columnCount + 1) * 0.9;
		double blockHeight = arena.getPrefHeight() / (rowCount + 1) * 0.9;

		for(int rowLabelIndex = 0;rowLabelIndex <= rowCount;rowLabelIndex++){
			RowConstraints row = new RowConstraints();
			row.setPercentHeight(100.0 / (rowCount + 1));
			arena.getRowConstraints().add(row);
		}

		for(int colLabelIndex = 0;colLabelIndex <= columnCount;colLabelIndex++){
			ColumnConstraints col = new ColumnConstraints();
			col.setPercentWidth(100.0 / (columnCount + 1));
			arena.getColumnConstraints().add(col);
		}


		for(int colLabelIndex = 1;colLabelIndex <= columnCount;colLabelIndex++){
			Label colLabel = new Label("" + colLabelIndex);
			colLabel.setFont(new Font(indexFont));
			colLabel.setMinSize(blockWidth, blockHeight);
			colLabel.setAlignment(Pos.CENTER);
			arena.add(colLabel, colLabelIndex, 0);

		}

		for(int rowLabelIndex = 1;rowLabelIndex <= rowCount;rowLabelIndex++){

			Label colLabel = new Label("" + rowLabelIndex);
			colLabel.setFont(new Font(indexFont));

			colLabel.setMinSize(blockWidth, blockHeight);
			colLabel.setAlignment(Pos.CENTER);
			arena.add(colLabel, 0, rowLabelIndex);

		}



		for(int rowIndex = 1; rowIndex <= rowCount;rowIndex++){
			for(int colIndex = 1; colIndex <= columnCount;colIndex++){
				Rectangle rec = new Rectangle(blockWidth, blockHeight);
				//	rec.setId("" + (rowIndex - 1) + "Block" + (colIndex - 1));
				rec.setFill(UNEXPLORED_COLOR);
				recs[rowIndex - 1][colIndex - 1] = rec;
				rec.setArcHeight(20);
				rec.setArcWidth(20);
				arena.add(rec,colIndex, rowIndex);
			}
		}

	}

	private FileChooser getMapDescriptorFileChooser() {
		FileChooser fileChooser = new FileChooser();
		fileChooser.getExtensionFilters().add(
				new ExtensionFilter("Map Descripter", "*.txt"));
		return fileChooser;
	}

	@FXML 
	void onDescriptorSaved(){
	  if(this.model == null) {
		  this.setMessage("The model is null");
		  return;
	  }
	  String description = this.model.getExploredDescriptor();




		FileChooser fileChooser = getMapDescriptorFileChooser();
		File savedFile = fileChooser.showSaveDialog(this.stage);
		if(savedFile != null){
			try(BufferedWriter bw = new BufferedWriter(
					new FileWriter(savedFile))) {

				bw.write(description);
				this.msgLabel.setText("Save to " + savedFile.getName());

			} catch (IOException e) {
				//e.printStackTrace();
				this.msgLabel.setText("Save to  " + savedFile.getName() + "failed...");
			}
		}
	}


	private void refleshView() {
				
				for(int rowID = 0;rowID < rowCount; rowID++){
					for(int colID = 0;colID < colCount;colID++){
						Rectangle rectToPaint = getRectangle(rowID,colID);
						assert(rectToPaint != null);
						Cell cellModel = this.model.getCellStatus(rowID, colID);
						paintRectBasedOnStatus(rectToPaint,cellModel);
					}
				}
				
				this.stepCountLabel.setText("" + this.model.getCurrentStepCount());
				this.turnCountLabel.setText("" + this.model.getCurrentTurnCount());
				double coverage = this.model.getExploredCoverage();
				this.coverageLabel.setText(roundToDigitsAfterDecimal(coverage,2));
	}

	private String roundToDigitsAfterDecimal(double coverage, int digitsAfterDecimal) {
		long base = 1;
		for(int digit = 0;digit < digitsAfterDecimal;digit++){
			base *= 10;
		}
		double result = (double)((long)(coverage * base)) / (double)base;
		return "" + result;
	}

	private void paintRectBasedOnStatus(Rectangle rectToPaint, Cell cellModel) {
		if(cellModel == Cell.UNEXMPLORED){
			rectToPaint.setFill(UNEXPLORED_COLOR);

		}else if(cellModel == Cell.EMPTY){
			rectToPaint.setFill(EMPTY_COLOR);

		}else if(cellModel == Cell.OBSTACLE){
			rectToPaint.setFill(OBSTACLE_COLOR);

		}else if(cellModel == Cell.ROBOT){
			rectToPaint.setFill(ROBOT_COLOR);

		}else if(cellModel == Cell.ROBOT_DIRECTION){
			rectToPaint.setFill(DIRECTION_COLOR);

		}else if(cellModel == Cell.PATH){
			rectToPaint.setFill(PATH_COLOR);

		}else if(cellModel == Cell.EMPTY){
			rectToPaint.setFill(EMPTY_COLOR);
		}else if(cellModel == Cell.START){
			rectToPaint.setFill(START_COLOR);
		}else if(cellModel == Cell.GOAL){
			rectToPaint.setFill(GOAL_COLOR);
		}
	}
	private Rectangle getRectangle(int rowID, int colID) {
		//System.out.println("" + rowID + "Block" + colID);
		//	return (Rectangle) this.scene.lookup("" + rowID + "Block" + colID);
		return recs[rowID][colID];
	}

	@FXML
	public void onArenaHovered(MouseEvent me){
		if(GlobalUtil.ViewDEBUG){
			System.out.println("onArenaHovered");
		}
		double xCdn = me.getSceneX();
		double yCdn = me.getSceneY();

		int rowIndex = computeArenaRowIndex(yCdn);
		int columnIndex = computeArenaColumnIndex(xCdn);

		updateCellIndexDisplay(rowIndex, columnIndex);
		if(this.model != null){
			updateCellStateDisplay(rowIndex, columnIndex);
		}
	}

	private int computeArenaRowIndex(double yCdn){ //xCdn = Coordinate X on the scene
		double arenaYCdn = this.arena.getLayoutY();
		double cellHeight = this.arena.getPrefHeight() / (rowCount + 1);
		int rowIndex = (int)((yCdn - arenaYCdn) / cellHeight);
		rowIndex--;

		return rowIndex;

	}

	private void updateCellStateDisplay(int rowIndex, int columnIndex) {

		if((0 <= rowIndex && rowIndex <= rowCount - 1 ) &&
				(0 <= columnIndex && columnIndex <= colCount - 1)){
			Cell cell = this.model.getCellStatus(rowIndex, columnIndex);
			paintRectBasedOnStatus(this.demoCell, cell);
			updateDemoLabel(cell);
		}else{
			this.cellTypeLabel.setText("---");
			this.demoCell.setFill(UNEXPLORED_COLOR);
		}
	}



	private void updateDemoLabel(Cell cell) {
		if(cell == Cell.UNEXMPLORED){
			this.cellTypeLabel.setText("UNEXPLORED");

		}else if(cell == Cell.EMPTY){
			this.cellTypeLabel.setText("EMPTY");

		}else if(cell == Cell.OBSTACLE){
			this.cellTypeLabel.setText("OBSTACLE");

		}else if(cell == Cell.ROBOT){
			this.cellTypeLabel.setText("ROBOT");

		}else if(cell == Cell.ROBOT_DIRECTION){
			this.cellTypeLabel.setText("DIRECTION");

		}else if(cell == Cell.PATH){
			this.cellTypeLabel.setText("PATH");

		}else if(cell == Cell.GOAL){
			this.cellTypeLabel.setText("GOAL");
		}else if(cell == Cell.START){
			this.cellTypeLabel.setText("START");
		}
	}
	//return a value between [0,GlobalUtil.columnCount - 1]
	private int computeArenaColumnIndex(double xCdn){ //xCdn = Coordinate X on the scene
		double arenaXCdn = this.arena.getLayoutX();
		double cellWidth = this.arena.getWidth() / (colCount + 1);
		int columnIndex = (int)((xCdn - arenaXCdn) / cellWidth);
		columnIndex--;

		return columnIndex;

	}

	private void updateCellIndexDisplay(int rowIndex, int columnIndex) {
		if((0 <= rowIndex && rowIndex <= rowCount - 1 ) &&
				(0 <= columnIndex && columnIndex <= colCount - 1)){
			this.rowIndexLabel.setText("" + (rowIndex + 1));
			this.colIndexLabel.setText("" + (columnIndex + 1));

		}else{
			this.rowIndexLabel.setText("-");
			this.colIndexLabel.setText("-");

		}
	}
	
	private boolean missionCompleted = false;
	@FXML
	public void onBackwardPressed(){
		if(GlobalUtil.ViewDEBUG){
			System.out.println("onBackwardPressed");
		}
		String actionDescription = null;
		try {
			actionDescription = this.model.backward();
			if(actionDescription == null){
				actionDescription = "Already at start";
				this.backwardButton.setDisable(true);
			}
		} catch (IOException e) {
			setMessage(e.getMessage());
			this.onResetPressed(null);
			return;
		}
		setMessage(actionDescription);
		missionCompleted = false;
		refleshView();
		
		this.forwardButton.setDisable(false);
	}
	
	@FXML
	public void onForwardPressed(ActionEvent event){
		if(GlobalUtil.ViewDEBUG){
			System.out.println("onForwardPressed");
		}
		String actionDescription = null;
		try {
			actionDescription = this.model.forward();
			
			if(actionDescription == null){
				actionDescription = "Mission Completed...";
				missionCompleted = true;
				setControlWidgetsDisabled(true);
				setConnectionWidgetsDisabled(false);
				this.resetButton.setDisable(false);
			}else{
				this.backwardButton.setDisable(false);
			}
		} catch (IOException e) {
			setMessage(e.getMessage());
			this.onResetPressed(null);
			return;
		}
		setMessage(actionDescription);

		refleshView();
		
	}

	private void setControlWidgetsDisabled(boolean value) {
		this.forwardButton.setDisable(value);
		this.backwardButton.setDisable(value);
		this.startpausedButton.setDisable(value);
		this.resetButton.setDisable(value);
	}
	
	Timer timer = null;
	@FXML
	public void onStartPausedPressed(ActionEvent e){
		if(GlobalUtil.ViewDEBUG){
			System.out.println("onStartPausedPressed");
		}
		if (startpausedButton.isSelected()){
			//Start button is pressed
			
			this.resetButton.setDisable(true);
			this.forwardButton.setDisable(true);
			this.backwardButton.setDisable(true);
			startpausedButton.setText("Pause");
			
			timer = new Timer();
			double secondPerStep = Double.parseDouble(this.secondsPerStepChoiceBox.getValue());
			long millisecondPerStep = (long)( 1000 * secondPerStep);
			forwardWithFrequency(millisecondPerStep);
		}else{
			//Pause button is pressed
			onPausedPressed();
		}
	}
	
	private void forwardWithFrequency(long delay) {
		if(missionCompleted){
			this.onPausedPressed();
		}else{
			this.onForwardPressed(null);
			this.timer = new Timer();
			timer.schedule(new TimerTask() {
				
				@Override
				public void run() {
					Platform.runLater(new Runnable() {
						
						@Override
						public void run() {
							//Recursive call
							forwardWithFrequency(delay);
						} //End of run()
					});//End of runLater() method
				}//End of run
			}, delay);//End of schedule task
		}
	}

	protected void onPausedPressed() {
		timer.cancel();
		startpausedButton.setText("Start");
		this.resetButton.setDisable(false);
	}
	
	@FXML
	public void onResetPressed(ActionEvent e){
		this.setConnectionWidgetsDisabled(false);
		this.setControlWidgetsDisabled(true);
		this.startpausedButton.setSelected(false);
		this.startpausedButton.setText("Start");
		if(GlobalUtil.ViewDEBUG){
			System.out.println("onResetPressed");
		}
		try {
			this.model.reset();
		} catch (IOException e1) {
			setMessage(e1.getMessage());
		}
		this.refleshView();
	}
	
	@FXML
	public void onConnectPressed(){
		if(GlobalUtil.ViewDEBUG){
			System.out.println("onResetPressed");
		}
		String ipAddress = this.ipTextField.getText();
		String portStr = this.portTextField.getText();
		int initSouthWestRowID = Integer.parseInt(this.initRowChoiceBox.getValue()) - 1;
		int initSouthWestColID = Integer.parseInt(this.initColChoiceBox.getValue()) - 1;
		Orientation initOrientation = getOrientationFromString(this.initOrientationChoiceBox.getValue());
		
		Robot robot = new Robot(initSouthWestRowID, 
								initSouthWestColID,
								GlobalUtil.robotDiamterInCellNum,
								initOrientation, 3);
		this.setMessage("Connecting...");
		try {
			this.model = new RobotMonitorModel(robot, 
											  ipAddress, portStr,
											  rowCount, colCount, 
											  GlobalUtil.startSouthWestBlock, 
											  GlobalUtil.goalSouthWestBlock);
		} catch (RobotMonitorModelException e) {
			this.model = null;
			setMessage("Connection Failed: " + e.getMessage());
			return;
		}
		this.setConnectionWidgetsDisabled(true);
		this.startpausedButton.setDisable(false);
		this.forwardButton.setDisable(false);
		this.resetButton.setDisable(false);
		this.refleshView();
		this.setMessage("Connection Succeeded...");
	
	}

	private void setMessage(String msg) {
		this.msgLabel.setText(msg);
	}

	private Orientation getOrientationFromString(String value) {
		if(value.equals(Orientation.NORTH.toString())){
			return Orientation.NORTH;
		}
		if(value.equals(Orientation.SOUTH.toString())){
			return Orientation.SOUTH;
		}
		if(value.equals(Orientation.EAST.toString())){
			return Orientation.EAST;
		}
		if(value.equals(Orientation.WEST.toString())){
			return Orientation.WEST;
		}
		assert(false):"Should not reach here. No other orientation available...";
		return null;
	}

	public void setStage(Stage primaryStage) {
		this.stage = primaryStage;
	}

}
