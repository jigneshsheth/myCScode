/** Author:    Paramvir "Paul" Hundal
*   Project 3: Monte Carlo Approximation
*   Date:      October 23, 2013
*   Purpose:   Parallelizing Monte Carlo randomization
*		algorithm to approximate the value of pi.
*   Input:	argc, argv
*   Output:	PI
*		Estimated PI
*		Number of Darts
*		Error in Estimate
*		Time of Computation
*   Compile:	mpicc -D MACRO -Wall -o p3 mpi_monte_carlo.c
*		NOTE*: MACRO refers to user choice of:
*		LOOP, TREE, BFLY, Reduce, Allreduce
*
*   Run:	mpiexec -n <p> ./p3 n-value
*		NOTE*: <p> is the number of processes
*		n-value is the input for darts
*/

#include <stdio.h>
#include <stdlib.h>
#include <math.h>
#include <time.h>
#include <mpi.h>

int Butterfly(int my_contrib, int my_rank, int p, MPI_Comm comm);
int Global_sum(int my_contrib, int my_rank, int p, MPI_Comm comm);
double Get_max_time(double par_elapsed, int my_rank, int p);

int main(int argc, char *argv[]) {

    int procID; /*process size */
    int my_rank; /*process rank */
    int i;      /*index*/
    int q = 0;	/*loop variable */
    long long int local_n; /*local n value */

    long long int pi_recv = 0;  /*Receiving variable*/
    long long int sum;	/* The sum of darts*/
    long long int n;    /*number of processors*/
    long long int in_circle_count= 0; /*dart count*/
    double pi_estimate;	/* Estimate of PI */

    double PI = 4.0*atan(1.0); /*PI*/
    double x,y;       /*coordinates b/w -1 & 1*/
    double start_time, finish_time = 0; /* clock time*/
    double error;       /* error in PI calculation*/
    double diff_time;   /*difference in time*/
    double totaltime;


    MPI_Init(&argc, &argv);

    MPI_Comm_rank(MPI_COMM_WORLD, &my_rank);
    MPI_Comm_size(MPI_COMM_WORLD, &procID);

    if(argc != 2 ) {
	printf("Not enough input arguments!");
	MPI_Finalize();
	exit(0);
    	}
	 else {
	n = strtol(argv[1], NULL, 10);
    }


    MPI_Bcast(&n, 1, MPI_LONG_LONG, 0, MPI_COMM_WORLD);

    start_time = MPI_Wtime();

    local_n = n/procID;

    srand(my_rank+1);
    for(i = 0; i < local_n; i++){
        x = random()/((double) RAND_MAX);
        x = 2.0*x - 1.0;
        y = random()/((double) RAND_MAX);
        y = 2.0*y - 1.0;


        if(x*x + y*y <= 1.0){
           in_circle_count++;
        }
    }

#  ifdef LOOP

    MPI_Status status;
    if(my_rank == 0) {
	sum = in_circle_count;
	for(q = 1; q <procID ; q++){
            MPI_Recv(&pi_recv, 1, MPI_LONG_LONG, q, 0, MPI_COMM_WORLD, &status);
	    sum += pi_recv;
        }
    }
	else{
        MPI_Send(&in_circle_count, 1, MPI_LONG_LONG, 0, 0, MPI_COMM_WORLD);
        }

#   elif defined TREE

    sum = Global_sum(in_circle_count, my_rank, procID, MPI_COMM_WORLD);

#   elif defined Reduce

    MPI_Reduce(&in_circle_count, &sum, 1, MPI_LONG_LONG, MPI_SUM, 0, MPI_COMM_WORLD);

#   elif defined Allreduce

    MPI_Allreduce(&in_circle_count, &sum, 1, MPI_LONG_LONG, MPI_SUM, MPI_COMM_WORLD);

#    else
    sum = Butterfly(in_circle_count, my_rank, procID, MPI_COMM_WORLD);

#endif



    finish_time = MPI_Wtime();
    diff_time = finish_time - start_time;
    totaltime = Get_max_time(diff_time, my_rank, procID);

    if(my_rank == 0) {
        pi_estimate = (4.0*sum)/n;
	error = fabs(pi_estimate - PI);

	printf("    MONTE CARLO PI ESTIMATION\n");
	printf("    Total number of darts : %lli\n", n);
        printf("    Known value of PI : %11.10f\n", PI);
        printf("    Number of darts that hit : %llu\n", sum);
	printf("    Estimated value of PI: %f\n", pi_estimate);
        printf("    Elapsed time(sec)   : %10.8f\n", totaltime);
        printf("    Error in calculation :  %11.10f\n" ,error);
    }
    MPI_Finalize();

    return 0;
  }

