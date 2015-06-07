package algorithms;

import algorithms.priorityqueue.IndirectHeap;
import grid.GridGraph;

/**
 * An modification of Theta* that I am experimenting with. -Oh
 * @author Oh
 * 
 * Ideas:
 * Heuristic trap:
 *  - The heuristic value of the final node is 1.1f instead of 0.
 *  - A lot of inoptimality comes because the algorithm is too eager to relax
 *    the final vertex. The slightly higher heuristic encourages the algorithm
 *    to explore a little more first.
 *
 */
public class StrictThetaStar extends BasicThetaStar {

    private static final float BUFFER_VALUE = 1f;

    public StrictThetaStar(GridGraph graph, int sx, int sy, int ex, int ey) {
        super(graph, sx, sy, ex, ey);
        this.postSmoothingOn = true;
    }
    
    public static StrictThetaStar noHeuristic(GridGraph graph, int sx, int sy, int ex, int ey) {
        StrictThetaStar algo = new StrictThetaStar(graph, sx, sy, ex, ey);
        algo.heuristicWeight = 0f;
        return algo;
    }

    @Override
    public void computePath() {
        int totalSize = (graph.sizeX+1) * (graph.sizeY+1);

        int start = toOneDimIndex(sx, sy);
        finish = toOneDimIndex(ex, ey);
        
        distance = new Float[totalSize];
        parent = new int[totalSize];
        
        initialise(start);
        visited = new boolean[totalSize];
        
        pq = new IndirectHeap<Float>(distance, true);
        pq.heapify();
        
        while (!pq.isEmpty()) {
            int current = pq.popMinIndex();
            tryFixBufferValue(current);
            
            if (current == finish || distance[current] == Float.POSITIVE_INFINITY) {
                maybeSaveSearchSnapshot();
                break;
            }
            visited[current] = true;

            int x = toTwoDimX(current);
            int y = toTwoDimY(current);
            

            tryRelaxNeighbour(current, x, y, x-1, y-1);
            tryRelaxNeighbour(current, x, y, x, y-1);
            tryRelaxNeighbour(current, x, y, x+1, y-1);
            
            tryRelaxNeighbour(current, x, y, x-1, y);
            tryRelaxNeighbour(current, x, y, x+1, y);
            
            tryRelaxNeighbour(current, x, y, x-1, y+1);
            tryRelaxNeighbour(current, x, y, x, y+1);
            tryRelaxNeighbour(current, x, y, x+1, y+1);

            maybeSaveSearchSnapshot();
        }
        
        maybePostSmooth();
    }
    
    protected float heuristic(int x, int y) {
        if (x == ex && y == ey) {
            return 1.1f;
        } else { 
            return heuristicWeight*graph.distance(x, y, ex, ey);
        }
    }

    private void tryFixBufferValue(int current) {
        if (parent[current] < 0 && parent[current] != -1) {
            //System.out.println("FIXCOUNT " + fixCount + " OUT OF " + outOf);

            parent[current] -= Integer.MIN_VALUE;
            distance[current] = distance[parent[current]] + physicalDistance(current, parent[current]);
            
            //parent[current] = wrap(current);
            
            /*int target = parent[current];
            while (target != -1) {
                if (isTaut(current, target) && lineOfSight(current, target)) {
                    distance[current] = distance[target] + physicalDistance(current, target);
                    parent[current] = target;
                    return;
                }
                target = parent[target];
            }*/
        }
    }
    
    private int wrap(int v) {
        int u = parent[v];
        int p = parent[u];
        // We can assume that v has no line of sight to p.
        
        return u;
    }
    
    private boolean tryLocateTautParent(int current, int from) {
        int u = parent[from];
        if (isTaut(current, u)) {
            if (lineOfSight(current, u)) {
                parent[current] = u;
                distance[current] = distance[u] + physicalDistance(current, u);
                return true;
            }
        }
        return false;
    }

