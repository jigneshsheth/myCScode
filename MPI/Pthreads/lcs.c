/* Author: Paramvir "Paul" Hundal
 * Purpose: To find the longest common subsequence
 *
 * Input: Global array's A, B
 *
 * Output: L (longest common subsequence)
 *
 * Compile:
 * 	gcc -g -Wall -o p5 lcs.c -lpthread
 * Usage:
 *	./p5 <thread_count>
 *
 * Algorithm:
 *	0. Compute the diag_leng
 *	1. Compute row for thread
 *	2. Compute col for thread
 *	3. Compute the LCS
 * Note:
 *	Barrier cannot be run on MAC machines
 *	Serial Version of the code is fully functional.
 */
#include<stdlib.h>
#include<stdio.h>
#include<pthread.h>

/*Global Variables*/
int     thread_count;
pthread_mutex_t    sequence_mutex;
pthread_cond_t ok_to_proceed;
//pthread_barrier_t barrier;


int     m , n;
int*    A;
int*    B;
int*    L;

/*Serial Functions*/
void Usage(char* prog_name);
int Get_diag_len(int diag);
int Compute_row(int diag, int diag_entry);
int Compute_col(int diag, int diag_entry);
int Compute_L(int row, int col);
int LCS_serial(int row, int col);
int Length(int x);

/*Parallel Function*/
void *Pth_lcs_sol(void* rank);

int main(int argc, char* argv[]) {
    long        thread;
    pthread_t*  thread_handles;
    int         m, n;
    int         i;

    if(argc!= 2) Usage(argv[0]);
    thread_count = strtol(argv[1], NULL, 10);
    thread_handles = malloc(thread_count*sizeof(pthread_t));

    printf("Enter the array size of A: \n");
    scanf("%d", &m);
    A = (int *)malloc(m*sizeof(int));
    printf("Enter the array size of B: \n");
    scanf("%d", &n);
    B = (int *)malloc(n*sizeof(int));
    L = (int *)malloc(m*n*sizeof(int));

    printf("Insert your array A: \n");
    for(i = 0; i < m; i ++) {
	scanf("%d", &A[i]);
    }

    printf("Insert your array B: \n");
    for(i = 0; i < n; i ++) {
	scanf("%d", &B[i]);
    }

    //pthread_barrier_init(barrier, NULL, thread_count);
    pthread_mutex_init(&sequence_mutex, NULL);
    pthread_cond_init(&ok_to_proceed, NULL);

    for(thread = 0; thread < thread_count; thread++) {
        pthread_create(&thread_handles[thread], NULL,
                     Pth_lcs_sol, (void*) thread);
    }

    for(thread = 0; thread < thread_count; thread++) {
        pthread_join(thread_handles[thread], NULL);
    }

    printf("The LCS is: \n");
    for(i = 0; i < m*n; i++) {
	printf("%d", L[i]);
    }
    pthread_mutex_destroy(&sequence_mutex);
    pthread_cond_destroy(&ok_to_proceed);
    //pthread_barrier_destroy(&barrier);
    free(A);
    free(B);
    free(L);
    free(thread_handles);

    return 0;
}

/*------------------------------------------------------------------
 * Function:    Pth_lcs_sol
 * Purpose:     To compute the longest common subsequence
 *
 * In arg:      arrays A and B
 */

void *Pth_lcs_sol(void* rank) {
    long my_rank = (long) rank;
    int row, col;
    int diag_len;
    int diag_entry;

    int diag_count = m + n -1;
    int diag;
    for(diag = 0; diag < diag_count; diag++) {
        diag_len = Get_diag_len(diag);
        for(diag_entry = my_rank; diag_entry < diag_len;
            diag_entry += thread_count) {
            row = Compute_row(diag, diag_entry);
            col = Compute_col(diag, diag_entry);
            L[row,col] = Compute_L(row, col);
            }
           //pthread_barrier_wait(&barrier);
        }
} /*Pth_lcs_sol*/

