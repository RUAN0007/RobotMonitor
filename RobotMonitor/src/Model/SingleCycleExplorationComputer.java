package Model;

import Model.ArenaTemplate.CellState;
import application.GlobalUtil;

public class SingleCycleExplorationComputer extends ExplorationComputer {
	
	private Block startSouthWestBlock = GlobalUtil.startSouthWestBlock;
	
	public SingleCycleExplorationComputer(int rowCount, int colCount,
			ExplorationEnvironment env) {
		super(rowCount, colCount, env);
	}

	
	private boolean hasReachedStart = false;
	private boolean hasRobotLeftSideOnEdge = false;
	private boolean hasFinishedLooping = false;
	
	@Override
	public Action getNextStep(Robot robot) {
		if(!hasReachedStart){
			if(!robot.getSouthWestBlock().equals(startSouthWestBlock)){
				return moveToStart(robot);
			}else{
				hasReachedStart = true;
			}
		}
		
		if(!hasRobotLeftSideOnEdge){
			if(!robotOnArenaEdge(robot, robot.getCurrentOrientation().relativeToLeft())){
				return Action.TURN_RIGHT;
			}else{
				hasRobotLeftSideOnEdge = true;
			}
		}
		
		if(!hasFinishedLooping){
			if(!robot.getSouthWestBlock().equals(startSouthWestBlock)){
				return moveAlongLeftWall(robot);
			}else{
				hasFinishedLooping = true;
			}
		}
		
		return null;
	}
	
	private Action moveAlongLeftWall(Robot robot) {
		// TODO Auto-generated method stub
		return null;
	}

	private Action moveToStart(Robot robot) {
		// TODO Auto-generated method stub
		return null;
	}

	//return 1 if on ori side exists obstacle or on the arena edge
	//return 0 if on ori side no unexplored cell or obstacle
	//return -1 if on ori side there is unexplored cell
	private int robotSurroundingStatus(Robot robot,Orientation ori){
		if(robotOnArenaEdge(robot, ori)) return 1;
		if(existsCellOnOrientaion(robot, ori, CellState.OBSTACLE)) return 1;
		//The following condition only applies when no obstacle around;
		if(existsCellOnOrientaion(robot, ori, CellState.UNEXPLORED)) return -1;
		//No unexplored cell as well as no obstacle
		return 0;
	}
	
	private boolean existsCellOnOrientaion(Robot robot, Orientation ori,CellState state){
		Boolean needExplore = null;
		if(robotOnArenaEdge(robot, ori)) return false;
		if(ori.equals(Orientation.NORTH))  needExplore = existsCellOnTheNorth(robot,state);
		if(ori.equals(Orientation.WEST))  needExplore = existsCellOnTheWest(robot,state);
		if(ori.equals(Orientation.SOUTH))  needExplore = existsCellOnTheSouth(robot,state);
		if(ori.equals(Orientation.EAST))  needExplore = existsCellOnTheEast(robot,state);

		return needExplore;

	}
	
	private boolean robotOnArenaEdge(Robot robot,Orientation ori){
		if(ori.equals(Orientation.NORTH)){
			return robot.getSouthWestBlock().getRowID() == robot.getDiameterInCellNum() - 1;
		}
		
		if(ori.equals(Orientation.EAST)){
			return robot.getSouthWestBlock().getColID() == this.exploredMap.getColumnCount() - robot.getDiameterInCellNum();
		}
		
		if(ori.equals(Orientation.SOUTH)){
			return robot.getSouthWestBlock().getRowID() == this.exploredMap.getRowCount() - 1;
		}
		
		if(ori.equals(Orientation.WEST)){
			return robot.getSouthWestBlock().getColID() == 0;
		}
		assert(false):"No other direction...";
		return false;
	}

	
	

	//If the north border of the robot is on the side of the arena, return null
	private boolean existsCellOnTheNorth(Robot robot, CellState cell) {
		int robotDiamterInCellNum = robot.getDiameterInCellNum();
		int rowID = robot.getSouthWestBlock().getRowID() - robotDiamterInCellNum;
		for(int colOffset = 0;colOffset < robotDiamterInCellNum;colOffset++){
			int colID = robot.getSouthWestBlock().getColID() + colOffset;
			if(this.exploredMap.getCell(rowID, colID) == cell) return true;
		}
		return false;
	}

	private Boolean existsCellOnTheEast(Robot robot, CellState cell) {
		int robotDiamterInCellNum = robot.getDiameterInCellNum();
		int colID = robot.getSouthWestBlock().getColID() + robotDiamterInCellNum;
		for(int rowOffset = 0;rowOffset < robotDiamterInCellNum;rowOffset++){
			int rowID = robot.getSouthWestBlock().getRowID() - rowOffset;
			if(this.exploredMap.getCell(rowID, colID) == cell) return true;
		}
		return false;
	}

	private Boolean existsCellOnTheWest(Robot robot, CellState cell) {
		int robotDiamterInCellNum = robot.getDiameterInCellNum();
		int colID = robot.getSouthWestBlock().getColID() - 1;
		for(int rowOffset = 0;rowOffset < robotDiamterInCellNum;rowOffset++){
			int rowID = robot.getSouthWestBlock().getRowID() - rowOffset;

			if(this.exploredMap.getCell(rowID, colID) == cell) return true;
		}
		return false;
	}

	private Boolean existsCellOnTheSouth(Robot robot, CellState cell) {
		int robotDiamterInCellNum = robot.getDiameterInCellNum();
		int rowID = robot.getSouthWestBlock().getRowID() + 1;
		for(int colOffset = 0;colOffset < robotDiamterInCellNum;colOffset++){
			int colID = robot.getSouthWestBlock().getColID() + colOffset;
			if(this.exploredMap.getCell(rowID, colID) == cell) return true;
		}
		return false;
	}
}
