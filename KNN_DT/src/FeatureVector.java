package Assignment1D;

import java.util.ArrayList;
import java.util.List;

/**
 * Handling FeatureVector operations
 * @author Itay_Guy
 */
public class FeatureVector {
	private List<Double> _feature_vector; // numeric data
	private String _name; //data labeling
	
	public FeatureVector() {
		this._feature_vector = new ArrayList<Double>();
		this._name = new String();
	}
	
	public FeatureVector(String[] features) {
		this();
		for (String f : features) {
			try {
				double d = Double.parseDouble(f);
				this._feature_vector.add(d);
			}catch(Exception ex) {
				_name = new String(f.trim());
			}
		}
	}
	
	public FeatureVector(FeatureVector other) {
		this();
		for (Double d : other._feature_vector) {
			this._feature_vector.add(d);
		}
		this._name = new String(other._name);
	}
	
	/**
	 * Minkovski distance
	 * @param other	FeatureVector to comparison with
	 * @param p	if p=1 -> (Manhattan distance), if p=2 -> (euclidian distance),...
	 * @return	the distance number
	 */
	public double distance_p(FeatureVector other,int p) {
		double sum = 0.0;
		for (int i = 0; i < this._feature_vector.size(); i++) {
			sum += Math.pow(Math.abs(this.getFeature(i) - other.getFeature(i)),p);
		}
		return Math.pow(sum,1.0/p);
	}
	
	public String getName() {
		return this._name;
	}
	
	/**
	 * @param i	feature index
	 * @return	feature value at location i
	 */
	public double getFeature(int i) {
		return this._feature_vector.get(i);
	}
	
	public List<Double> get_feature_vector(){
		return this._feature_vector;
	}
	
	/*
	 * make revese to the feature vector
	 */
	private void reverse() {
		for(int i = 0;i < this._feature_vector.size() - 1;i++) {
			if(i == (this._feature_vector.size() - i - 1) || i > (this._feature_vector.size() - i - 1)) {
				return;
			}
			this._feature_vector.set(this._feature_vector.size() - i - 1,this._feature_vector.get(i));
		}
	}
	
	public int getSize() {
		return this._feature_vector.size() + 1;
	}
	
	public String toString() {
		String res = "Feature vector = [";
		for (Double d : this._feature_vector) {
			res += d + ",";
		}
		res += "Label:" + this._name + "]";
		return res;
	}
}
