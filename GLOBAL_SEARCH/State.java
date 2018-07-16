package assignment1A;

import java.awt.Point;
import java.util.ArrayList;
import java.util.Stack;

/**
 * Represent State in game -> node to develop or not , is that win state or not
 * RushHour class use this class as pivot for the algorithm and the solution
 * @author Itay_Guy
 * Date: 02/04/18
 */
public class State {
	public static final int BOARD_SIZE = 6;
	private double _f;
	private State _predecessor;
	private char[][] _board; // for graphic presentation
	private ArrayList<Item> _boardItems;
	private Item _targetItem;
	private double _h;
	private String _movement;
		

	public State(String game) {
		this._board = new char[BOARD_SIZE][BOARD_SIZE];
		for(int i = 0;i <  BOARD_SIZE;i++) {
			for(int j = 0;j < BOARD_SIZE;j++) {
				this._board[i][j] = game.charAt(i*BOARD_SIZE + j);
			}
		}
		ArrayList<Item> horItems = this.extractHorizontalItems(this._board);
		for (Item item : horItems) {
			if(item.getName() == Item.NAME_TARGET) {
				this._targetItem = item;
			}
		}
		ArrayList<Item> verItems = this.extractVerticalItems(this._board);
		this._boardItems = this.mergeItemLists(horItems, verItems);
		this._movement = "";
		this.computeF();
	}
	
	public State(State other,ArrayList<Item> boardItems) {
		this._f = other._f;
		this._predecessor = other._predecessor;
		this._board = new char[BOARD_SIZE][BOARD_SIZE];
		for (Item item : boardItems) {
			if(item.getOrient() == 'H') {
				if(item.getName() == Item.NAME_TARGET) {
					this._targetItem = item;
				}
				int x = (int) item.getTailPos().getX();
				for(int i = (int) item.getTailPos().getY();i <= item.getHeadPos().getY();i++) {
					this._board[x][i] = item.getName();
				}
			} else {
				int y = (int) item.getTailPos().getY();
				for(int i = (int) item.getTailPos().getX();i <= item.getHeadPos().getX();i++) {
					this._board[i][y] = item.getName();
				}				
			}
		}
		for (int i = 0; i < this._board.length; i++) {
			for (int j = 0; j < _board.length; j++) {
				if(this._board[i][j] < 'A' || this._board[i][j] > 'Z') {
					this._board[i][j] = '.';
				}	
			}
		}
		this._boardItems = new ArrayList<Item>(boardItems);
		this._movement = other._movement;
		this.computeF();
	}
	
	public State(State other) {
		this._f = other._f;
		this._predecessor = other._predecessor;
		this._board = other._board.clone();
		this._boardItems = new ArrayList<Item>(other._boardItems);
		for (Item item : _boardItems) {
			if(item.getName() == Item.NAME_TARGET) {
				this._targetItem = item;
			}
		}
		this._movement = other._movement;
		this.computeF();
	}
	
	public State(State other, ArrayList<Item> boardItems, String movement) {
		this(other,boardItems);
		this._movement = new String(movement);
	}

	public double getH() {
		return this._h;
	}
	
	public double getF() {
		return this._f;
	}
	
	public void setF(double f) {
		this._f = f;
	}
	
	public boolean isSuccess() {
		for(Item item : this._boardItems) {
			if(item.isTarget()) {
				return item.isSuccessPoint();
			}
		}
		return false;
	}
	
	public State getAffineState(Stack<State> stack) {
		for (int i = 0; i < stack.size(); i++) {
			int passCounter = 0;
			for (Item item : stack.elementAt(i)._boardItems) {
				for(Item thisItem : this._boardItems) {
					if(item.equals(thisItem)) {
						passCounter++;
					}
				}
			}
			if(passCounter == stack.elementAt(i)._boardItems.size()) {
				return new State(stack.elementAt(i));
			}
		}
		return null;
	}
	
	public void putPredecessorLink(State state) {
		this._predecessor = state;
	}
	
	public State getPredecessor() {
		return this._predecessor;
	}
	