/*------------------------------------------------------------------
n:   LCS_serial
 * Purpose:    Calculates the LCS
 * Input:      A     :  the sequence of A          (global)
 *             B     :  the sequence of B          (global)
 *             m     :  the size of the sequence A (global)
 *             n     :  the size of the sequence B (global)
 *             L     :  the LCS                    (global)
 *             row   :  row
 *             col   : column
 * Output:     the LCS
 *
 */
int LCS_serial(int row, int col) {
	int i, j, len1, len2;

	for (i = 0; i < m; i++) {
		for (j = 0; j < n; j++) {
			if (A[i] == B[j]) {
				if (i == 0)
					L[i*n+j] = L[(i)*n + j-1] + 1;
				else if (j==0)
					L[i*n+j] = L[(i-1)*n + j] + 1;
				else if (i==0 && j == 0)
					L[i*n+j] = L[(i)*n + j] + 1;
				else
					L[i*n+j] = L[(i-1)*n + j-1] + 1;
			} else {
				len1 = Length(i*n+j-1);
 				len2 = Length((i-1)*n+j);
				if (len1 > len2) {
					L[i*n+j] = L[(i*n+j)-1];
				} else {
					L[i*n+j] = L[(i*n) -1 +j];
				}
			}
		}
	}

	return L[m*n-1];
} /* LCS_serial */

/*------------------------------------------------------------------
 * Function:    Compute_L
 * Purpose:     To get the lcs
 *
 * In arg:      row, col
 *
 * Out arg:     none
 */
int Compute_L(int row, int col) {
	int len1, len2;

	for (row = 0; row < n; row++) {
		for (col = 0; col < m; col++) {
			if (A[row] == B[col]) {
				if (row == 0)
					L[row*m + col] = L[(row)*m + col - 1] + 1;
				else if (col == 0)
					L[row*m + col] = L[(row - 1)*m + col] + 1;
				else if (row == 0 && col == 0)
					L[row*m + col] = L[(row)*m + col] + 1;
				else
					L[row*m + col] = L[(row-1)*m + col - 1] + 1;
			} else {
				len1 = Length(row*m + col - 1);
				len2 = Length((row - 1)*m + col);
				if (len1 >= len2) {
					L[row*m + col] = L[(row*m + col) - 1];
				} else {
					L[row*m + col] = L[(row*m) - 1 + col];
				}
			}
		}
	}


	return L[row*m + col];
} /* Compute_L */

/*------------------------------------------------------------------
 * Function:    Get_diag_len
 * Purpose:     To get the length of the diagonal
 *
 * In arg:      the diag
 *
 * Out arg:     diag_length
 */

int Get_diag_len(int diag) {
    if(diag < n) {
        return diag + 1;
    } else {
        return n + m -1 - diag;
    }

} /* Get_diag_len */


/*------------------------------------------------------------------
 * Function:    Compute_row
 * Purpose:     Compute each row
 *
 * In arg:      diag, diag_entry
 *
 */
int Compute_row(int diag, int diag_entry) {
    if(diag < m) {
        return diag - diag_entry;
    } else {
        return m - 1 - diag_entry;
    }

} /*Compute_row*/


/*----------------------------------------------------------
 *
 * Function:    Compute_col
 * Purpose:     Compute each column
 *
 * In arg:      diag, diag_entry
 *
 */
int Compute_col(int diag, int diag_entry) {
    if(diag < n) {
        return diag - diag_entry;
    } else {
        return diag - m + 1 + diag_entry;
    }

} /*Compute_col*/

/*--------------------------------------------------------------------
 * Function:   Length
 * Purpose:    Calculates current length
 * Input:      x     :  length
 * Output:     total length
 *
 */
int Length(int x) {
	int count;

	for (count = 0; count <= x; count++) {
		count++;
	}
	return count;
}

/*------------------------------------------------------------------
 * Function:  Usage
 * Purpose:   print a message showing what the command line should
 *            be, and terminate
 * In arg :   prog_name
 */
void Usage (char* prog_name) {
    fprintf(stderr, "usage: %s <thread_count> <m> <n>\n", prog_name);
    exit(0);
}  /* Usage */