package main;
import algorithms.*;
import grid.GridAndGoals;
import grid.GridGraph;
import main.graphgeneration.DefaultGenerator;
import main.testgen.TestDataGenerator;
import uiandio.GraphImporter;
import algorithms.visibilitygraph.BFSVisibilityGraph;

/**
 * Instructions: Look for the main method.
 * We can either run tests or trace the algorithm.
 * 
 * When tracing algorithms,
 * Choose the maze in loadMaze();
 * Choose the algorithm in setDefaultAlgoFunction();
 * 
 * The tracing / experimentation functions are detailed in the traceAlgorithm() method.
 */
public class AnyAnglePathfinding {
    public static final String PATH_TESTDATA = "testdata/";
    public static final String PATH_MAZEDATA = "mazedata/";
    static AlgoFunction algoFunction; // The algorithm is stored in this function.

    public static void main(String[] args) { // uncomment the one you need to use.\
        int choice = 0;

        switch(choice) {
            case 0:
                Visualisation.run();
                break;
            case 1:
                AlgoTest.run();
                break;
            case 2:
                Experiment.run();
                break;
            case 3:
                TestDataGenerator.run();
                break;
            case 4:
                GridGraphVisualiser.run();
                break;
        }
    }
    
    /**
     * Choose a maze. (a gridGraph setting)
     */
    static GridAndGoals loadMaze() {
        int choice = 1; // Adjust this to choose a maze.
        
        switch(choice) {
            case 0 : {// UNSEEDED
                int unblockedRatio = 12;      // chance of spawning a cluster of blocked tiles is 1 in unblockedRatio.
                int sizeX = 300;               // x-axis size of grid
                int sizeY = 300;               // y-axis size of grid

                int sx = 9;                   // x-coordinate of start point
                int sy = 217;                  // y-coordinate of start point
                int ex = 213;                  // x-coordinate of goal point
                int ey = 35;                   // y-coordinate of goal point
                return DefaultGenerator.generateUnseeded(sizeX, sizeY, unblockedRatio, sx, sy, ex, ey);
            }
            case 1 : { // SEEDED
                int unblockedRatio = 7;      // chance of spawning a cluster of blocked tiles is 1 in unblockedRatio.
                int seed = 12311;        // seed for the random.
                
                int sizeX = 60;              // x-axis size of grid
                int sizeY = 60;              // y-axis size of grid

                int sx = 12;                   // x-coordinate of start point
                int sy = 40;                  // y-coordinate of start point
                int ex = 52;                  // x-coordinate of goal point
                int ey = 34;                   // y-coordinate of goal point
                return DefaultGenerator.generateSeeded(seed, sizeX, sizeY, unblockedRatio, sx, sy, ex, ey);
            }
            case 2 :
                return GraphImporter.importGraphFromFile("maze.txt", 25, 17, 2, 9);
            case 3 :
                return DefaultGenerator.generateSeededOld(-98783479, 40, 40, 7, 1, 4, 18, 18); // maze 3
            case 4 :
                return DefaultGenerator.generateSeededOld(-565315494, 15, 15, 9, 1, 2, 1, 13); // maze 2
            case 5 :
                return DefaultGenerator.generateSeededOld(53, 15, 15, 9, 0, 0, 10, 14); // maze 1
            case 6 :
                return DefaultGenerator.generateSeededOld(-159182402, 15, 15, 9, 1, 1, 13, 12); // anya previously gave incorrect path
            case 7 :
                return GraphImporter.importGraphFromFile("maze14x11.txt", 0, 0, 10, 10); // Maze to contradict Theta* / A*
            case 8 :
                return GraphImporter.importGraphFromFile("mazeWCS.txt", 2, 0, 28, 25); // Worst Case Scenario path length.
            case 9 :
                return DefaultGenerator.generateSeededOld(-410889275, 15, 15, 7, 0, 1, 10, 12); // maze 4
            case 10 :
                return GraphImporter.importGraphFromFile("mazeThetaWCS.txt", 0, 0, 28, 13); // Worst Case Scenario for Theta*
            case 11 :
                return GraphImporter.importGraphFromFile("mazeReuseWCS.txt", 1, 28, 0, 27); // Worst Case Scenario for Visibility Graph reuse.
            case 12 :
                return GraphImporter.importGraphFromFile("anyaCont2.txt", 1, 6, 9, 1); // anya gives incorrect path
            case 13 :
                return DefaultGenerator.generateSeeded(-1155797147, 47, 32, 38, 46, 30, 20, 1); // issue for Strict Theta*
            case 14 :
                return DefaultGenerator.generateSeeded(-1155849806, 11, 13, 40, 7, 12, 9, 0); // Strict Theta* longer than Basic Theta*
            case 15 :
                return GraphImporter.loadStoredMaze("sc2_scrapstation", "81-16_138-51");
            default :
                return null;
        }
    }
    
