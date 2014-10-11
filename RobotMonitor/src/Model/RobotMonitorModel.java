package Model;

import java.io.IOException;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import Model.ArenaTemplate.CellState;
import Model.CustomizedArena.ArenaException;
import Model.ExplorationComputer.ExplorationEnvironment;

public class RobotMonitorModel implements ExplorationEnvironment{

	public class RobotMonitorModelException extends Exception{
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		private int id;
		private String msg;
		private RobotMonitorModelException(int id, String msg) {
			super();
			this.id = id;
			this.msg = msg;
		}
		public int getId() {
			return id;
		}
		public String getMessage() {
			return msg;
		}


	}

	private JavaClient rpi;
	private ExplorationComputer explorationComputer;
	private FastestPathComputer pathComputer;
	private Robot robot;
	private Block startSouthWestBlock;
	private Block goalSouthWestBlock;
	private int rowCount;
	private int colCount;
	private Cell[][] status;
	private Map<Block,CellState> exploredCells = new HashMap<>();

	public RobotMonitorModel(Robot robot, String ipAddress, String portStr,
			int rowCount,int colCount,
			Block startSouthWestBlock,Block goalSouthWestBlock)
					throws RobotMonitorModelException{

		this.rowCount = rowCount;
		this.colCount = colCount;
		this.robot = robot;
		this.startSouthWestBlock = startSouthWestBlock.clone();
		this.goalSouthWestBlock = goalSouthWestBlock.clone();

	//	this.pathComputer = new MinStepTurnPathComputer(1, 1);
		this.pathComputer = new CloseWallPathComputer(Direction.RIGHT);
		this.explorationComputer = new HalfCycleExplorationComputer(rowCount, colCount, this);
		if(!explorationComputer.setRobotsInitialCell(robot)){
			throw new RobotMonitorModelException(2, "Can not place robot here");
		}

		try {
			InetAddress ip = InetAddress.getByName(ipAddress);
			//TODO Testing
			this.rpi = new FakeJavaClient(ip, Integer.parseInt(portStr));
			this.exploredCells = this.parseRpiCommand(this.rpi.recv());
			this.explorationComputer.explore();
		} catch (NumberFormatException | IOException e) {
			throw new RobotMonitorModelException(1, e.getMessage());
		}
		updateStatus();
	}

	private Map<Block, CellState> parseRpiCommand(String recv) {
		//Parse the command from robot based on protocol
		
		HashMap<Block,CellState> results = new HashMap<>();
		
		//Testing purpose
		if(recv.equals("ACK")){
			return results;
		}
		
		assert(recv != null);
		assert(recv.length() == 5);
		String firstDigit =  recv.substring(0, 1); 
		Orientation robotLeftSideOrientation = this.robot.getCurrentOrientation().relativeToLeft();
		Map<Block,CellState> middleLeftExploredResults = addExploredBlocks(robotLeftSideOrientation,MIDDLE,firstDigit);
		results.putAll(middleLeftExploredResults);

		Orientation robotCurrentOrientation = this.robot.getCurrentOrientation().clone();

		String secondDigit = recv.substring(1, 2);
		Map<Block,CellState> leftFrontExploredResults = addExploredBlocks(robotCurrentOrientation,LEFT_SIDE,secondDigit);
		results.putAll(leftFrontExploredResults);

		String thirdDigit = recv.substring(2, 3);
		Map<Block,CellState> middleFrontExploredResults = addExploredBlocks(robotCurrentOrientation,MIDDLE,thirdDigit);
		results.putAll(middleFrontExploredResults);

		String fourthDigit = recv.substring(3, 4);
		Map<Block,CellState> rightFrontExploredResults = addExploredBlocks(robotCurrentOrientation,RIGHT_SIDE,fourthDigit);
		results.putAll(rightFrontExploredResults);


		Orientation robotRightSideOrientation = this.robot.getCurrentOrientation().relativeToRight();
		String fifthDigit = recv.substring(4, 5);
		Map<Block,CellState> middleRightExploredResults = addExploredBlocks(robotRightSideOrientation,MIDDLE,fifthDigit);
		results.putAll(middleRightExploredResults);

		///////////////////////////////////////////////////
		return results;
	}

