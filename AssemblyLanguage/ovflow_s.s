Overflow:

push    %rbp            # The usual setup: push old base ptr
mov     %rsp, %rbp      # Set new base ptr to stack ptr
add     %rdi, %rsi
jo      ovflow
mov     $0, %rax
jmp     done

ovflow: mov     $1, %rax


done:   leave           # The usual cleanup: set stack ptr
                        # to base ptr, pop old base ptr
ret                     # Pop ret addr and jump to it