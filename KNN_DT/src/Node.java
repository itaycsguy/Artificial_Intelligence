package Assignment1D;

import java.util.List;

/**
 * Handling each Node management
 * @author Itay_Guy
 */
public class Node {
	private int _goal_class;
	private String _split_by;
	private List<String> _attributes;
	private Node _left;
	private Node _right;
	
	public Node(String split_by,List<String> attributes) {
		this._split_by = split_by;
		this._attributes = attributes;
		this._goal_class = -1;
		this._left = null;
		this._right = null;
	}
	
	public Node(int goal_class,List<String> attributes) {		
		this._attributes = attributes;
		this._goal_class = goal_class;
		this._split_by = null;
		this._left = null;
		this._right = null;
	}
	
	public int get_goal_class() {
		return this._goal_class;
	}
	
	public String get_split_by() {
		return this._split_by;
	}
	
	public Node getLeft() {
		return this._left;
	}
	
	public void setLeft(Node left) {
		this._left = left;
	}

	public Node getRight() {
		return this._right;
	}
	
	public void setRight(Node right) {
		this._right = right;
	}
	
	public String toString() {
		return this._split_by;
	}
}

