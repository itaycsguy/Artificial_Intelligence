package assignment1A;

import java.awt.Point;
import java.util.ArrayList;

public class Heuristic {
	private double _h;
	
	// H(x) = number of blocking to the exit item + distance to exit
	public Heuristic(Item targetX,Point successTailPos,Point successHeadPos,ArrayList<Item> boardItems) {
		this._h = targetX.findExitBlocking(boardItems) + targetX.findExitPathCount();
	}
	
	public double getH() {
		return this._h;
	}
}
