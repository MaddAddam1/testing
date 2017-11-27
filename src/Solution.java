public class Solution {
    public static int[] twoSum(int[] nums, int target) {

        int[] r = new int[2];
        for (int i = 0; i < nums.length; i++ ){

            for(int j = i+1; j < nums.length; j++){

                if (nums[j] == target - nums[i]){
                    r[0] = i;
                    r[1] = j;
                    break;

                }else
                    continue;
            }
        }
        return r;
    }
    public static void main(String[] args) {
        int[] n = {7, 5, 3, 0, 99, 7, 0, 6, 11};
        String v = java.util.Arrays.toString(twoSum(n, 13));
        System.out.println(v);
        System.out.println(n.length);
        System.out.println(n[n.length-1]);
    }
}