    /**
     * Choose an algorithm.
     */
    static void setDefaultAlgoFunction() {
        int choice = 14; // adjust this to choose an algorithm
        
        switch (choice) {
            case 1 :
                algoFunction = (gridGraph, sx, sy, ex, ey) -> new AStar(gridGraph, sx, sy, ex, ey);
                break;
            case 2 :
                algoFunction = (gridGraph, sx, sy, ex, ey) -> new BreadthFirstSearch(gridGraph, sx, sy, ex, ey);
                break;
            case 3 :
                algoFunction = (gridGraph, sx, sy, ex, ey) -> BreadthFirstSearch.postSmooth(gridGraph, sx, sy, ex, ey);
                break;
            case 4 :
                algoFunction = (gridGraph, sx, sy, ex, ey) -> AStar.postSmooth(gridGraph, sx, sy, ex, ey);
                break;
            case 5 :
                algoFunction = (gridGraph, sx, sy, ex, ey) -> AStar.dijkstra(gridGraph, sx, sy, ex, ey);
                break;
            case 6 :
                algoFunction = (gridGraph, sx, sy, ex, ey) -> new Anya(gridGraph, sx, sy, ex, ey);
                break;
            case 7 :
                algoFunction = (gridGraph, sx, sy, ex, ey) -> new VisibilityGraphAlgorithm(gridGraph, sx, sy, ex, ey);
                break;
            case 8 :
                algoFunction = (gridGraph, sx, sy, ex, ey) -> new BasicThetaStar(gridGraph, sx, sy, ex, ey);
                break;
            case 9 :
                algoFunction = (gridGraph, sx, sy, ex, ey) -> BasicThetaStar.noHeuristic(gridGraph, sx, sy, ex, ey);
                break;
            case 10 :
                algoFunction = (gridGraph, sx, sy, ex, ey) -> BasicThetaStar.postSmooth(gridGraph, sx, sy, ex, ey);
                break;
            case 11 :
                algoFunction = (gridGraph, sx, sy, ex, ey) -> new AcceleratedAStar(gridGraph, sx, sy, ex, ey);
                break;
            case 12 :
                algoFunction = (gridGraph, sx, sy, ex, ey) -> VisibilityGraphAlgorithm.graphReuseNoHeuristic(gridGraph, sx, sy, ex, ey);
                break;
            case 13 :
                //algoFunction = null; // reserved
                algoFunction = (gridGraph, sx, sy, ex, ey) -> new AdjustmentThetaStar(gridGraph, sx, sy, ex, ey);
                break;
            case 14 :
                //algoFunction = null; // reserved
                algoFunction = (gridGraph, sx, sy, ex, ey) -> new StrictThetaStar(gridGraph, sx, sy, ex, ey);
                break;
            case 15 :
                //algoFunction = null; // reserved
                algoFunction = (gridGraph, sx, sy, ex, ey) -> StrictThetaStar.noHeuristic(gridGraph, sx, sy, ex, ey);
                break;
            case 16 :
                algoFunction = (gridGraph, sx, sy, ex, ey) -> BFSVisibilityGraph.graphReuse(gridGraph, sx, sy, ex, ey);
                break;
            case 17 :
                //algoFunction = null; // reserved
                algoFunction = (gridGraph, sx, sy, ex, ey) -> new RestrictedVisibilityGraphAlgorithm(gridGraph, sx, sy, ex, ey);
                break;
            case 18 :
                //algoFunction = null; // reserved
                algoFunction = (gridGraph, sx, sy, ex, ey) -> new StrictVisibilityGraphAlgorithm(gridGraph, sx, sy, ex, ey);
                break;
            case 19 :
                algoFunction = (gridGraph, sx, sy, ex, ey) -> new LazyThetaStar(gridGraph, sx, sy, ex, ey);
                break;
        }
    }

    interface AlgoFunction {
        public abstract PathFindingAlgorithm getAlgo(GridGraph gridGraph, int sx, int sy, int ex, int ey);
    }
}