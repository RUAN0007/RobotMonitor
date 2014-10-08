package Model;

import java.util.LinkedList;

import Model.ArenaTemplate.CellState;
import application.GlobalUtil;

public class SingleCycleExplorationComputer extends ExplorationComputer {
	////////////////////////////
	//Parameters
	private Block startSouthWestBlock = GlobalUtil.startSouthWestBlock;
	private Orientation firstOrientation = Orientation.NORTH;
	private Orientation secondOrientation = Orientation.EAST;
	private Direction sideOnWallObstacle = Direction.LEFT;
	//Default action when the left, front and right side of the robot
	//is obstacled.
	private Action defaultAction = Action.TURN_LEFT; 
	///////////////////////////////////

	public SingleCycleExplorationComputer(int rowCount, int colCount,
			ExplorationEnvironment env) {
		super(rowCount, colCount, env);
		assert(!firstOrientation.toOppsite().equals(secondOrientation)):
			"Prefered orientations should be adjacent";
		assert(sideOnWallObstacle == Direction.LEFT || sideOnWallObstacle == Direction.RIGHT):
			"sideOnWall should be either left or right";
	}


	private boolean hasTouchedOnEdge = false;
	private boolean hasRobotSideOnEdge = false;
	private boolean hasOccupiedStartBlock = false;
	private boolean hasFinishedLooping = false;

	@Override
	public Action getNextStep(Robot robot) {
		assert(robotSurroundingStatus(robot, robot.getCurrentOrientation()) != -1);
		if(!hasTouchedOnEdge){
			if(!robotOnArenaEdge(robot, firstOrientation)){
				
				return moveToOrientation(robot);
			}else{
//				System.out.println("Robot has touched on edge...");
				hasTouchedOnEdge = true;
			}
		}

		if(!hasRobotSideOnEdge){
			Orientation currentOrientation = robot.getCurrentOrientation();
			if(sideOnWallObstacle == Direction.LEFT &&
					!robotOnArenaEdge(robot, currentOrientation.relativeToLeft())){
				return Action.TURN_RIGHT;
			}

			if(sideOnWallObstacle == Direction.RIGHT &&
					!robotOnArenaEdge(robot, currentOrientation.relativeToRight())){
				return Action.TURN_LEFT;
			}
//			System.out.println("Robot has " + Direction.LEFT + " side on edge...");
			hasRobotSideOnEdge = true;

		}

		if(!hasFinishedLooping){
			if(robot.getSouthWestBlock().equals(startSouthWestBlock)){
				if(!hasOccupiedStartBlock){
				//	System.out.println("Robot has occupied start block...");
					Action next = moveAlongWallObstacle(robot);
					if(next.equals(Action.MOVE_FORWARD) || next.equals(Action.DRAW_BACK)){
						hasOccupiedStartBlock = true;
					}
					return next;
				}else{
					//System.out.println("Robot has finish looping...");
				
					hasFinishedLooping = true; 
				}
			}else{
				return moveAlongWallObstacle(robot);
			}
		
		}

		return null;
	}
	
	private LinkedList<Action> bufferedActions = new LinkedList<Action>();
	private Direction tentativeTurn = Direction.NULL;
	private Action moveAlongWallObstacle(Robot robot) {
		
		if(bufferedActions.size() > 0) return bufferedActions.pollFirst();
		
		Direction sideOppositeWallObstacle = oppoDirection(sideOnWallObstacle);
		
		Orientation currentOrientation = robot.getCurrentOrientation();
		Orientation orientationOnWallObstacle = orientationOnDirection(currentOrientation, sideOnWallObstacle);
		Orientation orientationOppositeWallObstacle = orientationOnDirection(currentOrientation,
																			sideOppositeWallObstacle
																	);
		
		if(tentativeTurn != Direction.NULL){
			Action next = null;
			if(robotSurroundingStatus(robot, currentOrientation) == 1){
				next = directionToAction(oppoDirection(tentativeTurn));
			}else if(robotSurroundingStatus(robot, currentOrientation) == 0){
				next = Action.MOVE_FORWARD;
			}else{
				assert(false):"All the front cells should be explored";
			}
			tentativeTurn = Direction.NULL;
			return next;
		}
		
		
		assert(robotSurroundingStatus(robot, currentOrientation) != -1):
			"The robot's front cells must be explored.";
		
		if(robotSurroundingStatus(robot, orientationOnWallObstacle) == 0){

			//None unexplored or obstacle cells on following side
			//Turn to following direction and forward
			bufferedActions.add(directionToAction(sideOnWallObstacle));
			bufferedActions.add(Action.MOVE_FORWARD);
		}else if(robotSurroundingStatus(robot, orientationOnWallObstacle) == -1){
			//Just Turn to following direction
			bufferedActions.add(directionToAction(sideOnWallObstacle));
			tentativeTurn = sideOnWallObstacle;
		}else if(robotSurroundingStatus(robot, currentOrientation) == 0){
			//No obstacle ahead

			bufferedActions.add(Action.MOVE_FORWARD);
		}else if(robotSurroundingStatus(robot, orientationOppositeWallObstacle) == 0){
			//No unexplored or  obstacle cell exists on the opposite of the following direction
			
			bufferedActions.add(directionToAction(sideOppositeWallObstacle));
			bufferedActions.add(Action.MOVE_FORWARD);
		}else if(robotSurroundingStatus(robot, orientationOppositeWallObstacle) == -1){
			//Unexplored cell exists on the opposite of the following direction
			tentativeTurn = sideOppositeWallObstacle;
			bufferedActions.add(directionToAction(sideOppositeWallObstacle));
		}else{
			//Left,front and right side of the robot exists obstacles. 
			//Turn the head by making a double turn
			assert(robotSurroundingStatus(robot, currentOrientation) == 1);
			assert(robotSurroundingStatus(robot, orientationOnWallObstacle) == 1);
			assert(robotSurroundingStatus(robot, orientationOppositeWallObstacle) == 1);
			bufferedActions.add(directionToAction(sideOnWallObstacle));
			bufferedActions.add(directionToAction(sideOnWallObstacle));

		}
		return bufferedActions.pollFirst();
	}

