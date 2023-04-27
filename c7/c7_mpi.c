#include <stdio.h>
#include <stdlib.h>
#include <mpi.h>
#include <sys/time.h>

int main(int argc, char **argv)
{
    int rank, size;
    MPI_Init(&argc, &argv);
    MPI_Comm_rank(MPI_COMM_WORLD, &rank);
    MPI_Comm_size(MPI_COMM_WORLD, &size);

    int elem = argc > 1 ? atoi(argv[1]) : 10000000;
    struct timeval stop, start;
    gettimeofday(&start, NULL);

    int point_in_sqrt = elem;
    int point_in_circle = 0;
    int local_point_in_circle = 0;

    int *global_points = malloc(sizeof(int) * size);
    int points;

    for (int i = 0; i < size; i++)
    {
        if (i != size - 1)
        {
            // printf("%d -%d\n", elem, size);
            // printf("%d\n", elem / size);
            global_points[i] = (elem / size);
        }
        else
        {
            global_points[i] = elem - (elem / size) * (size - 1);
        }
    }

    double start_time = MPI_Wtime();

    MPI_Scatter(global_points, 1, MPI_INT, &points, 1, MPI_INT, 0, MPI_COMM_WORLD);
    for (int i = 0; i < points; i++)
    {
        double x = (double)rand() / ((double)RAND_MAX / 2) - 1;
        double y = (double)rand() / ((double)RAND_MAX / 2) - 1;
        if (x * x + y * y < 1)
        {
            local_point_in_circle++;
        }
    }

    MPI_Reduce(&local_point_in_circle, &point_in_circle, 1, MPI_INT, MPI_SUM, 0, MPI_COMM_WORLD);

    MPI_Finalize();

    if (rank == 0)
    {

        double result = 4 * ((double)point_in_circle) / ((double)point_in_sqrt);
        double end_time = MPI_Wtime();
        printf("Proces %d: czas wykonania: %d mikrosekund\n", rank, (int)((end_time - start_time) * 1000000));
        printf("RESULT OF PI IS : %f\n", result);
    }
}