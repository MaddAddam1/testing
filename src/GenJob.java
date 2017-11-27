import java.util.*;

class GenJob {
    private static Random rand = new Random();

    /*
     *  gen_job_profile1() returns the size of a job from the first profile.
     *  Requested sizes range from 1-1024 blocks
     */
    static int gen_job_profile1() {
        int result = 0;

        return rand.nextInt(100) + 1;
    } // end of gen_job_profile1

    /*
     *  gen_job_profile2() returns the size of a job from the second profile.
     *  Requested sizes range from 1-100 blocks
     */
    static int gen_job_profile2() {
        int result = 1;
        int roll = rand.nextInt(10);
        while ((roll != 0) && (result + roll < 1024)) {
            result += roll;
            roll = rand.nextInt(10);
        }

        return result;
    } // end of gen_job_profile2
}
