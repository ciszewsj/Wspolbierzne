#include <stdio.h>
#include <stdlib.h>
#include <mpi.h>
#include <math.h>
#include "count_integral.h"

int main(int argc, char **argv)
{
    int rank, size;
    double begin, end, result;
    int num_points;

    MPI_Init(&argc, &argv);
    MPI_Comm_rank(MPI_COMM_WORLD, &rank);
    MPI_Comm_size(MPI_COMM_WORLD, &size);

    if (rank == 0)
    {
        if (argc != 4)
        {
            printf("Usage: mpiexec -n num_procs %s begin end num_points\n", argv[0]);
            return 1;
        }

        begin = atof(argv[1]);
        end = atof(argv[2]);
        num_points = atoi(argv[3]);

        // Send input values to all processes
        for (int i = 1; i < size; i++)
        {
            MPI_Send(&begin, 1, MPI_DOUBLE, i, 0, MPI_COMM_WORLD);
            MPI_Send(&end, 1, MPI_DOUBLE, i, 0, MPI_COMM_WORLD);
            MPI_Send(&num_points, 1, MPI_INT, i, 0, MPI_COMM_WORLD);
        }
    }
    else
    {
        // Receive input values from rank 0
        MPI_Recv(&begin, 1, MPI_DOUBLE, 0, 0, MPI_COMM_WORLD, MPI_STATUS_IGNORE);
        MPI_Recv(&end, 1, MPI_DOUBLE, 0, 0, MPI_COMM_WORLD, MPI_STATUS_IGNORE);
        MPI_Recv(&num_points, 1, MPI_INT, 0, 0, MPI_COMM_WORLD, MPI_STATUS_IGNORE);
    }

    // Divide work among processes
    int chunk_size = num_points / size;
    int remainder = num_points % size;

    int start, end_chunk;

    if (rank < remainder)
    {
        start = rank * (chunk_size + 1);
        end_chunk = start + chunk_size + 1;
    }
    else
    {
        start = rank * chunk_size + remainder;
        end_chunk = start + chunk_size;
    }

    double local_begin = begin + rank * (end - begin) / size;
    double local_end = local_begin + (end - begin) / size;

    printf("<<< %f - %f - %d >>>\n", local_begin, local_end, chunk_size);

    double local_sum = integrate(sin, local_begin, local_end, chunk_size);

    // Reduce local sums to the final result
    if (rank == 0)
    {
        result = local_sum;

        for (int i = 1; i < size; i++)
        {
            double recv_sum;
            MPI_Recv(&recv_sum, 1, MPI_DOUBLE, i, 0, MPI_COMM_WORLD, MPI_STATUS_IGNORE);
            result += recv_sum;
        }

        printf("Result: %lf\n", result);
    }
    else
    {
        MPI_Send(&local_sum, 1, MPI_DOUBLE, 0, 0, MPI_COMM_WORLD);
    }

    MPI_Finalize();

    return 0;
}