package Assignment1D;

import java.awt.Dimension;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Handling KNN model
 * @author Itay_Guy
 */
public class KNN {
	private int P; // P for distance function
	
	public KNN(int p) {
		P = p;
	}
	
	public int getP() {
		return this.P;
	}
	
	/**
	 * pick k neighbors
	 * @param data	data-set object
	 * @param f	sample from the test-set
	 * @param k	for picking the k-neighbors
	 * @return	k neighbors into array
	 */
	public List<FeatureVector> make_subset(Dataset data,FeatureVector f,int k) {
		List<FeatureVector> train_set = data.getTrainingset();
		Collections.sort(train_set,new Comparator<FeatureVector>() { // sorting bottom up
			@Override
			public int compare(FeatureVector o1, FeatureVector o2) {
				double x = f.distance_p(o1,P);
				double y = f.distance_p(o2,P);
				if(x < y) return -1;
				else if(x > y) return 1;
				else return 0;
			}
		});
		List<FeatureVector> k_neighbors = new ArrayList<FeatureVector>();
		for (int i = 0; i < k;i++) {
			k_neighbors.add(new FeatureVector(train_set.get(i)));
		}
		return k_neighbors;
	}
	
	/**
	 * finding the class that the sample from test-set is belong to
	 * @param best_k	array of best k neighbor from "make_subset"
	 * @param weighted	true if need weighted technique and false otherwise
	 * @return	classified class for the test-set's sample
	 */
	public String choose_best(List<FeatureVector> best_k,boolean weighted) { // best_k is already sorted
		int k = best_k.size();
		Map<String, HashMap<String,Double>> freq = new HashMap<String,HashMap<String,Double>>();
		for (int i = 0; i < k; i++) {
			double eps = 0.0;
			if(weighted) {
				eps = 1.0*k-i; // weight for each neighbor by their distance from f
			}
			
			HashMap<String,Double> h = new HashMap<String,Double>();
			h.put("weight",1.0 + eps);
			freq.put(best_k.get(i).getName() + "@" + i, h);
		}
		Map<String,Double> counter = new HashMap<String,Double>();
		for (String key : freq.keySet()) {
			HashMap<String,Double> h = freq.get(key);
			double val = h.get("weight").doubleValue();
			key = key.split("@")[0];
			if(!counter.containsKey(key)) {
				counter.put(key,val);
			} else {
				counter.put(key,counter.get(key) + val);
			}
		}
		String max_label = "";
		double max_appearances = 0.0;
		for (String key : counter.keySet()) {
			double val = counter.get(key).doubleValue();
			if(val > max_appearances) {
				max_appearances = val;
				max_label = key;
			}
		}
		return  max_label;	
	}
	
	/**
	 * making classification for some data - training-set or test-set
	 * @param data	Data-set
	 * @param k	number of neighbors
	 * @param weighted	need to use weighted method for finding the class between k neighbors
	 * @param validate_training_set	if need to classify the training-set for learning curve for example
	 * @return	confusion matrix
	 */
	public Map<String,HashMap<String,Integer>> classify(Dataset data,int k,boolean weighted,boolean validate_training_set) {
		List<FeatureVector> test = null;
		if(validate_training_set) {
			System.out.println("******\nClassification: Training-Set");
			test = data.getTrainingset();
		} else {
			System.out.println("******\nClassification: Test-Set");
			test = data.getTestSet();
		}
		Map<String,HashMap<String,Integer>> confusion_matrix = new HashMap<String,HashMap<String,Integer>>();
		System.out.println("\nUsing k = " + k + " and Weighted-Sum = " + weighted + "\n******");
		for (FeatureVector f_test : test) {
			System.out.println("For Test Example: " + f_test);
			List<FeatureVector> best_k = this.make_subset(data,f_test, k);
			int i = 0;
			for (FeatureVector fv : best_k) {
				System.out.println("\tNeighbor " + i + ": " + fv);
				i++;
			}
			String res = this.choose_best(best_k, weighted); // weighted = true of false
			if(res.equals(f_test.getName())) {
				System.out.println("\t+ " + res + " Classified correctly");
			} else {
				System.out.println("\t- Truth value: " + f_test.getName() + " => but classified as: " + res);
			}
			// build confusion matrix
			HashMap<String,Integer> h = new HashMap<String,Integer>();
			if(confusion_matrix.keySet().contains(f_test.getName())){
				if(confusion_matrix.get(f_test.getName()).keySet().contains(res)) {
					confusion_matrix.get(f_test.getName()).put(res,confusion_matrix.get(f_test.getName()).get(res) + 1);					
				} else {
					h.put(res,1);
					confusion_matrix.get(f_test.getName()).put(res,1);					
				}
			} else {
				h.put(res,1);
				confusion_matrix.put(f_test.getName(),h);
			}
		}
		ArrayList<String> titles = new ArrayList<String>();
		for (String name : confusion_matrix.keySet()) {
			titles.add(name);
		}
		for (String name : confusion_matrix.keySet()) {
			for (int i = 0; i < titles.size(); i++) {
				if(!confusion_matrix.get(name).containsKey(titles.get(i))) {
					confusion_matrix.get(name).put(titles.get(i),0);
				}
			}
		}
		return confusion_matrix;
	}
	
