#include <stdio.h>
#include <stdlib.h>
#include <math.h>
#include <mpi.h>
#include "count_integral.h"

int main(int argc, char **argv)
{
    int rank, size, tag = 0;
    double begin, end;
    int num_points;
    double local_sum = 0.0, result = 0.0;
    MPI_Status status;
    MPI_Request request_send[2], request_recv[2];
    begin = atof(argv[1]);
    end = atof(argv[2]);
    num_points = atoi(argv[3]);
    if (argc != 4)
    {
        printf("Usage: mpiexec -n num_procs %s begin end num_points\n", argv[0]);
        return 1;
    }

    MPI_Init(&argc, &argv);
    MPI_Comm_rank(MPI_COMM_WORLD, &rank);
    MPI_Comm_size(MPI_COMM_WORLD, &size);

    int local_num_points = num_points / size;
    double local_begin = begin + rank * local_num_points * (end - begin) / num_points;
    double local_end = local_begin + local_num_points * (end - begin) / num_points;

    printf("<<< %f - %f - %d >>>\n", local_begin, local_end, local_num_points);

    local_sum = integrate(sin, local_begin, local_end, local_num_points);

    if (rank == 0)
    {

        result = local_sum;
        for (int i = 1; i < size; i++)
        {
            MPI_Irecv(&local_sum, 1, MPI_DOUBLE, i, tag, MPI_COMM_WORLD, &request_recv[0]);
            MPI_Wait(&request_recv[0], &status);
            result += local_sum;
        }
    }
    else
    {
        MPI_Isend(&local_sum, 1, MPI_DOUBLE, 0, tag, MPI_COMM_WORLD, &request_send[0]);
        MPI_Wait(&request_send[0], &status);
    }

    if (rank == 0)
    {
        printf("Result: %lf\n", result);
    }

    MPI_Finalize();

    return 0;
}
