#include <mpi.h>
#include <stdlib.h>
#include <stdio.h>
#include <stdbool.h>
#include <string.h>

#define VECTOR_LENGTH 3000
#define PART_LENGTH 1000

void mpiCleanup()
{
    MPI_Finalize();
}

int main(int argc, char **argv)
{
    MPI_Init(&argc, &argv);
    atexit(mpiCleanup);
    int worldSize, worldRank;
    MPI_Comm_size(MPI_COMM_WORLD, &worldSize);
    MPI_Comm_rank(MPI_COMM_WORLD, &worldRank);

    MPI_Comm cartesianCommunicator;
    int dimensionSizes[] = {worldSize};
    int periods[] = {0};
    MPI_Cart_create(MPI_COMM_WORLD, 1, dimensionSizes, periods, true, &cartesianCommunicator);

    int cartesianRank;
    MPI_Comm_rank(cartesianCommunicator, &cartesianRank);

    int previousCartesianRank, nextCartesianRank;
    MPI_Cart_shift(cartesianCommunicator, 0, 1, &previousCartesianRank, &nextCartesianRank);

    int *vector;
    if (cartesianRank == 0)
    {
        vector = malloc(VECTOR_LENGTH * sizeof(*vector));
        for (int i = 0; i < VECTOR_LENGTH; i++)
        {
            vector[i] = i;
        }
    }

    long long sum = 0;

    for (int i = 0; i < VECTOR_LENGTH / PART_LENGTH; i++)
    {
        int part[PART_LENGTH];

        if (cartesianRank == 0)
        {
            int shift = i * PART_LENGTH;
            memcpy(part, vector + shift, PART_LENGTH * sizeof(*part));
        }
        else
        {
            MPI_Recv(part, PART_LENGTH, MPI_INT, previousCartesianRank, 0, cartesianCommunicator, MPI_STATUS_IGNORE);
        }
        if (cartesianRank != 0 && cartesianRank != worldSize - 1)
        {
            for (int j = 0; j < PART_LENGTH; j++)
            {
                part[j] = part[j] + 1;
            }
        }
        else if (cartesianRank == worldSize - 1)
        {
            for (int j = 0; j < PART_LENGTH; j++)
            {
                sum += part[j];
            }
        }

        MPI_Send(part, PART_LENGTH, MPI_INT, nextCartesianRank, 0, cartesianCommunicator);
        printf("Wysłano %d < %d - %d >\n", cartesianRank, *(part), *(part + PART_LENGTH - 1));
    }

    if (cartesianRank == worldSize - 1)
    {
        printf("Sum of received values: %lld\n", sum);
    }

    return EXIT_SUCCESS;
}