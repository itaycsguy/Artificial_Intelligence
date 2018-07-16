package Assignment1D;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Handling the DT-ID3
 * @author Itay_Guy
 */
public class DecisionTree {
	private static final int[] BINARY_SET = new int[] {0,1}; // this is a binary problem
	private Dataset _data;
	private Tree _hypotheses_tree; // keep here the model
	
	public DecisionTree(Dataset data) {
		this._data = data;
	}
	
	/**
	 * @return	prepared model
	 */
	public Tree get_hypotheses() {
		return this._hypotheses_tree;
	}
	
	/*
	 * execute over the tree until node is reached than classify - recursive method
	 */
	private String classify_by_leaf(Node node,FeatureVector f){
		if(node.get_split_by() == null) {
			return this._data.convert_target(node.get_goal_class());
		}
		int feature_val = (int) f.getFeature(this._data.getAttrIndex(node.get_split_by()) - 1);
    	if(feature_val == 0) {
    		return this.classify_by_leaf(node.getLeft(),f);    		
    	}
    	return this.classify_by_leaf(node.getRight(),f);
	}
	
	/**
	 * predict test-set
	 * @param chunck	if chunck == -1 -> classify all test-set otherwise "chunck" featurevectors is predicted
	 * @return	accuracy
	 */
	public Double predict(int chunck) {
		Map<String,String> res = new HashMap<String,String>();
		int iter = 0,suc = 0;
		for (FeatureVector f : this._data.getTestSet()) {
			if(chunck == -1 || iter <= chunck) { // -1 take all test-set samples
				String real = f.getName();
				String pred = this.classify_by_leaf(this._hypotheses_tree.get_root(),f);
				res.put(real + "@" + iter,pred);
				iter++;
				if(real.equals(pred)) {
					suc++;
				}
			} else {
				break;
			}
		}
		DecisionTree.disp_confusion_matrix(res);
		double accuracy = (double)suc/iter;
		System.out.println("Accuracy: " + accuracy);
		return accuracy;
	}
	
	/*
	 * display each machine-learning parameters to the screen:
	 */
	private static void disp_confusion_matrix(Map<String, String> res) {
		// build confusion matrix:
		Map<String,HashMap<String,Integer>> counter = new HashMap<String,HashMap<String,Integer>>();
		for (String key : res.keySet()) {
			if(key.contains(res.get(key))){
				if(!counter.containsKey(res.get(key))) {
					HashMap<String,Integer> h = new HashMap<String,Integer>();
					h.put(res.get(key), 1);
					counter.put(res.get(key),h);
				} else {
					if(!counter.get(res.get(key)).containsKey(res.get(key))) {
						counter.get(res.get(key)).put(res.get(key), 1);
					} else {
						counter.get(res.get(key)).put(res.get(key),counter.get(res.get(key)).get(res.get(key)) + 1);
					}
				}
			} else {
				String subkey = key.split("@")[0];
				if(!(counter.get(subkey) == null)) {
					if(!counter.get(subkey).containsKey(res.get(key))) {
						counter.get(subkey).put(res.get(key),1);
					} else {
						counter.get(subkey).put(res.get(key),counter.get(subkey).get(res.get(key)) + 1);
					}
				} else {
					HashMap<String,Integer> inner_hash = new HashMap<String,Integer>();
					inner_hash.put(res.get(key),1);
					counter.put(subkey, inner_hash);
				}
			}
		}
		//print confusion matrix:
		System.out.println("******\nConfusion Matrix: ");
		System.out.println("=================");
		for (String inner_key : counter.keySet()) {
			System.out.print("\t  " + inner_key);
		}
		for (String key : counter.keySet()) {
			System.out.println();
			System.out.print(key + "\t");
			for (String key_deeper : counter.keySet()) {
				if(counter.get(key).get(key_deeper) == null) {
					System.out.print(0 + "\t  ");
					counter.get(key).put(key_deeper, 0);
				} else {
					System.out.print(counter.get(key).get(key_deeper) + "\t  ");
				}
			}
		}
		System.out.println("\n******");
		// print precision,recall and F-[]-measure parameters:
		int size = 0;
		if(counter.keySet().size() == 0) {
			size = counter.keySet().size() + 2;
		} else if(counter.keySet().size() == 1) {
			size = counter.keySet().size() + 1;			
		}
		String[] names = new String[size];
		String name0 = counter.keySet().toArray(names)[0];
		String name1 = counter.keySet().toArray(names)[1];
		double name0_precision = 0.0;
		try{
			name0_precision = counter.get(name0).get(name0)/(1.0*(counter.get(name0).get(name0) + counter.get(name0).get(name1)));
		}catch(Exception ex) {}
		double name1_precision = 0.0;
		try{
			name1_precision = counter.get(name1).get(name1)/(1.0*(counter.get(name1).get(name0) + counter.get(name1).get(name1)));
		}catch(Exception ex) {}
		System.out.println(name0 + " Precision: " + name0_precision);
		System.out.println(name0 + " Precision: " + name1_precision + "\n******");
		double name0_recall = 0.0;
		try{
			name0_recall = counter.get(name0).get(name0)/(1.0*(counter.get(name0).get(name0) + counter.get(name1).get(name0)));
		}catch(Exception ex) {}
		double name1_recall = 0.0;
		try{
			name1_recall = counter.get(name1).get(name1)/(1.0*(counter.get(name0).get(name1) + counter.get(name1).get(name1)));
		}catch(Exception ex) {}
		System.out.println(name0 + " Recall: " + name0_recall);
		System.out.println(name0 + " Recall: " + name1_recall + "\n******");
		double name0_F_measure = 2*name0_precision*name0_recall/(name0_precision + name0_recall);
		double name1_F_measure = 2*name1_precision*name1_recall/(name1_precision + name1_recall);
		System.out.println("F-[" + name0 + "]-measure: " + name0_F_measure);
		System.out.println("F-[" + name0 + "]-measure: " + name1_F_measure);
		System.out.println("******");
	}

