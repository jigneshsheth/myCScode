# File: bubble.s
# Purpose: To sort a list using bubble sort, calling x86
#	   function from the C main function.
#
# Args:   rdi = a, rsi = n
#
# Output: list a which is sorted
# compile instruction:
#			gcc -g -Wall -o bubble bubble.c bubble.s
#
# to run:
#
#			./bubble <n> <g|i>
#			g is for random input
#			i is for user input
# Author: Paul Hundal

    .section .text
    .global Bubble_sort

Bubble_sort:

            push    %rbp
            mov     %rsp, %rbp
            sub     $32, %rsp

            mov     %rdi, 0(%rsp)   # list-a
            mov     %rsi, 8(%rsp)   # n
            mov     %rsi, %r9      #list length

loop_out:


            cmp $2, %r9
            jl  done

            mov %r9, %r10
            sub $1, %r10        #store n-1=list_length

            mov $0, %r11

loop_inner:

            cmp %r10, %r11
            jge  break

            mov     0(%rdi, %r11, 8), %rdx
            mov     %rdx, 16(%rsp)

            mov     %r11, %r12
            add     $1, %r12        #i = i + 1
            mov     0(%rdi, %r12, 8), %rcx
            mov     %rcx, 24(%rsp)

            cmp     %rcx, %rdx
            jle     i_plus
            call    swap

break:

            sub     $1, %r9
            jmp     loop_out

swap:

            mov     %rdx, %r13
            mov     %rcx, 0(%rdi, %r11, 8)
            mov     %r13, 0(%rdi, %r12, 8)

i_plus:

            mov     %r12, %r11
            call    loop_inner

done:

            leave
            ret


