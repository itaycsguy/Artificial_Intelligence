package Assignment1D;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Handling human communication with the user
 * @author Itay_Guy
 */
public class DriverML {
	public static void main(String[] args) {
		String name = null;	// full path
		String algorithm = "knn";	// knn or dt
		int training_set_size = 80;
		boolean uniform = false; // data splitting connection
		boolean need_learning_curve = false;
		boolean weighted = false;
		int k = 7;
		int P = 2;
		try {
			name = args[0].trim();
		}catch(Exception ex) {
			System.out.println("Need at-least some dataset file with absolute path to initial the application, try again.");
			System.exit(0);
		}
		try {
			algorithm = args[1].trim();
		}catch(Exception ex) {}
		try {
			training_set_size = Integer.parseInt(args[2].trim());
		}catch(Exception ex) {}
		try {
			uniform = Boolean.parseBoolean(args[3].trim());
		}catch(Exception ex) {}
		try {
			need_learning_curve = Boolean.parseBoolean(args[4].trim());
		}catch(Exception ex) {}
		try {
			weighted = Boolean.parseBoolean(args[5].trim());
		}catch(Exception ex) {}
		try {
			k = Integer.parseInt(args[6].trim());
		}catch(Exception ex) {}
		try {
			P = Integer.parseInt(args[7].trim());
		}catch(Exception ex) {}
		Dataset d = new Dataset(name);
		// pick some algorithm:
		if(algorithm.equals("dt")) {
			boolean headers = true;
			try {
				d.prepare_data(training_set_size,uniform,headers);
			} catch (IOException e) {
				e.printStackTrace();
			}
			DecisionTree tree = new DecisionTree(d);
			tree.fit();
			if(need_learning_curve) {
				Map<Integer,Double> points = new HashMap<Integer,Double>();
				int test_size = d.getTestSet().size();
				for(int i = 0;i < test_size;i++) {
					points.put(i,tree.predict(i)*100);
				}
				tree.plot_learning_curve(points,training_set_size);
			} else {
				tree.predict(-1);
			}
		} else if(algorithm.equals("knn")) {
			boolean  headers = false;
			try {
				d.prepare_data(training_set_size,uniform,headers);
			} catch (IOException e) {
				e.printStackTrace();
			}
			System.out.println("Distance Method: Minkovski Distance with P = " + P);
			if(need_learning_curve) {
				Map<Integer,HashMap<Integer,Double>> train_plot = KNN.get_plot_learning_curve(d,training_set_size,P,weighted,true);
				Map<Integer,HashMap<Integer,Double>> test_plot = KNN.get_plot_learning_curve(d,training_set_size,P,weighted,false);
				KNN.plot_graphics(train_plot,test_plot,training_set_size);
				for(Integer t1 : train_plot.keySet()) {
					for (Integer i : train_plot.get(t1).keySet()) {
						System.out.println(i + "\t" + train_plot.get(t1).get(i));
					}
				}
				for(Integer t1 : test_plot.keySet()) {
					for (Integer i : test_plot.get(t1).keySet()) {
						System.out.println(i + "\t" + test_plot.get(t1).get(i));
					}
				}
			} else {
				boolean disp_confusion_matrix = true;
				KNN.run_by_accuracy(d, k, P, weighted,disp_confusion_matrix,true);
				KNN.run_by_accuracy(d, k, P, weighted,disp_confusion_matrix,false);
			}
		}
	}
}
