/**
 *  * Author:  Paramvir "Paul" Hundal
 *   * File:    mpi_samplesort.c
 *    * Purpose: Implement a parallel version of sample sort algorithm for
 *     *          sorting an array of user input values on different processes
 *      *          and merging them together at the end.
 *       *
 *        * Compile: mpicc -g -Wall -o mpi_samplesort mpi_samplesort.c
 *         * Run:     mpiexec -n <number of process> ./mpi_samplesort
 *          *
 *           * Input:   n:  The array size
 *            *          s:  The sample size
 *             *
 *              * Output:  output array after being updated on all processes.
 *               *
 *                * Notes:
 *                 * 1. n, the array size should be divisible by p.
 *                  * 2. The sample size s is < n.
 *                   */

#include <stdio.h>
#include <stdlib.h>
#include <math.h>
#include <mpi.h>
#include <string.h>

int intcompare(const void* a_p, const void* b_p);
int* GetSample(int local_n, int local_x[], int my_rank);
void GetList(int lst[], int n);
void PrintList(int local_A[], int rank, int local_n_p);
double GetSplitter(int my_rank, int sample_lst[], int local_n, int p);
void Butterfly(int my_rank, int p, double splits[], int* local_n,
               int local_lst[]);


int main(int argc, char* argv[]){
	int my_rank, local_n, local_s, p, i, s, n;
    double splitter; double* split_global;
	int* lst = NULL; int* local_x; int* sample;
	int* samples; int* sl_samples;

	MPI_Init(&argc, &argv);
	MPI_Comm_size(MPI_COMM_WORLD, &p);
	MPI_Comm_rank(MPI_COMM_WORLD, &my_rank);

	if (my_rank == 0){
		printf("Enter size of the sample: \n");
		scanf("%d", &s);
		printf("Enter (n) size of list: \n");
		scanf("%d", &n);
		lst = malloc(n*sizeof(int));

		GetList(lst, n);
	}

	MPI_Bcast(&s, 1, MPI_INT, 0, MPI_COMM_WORLD);
	MPI_Bcast(&n, 1, MPI_INT, 0, MPI_COMM_WORLD);

	local_n = n/p;
	local_s = s/p;

	local_x = (int*) malloc(local_n*sizeof(int));
	sample = (int*) malloc(local_s*sizeof(int));

	MPI_Scatter(lst, local_n, MPI_INT, local_x, local_n,
                MPI_INT, 0, MPI_COMM_WORLD);

    qsort(local_x, local_n, sizeof(int), intcompare);
	sample = GetSample(local_s, local_x, my_rank);
    samples = (int*) malloc(s*sizeof(int));

    MPI_Allgather(sample, local_s, MPI_INT, samples, local_s, MPI_INT,
                  MPI_COMM_WORLD);

    qsort(samples, s, sizeof(int), intcompare);
    sl_samples = (int*) malloc(local_s*sizeof(int));

    MPI_Scatter(samples, local_s, MPI_INT, sl_samples, local_s, MPI_INT, 0,
                MPI_COMM_WORLD);

    split_global = (double*) malloc(p*sizeof(double));
    splitter = GetSplitter(my_rank, sl_samples, local_s, p);

    MPI_Allgather(&splitter, 1, MPI_DOUBLE, split_global, 1, MPI_DOUBLE,
                  MPI_COMM_WORLD);

    Butterfly(my_rank, p, split_global, &local_n, local_x);

    PrintList(local_x, my_rank, local_n);
    free(sample);
    free(local_x);
    free(sl_samples);
    free(split_global);

	MPI_Finalize();
	return 0;
} /* main */


/*------------------------------------------------------------------------
 *  * Function:    Butterfly
 *   * Purpose:     Uses butterfly structured sample sort
 *    * Input:       my_rank    : current process
 *     *              p          : total number of processes
 *      *              split_global     : array of splitters
 *       *              local_n    : size of local list
 *        *              local_lst  : local list
 *         */
