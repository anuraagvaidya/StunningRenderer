package com.bysness.stunningrenderer;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import swt.SWTResourceManager;

/**
 * Created by Anuraag on 7/3/2016.
 */
public class StunningWindow extends Shell {
    Display display;
    StunningWindow(Display display) {
        super(display, SWT.SHELL_TRIM);
        setBackground(SWTResourceManager.getColor(93,93,93));
        setLayout(null);
    }
    @Override
    protected void checkSubclass() {
        // Disable the check that prevents subclassing of SWT components
    }
}
