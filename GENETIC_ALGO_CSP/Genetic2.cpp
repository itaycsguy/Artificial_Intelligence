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
#define GA_ELITRATE		0.50f		// elitism rate
#define GA_MUTATIONRATE	0.25f		// mutation rate
#define GA_MUTATION		RAND_MAX * GA_MUTATIONRATE

using namespace std;				// polluting global namespace, but hey...

unsigned int GA_NQUE_SIZE;	// board length
vector<int> TARGET_BOARD;	// holding solutions for some board sizes

struct state {
	vector<int> _board;	// cols vector representation
	unsigned int _fitness;
};

typedef vector<state> ga_vector;

namespace Genetic2 {
	class Ga_CrossOver {
	public:
		static void onePoint(state &populationi1, state &populationi2, state &bufferi, int spos)
		{
			// pick one place to exchange (randomly)
			for (int i = 0; i < spos; i++)
			{
				bufferi._board[i] = populationi1._board[i];
			}
			for (int j = spos; j < GA_NQUE_SIZE; j++)
			{
				bufferi._board[j] = populationi2._board[j];
			}
		}
		static void twoPoints(state &populationi1, state &populationi2, state &bufferi, int spos1, int spos2)
		{
			// pick two places to exchange (randomly)
			for (int i = 0; i < spos1; i++)
			{
				bufferi._board[i] = populationi1._board[i];
			}
			for (int i = spos1; i < spos2; i++)
			{
				bufferi._board[i] = populationi2._board[i];
			}
			for (int i = spos2; i < GA_NQUE_SIZE; i++)
			{
				bufferi._board[i] = populationi1._board[i];
			}
		}
		static void uniform(state &populationi1, state &populationi2, state &bufferi)
		{
			for(int i = 0;i < GA_NQUE_SIZE;i++){
				int gen_to_select = rand() % 2;
				if(gen_to_select == 0){
					bufferi._board[i] = populationi1._board[i]; 
				} else {
					bufferi._board[i] = populationi2._board[i];
				}
			}
		}
		static void PMX(state &populationi1, state &populationi2, state &bufferi)
		{
			//initial buffer for result usage
			for(int i = 0;i < GA_NQUE_SIZE;i++){
				bufferi._board[i] = -1;
			}
			//picking randomly block chunck
			int ipos = rand() % GA_NQUE_SIZE;
			int jpos = rand() % GA_NQUE_SIZE;
			int low = 0,high = 0;
			if(ipos <= jpos){
				low = ipos;
				high = jpos;
			} else {
				low = jpos;
				high = ipos;
			}
			//directly trasform the p1 segment to the children keeping the alels order
			for(int i = low;i <= high;i++){
				bufferi._board[i] = populationi1._board[i];
			}
			vector<int> p2_src; // values from p1
			vector<int> p2_dest; // values from p2
			for(int i = low;i <= high;i++){
				bool IS_THERE = false;
				for(int j = low;j <= high;j++){
					if(populationi2._board[i] == populationi1._board[j]){
						IS_THERE = true;
						break;
					}
				}
				//building the mapping between values in p2 to values same location in p1 where these values are no in child already
				if(!IS_THERE){
					p2_src.push_back(populationi2._board[i]);
					p2_dest.push_back(populationi1._board[i]);
				}
			}
			for(int i = 0;i < p2_dest.size();i++){
				for(int j = 0;j < GA_NQUE_SIZE;j++){
					// find mapped value in the p2 array
					if(populationi2._board[j] == p2_dest[i]){
						//check if this place is empty
						if(bufferi._board[j] == -1){
							bufferi._board[j] = p2_src[i];
						} 
						//else {
							// because of duplication is allowd better is to live this case because of Inf loop and bad performance we could achieve.
							// this is working well without it!
						//}
					}
				}
			}
			for(int i = 0;i < GA_NQUE_SIZE;i++){
				if(bufferi._board[i] == -1){
					bufferi._board[i] = populationi2._board[i];
				}
			}
		}
		static void OX(state &populationi1, state &populationi2, state &bufferi)
		{
			//initial buffer for result usage
			for(int i = 0;i < GA_NQUE_SIZE;i++){
				bufferi._board[i] = -1;
			}
			// pick chunck of alels from populationi1
			int ipos = rand() % GA_NQUE_SIZE;
			int jpos = rand() % GA_NQUE_SIZE;
			int low = 0,high = 0;
			if(ipos <= jpos){
				low = ipos;
				high = jpos;
			} else {
				low = jpos;
				high = ipos;
			}
			//directly trasform the p1 segment to the children keeping the alels order
			for(int i = low;i <= high;i++){
				bufferi._board[i] = populationi1._board[i];
			}
			//picking each value in index i to be in buffer index i -> this operation is handling duplications otherwise there is no any coverage.
			for(int i = 0;i < low;i++){
				bufferi._board[i] = populationi2._board[i];
			}
			for(int i = (high + 1);i < GA_NQUE_SIZE;i++){
				bufferi._board[i] = populationi2._board[i];
			}
		}
	};

