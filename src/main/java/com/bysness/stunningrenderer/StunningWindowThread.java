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
    final CopyOnWriteArrayList<String> responses;
    private final ConcurrentHashMap<String,StunningWindow> windows;
    private boolean stopped=false;
    StunningWindowThread()
    {
        commands=new CopyOnWriteArrayList<String>();
        responses=new CopyOnWriteArrayList<String>();
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
        StunningWindow window=new StunningWindow(windowId,responses);
        windows.put(windowId,window);
        API_Respond(keyValueStr("event","create") + "," + keyValueStr("object","window") + "," + keyValueStr("windowId",windowId));
    }
    void API_createTool(String windowId,String parentId,String toolId,String type,String paramAndValuesStr)
    {
        if(windows.containsKey(windowId))
        {
            if(type.equals("rectangle"))
            {
                StunningWindow window=windows.get(windowId);
                if(!window.tools.containsKey(toolId))
                {
                    window.addRectangle(toolId,parentId,paramAndValuesStr);
                    API_Respond(keyValueStr("event","create") + "," + keyValueStr("object","tool") + "," + keyValueStr("windowId",windowId) + "," + keyValueStr("toolId",toolId));
                }
                else
                {
                    API_throwError("Tool already exists");
                }
            }
        }
        else
        {
            API_throwError("Incorrect window_id");
        }
    }
    void API_setStyleWindow(String windowId,String paramAndValuesStr)
    {
        if(windows.containsKey(windowId))
        {
            String[] paramAndValues=paramAndValuesStr.split("&");
            for(int j=0;j<paramAndValues.length;j++)
            {
                String[] paramAndValue=paramAndValues[j].split("=");
                String param=paramAndValue[0];
                String value=paramAndValue[1];
                value=value.replaceAll("\\$"," ");
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
                else if(param.equals("state"))
                {
                    if(value.equals("maximized"))
                    {
                        window.design_frame.setState(Frame.MAXIMIZED_BOTH);
                    }
                    else if(value.equals("minimized"))
                    {
                        window.design_frame.setState(Frame.ICONIFIED);
                    }
                    else if(value.equals("normal"))
                    {
                        window.design_frame.setState(Frame.NORMAL);
                    }
                }
            }
            API_Respond(keyValueStr("event","style_applied") + "," + keyValueStr("object","window") + "," + keyValueStr("windowId",windowId) + "," + keyValueStr("values",paramAndValuesStr));
        }
        else
        {
            API_throwError("Incorrect window_id");
        }
    }
    void API_setStyleTool(String windowId,String toolId,String paramAndValuesStr)
    {
        if(windows.containsKey(windowId))
        {
            StunningWindow window=windows.get(windowId);
            if(window.tools.containsKey(toolId))
            {
                window.setToolStyle(toolId,paramAndValuesStr);
                API_Respond(keyValueStr("event","style_applied") + "," + keyValueStr("object","tool") + "," + keyValueStr("windowId",windowId) +"," + keyValueStr("toolId",toolId) + "," + keyValueStr("values",paramAndValuesStr));
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
    void API_attachEvent(String windowId,String toolId,String event)
    {
        if(windows.containsKey(windowId))
        {
            StunningWindow window=windows.get(windowId);
            if(window.tools.containsKey(toolId))
            {
                window.attachEvent(toolId,event);
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
                if(command.length<6)
                {
                    API_throwError("Usage: createtool:window_id:parentId:toolId:type:params");
                }
                else
                {
                    API_createTool(command[1],command[2],command[3],command[4],command[5]);
                }
            }
            else if(command[0].equals("windowstyle"))
            {
                if(command.length<3)
                {
                    API_throwError("Usage: windowstyle:window_id:param1=value1&param2=value2");
                }
                else
                {
                    API_setStyleWindow(command[1],command[2]);
                }
            }
            else if(command[0].equals("toolstyle"))
            {
                if(command.length<4)
                {
                    API_throwError("Usage: toolstyle:window_id:tool_id:param1=value1&param2=value2");
                }
                else
                {
                    API_setStyleTool(command[1],command[2],command[3]);
                }
            }
            else if(command[0].equals("attachevent"))
            {
                if(command.length<4)
                {
                    API_throwError("Usage: attachevent:window_id:tool_id:event");
                }
                else
                {
                    API_attachEvent(command[1],command[2],command[3]);
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

    void pushResponses()
    {
        synchronized (responses)
        {
            for(int i=0;i<responses.size();i++)
            {
                System.out.println(responses.get(i));
                responses.remove(i);
                i--;
            }
        }
    }
    @Override
    public void run()
    {
        while (!stopped)
        {
            decodeCommands();
            pushResponses();
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
