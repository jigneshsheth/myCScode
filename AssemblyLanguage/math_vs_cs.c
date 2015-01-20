/* Author: Paul Hundal
 * Program: Math_vs_CSMethod
 * Output:
 *	Tells if the compiler produces a remainder 
 *	and quotient using the CS or Math way.
 *
 * Input: Divisor and Dividend
 * Output: math or cs method
 *
 * Instructions: Enter the divisor and dividend from input
 * Compile:	gcc -g -Wall -o check ./math_vs_cs.c
 * Run:		./check
 *
 * NOTE: When run on the lab machines the compiler used produces
 * 	 an output using the CS method.
 * 
 */
#include <stdio.h>
#include <stdlib.h>

int main(void) {
    
    int quotient;
    int remainder;
    int divisor;
    int dividend;
    
    printf("Enter the dividend followed by divisor: \n");
    scanf("%d %d", &dividend, &divisor);
    

    quotient = (dividend/divisor);
    
    remainder = ((dividend) % (divisor));
    
    if(remainder < 0){
        if((dividend > 0 && divisor > 0) || (dividend < 0 && divisor < 0)) {
            if(quotient > 0) {
                printf("This is the CS Method");
            }
        } else if((dividend > 0 && divisor < 0) || (dividend < 0 && divisor > 0)) {
            if(quotient < 0) {
                printf("This is the CS Method");
            }
        }
    } else {
        printf("This is the Mathemiticians Method.");
    }
    return 0;
}