	class Ga_Selection {
	public:
		static void elitism(ga_vector &population, ga_vector &buffer, int esize)
		{
			for (int i = 0; i < esize; i++)
			{
				buffer[i] = population[i];
				buffer[i]._board = population[i]._board;
			}
		}
		static int SUS(ga_vector &population, ga_vector &buffer)
		{
			ga_vector leftOver(GA_POPSIZE);
			double m = 0;
			int sum = population[0]._fitness;
			for (int i = 0; i < population.size(); i++)
			{
				m += population[i]._fitness;
			}
			m /= population.size(); // finding avg of population
			double r = (double)rand() / (RAND_MAX + 1);// pick random number between 0 to 1
			double delta = r * m;
			int j = 0, i = 0, k = 0;
			do {
				if (delta < sum) // for some delta we divide the population by sum - that is the point of this method [part of roullete]
				{
					buffer[i] = population[j];
					buffer[i]._board = population[j]._board;
					i++;
					delta += sum;
				}
				else
				{
					// keep into leftOver all individuals about sum less of equals than delta
					sum += population[j]._fitness;
					leftOver[k] = population[j];
					leftOver[k]._board = population[j]._board;
					k++;
					j++;
				}
			} while (j < population.size());
			int startRest = i;
			for (k = i, j = 0; k < GA_POPSIZE - i; k++, j++) // coping leftOvers to the buffer's back
			{
				buffer[k] = leftOver[j];
				buffer[k]._board = leftOver[j]._board;
			}
			return startRest;
		}
		static int Tournament(ga_vector &population, ga_vector &buffer)
		{
			int K = 2;//rand() % GA_POPSIZE;
			vector<int> rest;
			state best;
			bool isBestNull = true;
			for (int i = 0; i < K; i++) // taking K best random indecies
			{
				int ind = rand() % population.size();
				if (isBestNull || (best._fitness > population[ind]._fitness))
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
				if (population[ipos]._fitness < population[jpos]._fitness)
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
		static void simpleReverse(state &member)
		{
			// simple reverse random chunck
			int tsize = GA_NQUE_SIZE;
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
			vector<int> s(GA_NQUE_SIZE);
			for (int i = 0; i < low; i++)
			{
				s.push_back(member._board[i]);
			}
			vector<int> toRev;
			for (int i = low; i <= high; i++)
			{
				toRev.push_back(member._board[i]);
			}
			reverse(toRev.begin(), toRev.end());
			for (int i = 0; i < toRev.size(); i++)
			{
				s.push_back(toRev[i]);
			}
			for (int i = high + 1; i < GA_NQUE_SIZE; i++)
			{
				s.push_back(member._board[i]);
			}
			for(int i = 0;i < GA_NQUE_SIZE;i++){
				member._board[i] = s[i];	
			}
		}
		static void swap(state &member)
		{
			//simple swap between random place
			int tsize = GA_NQUE_SIZE;
			int ipos = rand() % tsize;
			int jpos = rand() % tsize;

			char temp = member._board[ipos];
			member._board[ipos] = member._board[jpos];
			member._board[jpos] = temp;
		}
		static void insertion(state &member)
		{
			// insert new random letter to random place
			int tsize = GA_NQUE_SIZE;
			int ipos = rand() % tsize;
			int nextPlace = rand() % tsize;

			vector<int> temp(GA_NQUE_SIZE);
			for(int i = 0;i < GA_NQUE_SIZE;i++){
				if(i != ipos){
					temp.push_back(member._board[i]);
				} else if(i == nextPlace){
					temp.push_back(member._board[i]);
					temp.push_back(member._board[ipos]);
				}
			}
			for(int i = 0;i < GA_NQUE_SIZE;i++){
				member._board[i] = temp[i];
			}
		}
	};
}
class NQueens {
	ga_vector items;	// holding array of states which the board could be

