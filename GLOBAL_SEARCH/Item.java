package assignment1A;

import java.awt.Point;
import java.util.ArrayList;

/**
 * Represent each car on Rush-Hour board game
 * @author Itay_Guy
 *
 */
public class Item {
	public static final Point SUCCESS_POS_HEAD = new Point(2,5); // convention
	public static final Point SUCCESS_POS_TAIL = new Point(2,4); // convention
	static final char NAME_TARGET = 'X'; // convention
	private Point _headPos; // -> head is the front down of right
	private Point _tailPos;
	private char _name;
	private char _orient;

	public Item(char name,char orient,Point head,Point tail) {
		this._name = name;
		this._orient = orient;
		this._headPos = new Point(head);
		this._tailPos = new Point(tail);
	}
	
	public Item(Item other) {
		this._name = other._name;
		this._orient = other._orient;
		this._headPos = new Point(other._headPos);
		this._tailPos = new Point(other._tailPos);
	}

	public Point getHeadPos() {
		return this._headPos;
	}
	
	public Point getTailPos() {
		return this._tailPos;
	}

	public boolean isTarget() {
		if(this._name == NAME_TARGET) {
			return true;
		}
		return false;
	}
	
	public boolean isCar() {
		// convention is to pick those letters to be cars from length 2 
		return ((this._name >= 'A' && this._name <= 'K') || this.isTarget());
	}

	public boolean isSuccessPoint() {
		if(this._name == NAME_TARGET && this._headPos.equals(SUCCESS_POS_HEAD) && 
				this._tailPos.equals(SUCCESS_POS_TAIL)) {
			return true;
		}
		return false;
	}
	
	@Override
	public boolean equals(Object other) {
		Item i = ((Item)other);
		if(this._name == i._name &&
			this._headPos.getX() == i._headPos.getX() && this._headPos.getY() == i._headPos.getY() &&
			this._tailPos.getX() == i._tailPos.getX() && this._tailPos.getY() == i._tailPos.getY()) {
			return true;
		}
		return false;
	}

	public char getName() {
		return this._name;
	}

	public char getOrient() {
		return this._orient;
	}
	
	// for the heuristic quick calculation
	public int findExitBlocking(ArrayList<Item> neigbors) {
		int sum = 0;
		for(Item item : neigbors) {
			if(!item.isTarget() && item._tailPos.getY() > this._headPos.getY() && item._tailPos.getX() <= this._tailPos.getX() && item._headPos.getX() >= this._headPos.getX()) {
				sum++;
			}
		}
		return sum;
	}
	
	// for the heuristic quick calculation	
	public int findExitPathCount() {
		return (int) (Item.SUCCESS_POS_HEAD.getY()-this._headPos.getY());
	}
}