	private Map<Block, CellState> addExploredBlocks(
			Orientation orientation, int side,
			String value) {
		int distanceRange = Integer.parseInt(value);
		assert(1 <= distanceRange && distanceRange <= 4);

		Map<Block,CellState> results = new HashMap<>();
		int distance;
		for( distance = 1;distance < distanceRange;distance++){
			Block targetBlock = getBlockRelativeToRobotSide(orientation, side, distance);
			if(withInArenaRange(targetBlock.getRowID(), targetBlock.getColID())){
				results.put(targetBlock, CellState.EMPTY);
			}
		}
		if(distance < 4){
			Block targetBlock = getBlockRelativeToRobotSide(orientation, side, distance);
			if(withInArenaRange(targetBlock.getRowID(), targetBlock.getColID())){
				results.put(targetBlock, CellState.OBSTACLE);
			}
		}
		return results;
	}

	private static int LEFT_SIDE = 0;
	private static int MIDDLE = 1;
	private static int RIGHT_SIDE = 2;
	private Block getBlockRelativeToRobotSide(Orientation orientation, int side,int distance){
		int robotDiamterInCellNum = this.robot.getDiameterInCellNum();
		assert(robotDiamterInCellNum == 3);

		int robotSouthWestCellRowID = this.robot.getSouthWestBlock().getRowID();
		int robotSouthWestCellColID = this.robot.getSouthWestBlock().getColID();

		int relativeBlockRowID;
		int relativeBlockColID;

		int targetBlockRowID = -1;
		int targetBlockColID = -1;

		if(orientation.equals(Orientation.NORTH)){
			//NorthWest Block as the relative block
			relativeBlockRowID = robotSouthWestCellRowID - robotDiamterInCellNum + 1;
			relativeBlockColID = robotSouthWestCellColID;
			targetBlockRowID = relativeBlockRowID - distance;
			targetBlockColID = relativeBlockColID + side;

		}else if(orientation.equals(Orientation.EAST)){
			//NorthEast Block as the relative block
			relativeBlockRowID = robotSouthWestCellRowID - robotDiamterInCellNum + 1;
			relativeBlockColID = robotSouthWestCellColID + robotDiamterInCellNum - 1;
			targetBlockRowID = relativeBlockRowID + side;
			targetBlockColID = relativeBlockColID + distance;
		}else if(orientation.equals(Orientation.SOUTH)){
			//SouthEast Block as the relative block
			relativeBlockRowID = robotSouthWestCellRowID;
			relativeBlockColID = robotSouthWestCellColID + robotDiamterInCellNum - 1;
			targetBlockRowID = relativeBlockRowID + distance;
			targetBlockColID = relativeBlockColID - side;
		}else if(orientation.equals(Orientation.WEST)){
			relativeBlockRowID = robotSouthWestCellRowID;
			relativeBlockColID = robotSouthWestCellColID;
			targetBlockRowID = relativeBlockRowID - side;
			targetBlockColID = relativeBlockColID - distance;
		}else{
			assert(false):"No other orientation...";
		}
		return new Block(targetBlockRowID, targetBlockColID);
	}

