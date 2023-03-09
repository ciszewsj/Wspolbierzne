#include <stdlib.h>
#include <stdio.h>
#include <sys/types.h>
#include <sys/wait.h>
#include <unistd.h>
#include <sys/ipc.h>
#include <sys/shm.h>
#include <sys/time.h>
#define BUFFOR_SIZE 80

// 311192 Ciszewski Jakub

void on_usr1(int signal)
{
	// printf("Otrzymałem USR1\n");
}

int main(int argc, char **argv)
{
	FILE *f = fopen("vector.dat", "r");
	char buffor[BUFFOR_SIZE + 1];
	double *vector;
	int n;
	int i;

	int pmax = argc > 1 ? atoi(argv[1]) : 2;
	pid_t pid;
	pid_t *pid_list = malloc(sizeof(pid_t) * pmax);;

	int *results;

	char *mem1 = "c1.c";

	key_t key1 = ftok(mem1, 65);
	key_t key2 = ftok(mem1, 67);
	struct timeval stop, start;

	gettimeofday(&start, NULL);

	for (int k = 0; k < pmax; k++)
	{
		switch (pid = fork())
		{
		case -1:
			fprintf(stderr, "Blad w fork\n");
			return EXIT_FAILURE;
		case 0:
			int number_p = k;
			sigset_t mask;
			struct sigaction usr1;
			sigemptyset(&mask);
			usr1.sa_handler = (&on_usr1);
			usr1.sa_mask = mask;
			usr1.sa_flags = SA_SIGINFO;
			sigaction(SIGUSR1, &usr1, NULL);

			sigemptyset(&mask);
			sigprocmask(SIG_BLOCK, &mask, NULL);

			int nmin, nmax;

			pause();

			{
				int shmid;
				if ((shmid = shmget(key1, sizeof(int) * (pmax + 1), 0666 | IPC_CREAT)) == -1)
				{
					fprintf(stderr, "shmget failed");
					exit(1);
				}

				int *tab;
				if ((tab = (int *)shmat(shmid, (void *)0, 0)) < 0)
				{
					fprintf(stderr, "shmat failed");
					exit(1);
				}

				nmin = tab[number_p];
				nmax = tab[number_p + 1];

				if (shmdt(tab) < 0)
				{
					fprintf(stderr, "shmdt failed");
					exit(1);
				}
			}

			if (nmin < nmax)
			{
				int shmid;
				if ((shmid = shmget(key2, sizeof(double), 0666 | IPC_CREAT)) == -1)
				{
					fprintf(stderr, "shmget failed");
					exit(1);
				}

				double *tab;

				if ((tab = (double *)shmat(shmid, (void *)0, 0)) < 0)
				{
					fprintf(stderr, "shmat failed");
					exit(1);
				}

				double result = 0;
				for (int i = nmin; i < nmax; i++)
				{
					result += tab[i];
				}
				tab[nmin] = result;
				if (shmdt(tab) < 0)
				{
					fprintf(stderr, "shmdt failed");
					exit(1);
				}
			}

			return EXIT_SUCCESS;
		default:
			pid_list[k] = pid;
		}
	}

	// printf("WCZYTUJE LISTĘ\n");
	// printf("WCZYTUJE LISTĘ\n");
	// printf("WCZYTUJE LISTĘ\n");

	fgets(buffor, BUFFOR_SIZE, f);
	n = atoi(buffor);
	vector = malloc(sizeof(double) * n);
	printf("Vector has %d elements\n", n);
	for (i = 0; i < n; i++)
	{
		fgets(buffor, BUFFOR_SIZE, f);
		vector[i] = atof(buffor);
	}
	fclose(f);

	// printf("INICJALIZUJE PAMIĘĆ WSPÓŁDZIELONĄ\n");
	// printf("INICJALIZUJE PAMIĘĆ WSPÓŁDZIELONĄ\n");
	// printf("INICJALIZUJE PAMIĘĆ WSPÓŁDZIELONĄ\n");

	int shmid1;
	if ((shmid1 = shmget(key1, sizeof(int) * (pmax + 1), 0666 | IPC_CREAT)) == -1)
	{
		fprintf(stderr, "shmget failed");
		exit(1);
	}
	int *tab;
	if ((tab = (int *)shmat(shmid1, (void *)0, 0)) < 0)
	{
		fprintf(stderr, "shmat failed");
		exit(1);
	}

	int tmp_k = 0;
	int diff = (n + pmax - 1) / pmax;
	for (int k = 0; k < pmax; k++)
	{
		if (tmp_k < n)
		{
			tab[k] = tmp_k;
			tmp_k += diff;
		}
		else
		{
			tab[k] = n;
		}
	}
	tab[pmax] = n;

	// printf("INICJALIZUJE PAMIĘĆ WSPÓŁDZIELONĄ 2\n");
	// printf("INICJALIZUJE PAMIĘĆ WSPÓŁDZIELONĄ 2\n");
	// printf("INICJALIZUJE PAMIĘĆ WSPÓŁDZIELONĄ 2\n");
	int shmid2;
	if ((shmid2 = shmget(key2, sizeof(double) * n, 0666 | IPC_CREAT)) == -1)
	{
		fprintf(stderr, "shmget failed");
		exit(1);
	}
	double *results_t;
	if ((results_t = (double *)shmat(shmid2, (void *)0, 0)) < 0)
	{
		fprintf(stderr, "shmat failed");
		exit(1);
	}
	for (int k = 0; k < n; k++)
	{
		results_t[k] = vector[k];
	}

	// printf("STARTUJE PROCESY\n");
	// printf("STARTUJE PROCESY\n");
	// printf("STARTUJE PROCESY\n");

	for (int k = 0; k < pmax; k++)
	{
		kill(pid_list[k], SIGUSR1);
	}

	// printf("KOŃCZE PROCESY\n");
	// printf("KOŃCZE PROCESY\n");
	// printf("KOŃCZE PROCESY\n");

	for (int k = 0; k < pmax; k++)
	{
		waitpid(pid_list[k], 0, 0);
	}

	// printf("STARTUJE ZLICZANIE\n");
	// printf("STARTUJE ZLICZANIE\n");
	// printf("STARTUJE ZLICZANIE\n");

	double final_result = 0;

	for (int i = 0; i < pmax; i++)
	{
		if (tab[i] >= n)
		{
			break;
		}
		// printf("tab[%d] = %f  \n", i, results_t[tab[i]]);

		final_result += results_t[tab[i]];
	}

	// printf("CZYTAM Z MEMORY\n");
	// printf("CZYTAM Z MEMORY\n");
	// printf("CZYTAM Z MEMORY\n");

	if (shmdt(tab) < 0)
	{
		fprintf(stderr, "shmdt failed");
		exit(1);
	}

	if (shmctl(shmid1, IPC_RMID, NULL) < 0)
	{
		fprintf(stderr, "smctl failed");
		exit(1);
	}

	// printf("ZAMYKAM MEMORY 2\n");
	// printf("ZAMYKAM MEMORY 2\n");
	// printf("ZAMYKAM MEMORY 2\n");

	if (shmdt(results_t) < 0)
	{
		fprintf(stderr, "shmdt failed");
		exit(1);
	}

	if (shmctl(shmid2, IPC_RMID, NULL) < 0)
	{
		fprintf(stderr, "smctl failed");
		exit(1);
	}

	// printf("WYPISANIE WYNIKU\n");
	// printf("WYPISANIE WYNIKU\n");
	// printf("WYPISANIE WYNIKU\n");
	gettimeofday(&stop, NULL);
	printf("Obliczenie zajeło =  %lu us\n", (stop.tv_sec - start.tv_sec) * 1000000 + stop.tv_usec - start.tv_usec);
	printf("Suma elementów w wektorze = %f\n", final_result);

	return 0;
}
