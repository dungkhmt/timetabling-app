package openerp.openerpresourceserver.examtimetabling.algorithm.mapdata;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class MapDataRoom {
    private int id;
    private String code;
    private int capacity;
}
