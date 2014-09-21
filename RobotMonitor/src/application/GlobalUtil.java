package application;

import Model.Block;

public class GlobalUtil {
	public static boolean ViewDEBUG = false;
	public static int rowCount = 20;
	public static int colCount = 15;
	
	public static int robotDiamterInCellNum = 3;

	public static Block startSouthWestBlock = new Block(rowCount - 1,  0);
	public static Block goalSouthWestBlock = new Block(robotDiamterInCellNum - 1,
														colCount- robotDiamterInCellNum);

}
