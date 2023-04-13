#include <mpi.h>
#include <stdio.h>
#include <stdlib.h>
#include <math.h>
#include "count_integral.h"
#include <sys/time.h>


double fun(double x){
    return x*x/3;
}


typedef struct
{
    double begin;
    double end;
    int chunk_size;
} Element;

int main(int argc, char **argv)
{
    int rank, size, num_points;
    double begin, end, result, local_result;
    double local_begin, local_end;
    Element local;
    int num_points_per_process = 0;

    struct timeval stop, start;
    gettimeofday(&start, NULL);

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

    // Utwórz tablicę opisującą typy i przesunięcia dla każdego pola struktury
    int block_lengths[3] = {1, 1, 1};
    MPI_Aint displacements[3] = {0, sizeof(double), 2 * sizeof(double)};
    MPI_Datatype types[3] = {MPI_DOUBLE, MPI_DOUBLE, MPI_INT};

    // Zdefiniuj niestandardowy typ danych opisujący strukturę
    MPI_Datatype mpi_point_type;
    MPI_Type_create_struct(3, block_lengths, displacements, types, &mpi_point_type);
    MPI_Type_commit(&mpi_point_type);

    Element *arr = (Element *)malloc(sizeof(Element) * (size));

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
            arr[i].begin = local_begin;
            arr[i].end = local_end;
            arr[i].chunk_size = chunk_size;
        }
        end = begin + (end - begin) / size;
        num_points = num_points - chunk_size * (size - 1);
        arr[0].begin = begin;
        arr[0].end = end;
        arr[0].chunk_size = chunk_size;
    }

    MPI_Scatter(arr, 1, mpi_point_type, &local, 1, mpi_point_type, 0, MPI_COMM_WORLD);

    local_result = integrate(fun, local.begin, local.end, local.chunk_size);
    printf("<<< %f - %f - %d  << %d >>  =  %f>>>\n", local.begin, local.end, local.chunk_size, rank, local_result);

    MPI_Reduce(&local_result, &result, 1, MPI_DOUBLE, MPI_SUM, 0, MPI_COMM_WORLD);

    MPI_Finalize();
     if (rank == 0)
    {
        printf("Result: %lf\n", result);
        gettimeofday(&stop, NULL);
        printf("Obliczenie zajeło =  %lu us\n", (stop.tv_sec - start.tv_sec) * 1000000 + stop.tv_usec - start.tv_usec);
    }
    return 0;
}
