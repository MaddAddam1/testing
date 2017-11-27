import java.util.*;
import java.io.*;

/**
 *  memsim.cpp
 *
 *  @author N. Howe
 *
 *  This program simulates a memory with variable-sized partitions.
 */
public class MemSim {
    // constant definitions
    static final int MEM_SIZE = 1750;
    static final int WARMUP_STEPS = 1000;
    static final int EXPERIMENT_STEPS = 6000;

    // global fields
    private static Random rand = new Random();
    private static int nchecks;

    /**
     *  Class MemBlock keeps track of a block of pages.  It may represent
     *  either a hole or a partition.
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
     *  Class SimStats holds statistics computed about the simulation.
     *  Add any number of public fields to this class.
     */
    static class SimStats {




    }


    /**
     *  insertPart() inserts a partition into a hole.  If the hole is not large
     *  enough, it will do nothing and return false.  If it is exactly the
     *  right size, it will simply modify the partition in place.  If too big,
     *  it will decrease the size of the hole and create a new block for the
     *  partition.
     *
     *  R/O size:    Size of partition to create
     *  R/W blocks:  Entire list of blocks
     *  R/W loc:    Original hole location
     *  Return value:  T/F was partition inserted?
     */
    public static boolean insertPart(int size, ArrayList<MemBlock> blocks, // size is process size, blocks are collections
                                     int loc) {                     // of memory blocks, which are collections of pages
        MemBlock b = blocks.get(loc);                             // pages = like 1 for each hk size, so 50kb = mem block of 50 pages

        if ((b.getSize() < size)||(b.isPart())) {
            // can't create partition
            return false;

        } else if (b.getSize() == size) {
            // partition fits exactly
            b.makePart();

        } else {
            // extra hole left over
            b.setSize(b.getSize() - size);
            //blocks.insertElementAt(new MemBlock(size,false), loc); //changed from vector
            blocks.add(loc, new MemBlock(size,false));
        }
        return true;
    } // end of insertPart()


    /**
     *  removePart() removes a partition.  If there is a hole on either side,
     *  it merges all the space into one big hole.
     *
     *  R/W blocks:  Entire list of blocks
     *  R/W loc:    Original partition location
     *
     * @returns  new location (after consolidating holes)
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


    /** chooseEviction() chooses a partition at random and evicts it.
     *
     * TODO Must change and make it evict once it's time is up
     *  R/W blocks:  Entire list of blocks
     *  Return value:  Hole created by eviction
     */
    public static int chooseEviction(ArrayList<MemBlock> blocks) {
        MemBlock b;
        int victim;
        int loc = 0;
        int npart = 0;
        int count = 0;

        // count partitions
        for (int i = 0; i < blocks.size(); i++) {
            b = blocks.get(i);
            if (b.isPart()) {
                npart++;
            }
        }
        //System.out.println(npart);

        // make sure memory is not empty
        if (npart == 0) {
            System.err.println("Error:  attempted to free partition in empty memory.");
            System.exit(-1);
        }

        // choose a victim
        victim = rand.nextInt(npart);

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
        return loc;
    } // end of chooseEviction()


    /**
     *  worstFit() returns the biggest hole it can find.
     *  If a big enough one does not exist, it evicts partitions until
     *  one does.
     *
     *  R/O size:    Size of partition to create
     *  R/W blocks:  Entire list of blocks
     *  GLOBAL:  nchecks stores number of blocks examined
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

        // if not big enough, perform evictions
        while (maxSize < size) {
            loc = chooseEviction(blocks);
            //printBlocks(blocks);
            System.out.println("Evicting partition of size "
                    +blocks.get(loc).getSize());
            loc = removePart(blocks,loc);
            maxSize = blocks.get(loc).getSize();
        }
        return loc;
    } // end of worstFit()

    /**
     *  gatherStats() counts the total fragmentation and the average hole size.
     *
     *  R/O blocks:  Entire list of blocks
     *  W/O nhole:   Number of holes
     *  W/O frag:    Fragmentation (fraction of memory in holes)
     */
    public static SimStats gatherStats(ArrayList<MemBlock> blocks) {
        MemBlock b;
        SimStats stats = new SimStats();

        for (int i = 0; i < blocks.size(); i++) {
            b = blocks.get(i);
            // fill in here
        }
        return stats;
    } // end of gatherStats()

    /**
     *  printBlocks() displays a list of all the blocks in memory.
     *  This is for debugging purposes.
     *
     *  R/O blocks:  Entire list of blocks
     */
    public static void printBlocks(ArrayList<MemBlock> blocks) {
        MemBlock b;

        System.out.println("List of blocks in memory:");
        for (int i = 0; i < blocks.size(); i++) {
            b = blocks.get(i);
            if (b.isHole()) {
                System.out.println("  Hole of size "+b.getSize());
            } else {
                System.out.println("  Partition of size "+b.getSize());
            }
        }

    } // end of printBlocks()

    /**
     *  main() runs the simulation and prints the results.
     */
    public static void main(String[] args) {
        ArrayList<MemBlock> blocks = new ArrayList<MemBlock>();
        int hole;
        int time;
        int size;
        int nchecks;

        // insert code here to choose job profile and placement policy

        // start memory as one big hole
        blocks.add(new MemBlock(MEM_SIZE,true));

        // warm up
        for (time = 0; time < WARMUP_STEPS; time++) {
            size = GenJob.gen_job_profile1();
            System.out.println("Inserting job of size " + size);
            hole = worstFit(size, blocks);
            insertPart(size, blocks, hole);
        }

        // experiment
        for (time = 0; time < EXPERIMENT_STEPS; time++) {
            size = GenJob.gen_job_profile1();
            System.out.println("Inserting job of size " + size);
            hole = worstFit(size, blocks);
            insertPart(size, blocks, hole);
            // gather statistics here
        }

        // print results here
    }
}
