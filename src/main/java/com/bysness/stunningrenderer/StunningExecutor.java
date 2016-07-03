package com.bysness.stunningrenderer;

import org.eclipse.swt.widgets.Display;

import java.util.ArrayList;
import java.util.Scanner;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Created by Anuraag on 7/3/2016.
 */
public class StunningExecutor extends Thread
{

    private Thread t;
    final CopyOnWriteArrayList<String> commands;
    StunningWindowThread windowThread;
    private boolean stopped=false;
    StunningExecutor()
    {
        commands=new CopyOnWriteArrayList<String>();
        windowThread=new StunningWindowThread();
    }
    void API_createWindow(){
        synchronized (windowThread.commands)
        {
            windowThread.commands.add("createwindow");
        }
    }
    void decodeCommands(){
        for(int i=0;i<commands.size();i++)
        {
            if(commands.get(i).equals("createwindow"))
            {
                API_createWindow();
            }
            if(commands.get(i).equals("exit"))
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
        while(!stopped)
        {
            decodeCommands();
        }
    }

    @Override
    public void start ()
    {
        t = new Thread (this, "Executor");
        t.start();
        windowThread.start();
    }
}
