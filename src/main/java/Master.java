import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Scanner;

public class Master {

    private List<Worker> workers;


    private static String MERGER_TOPIC;
    public static void main(String[] args) throws IOException, InterruptedException {
        Master master = new Master();
    }

    public Master() throws IOException, InterruptedException {
        Scanner input = new Scanner(System.in);
        System.out.print("Hoeveel nodes zullen we gebruiken voor deze operatie?");
        int amountOfNodes = input.nextInt();
        createOfficer(amountOfNodes); //TODO method could be void
        Process[] processes = createWorkers(amountOfNodes);
        for(Process p : processes){
            p.waitFor(); //Wacht totdat alle workers hun eigen data gegenereert en gesorteerd hebben.
        }

        //De officer kan nu de hoogste, laagste en pivots opvragen aan de nodes.
        //De workers hebben nu hun eigen data gesorteerd en deze gesorteerde lijst lokaal ter beschikking; private List<Coin> sortedElements;



    }

    public Process[] createMergers(int amountOfNodes) throws IOException {
        Process[] processes = new Process[amountOfNodes];

        String javaBin = System.getProperty("java.home") + File.separator + "bin" + File.separator + "java";
        String classPath = System.getProperty("java.class.path");

        //Maak nieuwe workers.
        for (int i = 0; i < amountOfNodes; i++){
            if(i%2 == 0) {
                ProcessBuilder child = new ProcessBuilder(
                        javaBin, "-classpath", classPath, JMSMerger.class.getCanonicalName(),
                        "--mergerId", String.valueOf((i/2)+1),
                        "--numClients", String.valueOf(amountOfNodes)


                );
                ///Deze workers zullen beginnen met getallen genereren en sorteren wanner .start() aangeroepen wordt
                processes[i] = child.inheritIO().start();
            }

        }
        return processes;
    }

    public Process[] createWorkers(int amountOfNodes) throws IOException {
        Process[] processes = new Process[amountOfNodes];

        String javaBin = System.getProperty("java.home") + File.separator + "bin" + File.separator + "java";
        String classPath = System.getProperty("java.class.path");

        //Maak nieuwe workers.
        for (int i = 0; i < amountOfNodes; i++){

        ProcessBuilder child = new ProcessBuilder(
                javaBin, "-classpath", classPath, Worker.class.getCanonicalName(),
                "--workerId", String.valueOf(i+1),
                "--numClients", String.valueOf(amountOfNodes)


        );
            ///Deze workers zullen beginnen met getallen genereren en sorteren wanner .start() aangeroepen wordt
            processes[i] = child.inheritIO().start();


        }
        return processes;
    }

    public Process createOfficer(int amountOfNodes) throws IOException {


        String javaBin = System.getProperty("java.home") + File.separator + "bin" + File.separator + "java";
        String classPath = System.getProperty("java.class.path");
        ProcessBuilder child = new ProcessBuilder(

                    javaBin, "-classpath", classPath, Officer.class.getCanonicalName(),  "--numClients", String.valueOf(amountOfNodes));
       return child.inheritIO().start();

    }
}
