import java.util.ArrayList;
import java.util.Random;

public class Main {

    public static final int MAINMEM = 1750;
    // creating the random number generator
    public static Random randomizer = new Random();
    // MAX cpu cycles possible is 6000 VTUs
    public static final int MAX_VTUS = 6000;

    //Array of possible memory sizes, will use the randomizer to choose an index/value from this array to serve as the random process' size
    public static final int[] PROCESS_MEMORY_SIZES = {50, 60, 70, 80, 90, 100, 110, 120, 130, 140, 150, 160, 170, 180, 190,
            200, 210, 220, 230, 240, 250, 260, 270, 280, 290, 300};
    //Array of possible process execution times, will use the randomizer to choose an index/value from this array to serve as the random process' execution time in VTUs
    public static final int[] PROCESS_EXECUTION_TIMES = {5, 10, 15, 20, 25, 30, 35, 40, 45, 50, 55, 60};

    public static int currentVtu;
    protected ArrayList<Sim.Process> processes;
    public static int rejectedProcesses, finishedProcesses;


    public static ArrayList<ArrayList<Integer>> mem = new ArrayList<ArrayList<Integer>>();
    public static ArrayList<Integer> processId = new ArrayList<>();

    public static final char FREE_MEMORY = '.';
    public static final char RESERVED_MEMORY = '#';
    public static char[] main_memory = new char[MAINMEM];

    /**
     * Print the current contents of memory
     */
    public static void printMemory() {
        System.out.print("Memory at time " + currentVtu + ":");
        for (int i = 0; i < main_memory.length; i++) {
            if (i % 500 == 0) {
                System.out.println("");
            }
            System.out.print( main_memory[i] + "" );
        }
        System.out.println("");
    }

    public static void main(String[] args) {

      
        initializeMainMemory(main_memory);


        ArrayList<Sim.Process> processList = new ArrayList<Sim.Process>();
        ArrayList<Sim.Process> readyQueue = new ArrayList<Sim.Process>();
        ArrayList<Sim.Process> running = new ArrayList<>();
        ArrayList<Sim.Process> finished = new ArrayList<>();

        int timeWhenProcessArrived = 0, rejectedProcesses = 0;


        int vtu = 0;
        int processSize, processTime, processSizeIndex, processTimeIndex, processArrival;

        while (vtu < 4000) {  // could move into main loop and do each loop

            processSizeIndex = randomizer.nextInt(PROCESS_MEMORY_SIZES.length);
            processSize = PROCESS_MEMORY_SIZES[processSizeIndex];

            processTimeIndex = randomizer.nextInt(PROCESS_EXECUTION_TIMES.length);
            processTime = PROCESS_EXECUTION_TIMES[processTimeIndex];
            processArrival = 1 + (int) (Math.random() * ((10 - 1) + 1));

            processList.add(new Sim.Process(vtu, processSize, processTime, processArrival, 0, 0));

            vtu++;
        }
//******************************************************** END PROCESS CREATION ************************************************************


        int readySize, toBeRemoved;


        while (currentVtu < MAX_VTUS) {

            // loading up the ready queue
            if (processList.get(0).getArrivalDelay() == currentVtu - timeWhenProcessArrived) {
                Sim.Process temp = processList.get(0);
                readyQueue.add(temp);
                readyQueue.get(readyQueue.size() - 1).start = currentVtu;
                readyQueue.get(readyQueue.size() - 1).end = currentVtu + readyQueue.get(readyQueue.size() - 1).processTime;
                timeWhenProcessArrived = currentVtu;
                processList.remove(0);
            }


            //Processes exit the system
            ArrayList<Sim.Process> toRemove = new ArrayList<Sim.Process>();
            for (Sim.Process p : readyQueue) {
                if (p.getEnd() == currentVtu) {
                    Sim.removeFromMemory(p);
                    toRemove.add(p);
                }
            }
            for (Sim.Process p : toRemove) {
                readyQueue.remove(p);
            }

            //Processes enter the system
            for (Sim.Process p : readyQueue) {
                if (p.getStart() == currentVtu) {
                    Sim.putInMemory(p);
                }
            }

            printMemory();

            currentVtu++;
        }
    }

    private static void initializeMainMemory(char[] main_memory) {

        for (int i = 1750; i < main_memory.length; i++) {
            main_memory[i] = FREE_MEMORY;
        }
    }
}