	/**
	 * classification for same data examples
	 * @param data	Data-set
	 * @param k	number of neighbors
	 * @param weighted	need to use weighted method or not
	 * @param need_confution_matrix_disp	if we would like to display the confusion matrix
	 * @param validate_training_set	true if we need to classify the training-set
	 * @return	match accuracy of this model
	 */
	public double classify_by_accuracy(Dataset data,int k,boolean weighted,boolean need_confution_matrix_disp,boolean validate_training_set) {
		Map<String,HashMap<String,Integer>> confusion_matrix = this.classify(data, k, weighted,validate_training_set);
		if(need_confution_matrix_disp) {
			System.out.println(KNN.disp_confusion_matrix(confusion_matrix));
		}
		int total = 0;
		int correct = 0;
		//counting how many is correct predictions:
		for (String key : confusion_matrix.keySet()) {
			for(String name : confusion_matrix.get(key).keySet()){
				if(key.equals(name)) {
					correct += confusion_matrix.get(key).get(name);
				}
				total += confusion_matrix.get(key).get(name);
			}
		}
		if(!need_confution_matrix_disp) {
			System.out.println("******");
		}
		double accuracy = correct*100.0/total;
		System.out.println("Accuracy = " + accuracy);
		return accuracy;
	}
	
	/**
	 * execution for classification some data
	 * @param d	Data-set
	 * @param k	number of neighbors
	 * @param P	distance parameter
	 * @param weighted	true if weighted method is need to be used
	 * @param validate_training_set	true if need to classify the training-set
	 * @return	accuracy number
	 */
	public static double run_by_accuracy(Dataset d,int k,int P,boolean weighted,boolean disp_confution_matrix,boolean validate_training_set) {
		KNN knn = new KNN(P);
		double corr_precision = knn.classify_by_accuracy(d,k,weighted,disp_confution_matrix,validate_training_set);
		return corr_precision;
	}
	
	/**
	 * execution for classification some data
	 * @param d	Data-set
	 * @param k	number of neighbors
	 * @param P	distance parameter
	 * @param weighted	true if weighted method is need to be used
	 * @param validate_training_set	true if need to classify the training-set
	 * @return	confusion matrix
	 */
	public static Map<String,HashMap<String,Integer>> run(Dataset d,int k,int P,boolean weighted,boolean validate_training_set) {
		KNN knn = new KNN(P);
		Map<String,HashMap<String,Integer>> confusion_matrix = knn.classify(d,k,weighted,validate_training_set);
		return confusion_matrix;
	}
	
