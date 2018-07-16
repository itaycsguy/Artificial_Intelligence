//#include <SDKDDKVer.h>
//#include <tchar.h>

#include <stdio.h>

#pragma execution_character_set("utf-8")
#pragma warning(disable:4786)		// disable debug warning

#include <iostream>					// for cout etc.
#include <vector>					// for vector class
#include <string>					// for string class
#include <algorithm>				// for sort algorithm
#include <time.h>					// for random seed
#include <math.h>					// for abs()

#define GA_POPSIZE		2048		// ga population size
#define GA_MAXITER		16384		// maximum iterations
#define GA_ELITRATE		0.25f		// elitism rate
#define GA_MUTATIONRATE	0.10f		// mutation rate
#define GA_MUTATION		RAND_MAX * GA_MUTATIONRATE

using namespace std;				// polluting global namespace, but hey...
string GA_TARGET;

struct ga_struct
{
	time_t elapsedTime;
	string str;						// the string
	double mean;
	double std;
	double cpuTicks;
	int caughtPlaces;
	int fitness;					// its fitness
	int sumFitnesses;
};

typedef vector<ga_struct> ga_vector;// for brevity

namespace Genetic1{
	class Ga_CrossOver {
	public:
		static void onePoint(ga_struct &populationi1, ga_struct &populationi2, ga_struct &bufferi, int spos)
		{
			bufferi.str = populationi1.str.substr(0, spos) +
				populationi2.str.substr(spos, GA_TARGET.size() - spos);
		}
		static void twoPoints(ga_struct &populationi1, ga_struct &populationi2, ga_struct &bufferi, int spos1, int spos2)
		{
			bufferi.str = populationi1.str.substr(0, spos1) +
				populationi2.str.substr(spos1, spos2 - spos1) +
				populationi1.str.substr(spos2, GA_TARGET.size() - spos2);
		}
		static void uniform(ga_struct &populationi1, ga_struct &populationi2, ga_struct &bufferi)
		{
			for(int i = 0;i < GA_TARGET.size();i++){
				int gen_to_select = rand() % 2;
				if(gen_to_select == 0){
					bufferi.str[i] = populationi1.str[i]; 
				} else {
					bufferi.str[i] = populationi2.str[i];
				}
			}
		}
	};

	class Ga_Selection {
	public:
		static void elitism(ga_vector &population, ga_vector &buffer, int esize)
		{
			for (int i = 0; i<esize; i++)
			{
				buffer[i] = population[i];
				buffer[i].str = population[i].str;
			}
		}
		static int SUS(ga_vector &population, ga_vector &buffer)
		{
			ga_vector leftOver(GA_POPSIZE);
			double m = 0;
			int sum = population[0].fitness;
			for (int i = 0; i < population.size(); i++)
			{
				m += population[i].fitness;
			}
			m /= population.size(); // finding avg of population
			double r = (double)rand() / (RAND_MAX + 1);// pick random number between 0 to 1
			double delta = r * m;
			int j = 0, i = 0, k = 0;
			do {
				if (delta < sum) // for some delta we divide the population by sum - that is the point of this method [part of roullete]
				{
					buffer[i] = population[j];
					buffer[i].str = population[j].str;
					i++;
					delta += sum;
				}
				else
				{
					// keep into leftOver all individuals about sum less of equals than delta
					sum += population[j].fitness;
					leftOver[k] = population[j];
					leftOver[k].str = population[j].str;
					k++;
					j++;
				}
			} while (j < population.size());
			int startRest = i;
			for (k = i, j = 0; k < GA_POPSIZE - i; k++, j++) // coping leftOvers to the buffer's back
			{
				buffer[k] = leftOver[j];
				buffer[k].str = leftOver[j].str;
			}
			return startRest;
		}
		static int Tournament(ga_vector &population, ga_vector &buffer)
		{
			int K = 2;	//rand() % GA_POPSIZE;
			vector<int> rest;
			ga_struct best;
			bool isBestNull = true;
			for (int i = 0; i < K; i++) // taking K best random indecies
			{
				int ind = rand() % population.size();
				if (isBestNull || (best.fitness > population[ind].fitness))
				{
					best = population[ind];
					isBestNull = false;
					rest.push_back(ind);
				}
			}
			// copy relevant object to buffer
			int j = 0, k = 0;
			for (int i = 0; i < GA_POPSIZE; i++)
			{
				if ((k < rest.size()) && (i == rest[k]))
				{
					buffer[j] = population[i];
					j++;
					k++;
				}
			}
			int restStart = j; // esize index for next mutation the rest population before next round
			k = 0;
			// copy the rest to buffer - the order between two groups is very important
			for (int i = 0; (k < rest.size()) && (j < GA_POPSIZE) && (i < GA_POPSIZE); i++)
			{
				if (i != rest[k])
				{
					buffer[j] = population[i];
					j++;
					k++;
				}
			}
			return restStart;
		}
		static int Turnir(ga_vector &population, ga_vector &buffer)
		{
			int K = 2;//rand() % GA_POPSIZE;
			vector<int> rest;
			int j = 0;
			for (int i = 0; i < K; i++)
			{
				int ipos = rand() % GA_POPSIZE;
				int jpos = rand() % GA_POPSIZE;
				bool SEEN = false;
				for (int h = 0; h < i; h++)
				{
					if (rest[h] == ipos || rest[h] == jpos)
					{
						// pick k random indivuduals and check there is no duplications
						h = i;
						SEEN = true;
					}
				}
				if (SEEN == true)
				{
					continue;
				}
				if (population[ipos].fitness < population[jpos].fitness)
				{
					// if one individual is bigger than the other he will win and pass to the next generation
					buffer[j] = population[ipos];
					rest.push_back(jpos);
				}
				else
				{
					buffer[j] = population[jpos];
					rest.push_back(ipos);
				}
				j++;
			}
			sort(rest.begin(), rest.end());
			int restStart = j;
			int k = 0;
			for (int i = 0, k = 0; i < GA_POPSIZE; i++)	// put in the buffer's back
			{
				if (i == rest[k])
				{
					buffer[j] = population[i]; // rest[j]
					j++;
					k++;
				}
			}
			return restStart;
		}
	};

