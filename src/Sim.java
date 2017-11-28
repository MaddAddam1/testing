import java.util.ArrayList;
import java.util.Random;
import java.util.*;

public abstract class Sim {
    protected static final char FREE_MEMORY = '.';
    protected static final char RESERVED_MEMORY = '#';
    protected static int currentVtu = -1;
    public static final int MAIN_MEMORY_SIZE = 1750;

    protected static char[] main_memory = new char[MAIN_MEMORY_SIZE];
    protected ArrayList<Process> processes;

    protected static final boolean MEMSIM_DEBUG = false;


        static class Process {

            int pid, processSize, processTime, arrivalDelay, start, end;

            public Process(int pid, int processSize, int processTime, int arrivalDelay,
                           int start, int end) {

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

            public int getArrivalDelay() {
                return arrivalDelay;
            }

            public int getStart() {
                return start;
            }

            public int getEnd() {
                return end;
            }

        }

    /**
     * Put a process into memory
     * @param p The process to put into memory
     */
    /**
     * Put a process in memory (noncontiguously).
     * @param p The process to put in memory.
     */

    public static void putInMemory(Process p) {
        int remainingToPlace = p.getProcessSize();
        for (int i = 0; i < main_memory.length && remainingToPlace > 0; i++) {
            if (main_memory[i] == FREE_MEMORY) {
                main_memory[i] = (char) p.getPid();
                remainingToPlace--;
            }
        }

    }



    public static int worst(int slotSize){

        //Go through and find the index of the biggest gap
        int best_start = -1;
        int current_start = -1;
        int biggest_size = 0;
        int found_size = 0;

        for (int i = 0; i < main_memory.length; i++) {
            if (main_memory[i] == FREE_MEMORY) {
                if (found_size == 0) {
                    current_start = i;
                }
                found_size++;
            } else {
                //Just hit non-free memory
                if (found_size > biggest_size) {
                    biggest_size = found_size;
                    best_start = current_start;
                }
                found_size = 0;
            }
        }

        //If the last slot is free, we take care of that here
        if (found_size > biggest_size) {
            biggest_size = found_size;
            best_start = current_start;
        }

        if (slotSize > biggest_size) { //No slot available
            return -1;
        } else {
            return best_start;
        }
    }



    /**
     * Take a process out of memory
     * @param p The process to remove from memory
     */
    public static void removeFromMemory(Process p) {
        for (int i = 0; i < main_memory.length; i++) {
            if (main_memory[i] == p.getPid()) {
                main_memory[i] = FREE_MEMORY;
            }
        }
    }

    /**
     * Attempt to defragment main memory
     */
    public static void defragment() {
        HashMap<Character, Integer> processesMoved = new HashMap<Character, Integer>();

        System.out.println("Performing defragmentation...");

        int destination = 1750;
        for (int i = 0; i < main_memory.length; i++) {
            if (main_memory[i] != FREE_MEMORY

                    && i != destination ) {
                main_memory[destination] = main_memory[i];
                main_memory[i] = FREE_MEMORY;
                destination++;
                processesMoved.put(main_memory[i], null);
            }
        }
        int numMoved = processesMoved.size();
        int freeBlockSize = main_memory.length - destination;
        double percentage = (double)freeBlockSize / (double)main_memory.length;

    }




//******************************************** END MEM BLOCK STUFF ****************************************************************************************************





    }



