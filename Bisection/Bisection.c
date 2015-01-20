/*Author: Paramvir "Paul" Hundal
 *September 9, 2013
 *File: bisection.c
 *Purpose: Calculate an approximation to f(x) = 0 using bisection rule
 *Input: a, b, tol, max_iters
 *Output: A message stating if the output converged, the number of iterations, the approximate
 *solution, and the value of the function at the approximate solution.
 *
 *
 *
 *Compile: gcc -g -Wall -o bisection Bisection.c
 *Run: ./bisection
 *
 *Note: The function f(x) is hardwired.
 */


#include <stdio.h>
#include <math.h>
double f(double x);
double bisection(double a, double b, double tol, int max_iters);

int main(void){
    double a, b;
    double tol;
    int max_iters;
    printf("Please enter a: \n");
    scanf("%lf", &a);
    printf("Please enter b: \n");
    scanf("%lf", &b);
    printf("Please enter your desired tolerance: \n");
    scanf("%lf", &tol);
    printf("Please enter the maximum number of iterations: \n");
    scanf("%d", &max_iters);

    double value = bisection(a, b, tol, max_iters);
    printf("\nThe approximate solution is: %lf\n", value);
    printf("The function value of the approximate solution is: %lf\n", f(value));
    return 0;

}

/*main*/


/*-----------------------------------------------------------

 *Function:     Bisection

 *Purpose:      To compute the bisection method with approximation

 *Input args    a: first approximation

                b: second approximation

                tol: The tolerance of upper bounded iterations

                max_iters: The maximum number of user iterations

 *Return value: Return of the midpoint approximation and actual value

 *

 */


double bisection(double a, double b, double tol, int max_iters){

    int iters = 0;
    double m = (a+b)/2;
    if(f(m) == 0){
        printf("The function converged at value: %f\n", f(m));
        return 0;
       if(f(a)*f(b) > 0){
            printf("Error: The function did not converge!");
            return 0;
        } else if (f(a) == 0){
            printf("The solution is equal to: %f", a);
            return 0;
        } else if(f(b) == 0){
            printf("The solution is equal to: %f", b);
            return 0;
        }
    }


    while(iters < max_iters && fabs(b-a) > tol){

        iters++;
        m = (a+b)/2;
        if(f(m) == 0){
            printf("It took %d iterations to converge.", iters);
            return m;
        } else if(f(m)*f(a) < 0){
            b = m;
        } else if(f(m)*f(b) < 0){
            a = m;
        }
    }

    printf("The function iterated for a total of %d iterations.\n", iters);
    printf("The function did not converge!");
    return m;



}

/*bisection*/


/*-----------------------------------------------------------

 *Function:     f

 *Purpose:      The function that we are approximating

 *Input args    x: The value for which we are computing the function

 *Return value: The computed value of the function

 *

 */


double f(double x){

    double functionValue;
    functionValue = x*x - 2;
    return functionValue;

}

/*f*/