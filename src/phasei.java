
import java.util.*;
public class phasei {

    // creating the random number generator
    public static Random randomizer = new Random();
    // MAX cpu cycles possible is 6000 VTUs
    public static final int MAX_VTUS = 6000;
    public static final int MEM_SIZE = 1750;

    //Array of possible memory sizes, will use the randomizer to choose an index/value from this array to serve as the random process' size
    public static final int[] PROCESS_MEMORY_SIZES = {50, 60, 70, 80, 90, 100, 110, 120, 130, 140, 150, 160, 170, 180, 190,
            200, 210, 220, 230, 240, 250, 260, 270, 280, 290, 300};

    //Array of possible process execution times, will use the randomizer to choose an index/value from this array to serve as the random process' execution time in VTUs
    public static final int[] PROCESS_EXECUTION_TIMES = {5, 10, 15, 20, 25, 30, 35, 40, 45, 50, 55, 60};

    public static ArrayList<Process> processList = new ArrayList<Process>();
    public static ArrayList<Process> readyQueue = new ArrayList<Process>();
    public static ArrayList<Process> running = new ArrayList<>();
    public static ArrayList<Process> finished = new ArrayList<>();

    public static int nchecks, currentVtu, occupiedBlocks;
    public static int finishedProcesses;

    public static int externalFrag = 0;

    // variables used for calculating the required statistics
    public static double averageOccupied = 0, averageMemoryHoleSize = 0, averageTurnaroundTime = 0,
            averageWaitingTime = 0, averageProcessingTime = 0;

    // time when a new process has completed its wait time and is ready for allocation
    public static int timeWhenProcessArrived = 0;
    // tracking the number of total rejected processes, is cumulative throughout the 6000 cycles
    public static int rejectedProcesses = 0, holesInMemory = 1;
    // used in calculating the average stats
    public static int avgWait = 0, avgTurn = 0, avgProcess = 0;

//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    static class Process{

    int pid, processSize, processTime, arrivalDelay, start, end;

        public Process(int pid, int processSize, int processTime, int arrivalDelay,
                       int start, int end){

            this.pid = pid;
            this.processSize = processSize;
            this.processTime = processTime;
            this.arrivalDelay = arrivalDelay;
            this.start = start;
            this.end = end;


        } // end Process method

        public int getPid() {
            return pid;
        }

        public int getProcessSize() {
            return processSize;
        }

        public int getProcessTime() {
            return processTime;
        }
        public int getArrivalDelay(){
            return arrivalDelay;
        }

        public int getStart() {
            return start;
        }

        public int getEnd() {
            return end;
        }

    } // end class process

