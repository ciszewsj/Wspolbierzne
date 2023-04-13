#include <stdio.h>
#include <stdlib.h>
#include <math.h>
#include <mpi.h>
#include "count_integral.h"
#include <sys/time.h>

double fun(double x){
    return x*x/3;
}

int main(int argc, char **argv)
{
    int rank, size, tag = 0;
    double begin, end;
    int num_points;
    double local_sum = 0.0, result = 0.0;

    struct timeval stop, start;
    gettimeofday(&start, NULL);

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

    if (rank == 0)
    {
        int chunk_size = num_points / size;
        int remainder = num_points % size;
        int start, end_chunk;
        for (int i = 1; i < size; i++)
        {
            if (i < remainder)
            {
                start = i * (chunk_size + 1);
                end_chunk = start + chunk_size + 1;
            }
            else
            {
                start = i * chunk_size + remainder;
                end_chunk = start + chunk_size;
            }

            double local_begin = begin + i * (end - begin) / size;
            double local_end = local_begin + (end - begin) / size;
            MPI_Isend(&local_begin, 1, MPI_DOUBLE, i, 0, MPI_COMM_WORLD, &request_send[0]);
            MPI_Isend(&local_end, 1, MPI_DOUBLE, i, 0, MPI_COMM_WORLD, &request_send[0]);
            MPI_Isend(&chunk_size, 1, MPI_INT, i, 0, MPI_COMM_WORLD, &request_send[0]);
        }
        end = begin + (end - begin) / size;
        num_points = num_points - chunk_size * (size - 1);

        local_begin = begin;
        local_end = end;
        local_num_points = num_points;
    }
    else
    {
        // Receive input values from rank 0
        MPI_Irecv(&local_begin, 1, MPI_DOUBLE, 0, tag, MPI_COMM_WORLD, &request_recv[0]);
        MPI_Irecv(&local_end, 1, MPI_DOUBLE, 0, tag, MPI_COMM_WORLD, &request_recv[0]);
        MPI_Irecv(&local_num_points, 1, MPI_INT, 0, tag, MPI_COMM_WORLD, &request_recv[0]);
        // MPI_Wait(&request_recv[0], &status);
    }

    printf("<<< %f - %f - %d >>>\n", local_begin, local_end, local_num_points);

    local_sum = integrate(fun, local_begin, local_end, local_num_points);

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
    }

    if (rank == 0)
    {
        printf("Result: %lf\n", result);
        gettimeofday(&stop, NULL);
        printf("Obliczenie zajeło =  %lu us\n", (stop.tv_sec - start.tv_sec) * 1000000 + stop.tv_usec - start.tv_usec);
    }

    MPI_Finalize();

    return 0;
}
