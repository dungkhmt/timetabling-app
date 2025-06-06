package openerp.openerpresourceserver.generaltimetabling.algorithms.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class MaxClique {
    Map<AClass, SolutionClass> sol;
    int n;
    boolean[][] A;
    int[] x;

    private boolean check(int v, int k) {
        for (int i = 0; i < k; i++)
            if (A[x[i]][v] == false) return false;
        return true;
    }

    private void tryValue(int k) {
        //System.out.println("tryValue(" + k + "/" + n + ")");
        for (int v = x[k - 1] + 1; v < n; v++) { // check v greater than the last added vertex
            if (check(v, k)) {
                x[k] = v;
                //System.out.println("tryValue(" + k + "/" + n + "), assign x[" + k + "] = " + v);
                if (k + 1 > res) res = k + 1;
                if (k == n - 1) {
                    if (k + 1 > res) res = k + 1;
                } else tryValue(k + 1);
            }
        }
    }

    int res;

    public int computeMaxClique(Map<AClass, SolutionClass> sol) {
        this.sol = sol;
        n = sol.keySet().size();
        List<AClass> V = new ArrayList<>();
        for (AClass c : sol.keySet()) {
            V.add(c);
        }
        A = new boolean[n][n];
        for (int i = 0; i < n; i++)
            for (int j = 0; j < n; j++)
                A[i][j] = false;
        for (int i = 0; i < n; i++) {
            for (int j = i + 1; j < n; j++) {
                AClass ci = V.get(i);
                AClass cj = V.get(j);
                SolutionClass si = sol.get(ci);
                SolutionClass sj = sol.get(cj);
                if (si.overlap(sj)) {
                    A[i][j] = true;
                    A[j][i] = true;
                }
            }
        }
        x = new int[n];
        res = 1;
        // try each vertex v as a starting point for a clique
        // then try to build the largest possible clique starting from v
        for (int v = 0; v < n; v++) {
            x[0] = v;
            tryValue(1);
        }
        return res;
    }
}
