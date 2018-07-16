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

#define MAXLOOPS 2048				// for end promissing

using namespace std;				// polluting global namespace, but hey...

unsigned int WIN_BOARD_SIZE = 4;
vector<vector<int>> GOOD_ENTRY_POINT;

struct stateBoard {
	vector<int> _board;
	unsigned int _fitness;
};

class MinimalConflictsNQueens {
	stateBoard state;
	vector<int> conflics_col;		// treatment queen will be selected randomly for movement

public:
	static void assignGoodEntryPoint(int board_size = 4)
	{
		// there is one little example because i talked with Shay and he told me that is not good with bad begin states and this is the once i recognize - this is okay from him!
		// in addition - if you have some good begin state you can set up the board_size , write the board and run the application - this would work!
		if (board_size <= 0)
		{
			exit(42);
		}
		vector<int> goodEntry;
		srand(time(0));
		for(int i = 0;i < board_size;i++){
			//printf("%d\n",rand() % board_size);
			goodEntry.push_back(rand() % board_size);
		}
		//system("PAUSE");
		WIN_BOARD_SIZE = board_size;
	}
	unsigned int findMinimalConflictsRowAtCol(int col)
	{
		//find the minimum conflic in specific col:
		int min_idx = 0;
		int min_conflics = WIN_BOARD_SIZE;
		for (int i = 0; i < WIN_BOARD_SIZE; i++)
		{
			int curr_conflics = 0;
			bool UP = false, DOWN = false, MID = false;		// calculate each direction treatment once
			for (int j = (col+1); j < WIN_BOARD_SIZE; j++)	// starting from col+1 col because prev. treatments already counted
			{
				if (!MID && (i == state._board[j]))
				{
					curr_conflics++;
					MID = true;
				}
				else if (abs(state._board[j] - i) == abs(j - col))	// if there is diff. value similatiry to the counter diff between indecies
				{
					if (!DOWN && ((j - col) < 0))
					{
						curr_conflics++;
						DOWN = true;
					}
					else if (!UP && ((j - col) >= 0))
					{
						curr_conflics++;
						UP = true;
					}
				}
			}
			if (curr_conflics <= min_conflics)	// updating the minimum index at column `col` to be the next movement location
			{
				min_conflics = curr_conflics;
				min_idx = i;
			}
			UP = false, DOWN = false, MID = false;
		}
		return min_idx;
	}
	void assignAllConflicsCols()
	{
		for (int i = 0; i < WIN_BOARD_SIZE; i++)
		{
			bool UP = false, DOWN = false, MID = false;		// calculate each direction treatment once
			for (int j = (i+1); j < WIN_BOARD_SIZE; j++)	// starting from i+1 col because prev. treatments already counted
			{
				if (!MID && (state._board[i] == state._board[j]))
				{
					MID = true;
				}
				else if (abs(state._board[j] - state._board[i]) == abs(j - i))	// if there is diff. value similatiry to the counter diff between indecies
				{
					if (!DOWN && ((j - i) < 0))
					{
						DOWN = true;
					}
					else if (!UP && ((j - i) >= 0))
					{
						UP = true;
					}
				}
			}
			if (UP == true || DOWN == true || MID == true)	// add queen if there is some treatment from her to vector which will randomly selected as next movement
			{
				conflics_col.push_back(i);
			}
			UP = false, DOWN = false, MID = false;
		}
	}
	void calc_fitness()
	{
		int sum_conflics = 0;
		for (int i = 0; i < WIN_BOARD_SIZE; i++)
		{
			bool UP = false, DOWN = false, MID = false;		// calculate each direction treatment once
			for (int j = (i+1); j < WIN_BOARD_SIZE; j++)	// starting from i+1 col because prev. treatments already counted
			{
				if (!MID && (state._board[i] == state._board[j]))
				{
					sum_conflics++;
					MID = true;
				}
				else if (abs(state._board[j] - state._board[i]) == abs(j - i))	// if there is diff. value similatiry to the counter diff between indecies
				{
					if (!DOWN && ((j - i) < 0))
					{
						sum_conflics++;
						DOWN = true;
					}
					else if (!UP && ((j - i) >= 0))
					{
						sum_conflics++;
						UP = true;
					}
				}
			}
			UP = false, DOWN = false, MID = false;
		}
		state._fitness = sum_conflics;	// object updating
	}
	void init_board()
	{
		bool isEmptyEntry = GOOD_ENTRY_POINT.empty();	// if there is some known good beginning it will placed here
		state._board.resize(WIN_BOARD_SIZE);
		for (int i = 0; i < WIN_BOARD_SIZE; i++)
		{
			if (!isEmptyEntry)
			{
				state._board[i] = GOOD_ENTRY_POINT[0][i];
			}
			else
			{
				state._board[i] = rand() % WIN_BOARD_SIZE;	// otherwise we will pickup randomly for each column
			}
		}
		calc_fitness();
	}
	bool isWinState()
	{
		return (state._fitness == 0);
	}
	void makeMovement()
	{
		// go ahead using some smart step where there is minimum threats
		assignAllConflicsCols();
		unsigned int random_conflic_col = conflics_col[rand() % conflics_col.size()];	// pickup treatment queen randomly for the pool to move to less conflics location
		unsigned int min_conf_idx = findMinimalConflictsRowAtCol(random_conflic_col);
		state._board[random_conflic_col] = min_conf_idx;
		conflics_col.clear();	// clear all conflicts and will be calculate at the next loop if needed
	}
	inline void print_state()
	{
		printf(" State: ");
		printf("<");
		for (int i = 0; i < WIN_BOARD_SIZE; i++)
		{
			printf("%d", state._board[i]);
			if (i < (WIN_BOARD_SIZE - 1))
			{
				printf(",");
			}
		}
		printf(">");
		printf(" (%d)\n", state._fitness);
	}
	void plot_solution(bool real_solution)
	{
		if(real_solution){
			printf("\n Solution visualization:\n");
			printf(" -----------------------\n");
		} else {
			printf("\n Incomplete Board State Visualization:\n");
			printf(" -------------------------------------\n");
		}
		for (int i = 0; i < WIN_BOARD_SIZE; i++)
		{
			// run each row:
			for (int j = 0; j < WIN_BOARD_SIZE; j++)
			{
				if (state._board[j] == i)
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

int queens_min_conflicts_solver(int board_size = 4)
{
	MinimalConflictsNQueens::assignGoodEntryPoint(board_size);
	MinimalConflictsNQueens queens;
	queens.init_board();
	for (int i = 0; i < MAXLOOPS; i++)
	{
		queens.print_state();
		if (queens.isWinState())
		{
			queens.plot_solution(true);  
			cin.get();
			return 0;
		}

		queens.makeMovement();
		queens.calc_fitness();	// need to become -> 0 for some solution!
	}
	printf("\n >> no any solution is found.\n");
	queens.plot_solution(false);
	cin.get();
	return 0;
}

void PLAY_MIN_CONFLICTS_QUEENS_BY_UI()
{
	printf(" Enter board-size to initial Minimal Conflicts algorithm: ");
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
	printf("\n");
	queens_min_conflicts_solver(board_size);
	printf("\n "); system("PAUSE");
}

int main()
{
	PLAY_MIN_CONFLICTS_QUEENS_BY_UI();
	return 0;
}