void Butterfly(int my_rank, int p, double splits[], int* local_n,
               int local_lst[]){
    int partner, i, c_i, c, n_i, mid = p/2, sz = *local_n;
    int* send = NULL; int* new = NULL; int* temp = NULL; int* lst = NULL;
    unsigned bitmask = (unsigned) (p/2);
    lst = malloc(*local_n*sizeof(int));

    memcpy(lst, local_lst, *local_n*sizeof(int));

    while (bitmask > 0 && mid > 0 && mid < p && bitmask < p){
        c_i = n_i = c = 0;
        send = new = temp = NULL;
        partner = my_rank ^ bitmask;

        if (my_rank < mid){
            for (i = 0; i < sz; i++){
                if (lst[i] > splits[mid]){;
                    c_i++;
                    send = realloc(send, c_i*sizeof(int));
                    send[c_i-1] = lst[i];
                } else {
                    n_i++;
                    new = realloc(new, n_i*sizeof(int));
                    new[n_i-1] = lst[i];
                }
            }
            mid--;

        } else if (my_rank >= mid){
            for (i = 0; i < sz; i++){
                if (lst[i] < splits[mid]) {
                    c_i++;
                    send = realloc(send, c_i*sizeof(int));
                    send[c_i-1] = lst[i];
                } else {
                    n_i++;
                    new = realloc(new, n_i*sizeof(int));
                    new[n_i-1] = lst[i];
                }
            }
            mid++;
        }

        MPI_Status status;

        MPI_Send(send, c_i, MPI_INT, partner, 0, MPI_COMM_WORLD);
        MPI_Probe(partner, 0, MPI_COMM_WORLD, &status);
        MPI_Get_count(&status, MPI_INT, &c);

        temp = malloc(c*sizeof(int));

        MPI_Recv(temp, c, MPI_INT, partner, 0, MPI_COMM_WORLD, &status);

        lst = realloc(lst, (n_i + c)*sizeof(int));
        sz = (n_i + c);

        for (i = 0; i < n_i; i++){
            lst[i] = new[i];
        }

        for (i = n_i; i < sz; i++){
            lst[i] = temp[i - n_i];
        }

        if (send != NULL) free(send);
        if (new != NULL) free(new);
        if (temp != NULL) free(temp);

        bitmask >>= 1;
    }

    qsort(lst, sz, sizeof(int), intcompare);
    local_lst = realloc(local_lst, sz*sizeof(int));
    memcpy(local_lst, lst, sz*sizeof(int));
    *local_n = sz;
} /* Butterfly */


/*------------------------------------------------------------------------
 *  * Function:    Splitter
 *   * Purpose:     To get the splitter
 *    * Input:       my_rank    : my current rank (process)
 *     *              local_x    : number of elements in local list
 *      *              sample : the sample list
 *       *              p          : total number of processes
 *        * Output:      splitter
 *         */
double GetSplitter(int my_rank, int sample_lst[], int local_n, int p) {
	double splitter = 0.0;
	int temp;

    if (my_rank != p-1) {
   	    MPI_Send(&sample_lst[local_n-1], 1, MPI_INT, my_rank + 1, 0,
                 MPI_COMM_WORLD);
   	}

   	if (my_rank != 0) {
        MPI_Recv(&temp, 1, MPI_INT, my_rank - 1, 0, MPI_COMM_WORLD,
                 MPI_STATUS_IGNORE);
        splitter = (temp + sample_lst[0]) / 2.0;
    }

    if (my_rank == 0) {
    	return sample_lst[0];
    }

	return splitter;
} /* GetSplitter */


/*------------------------------------------------------------------------
 *  * Function:    GetSample
 *   * Purpose:     To get a sample of the list you want sorted.
 *    * Input:       local_n   : number of elements in the local list
 *     *              local_x : the local list
 *      *              my_rank   : current process/rank
 *       * Output:      sorted sample of the list.
 *        */
int* GetSample(int local_n, int local_x[], int my_rank) {
	int* sample;
	int sub;
	int i;
	int temp = 0;

	sample = (int*) malloc(local_n*sizeof(int));

	srandom(my_rank + 1);

	do {
		sub = random() % local_n;

		for (i = 0; i <= local_n; i++) {
         	if (local_x[sub] == sample[i]) {
         		break;
         	} else if (i == local_n && local_x[sub] != sample[i]){
         		sample[temp] = local_x[sub];
         		temp++;
         		break;
      		}
        }
	} while (temp != local_n);

	qsort(sample, local_n, sizeof(int), intcompare);

    return sample;
} /* GenerateSortedSample */

/*------------------------------------------------------------------------
 *  * Function:    intcompare
 *   * Purpose:     To compare two ints, used by the qsort library to compare
 *    *              2 different int values.
 *     *
 *      * Input:       a_p
 *       *              b_p
 *        *
 *         * Output:      1 || -1 || 0
 *          *
 *           */
int intcompare(const void* a_p, const void* b_p) {
    int a = *((int*)a_p);
    int b = *((int*)b_p);

    if (a < b)
        return -1;
    else if (a == b)
        return 0;
    else /* a > b */
        return 1;
}  /* intcompare */


/*------------------------------------------------------------------------
 *  * Function:    GetList
 *   * Purpose:     Prompts user to input list of elements
 *    * Input:       lst      : array of ints
 *     *              n        : number of elements in lst
 *      */
void GetList(int lst[], int n) {
    int   i;

    printf("Enter a list of %d elements: \n", n);
    for (i = 0; i < n; i++)
        scanf(" %d", &lst[i]);
} /* GetList */


/*------------------------------------------------------------------------
 *  * Function:     PrintList
 *   * Purpose:      Prints out the list
 *    * Input:        local_A       : array of ints
 *     *               rank          : process
 *      *               local_n_p     : number of elements in local_A
 *       *
 *        */
void PrintList(int local_A[], int rank, int local_n_p) {
    int i;
    printf("\nProcess %d >", rank);
    for (i = 0; i < local_n_p; i++) {
        printf(" %d ", local_A[i]);
    }
    printf("\n");
}  /* PrintList */