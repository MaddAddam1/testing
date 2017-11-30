import java.util.*;

public class lastResort {


    // creating the random number generator
        public static Random randomizer = new Random();
        // MAX cpu cycles possible is 6000 VTUs
        public static final int MAX_VTUS = 6000;

        public static final int MEM_SIZE = 1750;

        public static final int ROUND_ROBIN = 5;

        //Array of possible memory sizes, will use the randomizer to choose an index/value from this array to serve as the random process' size
        public static final int[] PROCESS_MEMORY_SIZES = {50, 60, 70, 80, 90, 100, 110, 120, 130, 140, 150, 160, 170, 180, 190,
                200, 210, 220, 230, 240, 250, 260, 270, 280, 290, 300};

        //Array of possible process execution times, will use the randomizer to choose an index/value from this array to serve as the random process' execution time in VTUs
        public static final int[] PROCESS_EXECUTION_TIMES = {5, 10, 15, 20, 25, 30, 35, 40, 45, 50, 55, 60};

        public static ArrayList<Process> processList = new ArrayList<Process>();
        public static ArrayList<Process> readyQueue = new ArrayList<Process>();
        public static ArrayList<Process> running = new ArrayList<Process>();
        public static ArrayList<Process> finished = new ArrayList<Process>();

        public static ArrayList<Process> pending = new ArrayList<>(100);


        public static int nchecks, currentVtu = 0;

        public static int externalFrag = 0; // TODO **************************************************

        // variables used for calculating the required statistics
        public static double averageOccupied = 0, averageMemoryHoleSize = 0, averageTurnaroundTime = 0,
                averageWaitingTime = 0, averageProcessingTime = 0;

        // time when a new process has completed its wait time and is ready for allocation
        public static int timeWhenProcessArrived = 0;

        // used in calculating the average stats
        public static int totalWaitTimes = 0, totalTurnaroundTimes = 0, totalProcessTimes = 0;

        ////*********************************************************** Class Process *************************************************************************************************
        static class Process{

            int pid, processSize, processTime, arrivalDelay, start, end, elapsedTime, lastStartTime, lastEndTime;

            public Process(int pid, int processSize, int processTime, int arrivalDelay,
                           int start, int end, int elapsedTime, int lastStartTime, int lastEndTime){

                this.pid = pid;
                this.processSize = processSize;
                this.processTime = processTime;
                this.arrivalDelay = arrivalDelay;
                this.start = start;
                this.end = end;
                this.elapsedTime = elapsedTime;
                this.lastStartTime = lastStartTime;
                this.lastEndTime = lastEndTime;

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

            public int getElapsedTime(){
                return elapsedTime;
            }
            public int getLastStartTime() {
                return lastStartTime;
            }

            public int getLastEndTime() {
                return lastEndTime;
            }

        } // end class process

//*********************************************************** End Class Process *************************************************************************************************


        /**
         * Class MemoryBlock keeps track of a block of pages.  It may represent
         * either a hole or a partition.
         */
        static class MemBlock {
            private boolean hole;
            private int size;
            private int pid;

            public MemBlock(int s, boolean h, int id) { //mem block size should be the process size
                size = s;
                hole = h;
                pid = id;
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
            public int getPid() {
                return pid;
            }
            public void setPid(int id) {
                pid = id;
            }

        }

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
            MemBlock c = new MemBlock(holeSize, true, -1);
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


