public class Node {
    Coin c;
    Node prev, next;

    Node(Coin c){
        this.c = c;
        next = null;
        prev = null;
    }

    public void print(Node n){
        Node temp = n;
        System.out.println("In print method");
        while(n != null){
            System.out.println("Node: " + n.c.toString());
            temp = n;
            n = n.next;
        }
        while(temp != null){
            System.out.println("Temp: " + temp.c.toString());
            System.out.println("temp.prev = " + temp.prev);
            temp = temp.prev;
            System.out.println("Temp is now temp .prev:  temp is: " + temp.c.toString());
        }
    }
}
