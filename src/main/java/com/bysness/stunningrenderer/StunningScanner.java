package com.bysness.stunningrenderer;

import org.eclipse.swt.widgets.Display;

import java.util.Scanner;

/**
 * Created by Anuraag on 7/3/2016.
 */
public class StunningScanner extends Thread {
    private Thread t;
    private StunningWindowThread windowThread;
    private Scanner scanner;
    StunningScanner(){
        windowThread=new StunningWindowThread();
    }

    void queueCommand(String command)
    {
        synchronized (windowThread.commands){
            windowThread.commands.add(command);
        }
    }
    void decodeCommand(Scanner scanner)
    {
        while(scanner.hasNext())
        {
            String command=scanner.next();
            System.out.println("{\"status\":\"ready\"}");
            queueCommand(command);
        }
    }
    @Override
    public void run()
    {
        decodeCommand(scanner);
    }

    @Override
    public void start ()
    {
        t = new Thread (this, "Scanner");
        scanner = new Scanner(System.in);
        System.out.println("{\"status\":\"ready\"}");
        t.start();
        windowThread.start();
    }

}