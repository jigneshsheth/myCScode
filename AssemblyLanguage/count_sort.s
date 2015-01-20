# Author:     Paul Hundal
 # File:      count_sort.s
# Purpose:   Use count sort to sort a user-input list of ints
#
# Input:     Number of ints in list (n)
#            Elements of list (one element per line)
# Output:    Sorted list
# 
# Note:      list is statically allocated on the stack
	.text
	.globl	main
main:
	addi	$sp, $sp, -812          # Make additional stack space.
                                        #   3 words for $ra, $s0, $s1
                                        #   100 words for list 
                                        #   100 words for temp list
        sw      $ra, 808($sp)           # Put contents of $ra on stack
        sw      $s0, 804($sp)           # Put $s0 on stack
        sw      $s1, 800($sp)           # Put $s1 on stack
        move    $s0, $sp                # $s0 = stores start address of list
                                        #     = $sp

	# Ask the OS to read a number and put it in $s1 = n
	li	$v0, 5			# Code for read int.
	syscall				# Ask the system for service.
	move    $s1, $v0                # Put the input value (n) in a safe
                                        #    place

        # Now read in the list
        move    $a0, $s0                # First arg is list
        move    $a1, $s1                # Second arg is n
        jal     rd_lst

        # Now sort the list
        move    $a0, $s0                # First arg is list
        move    $a1, $s1                # Second arg is n
        jal     count_sort

        # Now print the list
        move    $a0, $s0                # First arg is list
        move    $a1, $s1                # Second arg is n
        jal     pr_lst

        # Prepare for return
        lw      $ra, 808($sp)           # Retrieve return address
        lw      $s0, 804($sp)           # Retrieve $s0
        lw      $s1, 800($sp)           # Retrieve $s1
	addi	$sp, $sp, 412           # Make additional stack space.
        # jr    $ra                     # Return, for SPIM

        li      $v0, 10                 # For MARS
        syscall
        
                ###############################################################
        # Read_list function
        #    $a0 is the address of the beginning of list (In/out)
        #    $a1 is n (In)
        # Note:  $a0 isn't changed:  the block of memory it refers
        #    to is changed
rd_lst: 
        # Setup
        addi    $sp, $sp, -4            # Make space for return address
        sw      $ra, 0($sp)             # Save return address

        # Main for loop
        move    $t0, $zero              # $t0 = i = 0
rd_tst: bge     $t0, $a1, rddone        # If  i = $t0 >= $a1 = n 
                                        #    branch out of loop.
                                        #    Otherwise continue.
	li	$v0, 5			# Code for read int.
	syscall				# Ask the system for service.
        sll     $t1, $t0, 2             # Words are 4 bytes:  use 4*i, not i
        add     $t1, $a0, $t1           # $t1 = list + i
	sw      $v0, 0($t1)             # Put the input value in $v0 in
                                        #    list[i]
        addi    $t0, $t0, 1             # i++
        j       rd_tst                  # Go to the loop test
        
        # Prepare for return
rddone: lw      $ra, 0($sp)             # retrieve return address
        addi    $sp, $sp 4              # adjust stack pointer
        jr      $ra                     # return


        ###############################################################
        # Print_list function
        #    $a0 is the address of the beginning of list (In)
        #    $a1 is n  (In)
pr_lst: 
        # Setup
        addi    $sp, $sp, -4            # Make space for return address
        sw      $ra, 0($sp)             # Save return address

        # Main for loop
        move    $t2, $a0                # Need $a0 for syscall:  so
                                        #    copy to t2
        move    $t0, $zero              # $t0 = i = 0
pr_tst: bge     $t0, $a1, prdone        # If  i = $t0 >= $a1 = n 
                                        #    branch out of loop.
                                        #    Otherwise continue.
        sll     $t1, $t0, 2             # Words are 4 bytes:  use 4*i, not i
        add     $t1, $t2, $t1           # $t1 = list + i
	lw      $a0, 0($t1)             # Put the value to print in $a0
	li	$v0, 1			# Code for print int.
	syscall

        # Print a space 
        la      $a0, space              # 
        li      $v0, 4                  # Code for print string
        syscall

        addi    $t0, $t0, 1             # i++
        j       pr_tst                  # Go to the loop test
        
        # print a newline
prdone: 
        la      $a0, newln
        li      $v0, 4                  # code for print string
        syscall

        # Prepare for return
        lw      $ra, 0($sp)             # retrieve return address
        addi    $sp, $sp 4              # adjust stack pointer
        jr      $ra                     # return

count_sort:
	subu	$sp, $sp 412
	sw	$ra 408($sp)		# store return address
	sw	$s1, 404($sp) 		# store $s1
	sw	$s0, 400($sp)		# store new list 
	
	move	$s0, $sp		# move $s2 = start of new list
	move	$t0, $zero		# t0 = i = 0
	move	$t1, $zero		# t1 = loc = 0
	
loop_test:
	bgt	$t0, $a1, count_done
	sll	$t2, $t0, 2		# shifting bytes over 4
	add	$t2, $a0, $t2		# list = i*4 + list
	sw	$t3, 0($t2)		# storing value of t2 in t3
	
	move	$a2, $t3		# store new list
	move	$a3, $t0		# store i 
	
	addi    $t0, $t0, 1             # i++
	
	jal	find_pos
	
	ble	$t0, $a1, loop_test
	
	move	$s0, $s1
	
	jal 	copy_list
	
count_done: 
	lw	$ra, 808($sp)
	lw	$s0, 804($sp)
	lw	$s1, 800($sp)
	addi	$sp, $sp, 812
	jr	$ra
	
find_pos:
	subu	$sp, $sp 8
	sw	$ra, 4($sp)
	sw	$t4, 0($sp)
	
	move 	$t0, $zero		# j = 0 = val
	
pos_test:
	bgt	$t0, $a0, pos_done	# if j > n break out
	sll	$t2, $t0, 2		# shifting bytes over 4
	add	$t2, $a0, $t2		# list = i*4 + list
	sw	$t3, 0($t2)		# storing value of t2 in t3
	
	bge	$t3, $t0, pos_done
	
	addi	$t4, $t4, 1
	
	bne	$t3, $t0, pos_done
	
	blt 	$a3, $t0, pos_done
	
	addi	$t4, $t4, 1
	
	lw      $ra, 808($sp)             # retrieve return address
        addi    $sp, $sp 812              # adjust stack pointer
        jr      $ra                     # return
	
	
	
	
	
	