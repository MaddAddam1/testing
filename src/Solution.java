import java.util.ArrayList;

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
        ArrayList<Integer> n = new ArrayList<Integer>();
        n.add(0, 3);
        n.add(1, 6);

        n.add(2, 124);
        n.add(3, 42);


        System.out.println(n.size());

        n.add(2, 9999);
        System.out.println(n.get(2));
        System.out.println(n.get(3));
        System.out.println(n.size());

    }
}

