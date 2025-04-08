package openerp.openerpresourceserver.generaltimetabling.algorithms.mapdata;

import openerp.openerpresourceserver.generaltimetabling.model.entity.ClassGroup;
import openerp.openerpresourceserver.generaltimetabling.model.entity.general.GeneralClass;
import openerp.openerpresourceserver.generaltimetabling.model.entity.general.TimeTablingClass;

import java.sql.Time;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ConnectedComponentClassSolver {
    List<TimeTablingClass> cls;
    List<ClassGroup> classGroups;
    int n;
    List<Integer>[] A;// Adjacent list
    boolean[] visited;

    public List<List<TimeTablingClass>> computeConnectedComponents(List<TimeTablingClass> cls,List<ClassGroup> classGroups){
        this.cls = cls;
        this.classGroups = classGroups;
        Map<Long, List<Long>> mGroupId2ClassIds = new HashMap<>();

        for(ClassGroup cg: classGroups){
            if(mGroupId2ClassIds.get(cg.getGroupId())==null)
                mGroupId2ClassIds.put(cg.getGroupId(),new ArrayList<>());
            mGroupId2ClassIds.get(cg.getGroupId()).add(cg.getClassId());
        }
        Map<Long, Integer> mClassId2Index = new HashMap<>();
        for(int i = 0; i < this.cls.size(); i++)
            mClassId2Index.put(cls.get(i).getId(),i);
        n = cls.size();
        A = new ArrayList[n];
        for(int i = 0; i < n; i++) A[i] = new ArrayList<>();
        for(Long gId: mGroupId2ClassIds.keySet()){
            List<Long> L = mGroupId2ClassIds.get(gId);
            for(int i = 0; i < L.size(); i++){
                for(int j = i+1; j < L.size(); j++){
                    Long ri = L.get(i);
                    Long rj = L.get(j);
                    int i1 = mClassId2Index.get(ri);
                    int i2 = mClassId2Index.get(rj);
                    A[i1].add(i2);
                    A[i2].add(i1);
                }
            }
        }
        visited = new boolean[n];
        for(int i = 0; i < n; i++) visited[i] = false;
        List<List<TimeTablingClass>> res = new ArrayList<>();
        for(int i = 0; i < n; i++){
            if(visited[i] == false){
                List<TimeTablingClass> CC = new ArrayList<>();
                dfs(i,CC);
                res.add(CC);
            }
        }
        return res;
    }
    private void dfs(int i, List<TimeTablingClass> CC){
        visited[i] = true;
        CC.add(cls.get(i));
        for(int j: A[i]){
            if(!visited[j]){
                dfs(j,CC);
            }
        }
    }
}
