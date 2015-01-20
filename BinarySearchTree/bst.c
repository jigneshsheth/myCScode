/* File: bst.c
 * Author : Paramvir "Paul" Hundal
 * Date: October 2, 2013
 *
 * Purpose: Implement a binary search tree with ops insert, print tree,
 * print sort, member, delete, free_list.
 * 
 *
 * Input:   Single character lower case letters to indicate operators, 
 *          followed by arguments needed by operators.
 *           
 *
 * Output:  Results of operations.
 *
 * Compile: gcc -g -Wall -o bst bst.c
 * Run: ./bst
 *
 * 
 *     
 */

#include <stdio.h>
#include <stdlib.h>
#include <stdlib.h>
#include <string.h>

const int MAX_INDENT = 80;

struct tree_node_s {
    int key;
    struct tree_node_s* lc;
    struct tree_node_s* rc;
};

int Member(struct tree_node_s* root_p, int val);
struct tree_node_s* Insert(struct tree_node_s* root_p, int val);
struct tree_node_s* Delete(struct tree_node_s* root_p, int val);
struct tree_node_s* findMin(struct tree_node_s* nodeToDelete);
struct tree_node_s* Free_tree(struct tree_node_s* root_p);

typedef struct tree_node_s tree_node_t; 
void Print_tree(tree_node_t* curr_p, char indent[]);
void Print_tree_wrapper(tree_node_t* root_p);
char Get_command(void);
int Get_value(void);
void Print_sort(tree_node_t* curr_p);

int main(void){
    char command;
    int value;
    struct tree_node_s* root_p = NULL;
    
    command = Get_command();
    while (command != 'q' && command != 'Q') {
        switch (command) {
            case 'i':
            case 'I':
                value = Get_value();
                root_p = Insert(root_p, value);
                break;
            case 's':
            case 'S':
                Print_sort(root_p);
                break;
            case 'm':
            case 'M':
                value = Get_value();
                if (Member(root_p, value))
                    printf("%d is in the list\n", value);
                else
                    printf("%d is not in the list\n", value);
                break;
            case 'd':
            case 'D':
                value = Get_value();
                root_p = Delete(root_p, value);
                break;
            case 'f':
            case 'F':
                root_p = Free_tree(root_p);
                break;
            case 'q':
            case 'Q':
                exit(0);
                break;
            case 'p':
            case 'P':
                Print_tree_wrapper(root_p);
                break;
            default:
                printf("There is no %c command\n", command);
                printf("Please try again\n");
        }
        command = Get_command();
    }
    
    root_p = Free_tree(root_p);
    
    return 0;
}  /* main */


/*-------------------------------------------------------------------
 * Function:  Print_sort
 * Purpose:   Print a sorted list of keys currently in the tree.
 *
 *
 * In args:   curr_p:   pointer to the current node.
 */
void Print_sort(tree_node_t* curr_p){
    if(curr_p != NULL){
        Print_sort(curr_p -> lc);
        printf("%d ", curr_p->key);
        Print_sort(curr_p->rc);
    }
}

/* Print_sort */

/*---------------------------------------------------------------------
 * Function:  Print_tree_wrapper
 * Purpose:   Set up for call to Print_tree function by creating
 *              storage for a string that will be used to control
 *              indentation.
 * In arg:    root_p:  pointer to the root of the tree.
 */
void Print_tree_wrapper(tree_node_t* root_p) {
    char indent[MAX_INDENT];
    
    indent[0] = '\0';
    Print_tree(root_p, indent);
}  /* Print_tree_wrapper */


/*---------------------------------------------------------------------
 * Function:    Print_tree
 * Purpose:     Print the keys in the tree showing the structure of
 *                 the tree.  (Preorder traversal)
 * In args:     curr_p:  pointer to the current node
 * In/out arg:  indent:  array of char specifying the indentation
 *              for the node.
 *
 * Note:
 * Each new level of the tree is indented 3 spaces to the right
 *    on the screen
 */
void Print_tree(tree_node_t* curr_p, char indent[]) {
    
    if (curr_p != NULL) {
        printf("%s %d\n", indent, curr_p->key);
        strcat(indent, "   ");
        Print_tree(curr_p->lc, indent);
        Print_tree(curr_p->rc, indent);
        indent[strlen(indent) - 3] = '\0';
    }
}  /* Print_tree */

/*----------------------------------------------------------------------
* Function: Member
* Purpose: Determine wheater a user-specifed key is in the tree. If it is 
* the function should return true (nonzero). Otherwise it should return
* false(zero).
*
* Input args: curr_p:   pointer to the current node we want to check
*             val:      the key to the current node
*
* Output args: found:   boolean to check if member is in tree or not.
*               val:     the value of the member.
*
* Return val: the value of the key
*
*/