	static bool fitness_sort(state x, state y)
	{
		return (x._fitness < y._fitness);
	}
	inline void update_state(ga_vector &buffer)
	{
		items = buffer;
	}
	int get_number_of_conflicts(vector<int>& board) {
		int number_of_conflicts = 0;
		for (int i = 0; i < (TARGET_BOARD.size() - 1); i++) {
			bool SAME_ROW = false,SAME_UP = false,SAME_DOWN = false;
			for (int k = 1; k < (TARGET_BOARD.size() - i); k++) {
				if ((!SAME_ROW) && (board[i] == board[i + k])) {
					number_of_conflicts++;
					SAME_ROW = true;
				} else if(board[i + k] > board[i]){ 
					if ((!SAME_UP) && (abs(board[i + k] - board[i]) == k)) {
						SAME_UP = true;
						number_of_conflicts++;
					}
				} else if((!SAME_DOWN) && (abs(board[i] - board[i + k]) == k)) {
					SAME_DOWN = true;
					number_of_conflicts++;
				}

			}
		}
		return number_of_conflicts;
	}
public:
	static void init_global_winner_state(unsigned int board_size = 4)
	{
		if(board_size <= 0){
			exit(42);
		}
		srand(time(0));
		for(int i = 0;i < board_size;i++){
			TARGET_BOARD.push_back(rand() % board_size);
		}
		GA_NQUE_SIZE = board_size;
	}
	void init_population()
	{
		items.resize(GA_POPSIZE);
		for (int i = 0; i < GA_POPSIZE; i++)
		{
			items[i]._board.resize(GA_NQUE_SIZE);
			for (int j = 0; j < GA_NQUE_SIZE; j++)
			{
				items[i]._board[j] = rand() % GA_NQUE_SIZE; // randomly position for the first gens
			}
			items[i]._fitness = 0;
		}
	}
	state* getFirst()
	{
		return &(items[0]);
	}
	ga_vector* getBoard()
	{
		return &items;
	}
	void calc_fitness()
	{
		for (int i = 0; i < GA_POPSIZE; i++)
		{
			//fitness += abs(items[i]._board[j] - TARGET_BOARD[j]);	// l1 distance between two cols vectors
			int fitness = get_number_of_conflicts(items[i]._board);
			items[i]._fitness = fitness;
		}
	}
	inline void sort_by_fitness()
	{
		sort(items.begin(),items.end(), NQueens::fitness_sort);
	}
	inline void print_best_state(int generation)
	{
		printf(" Best Position: ");
		printf("<");
		for (int i = 0; i < GA_NQUE_SIZE; i++)
		{
			printf("%d",items[0]._board[i]);
			if (i < (GA_NQUE_SIZE - 1))
			{
				printf(",");
			}
		}
		printf(">");
		printf(" (fitness=%d,generation=%d)\n", items[0]._fitness,generation);
	}
	void mate(string selection, string crossOver, string mutate)
	{
		int esize = GA_POPSIZE * GA_ELITRATE;
		int tsize = GA_NQUE_SIZE, spos1, spos2, i1, i2;
		ga_vector buffer(GA_POPSIZE);
		for (int i = 0; i < GA_POPSIZE; i++)
		{
			buffer[i]._board.resize(GA_NQUE_SIZE);
		}

		if (selection.compare("elitism") == 0)
		{
			Genetic2::Ga_Selection::elitism(items, buffer, esize);
		}
		else if (selection.compare("SUS") == 0)	// stochastic method
		{
			esize = Genetic2::Ga_Selection::SUS(items, buffer);
		}
		else if (selection.compare("TournamentSelection") == 0)
		{
			esize = Genetic2::Ga_Selection::Tournament(items, buffer);
		}
		else if (selection.compare("TurnirSelection") == 0)
		{
			esize = Genetic2::Ga_Selection::Turnir(items, buffer);
		}

		// Mate the rest
		for (int i = esize; i<GA_POPSIZE; i++) {
			i1 = rand() % (GA_POPSIZE / 2);
			i2 = rand() % (GA_POPSIZE / 2);
			spos1 = rand() % tsize;
			spos2 = rand() % tsize;

			if (crossOver.compare("onePoint") == 0)
			{
				Genetic2::Ga_CrossOver::onePoint(items[i1], items[i2], buffer[i], spos1);
			}
			else if (crossOver.compare("twoPoints") == 0)
			{
				Genetic2::Ga_CrossOver::twoPoints(items[i1], items[i2], buffer[i], spos1, spos2);
			}
			else if (crossOver.compare("uniform") == 0)
			{
				Genetic2::Ga_CrossOver::uniform(items[i1], items[i2], buffer[i]);
			}
			else if(crossOver.compare("PMX") == 0)
			{
				Genetic2::Ga_CrossOver::PMX(items[i1], items[i2], buffer[i]);
			}
			else if(crossOver.compare("OX") == 0)
			{
				Genetic2::Ga_CrossOver::OX(items[i1], items[i2], buffer[i]);
			}
			if (rand() < GA_MUTATION)
			{
				if (mutate.compare("insertion") == 0)
				{
					Genetic2::Ga_Mutate::insertion(buffer[i]);
				}
				else if (mutate.compare("swap") == 0)
				{
					Genetic2::Ga_Mutate::swap(buffer[i]);
				}
				else if (mutate.compare("simpleReverse") == 0)
				{
					Genetic2::Ga_Mutate::simpleReverse(buffer[i]);
				}
			}
		}
		update_state(buffer);
	}
	void plot_solution(bool real_solution)
	{
		if(real_solution){
			printf("\n Solution visualization:\n");
			printf(" -----------------------\n");
		} else {
			printf("\n Incomplete Board State Visualization:\n");
			printf("\n -----------------------------------\n");
		}
		state winner = items[0];
		for (int i = 0; i < GA_NQUE_SIZE; i++)
		{
			// run each row:
			for (int j = 0; j < GA_NQUE_SIZE; j++)
			{
				if (winner._board[j] == i)
				{
					printf(" %c ", 'X');
				}
				else
				{
					printf(" %c ", 'O');
				}
			}
			printf("\n");
		}
	}
};

/*
	OOP Implementation
*/
int queens_genetic_solver(int board_size,string selection,string crossover,string mutation)
{
	const unsigned int BOARD_SIZE = board_size;
	NQueens::init_global_winner_state(BOARD_SIZE);
	printf(" %d Queens solution using Genetic algorithms:\n", board_size);
	NQueens game;
	game.init_population();
	clock_t startClock = clock();
	time_t startTime = time(0);
	for (int i = 0; i < GA_MAXITER; i++)
	{
		game.calc_fitness();
		game.sort_by_fitness();
		game.print_best_state(i + 1);

		if ((*game.getFirst())._fitness == 0){
			game.plot_solution(true);
			cin.get();
			printf("\n End-up after: %.2f ticks and %.2f seconds\n", ((double)(clock() - startClock)),difftime(time(0), startTime));
			return 0;
		}
		
		game.mate(selection,crossover,mutation);
	}
	printf("\n End-up after: %.2f ticks and %.2f seconds\n", ((double)(clock() - startClock)),difftime(time(0), startTime));
	printf("\n >> no any solution is found.\n");
	game.plot_solution(false);
	cin.get();
	return 0;
}

void PLAY_GENETIC_QUEENS_BY_UI()
{
	printf(" N-Queens problem using GA:\n");
	printf("===========================\n");
	printf(" Enter N-Queens board-size: ");
	int board_size;
	do {
		scanf("%d", &board_size);
		if(board_size <= 0)
		{
			printf(" \n[invalid board-size. try again.]\n");
		}
		else
		{
			break;
		}
	} while (true);
	string select = "elitism", cross = "onePoint", mutate = "mutate";
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
	printf(" 4. PMX\n");
	printf(" 5. OX\n");
	printf(" Pick a crossover method number to use: ");
	int crossover;
	scanf("%d", &crossover);
	if(crossover < 1 || crossover > 3){
		printf(" >> Using %s as default crossover method...\n",cross.c_str());
	}
	printf("\n Mutation Methods:\n");
	printf(" 1. Mutate - change a letter to random value in random place\n");
	printf(" 2. Swap\n");
	printf(" 3. SimpleReverse\n");
	printf(" 4. Insertion\n");
	printf(" Pick a mutation method number to use: ");
	int mutation;
	scanf("%d",&mutation);
	if(mutation < 1 || mutation > 2){
		printf(" >> Using %s as default mutation method...\n",mutate.c_str());
	}
	printf(" \n");
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
	case(3): cross = "uniform";
		break;
	case(4): cross = "PMX";
		break;
	case(5): cross = "OX";
		break;
	}
	switch (mutation)
	{
	case(1): mutate = "mutate";
		break;
	case(2): mutate = "swap";
		break;
	case(3): mutate = "simpleReverse";
		break;
	case(4): mutate = "insertion";
		break;
	}
	printf("\n");
	queens_genetic_solver(board_size,select,cross,mutate);
	printf("\n "); system("PAUSE");
}

int main()
{
	PLAY_GENETIC_QUEENS_BY_UI();
	return 0;
}