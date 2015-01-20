# Author: Paramvir "Paul" Hundal
# Date: February 7, 2013
# Homework 2
# Purpose:
# Program to read three numbers, store them in memory, adds the first
# two, subtracts the third from the sum of the two, and prints
# the result.
#

.text
.globl	main
main:
subu	$sp, $sp, 20	# Make additional stack space.
sw	$ra, 16($sp)		# Save the return address

# Ask the OS to read a number and store it in memory.
li	$v0, 5			# Code for read int.
syscall				# Ask the system for service.
sw      $v0, 12($sp)		# Copy to memory (this is x).

# Ask for another number.
li	$v0, 5			# Code for read int.
syscall				# Ask the system for service.
sw      $v0, 8($sp)		# Copy to memory (this is y).

# Ask for third number.
li  $v0, 5
syscall
sw      $v0, 4($sp) # This is z

# Load the two values from memory into registers
lw      $t0, 12($sp)             # Get first int (x)
lw      $t1, 8($sp)             # Get second int (y)
lw      $t2, 4($sp)             # Get third int  (z)

# Add the values we just loaded from memory
add	$t3, $t0, $t1           # Add the two values

# Subtract the sum from third value from the sum
sub $t4, $t3, $t2

# Now store the result
sw      $t4, 0($sp)             # This is result

# Ask the system to print it.
lw      $a0, 0($sp)             # Put the result where it can be
#    printed
li	$v0, 1			# Code for print int.
syscall

# Restore the values from the stack, and release the stack space.
lw	$ra, 16($sp)		# Retrieve the return address
addu	$sp, $sp, 20	        # Free up stack space.

# Exit system call:  this works with MARS and SPIM
li      $v0, 10
syscall