int Member(struct tree_node_s* curr_p, int val){
    
    int found = 0;
    
    if(curr_p == NULL){
        return 0;
    }
        
    if(curr_p->key == val){
        return val;
    }
    found = Member(curr_p->lc, val);
    if(found != 0){
        return found;
    }
    found = Member(curr_p->rc, val);
    if(found != 0){
        return found;
    }
    return 0;
}

/*-----------------------------------------------------------------------
* Function: Insert
* Purpose: Insert into the tree a new node with a user-specified key. 
*          If the key is already in the tree, the function should print
*          a message and return.
*
*
* Input args: root_p: the root of the tree
              val: the key to insert
* 
* Return val: Pointer to the current node of the tree.
*/

struct tree_node_s* Insert(struct tree_node_s* root_p, int val){
    
    struct tree_node_s* curr_p = root_p;
    
    if(curr_p == NULL){
        curr_p = malloc(sizeof(struct tree_node_s));
        curr_p->key = val;
        curr_p->lc = curr_p->rc = NULL;
        
    } else if(val< curr_p->key){
        curr_p->lc = Insert(curr_p->lc, val);
    } else if(val > curr_p->key){
        curr_p->rc = Insert(curr_p->rc, val);
    } else
        printf("%d is already in the tree.", val);

    return curr_p;
    
}

/*---------------------------------------------------------------------------
* Function: Delete
* Purpose:  Delete from the tree the node with a user-specified key. 
*           If the key is already in the tree, the function should print
*           a message and return.
*
* Input args:   curr_p: the current node you are on.
*               val: the key to the node.
*
*
* Return val:   a null pointer
*/

struct tree_node_s* Delete(struct tree_node_s* curr_p, int val){
    
    struct tree_node_s* temp;
    
    if(curr_p==NULL){
        printf("\nElement was not found\n");
    } else {
        
        if(val < curr_p->key){
            curr_p->lc = Delete(curr_p->lc, val);
    }   else if(val > curr_p->key){
            curr_p->rc = Delete(curr_p->rc, val);
    }   else {
        
            if(curr_p->lc && curr_p->rc){
                temp = findMin(curr_p->rc);
                curr_p->key = temp->key;
                curr_p->rc = Delete(curr_p->rc, curr_p->key);
    }       else if(curr_p->lc == NULL){
                temp = curr_p;
                curr_p = curr_p->rc;
                free(temp);
    }       else{
                temp = curr_p;
                curr_p = curr_p->lc;
                free(temp);
            }
        }
    }
    return curr_p;
    
} /*Delete*/


/*----------------------------------------------------------------------
* Function: findMin
* Purpose: to find the min value of the left most child
*
* Input/Output args:    curr_p : the node you want to recruse left on.
*
* Return val:           a pointer to min node.
*/
struct tree_node_s* findMin(struct tree_node_s* curr_p){
    
    if(curr_p->lc == NULL){
        return curr_p;
    }
    return findMin(curr_p->lc);
} /*findMin*/

/*---------------------------------------------------------------------------
* Function: Free Tree
* Purpose: To free the tree by deallocating the memory
* Input args: root_p:
*               
* Return val: a NULL pointer
* Note: root_p will be NULL on completion, indicating tree is NULL
*/

struct tree_node_s* Free_tree(struct tree_node_s* root_p){
    
    struct tree_node_s* curr_p = root_p;
    
    if(curr_p != NULL){
        curr_p->lc = Free_tree(curr_p->lc);
        curr_p->rc = Free_tree(curr_p->rc);
        free(curr_p);
    }
    return NULL;
    } /*Free_tree*/

/*-----------------------------------------------------------------
 * Function:      Get_command
 * Purpose:       Get a single character command from stdin
 * Return value:  the first non-whitespace character from stdin
 */
char Get_command(void) {
    char c;
    
    printf("Please enter a command (i, s, p, m, d, f, q):  ");
    /* Put the space before the %c so scanf will skip white space */
    scanf(" %c", &c);
    return c;
}  /* Get_command */

/*-----------------------------------------------------------------
 * Function:   Get_value
 * Purpose:    Get an int from stdin
 * Return value:  the next int in stdin
 * Note:       Behavior unpredictable if an int isn't entered
 */
int  Get_value(void) {
    int val;
    
    printf("Please enter a value:  ");
    scanf("%d", &val);
    return val;
}  /* Get_value */
