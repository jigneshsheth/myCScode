/* findminmax_seq.c - find the min and max values in a random array
 *
 * usage: ./findminmax <seed> <arraysize>
 *
 */
#include <stdio.h>
#include <stdlib.h>
#include <unistd.h>
#include "measure.h"
#include <sys/types.h>
#include <fcntl.h>

/* a struct used to pass results to caller */
struct results {
    int min;
    int max;
};

/* given an array of ints and the size of array, find min and max values */
struct results find_min_and_max(int *subarray, int n)
{
    int i, min, max;
    min = max = subarray[0];
    struct results r;
    
    for (i = 1; i < n; i++) {
        if (subarray[i] < min) {
            min = subarray[i];
        }
        if (subarray[i] > max) {
            max = subarray[i];
        }
    }
    
    r.min = min;
    r.max = max;
    return r;
}

int main(int argc, char **argv)
{
    int *array;
    int arraysize = 0;
    int seed;
    char randomstate[8];
    struct results r;
    int i, pid;
    int j, y, x, fd, nprocs, chunk;
    int *fildes;
        
    /* process command line arguments */
    if (argc != 4) {
        printf("usage: ./findminmax <seed> <arraysize> <nprocs>\n");
        return 1;
    }
    
    seed = atoi(argv[1]);
    arraysize = atoi(argv[2]);
    nprocs = atoi(argv[3]);
    
    /* allocate array and populate with random values */
    array = (int *) malloc(sizeof(int) * arraysize);
    fildes = (int *) malloc(sizeof(int) * 2 * nprocs);
    chunk = arraysize / nprocs;
    
    initstate(seed, randomstate, 8);
    
    for (i = 0; i < arraysize; i++) {
        array[i] = random();
    }
    
    
    /* begin computation */
    
    mtf_measure_begin();
    
    for(fd = 0; fd < nprocs; fd++) {
        pipe(fildes + (2*fd));
    }
    
    for(j = 0; j < nprocs; j++) {
        
        pid = fork();
        if(pid == -1) {
            printf("Fork error.");
            exit(1);
        }
        
        if(pid == 0) {
           
            r = find_min_and_max(array, chunk);
            close(fildes[j*2]); 
            write(fildes[j*2+1], &r, sizeof(struct results));
            close(fildes[j*2+1]);
            exit(0);
        }
        
        else {
            
            array = array + chunk;
            
        }
    }
    
    for(y = 0; y < nprocs; y++) {
        pid = wait(NULL);
    }
    
    for(x = 0; x < nprocs; x++) {
        
        close(fildes[x*2+1]);
        struct results vals;
        /* Read in a string from the pipe */
        read(fildes[x*2], &vals, sizeof(struct results));
        
        if(vals.min <= r.min) {
            r.min = vals.min;
        }
        
        if(vals.max >= r.max) {
            r.max = vals.max;
        }
        
        close(fildes[x*2]);
    }
    
    
    mtf_measure_end();
    
    printf("Execution time: ");
    mtf_measure_print_seconds(1);
    
    printf("min = %d, max = %d\n", r.min, r.max);
    
    return 0;
}