	/**
	 * build new DT model
	 */
	public void fit() {
		List<FeatureVector> train = this._data.getTrainingset();
		List<String> attributes = this._data.getAttributes(false); // false is for taking attribute without labels
		String defy = this.majority_value(train); // pickup the majority label in the training-set samples
		this._hypotheses_tree = this.build_decision_tree(train,attributes,defy);
	}
	
	private Tree build_decision_tree(List<FeatureVector> examples,List<String> attributes,String defy) {
		if(examples == null || examples.isEmpty()) {
			return new Tree(new Node(this._data.convert_class(defy),attributes));
		}else if(this.has_homogenous_class(examples) || attributes.isEmpty()) {
			return new Tree(new Node(this._data.convert_class(this.majority_value(examples)),attributes));
		} else {
			String best = this.choose_attribute(examples, attributes);
			Tree tree = new Tree(new Node(best,attributes));
			for(int vi : DecisionTree.BINARY_SET) { // build node options
				List<FeatureVector> examplesi = this.choose_subtrain(examples, vi, best);
				Tree subtree = this.build_decision_tree(examplesi,this.subtract_attr(attributes,best),this.majority_value(examples));
				tree.add_branch(subtree,vi,best); // add new sub-tree to this vi side in this node
			}
			return tree;
		}
	}
	
	/*
	 * find the majority label appearances in the training-set featurevectors:
	 */
	private String majority_value(List<FeatureVector> examples) {
		int total = examples.size(),count = 0;
		for (FeatureVector f : examples) {
			if(this._data.convert_class(f.getName()) == 1) {
				count++;
			}
		}
		if(((double)count/total) > 0.5) {
			return this._data.convert_target(1);
		}
		return this._data.convert_target(0);
	}
	
	/*
	 * picking sub-training-set according to the best feature "val" - this is the turn around in the tree current node:
	 */
	private List<FeatureVector> choose_subtrain(List<FeatureVector> train,int val,String best){
		int best_idx = this._data.getAttrIndex(best) - 1;
		List<FeatureVector> res = new ArrayList<FeatureVector>();
		for (FeatureVector fv : train) {
			if(fv.getFeature(best_idx) == val) {
				res.add(new FeatureVector(fv));
			}
		}
		return res;
	}
	
