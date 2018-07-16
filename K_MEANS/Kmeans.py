import pandas as pd,numpy as np,seaborn as sns,random,matplotlib.pyplot as plt,argparse,sys,os

class Kmeans():
	#kind of selection algorithms:
	FORGY = 'forgy'
	RANDOM = 'random'
	INIT_PLUS_PLUS = 'init++'
	RAND_INT = 10   #using in random selection method
	XHeaders = ['X1', 'X2', 'X3', 'X4'] #dataframe feature columns
	YHeader = ['Y'] #dataframe labels column
	def __init__(self):
		self.__trainset = None
		self.__ytrain = None
		self.__kmeans = None
		self.__k = 3
		self.__MAX_ITER = 3
		self.__selection_method = Kmeans.FORGY


	"""
	path - iris data file path
	"""
	def load_data(self,path):
		dataset = pd.DataFrame(columns=Kmeans.XHeaders)
		y = pd.DataFrame(columns=Kmeans.YHeader)
		with open(path.strip(),"r") as d:
			lines = d.readlines()
			for i,line in enumerate(lines):
				line = line.split(",")
				dataset.loc[i] = line[0:len(line) - 1]
				y.loc[i] = line[len(line) - 1]
		self.__trainset = dataset
		self.__ytrain = y


	def fit(self,k=3,max_iter=3,selection_method='forgy'):
		if k <= 0:
			k = 3
		if max_iter <= 0:
			max_iter = 3
		self.__MAX_ITER = max_iter
		self.__k = k
		self.__selection_method = selection_method
		[distances_hash,labels_hash] = self.__init_hash_distances(k)
		kmeans = pd.DataFrame(columns=Kmeans.XHeaders)
		if selection_method == Kmeans.FORGY:
			kmeans = self.__forgy(k)
		elif selection_method == Kmeans.RANDOM:
			kmeans = self.__random(k)
		elif selection_method == Kmeans.INIT_PLUS_PLUS:
			kmeans = self.__init_plusPlus(k)
		iteration = 0
		while iteration < self.__MAX_ITER:
			for index,row in self.__trainset.iterrows():
				min_centroid_index = self.__find_min_centroid_dist(kmeans,row)
				min_centroid_index = str(min_centroid_index)
				distances_hash[min_centroid_index].append({str(index):row})
				labels_hash[min_centroid_index].append({str(index):self.__ytrain.loc[index]})

			new_kmeans = self.__avg_and_flush(distances_hash)
			if self.__has_coverage(kmeans,new_kmeans):
				self.__kmeans = new_kmeans
				break

			kmeans = new_kmeans
			[distances_hash,labels_hash] = self.__init_hash_distances(k)


	def __init_hash_distances(self,k):
		distances_hash = {}
		labels_hash = {}
		for i in range(0, k):
			distances_hash[str(i)] = list()
			labels_hash[str(i)] = list()
		return [distances_hash,labels_hash]


	"""
	n - number of seeds we have to sample randomly from the concrete data
	"""
	def __forgy(self,k):
		kmeans = pd.DataFrame(columns=Kmeans.XHeaders)
		data_indecies = np.asarray(range(0,len(self.__trainset)))
		np.random.shuffle(data_indecies)
		for idx,r in enumerate(data_indecies):
			#break out after k steps:
			if idx >= k:
				break
			kmeans.loc[idx] = self.__trainset.iloc[r]
		return kmeans


	"""
	k - number of vector seeds we should create and fill-in with random numbers
	"""
	def __random(self,k):
		kmeans = pd.DataFrame(columns=Kmeans.XHeaders)
		for i in range(0, k):
			kmeans.loc[i] = np.random.rand(len(Kmeans.XHeaders))
			for idx, item in enumerate(kmeans.loc[i]):
				rand_int = random.uniform(1, Kmeans.RAND_INT)
				kmeans.loc[i][idx] = rand_int * kmeans.loc[i][idx]
		return kmeans



	"""
	k - number of seeds we should pick in smart probability way
	"""
	def __init_plusPlus(self,k):
		kmeans = pd.DataFrame(columns=Kmeans.XHeaders)
		samp_df = self.__trainset.sample(n=1)
		k_idx = [samp_df.index.values.astype(int)[0]]
		kmeans.loc[0] = np.asarray(samp_df)[0]
		#each iterate we pick the next vector:
		for i in range(1, k):
			last_mean = np.asarray(kmeans)
			last_mean = last_mean[len(last_mean) - 1]
			for idx, _ in enumerate(last_mean):
				last_mean[idx] = float(last_mean[idx])
			next_sample_vec = None
			max_prop = 0
			total_sum = 0.0
			#computing the sum:
			for control, row in self.__trainset.iterrows():
				if control not in k_idx:
					row = np.asarray(row)
					for idx, _ in enumerate(row):
						row[idx] = float(row[idx])
					local_sum = 0.0
					for k1,k2 in zip(row,last_mean):
						local_sum = (k1 - k2) ** 2
					total_sum = abs(total_sum + local_sum)
			#computing the probability and pick the far away one:
			for control, row in self.__trainset.iterrows():
				if control not in k_idx:
					row = np.asarray(row)
					for idx, _ in enumerate(row):
						row[idx] = float(row[idx])
					local_sum = 0.0
					for k1,k2 in zip(row,last_mean):
						local_sum = (k1 - k2) ** 2
					curr_prop = local_sum*(1/total_sum)
					if curr_prop > max_prop:
						max_prop = curr_prop
						next_sample_vec = row
			if next_sample_vec is not None:
				kmeans.loc[i - 1] = next_sample_vec
		return kmeans


	def __has_coverage(self,curr_kmeans,new_kmeans):
		iter_item = 0
		for _, new_row in new_kmeans.iterrows():
			new_row = np.asarray(new_row)
			curr_row = np.asarray(curr_kmeans.iloc[iter_item])
			if not np.array_equal(new_row,curr_row):
				return False
			iter_item = iter_item + 1
		return True


	def __avg_and_flush(self,distances_hash):
		new_kmeans = pd.DataFrame(columns=Kmeans.XHeaders)
		i = 0
		for _,arrays in zip(distances_hash.keys(),distances_hash.values()):
			avg_arr = np.zeros(len(Kmeans.XHeaders))
			amount = 0
			for h in arrays:
				if h == {}:
					break
				for _,value in zip(h.keys(),h.values()):
					value = np.asarray(value)
					avg_arr = np.asarray(avg_arr)
					avg_arr = avg_arr + value
					amount = amount + 1
			if amount != 0:
				new_kmeans.loc[i] = avg_arr*(1/amount)
			i = i + 1
		return new_kmeans


	def __find_min_centroid_dist(self,kmeans,row):
		min_centroid_index = -1
		min_centroid_distance = np.Inf
		centroid_iter = 0
		for _, centroid_row in kmeans.iterrows():
			row = np.asarray(row)
			centroid_row = np.asarray(centroid_row)
			for idx in range(0, len(row)):
				row[idx] = float(row[idx])
				centroid_row[idx] = float(centroid_row[idx])
			distance = self.__calc_p_dist(row, centroid_row)
			if distance < min_centroid_distance:
				min_centroid_index = centroid_iter
				min_centroid_distance = distance
			centroid_iter = centroid_iter + 1
		return min_centroid_index


	def __calc_p_dist(self,a,b,p=2):
		if p <= 0:
			p = 2
		distance = 0.0
		for val1,val2 in zip(a,b):
			distance = distance + (val1 - val2)**p
		return distance**(1/p)


	def predict(self):
		seed_solutions = {}
		seed_label = {}
		dataset = self.__trainset
		yset = self.__ytrain
		for idx, row in dataset.iterrows():
			min_dist_centroid_index = self.__find_min_centroid_dist(self.__kmeans,row)
			min_dist_centroid_index = str(min_dist_centroid_index)
			row = np.asarray(row)
			if min_dist_centroid_index in seed_solutions.keys():
				seed_solutions[str(min_dist_centroid_index)].append(row)
				seed_label[str(min_dist_centroid_index)].append(yset.loc[idx])
			else:
				seed_solutions[str(min_dist_centroid_index)] = [row]
				seed_label[str(min_dist_centroid_index)] = [yset.loc[idx]]
		return [seed_solutions,seed_label]


	def __calc_inertia(self,preds):
		inertia_hash = {}
		for i in range(0,self.__k):
			i = str(i)
			inertia_hash[i] = list()
		for p,m in zip(preds,self.__kmeans.iterrows()):
			curr_diff = 0.0
			p = str(p)
			for vec in preds[p]:
				vec = np.asarray(vec)
				sum = 0.0
				for item in range(0,len(Kmeans.XHeaders)):
					diff = float(vec[item]) - float(m[1][item])
					sum = sum + diff**2
				curr_diff = curr_diff + sum
			inertia_hash[p] = curr_diff
		return inertia_hash


	def print_inertia_graph(self,samples_num=10,show_inetia_matrix=False):
		if samples_num <= 0:
			samples_num = 10
		cols = list()
		for i in range(0,samples_num):
			cols.append(str(i + 1))
		df = pd.DataFrame(data=np.array([np.zeros(samples_num)]*samples_num).T.astype(str),columns=cols)
		for i in range(0,samples_num):
			self.fit(k=(i + 1))
			[pred, _] = kmeans.predict()
			inertia = self.__calc_inertia(pred)
			for key,value in zip(inertia.keys(),inertia.values()):
				df.loc[i][str(int(key) + 1)] = str(value)
		if show_inetia_matrix:
			print(df.to_string())
		sns.barplot(data=df)
		plt.xlabel("K Clusters")
		plt.ylabel("Inertia")
		plt.title("Algorithm Inertia Convergence")
		plt.show()


	def print_successive_table(self):
		[_,labels] = kmeans.predict()
		types = {}
		for i in range(0,self.__k):
			i = str(i)
			types[i] = 0
		index = list()
		for i in range(0,self.__k):
			index.append(int(i))
		table = np.array([np.zeros(self.__k)]*3).T.astype(int)
		df = pd.DataFrame(data=table,columns=['setosa','versicolor','virginica'],index=index)
		for lk,lv in zip(labels.keys(),labels.values()):
			lk = int(lk)
			for type in lv:
				if type['Y'].strip() == 'Iris-setosa':
					df.loc[lk]['setosa'] = df.loc[lk]['setosa'] + 1
				if type['Y'].strip() == 'Iris-versicolor':
					df.loc[lk]['versicolor'] = df.loc[lk]['versicolor'] + 1
				if type['Y'].strip() == 'Iris-virginica':
					df.loc[lk]['virginica'] = df.loc[lk]['virginica'] + 1
		print("Using Selection method =",self.__selection_method,"and k =",self.__k,":")
		print("==================================")
		print(df.to_string())


