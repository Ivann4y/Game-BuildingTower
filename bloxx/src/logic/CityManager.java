package logic;

import data.*;
import java.awt.Point;
import java.util.*;

public class CityManager {
    public Map<Point, FinishedBuilding> cityGrid = new HashMap<>();
    public Map<Point, List<Point>> cityAdjacencyGraph = new HashMap<>();
    public final int CITY_WIDTH=5, CITY_HEIGHT=4;
    public Point nextCityPlot = new Point(0,0);

    public CityManager() { buildGraph(); }

    private void buildGraph() {
        for(int y=0; y<CITY_HEIGHT; y++)
            for(int x=0; x<CITY_WIDTH; x++) {
                Point p=new Point(x,y);
                cityAdjacencyGraph.putIfAbsent(p, new ArrayList<>());
                int[] dx={0,0,1,-1}, dy={1,-1,0,0};
                for(int i=0;i<4;i++) {
                    Point np=new Point(x+dx[i],y+dy[i]);
                    if(np.x>=0 && np.x<CITY_WIDTH && np.y>=0 && np.y<CITY_HEIGHT)
                        cityAdjacencyGraph.get(p).add(np);
                }
            }
    }

    public long calculateSynergyBonus(Point position) {
        FinishedBuilding source = cityGrid.get(position);
        if(source==null) return 0;
        long bonus=0;
        for(Point neighbor: cityAdjacencyGraph.get(position)) {
            FinishedBuilding nb = cityGrid.get(neighbor);
            if(nb!=null) {
                if(source.type==BlockType.PERUMAHAN && nb.type==BlockType.TAMAN) bonus+=250;
                if(source.type==BlockType.BISNIS && nb.type==BlockType.PERUMAHAN) bonus+=150;
            }
        }
        return bonus;
    }
}