    protected void tryRelaxNeighbour(int current, int currentX, int currentY, int x, int y) {
        if (!graph.isValidCoordinate(x, y))
            return;
        
        int destination = toOneDimIndex(x,y);
        if (visited[destination])
            return;
        if (parent[current] != -1 && parent[current] == parent[destination]) // OPTIMISATION: [TI]
            return; // Idea: don't bother trying to relax if parents are equal. using triangle inequality.
        if (!graph.neighbourLineOfSight(currentX, currentY, x, y))
            return;
        
        if (relax(current, destination, weight(currentX, currentY, x, y))) {
            // If relaxation is done.
            pq.decreaseKey(destination, distance[destination] + heuristic(x,y));
        }
    }
    
    @Override
    protected boolean relax(int u, int v, float weightUV) {
        // return true iff relaxation is done.
        float tempWeight = Float.POSITIVE_INFINITY;
        int tempParent = -1;
        
        if (lineOfSight(parent[u], v)) {
            int u2 = parent[u];
            
            float newWeight = distance[u2] + physicalDistance(u2, v);
            if (newWeight < distance[v]) {
                if (isTaut(v, u2)) {
                    distance[v] = newWeight;
                    parent[v] = u2;
                    return true;
                } else if (tryLocateTautParent(v, u2)) {
                    return true;
                } else {
                    newWeight += BUFFER_VALUE;
                    if (newWeight < distance[v]) {
                        tempWeight = newWeight;
                        tempParent = u2;
                    }
                }
                // else don't return.
            } else {
                return false;
            }
        } 
        { // if haven't returned,
            float newWeight = distance[u] + weightUV;
            if (newWeight < distance[v]) {
                if (isTaut(v, u)) {
                    distance[v] = newWeight;
                    parent[v] = u;
                    return true;
                } else if (tryLocateTautParent(v, u)) {
                    return true;
                } else {
                    newWeight += BUFFER_VALUE;
                    if (newWeight < tempWeight && newWeight < distance[v]) {
                        tempWeight = newWeight;
                        //tempWeight = newWeight;
                        tempParent = u;
                    }
                    /*distance[v] = newWeight * BUFFER_VALUE;
                    parent[v] = Integer.MIN_VALUE+u;
                    return true;*/
                    /*distance[v] = newWeight*2;
                    parent[v] = Integer.MIN_VALUE+u;
                    return true;*/
                    //return false;
                }
            }
        }
        
        if (tempParent != -1) {
            //distance[v] = distance[tempParent] + physicalDistance(u, v);
            distance[v] = tempWeight;
            parent[v] = Integer.MIN_VALUE+tempParent;
            return true;
        }
        
        return false;
    }

    /**
     * Checks whether the path v, u, p=parent[u] is taut.
     */
    private boolean isTaut(int v, int u) {
        int p = parent[u]; // assert u != -1
        if (p == -1) return true;
        int x1 = toTwoDimX(v);
        int y1 = toTwoDimY(v);
        int x2 = toTwoDimX(u);
        int y2 = toTwoDimY(u);
        int x3 = toTwoDimX(p);
        int y3 = toTwoDimY(p);
        
        if (x1 < x2) {
            if (y1 < y2) {
                return isTautFromBottomLeft(x1, y1, x2, y2, x3, y3);
            } else if (y2 < y1) {
                return isTautFromTopLeft(x1, y1, x2, y2, x3, y3);
            } else { // y1 == y2
                return isTautFromLeft(x1, y1, x2, y2, x3, y3);
            }
        } else if (x2 < x1) {
            if (y1 < y2) {
                return isTautFromBottomRight(x1, y1, x2, y2, x3, y3);
            } else if (y2 < y1) {
                return isTautFromTopRight(x1, y1, x2, y2, x3, y3);
            } else { // y1 == y2
                return isTautFromRight(x1, y1, x2, y2, x3, y3);
            }
        } else { // x2 == x1
            if (y1 < y2) {
                return isTautFromBottom(x1, y1, x2, y2, x3, y3);
            } else if (y2 < y1) {
                return isTautFromTop(x1, y1, x2, y2, x3, y3);
            } else { // y1 == y2
                throw new UnsupportedOperationException("v == u?");
            }
        }
    }

    
    private boolean isTautFromBottomLeft(int x1, int y1, int x2, int y2, int x3,
            int y3) {
        if (x3 < x2 || y3 < y2) return false;
        
        int compareGradients = (y2-y1)*(x3-x2) - (y3-y2)*(x2-x1); // m1 - m2
        if (compareGradients < 0) { // m1 < m2
            return bottomRightOfBlockedTile(x2, y2);
        } else if (compareGradients > 0) { // m1 > m2
            return topLeftOfBlockedTile(x2, y2);
        } else { // m1 == m2
            return true;
        }
    }

    
    private boolean isTautFromTopLeft(int x1, int y1, int x2, int y2, int x3,
            int y3) {
        if (x3 < x2 || y3 > y2) return false;
        
        int compareGradients = (y2-y1)*(x3-x2) - (y3-y2)*(x2-x1); // m1 - m2
        if (compareGradients < 0) { // m1 < m2
            return bottomLeftOfBlockedTile(x2, y2);
        } else if (compareGradients > 0) { // m1 > m2
            return topRightOfBlockedTile(x2, y2);
        } else { // m1 == m2
            return true;
        }
    }
    
