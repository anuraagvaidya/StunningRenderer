package com.bysness.stunningrenderer;

import com.jogamp.opengl.GLAutoDrawable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;

/**
 * Created by Anuraag on 5/2/2016.
 */
public class StunningUI_CollisionMap {
    float tilesPerLine=50;
    float tileWidth=0;
    float tileHeight=0;
    int tileInternalWidth=0;
    int tileInternalHeight=0;
    ArrayList<StunningUI_CollisionTile> tiles;
    boolean created=false;
    int surfaceWidth;
    int surfaceHeight;
    StunningUI_CollisionMap(){
        tiles=new ArrayList<StunningUI_CollisionTile>();
    }
    void create(GLAutoDrawable drawable){
        tiles=new ArrayList<StunningUI_CollisionTile>();
        recreate(drawable);
    }
    void recreate(GLAutoDrawable drawable){
        tiles.clear();
        surfaceWidth=drawable.getSurfaceWidth();
        surfaceHeight=drawable.getSurfaceHeight();
        tileWidth=surfaceWidth/tilesPerLine;
        tileHeight=surfaceHeight/tilesPerLine;
        for(int i=0;i<tilesPerLine;i++)
        {
            for(int j=0;j<tilesPerLine;j++)
            {
                tiles.add(new StunningUI_CollisionTile(j*tileInternalWidth,i*tileInternalHeight));
            }
        }
        created=true;
    }
    ArrayList<StunningUI_Sprite> findComponents(float x, float y)
    {
        int tileX=(int)(tilesPerLine/(surfaceWidth / x));
        int tileY=(int)(tilesPerLine/(surfaceHeight / y));
        System.out.println("Finding components in " + tileX + "," + tileY + ", surfW: " + surfaceWidth + ", surfH: " + surfaceHeight);
        if(tileX + ((int)tilesPerLine * tileY)>=0 && (tileX + ((int)tilesPerLine * tileY))<tiles.size())
        {
            System.out.println("Found " + tiles.get(tileX + ((int)tilesPerLine * tileY)).claimants.size() + " components in");
            return tiles.get(tileX + ((int)tilesPerLine * tileY)).claimants;
        }
        return null;
    }

    void deleteComponent(StunningUI_Sprite sprite){
        for(int i=0;i<sprite.children.size();i++)
        {
            deleteComponent(sprite.children.get(i));
        }
        for(int i=0;i<sprite.tiles.size();i++)
        {
            if(sprite.tiles.get(i)>=0 && sprite.tiles.get(i)<tiles.size())
            {
                tiles.get(sprite.tiles.get(i)).deleteClaimant(sprite);
            }
        }
    }
    void updateComponentChildren(StunningUI_Sprite sprite){
        for(int i=0;i<sprite.children.size();i++)
        {
            updateComponent(sprite.children.get(i));
        }
    }
    void updateComponent(StunningUI_Sprite sprite)
    {
        deleteComponent(sprite);
        addComponent(sprite);

        updateComponentChildren(sprite);
    }
    void addComponent(StunningUI_Sprite sprite)
    {
//        int tileX0=(int)(tilesPerLine/((float)surfaceWidth / (float)sprite.style.totalLeft));
//        int tileY0=(int)(tilesPerLine/((float)surfaceHeight / (float)(sprite.style.totalTop + sprite.parent.style.scrollTop)));
//
//        int tileX1=(int)(tilesPerLine/((float)surfaceWidth/((float)sprite.style.totalLeft + (float)sprite.style.width)));
//        int tileY1=(int)(tilesPerLine/((float)surfaceHeight/((float)(sprite.style.totalTop + sprite.parent.style.scrollTop) + (float)sprite.style.height)));

        float currentZoom=sprite.parent.style.zoom!=1f?sprite.parent.style.zoom:sprite.style.zoom;
        int tileX0=(int)(tilesPerLine/((float)surfaceWidth / (float)sprite.style.totalLeft));
        int tileY0=(int)(tilesPerLine/((float)surfaceHeight / (float)(sprite.style.totalTop + (currentZoom*(float)sprite.parent.style.scrollTop))));

        int tileX1=(int)(tilesPerLine/((float)surfaceWidth/((float)sprite.style.totalLeft + (currentZoom*(float)sprite.style.width))));
        int tileY1=(int)(tilesPerLine/((float)surfaceHeight/((float)(sprite.style.totalTop) + (currentZoom*(float)(sprite.style.height+sprite.parent.style.scrollTop)))));

        if(sprite.tag.length()>0)
        {
            System.out.println("Tiles for " + sprite.tag + "-> x0:" + tileX0 + ",y0:" + tileY0 + ",x1:" + tileX1 + ",y1:" + tileY1 + ",zoom:" + currentZoom);
        }
        for(int i=tileX0;i<=tileX1;i++)
        {
            for(int j=tileY0;j<=tileY1;j++)
            {
                if((i + ((int)tilesPerLine * j))>=0 && (i + ((int)tilesPerLine * j))<tiles.size())
                {
                    sprite.tiles.add(i + ((int)tilesPerLine * j));
                    tiles.get(i + ((int)tilesPerLine * j)).addClaimant(sprite);
                }
            }
        }
    }

    void dispose(){
        tiles.clear();
        tiles=null;
    }
}
