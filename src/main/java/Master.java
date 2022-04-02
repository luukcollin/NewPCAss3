import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Scanner;

public class Master {

    private List<Worker> workers;
    private Process[] processes;

    private static String MERGER_TOPIC;
    public Master() throws IOException, InterruptedException {
        Scanner input = new Scanner(System.in);
        System.out.print("Hoeveel nodes zullen we gebruiken voor deze operatie?");
        int amountOfNodes = input.nextInt();
        processes = createWorkers(amountOfNodes);
        for(Process p : processes){
            p.waitFor(); //Wacht totdat alle workers hun eigen data gegenereert en gesorteerd hebben.
        }
        //De officer kan nu de hoogste, laagste en pivots opvragen aan de nodes.
        //De workers hebben nu hun eigen data gesorteerd en deze gesorteerde lijst lokaal ter beschikking; private List<Coin> sortedElements;


        new Officer("HetMergerTopic").generateMergerQueues();
    }

    public Process[] createWorkers(int amountOfNodes) throws IOException {
        Process[] processes = new Process[amountOfNodes];

        String javaBin = System.getProperty("java.home") + File.separator + "bin" + File.separator + "java";
        String classPath = System.getProperty("java.class.path");

        //Maak nieuwe workers.
        for (int i = 0; i < amountOfNodes; i++){

        ProcessBuilder child = new ProcessBuilder(
                javaBin, "-classpath", classPath, Worker.class.getCanonicalName(),
                "--nodeId", String.valueOf(i+1),
                "--numClients", String.valueOf(amountOfNodes)

        );
            ///Deze workers zullen beginnen met getallen genereren en sorteren wanner .start() aangeroepen wordt
            processes[i] = child.inheritIO().start();

        }
        return processes;
    }
}