    private boolean isTautFromBottomRight(int x1, int y1, int x2, int y2, int x3,
            int y3) {
        if (x3 > x2 || y3 < y2) return false;
        int compareGradients = (y2-y1)*(x3-x2) - (y3-y2)*(x2-x1); // m1 - m2
        if (compareGradients < 0) { // m1 < m2
            return topRightOfBlockedTile(x2, y2);
        } else if (compareGradients > 0) { // m1 > m2
            return bottomLeftOfBlockedTile(x2, y2);
        } else { // m1 == m2
            return true;
        }
    }

    
    private boolean isTautFromTopRight(int x1, int y1, int x2, int y2, int x3,
            int y3) {
        if (x3 > x2 || y3 > y2) return false;
        
        int compareGradients = (y2-y1)*(x3-x2) - (y3-y2)*(x2-x1); // m1 - m2
        if (compareGradients < 0) { // m1 < m2
            return topLeftOfBlockedTile(x2, y2);
        } else if (compareGradients > 0) { // m1 > m2
            return bottomRightOfBlockedTile(x2, y2);
        } else { // m1 == m2
            return true;
        }
    }

    
    private boolean isTautFromLeft(int x1, int y1, int x2, int y2, int x3,
            int y3) {
        if (x3 < x2) return false;
        
        int dy = y3 - y2;
        if (dy < 0) { // y3 < y2
            return topRightOfBlockedTile(x2, y2);
        } else if (dy > 0) { // y3 > y2
            return bottomRightOfBlockedTile(x2, y2);
        } else { // y3 == y2
            return true;
        }
    }

    private boolean isTautFromRight(int x1, int y1, int x2, int y2, int x3,
            int y3) {
        if (x3 > x2) return false;
        
        int dy = y3 - y2;
        if (dy < 0) { // y3 < y2
            return topLeftOfBlockedTile(x2, y2);
        } else if (dy > 0) { // y3 > y2
            return bottomLeftOfBlockedTile(x2, y2);
        } else { // y3 == y2
            return true;
        }
    }

    private boolean isTautFromBottom(int x1, int y1, int x2, int y2, int x3,
            int y3) {
        if (y3 < y2) return false;
        
        int dx = x3 - x2;
        if (dx < 0) { // x3 < x2
            return topRightOfBlockedTile(x2, y2);
        } else if (dx > 0) { // x3 > x2
            return topLeftOfBlockedTile(x2, y2);
        } else { // x3 == x2
            return true;
        }
    }

    private boolean isTautFromTop(int x1, int y1, int x2, int y2, int x3,
            int y3) {
        if (y3 > y2) return false;
        
        int dx = x3 - x2;
        if (dx < 0) { // x3 < x2
            return bottomRightOfBlockedTile(x2, y2);
        } else if (dx > 0) { // x3 > x2
            return bottomLeftOfBlockedTile(x2, y2);
        } else { // x3 == x2
            return true;
        }
    }


    
    private boolean topRightOfBlockedTile(int x, int y) {
        return graph.isBlocked(x-1, y-1);
    }

    private boolean topLeftOfBlockedTile(int x, int y) {
        return graph.isBlocked(x, y-1);
    }

    private boolean bottomRightOfBlockedTile(int x, int y) {
        return graph.isBlocked(x-1, y);
    }

    private boolean bottomLeftOfBlockedTile(int x, int y) {
        return graph.isBlocked(x, y);
    }

}