	/**
	 * building very friendly display of machine learning parameter [such as: confuction matrix,precision,recall and F1-measure]
	 * @param confusion_matrix
	 * @return	display String object
	 */
	public static String disp_confusion_matrix(Map<String,HashMap<String,Integer>> confusion_matrix) {
		List<Double> f_measure = new ArrayList<Double>();
		double prec = 0.0,rec = 0.0;
		String precision = "";
		String recall = "";
		String[] S = new String[confusion_matrix.keySet().size()];
		String[] keys = confusion_matrix.keySet().toArray(S);
		String sol = "******\nConfusion Matrix: \n\t\t\t";
		for(int i = 0;i < keys.length;i++) {
			sol += keys[i] + "\t\t";
		}
		sol += "\n";
		for(int i = 0;i < keys.length;i++) {
			sol += "\t" + keys[i] + "\t\t";
			precision += keys[i] + " Precision = ";
			int counter_prec = 0,counter_rec = 0;
			boolean col_in = false;
			for(int j = 0;j < keys.length;j++) {
				if(confusion_matrix.get(keys[i]).get(keys[j]) != null) {
					counter_prec += confusion_matrix.get(keys[i]).get(keys[j]);
					sol += confusion_matrix.get(keys[i]).get(keys[j]);
					if(!col_in) {
						recall += keys[i] + " Recall = ";
						for (int col = 0; col < keys.length; col++) {
							counter_rec += confusion_matrix.get(keys[col]).get(keys[i]);
						}
						if(counter_rec > 0) {
							rec = confusion_matrix.get(keys[i]).get(keys[i])*100.0/counter_rec;
							recall += confusion_matrix.get(keys[i]).get(keys[i])*100.0/counter_rec + "\n";
						} else {
							recall += 0.0 + "\n";
						}
						col_in = true;
					}
				} else {
					sol += 0;
				}
				sol += "\t\t\t";
			}
			if(counter_prec > 0) {
				prec = confusion_matrix.get(keys[i]).get(keys[i])*100.0/counter_prec;
				precision += confusion_matrix.get(keys[i]).get(keys[i])*100.0/counter_prec + "\n";
			} else {
				precision += 0.0 + "\n";
			}
			sol += "\n";
			f_measure.add(prec);
			f_measure.add(rec);
			prec = 0.0;
			rec = 0.0;
		}
		
		sol += "******\n" + precision + "******\n" + recall + "******\n";
		for(int i = 0;i < keys.length;i++) {
			sol += "F-[" + keys[i] + "]-measure = " + 2.0*f_measure.get(0)*f_measure.get(1)/(f_measure.get(0) + f_measure.get(1)) + "\n";
			try {
				f_measure.remove(0);
				f_measure.remove(1);
			}catch(Exception ex) {}
		}
		return sol + "******";
	}
	
	/**
	 * build plotting data points
	 * @param d	Data-set
	 * @param set_size	training-set size
	 * @param p	distance parameter
	 * @param weighted	true if need weighted method to be used
	 * @param validate_training_set	true if need to classify training-set
	 * @return	points ready to plot
	 */
	public static Map<Integer,HashMap<Integer,Double>> get_plot_learning_curve(Dataset d,int set_size,int p,boolean weighted,boolean validate_training_set) {
		Map<Integer,HashMap<Integer,Double>> plot_points = new HashMap<Integer,HashMap<Integer,Double>>();
		HashMap<Integer,Double> h = new HashMap<Integer,Double>();
		int MAX_K = d.getTrainingset().size();
		boolean disp_confution_matrix = true;
		Map<String,HashMap<String,Integer>> confusion_matrix = null;
		for (int k = 1; k < MAX_K; k++) {
			confusion_matrix = KNN.run(d,k,p,weighted,validate_training_set);
			double accuracy = KNN.run_by_accuracy(d,k,p,weighted,disp_confution_matrix,validate_training_set);
			h.put(k,accuracy);
			confusion_matrix.clear();
		}
		plot_points.put(set_size,h);
		return plot_points;
	}
	
	/**
	 * invoker to Graphic class
	 * @param train_plot
	 * @param test_plot
	 * @param set_size
	 */
	public static void plot_graphics(Map<Integer,HashMap<Integer,Double>> train_plot,Map<Integer,HashMap<Integer,Double>> test_plot,int set_size) {
    	MultiGraphicsPlot.createAndShowGuiKNN(train_plot, test_plot, set_size);
	}
}