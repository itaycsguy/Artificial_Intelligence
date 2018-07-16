package Assignment1D;

/**
 * Handling DT management
 * @author Itay_Guy
 *
 */
public class Tree {
	private Node _root;
	
	public Tree(Node root) {
		this._root = root;
	}
	
	/**
	 * append new branch to some sub-tree key
	 * @param subtree
	 * @param target_val	0 or 1 according to the labeling
	 * @param best		specific key to be subtree father
	 */
	public void add_branch(Tree subtree,int target_val,String best) {
		Node ptr = this.find_by_best(this._root,best);
		if(target_val == 0) { // as convention = left
			ptr.setLeft(subtree.get_root());
		} else { // as convention 1 = right
			ptr.setRight(subtree.get_root());
		}
	}
	
    public Node find_by_best(Node start,String best)
    {
        if (start.get_split_by().equals(best)) {
            return start;
        }
        find_by_best(start.getLeft(),best);
        find_by_best(start.getRight(),best);
        return null;
    }
	
	public Node get_root() {
		return this._root;
	}
}
