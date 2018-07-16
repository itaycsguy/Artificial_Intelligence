//#include <SDKDDKVer.h>
//#include <tchar.h>

#include <stdio.h>

#pragma execution_character_set("utf-8")
#pragma warning(disable:4786)				// disable debug warning

#include <iostream>							// for cout etc.
#include <vector>							// for vector class
#include <string>							// for string class
#include <algorithm>						// for sort algorithm
#include <time.h>							// for random seed
#include <math.h>							// for abs()

#define GA_POPSTOP_RATIO	0.9				// population expansion
#define POPSOL_NO_CHANGE	20				// count number of best solution is repeating to finish
#define GA_POPSIZE			1024			// ga population size
#define GA_MAXITER			2048			// maximum iterations
#define GA_ELITRATE			0.10f			// elitism rate
#define GA_MUTATION_RATE	0.25f			// mutation rate
#define GA_MUTATION			RAND_MAX * GA_MUTATION_RATE

using namespace std;						// polluting global namespace, but hey...

int KNAPSACK_ITEMS;
int KNAPSACK_CAPACITY;

vector<int> KNOWN_WEIGHTS;	// of the current problem
vector<int> KNOWN_PROFITS;	// of the current problem


struct item {
	vector<pair<pair<unsigned int, unsigned int>,unsigned int>> _products; // one vector usage because of conceptualy understanding the problem
	unsigned int _weights; // constraint
	unsigned int _fitness; // profit
};

typedef vector<item> ga_couples;

namespace Csp2 {
	class Ga_Selection {
	public:
		static void elitism(ga_couples &population, ga_couples &buffer, int esize)
		{
			for (int i = 0; i < esize; i++) // picking esize people as auto to the next generation
			{
				buffer[i] = population[i]; // deep copy because of the sorting we using to do each iteration - the best fitness individual will pass to the next generation
			}
		}
	};

	class Ga_CrossOver {
	public:
		static void onePoint(item &populationi1, item &populationi2, item &bufferi, int spos) // spos is randomly picked
		{
			// change one location randomly
			for (int i = 0; i < spos; i++)
			{
				bufferi._products[i] = populationi1._products[i];
			}
			for (int j = spos; j < KNAPSACK_ITEMS; j++)
			{
				bufferi._products[j] = populationi2._products[j];
			}
		}
	};

	class Ga_Mutate {
	public:
		static void randomBitFlip(item &member) // that is the best method for mutation because it is 0/1 problem
		{
			int tsize = KNAPSACK_ITEMS;
			int ipos = rand() % tsize;

			if (member._products[ipos].second == 1)
			{
				member._products[ipos].second = 0;
			}
			else
			{
				member._products[ipos].second = 1;
			}
		}
	};
}
class Knapsack {
	ga_couples _items;
	item _last_best_sol;
	int _count_of_population_solution_that_did_not_change;

