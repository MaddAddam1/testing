import java.util.*;
public class phasei {

    // creating the random number generator
    public static Random randomizer = new Random();
    // MAX cpu cycles possible is 6000 VTUs
    public static final int MAX_VTUS = 6000;

    //Array of possible memory sizes, will use the randomizer to choose an index/value from this array to serve as the random process' size
    public static final int[] PROCESS_MEMORY_SIZES = {50, 60, 70, 80, 90, 100, 110, 120, 130, 140, 150, 160, 170, 180, 190,
            200, 210, 220, 230, 240, 250, 260, 270, 280, 290, 300};
    //Array of possible process execution times, will use the randomizer to choose an index/value from this array to serve as the random process' execution time in VTUs
    public static final int[] PROCESS_EXECUTION_TIMES = {5, 10, 15, 20, 25, 30, 35, 40, 45, 50, 55, 60};

    public static int FREE_MEMORY = 0;
    public static int RESERVED_MEMORY = 1;
    public int CURRENT_TIME = -1;
    public static int nchecks;

    public static int[] main_memory = new int[2000];
    public static int rejectedProcesses, finishedProcesses;



    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    public static void initializeMainMemory() {
        for (int i = 0; i < 250 && i < main_memory.length; i++) {
            main_memory[i] = RESERVED_MEMORY;
        }
        for (int i = 1750; i < main_memory.length; i++) {
            main_memory[i] = FREE_MEMORY;
        }
    }

    static class Process{

    int pid, processSize, processTime, arrivalDelay, start, end, startIndex, endIndex;

        public Process(int pid, int processSize, int processTime, int arrivalDelay,
                       int start, int end, int startIndex, int endIndex){

            this.pid = pid;
            this.processSize = processSize;
            this.processTime = processTime;
            this.arrivalDelay = arrivalDelay;
            this.start = start;
            this.end = end;
            this.startIndex = startIndex;
            this.endIndex = endIndex;

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

        public int getStartIndex() {
            return startIndex;
        }

        public int getEndIndex() {
            return endIndex;
        }
    } // end class process

//********************************************************************************************************************************************


        // constant definitions
        static final int MEM_SIZE = 1750;
        static final int WARMUP_STEPS = 1000;
        static final int EXPERIMENT_STEPS = 6000;

        // global fields
        private static Random rand = new Random();


