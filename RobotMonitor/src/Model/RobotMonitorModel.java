package Model;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;

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

		} catch (NumberFormatException | IOException e) {
			throw new RobotMonitorModelException(1, e.getMessage());
		}
		
	}

	@Override
	public void explore(CustomizedArena exploredMap) {
		// TODO Auto-generated method stub
		
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
	
}
