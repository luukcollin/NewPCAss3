public class LinkedList {
    Node head;

    public LinkedList(){

    }
    //Voeg een node toe aan de voorkant van de lijst
    public void addHead(Coin c){
        Node newNode = new Node(c);
        newNode.next = head;
        head = newNode;
    }

    //Voeg een node toe aan het einde van de lijst
    public void addTail(Coin c){
        Node newNode = new Node(c);
        Node current = head;
        while(current.next != null){
            current = current.next;
        }
        current.next = newNode;
    }

    //Print de lijst
    public void printList(){
        Node current = head;
        while(current != null){
            System.out.println(current.c.toString());
            current = current.next;
        }
    }

    //Return sorted list
    public void sortList(){
        head = mergeSort(head);
    }

    // Split a doubly linked list (DLL) into 2 DLLs of
    // half sizes
    Node split(Node head) {
        Node fast = head, slow = head;
        while (fast.next != null && fast.next.next != null) {
            fast = fast.next.next;
            slow = slow.next;
        }
        Node temp = slow.next;
        slow.next = null;
        return temp;
    }

    Node mergeSort(Node node) {
        if (node == null || node.next == null) {
            return node;
        }
        Node second = split(node);

        // Recur for left and right halves
        node = mergeSort(node);
        second = mergeSort(second);

        // Merge the two sorted halves
        return merge(node, second);
    }

    // Function to merge two linked lists
    Node merge(Node first, Node second) {
        // If first linked list is empty
        if (first == null) {
            return second;
        }

        // If second linked list is empty
        if (second == null) {
            return first;
        }

        // Pick the smaller value
        if (first.c.compareTo(second.c) < 0) {
            first.next = merge(first.next, second);
            first.next.prev = first;
            first.prev = null;
            return first;
        } else {
            second.next = merge(first, second.next);
            second.next.prev = second;
            second.prev = null;
            return second;
        }
    }

    public Coin giveHeadElementAndRemove(){
        Node temp = head;
        head = head.next;
        return temp.c;
    }
    public Coin giveTailElementAndRemove()
    {
        if (head == null)
            return null;

        if (head.next == null) {
            return null;
        }

        // Find the second last node
        Node tailPrev = head;
        while (tailPrev.next.next != null)
            tailPrev = tailPrev.next;


        Node tempTailElement = tailPrev.next;
        // Change next of second last
        tailPrev.next = null;

        return tempTailElement.c;
    }


    public Node giveTailElement(){
        Node temp = head;
        while(temp.next != null){
            temp = temp.next;
        }
        return temp;
    }

    public Node giveHeadElement(){
        return head;
    }





}