	@Override
	public void explore(CustomizedArena exploredMap) {

		//If no explored cells, it means testing. 
		if(this.exploredCells.size() != 0){
			for(Map.Entry<Block, CellState> exploredCell : this.exploredCells.entrySet()){
				int rowID = exploredCell.getKey().getRowID(); 
				int colID = exploredCell.getKey().getColID();
				CellState state = exploredCell.getValue();
				exploredMap.setCellState(rowID, colID, state);
			}

		}else{
//			Testing
//			Assume the robot can explore the front 3 * 3 cells
			////////////////////////////////////////////
					
					int robotLeftFrontRowID;
					int robotLeftFrontColID;
					
					int robotRightFrontRowID;
					int robotRightFrontColID;
					
					int robotDiameterInCellNum = this.robot.getDiameterInCellNum();
					int robotExplorationRange = this.robot.getExplorationRange();
					if(this.robot.getCurrentOrientation().equals(Orientation.NORTH)){
						robotLeftFrontRowID = this.robot.getSouthWestBlock().getRowID() - robotDiameterInCellNum + 1;
						robotLeftFrontColID = this.robot.getSouthWestBlock().getColID();
						robotRightFrontRowID = robotLeftFrontRowID;
						robotRightFrontColID = robotLeftFrontColID + robotDiameterInCellNum - 1;
						
						for(int colID = robotLeftFrontColID;colID <= robotRightFrontColID;colID++){
							for(int rowOffset = 1;rowOffset <= robotExplorationRange;rowOffset++){
								int rowID = robotLeftFrontRowID - rowOffset;
								if(!withInArenaRange(rowID, colID)) break;
								
								CellState state = this.exploreBlock(rowID, colID);
								exploredMap.setCellState(rowID, colID, state);
								
								if(exploredMap.getCell(rowID, colID) == CellState.OBSTACLE) break;
							}
						}
					}else if(this.robot.getCurrentOrientation().equals(Orientation.EAST)){
						robotLeftFrontRowID = this.robot.getSouthWestBlock().getRowID() - robotDiameterInCellNum + 1;
						robotLeftFrontColID = this.robot.getSouthWestBlock().getColID() + robotDiameterInCellNum - 1;
						robotRightFrontRowID = this.robot.getSouthWestBlock().getRowID();
						robotRightFrontColID = robotLeftFrontColID;
						
						for(int rowID = robotLeftFrontRowID;rowID <= robotRightFrontRowID;rowID++){
							for(int colOffset = 1;colOffset <= robotExplorationRange;colOffset++){
								int colID = robotLeftFrontColID + colOffset;
								if(!withInArenaRange(rowID, colID)) break;
								CellState state = this.exploreBlock(rowID, colID);
								exploredMap.setCellState(rowID, colID, state);
								if(exploredMap.getCell(rowID, colID) == CellState.OBSTACLE) break;
							
							}
						}
						
					}else if(this.robot.getCurrentOrientation().equals(Orientation.SOUTH)){
						robotLeftFrontRowID = this.robot.getSouthWestBlock().getRowID();
						robotLeftFrontColID = this.robot.getSouthWestBlock().getColID() + robotDiameterInCellNum - 1;
						robotRightFrontRowID = robotLeftFrontRowID;
						robotRightFrontColID = this.robot.getSouthWestBlock().getColID();
						
						for(int colID = robotRightFrontColID; colID <= robotLeftFrontColID;colID++){
							for(int rowOffset = 1;rowOffset <= robotExplorationRange;rowOffset++){
								int rowID = robotLeftFrontRowID + rowOffset;
								
								if(!withInArenaRange(rowID, colID)) break;
								CellState state = this.exploreBlock(rowID, colID);
								exploredMap.setCellState(rowID, colID, state);
								if(exploredMap.getCell(rowID, colID) == CellState.OBSTACLE) break;
							
							}
						}
					}else if(this.robot.getCurrentOrientation().equals(Orientation.WEST)){
						robotLeftFrontRowID = this.robot.getSouthWestBlock().getRowID();
						robotRightFrontRowID = robotLeftFrontRowID - robotDiameterInCellNum + 1;
						robotLeftFrontColID = this.robot.getSouthWestBlock().getColID();
						robotRightFrontColID = robotLeftFrontColID;
						
						for(int rowID = robotRightFrontRowID;rowID <= robotLeftFrontRowID;rowID++){
							for(int colOffset = 1;colOffset <= robotExplorationRange;colOffset++){
								int colID = robotLeftFrontColID - colOffset;
								if(!withInArenaRange(rowID, colID)) break;
								CellState state = this.exploreBlock(rowID, colID);
								exploredMap.setCellState(rowID, colID, state);
								if(exploredMap.getCell(rowID, colID) == CellState.OBSTACLE) break;
							
							}
						}
						
					}else{
						assert(false):"No other ORIENTAIN AVAILABLE...";
					}
		}
		
		
		
	}


	private boolean withInArenaRange(int rowID,int colID){

		if(rowID < 0 || rowID >= rowCount) return false;
		if(colID < 0 || colID >= colCount) return false;
		return true;
	}


	//This method after testing
	private CellState exploreBlock(int rowID,int colID){
		CustomizedArena testArena = null; 
		try {
			testArena = new CustomizedArena(rowCount, colCount);
			testArena.setDescriptor("FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF" + 
					"\n" +
					"000000000400000001C800000000000700000000800000001F80000700000000020000000000");
		} catch (ArenaException e) {
			e.printStackTrace();
		}

		return testArena.getCell(rowID, colID);
	}



	public void reset() throws IOException {
		this.explorationComputer.initExploredMap(rowCount, colCount);
		this.robot = null;
		if(this.rpi != null){
			this.rpi.close();
		}
		this.rpi = null;
		this.updateStatus();
	}

	private void updateStatus() {
		Cell[][] mapStatus = this.explorationComputer.getExploredStatus();
		if(robot != null){

			updateForStart(mapStatus);
			updateForGoal(mapStatus);
			updateForRobot(mapStatus);

		}
		this.status = mapStatus;
	}

