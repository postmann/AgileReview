/**
 * Copyright (c) 2011, 2012 AgileReview Development Team and others.
 * All rights reserved. This program and the accompanying materials are made available under
 * the terms of the Eclipse Public License - v 1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 * Contributors: Malte Brunnlieb, Philipp Diebold, Peter Reuter, Thilo Rauch
 */
package org.agilereview.ui.basic.commentSummary;

import org.agilereview.ui.basic.commentSummary.filter.SearchFilter;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.MenuItem;

/**
 * This {@link ViewController} handles all events related to the CommentSummaryView
 * @author Malte Brunnlieb (28.04.2012)
 */
public class ViewController implements Listener, KeyListener, ISelectionChangedListener, IDoubleClickListener {
    
    private final Composite parent;
    private final CSTableViewer tableViewer;
    
    private SearchFilter currentSearchFilter;
    
    /**
     * Creates a new instance of {@link ViewController} for a given {@link CommentSummaryView}
     * @param view {@link CommentSummaryView} which should be controlled
     * @author Malte Brunnlieb (28.04.2012)
     */
    ViewController(Composite parent, CSTableViewer tableViewer) {
        this.parent = parent;
        this.tableViewer = tableViewer;
    }
    
    /*
     * (non-Javadoc)
     * @see org.eclipse.swt.widgets.Listener#handleEvent(org.eclipse.swt.widgets.Event)
     * @author Malte Brunnlieb (08.04.2012)
     */
    @Override
    public void handleEvent(Event event) {
        // filter selection (DropDown box ToolBar)
        MenuItem item = (MenuItem) event.widget;
        tableViewer.removeFilter(currentSearchFilter);
        currentSearchFilter = new SearchFilter(item.getText());
        tableViewer.addFilter(currentSearchFilter);
        itemDropDown.setText("Search for " + item.getText());
        toolBar.pack();
        parent.layout();
        
        // filter menu (DropDown box ToolBar)
        if (event.detail == SWT.ARROW || event.detail == 0) {
            Rectangle bounds = itemDropDown.getBounds();
            Point point = toDisplay(bounds.x, bounds.y + bounds.height);
            menu.setLocation(point);
            menu.setVisible(true);
            filterText.setFocus();
        }
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.swt.events.KeyListener#keyPressed(org.eclipse.swt.events.KeyEvent)
     * @author Malte Brunnlieb (08.04.2012)
     */
    @Override
    public void keyPressed(KeyEvent e) {
        // not needed
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.swt.events.KeyListener#keyReleased(org.eclipse.swt.events.KeyEvent)
     * @author Malte Brunnlieb (08.04.2012)
     */
    @Override
    public void keyReleased(KeyEvent e) {
        commentFilter.setSearchText(filterText.getText());
        viewer.refresh();
        filterComments();
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.jface.viewers.ISelectionChangedListener#selectionChanged(org.eclipse.jface.viewers.SelectionChangedEvent)
     * @author Malte Brunnlieb (28.04.2012)
     */
    @Override
    public void selectionChanged(SelectionChangedEvent event) {
        // TODO Auto-generated method stub
        // for TableViewer
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.jface.viewers.IDoubleClickListener#doubleClick(org.eclipse.jface.viewers.DoubleClickEvent)
     * @author Malte Brunnlieb (28.04.2012)
     */
    @Override
    public void doubleClick(DoubleClickEvent event) {
        // TODO Auto-generated method stub
        // for TableViewer
    }
}