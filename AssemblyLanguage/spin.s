# File:           spin.s
# Purpose:        Implement a spinlock using x86 assembly
# Functions:      Acquire (the lock) and Relinquish (the lock)
        
        .section .data
lock:   .quad  0  # unlocked = 0, 1 = locked

#######################################################################
# Function:       Acquire
# Purpose:        Acquire the lock
# C Prototype:    void Acquire(void)
#
# Args:           none
# Global in/out:  lock

        .section .text

        .global Acquire
Acquire:
        push    %rbp            # The usual setup: push old base ptr
        mov     %rsp, %rbp      # Set new base ptr to stack ptr

busy_wait:
        mov     $1, %r8         # Set r8 to 1 (locked)
        xchg    %r8, lock       # Atomically exchange contents of lock 
                                #   (in memory) and contents of r8
        cmp     $0, %r8         # If we succeeded, r8 = 0
        jne     busy_wait       # If we failed, r8 = 1.  Try again

        leave                   # The usual cleanup: set stack ptr
                                # to base ptr, pop old base ptr
        ret                     # Pop ret addr and jump to it


#######################################################################
# Function:       Relinquish
# Purpose:        Relinquish the lock
# C Prototype:    void Relinquish(void)
#
# Args:           none
# Global in/out:  lock
        .global Relinquish
Relinquish:
        push    %rbp            # The usual setup: push old base ptr
        mov     %rsp, %rbp      # Set new base ptr to stack ptr

        mov     $0, %r8         # Set r8 to 0 (unlocked)
        xchg    %r8, lock       # Atomically exchange contents of lock
                                #   and contents of r8, setting lock
                                #   to 0 (unlocked)

        leave                   # The usual cleanup: set stack ptr
                                # to base ptr, pop old base ptr
        ret                     # Pop ret addr and jump to it