	private void updateForGoal(Cell[][] mapStatus) {
		int robotDiameterInCellNum = this.robot.getDiameterInCellNum();
		int southWestGoalRowID = this.goalSouthWestBlock.getRowID();
		int southWestGoalColID = this.goalSouthWestBlock.getColID();

		for(int rowID = 0; rowID < robotDiameterInCellNum; rowID++){
			for(int colID = 0;colID < robotDiameterInCellNum; colID++){
				if(mapStatus[southWestGoalRowID - rowID][southWestGoalColID + colID] == Cell.UNEXMPLORED) continue;
				mapStatus[southWestGoalRowID - rowID][southWestGoalColID + colID]
						= Cell.GOAL;
			}
		}
	}


	private void updateForStart(Cell[][] mapStatus) {
		int robotDiameterInCellNum = this.robot.getDiameterInCellNum();
		int southWestStartRowID = this.startSouthWestBlock.getRowID();
		int southWestStartColID = this.startSouthWestBlock.getColID();

		for(int rowID = 0; rowID < robotDiameterInCellNum; rowID++){
			for(int colID = 0;colID < robotDiameterInCellNum; colID++){
				if(mapStatus[southWestStartRowID - rowID][southWestStartColID + colID] == Cell.UNEXMPLORED) continue;
				mapStatus[southWestStartRowID - rowID][southWestStartColID + colID]
						= Cell.START;
			}
		}
	}



	private void updateForRobot(Cell[][] mapStatus) {
		int robotDiameterInCellNum = this.robot.getDiameterInCellNum();
		int cellRowIndex, cellColIndex;
		for(int rowOffset = 0;rowOffset < robotDiameterInCellNum;rowOffset++){
			cellRowIndex = this.robot.getSouthWestBlock().getRowID() - rowOffset;
			for(int colOffset = 0;colOffset < robotDiameterInCellNum;colOffset++){
				cellColIndex = this.robot.getSouthWestBlock().getColID() + colOffset;

				assert(mapStatus[cellRowIndex][cellColIndex] != Cell.OBSTACLE);
				mapStatus[cellRowIndex][cellColIndex] = Cell.ROBOT;		
			}
		}

		//Draw the Direction Cell

		if(this.robot.getCurrentOrientation().equals(Orientation.WEST)){

			cellRowIndex = this.robot.getSouthWestBlock().getRowID();
			cellColIndex = this.robot.getSouthWestBlock().getColID();

			for(int offset = 0;offset < robotDiameterInCellNum;offset++){
				mapStatus[cellRowIndex][cellColIndex] = Cell.ROBOT_DIRECTION;
				cellRowIndex --;
			}
		}else if(this.robot.getCurrentOrientation().equals(Orientation.EAST)){

			cellRowIndex = this.robot.getSouthWestBlock().getRowID();
			cellColIndex = this.robot.getSouthWestBlock().getColID() + robotDiameterInCellNum - 1;

			for(int offset = 0;offset < robotDiameterInCellNum;offset++){
				mapStatus[cellRowIndex][cellColIndex] = Cell.ROBOT_DIRECTION;
				cellRowIndex --;
			}
		}else if(this.robot.getCurrentOrientation().equals(Orientation.NORTH)){
			cellRowIndex = this.robot.getSouthWestBlock().getRowID() - robotDiameterInCellNum + 1;
			cellColIndex = this.robot.getSouthWestBlock().getColID();

			for(int offset = 0;offset < robotDiameterInCellNum;offset++){
				mapStatus[cellRowIndex][cellColIndex] = Cell.ROBOT_DIRECTION;
				cellColIndex ++;
			}

		}else if(this.robot.getCurrentOrientation().equals(Orientation.SOUTH)){
			cellRowIndex = this.robot.getSouthWestBlock().getRowID();
			cellColIndex = this.robot.getSouthWestBlock().getColID();

			for(int offset = 0;offset < robotDiameterInCellNum;offset++){
				mapStatus[cellRowIndex][cellColIndex] = Cell.ROBOT_DIRECTION;
				cellColIndex ++;
			}
		}

	}

	public String getExploredDescriptor(){
		return this.explorationComputer.getMapDescriptor();
	}

	public Cell getCellStatus(int rowID,int colID){
		return this.status[rowID][colID];
	}

	private ArrayList<Action> actions = new ArrayList<>();
	private int actionIndex = -1; //Point to the index of lastly executed action
	private boolean finishExploration = false;
	private static final String ACK = "ACK";
	private static final String FINISH = "FINISH";

	//return null if no previous step
	public String backward() throws IOException{
		if(this.actionIndex < 0) return null;
		Action preAction = this.actions.get(actionIndex);
		this.actionIndex--;
		moveRobot(Action.revert(preAction));
		return preAction.toString();
	}

