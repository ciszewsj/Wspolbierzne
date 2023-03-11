#include <stdio.h>
#include <pthread.h>
#include <stdlib.h>
#include <math.h>

// 311192 Ciszewski Jakub

typedef struct queue
{
    int index;
    struct queue *before;
} squeue;

// squeue *nullpointer;

squeue *q;

int numberOfElems = 0;

double **A;
double **B;
double **C;

double sumOfC, frebousian = 0;

int ma, mb, na, nb;

pthread_mutex_t lock;
pthread_mutex_t lockValue;

void put(int i)
{
    pthread_mutex_lock(&lock);

    squeue *new = malloc(sizeof(squeue));

    new->index = i;
    new->before = q;
    q = new;
    numberOfElems++;

    pthread_mutex_unlock(&lock);
}
int getNumberOfElems()
{
    return numberOfElems;
}
int pop()
{
    if (numberOfElems <= 0)
    {
        return 0;
    }
    int value = 0;
    squeue *old = q;
    value = old->index;
    q = old->before;
    free(old);
    numberOfElems--;
    return value;
}

void *multiplyABMatrixs(void *vargp)
{
    int indexToCount;
    int i, j, k;
    double s;

    while (1 == 1)
    {
        pthread_mutex_lock(&lock);

        if (getNumberOfElems() <= 0)
        {
            pthread_mutex_unlock(&lock);
            break;
        }
        indexToCount = pop();
        pthread_mutex_unlock(&lock);

        // printf("index : %d\n", indexToCount);

        i = indexToCount / nb;
        j = indexToCount % nb;

        s = 0;
        for (k = 0; k < na; k++)
        {
            s += A[i][k] * B[k][j];
        }
        C[i][j] = s;
    }
    return NULL;
}

void *addCMatrixs(void *vargp)
{
    int indexToCount;
    int i, j, k;

    while (1 == 1)
    {
        pthread_mutex_lock(&lock);

        if (getNumberOfElems() <= 0)
        {
            pthread_mutex_unlock(&lock);
            break;
        }
        indexToCount = pop();
        pthread_mutex_unlock(&lock);

        // printf("index : %d\n", indexToCount);

        i = indexToCount / nb;
        j = indexToCount % nb;

        pthread_mutex_lock(&lockValue);
        sumOfC += C[i][j];
        pthread_mutex_unlock(&lockValue);
    }
    return NULL;
}

void *countFrebousian(void *vargp)
{
    int indexToCount;
    int i, j, k;
    double s;

    while (1 == 1)
    {
        pthread_mutex_lock(&lock);

        if (getNumberOfElems() <= 0)
        {
            pthread_mutex_unlock(&lock);
            break;
        }
        indexToCount = pop();
        pthread_mutex_unlock(&lock);

        // printf("index : %d\n", indexToCount);

        i = indexToCount / nb;
        j = indexToCount % nb;
        s = C[i][j] * C[i][j];
        pthread_mutex_lock(&lockValue);
        frebousian += s;
        pthread_mutex_unlock(&lockValue);
    }
    return NULL;
}

