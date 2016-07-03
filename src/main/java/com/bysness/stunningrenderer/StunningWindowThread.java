package com.bysness.stunningrenderer;

import org.eclipse.swt.widgets.Display;

import java.util.ArrayList;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Created by Anuraag on 7/3/2016.
 */
public class StunningWindowThread extends Thread {
    Thread t;
    final CopyOnWriteArrayList<String> commands;
    private final ArrayList<StunningWindow> windows;
    private boolean stopped=false;
    StunningWindowThread()
    {
        commands=new CopyOnWriteArrayList<String>();
        windows=new ArrayList<StunningWindow>();
    }
    void API_createWindow()
    {
        Display display = Display.getDefault();
        StunningWindow window=new StunningWindow(display);
        windows.add(window);
        window.open();
        window.layout();
    }
    void decodeCommands()
    {
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
    void checkWindowStatus()
    {
        final Display display = Display.getDefault();
        for(int i=0;i<windows.size();i++)
        {
            final int index = i;
            display.syncExec(new Runnable () {
                public void run ()
                {
                    if(!windows.get(index).isDisposed()) {
                        windows.get(index).redraw();
                        if (!display.readAndDispatch()) {
                            display.sleep();
                        }
                    }
                }
            });

        }
    }
    @Override
    public void run()
    {
        while (!stopped)
        {
            decodeCommands();
            checkWindowStatus();
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