	public ArrayList<State> expand(){
		ArrayList<State> horizontalStates = this.fetchHorizontalStates();
		ArrayList<State> verticalStates = this.fetchVerticalStates();
		return this.mergeStateLists(horizontalStates,verticalStates);
	}
	
	public String toString() {
		String str = "";
		for (int i = 0; i < this._board.length; i++) {
			for (int j = 0; j < this._board.length; j++) {
				str += this._board[i][j] + " ";
			}
			str += "\n";
		}
		return str;
	}
	
	public void computeF() {
		Heuristic heuristic = new Heuristic(this._targetItem,Item.SUCCESS_POS_TAIL,Item.SUCCESS_POS_HEAD,this._boardItems);
		this._h = heuristic.getH();
		this._f = this._h + 1; // g(s) = 1 -> classical naive dijkstra for global search
		
	}
	
	private ArrayList<State> fetchVerticalStates() {
		ArrayList<Item> boardItems = new ArrayList<Item>();
		ArrayList<State> verStates = new ArrayList<State>();
		Item temp = null;
		for(Item item : this._boardItems) {
			temp = new Item(item);
			int y = (int)item.getHeadPos().getY();
			if(item.getOrient() == 'V') {
				//down [-right-]
				for(int i = (int)item.getHeadPos().getX()+1;(i < BOARD_SIZE) && (this._board[i][y] == '.');i++) {
					Point tail = new Point(i-2,y);
					if(item.isCar()) {
						tail = new Point(i-1,y);
					}
					boardItems.add(new Item(item.getName(),item.getOrient(),new Point(i,y),tail));
					boardItems.addAll(new ArrayList<Item>(this._boardItems));
					Item toRemove = null;
					for(Item player : boardItems) {
						if(player.equals(temp)) {
							toRemove = player;
						}
					}
					boardItems.remove(toRemove);
					verStates.add(new State(this,boardItems,item.getName()+"D"+((int)(i-item.getHeadPos().getX()))));
					boardItems.clear();
				}
				//up [-left-]
				for(int i = (int)item.getTailPos().getX()-1;(i >= 0) && (this._board[i][y] == '.');i--) {
					Point head = new Point(i+2,y);
					if(item.isCar()) {
						head = new Point(i+1,y);
					}
					boardItems.add(new Item(item.getName(),item.getOrient(),head,new Point(i,y)));
					boardItems.addAll(new ArrayList<Item>(this._boardItems));
					Item toRemove = null;
					for(Item player : boardItems) {
						if(player.equals(temp)) {
							toRemove = player;
						}
					}
					boardItems.remove(toRemove);
					verStates.add(new State(this,boardItems,item.getName()+"U"+((int)(item.getTailPos().getX()-i))));
					boardItems.clear();
				}
				temp = null;
			}
		}
		return verStates;
	}

	private ArrayList<State> fetchHorizontalStates() {
		ArrayList<Item> boardItems = new ArrayList<Item>();
		ArrayList<State> horStates = new ArrayList<State>();
		Item temp = null;
		for(Item item : this._boardItems) {
			temp = new Item(item);
			int x = (int)item.getHeadPos().getX();
			if(item.getOrient() == 'H') {
				//right
				for(int i = (int)item.getHeadPos().getY()+1;(i < BOARD_SIZE) && (this._board[x][i] == '.');i++) {
					Point tail = new Point(x,i-2);
					if(item.isCar()) {
						tail = new Point(x,i-1);
					}
					boardItems.add(new Item(item.getName(),item.getOrient(),new Point(x,i),tail));
					boardItems.addAll(new ArrayList<Item>(this._boardItems));
					Item toRemove = null;
					for(Item player : boardItems) {
						if(player.equals(temp)) {
							toRemove = player;
						}
					}
					boardItems.remove(toRemove);
					horStates.add(new State(this,boardItems,item.getName()+"R"+((int)(i-item.getHeadPos().getY()))));
					boardItems.clear();
				}
				//left
				for(int i = (int)item.getTailPos().getY()-1;(i >= 0) && (this._board[x][i] == '.');i--) {
					Point head = new Point(x,i+2);
					if(item.isCar()) {
						head = new Point(x,i+1);
					}
					boardItems.add(new Item(item.getName(),item.getOrient(),head,new Point(x,i)));
					boardItems.addAll(new ArrayList<Item>(this._boardItems));
					Item toRemove = null;
					for(Item player : boardItems) {
						if(player.equals(temp)) {
							toRemove = player;
						}
					}
					boardItems.remove(toRemove);
					horStates.add(new State(this,boardItems,item.getName()+"L"+((int)(item.getTailPos().getY()-i))));
					boardItems.clear();
				}
				temp = null;
			}
		}
		return horStates;
	}

