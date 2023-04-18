#include <mpi.h>
#include <stdlib.h>
#include <stdio.h>
#include <math.h>
#include <stdbool.h>

int nwd(int a, int b)
{
    if (b > a)
    {
        int tmp = a;
        a = b;
        b = tmp;
    }

    while (b != 0)
    {
        int c = b;
        b = a % b;
        a = c;
    }

    return a;
}

int simplePow(int number, int exponent)
{
    int result = 1;
    while (exponent--)
        result *= number;

    return result;
}
void mpiClenup()
{
    MPI_Finalize();
}
int main(int argc, char **argv)
{
    MPI_Init(NULL, NULL);
    atexit(mpiClenup);

    int world_size, world_rank;

    MPI_Comm_size(MPI_COMM_WORLD, &world_size);
    MPI_Comm_rank(MPI_COMM_WORLD, &world_rank);

    int k = 1;
    while (k < world_size)
    {
        k *= 2;
    }
    if (k != world_size || k != argc - 1)
    {
        return EXIT_FAILURE;
    }

    int values[world_size];
    int totalSteps = log2(world_size);

    for (int i = 0; i < argc - 1; i++)
    {
        int value = atoi(argv[i + 1]);
        values[i] = value;
    }

    int myValue = values[world_rank];

    for (int step = 0; step < totalSteps; step++)
    {
        int shift = simplePow(2, step);
        int sendToRank = (world_rank + shift) % world_size;
        int reciveFromRank = world_rank - shift;
        if (reciveFromRank < 0)
            reciveFromRank += world_size;
        int partnerValue;
        MPI_Sendrecv(&myValue, 1, MPI_INT, sendToRank, 0,
                     &partnerValue, 1, MPI_INT, reciveFromRank, 0,
                     MPI_COMM_WORLD, MPI_STATUS_IGNORE);
        myValue = nwd(myValue, partnerValue);
    }
    if (world_rank == 0)
    {
        printf("Result %d = %d\n", world_rank, myValue);
    }
    return EXIT_SUCCESS;
}