/*-----------------------------------------------------------------*/
/* Function:    Global_sum
 * Purpose:     Compute the global sum of ints stored on the processes
 *
 * Input args:  my_contrib = process's contribution to the global sum
 *              my_rank = process's rank
 *              p = number of processes
 *              comm = communicator
 * Return val:  Sum of each process's my_contrib:  valid only
 *              on process 0.
 *
 * Notes:
 *    1.  Uses tree structured communication.
 *    2.  p, the number of processes must be a power of 2.
 *    3.  The return value is valid only on process 0.
 *    4.  The pairing of the processes is done using bitwise
 *        exclusive or.  Here's a table showing the rule for
 *        for bitwise exclusive or
 *           X Y X^Y
 *           0 0  0
 *           0 1  1
 *           1 0  1
 *           1 1  0
 *        Here's a table showing the process pairing with 8
 *        processes (r = my_rank, other column heads are bitmask)
 *           r     001 010 100
 *           -     --- --- ---
 *           0 000 001 010 100
 *           1 001 000  x   x
 *           2 010 011 000  x
 *           3 011 010  x   x
 *           4 100 101 110 000
 *           5 101 100  x   x
 *           6 110 111 100  x
 *           7 111 110  x   x
 */

int Global_sum(int my_contrib, int my_rank, int p, MPI_Comm comm) {
    int sum = my_contrib;
    int temp;
    int partner;
    int done = 0;
    unsigned bitmask = (unsigned) 1;

#   ifdef DEBUG
    int my_pass = -1;
    partner = -1;
    printf("Proc %d > partner = %d, bitmask = %d, pass = %d\n",
           my_rank, partner, bitmask, my_pass);
    fflush(stdout);
#   endif

    while (!done && bitmask < p) {
        partner = my_rank ^ bitmask;
#       ifdef DEBUG
        my_pass++;
        printf("Proc %d > partner = %d, bitmask = %d, pass = %d\n",
               my_rank, partner, bitmask, my_pass);
        fflush(stdout);
#       endif
        if (my_rank < partner) {
            if (partner < p) {
                MPI_Recv(&temp, 1, MPI_INT, partner, 0, comm,
                         MPI_STATUS_IGNORE);
                sum += temp;
            }
            bitmask <<= 1;
        } else {
            MPI_Send(&sum, 1, MPI_INT, partner, 0, comm);
            done = 1;
        }
    }
    /* Valid only on 0 */
    return sum;
}  /* Global_sum */


/*------------------------------------------------------------------
 * Function:   Get_max_time
 * Purpose:    Find the maximum elapsed time across the processes
 * In args:    my_rank:       calling process' rank
 *             p:             total number of processes
 *             par_elapsed:   elapsed time on calling process
 * Ret val:    Process 0:     max of all processes times
 *             Other procs:   input value for par_elapsed
 */
double Get_max_time(double par_elapsed, int my_rank, int p) {
   int source;
   MPI_Status status;
   double temp;

   if (my_rank == 0) {
      for (source = 1; source < p; source++) {
         MPI_Recv(&temp, 1, MPI_DOUBLE, source, 0, MPI_COMM_WORLD, &status);
         if (temp > par_elapsed) par_elapsed = temp;
      }
      } else {
      MPI_Send(&par_elapsed, 1, MPI_DOUBLE, 0, 0, MPI_COMM_WORLD);
   }
   return par_elapsed;
}  /* Get_max_time */

/*-----------------------------------------------------------------------
*Function:	Butterfly
*Purpose:	Butterfly structure used to find Global Sum
*In args:	my contrib:	process contributor to global sum
*		my_rank:	process rank
*		p:		number of processes
*		comm:		communicator
*Return val:	Sum of the my_contrib for each process
*
*/

int Butterfly(int my_contrib, int my_rank, int p, MPI_Comm comm) {

	int sum = my_contrib;
	int temp;
	int partner;
	unsigned bitmask = (unsigned) 1;
	MPI_Status status;

	while(bitmask<p) {
		partner = my_rank ^ bitmask;
		MPI_Send(&sum, 1, MPI_INT, partner, 0, comm);
		MPI_Recv(&temp, 1, MPI_INT, partner, 0, comm, MPI_STATUS_IGNORE);
		sum+=temp;
		bitmask<<=1;
	}
	return sum;
}