//********************************************************************************************************************************************

        /**
         * Class MemoryBlock keeps track of a block of pages.  It may represent
         * either a hole or a partition.
         */
        static class MemBlock {
            private boolean hole;
            private int size;

            public MemBlock(int s, boolean h) { //mem block size should be the process size
                size = s;
                hole = h;
            }

            public boolean isHole() {
                return hole;
            }

            public boolean isPart() {
                return !hole;
            }

            public int getSize() {
                return size;
            }

            public void setSize(int s) {
                size = s;
            }

            public void makeHole() {
                hole = true;
            }

            public void makePart() {
                hole = false;
            }
        }

        /**
         * insertPart() inserts a partition into a hole.  If the hole is not large
         * enough, it will do nothing and return false.  If it is exactly the
         * right size, it will simply modify the partition in place.  If too big,
         * it will decrease the size of the hole and create a new block for the
         * partition.
         * <p>
         * R/O size:    Size of partition to create
         * R/W blocks:  Entire list of blocks
         * R/W loc:    Original hole location
         * Return value:  T/F was partition inserted?
         *
         */
        public static boolean insertPart(int size, ArrayList<MemBlock> blocks, // size is process size, blocks are collections
                                         int loc) {                     // of memory blocks, which are collections of pages
            MemBlock b = blocks.get(loc);                             // pages = like 1 for each hk size, so 50kb = mem block of 50 pages
            int newHoleLoc, holeSize;

            if (b.getSize() == size) {
                // partition fits exactly
                b.makePart();

            } else {
                // extra hole left over
                //////////////////////////////
                holeSize = b.getSize() - size;
                b.setSize(size);
                b.makePart();
                //b.makePart();
                //blocks.insertElementAt(new MemoryBlock(size,false), loc); //changed from vector
                MemBlock c = new MemBlock(holeSize, true);
                blocks.add(loc + 1, c);     // replacing original block
            }

            return true;
        } // end of insertPart()

        /**
         * removePart() removes a partition.  If there is a hole on either side,
         * it merges all the space into one big hole.
         * <p>
         * R/W blocks:  Entire list of blocks
         * R/W loc:    Original partition location
         *
         * @returns new location (after consolidating holes)
         */
        public static int removePart(ArrayList<MemBlock> blocks, int loc) { // loc is an index value indicator for vector location
            MemBlock b = blocks.get(loc);

            b.makeHole();

            if (loc > 0) {
                MemBlock bPrev = blocks.get(loc - 1);

                if (bPrev.isHole()) {
                    b.setSize(b.getSize() + bPrev.getSize());
                    blocks.remove(loc - 1);
                    loc--;
                }
            }
            if (loc < blocks.size() - 1) {
                MemBlock bNext = blocks.get(loc + 1);

                if (bNext.isHole()) {
                    b.setSize(b.getSize() + bNext.getSize());
                    blocks.remove(loc + 1);

                }
            }
            return loc;
        } // end of removePart()


    public static void printStats(int b, ArrayList<MemBlock> blocks){

        if(b == 5000){
            for (int r = 0; r < finished.size(); r++) { // for loop calculating average wait, turnaround, and process times
                avgWait += finished.get(r).getArrivalDelay();
                avgProcess += finished.get(r).getProcessTime();
                avgTurn += (finished.get(r).getArrivalDelay() + finished.get(r).getProcessTime());
            }
            averageProcessingTime = avgProcess / (finished.size());
            averageTurnaroundTime = avgTurn / (finished.size());
            averageWaitingTime = avgWait / (finished.size());
        }

//********************************************************** IF statements for printing STATS ************************************************************
        // the necessary code to calculate the number of occupied blocks and their average sizes
        if (b % 200 == 0 && b > 0) {
            // logic for computing stats concerning free and occpied space in memory
            int ff = 0, partitions = 0;
            for(int u = 0; u < blocks.size(); u++){
                if(blocks.get(u).isPart()) {
                    ff += blocks.get(u).getSize();                 // calculating AVG size of occupied blocks
                    partitions++;
                }
            }
            if(partitions > 0) {
                averageOccupied = (ff / (partitions));
            }
            System.out.println("\nCurrent VTU cycle is: " + b);
            System.out.println("\nAverage size of occupied blocks: " + averageOccupied);
            System.out.println("\nNumber of occupied blocks: " + partitions + "\n");
        }
        // the necessary code to calculate the number of free blocks in memory and their sizes
        if (b % 300 == 0 && b > 0) {
            int hh = 0, holes = 0;
            for(int q = 0; q < blocks.size(); q++){ // calculating AVG size of the holes in MEMORY
                if(blocks.get(q).isHole()){
                    hh += blocks.get(q).getSize();
                    holes++;
                }
            }
            if(holes > 0) {
                averageMemoryHoleSize = (hh / (holes));
            }
            System.out.println("\nCurrent VTU cycle is: " + b);
            System.out.println("\nAverage size of memory holes: " + averageMemoryHoleSize);
            System.out.println("\nNumber of holes in memory: " + holes + "\n");
        }

        // average turnaround, waiting, and processing times print out
        if (b == 5000) {
            System.out.println("\nCurrent VTU cycle is: " + b);
            System.out.println("\nAverage Turnaround time for all processes: " + averageTurnaroundTime);

            System.out.println("\nAverage waiting time for all processes: " + averageWaitingTime);
            System.out.println("\nAverage processing time for all processes: " + averageProcessingTime + "\n");
        }
        // rejected jobs between 1000-5000 VTUs -- cumulative, adds them up and shows total every 1000 VTUs, not just the number between that specific range
        if (b == 2000) {
            System.out.println("\nCurrent VTU cycle is: " + b);
            System.out.println("\nNumber of total rejected jobs between 1000-2000 VTUs: " + finished.size() + "\n");
        }
        if (b == 3000) {
            System.out.println("\nCurrent VTU cycle is: " + b);
            System.out.println("\nNumber of total rejected jobs between 2000-3000 VTUs: " + finished.size() + "\n");
        }
        if (b == 4000) {
            System.out.println("\nCurrent VTU cycle is: " + b);
            System.out.println("\nNumber of total rejected jobs between 3000-4000 VTUs: " + finished.size() + "\n");
        }
        if (b == 5000) {
            System.out.println("\nCurrent VTU cycle is: " + b);
            System.out.println("\nNumber of total rejected jobs between 4000-5000 VTUs: " + finished.size() + "\n");
        }

    } // end printStats method

        /**
         * worstFit() returns the biggest hole it can find.
         * If a big enough one does not exist, it evicts partitions until
         * one does.
         * <p>
         * R/O size:    Size of partition to create
         * R/W blocks:  Entire list of blocks
         * GLOBAL:  nchecks stores number of blocks examined
         */
        public static int worstFit(int size, ArrayList<MemBlock> blocks) {
            MemBlock b;
            int loc = 999;
            int maxSize = 0;

            // search current holes for biggest
            for (int i = 0; i < blocks.size(); i++) {
                b = blocks.get(i);
                if (b.isHole()) {
                    nchecks++;
                    if (b.getSize() >= size && b.getSize() > maxSize) {
                        maxSize = b.getSize();
                        loc = i;
                    }
                }
            }
            return loc;
        } // end of worstFit()

    /**
     * worstFit() returns the biggest hole it can find.
     * If a big enough one does not exist, it evicts partitions until
     * one does.
     * <p>
     * R/O size:    Size of partition to create
     * R/W blocks:  Entire list of blocks
     * GLOBAL:  nchecks stores number of blocks examined
     */
    public static int firstFit(int size, ArrayList<MemBlock> blocks) {
        MemBlock b;
        int loc = 999;

        // search current holes for the first one that's big enough
        for (int i = 0; i < blocks.size(); i++) {
            b = blocks.get(i);
            if (b.isHole()) {
                nchecks++;

                if (b.getSize() >= size) {
                    loc = i;
                    break;
                }
            }
        }
        return loc;
    } // end of firstFit()

    /**
     * worstFit() returns the biggest hole it can find.
     * If a big enough one does not exist, it evicts partitions until
     * one does.
     * <p>
     * R/O size:    Size of partition to create
     * R/W blocks:  Entire list of blocks
     * GLOBAL:  nchecks stores number of blocks examined
     */
    public static int bestFit(int size, ArrayList<MemBlock> blocks) {
        MemBlock b;
        int loc = 999;
        int best = -1;
        int smallest = -1;
        int inc = 0;

        // search current holes for biggest
        for (int i = 0; i < blocks.size(); i++) {
            b = blocks.get(i);
            if (b.isHole()) {
                nchecks++;

                if (b.getSize() >= size && (smallest == -1 || b.getSize() < smallest)) {

                    loc = i;
                    smallest = b.getSize();
                }
            }
        } // end for loop searching through blocks of MemBlocks

        return loc;
    } // end of bestFit()