	private String sendRpiForResponse(String msg) throws IOException{
		if(msg.equals(FINISH)){
			return this.rpi.sendForResponse("F");
		}
		if(msg.equals(Action.TURN_LEFT.toString())){
			return this.rpi.sendForResponse("L");
		}
		if(msg.equals(Action.TURN_RIGHT.toString())){
			return this.rpi.sendForResponse("R");
		}
		if(msg.equals(Action.MOVE_FORWARD.toString())){
			return this.rpi.sendForResponse("M");
		}
		assert(false):"Should not reach here...";
		return null;
	}

	//return null if no further steps
	public String forward() throws IOException{
		Action nextExploredAction = null;
		if(actionIndex == actions.size() - 1){
			nextExploredAction = this.explorationComputer.getNextStep(this.robot);
			if(nextExploredAction == null){
				//Exploration has finished
				if(!finishExploration){
					finishExploration = true;
					String response = this.sendRpiForResponse(FINISH);
					assert(response.equals(ACK)):"The ACK is wrong";

					//Compute round trip fastest path from start to goal
//						ArrayList<Action> roundTripActionFastestPath 
//					= getRoundTripFastestPath(this.robot,
//							this.explorationComputer.getExploredArena());
					ArrayList<Action> oneWayTrip = this.pathComputer.computeForFastestPath(
							this.explorationComputer.getExploredArena(), 
							robot, 
							this.goalSouthWestBlock.getRowID(), 
							this.goalSouthWestBlock.getColID());
					this.actions.addAll(oneWayTrip);
					return "Exploration Finished";
				}else{
					if(this.rpi != null){
						this.rpi.close();
						this.rpi = null;
					}
					return null;
				}

			}else{
				//Still on exploration phase
				this.actions.add(nextExploredAction);
			}
		}

		this.actionIndex++;
		Action nextAction = this.actions.get(actionIndex);


		moveRobot(nextAction);
		return nextAction.toString();
	}

	private void moveRobot(Action nextAction) throws IOException {
		String response = this.sendRpiForResponse(nextAction.toString());
		this.robot.move(nextAction);
		if(finishExploration){
			assert(response.equals(ACK)):"The ACK is wrong";
		}else{
			this.exploredCells = parseRpiCommand(response);
			this.explorationComputer.explore();
		}

		this.updateStatus();
	}

	private ArrayList<Action> getRoundTripFastestPath(Robot robot, CustomizedArena arena) {
		ArrayList<Action> roundTrip = new ArrayList<>();
		ArrayList<Action> oneWayTrip = this.pathComputer.computeForFastestPath(arena, 
				robot, 
				this.goalSouthWestBlock.getRowID(), 
				this.goalSouthWestBlock.getColID());
		roundTrip.addAll(oneWayTrip);

		//Reverse the orientation
		roundTrip.add(Action.TURN_LEFT);
		roundTrip.add(Action.TURN_LEFT);
		ArrayList<Action> returnTrip = reverseTrip(oneWayTrip);
		roundTrip.addAll(returnTrip);
		return roundTrip;
	}

	private ArrayList<Action> reverseTrip(ArrayList<Action> oneWayTrip) {
		ArrayList<Action> returnTrip = new ArrayList<Action>();
		for(	int actionID = oneWayTrip.size() - 1;actionID>=0;actionID--){
			Action action = oneWayTrip.get(actionID);
			if(action.equals(Action.TURN_LEFT)){
				returnTrip.add(Action.TURN_RIGHT);
			}else if(action.equals(Action.TURN_RIGHT)){
				returnTrip.add(Action.TURN_LEFT);
			}else{
				returnTrip.add(action.clone());
			}
		}
		return returnTrip;
	}

	public int getCurrentTurnCount(){
		int count = 0;
		for (int actionID = 0;actionID <= actionIndex;actionID++){
			if(actions.get(actionID) == Action.TURN_LEFT 
					|| actions.get(actionID) == Action.TURN_RIGHT){
				count++;
			}
		}
		return count;
	}

	public int getCurrentStepCount(){
		int count = 0;
		for (int actionID = 0;actionID <= actionIndex;actionID++){
			if(actions.get(actionID) == Action.MOVE_FORWARD 
					|| actions.get(actionID) == Action.DRAW_BACK){
				count++;
			}
		}
		return count;
	}

	public double getExploredCoverage(){
		return this.explorationComputer.getCoverage();
	}
}