	private Action moveToOrientation(Robot robot) {
		
		Direction preferedDirection = getPreferedDirection();

		Orientation currentOrientation = robot.getCurrentOrientation();
//		System.out.println("Current Orientation = " + currentOrientation.toString());
//		System.out.println("First Orientation = " + firstOrientation.toString());
//		System.out.println("Second Orientation = " + secondOrientation.toString());

		if(currentOrientation.equals(firstOrientation)){
			
			return moveTowardsDirectionInOrder(Direction.AHEAD,
												preferedDirection,
												oppoDirection(preferedDirection), 
												robot);
		}else if(currentOrientation.equals(secondOrientation)){
		
			return moveTowardsDirectionInOrder(oppoDirection(preferedDirection),
												Direction.AHEAD,
												preferedDirection, 
												robot);
		}else if(currentOrientation.toOppsite().equals(firstOrientation)){
			
			return moveTowardsDirectionInOrder(oppoDirection(preferedDirection),
												Direction.AHEAD,
												preferedDirection, 
												robot);
		}else if(currentOrientation.toOppsite().equals(secondOrientation)){
			
			return moveTowardsDirectionInOrder(preferedDirection,
												Direction.AHEAD,
												oppoDirection(preferedDirection), 
												robot);		
		}
		assert(false):"Should not reach here. No other circumstance";
		return null; 
	}

	private static Direction oppoDirection(Direction old){
		if(old == Direction.LEFT){
			return Direction.RIGHT;
		}else if(old == Direction.RIGHT){
			return Direction.LEFT;
		}else if(old == Direction.AHEAD){
			return Direction.BACK;
		}else if(old == Direction.BACK){
			return Direction.AHEAD;
		}else{
			assert(false):"Should not reach here. No more directions";
		}
		return Direction.NULL;
	}

	private Direction getPreferedDirection() {
		Direction preferedDirection = Direction.NULL;
		if(firstOrientation.relativeToLeft().equals(secondOrientation)){
			preferedDirection = Direction.LEFT;
		}else if(firstOrientation.relativeToRight().equals(secondOrientation)){
			preferedDirection = Direction.RIGHT;
		}else{
			assert(false):"Two Prefered Orientation should be adjacent";
		}
		return preferedDirection;
	}

	private Action moveTowardsDirectionInOrder(Direction firstChoice,
			Direction secondChoice,
			Direction thirdChoice,
			Robot robot){
		
		//Ensure no pair of direction choices are equal
		assert(firstChoice != secondChoice);
		assert(firstChoice != thirdChoice);
		assert(secondChoice != thirdChoice);
		///////////////////////////////////////////////
		Orientation currentOrientation = robot.getCurrentOrientation();
		Orientation firstChoiceOrientation = orientationOnDirection(currentOrientation, firstChoice);
		Orientation secondChoiceOrientation = orientationOnDirection(currentOrientation, secondChoice);
		Orientation thirdChoiceOrientation = orientationOnDirection(currentOrientation, thirdChoice);

		if(robotSurroundingStatus(robot, firstChoiceOrientation) <= 0){
			return directionToAction(firstChoice);
		}
		if(robotSurroundingStatus(robot, secondChoiceOrientation) <= 0){
			return directionToAction(secondChoice);
		}
		if(robotSurroundingStatus(robot, thirdChoiceOrientation) <= 0){
			return directionToAction(thirdChoice);
		}
		return defaultAction;
	}
		//Map Direction Left to Action Turn Left
		//Map Direction Right to Action Turn Right
		//Map Direction Ahead to Action Move_Forward
		//Map Direction Back to Action draw_back
		private static Action directionToAction(Direction direction){
			if(direction == Direction.AHEAD){
				return Action.MOVE_FORWARD;
			}else if(direction == Direction.LEFT){
				return Action.TURN_LEFT;
			}else if(direction == Direction.RIGHT){
				return Action.TURN_RIGHT;
			}else if(direction == Direction.BACK){
				return Action.DRAW_BACK;
			}else{
				assert(false):"Should not reach here";
			}
			return null;
		}

		//Return the new orientation based on the original orientation and its relative direction
		private static Orientation orientationOnDirection(Orientation original,Direction direction){
			if(direction == Direction.AHEAD){
				return original.clone();
			}else if(direction == Direction.LEFT){
				return original.relativeToLeft();
			}else if(direction == Direction.RIGHT){
				return original.relativeToRight();
			}else{
				assert(false):"Should not reach here";
			}
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