        public static void printStats(int b, ArrayList<MemBlock> blocks, ArrayList<Process> finished){

            if(b == 5000){
                for (int r = 0; r < finished.size(); r++) { // for loop calculating average wait, turnaround, and process times
                    totalWaitTimes += finished.get(r).getArrivalDelay();
                    totalProcessTimes += finished.get(r).getProcessTime();
                    totalTurnaroundTimes += (finished.get(r).getArrivalDelay() + finished.get(r).getProcessTime());
                }
                averageProcessingTime = totalProcessTimes / (finished.size());
                averageTurnaroundTime = totalTurnaroundTimes / (finished.size());
                averageWaitingTime = totalWaitTimes / (finished.size());
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

        } // end printStats method

//************************************************************ START FITTING ALGORITHMS ************************************************************************************
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

//******************************************************** END FITTING ALGORITHMS ************************************************************************************
public static void checkPending(ArrayList<MemBlock> blocks, ArrayList<Process> pending, ArrayList<Process> running, int vtu){

    int readySize, hole;
    if(pending.size() > 0){

        for (int h = 0; h < pending.size(); h++) {

            readySize = pending.get(h).getProcessSize();
            hole = worstFit(readySize, blocks);

            if (hole != 999) {
                insertPart(readySize, blocks, hole); // inserting partition of process size into main block of memory
                pending.get(h).lastStartTime = vtu;
                if(pending.get(h).getElapsedTime() == 0) {
                    pending.get(h).start = vtu;          // first time running
                }
                running.add(pending.get(h));
                pending.remove(h);

            }
        }

    } // end pending list check

}

    public static void checkReadyQueue(ArrayList<MemBlock> blocks, ArrayList<Process> ready, ArrayList<Process> running,
                                       ArrayList<Process> pending, int vtu){

        int readySize, hole;
        if(ready.size() > 0){

            for (int h = 0; h < ready.size(); h++) {

                readySize = ready.get(h).getProcessSize();
                hole = worstFit(readySize, blocks);

                if (hole != 999) {
                    insertPart(readySize, blocks, hole); // inserting partition of process size into main block of memory
                    ready.get(h).lastStartTime = vtu;
                    if(ready.get(h).getElapsedTime() == 0) {
                        ready.get(h).start = vtu;          // first time running
                    }
                    running.add(ready.get(h));
                    ready.remove(h);
                } else{
                    if(pending.size() < 100) {
                        pending.add(ready.get(h));
                    }
                    ready.remove(h);
                }
            }

        } // end pending list check

    }

    public static void checkRunning(ArrayList<MemBlock> blocks, ArrayList<Process> running, ArrayList<Process> pending,
                                    ArrayList<Process> finished, int vtu){

        if(running.size() > 0){

            for (int i = 0; i < running.size(); i++) {

                if(running.get(i).getElapsedTime() >= running.get(i).getProcessTime() || (currentVtu - running.get(i).getLastStartTime() == 5)) {

                    for (int j = 0; j < blocks.size(); j++) {

                        if(running.get(i).getProcessSize() == blocks.get(j).getSize() && blocks.get(j).isPart()){

                            removePart(blocks, j);
                        }
                    } // check all blocks against the running processes

                    if((currentVtu - running.get(i).getLastStartTime() == 5)){
                        running.get(i).lastEndTime = vtu;
                        running.get(i).elapsedTime = running.get(i).elapsedTime + 5;
                        pending.add(running.get(i));
                        running.remove(i);

                    }else {
                        running.get(i).end = vtu;
                        finished.add(running.get(i));
                        running.remove(i);
                    }

                } // end IF expiring

            } // check all running processes
        }
    }

//******************************************** START of MAIN METHOD ****************************************************************************************************

        public static void main(String[] args) {

            ArrayList<MemBlock> blocks = new ArrayList<>();
            int hole, readySize;

            blocks.add(new MemBlock(MEM_SIZE,true, -1));

            int vtu = 0;
            int processSize, processTime, processSizeIndex, processTimeIndex, processArrival;
//************************************************** Creating Processes and their associated info **************************************************

            while (vtu < 4000) {  // could move into main loop and do each loop

                processSizeIndex = randomizer.nextInt(PROCESS_MEMORY_SIZES.length);
                processSize = PROCESS_MEMORY_SIZES[processSizeIndex];

                processTimeIndex = randomizer.nextInt(PROCESS_EXECUTION_TIMES.length);
                processTime = PROCESS_EXECUTION_TIMES[processTimeIndex];
                processArrival = 1 + (int)(Math.random() * ((10 - 1) + 1));

                processList.add(new Process(vtu, processSize, processTime, processArrival, 0, 0,0, 0, 0));

                vtu++;
            }
//******************************************************** START MAIN SIMULATION FOR 6000 VTUS ************************************************************

            while(currentVtu < MAX_VTUS) {

                // loading up the ready queue
                if (processList.get(0).getArrivalDelay() == currentVtu - timeWhenProcessArrived) {

                    readyQueue.add(processList.get(0));
                    //readyQueue.get(readyQueue.size() - 1).start = currentVtu;
                    //readyQueue.get(readyQueue.size() - 1).end = currentVtu + readyQueue.get(readyQueue.size() - 1).processTime;
                    timeWhenProcessArrived = currentVtu;
                    processList.remove(0);
                }


            if(currentVtu < 100) { // check ready queue first for the first 100 cycles, so the pending list can be built up a little

                checkReadyQueue(blocks, readyQueue, running, pending, currentVtu);
                checkPending(blocks, pending, running, currentVtu);
                checkRunning(blocks, running, pending, finished, currentVtu);
            }

                checkPending(blocks, pending, running, currentVtu);
                checkReadyQueue(blocks, readyQueue, running, pending, currentVtu);
                checkRunning(blocks, running, pending, finished, currentVtu);


























                printStats(currentVtu, blocks, finished);
                currentVtu++;

            } // end OUTER MOST WHILE //********************************************************

            System.out.println("FINISHED: " + finished.size());
            System.out.println("\nREADY " + readyQueue.size() + "\n");
            System.out.println("PENDING " + pending.size());

        } // end main

    } // end phasei class


// TODO: MUST RUN FOR ALL ---- FITING ALGORITHMS ------- ***************************

/*
RR - let's the FIFO process in the ready queue execute for 5 vtus before moving on to the next one, looping back to the front
Have a second ready queue that unfinished processes get sent to I guess


 */