#Main of program
if __name__ == "__main__":
	parser = argparse.ArgumentParser(description='Processing Kmeans inputs')
	parser.add_argument('--file',default=None,type=str,help="The data-set file.")	
	parser.add_argument('--k',default=3,type=int,help="Some integer >> 0, k=3 is the default")
	parser.add_argument('--plot_inertia_graph',type=str,default="true",help="Inertia compares with sequence of different k to k-means")
	parser.add_argument('--selection_method',type=str,default='all',help="There are 4 options to initialize: all,random,forgy,init++")
	args = parser.parse_args()
	v = vars(args)
	help1 = "--help"
	help2 = "-h"
	for item in sys.argv:
		if item.strip().lower() == help1 or item.strip().lower() == help2:
			parser.print_help(sys.argv)
			break
	file = str(v['file']).strip()
	if not file:
		print("No file is attached.")
		sys.exit(0)
	elif not os.path.exists(file):
		print("The file is not exist.")
		sys.exit(0)
	k = int(v['k'])
	if k <= 0:
		print("k should be integer type > 0, setting to 3.")
		k = 3

	inertia = str(v['plot_inertia_graph']).strip().lower()
	op = ['true','false']
	if inertia not in op:
		print("Incorrect plot_inertia_graph, setting to true.")
		inertia = 'true'
	selection_method = str(v['selection_method']).strip().lower()
	op = ['all','forgy','random','init++']
	if selection_method not in op:
		print("Incorrect selection_method, setting to all.")
		selection_method = 'all'

	print("")
	#operate the App:
	kmeans = Kmeans()
	kmeans.load_data(file)
	if selection_method == 'all':
		kmeans.fit(k=k,selection_method='forgy')
		kmeans.print_successive_table()
		print("***")
		kmeans.fit(k=k,selection_method='random')
		kmeans.print_successive_table()
		print("***")
		kmeans.fit(k=k,selection_method='init++')
		kmeans.print_successive_table()
	elif selection_method == 'forgy':
		kmeans.fit(k=k,selection_method='forgy')
		kmeans.print_successive_table()
	elif selection_method == 'random':
		kmeans.fit(k=k,selection_method='random')
		kmeans.print_successive_table()
	elif selection_method == 'init++':
		kmeans.fit(k=k,selection_method='init++')
		kmeans.print_successive_table()
	if inertia == 'true':
		kmeans.print_inertia_graph()
	
	print("")