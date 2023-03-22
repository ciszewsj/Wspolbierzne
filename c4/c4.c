#include <stdio.h>
#include <stdlib.h>
#include <math.h>
#include <sys/time.h>

int main(int argc, char **argv)
{

	int numberOfThreads = argc > 1 ? atoi(argv[1]) : 4;
	long int N = argc > 2 ? atoi(argv[2]) : 10000000;
	long int S = (int)sqrt(N);
	long int M = N / 10;
	long int *a = malloc(sizeof(long int) * (S + 1));
	printf("ALOCATED A : %ld\n", N);
	int *pierwsze = malloc((M) * sizeof(int));
	printf("ALOCATED pierwsze : %ld\n", M);

	long i, k, liczba, reszta;
	long int lpodz;
	long int llpier = 0;
	double czas;
	FILE *fp;
	struct timeval stop, start;

	omp_set_num_threads(numberOfThreads);

	gettimeofday(&start, NULL);

	{
#pragma omp parallel for shared(a) private(i) schedule(dynamic)
		for (i = 2; i <= S; i++)
		{
			a[i] = 1;
		}
	}

	{
		for (i = 2; i <= S; i++)
		{
			if (a[i] == 1)
			{
				{
					pierwsze[llpier++] = i;
				}
#pragma omp parallel for shared(a) private(k) schedule(dynamic)
				for (k = i + i; k <= S; k += i)
				{
					{
						a[k] = 0;
					}
				}
			}
		}
	}

	lpodz = llpier;
	{
#pragma omp parallel for shared(llpier, pierwsze) private(k, reszta, liczba) schedule(dynamic)
		for (liczba = S + 1; liczba <= N; liczba++)
		{

			for (k = 0; k < lpodz; k++)
			{
				reszta = (liczba % pierwsze[k]);
				if (reszta == 0)
				{
					break;
				}
			}
			if (reszta != 0)
			{
				// #pragma omp critical
				{
					pierwsze[llpier++] = liczba;
				}
			}
		}
	}

	if ((fp = fopen("primes.txt", "w")) == NULL)
	{
		printf("Nie moge otworzyc pliku do zapisu\n");
		exit(1);
	}

	for (i = 0; i < llpier; i++)
	{
		fprintf(fp, "%ld ", pierwsze[i]);
	}
	fclose(fp);

	gettimeofday(&stop, NULL);
	printf("Obliczenie zajeło =  %lu us\n", (stop.tv_sec - start.tv_sec) * 1000000 + stop.tv_usec - start.tv_usec);

	free(a);
	free(pierwsze);

	return 0;
}