	static bool fitness_sort(item &x, item &y)
	{
		return (x._fitness > y._fitness);
	}
	inline void update_state(ga_couples &buffer)
	{
		_items = buffer;
	}
public:
	static void init_global_products(int problem_number = 1)
	{
		switch (problem_number)
		{
		case(1):
			KNOWN_WEIGHTS.push_back(23);
			KNOWN_WEIGHTS.push_back(31);
			KNOWN_WEIGHTS.push_back(29);
			KNOWN_WEIGHTS.push_back(44);
			KNOWN_WEIGHTS.push_back(53);
			KNOWN_WEIGHTS.push_back(38);
			KNOWN_WEIGHTS.push_back(63);
			KNOWN_WEIGHTS.push_back(85);
			KNOWN_WEIGHTS.push_back(89);
			KNOWN_WEIGHTS.push_back(82);

			KNOWN_PROFITS.push_back(92);
			KNOWN_PROFITS.push_back(57);
			KNOWN_PROFITS.push_back(49);
			KNOWN_PROFITS.push_back(68);
			KNOWN_PROFITS.push_back(60);
			KNOWN_PROFITS.push_back(43);
			KNOWN_PROFITS.push_back(67);
			KNOWN_PROFITS.push_back(84);
			KNOWN_PROFITS.push_back(87);
			KNOWN_PROFITS.push_back(72);

			KNAPSACK_ITEMS = 10;
			KNAPSACK_CAPACITY = 168;
			break;
		case(2):
			KNOWN_WEIGHTS.push_back(12);
			KNOWN_WEIGHTS.push_back(7);
			KNOWN_WEIGHTS.push_back(11);
			KNOWN_WEIGHTS.push_back(8);
			KNOWN_WEIGHTS.push_back(9);

			KNOWN_PROFITS.push_back(24);
			KNOWN_PROFITS.push_back(13);
			KNOWN_PROFITS.push_back(23);
			KNOWN_PROFITS.push_back(15);
			KNOWN_PROFITS.push_back(16);

			KNAPSACK_ITEMS = 5;
			KNAPSACK_CAPACITY = 26;
			break;
		case(3):
			KNOWN_WEIGHTS.push_back(56);
			KNOWN_WEIGHTS.push_back(59);
			KNOWN_WEIGHTS.push_back(80);
			KNOWN_WEIGHTS.push_back(64);
			KNOWN_WEIGHTS.push_back(75);
			KNOWN_WEIGHTS.push_back(17);

			KNOWN_PROFITS.push_back(50);
			KNOWN_PROFITS.push_back(50);
			KNOWN_PROFITS.push_back(64);
			KNOWN_PROFITS.push_back(46);
			KNOWN_PROFITS.push_back(50);
			KNOWN_PROFITS.push_back(5);

			KNAPSACK_ITEMS = 6;
			KNAPSACK_CAPACITY = 190;
			break;
		case(4):
			KNOWN_WEIGHTS.push_back(31);
			KNOWN_WEIGHTS.push_back(10);
			KNOWN_WEIGHTS.push_back(20);
			KNOWN_WEIGHTS.push_back(19);
			KNOWN_WEIGHTS.push_back(4);
			KNOWN_WEIGHTS.push_back(3);
			KNOWN_WEIGHTS.push_back(6);

			KNOWN_PROFITS.push_back(70);
			KNOWN_PROFITS.push_back(20);
			KNOWN_PROFITS.push_back(39);
			KNOWN_PROFITS.push_back(37);
			KNOWN_PROFITS.push_back(7);
			KNOWN_PROFITS.push_back(5);
			KNOWN_PROFITS.push_back(10);

			KNAPSACK_ITEMS = 7;
			KNAPSACK_CAPACITY = 50;
			break;
		case(5):
			KNOWN_WEIGHTS.push_back(25);
			KNOWN_WEIGHTS.push_back(35);
			KNOWN_WEIGHTS.push_back(45);
			KNOWN_WEIGHTS.push_back(5);
			KNOWN_WEIGHTS.push_back(25);
			KNOWN_WEIGHTS.push_back(3);
			KNOWN_WEIGHTS.push_back(2);
			KNOWN_WEIGHTS.push_back(2);

			KNOWN_PROFITS.push_back(350);
			KNOWN_PROFITS.push_back(400);
			KNOWN_PROFITS.push_back(450);
			KNOWN_PROFITS.push_back(20);
			KNOWN_PROFITS.push_back(70);
			KNOWN_PROFITS.push_back(8);
			KNOWN_PROFITS.push_back(5);
			KNOWN_PROFITS.push_back(5);

			KNAPSACK_ITEMS = 8;
			KNAPSACK_CAPACITY = 104;
			break;
		case(6):
			KNOWN_WEIGHTS.push_back(41);
			KNOWN_WEIGHTS.push_back(50);
			KNOWN_WEIGHTS.push_back(49);
			KNOWN_WEIGHTS.push_back(59);
			KNOWN_WEIGHTS.push_back(55);
			KNOWN_WEIGHTS.push_back(57);
			KNOWN_WEIGHTS.push_back(60);

			KNOWN_PROFITS.push_back(442);
			KNOWN_PROFITS.push_back(525);
			KNOWN_PROFITS.push_back(511);
			KNOWN_PROFITS.push_back(593);
			KNOWN_PROFITS.push_back(546);
			KNOWN_PROFITS.push_back(564);
			KNOWN_PROFITS.push_back(617);

			KNAPSACK_ITEMS = 7;
			KNAPSACK_CAPACITY = 170;
			break;
		case(7):
			KNOWN_WEIGHTS.push_back(70);
			KNOWN_WEIGHTS.push_back(73);
			KNOWN_WEIGHTS.push_back(77);
			KNOWN_WEIGHTS.push_back(80);
			KNOWN_WEIGHTS.push_back(82);
			KNOWN_WEIGHTS.push_back(87);
			KNOWN_WEIGHTS.push_back(90);
			KNOWN_WEIGHTS.push_back(94);
			KNOWN_WEIGHTS.push_back(98);
			KNOWN_WEIGHTS.push_back(106);
			KNOWN_WEIGHTS.push_back(110);
			KNOWN_WEIGHTS.push_back(113);
			KNOWN_WEIGHTS.push_back(115);
			KNOWN_WEIGHTS.push_back(118);
			KNOWN_WEIGHTS.push_back(120);

			KNOWN_PROFITS.push_back(135);
			KNOWN_PROFITS.push_back(139);
			KNOWN_PROFITS.push_back(149);
			KNOWN_PROFITS.push_back(150);
			KNOWN_PROFITS.push_back(156);
			KNOWN_PROFITS.push_back(163);
			KNOWN_PROFITS.push_back(173);
			KNOWN_PROFITS.push_back(184);
			KNOWN_PROFITS.push_back(192);
			KNOWN_PROFITS.push_back(201);
			KNOWN_PROFITS.push_back(210);
			KNOWN_PROFITS.push_back(214);
			KNOWN_PROFITS.push_back(221);
			KNOWN_PROFITS.push_back(229);
			KNOWN_PROFITS.push_back(240);

			KNAPSACK_ITEMS = 15;
			KNAPSACK_CAPACITY = 750;
			break;
		case(8):
			KNOWN_WEIGHTS.push_back(382745);
			KNOWN_WEIGHTS.push_back(799601);
			KNOWN_WEIGHTS.push_back(909247);
			KNOWN_WEIGHTS.push_back(729069);
			KNOWN_WEIGHTS.push_back(467902);
			KNOWN_WEIGHTS.push_back(44328);
			KNOWN_WEIGHTS.push_back(34610);
			KNOWN_WEIGHTS.push_back(698150);
			KNOWN_WEIGHTS.push_back(823460);
			KNOWN_WEIGHTS.push_back(903959);
			KNOWN_WEIGHTS.push_back(853665);
			KNOWN_WEIGHTS.push_back(551830);
			KNOWN_WEIGHTS.push_back(610856);
			KNOWN_WEIGHTS.push_back(670702);
			KNOWN_WEIGHTS.push_back(488960);
			KNOWN_WEIGHTS.push_back(951111);
			KNOWN_WEIGHTS.push_back(323046);
			KNOWN_WEIGHTS.push_back(446298);
			KNOWN_WEIGHTS.push_back(931161);
			KNOWN_WEIGHTS.push_back(31385);
			KNOWN_WEIGHTS.push_back(496951);
			KNOWN_WEIGHTS.push_back(264724);
			KNOWN_WEIGHTS.push_back(224916);
			KNOWN_WEIGHTS.push_back(169684);

			KNOWN_PROFITS.push_back(825594);
			KNOWN_PROFITS.push_back(1677009);
			KNOWN_PROFITS.push_back(1676628);
			KNOWN_PROFITS.push_back(1523970);
			KNOWN_PROFITS.push_back(943972);
			KNOWN_PROFITS.push_back(97426);
			KNOWN_PROFITS.push_back(69666);
			KNOWN_PROFITS.push_back(1296457);
			KNOWN_PROFITS.push_back(1679693);
			KNOWN_PROFITS.push_back(1902996);
			KNOWN_PROFITS.push_back(1844992);
			KNOWN_PROFITS.push_back(1049289);
			KNOWN_PROFITS.push_back(1252836);
			KNOWN_PROFITS.push_back(1319836);
			KNOWN_PROFITS.push_back(953277);
			KNOWN_PROFITS.push_back(2067538);
			KNOWN_PROFITS.push_back(675367);
			KNOWN_PROFITS.push_back(853655);
			KNOWN_PROFITS.push_back(1826027);
			KNOWN_PROFITS.push_back(65731);
			KNOWN_PROFITS.push_back(901489);
			KNOWN_PROFITS.push_back(577243);
			KNOWN_PROFITS.push_back(466257);
			KNOWN_PROFITS.push_back(369261);

			KNAPSACK_ITEMS = 24;
			KNAPSACK_CAPACITY = 6404180;
			break;
		}
	}
	void init_couples()
	{
		_items.resize(GA_POPSIZE);
		for (int i = 0; i < GA_POPSIZE; i++)
		{
			_items[i]._products.resize(KNAPSACK_ITEMS);
			_items[i]._weights = 0;
			_items[i]._fitness = 0; // value
			for (int j = 0; j < KNAPSACK_ITEMS; j++)
			{
				pair<unsigned int, unsigned int> p(KNOWN_PROFITS[j], KNOWN_WEIGHTS[j]);
				pair<pair<unsigned int, unsigned int>, unsigned int> q(p, rand() % 2);
				_items[i]._products[j] = q;
				if (_items[i]._products[j].second == 1)
				{
					_items[i]._weights += q.first.second;
					_items[i]._fitness += q.first.first;
				}
			}
		}
	}
	void calc_fitness()
	{
		for (int i = 0; i < GA_POPSIZE; i++)
		{
			int weight = 0;
			int fitness = 0;
			int ones_counter = 0;
			for (int j = 0; j < KNAPSACK_ITEMS; j++)
			{
				if (_items[i]._products[j].second == 1) // valid bit
				{
					ones_counter++;
					weight += _items[i]._products[j].first.second;
					fitness += _items[i]._products[j].first.first;
				}
			}
			while (weight > KNAPSACK_CAPACITY) // check abount capacity constraint
			{
				int to_change = rand() % ones_counter;
				int one_over_one = 0;
				for (int k = 0; k < KNAPSACK_ITEMS; k++)
				{
					if ((_items[i]._products[k].second == 1) && (one_over_one == to_change)) // take down bit randomly
					{
						_items[i]._products[k].second = 0;
						weight -= _items[i]._products[k].first.second;
						fitness -= _items[i]._products[k].first.first;
						k = KNAPSACK_ITEMS;
						if (ones_counter > 0)
						{
							ones_counter--;
						}
					}
					else if((_items[i]._products[k].second == 1) && (one_over_one < to_change))
					{
						one_over_one++;
					}
				}
				if ((ones_counter == 0) && (weight > KNAPSACK_CAPACITY))
				{
					break;
				}
			}
			_items[i]._weights = weight;
			_items[i]._fitness = fitness;
		}
	}
	inline void sort_by_fitness()
	{
		sort(_items.begin(), _items.end(), Knapsack::fitness_sort);
	}
	void mate()
	{
		int esize = GA_POPSIZE * GA_ELITRATE;
		int tsize = KNAPSACK_ITEMS, i1, i2,ipos;
		ga_couples buffer(GA_POPSIZE);
		for (int i = 0; i < GA_POPSIZE; i++)
		{
			buffer[i]._products.resize(KNAPSACK_ITEMS);
		}
		Csp2::Ga_Selection::elitism(_items, buffer, esize);

		// Mate the rest - continue from the last place with made elitism
		for (int i = esize; i < GA_POPSIZE; i++) {
			i1 = rand() % (GA_POPSIZE / 2);
			i2 = rand() % (GA_POPSIZE / 2);
			ipos = rand() % tsize;

			Csp2::Ga_CrossOver::onePoint(_items[i1], _items[i2], buffer[i], ipos);

			if (rand() < GA_MUTATION) 
				Csp2::Ga_Mutate::randomBitFlip(buffer[i]); // change valid bit randomly!
		}
		update_state(buffer);
	}
	inline void print_best_pack(int generation,bool trace_mode)
	{
		if (trace_mode == true)
		{
			printf("\n An Optimal Packing: ");
		}
		else
		{
			printf(" Best Solution: ");
		}
		printf("<");
		for (int i = 0; i < KNAPSACK_ITEMS; i++)
		{
			printf("%d", _items[0]._products[i].second); // valid bit
			if (i < (KNAPSACK_ITEMS - 1))
			{
				printf(",");
			}
		}
		printf(">");
		printf(" (Weight = %d,Profit = %d,Generation = %d)\n", _items[0]._weights,_items[0]._fitness,generation);
	}
	bool done()
	{
		// check if we done this:
		vector<int> amounts;
		vector<int> groups;
		for (int i = 0; i < GA_POPSIZE; i++)
		{
			bool SEEN = false;
			for (int k = 0; k < groups.size(); k++)
			{
				if (groups[k] == _items[i]._fitness)
				{
					SEEN = true;
					amounts[k]++;
				}
			}
			if (SEEN == false)
			{
				groups.push_back(_items[i]._fitness);
				amounts.push_back(1);
			}
		}
		int stop_step = GA_POPSIZE * GA_POPSTOP_RATIO;
		for (int i = 0; i < amounts.size(); i++)	// stop condition is not well and there is not stopping occur
		{
			if (amounts[i] >= stop_step) // percentage >= 90%
			{
				return true;
			}
		}
		if ((_count_of_population_solution_that_did_not_change == POPSOL_NO_CHANGE) &&
			(_last_best_sol._fitness == _items[0]._fitness) &&
			(_last_best_sol._weights == _items[0]._weights))
		{
			return true;
		}
		return false;
	}
	void update_best_pack()
	{
		if ((_items[0]._fitness == _last_best_sol._fitness) && (_items[0]._weights == _last_best_sol._weights))
		{
			_count_of_population_solution_that_did_not_change++;
		}
		else
		{
			_count_of_population_solution_that_did_not_change = 0;
		}
		_last_best_sol = _items[0];
	}
	void flush_global_products()
	{
		KNOWN_WEIGHTS.empty();
		KNOWN_PROFITS.empty();
	}
};

