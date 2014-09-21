package Model;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import Model.ArenaTemplate.CellState;
import Model.ExplorationComputer.ExplorationEnvironment;

public class RobotMonitorModel implements ExplorationEnvironment{
	
	public class RobotMonitorModelException extends Exception{
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
		
		this.pathComputer = new MinStepTurnPathComputer(1, 1);
		
		this.explorationComputer = new SingleCycleExplorationComputer(rowCount, colCount, this);
		explorationComputer.setRobotsInitialCell(robot);
		
		try {
			InetAddress ip = InetAddress.getByName(ipAddress);
			this.rpi = new JavaClient(ip, Integer.parseInt(portStr));
			this.exploredCells = this.parseRpiCommand(this.rpi.recv());
			this.explorationComputer.explore();
		} catch (NumberFormatException | IOException e) {
			throw new RobotMonitorModelException(1, e.getMessage());
		}
		
	}

	private Map<Block, CellState> parseRpiCommand(String recv) {
		// TODO Auto-generated method stub
		//Parse the command from robot based on protocol
		return null;
	}

	@Override
	public void explore(CustomizedArena exploredMap) {
		for(Map.Entry<Block, CellState> exploredCell : this.exploredCells.entrySet()){
			int rowID = exploredCell.getKey().getRowID(); 
			int colID = exploredCell.getKey().getColID();
			CellState state = exploredCell.getValue();
			exploredMap.setCellState(rowID, colID, state);
        }
	}

	public void reset() {
		this.explorationComputer.initExploredMap(rowCount, colCount);
		this.robot = null;
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
	
	//return null if no further steps
	public String forward() throws IOException{
		Action nextExploredAction = null;
		if(actionIndex == actions.size() - 1){
			nextExploredAction = this.explorationComputer.getNextStep(this.robot);
			if(nextExploredAction == null){
				//Exploration has finished
				if(!finishExploration){
					finishExploration = true;
					String response = this.rpi.sendForResponse(FINISH);
					assert(response.equals(ACK)):"The ACK is wrong";
					
					//Compute round trip fastest path from start to goal
					ArrayList<Action> roundTripActionFastestPath 
					= getRoundTripFastestPath(this.robot,
											this.explorationComputer.getExploredArena());
					this.actions.addAll(roundTripActionFastestPath);
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
		String response = this.rpi.sendForResponse(nextAction.toString());
		if(finishExploration){
			assert(response.equals(ACK)):"The ACK is wrong";
		}else{
			this.exploredCells = parseRpiCommand(response);
			this.explorationComputer.explore();
		}
		
		this.robot.move(nextAction);
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
