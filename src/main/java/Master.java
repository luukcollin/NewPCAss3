import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Scanner;

public class Master {
    public static void main(String[] args) throws IOException, InterruptedException {
        Scanner input = new Scanner(System.in);
        System.out.print("Hoeveel nodes zullen we gebruiken voor deze operatie?");
        int amountOfNodes = input.nextInt();
        createOfficer(amountOfNodes); //TODO method could be void
        Process[] processes = createWorkers(amountOfNodes);
        for(Process p : processes) {
            p.waitFor(); //Wacht totdat alle workers hun eigen data gegenereert en gesorteerd hebben.
        }
    }


    public static Process[] createWorkers(int amountOfNodes) throws IOException {
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

    //Er is maar één officier. Deze staat in verbinding met alle workers en geef orders aan hen.
    public static Process createOfficer(int amountOfNodes) throws IOException {
        String javaBin = System.getProperty("java.home") + File.separator + "bin" + File.separator + "java";
        String classPath = System.getProperty("java.class.path");
        ProcessBuilder child = new ProcessBuilder(
                    javaBin, "-classpath", classPath, Officer.class.getCanonicalName(),  "--numClients", String.valueOf(amountOfNodes));
       return child.inheritIO().start();
    }
}
