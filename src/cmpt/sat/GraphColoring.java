package cmpt.sat;

import com.microsoft.z3.BoolExpr;
import com.microsoft.z3.Context;
import com.microsoft.z3.Model;
import com.microsoft.z3.Solver;
import com.microsoft.z3.Status;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Scanner;
import java.util.StringTokenizer;

public class GraphColoring {

    public static final String inputFileName = "input.txt";
    public static final String outputFileName = "output.txt";

    public static void main(String[] args) {
        Graph graph = input();
        solveColoring(graph);
    }

    // Functions for getting input
    public static Graph input() {
        try {
            File inputFile = new File(inputFileName);
            Scanner scanner = new Scanner(inputFile);
            Graph graph = inputParameters(scanner);
            inputEdges(scanner, graph);
            return graph;
        } catch (IOException exception) {
            System.out.println("An error occurred.");
            exception.printStackTrace();
        }
        return null;
    }

    public static StringTokenizer inputLine(Scanner scanner) {
        if (scanner.hasNext()) {
            String s = scanner.nextLine();
            return new StringTokenizer(s, " ");
        }
        return null;
    }

    public static int inputInteger(StringTokenizer st) {
        return Integer.parseInt(st.nextToken());
    }

    public static Graph inputParameters(Scanner scanner) {
        StringTokenizer st = inputLine(scanner);
        if (st == null) {
            return null;
        }
        int n = inputInteger(st);
        int m = inputInteger(st);
        return new Graph(n, m);
    }

    public static void inputEdges(Scanner scanner, Graph graph) {
        StringTokenizer st = inputLine(scanner);
        while (st != null) {
            int v = inputInteger(st) - 1;
            int w = inputInteger(st) - 1;
            graph.insertEdge(v, w);
            st = inputLine(scanner);
        }
    }

    // Functions work with Z3
    public static void solveColoring(Graph graph) {
        Context context = new Context();
        Solver solver = context.mkSolver();
        BoolExpr[][] color = createVariables(context, graph);
        addFormulaEveryVertexColored(context, solver, color, graph);
        addFormulaEveryVertexAtMostOneColor(context, solver, color, graph);
        addFormulaAllConnectedVerticesDifferentColors(context, solver, color, graph);
        output(solver,color, graph);
    }

    public static BoolExpr[][] createVariables(Context context, Graph graph) {
        BoolExpr[][] color = new BoolExpr[graph.getNumberOfVertices()][graph.getNumberOfColors()];
        for (int i = 0; i < graph.getNumberOfVertices(); i++) {
            for (int j = 0; j < graph.getNumberOfColors(); ++j) {
                color[i][j] = context.mkBoolConst("p_" + i + "_" + j);
            }
        }
        return color;
    }

    public static void addFormulaEveryVertexColored(Context context, Solver solver, BoolExpr[][] color, Graph graph) {
        // (p00 \/ p01 \/ ... \/ p0m)  /\ ... /\ (pn0 \/ pn1 \/ ... \/ pnm)
        for (int i = 0; i < graph.getNumberOfVertices(); i++) {
            BoolExpr f = formulaAVertexColored(context, color, graph, i);
            solver.add(f);
        }
    }

    public static BoolExpr formulaAVertexColored(Context context, BoolExpr[][] color, Graph graph, int vertex) {
        // pi0 \/ pi1 \/ ... \/ pim
        BoolExpr f = context.mkFalse();
        for (int j = 0; j < graph.getNumberOfColors(); j++) {
            f = context.mkOr(f, color[vertex][j]);
        }
        return f;
    }

    public static void addFormulaEveryVertexAtMostOneColor(Context context, Solver solver, BoolExpr[][] color, Graph graph) {
        // (!p00 \/ !p01) /\ (!p00 \/ !p02) /\ ... /\ (!p0m-1 \/ !p0m)  /\ ... /\ (!pn0 \/ !pn1) /\ (!pn0 \/ !pn2) /\ ... /\ (!pnm-1 \/ !pnm)
        for (int i = 0; i < graph.getNumberOfVertices(); i++) {
            addFormulaAVertexAtMostOneColor(context, solver, color, graph, i);
        }
    }

    public static void addFormulaAVertexAtMostOneColor(Context context, Solver solver, BoolExpr[][] color, Graph graph, int vertex) {
        // (!pi0 \/ !pi1) /\ (!pi0 \/ !pi2) /\ ... /\ (!pim-1 \/ !pim)
        for (int j = 0; j < graph.getNumberOfColors(); j++) {
            for (int k = 0; k < graph.getNumberOfColors(); k++) {
                if (j != k) {
                    solver.add(context.mkOr(context.mkNot(color[vertex][j]), context.mkNot(color[vertex][k])));
                }
            }
        }
    }

    public static void addFormulaAllConnectedVerticesDifferentColors(Context context, Solver solver, BoolExpr[][] color, Graph graph) {
        // (!cv00 \/ !cv10) /\ (!cv01 \/ !cv11) /\ ... /\ (!cv0m \/ !cv1m)  /\ ... /\ (!cvn-10 \/ !cvn0) /\ (!cvn-11 \/ !cvn1) /\ ... /\ (!cvn-1m \/ !cvnm)
        for (int i = 0; i < graph.getNumberOfVertices(); i++) {
            for (int j = 0; j < graph.getEdges().get(i).size(); j++) {
                int w = graph.getEdges().get(i).get(j);
                addFormulaTwoConnectedVerticesDifferentColors(context, solver, color, graph, i, w);
            }
        }
    }

    public static void addFormulaTwoConnectedVerticesDifferentColors(Context context, Solver solver, BoolExpr[][] color, Graph graph, int v, int w) {
        // (!cv0 \/ !cw0) /\ (!cv1 \/ !cw1) /\ ... /\ (!cvm \/ !cwm)
        for (int k = 0; k < graph.getNumberOfColors(); k++) {
            solver.add(context.mkOr(context.mkNot(color[v][k]), context.mkNot(color[w][k])));
        }
    }

    // Functions for giving output
    public static void output(Solver solver, BoolExpr[][] color, Graph graph) {
        Status status = solver.check();
        try {
            FileWriter writer = new FileWriter(outputFileName);
            if (status == Status.SATISFIABLE) {
                Model model = solver.getModel();
                printColoring(writer, graph, model, color);
            } else if (status == Status.UNSATISFIABLE) {
                writer.write("No Solution");
                System.out.println("Successfully wrote to the file.");
            } else {
                writer.write("Unknown");
                System.out.println("Successfully wrote to the file.");
            }
            writer.close();
        } catch (IOException exception) {
            System.out.println("An error occurred.");
            exception.printStackTrace();
        }
    }

    public static void printColoring(FileWriter writer, Graph graph, Model model, BoolExpr[][] color) throws IOException {
        for (int i = 0; i < graph.getNumberOfVertices(); i++) {
            for (int j = 0; j < graph.getNumberOfColors(); ++j) {
                if (model.getConstInterp(color[i][j]).isTrue()) {
                    int v = i + 1;
                    int c = j + 1;
                    writer.write(v + " " + c + '\n');
                }
            }
        }
        System.out.println("Successfully wrote to the file.");
    }
}