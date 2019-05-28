package com.crx.kids.project.node.logic;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Queens {

    public static void main(String[] args) {
        Queens queens = new Queens();
        queens.calculateResults(8);
    }


    public List<QueensResult> calculateResults(int dimension) {


        // columns
        Integer[] queens = new Integer[dimension];
        List<Integer[]> results = calculate(queens, 0, dimension);

        results.forEach(r -> System.out.println(Arrays.toString(r)));

        return null;
    }



    public List<Integer[]> calculate(Integer[] queens, int columnForCheck, int dimension) {
        if (columnForCheck >= dimension) {
            return new ArrayList<>();
        }

        List<Integer[]> results = new ArrayList<>();

        for (int i = 0; i < dimension; i++) {
            queens[columnForCheck] = i;

            if (!attack(queens, i, columnForCheck)) {

                List<Integer[]> subResults = calculate(queens, columnForCheck + 1, dimension);
                results.addAll(subResults);

                if (columnForCheck == dimension - 1) {
                    results.add(Arrays.copyOf(queens, queens.length));
                }
            }
        }

        return results;
    }


//    _ _ Q _ _ _
//    Q _ _ _ _ _
//    _ _ _ _ _ _
//    _ _ _ _ Q?_
//    _ Q _ _ _ _
//    _ _ _ Q _ _ =
    public boolean attack(Integer[] queens, int queen, int queenPosition) {
        if (queenPosition == 0) {
            return false;
        }

        for (int q = 0; q < queenPosition; q++) {

            if (queens[q] == queen) {
                return true;
            }
            int distance = queenPosition - q;

            int upperAttackField = queen + distance;
            int lowerAttackField = queen - distance;

            if (upperAttackField < queens.length) {
                if (upperAttackField == queens[q]){
                    return true;
                }
            }

            if (lowerAttackField >= 0) {
                if (lowerAttackField == queens[q]){
                    return true;
                }
            }
        }
        return false;
    }


}