	class Ga_Mutate {
	public:
		static void simpleReverse(ga_struct &member)
		{
			// reverse chuck of str in member - very simple method:
			int tsize = GA_TARGET.size();
			int ipos = rand() % tsize;
			int jpos = rand() % tsize;

			int low = 0, high = 0;
			if (ipos < jpos)
			{
				low = ipos;
				high = jpos;
			}
			else
			{
				low = jpos;
				high = ipos;
			}
			string s;
			for (int i = 0; i < low; i++)
			{
				s += member.str[i];
			}
			string toRev;
			for (int i = low; i <= high; i++)
			{
				toRev += member.str[i];
			}
			reverse(toRev.begin(), toRev.end());
			s += toRev;
			for (int i = high + 1; i < member.str.length(); i++)
			{
				s += member.str[i];
			}
			member.str = s;
		}
		static void swap(ga_struct &member)
		{
			// swapping between two indecies - very simple method:
			int tsize = GA_TARGET.size();
			int ipos = rand() % tsize;
			int jpos = rand() % tsize;

			char temp = member.str[ipos];
			member.str[ipos] = member.str[jpos];
			member.str[jpos] = temp;
		}
		static void insertion(ga_struct &member)
		{
			// put random letter in another random place in str of member
			int tsize = GA_TARGET.size();
			int ipos = rand() % tsize;
			int nextPlace = rand() % tsize;

			string temp;
			for (int i = 0; i < tsize; i++)
			{
				if (i != ipos)
				{
					temp += member.str[i];
				}
				else if (i == nextPlace)
				{
					temp += member.str[ipos];
				}
			}
			for (int i = 0; i < tsize; i++)
			{
				member.str[i] = temp[i];
			}
		}
		static void mutate(ga_struct &member)
		{
			// change random place to random letter
			int tsize = GA_TARGET.size();
			int ipos = rand() % tsize;
			int delta = (rand() % 90) + 32;

			member.str[ipos] = ((member.str[ipos] + delta) % 122);
		}
	};
}
class Population {
static void calc_ga_stat(ga_vector &population)
{
	unsigned int sumFitness = 0;
	for (int i = 0; i < GA_POPSIZE; i++) // sum all
	{
		sumFitness += population[i].fitness;
	}
	double mean = sumFitness / GA_POPSIZE; // uniform mean
	double std = 0;
	for (int i = 0; i < GA_POPSIZE; i++)
	{
		double fitness = population[i].fitness;
		std += pow(fitness - mean, 2);
	}
	std = sqrt(std); // simple std as we recognize with
	for (int i = 0; i < GA_POPSIZE; i++)
	{
		population[i].mean = mean;
		population[i].std = std;
	}
}
static bool fitness_sort(ga_struct x, ga_struct y)
{
	return (x.fitness < y.fitness);
}
public:
	static void init_population(ga_vector &population,ga_vector &buffer)
	{
		int tsize = GA_TARGET.size();

		for (int i = 0; i < GA_POPSIZE; i++) {
			ga_struct citizen;

			citizen.fitness = 0;
			citizen.str.erase();

			for (int j = 0; j < tsize; j++)
				citizen.str += (rand() % 90) + 32;

			population.push_back(citizen);
		}

		buffer.resize(GA_POPSIZE);
	}
	static void calc_fitness(ga_vector &population, clock_t startClock, time_t  startTime,string fitnessEstimation = "l1")
	{
		string target = GA_TARGET;
		int tsize = target.size();
		if (fitnessEstimation.compare("l1") == 0)	// letters distance
		{
			unsigned int fitness;

			for (int i = 0; i < GA_POPSIZE; i++) {
				fitness = 0;
				for (int j = 0; j < tsize; j++) {
					fitness += abs(int(population[i].str[j] - target[j]));
				}

				population[i].fitness = fitness;
				population[i].cpuTicks = ((double)(clock() - startClock));
				population[i].elapsedTime = time(&population[i].elapsedTime) - startTime;
			}
		}
		else if (fitnessEstimation.compare("BACS") == 0)	// Bull And Cows
		{
			for (int i = 0; i < GA_POPSIZE; i++)
			{
				int fitness = 0;
				for (int j = 0; j < tsize; j++)
				{
					if (population[i].str[j] != target[j])
					{
						fitness++; //caught different places to count the same as compliment
					}
				}
				int localFitness = 0;
				for (int j = 0; j < tsize; j++)
				{
					bool SEEN = false;
					for (int k = 0; k < tsize; k++)
					{
						if (population[i].str[k] != target[k] && (j != k) && population[i].str[j] != target[k] && !SEEN)
						{
							localFitness++; // pay more for the difference places!
							SEEN = true;
						}
					}
				}
				// update and assign values:
				population[i].caughtPlaces = tsize - fitness;
				population[i].fitness = fitness + localFitness;
				population[i].cpuTicks = ((double)(clock() - startClock));
				population[i].elapsedTime = time(&population[i].elapsedTime) - startTime;
			}
		}
		Population::calc_ga_stat(population);
	}
	static inline void sort_by_fitness(ga_vector &population)
	{
		sort(population.begin(), population.end(), fitness_sort);
	}
	static void mate(ga_vector &population, ga_vector &buffer,string selection = "elitism",string crossOver = "onePoint",string mutate = "mutate")
	{
		int esize = GA_POPSIZE * GA_ELITRATE;
		int tsize = GA_TARGET.size(), spos1,spos2, i1, i2;

		if (selection.compare("elitism") == 0)
		{
			Genetic1::Ga_Selection::elitism(population, buffer, esize);
		}
		else if (selection.compare("SUS") == 0)	// stochastic selection method -> kind of roullete
		{
			esize = Genetic1::Ga_Selection::SUS(population, buffer);
		}
		else if (selection.compare("tournament") == 0)
		{
			esize = Genetic1::Ga_Selection::Tournament(population, buffer);
		}
		else if (selection.compare("turnir") == 0)
		{
			esize = Genetic1::Ga_Selection::Turnir(population,buffer);
		}

		// Mate the rest
		for (int i = esize; i<GA_POPSIZE; i++) {
			i1 = rand() % (GA_POPSIZE / 2);
			i2 = rand() % (GA_POPSIZE / 2);
			spos1 = rand() % tsize;
			spos2 = rand() % tsize;

			if (crossOver.compare("onePoint") == 0)
			{
				Genetic1::Ga_CrossOver::onePoint(population[i1], population[i2], buffer[i], spos1);
			}
			else if (crossOver.compare("twoPoints") == 0)
			{
				Genetic1::Ga_CrossOver::twoPoints(population[i1], population[i2], buffer[i], spos1, spos2);
			}
			else if (crossOver.compare("uniform") == 0)	// exectaly half parameter - implemented well but i picked "two points" method instead
			{
				Genetic1::Ga_CrossOver::uniform(population[i1], population[i2], buffer[i]);
			}
			if (rand() < GA_MUTATION)
			{
				if (mutate.compare("mutate") == 0)	// changing random place
				{
					Genetic1::Ga_Mutate::mutate(buffer[i]);
				}
				else if (mutate.compare("insertion") == 0)		// using gen alels - implemented well bu you did not ask this method formaly
				{
					Genetic1::Ga_Mutate::insertion(buffer[i]);
				}
				else if (mutate.compare("swap") == 0)				// using gen alels
				{
					Genetic1::Ga_Mutate::swap(buffer[i]);
				}
				else if (mutate.compare("simpleReverse") == 0)	// using gen alels
				{
					Genetic1::Ga_Mutate::simpleReverse(buffer[i]);
				}
			}
		}
	}
	static inline void print_best(ga_vector &gav,int generation)
	{
		string temp;
		for (int i = 0; i < GA_TARGET.size(); i++)
		{
			temp += gav[0].str[i];
		}
		cout << " Best: " << temp << " (" << gav[0].fitness << ")" << " -> [mean=" << gav[0].mean << ",std=" << gav[0].std << "]"
			 << " -> " << "CPU Clock Ticks=" << gav[0].cpuTicks << ",Elapsed Run Time=" << gav[0].elapsedTime << " -> " << "Generation=" << generation << endl;
	}
	static inline void swap(ga_vector *&population, ga_vector *&buffer)
	{
		ga_vector *temp = population;
		population = buffer;
		buffer = temp;
	}
};

