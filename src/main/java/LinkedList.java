public class LinkedList {
    Node head;

    public LinkedList() {

    }

    //Voeg een node toe aan de voorkant van de lijst
    public void addHead(Coin c) {
        Node newNode = new Node(c);
        newNode.next = head;
        head = newNode;

    }

    //Voeg een node toe aan het einde van de lijst
    public void addTail(Coin c) {
        if (head == null) {
            head = new Node(c);
        } else {

            Node newNode = new Node(c);
            Node current = head;
            while (current.next != null) {
                current = current.next;
            }
            current.next = newNode;

        }
    }

    public int size() {
        int count = 0;
        Node current = head;
        while (current != null) {
            current = current.next;
            count++;
        }
        return count;
    }

    //Print de lijst
    public void printList(boolean isLeft) {
        System.out.println(createFormattedOutput(isLeft, head));
    }

    public String createFormattedOutput(boolean isLeft, Node n) {
        String output = "";
        Node current = head;
        int count = 0;
        while (current != null) {
            output += current.c.toString() + "\n";
            current = current.next;
            count++;
        }
        String leftOrRight = isLeft ? "Left" : "Right\n";
        String headElement = "Head element is: " + giveHeadElement().c.toString() + "\n";
        String tailElement = "Tail element is: " + giveTailElement().c.toString() + "\n";
        return leftOrRight + headElement + tailElement + count + " elements in list:\n" + output;
    }


    //Return sorted list
    public void sortList() {
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

    public Coin giveHeadElementAndRemove() {
        Node temp = head;
        if (head == null) {
            return null;
        }
        head = head.next;

        return temp.c;

    }

    public Coin giveTailElementAndRemove() {
        //elements contains only one element
        if (head != null && head.next == null) {
            Node temp = head;
            head = null;

            return temp.c;
        }
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

    public Node giveTailElement() {
        if (head == null) {
            return null;
        }
        Node temp = head;
        if (temp.next != null) {
            while (temp.next != null) {
                temp = temp.next;
            }
        }
        return temp;
    }

    public Node giveHeadElement() {
        return head;
    }

    public void insert(Coin newCoin) {
        Node newNode = new Node(newCoin);
        Node current;

        if (head == null || head.c.compareTo(newCoin) > 0) {
            newNode.next = head;
            head = newNode;
        } else {
            current = head;
            while (current.next != null && current.next.c.compareTo(newCoin) < 0) {
                current = current.next;
            }
            newNode.next = current.next;
            current.next = newNode;
        }

    }

}
