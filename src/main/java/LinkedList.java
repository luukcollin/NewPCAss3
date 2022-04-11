public class LinkedList {
    Node head;

    public LinkedList(){

    }
    //Voeg een node toe aan de voorkant van de lijst
    public void addHead(Coin c){
        Node newNode = new Node(c);
        newNode.next = head;
        head = newNode;
        System.out.println("Inserted node at start of list");
    }

    //Voeg een node toe aan het einde van de lijst
    public void addTail(Coin c){
        Node newNode = new Node(c);
        Node current = head;
        while(current.next != null){
            current = current.next;
        }
        current.next = newNode;
        System.out.println("Inserted at back of list");
    }

    //Print de lijst
    public void printList(){
        Node current = head;
        while(current != null){
            System.out.println(current.c.toString());
            current = current.next;
        }
    }
}