	private ArrayList<State> mergeStateLists(ArrayList<State> listA,ArrayList<State> listB){
		ArrayList<State> merged = new ArrayList<State>();
		for(State item : listA) {
			merged.add(new State(item));
		}
		for(State item : listB) {
			merged.add(new State(item));
		}
		return merged;
	}
	
	private ArrayList<Item> mergeItemLists(ArrayList<Item> listA,ArrayList<Item> listB){
		ArrayList<Item> merged = new ArrayList<Item>();
		for(Item item : listA) {
			merged.add(new Item(item));
		}
		for(Item item : listB) {
			merged.add(new Item(item));
		}
		return merged;
	}
	
	private ArrayList<Item> extractVerticalItems(char[][] board) {
		ArrayList<Item> arr = new ArrayList<Item>();
		for(int j = 0;j < board.length;j++) {
			for(int i = 0;i < board[0].length;) {
				if(board[i][j] != '.') {
					Item tempHor = this.getVerticalItem(board, i, j);
					if(tempHor != null) {
						arr.add(tempHor);
						if(board[i][j] == 'X' || (board[i][j] >= 'A' && board[i][j] <= 'K')) {
							i += 2;
						} else if(board[i][j] >= 'O' && board[i][j] <= 'R') {
							i += 3;
						}
					} else {
						i++;
					}
				} else {
					i++;
				}
			}
		}
		return arr;
	}
	
	private Item getVerticalItem(char[][] board,int i,int j) {
		if(i < board[0].length-1 && board[i][j] == board[i+1][j]) {
			boolean isX = false;
			if(board[i][j] == 'X') {
				isX = true;
			}
			if(board[i][j] >= 'A' && board[i][j] <= 'K' || isX == true) {
				return new Item(board[i][j],'V',new Point(i+1,j),new Point(i,j));
			} else {
				return new Item(board[i][j],'V',new Point(i+2,j),new Point(i,j));
			}
		}
		return null;
	}
	
	private ArrayList<Item> extractHorizontalItems(char[][] board) {
		ArrayList<Item> arr = new ArrayList<Item>();
		for(int i = 0;i < board.length;i++) {
			for(int j = 0;j < board[0].length;) {
				if(board[i][j] != '.') {
					Item tempHor = this.getHorizontalItem(board, i, j);
					if(tempHor != null) {
						arr.add(tempHor);
						if(board[i][j] == 'X' || (board[i][j] >= 'A' && board[i][j] <= 'K')) {
							j += 2;
						} else if(board[i][j] >= 'O' && board[i][j] <= 'R') {
							j += 3;
						}
					} else {
						j++;
					}
				} else {
					j++;
				}
			}
		}
		return arr;
	}
	
	private Item getHorizontalItem(char[][] board,int i,int j) {
		if(j < board.length-1 && board[i][j] == board[i][j+1]) {
			boolean isX = false;
			if(board[i][j] == 'X') {
				isX = true;
			}
			if(board[i][j] >= 'A' && board[i][j] <= 'K' || isX == true) {
				return new Item(board[i][j],'H',new Point(i,j+1),new Point(i,j));
			} else {
				return new Item(board[i][j],'H',new Point(i,j+2),new Point(i,j));
			}
		}
		return null;
	}

	public String printSingleMovement() {
		// the difference between the last State -> to represent the movement to solution
		return this._movement;
	}
}