//******************************************** END MEM BLOCK STUFF ****************************************************************************************************

    public static void main(String[] args) {

        ArrayList<MemBlock> blocks = new ArrayList<>();
        int hole, readySize;

        blocks.add(new MemBlock(MEM_SIZE,true));

        int vtu = 0;
        int processSize, processTime, processSizeIndex, processTimeIndex, processArrival;
//************************************************** Creating Processes and their associated info **************************************************

        while (vtu < 4000) {  // could move into main loop and do each loop

        processSizeIndex = randomizer.nextInt(PROCESS_MEMORY_SIZES.length);
        processSize = PROCESS_MEMORY_SIZES[processSizeIndex];

        processTimeIndex = randomizer.nextInt(PROCESS_EXECUTION_TIMES.length);
        processTime = PROCESS_EXECUTION_TIMES[processTimeIndex];
        processArrival = 1 + (int)(Math.random() * ((10 - 1) + 1));

        processList.add(new Process(vtu, processSize, processTime, processArrival, 0, 0));

        vtu++;
        }
//******************************************************** START MAIN SIMULATION FOR 6000 VTUS ************************************************************

        while(currentVtu < MAX_VTUS) {

            // loading up the ready queue
            if (processList.get(0).getArrivalDelay() == currentVtu - timeWhenProcessArrived) {

                readyQueue.add(processList.get(0));
                readyQueue.get(readyQueue.size() - 1).start = currentVtu;
                readyQueue.get(readyQueue.size() - 1).end = currentVtu + readyQueue.get(readyQueue.size() - 1).processTime;
                timeWhenProcessArrived = currentVtu;
                processList.remove(0);
            }


            // loading into memory if space, rejecting process if not

            while (!readyQueue.isEmpty()) { // if there are processes ready to run // TODO could be infinite loop // ISNT FULL MEMORY

                for (int i = 0; i < readyQueue.size(); i++) { // run through all processes in the ready queue

                    readySize = readyQueue.get(i).getProcessSize();
                    hole = worstFit(readySize, blocks);

                    if (hole == 999) {
                        readyQueue.remove(i);
                        rejectedProcesses++;

                    } else {
                        insertPart(readySize, blocks, hole); // inserting partition of process size into main block of memory
                        running.add(readyQueue.get(i));
                        readyQueue.remove(i);
                    }
                } // end loop through all processes in ready queue
            } // end of ready queue adding processes to running

            if(running.size() > 0) {

                for (int v = 0; v < running.size(); v++) {

                    if (running.get(v).getEnd() == currentVtu) {

                        for (int g = 0; g < blocks.size(); g++) {

                            if (blocks.get(g).isPart() && (running.get(v).getProcessSize() == blocks.get(g).getSize())) {

                                removePart(blocks, g);
                                finished.add(running.get(v));
                            }
                        }                                   /////// Dont know if tracking & keeping total memory under 1750 or not ***********************
                        running.remove(v);
                    }                               // TODO: CALCULATE EXTERNAL FRAG IN BYTES !!!!!!!!!!!!!!!!!! **************************************
                } // end RUNNING loop
            }

        printStats(currentVtu, blocks);
        currentVtu++;

        } // end OUTER MOST WHILE //********************************************************

    } // end main

} // end phasei class


/*
RR - let's the FIFO process in the ready queue execute for 5 vtus before moving on to the next one, looping back to the front
Have a second ready queue that unfinished processes get sent to I guess


 */