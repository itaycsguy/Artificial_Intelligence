package Assignment1D;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * Handling the all Data-set
 * @author Itay_Guy
 */
public class Dataset {
	private Map<String,Integer> _class_map; // map from class name string to integer
	private List<String> _headers; // keep .csv file headers name and indices
	private Path _data_name; // path to data file
	private List<FeatureVector> _train_set;
	private List<FeatureVector> _test_set;
	
	public Dataset(String data_name) {
		this._data_name = Paths.get(data_name);
		this._train_set = new ArrayList<FeatureVector>();
		this._test_set = new ArrayList<FeatureVector>();
		this._class_map = new HashMap<String,Integer>();
	}
	
	/**
	 * Usage for mapping and tree splitting by name
	 * @param need_labels	if need to include labels with the attributes
	 * @return	all attributes
	 */
	public List<String> getAttributes(boolean need_labels){
		List<String> subheaders = new ArrayList<String>();
		if(!need_labels) {
			for (int i = 1; i < this._headers.size(); i++) {
				subheaders.add(this._headers.get(i));
			}
			return subheaders;
		}
		return this._headers;
	}
	
	/**
	 * @param best	the best attribute that is found using Gain function
	 * @return	best index or -1 if there no attribute as such
	 */
	public int getAttrIndex(String best) {
		for (int i = 1; i < this._headers.size(); i++) {
			if(best.equals(this._headers.get(i))) {
				return i;
			}
		}
		return -1;
	}
	
	/**
	 * class-integer converter from the data
	 * @param cl	class name
	 * @return	class tag number
	 */
	public int convert_class(String cl){
		return this._class_map.get(cl);
	}
	
	/**
	 * integer-class converter from the data
	 * @param target	class tag number
	 * @return	class name or null if there is no number as such
	 */
	public String convert_target(int target) {
		for (String cl : this._class_map.keySet()) {
			if(this._class_map.get(cl) == target) {
				return cl;
			}
		}
		return null;
	}
	
	/**
	 * prepare data to the model usage
	 * @param split_perc	training-set size percentage to split
	 * @param uniform	selection distribution type - if false there is random type
	 * @param headers	usage for DT-ID3 only and it says if there are headers to this data file
	 * @throws IOException
	 */
	public void prepare_data(int split_perc,boolean uniform,boolean headers) throws IOException {
		List<String> lines = Files.readAllLines(this._data_name); // taking data from Path file you provided in the constructor
		// handling headers:
		if(headers) {
			this._headers = new ArrayList<String>();
			String[] variables = lines.get(0).split(",");
			int i = 0;
			for (String var : variables) {
				this._headers.add(new String(var.trim()));
				i++;
			}
			lines.remove(0); // remove the labeling from headers array
		}
		int lines_amount = lines.size();
		int set_perc = (split_perc*lines.size())/100;
		List<Integer> seq = new ArrayList<Integer>();
		for (int i = 0; i < lines_amount; i++) {
			seq.add(i);
		}
		List<Integer> permut = this.make_permut(seq, lines_amount); // make randomized data lines
		if(!uniform) {
			// this is not uniform distribution -> take the first split_perc data lines to training set and the remain to test set
			for (int i = 0; i < lines_amount; i++) {
				String[] data_line = lines.get(permut.get(i)).split(",");
				if(i >= set_perc) {
					this._test_set.add(new FeatureVector(data_line));
				} else {
					this._train_set.add(new FeatureVector(data_line));
				}
			}
		} else {
			// this is distributed uniformly -> handling the issue there is same amounts in the training set:
			Map<String,Integer> types = new HashMap<String,Integer>();
			for (String line : lines) {
				String[] features = line.split(",");
				String name = features[features.length - 1].trim();
				if(types.containsKey(name)) {
					types.put(name,types.get(name).intValue()+1);
				} else {
					types.put(name,1);
				}
			}
			int uniform_chunk = lines_amount/types.keySet().size();
			for (String key : types.keySet()) {
				if(types.get(key).intValue() > uniform_chunk) {
					types.put(key,types.get(key).intValue()-(types.get(key).intValue()-uniform_chunk));
				}
			}
			boolean[] bucket = new boolean[lines_amount];
			for (int i = 0;i < bucket.length;i++) {
				bucket[i] = false;
			}
			for (int i = 0;i < lines_amount;i++) {
				String[] features = lines.get(permut.get(i)).split(",");
				String line_name = features[features.length - 1].trim();
				if(this._train_set.size() < set_perc){
					if(types.get(line_name).intValue() > 0) {
						types.put(line_name,types.get(line_name).intValue() - 1);
						this._train_set.add(new FeatureVector(features));
						bucket[permut.get(i)] = true;
					}
				} else {
					break;
				}
			}
			for (int i = 0;i < bucket.length;i++) {
				if(bucket[i] == false) {
					this._test_set.add(new FeatureVector(lines.get(i).split(",")));
				}
			}
		}
		this.prepare_class_mapping(this._train_set,this._test_set); // build class tag mapping
		System.out.println("Training-set size = " + this._train_set.size() + " , Test-set size = " + this._test_set.size());
	}
	
	/*
	 * build the class tag mapping into HashMap object
	 */
	private void prepare_class_mapping(List<FeatureVector> train,List<FeatureVector> test) {
		for(FeatureVector f : train) {
			if(!this._class_map.containsKey(f.getName())) {
				int new_cl = -1;
				for(Integer cl : this._class_map.values()) {
					if(cl > new_cl) {
						new_cl = cl;
					}
				}
				this._class_map.put(f.getName(), new_cl + 1);
			}
		}
	}
	
	/*
	 * return the training-set array
	 */
	public List<FeatureVector> getTrainingset(){
		ArrayList<FeatureVector> ret_train = new ArrayList<FeatureVector>();
		for (FeatureVector f : this._train_set) {
			ret_train.add(new FeatureVector(f));
		}
		return ret_train;
	}
	
	/*
	 * return the test-set array
	 */
	public List<FeatureVector> getTestSet(){
		ArrayList<FeatureVector> ret_test = new ArrayList<FeatureVector>();
		for (FeatureVector f : this._test_set) {
			ret_test.add(new FeatureVector(f));
		}
		return ret_test;
	}
 	
	/*
	 * make permutation to n items
	 */
	private List<Integer> make_permut(List<Integer> arr,int n){
		int rand = (new Random()).nextInt(n);
		int last = arr.get(n-1);
		arr.set(n-1,arr.get(rand));
		arr.set(rand,last);
		if(n == 1) {
			return arr;
		}
		return make_permut(arr, n-1);
	}
}
