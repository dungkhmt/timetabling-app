package openerp.openerpresourceserver.generaltimetabling.algorithms.mapdata;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ConnectedComponentSolver {

    List<ClassSegment> classSegments;
    List<List<ClassSegment>> connectedClassSegments;
    List<Integer>[] A; // A[i] is the list of classSegments having same group
    Map<Integer, List<Integer>> mGroupIndex2ClassSegmentIndex;
    boolean[] visited;
    private void dfs(int c, List<ClassSegment> CC){
        visited[c] = true; CC.add(classSegments.get(c));
        for(int ci: A[c]){
            if(!visited[ci]){
                dfs(ci,CC);
            }
        }
    }
    public List<List<ClassSegment>> computeConnectedComponent(List<ClassSegment> classSegments){
        this.classSegments = classSegments;

        mGroupIndex2ClassSegmentIndex = new HashMap<>();
        for(int i = 0; i < classSegments.size(); i++){
            ClassSegment c = classSegments.get(i);
            for(int g: c.getGroupIds()){
                if(mGroupIndex2ClassSegmentIndex.get(g) == null)
                    mGroupIndex2ClassSegmentIndex.put(g, new ArrayList<>());
                mGroupIndex2ClassSegmentIndex.get(g).add(i);
            }
        }
        A = new ArrayList[classSegments.size()];
        for(int i = 0; i < classSegments.size(); i++)
            A[i] = new ArrayList<>();
        for(int g: mGroupIndex2ClassSegmentIndex.keySet()){
            List<Integer> L = mGroupIndex2ClassSegmentIndex.get(g);
            if(L != null){
                for(int i = 0; i < L.size(); i++){
                    for(int j = i+1; j < L.size(); j++){
                        int ni = L.get(i); int nj = L.get(j);
                        A[ni].add(nj);
                        A[nj].add(ni);
                    }
                }
            }
        }
        visited = new boolean[classSegments.size()];
        connectedClassSegments = new ArrayList<>();
        for(int i = 0; i < classSegments.size(); i++) visited[i] = false;
        for(int i = 0; i < classSegments.size(); i++){
            if(!visited[i]){
                List<ClassSegment> CC = new ArrayList<>();
                dfs(i,CC);
                connectedClassSegments.add(CC);
            }
        }
        return connectedClassSegments;
    }
    public static void main(String[] args){

    }
}