void print_matrix(double **A, int m, int n)
{
    int i, j;
    printf("[");
    for (i = 0; i < m; i++)
    {
        for (j = 0; j < n; j++)
        {
            printf("%f ", A[i][j]);
        }
        printf("\n");
    }
    printf("]\n");
}
int main(int argc, char **argv)
{
    FILE *fpa;
    FILE *fpb;

    int i, j;
    double x;

    int numberOfThreads = argc > 1 ? atoi(argv[1]) : 4;

    pthread_t *thread_id = malloc(sizeof(pthread_t) * numberOfThreads);

    if (pthread_mutex_init(&lock, NULL) != 0 || pthread_mutex_init(&lockValue, NULL) != 0)
    {
        printf("\n mutex init failed\n");
        return 1;
    }

    fpa = fopen("A.txt", "r");
    fpb = fopen("B.txt", "r");
    if (fpa == NULL || fpb == NULL)
    {
        perror("błąd otwarcia pliku");
        exit(-10);
    }

    fscanf(fpa, "%d", &ma);
    fscanf(fpa, "%d", &na);

    fscanf(fpb, "%d", &mb);
    fscanf(fpb, "%d", &nb);

    printf("pierwsza macierz ma wymiar %d x %d, a druga %d x %d\n", ma, na, mb, nb);

    if (na != mb)
    {
        printf("Złe wymiary macierzy!\n");
        return EXIT_FAILURE;
    }

    /*Alokacja pamięci*/
    A = malloc(ma * sizeof(double));
    for (i = 0; i < ma; i++)
    {
        A[i] = malloc(na * sizeof(double));
    }

    B = malloc(mb * sizeof(double));
    for (i = 0; i < mb; i++)
    {
        B[i] = malloc(nb * sizeof(double));
    }

    /*Macierz na wynik*/
    C = malloc(ma * sizeof(double));
    for (i = 0; i < ma; i++)
    {
        C[i] = malloc(nb * sizeof(double));
    }

    printf("Rozmiar C: %dx%d\n", ma, nb);
    for (i = 0; i < ma; i++)
    {
        for (j = 0; j < na; j++)
        {
            fscanf(fpa, "%lf", &x);
            A[i][j] = x;
        }
    }

    printf("A:\n");
    print_matrix(A, ma, mb);

    for (i = 0; i < mb; i++)
    {
        for (j = 0; j < nb; j++)
        {
            fscanf(fpb, "%lf", &x);
            B[i][j] = x;
        }
    }

    printf("B:\n");
    print_matrix(B, mb, nb);

    // INICJALIZACJA KOLEJKI INDEXAMI
    for (i = 0; i < ma; i++)
    {
        for (j = 0; j < nb; j++)
        {
            put(i * nb + j);
        }
    }

    // MNOZENIE MACIERZY A I B NA WIELU WATKACH

    for (i = 0; i < numberOfThreads; i++)
    {
        pthread_create(&thread_id[i], NULL, multiplyABMatrixs, NULL);
    }
    // CZEKANIE  NA KONIEC
    for (i = 0; i < numberOfThreads; i++)
    {
        pthread_join(thread_id[i], NULL);
    }

    printf("C:\n");
    print_matrix(C, ma, nb);

    // INICJALIZACJA KOLEJKI INDEXAMI
    for (i = 0; i < ma; i++)
    {
        for (j = 0; j < nb; j++)
        {
            put(i * nb + j);
        }
    }

    // LICZENIE SUMY ELEMENTÓW MACIERZY C

    for (i = 0; i < numberOfThreads; i++)
    {
        pthread_create(&thread_id[i], NULL, addCMatrixs, NULL);
    }
    // CZEKANIE  NA KONIEC

    for (i = 0; i < numberOfThreads; i++)
    {
        pthread_join(thread_id[i], NULL);
    }

    printf("Suma elementów macierzy C : %f\n", sumOfC);

    // INICJALIZACJA KOLEJKI INDEXAMI
    for (i = 0; i < ma; i++)
    {
        for (j = 0; j < nb; j++)
        {
            put(i * nb + j);
        }
    }

    // LICZENIE SUMY NORMY FREBOUSIAN MACIERZY C
    for (i = 0; i < numberOfThreads; i++)
    {
        pthread_create(&thread_id[i], NULL, countFrebousian, NULL);
    }
    // CZEKANIE  NA KONIEC
    for (i = 0; i < numberOfThreads; i++)
    {
        pthread_join(thread_id[i], NULL);
    }
    frebousian = sqrt(frebousian);

    printf("Norma frebousian macierzy C : %f\n", frebousian);

    for (i = 0; i < na; i++)
    {
        free(A[i]);
    }
    free(A);

    for (i = 0; i < nb; i++)
    {
        free(B[i]);
    }
    free(B);

    for (i = 0; i < nb; i++)
    {
        free(C[i]);
    }
    free(C);

    free(thread_id);
    fclose(fpa);
    fclose(fpb);

    pthread_mutex_destroy(&lock);

    return 0;
}