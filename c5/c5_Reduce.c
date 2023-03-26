#include <mpi.h>
#include <stdio.h>
#include <stdlib.h>
#include <math.h>
#include "count_integral.h"

int main(int argc, char **argv)
{
    int rank, size, num_points;
    double begin, end, result, local_result;

    if (argc != 4)
    {
        printf("Usage: mpiexec -n num_procs %s begin end num_points\n", argv[0]);
        return 1;
    }

    begin = atof(argv[1]);
    end = atof(argv[2]);
    num_points = atoi(argv[3]);

    MPI_Init(&argc, &argv);
    MPI_Comm_rank(MPI_COMM_WORLD, &rank);
    MPI_Comm_size(MPI_COMM_WORLD, &size);

    int num_points_per_process = num_points / size;

    double local_begin = begin + rank * (end - begin) / size;
    double local_end = local_begin + (end - begin) / size;

    printf("<<< %f - %f - %d >>>\n", local_begin, local_end, num_points_per_process);
    
    local_result = integrate(sin, local_begin, local_end, num_points_per_process);

    MPI_Reduce(&local_result, &result, 1, MPI_DOUBLE, MPI_SUM, 0, MPI_COMM_WORLD);

    if (rank == 0)
    {
        printf("Result: %lf\n", result);
    }

    MPI_Finalize();
    return 0;
}
