/* Author: Paul Hundal
 * Date: September 2, 2014
 * Purpose: 
 *          To use the fork method and piping to find min and max values of a random array
 * findminmax_threaded.c - find the min and max values in a random array using processes
 *
 * usage: ./findminmax <seed> <arraysize> <processes>
 * put timings here!
 * put example of the odd array size case here
 * put on git
 *
 */
#include <stdio.h>
#include <stdlib.h>
#include <unistd.h>
#include"measure.h"
#include <sys/types.h>
#include <fcntl.h>
#include <string.h>
#include <sys/wait.h>

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
    int i, j, fd, x, y;
    int nprocs;
    int chunk;

    
    /* process command line arguments */
    if (argc != 4) {
        printf("usage: ./findminmax <seed> <arraysize> <nprocs>\n");
        return 1;
    }
    
    seed = atoi(argv[1]);
    arraysize = atoi(argv[2]);
    nprocs = atoi(argv[3]);
    
    /* allocate array and populate with random values */
    chunk = arraysize / nprocs;
    array = (int *) malloc(sizeof(int) * arraysize);
 
    
    initstate(seed, randomstate, 8);
    
    
    for (i = 0; i < arraysize; i++) {
        array[i] = random();
    }
    
    /* begin computation */
    
    mtf_measure_begin();
    
    int pid;

    for(j = 0; j < nprocs; j++) {
        pid = fork();
        
        char *file;
        file = malloc(sizeof(char) * 1);
        
        sprintf(file, "%d", j);
        
        if ( (fd = open(file, O_CREAT | O_WRONLY, 0600)) < 0) {
            printf("Cannot open %s\n", file);
            exit(1);
        }
        
        
        if(pid < 0) {
            printf("Error in making child processes.");
            exit(0);
        } else if(pid == 0) { 
            r = find_min_and_max(array, chunk);
            write(fd, &r, sizeof(struct results));
            close(fd);
            exit(0);
                
            
        } else {
            array = array + chunk;
        
    }
        
    for(x = 0; x < nprocs; x++) {
        pid = wait(NULL);
    }
       
    for(y = 0; y < nprocs; y++) {
        if ((fd = open(file, O_RDONLY)) < 0) {
            printf("Cannot open %s\n", file);
            exit(1);
        }
        
        struct results vals;
        read(fd, &vals, sizeof(struct results));
        
        if (vals.min <= r.min){
            r.min = vals.min;
        }
    
        if (vals.max >= r.max){
            r.max = vals.max;
        }
    
        }
    
  }
    
    
    
    mtf_measure_end();
    
    printf("Execution time: ");
    mtf_measure_print_seconds(1);
    
    printf("min = %d, max = %d\n", r.min, r.max);
    close(fd);
    return 0;
}
