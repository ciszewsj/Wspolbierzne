#include <stdlib.h>
#include <stdio.h>
#include <omp.h>
#include <sys/time.h>

int main(int argc, char **argv)
{
    srand(time(NULL));

    int n_threads = argc > 1 ? atoi(argv[1]) : 4;
    int elem = argc > 2 ? atoi(argv[2]) : 10000000;

    int point_in_circle = 0;
    struct timeval stop, start;

    omp_set_num_threads(n_threads);
    gettimeofday(&start, NULL);
    int i;
    double x, y;

    int local;
#pragma omp parallel shared(point_in_circle) private(i, x, y, local)
    {
        local = 0;
#pragma omp for
        for (i = 0; i < elem; i++)
        {
            x = (double)rand() / ((double)RAND_MAX / 2) - 1;
            y = (double)rand() / ((double)RAND_MAX / 2) - 1;
            if (x * x + y * y < 1)
            {
                local++;
            }
        }
#pragma omp critical
        point_in_circle += local;
    }
    gettimeofday(&stop, NULL);

    double result = 4 * ((double)point_in_circle) / ((double)elem);

    printf("Obliczenie zajeło =  %lu us\n", (stop.tv_sec - start.tv_sec) * 1000000 + stop.tv_usec - start.tv_usec);

    printf("RESULT OF PI IS : %f\n", result);
}