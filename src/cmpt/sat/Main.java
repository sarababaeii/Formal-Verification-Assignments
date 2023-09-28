package cmpt.sat;

import com.microsoft.z3.BoolExpr;
import com.microsoft.z3.Context;
import com.microsoft.z3.Model;
import com.microsoft.z3.Solver;
import com.microsoft.z3.Status;

import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        // Create a new context
        Context ctx = new Context();

        // Create a solver
        Solver solver = ctx.mkSolver();

        Scanner scanner = new Scanner(System.in);
        int n = scanner.nextInt(); // Num of vertices
        int m = scanner.nextInt(); // Num of colors
        int e = scanner.nextInt(); // Num of edges I ADDED IT

        // Create boolean variables for edges
        BoolExpr[][] edges = new BoolExpr[n][n];
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; ++j) {
                edges[i][j] = ctx.mkBoolConst("e_" + i + "_" + j);
            }
        }

        // Setting edges
        for (int i = 0; i < e; i++) {
            int v = scanner.nextInt();
            int w = scanner.nextInt();
            solver.add(ctx.mkIff(edges[v - 1][w - 1], ctx.mkTrue()));
            solver.add(ctx.mkIff(edges[w - 1][v - 1], ctx.mkTrue()));
        }

        // Create boolean variables for vertices and colors
        BoolExpr[][] color = new BoolExpr[n][m];
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < m; ++j) {
                color[i][j] = ctx.mkBoolConst("p_" + i + "_" + j);
            }
        }

        // Add formulas: every vertex is colored
        // (c00 \/ c01 \/ ... \/ c0m)  /\ ... /\ (cn0 \/ cn1 \/ ... \/ cnm)
        for (int i = 0; i < n; i++) {
            BoolExpr f = ctx.mkFalse();
            for (int j = 0; j < m; j++) {
                f = ctx.mkOr(f, color[i][j]);
            }
            solver.add(f);
        }

        // Add formulas: every vertex has at most one color
        // (!c00 \/ !c01) /\ (!c00 \/ !c02) /\ ... /\ (!c0m-1 \/ !c0m)  /\ ... /\ (!cn0 \/ !cn1) /\ (!cn0 \/ !cn2) /\ ... /\ (!cnm-1 \/ !cnm)
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < m; j++) {
                for (int k = 0; k < m; k++) {
                    if (j != k) {
                        solver.add(ctx.mkOr(ctx.mkNot(color[i][j]), ctx.mkNot(color[i][k])));
                    }
                }
            }
        }

        // Add formulas: no two connected vertices have same colors
        // (!e01 \/ !c00 \/ !c10) /\ (!e01 \/ !c01 \/ !c11) /\ ... /\ (!e01 \/ !c0m \/ !c1m)  /\ ... /\ (!en-1n \/ !cn-10 \/ !cn0) /\ (!en-1n \/ !cn-11 \/ !cn1) /\ ... /\ (!en-1n \/ !cn-1m \/ !cnm)
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                if (i != j) {
                    for (int k = 0; k < m; k++) {
                        solver.add(ctx.mkOr(ctx.mkNot(edges[i][j]), ctx.mkNot(color[i][k]), ctx.mkNot(color[j][k])));
                    }
                }
            }
        }

        // Check satisfiability
        Status status = solver.check();

        if (status == Status.SATISFIABLE) {
            // Get a model
            Model model = solver.getModel();

            for (int i = 0; i < n; i++) {
                for (int j = 0; j < m; ++j) {
                    if (model.getConstInterp(color[i][j]).isTrue()) {
                        int v = i + 1;
                        int c = j + 1;
                        System.out.println(v + " " + c);
                    }

                }
            }
        } else if (status == Status.UNSATISFIABLE){
            System.out.println("No Solution");
        } else {
            System.out.println("Unknown");
        }


//        System.out.println(model);
    }
}