void knapsack_solver(int problem_number)
{
	printf(" Computing problem number %d solution...\n", problem_number);
	Knapsack knapsack;
	Knapsack::init_global_products(problem_number); // initialize products through the problem number
	knapsack.init_couples();
	//srand(time(0));
	clock_t startClock = clock();
	time_t startTime = time(0);
	for (int i = 0; i < GA_MAXITER; i++)
	{
		knapsack.calc_fitness(); // fitness computing is about the constraint and the profit altogether
		knapsack.sort_by_fitness();
		knapsack.update_best_pack(); // saving the last solution for termination condition
		knapsack.print_best_pack(i + 1,false);

		if (knapsack.done())
		{
			printf("\n");
			knapsack.print_best_pack(i + 1,true);
			break;
		}

		knapsack.mate();
	}
	printf(" End-up after: %.2f ticks and %.2f seconds\n\n", ((double)(clock() - startClock)), difftime(time(0), startTime));
	knapsack.flush_global_products();
}

void PLAY_KNAPSACK_BY_UI()
{
	printf(" Knapsack Problems Menu:\n");
	printf(" =======================\n");
	printf(" 1. 10 weights and profits for a knapsack of capacity 165.\n");
	printf(" 2. 5 weights and profits for a knapsack of capacity 26.\n");
	printf(" 3. 6 weights and profits for a knapsack of capacity 190.\n");
	printf(" 4. 7 weights and profits for a knapsack of capacity 50.\n");
	printf(" 5. 8 weights and profits for a knapsack of capacity 104.\n");
	printf(" 6. 7 weights and profits for a knapsack of capacity 170 [The knapsack can be packed to an optimal weight of 169].\n");
	printf(" 7. 15 weights and profits for a knapsack of capacity 750 [from Kreher and Stinson, with an optimal profit of 1458].\n");
	printf(" 8. 24 weights and profits for a knapsack of capacity 6404180 [from Kreher and Stinson, with an optimal profit of 13549094].\n");
	int prob_num;
	do {
		printf(" Pick a problem number follow the menu: ");
		scanf("%d", &prob_num);
		if ((prob_num < 1) || (prob_num > 8))
		{
			printf("\n [invalid problem number, try again.]\n");
		}
		else
		{
			break;
		}
	} while (true);
	printf(" \n");
	knapsack_solver(prob_num);
	printf(" "); system("PAUSE");
}

int main()
{
	PLAY_KNAPSACK_BY_UI();
	return 0;
}