/*
	Functionality Implementation:
*/
int strings_matcher(string heuristic_method,string selection, string crossover, string mutation)
{
	srand(unsigned(time(NULL)));

	ga_vector pop_alpha, pop_beta;
	ga_vector *population, *buffer;

	Population::init_population(pop_alpha, pop_beta);	// initialize population of gens
	population = &pop_alpha;
	buffer = &pop_beta;
	clock_t startClock = clock();	// CPU ticks counting
	time_t startTime;
	time(&startTime);				// elapsed time counting
	for (int i = 0; i < GA_MAXITER; i++) 
	{
		Population::calc_fitness(*population, startClock, startTime, heuristic_method);	// calculate fitness
		Population::sort_by_fitness(*population);	// sort them
		Population::print_best(*population,i + 1);		// print the best one

		if((*population)[0].fitness == 0)
			break;

		Population::mate(*population, *buffer, selection,crossover,mutation);		// mate the population together -> there are some default values we can change to try another mate methods
		Population::swap(population, buffer);		// swap buffers for the next generation
	}
	printf(" End-up after: %.2f ticks and %.2f seconds\n", ((double)(clock() - startClock)), difftime(time(0), startTime));
	cin.get();
	return 0;
}

void PLAY_GENETIC_STRING_BY_UI(string custom = "")
{
	printf("\n String matching problem:\n");
	printf(" ========================\n");
	string str;
	if (custom.length() == 0)
	{
		str = "Hello World!";
	}
	else
	{
		str = custom;
	}
	string heuristic = "l1",select = "elitism", cross = "onePoint", mutate = "mutate";
	printf("\n Heuristic Methods:\n");
	printf(" 1. l1 distance\n");
	printf(" 2. BACS\n");
	printf(" Pick an heuristic method number to use: ");
	int h;
	scanf("%d",&h);
	if(h < 1 || h > 2){
		printf(" >> Using %s distance as default heuristic...\n",heuristic.c_str());
	}
	printf("\n Selection Methods:\n");
	printf(" 1. Elitism\n");
	printf(" 2. SUS\n");
	printf(" 3. Tournament\n");
	printf(" 4. Turnir\n");
	printf(" Pick a selection method number to use: ");
	int selection;
	scanf("%d",&selection);
	if(selection < 1 || selection > 4){
		printf(" >> Using %s as default selection method...\n",select.c_str());
	}
	printf("\n Crossover Methods:\n");
	printf(" 1. One point\n");
	printf(" 2. Two points\n");
	printf(" 3. Uniform\n");
	printf(" Pick a crossover method number to use: ");
	int crossover;
	scanf("%d", &crossover);
	if(crossover < 1 || crossover > 3){
		printf(" >> Using %s as default crossover method...\n",cross.c_str());
	}
	printf("\n Mutation Methods:\n");
	printf(" 1. Mutate - change a letter to random value in random place\n");
	printf(" 2. Swap\n");
	//printf(" 3. SimpleReverse\n");
	//printf(" 4. Insertion\n");
	printf(" Pick a mutation method number to use: ");
	int mutation;
	scanf("%d",&mutation);
	if(mutation < 1 || mutation > 2){
		printf(" >> Using %s as default mutation method...\n",mutate.c_str());
	}
	printf(" \n");
	if (str.length() == 0)
	{
		GA_TARGET = string("Hello world!");
	}
	else
	{
		GA_TARGET = str;
	}
	switch (h)
	{
	case(1):heuristic = "l1";
		break;
	case(2):heuristic = "BACS";
		break;
	}
	switch(selection)
	{
	case(1): select = "elitism";
		break;
	case(2): select = "SUS";
		break;
	case(3): select = "tournament";
		break;
	case(4): select = "turnir";
		break;
	}
	switch (crossover)
	{
	case(1): cross = "onePoint";
		break;
	case(2): cross = "twoPoints";
		break;
	case(3): cross = "uniform"; // - unmark this if you want this method
		break;
	}
	switch (mutation)
	{
	case(1): mutate = "mutate";
		break;
	case(2): mutate = "swap";
		break;
	/*
	case(3): mutate = "simpleReverse";
		break;
	case(4): mutate = "insertion"; // - unmark this if you want this method
		break;
	*/
	}
	strings_matcher(heuristic,select, cross, mutate);
	printf("\n "); system("PAUSE");
}

int main(int argc, char* argv[])
{
	string str;
	if (argc > 1)
	{
		str = argv[1];
	}
	PLAY_GENETIC_STRING_BY_UI(str);
	return 0;
}