#include <stdio.h>
#include <stdlib.h>
#include <math.h>
#include <sys/time.h>

#define N 10000000
#define S (int)sqrt(N)
#define M N / 10

int main(int argc, char **argv)
{

	long int a[S + 1];	  /*tablica pomocnicza*/
	long int pierwsze[M]; /*liczby pierwsze w przedziale 2..N*/
	long i, k, liczba, reszta;
	long int lpodz;		 /* l. podzielnikow*/
	long int llpier = 0; /*l. liczb pierwszych w tablicy pierwsze*/
	double czas;		 /*zmienna do  mierzenia czasu*/
	FILE *fp;
	/*wyznaczanie podzielnikow z przedzialow 2..S*/

	struct timeval stop, start;

	/*wyznaczanie podzielnikow z przedzialow 2..S*/
	gettimeofday(&start, NULL);

	for (i = 2; i <= S; i++)
		a[i] = 1; /*inicjowanie*/
	for (i = 2; i <= S; i++)
		if (a[i] == 1)
		{
			pierwsze[llpier++] = i; /*zapamietanie podzielnika*/
									/*wykreslanie liczb zlozonych bedacych wielokrotnosciami i*/
			for (k = i + i; k <= S; k += i)
				a[k] = 0;
		}

	lpodz = llpier; /*zapamietanie liczby podzielnikow*/
	/*wyznaczanie liczb pierwszych*/
	for (liczba = S + 1; liczba <= N; liczba++)
	{
		for (k = 0; k < lpodz; k++)
		{
			reszta = (liczba % pierwsze[k]);
			if (reszta == 0)
				break; /*liczba zlozona*/
		}
		if (reszta != 0)
			pierwsze[llpier++] = liczba; /*zapamietanie liczby pierwszej*/
	}
	if ((fp = fopen("primes.txt", "w")) == NULL)
	{
		printf("Nie moge otworzyc pliku do zapisu\n");
		exit(1);
	}
	for (i = 0; i < llpier; i++)
		fprintf(fp, "%ld ", pierwsze[i]);
	fclose(fp);
	gettimeofday(&stop, NULL);

	printf("Obliczenie zajeło =  %lu us\n", (stop.tv_sec - start.tv_sec) * 1000000 + stop.tv_usec - start.tv_usec);

	return 0;
}
