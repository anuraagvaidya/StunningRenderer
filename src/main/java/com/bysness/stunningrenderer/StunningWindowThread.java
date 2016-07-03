package com.bysness.stunningrenderer;

import org.eclipse.swt.widgets.Display;

import java.awt.*;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Created by Anuraag on 7/3/2016.
 */
public class StunningWindowThread extends Thread {
    Thread t;
    final CopyOnWriteArrayList<String> commands;
    private final ConcurrentHashMap<String,StunningWindow> windows;
    private boolean stopped=false;
    StunningWindowThread()
    {
        commands=new CopyOnWriteArrayList<String>();
        windows=new ConcurrentHashMap<String,StunningWindow>();
    }
    String surround(String str)
    {
        return "\"" + str + "\"";
    }
    String keyValueStr(String key,String value)
    {
        return surround(key) + ":" + surround(value);
    }
    void API_createWindow(String windowId)
    {
        StunningWindow window=new StunningWindow();
        windows.put(windowId,window);
        API_Respond(keyValueStr("event","create") + "," + keyValueStr("object","window") + "," + keyValueStr("windowId",windowId));
    }
    void API_createTool(String windowId,String parentId,String toolId,String type)
    {
        if(windows.containsKey(windowId))
        {
            StunningWindow window=windows.get(windowId);
            window.addComponent(toolId,parentId);
            API_Respond(keyValueStr("event","create") + "," + keyValueStr("object","tool") + "," + keyValueStr("windowId",windowId) + "," + keyValueStr("toolId",toolId));
        }
        else
        {
            API_throwError("Incorrect window_id");
        }
    }
    void API_setStyleWindow(String windowId,String param,String value)
    {
        if(windows.containsKey(windowId))
        {
            StunningWindow window=windows.get(windowId);
            if(param.equals("title"))
            {
                window.design_frame.setTitle(value);
            }
            else if(param.equals("width"))
            {
                window.design_frame.setSize(Integer.parseInt(value),window.design_frame.getHeight());
            }
            else if(param.equals("height"))
            {
                window.design_frame.setSize(window.design_frame.getWidth(),Integer.parseInt(value));
            }
            API_Respond(keyValueStr("event","style_applied") + "," + keyValueStr("object","window") + "," + keyValueStr("windowId",windowId) + "," + keyValueStr("key",param) + "," + keyValueStr("value",value));
        }
        else
        {
            API_throwError("Incorrect window_id");
        }
    }
    void API_setStyleTool(String windowId,String toolId,String param,String value)
    {
        if(windows.containsKey(windowId))
        {
            StunningWindow window=windows.get(windowId);
            if(window.tools.containsKey(toolId))
            {
                StunningUI_Rectangle rect=window.tools.get(toolId);
                if(param.equals("foregroundColor"))
                {
                    rect.style.foregroundColor=Color.decode(value);
                }
                else if(param.equals("width"))
                {
                    rect.style.width=Integer.parseInt(value);
                }
                else if(param.equals("height"))
                {
                    rect.style.height=Integer.parseInt(value);
                }
                else if(param.equals("left"))
                {
                    rect.style.left=Integer.parseInt(value);
                }
                else if(param.equals("top"))
                {
                    rect.style.top=Integer.parseInt(value);
                }
                API_Respond(keyValueStr("event","style_applied") + "," + keyValueStr("object","tool") + "," + keyValueStr("windowId",windowId) +"," + keyValueStr("toolId",toolId) + "," + keyValueStr("key",param) + "," + keyValueStr("value",value));
                window.forceRender();
            }
            else
            {
                API_throwError("Incorrect toolId");
            }
        }
        else
        {
            API_throwError("Incorrect window_id");
        }
    }
    void API_throwError(String error)
    {
        System.out.println("{error:1,msg:\"" + error + "\"}");
    }
    void API_Respond(String response)
    {
        System.out.println("{ " + response + " }");
    }
    void decodeCommands()
    {
        for(int i=0;i<commands.size();i++)
        {
            String[] command=commands.get(i).split(":");
            if(command[0].equals("createwindow"))
            {
                if(command.length<2)
                {
                    API_throwError("Usage: createwindow:window_id");
                }
                else
                {
                    API_createWindow(command[1]);
                }
            }
            else if(command[0].equals("createtool"))
            {
                if(command.length<5)
                {
                    API_throwError("Usage: createtool:window_id:parentId:toolId:type");
                }
                else
                {
                    API_createTool(command[1],command[2],command[3],command[4]);
                }
            }
            else if(command[0].equals("windowstyle"))
            {
                if(command.length<4)
                {
                    API_throwError("Usage: windowstyle:window_id:param:value");
                }
                else
                {
                    API_setStyleWindow(command[1],command[2],command[3]);
                }
            }
            else if(command[0].equals("toolstyle"))
            {
                if(command.length<5)
                {
                    API_throwError("Usage: toolstyle:window_id:tool_id:param:value");
                }
                else
                {
                    API_setStyleTool(command[1],command[2],command[3],command[4]);
                }
            }
            else if(command[0].equals("exit"))
            {
                stopped=true;
                break;
            }
            commands.remove(i);
            i--;
        }
    }
    @Override
    public void run()
    {
        while (!stopped)
        {
            decodeCommands();
//            checkWindowStatus();
        }
        System.exit(0);
    }

    @Override
    public void start ()
    {
        t = new Thread (this, "StunningWindow");
        t.start();
    }
}