        /**
         * Class MemBlock keeps track of a block of pages.  It may represent
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
         * Class SimStats holds statistics computed about the simulation.
         * Add any number of public fields to this class.
         */
        static class SimStats {


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
                                         int loc, ArrayList<Process> process, int index, ArrayList<Process> running) {                     // of memory blocks, which are collections of pages
            MemBlock b = blocks.get(loc);                             // pages = like 1 for each hk size, so 50kb = mem block of 50 pages
                                                                    // LOC = index in blocks of MEMBLOCKS array

            if ((b.getSize() < size) || (b.isPart())) {
                // can't create partition
                rejectedProcesses++;
                running.add(process.get(index));
                process.remove(index);
                return false;

            } else if (b.getSize() == size) {
                // partition fits exactly
                b.makePart();

            } else {
                // extra hole left over
                b.setSize(b.getSize() - size);
                //blocks.insertElementAt(new MemBlock(size,false), loc); //changed from vector
                blocks.add(loc, new MemBlock(size, false));     // replacing original block
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


        /**
         * chooseEviction() chooses a partition at random and evicts it.
         * <p>
         * TODO Must change and make it evict once it's time is up
         * R/W blocks:  Entire list of blocks
         * Return value:  Hole created by eviction
         *
         * TODO find if memory full by adding together the sizes of the memblocks in the arraylist of memblocks
         * TODO means index contains memory of process, so go thru arraylist and add up block.get(i).getSize()
         *
         */
        public static int chooseEviction(ArrayList<MemBlock> blocks, ArrayList<Process> process, int vtu, ArrayList<Process> finished) {
            MemBlock b; // memblock practically a block of process's memory its taking up
            int victim;
            int loc = 0;
            int npart = 0;
            int count = 0;
            int vic = 0;

            // count partitions
            for (int i = 0; i < blocks.size(); i++) {
                b = blocks.get(i);
                if (b.isPart()) {
                    npart++;
                }
            }

            // make sure memory is not empty
            if (npart == 0) {
                System.err.println("Error:  attempted to free partition in empty memory.");
                System.exit(-1);
            }

            for(int t = 0; t < process.size(); t++){
                if(process.get(t).getEnd() == vtu){     //TODO: probably not right, the selection of eviction, not lined up with blocks size and readyqueue size, maybe getting the wrong one
                        victim = t;
                        finished.add(process.get(t));
                        process.remove(t);
                        finishedProcesses++;
                    // figure the block number
                    for (int i = 0; i < blocks.size(); i++) {
                        b = blocks.get(i);
                        if (b.isPart()) {
                            if (count == victim) {
                                //System.out.println("Removing "+i);
                                loc = i;
                            }
                            count++;
                        }
                    }
                }
            }

            return loc;
        } // end of chooseEviction()


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
            int loc = 0;
            int maxSize = 0;

            // search current holes for biggest
            for (int i = 0; i < blocks.size(); i++) {
                b = blocks.get(i);
                if (b.isHole()) {
                    nchecks++;
                    if (b.getSize() > maxSize) {
                        maxSize = b.getSize();
                        loc = i;
                    }
                }
            }
            return loc;
        } // end of worstFit()

//******************************************** END MEM BLOCK STUFF ****************************************************************************************************

    public static void main(String[] args) {

        ArrayList<MemBlock> blocks = new ArrayList<>();
        int hole;
        int time;
        int size;
        int nchecks;

        blocks.add(new MemBlock(MEM_SIZE,true));

        ArrayList<Process> processList = new ArrayList<Process>();
        ArrayList<Process> readyQueue = new ArrayList<Process>();
        ArrayList<Process> running = new ArrayList<>();
        ArrayList<Process> finished = new ArrayList<>();

        int timeWhenProcessArrived = 0, rejectedProcesses = 0;


        int vtu = 0;
        int processSize, processTime, processSizeIndex, processTimeIndex, processArrival;

        while (vtu < 4000) {  // could move into main loop and do each loop

        processSizeIndex = randomizer.nextInt(PROCESS_MEMORY_SIZES.length);
        processSize = PROCESS_MEMORY_SIZES[processSizeIndex];

        processTimeIndex = randomizer.nextInt(PROCESS_EXECUTION_TIMES.length);
        processTime = PROCESS_EXECUTION_TIMES[processTimeIndex];
        processArrival = 1 + (int)(Math.random() * ((10 - 1) + 1));

        processList.add(new Process(vtu, processSize, processTime, processArrival, 0, 0, 0, 0));

        vtu++;
        }
//******************************************************** END PROCESS CREATION ************************************************************

        int currentVtu = 0;
        int readySize, toBeRemoved;


        while(currentVtu < MAX_VTUS){

            // loading up the ready queue
            if(processList.get(0).getArrivalDelay() == currentVtu - timeWhenProcessArrived){
                Process temp = processList.get(0);
                readyQueue.add(temp);
                readyQueue.get(readyQueue.size() - 1).start = currentVtu;
                readyQueue.get(readyQueue.size() - 1).end = currentVtu + readyQueue.get(readyQueue.size() - 1).processTime;
                timeWhenProcessArrived = currentVtu;
            }

            // loading into memory if space, rejecting process if not

            if(!readyQueue.isEmpty()){ // if there are processes ready to run // TODO could be infinite loop // ISNT FULL MEMORY

                for(int i = 0; i < readyQueue.size(); i++){ // run through all processes in the ready queue

                    readySize = readyQueue.get(i).getProcessSize();
                    hole = worstFit(readySize, blocks);
                    insertPart(readySize, blocks, hole, readyQueue, i, running); // inserting partition of process size into main block of memory
                    // TODO getting the right value for loc variable very important -- hole is the index location of the worst hole
                    // TODO have to check for space, if there's any, rejecting process

                } // end loop through all processes in ready queue
            } // end of ready queue adding processes to running

            if(!running.isEmpty()){

                toBeRemoved = chooseEviction(blocks, running, currentVtu, finished);
                removePart(blocks, toBeRemoved);

                } // end RUNNING if








        currentVtu++;
        } // end OUTER MOST WHILE //********************************************************


















    } // end main

} // end phasei class