	/*
	 * choosing best attribute using maximize Gain function:
	 */
	private String choose_attribute(List<FeatureVector> examples,List<String> attributes) {
		return this.select_by_gain(examples,attributes);
	}
	
	/*
	 * subtracting some attribute from array of attributes:
	 */
	private List<String> subtract_attr(List<String> attribute,String attr){
		attribute.remove(attr);
		return attribute;
	}
	
	/*
	 * checking if the current training-set there is homogeneous labeling:
	 */
	private boolean has_homogenous_class(List<FeatureVector> examples){
		String prev_val = examples.get(0).getName();
		for (FeatureVector fv : examples) {
			String curr_val = fv.getName();
			if(!curr_val.equals(prev_val)) {
				return false;
			}
		}
		return true;
	}
	
	/*
	 * computing the entropy for some attribute:
	 */
	private double calc_entropy(List<FeatureVector> train,String attribute,boolean is_label) {
		int count_one = 0;
		if(!is_label) {
			int attr = this._data.getAttrIndex(attribute) - 1;
			for(int i = 0; i < train.size(); i++) {
				FeatureVector record = train.get(i);
				if(((Double)record.getFeature(attr)).doubleValue() == 1.0) { // computing this value than calculating the complement value
					count_one++;
				}
			}
		} else {
			for(int i = 0; i < train.size(); i++) {
				FeatureVector record = train.get(i);
				if(record.getName().equals(attribute)) {
					count_one++;
				}
			}
		}
		double mul1 = ((double)count_one/train.size());
		double pvi = 0.0;
		if(mul1 != 0.0) {
			pvi = mul1*Math.log(mul1);
		}
		double mul2 = (1.0 - ((double)count_one/train.size()));
		double pvj = 0.0;
		if(mul2 != 0.0) {
			pvj = mul2*Math.log(mul2);
		}
		double entropy = (-1.0)*(pvi + pvj);
		return entropy;
	}
	
	/*
	 * finding the maximum Gain function:
	 */
	private double cond_prob_by_class(List<FeatureVector> train,String target,String attr) {
		int same = 0;
		for (FeatureVector fv : train) {
			if(fv.getFeature(this._data.getAttrIndex(attr) - 1) == 1.0) {
				if(fv.getName().equals(target)) {
					same++;
				}
			}
		}
		double res = 1.0*same/train.size();
		return res;
	}
	
	/**
	 * computing Gain function
	 * @param train	current training-set
	 * @param attributes	current attributes at this node to compute the best from this
	 * @return	best attribute by gain function
	 */
	public String select_by_gain(List<FeatureVector> train,List<String> attributes) {
		String target_name = train.get(0).getName();
		double class_entropy = this.calc_entropy(train,target_name,true);
		double max_cond_ent = 0.0;
		String future_attr = attributes.get(0);
		for (String cond_attr : attributes) {
			double prob = this.cond_prob_by_class(train, target_name, cond_attr); // probability
			double attr_entropy = this.calc_entropy(train, cond_attr, false); // attribute entropy value
			double cond_entropy = class_entropy - prob*attr_entropy;	//confitinal entropy computation
			if(cond_entropy > max_cond_ent) { // comparison to pick the best
				max_cond_ent = cond_entropy;
				future_attr = cond_attr;
			}
		}
		return future_attr;
	}
	
	/*
	 * making preorder and print values of this tour in the DT model:
	 */
    private void print_preorder_tree(Node node)
    {
        if (node == null || node.get_split_by() == null) {        	
            if((node != null)&&(node.get_goal_class() != -1)) {
            	System.out.println(this._data.convert_target(node.get_goal_class()));
        	}
        	return;
        }
        System.out.print(node + " ");
    	this.print_preorder_tree(node.getLeft());
        this.print_preorder_tree(node.getRight());
    }	
	
    /**
     * plotting graph which is depend on number of examples in the training-set and accuracy of the model to this classifications
     * @param points	points -> (amounts,accuracy for this amounts)
     * @param set_size	training-set size from the application initialization
     */
    public void plot_learning_curve(Map<Integer,Double> points,int set_size) {
    	SimpleGraphicsPlot.createAndShowGuiDT(points,set_size);
    }
}
