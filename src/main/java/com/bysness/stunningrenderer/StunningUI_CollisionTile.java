package com.bysness.stunningrenderer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;

/**
 * Created by Anuraag on 5/2/2016.
 */
public class StunningUI_CollisionTile {
    ArrayList<StunningUI_Sprite> claimants;
    int x;
    int y;

    StunningUI_CollisionTile(int x, int y)
    {
        this.x=x;
        this.y=y;
        claimants=new ArrayList<StunningUI_Sprite>();
    }
    void addClaimant(StunningUI_Sprite s){
        claimants.add(s);
    }
    void deleteClaimant(StunningUI_Sprite s){
        claimants.remove(s);
    }
}
