# Author: Paramvir "Paul" Hundal
# Purpose:
# Reads 2 ints and compares them to one another
# Prints if the first is greater than second, or
# if the seconds is greater than first.
# 
	.text
	.globl	main
main:
	subu	$sp, $sp, 4 	        # Make additional stack space.
	sw	$ra, 0($sp)		# Save the return address
	
	# Ask the OS to read a number and put it in a temporary register
	li	$v0, 5			# Code for read int.
	syscall				# Ask the system for service.
	move    $t0, $v0                # Put the input value in a safe
                                        #    place
                                        
        li	$v0, 5
        syscall
        move	$t1, $v0
        
        # Branch to appropriate print statement
        beq     $t1, $t0, equals         # if $t1 == 0, go to ge0
        
        blt	$t0, $t1, ge0

        # Print that input value is < 0
        li      $v0, 4                  # Code to print a string
        la      $a0, lt_msg             # Put the string in $a0
        syscall                         # Print the string
        j done                          # Skip next few statements:  go
                                        #    to done
equals:
	li	$v0, 4
	la	$a0, eq_msg
	syscall
	
        # Print that input value is >= 0
ge0:    li      $v0, 4                  # Code to print a string
        la      $a0, ge_msg             # Put the string in $a0
        syscall                         # Print the string

	# Restore the values from the stack, and release the stack space.
done:	lw	$ra, 0($sp)		# Retrieve the return address
	addu	$sp, $sp, 4 	        # Free added stack space.

        # Exit system call:  SPIM and MARS
        li      $v0, 10
        syscall

        .data
lt_msg: .asciiz "First is greater than second/n"
ge_msg: .asciiz "First is less than second/n"
eq_msg: .asciiz "They are equal/n"