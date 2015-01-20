# Author: Paul Hundal
# Date: February 11, 2014
# Purpose:
# Program to read a positive integer n, and compute the nth Fibonacci number,
# using recursion.
#
#     f_0 = 0
#     f_1 = 1
#     f_n = f_(n-1) + f_(n-2), n >= 2 
	.data
	 msg: .asciiz "Give the nth number to compute: "
	.text
	.globl	main
main:
	subu	$sp, $sp, 4 	        # Make additional stack space.
	sw	$ra, 0($sp)		# Save the return address
	
	# Print message
	li 	$v0, 4
	la	$a0, msg
	syscall
	
	# Ask the OS to read a number and put it in a temporary register
	li	$v0, 5			# Code for read int.
	syscall				# Ask the system for service.
	move    $a0, $v0                # Put n in a safe place

	jal	fib_recurse		# call the recursion
	
	move	$a0, $v0
	li	$v0, 1
	syscall
	
	li	$v0, 10
	syscall
        
fib_recurse:
	addi	$sp, $sp, -12		# save in stack
	sw	$ra, 0($sp)
	sw	$s0, 4($sp)
	sw	$s1, 8($sp)
	
	move	$s0, $a0		# move n to $s0
	
	addi	$t1, $zero, 1
	beq	$s0, $zero, is_zero	# check if n = 0
	beq	$s0, $t1, is_one	# check if n = 1
	
	addi	$a0, $s0, -1		
	
	jal 	fib_recurse		# call itself
	
	add	$s1, $zero, $v0		# fib(n-1)
	
	addi	$a0, $s0, -2
	
	jal	fib_recurse		# fib(n-2)
	
	add	$v0, $v0, $s1		# v0 = fib(n-2) + $s1

exit_fib:
	lw	$ra, 0($sp)
	lw	$s0, 4($sp)
	lw	$s1, 8($sp)
	addi	$sp, $sp, 12
	jr	$ra
	
is_one:
	li	$v0, 1
	j	exit_fib

is_zero:
	li	$v0, 0
	j	exit_fib