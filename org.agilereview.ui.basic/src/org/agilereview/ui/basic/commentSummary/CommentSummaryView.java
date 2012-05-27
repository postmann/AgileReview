/**
 * Copyright (c) 2011, 2012 AgileReview Development Team and others.
 * All rights reserved. This program and the accompanying materials are made available under
 * the terms of the Eclipse Public License - v 1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 * Contributors: Malte Brunnlieb, Philipp Diebold, Peter Reuter, Thilo Rauch
 */
package org.agilereview.ui.basic.commentSummary;

import org.agilereview.core.external.definition.IStorageClient;
import org.agilereview.ui.basic.Activator;
import org.agilereview.ui.basic.commentSummary.control.FilterController;
import org.agilereview.ui.basic.commentSummary.control.ViewController;
import org.agilereview.ui.basic.commentSummary.filter.ColumnComparator;
import org.agilereview.ui.basic.commentSummary.filter.SearchFilter;
import org.agilereview.ui.basic.commentSummary.table.TableContentProvider;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.ViewPart;

/**
 * {@link ViewPart} representing the {@link CommentSummaryView}
 * @author Malte Brunnlieb (08.04.2012)
 */
public class CommentSummaryView extends ViewPart {
    
    /**
     * The {@link ToolBar} for this ViewPart
     */
    private CSToolBar toolBar;
    /**
     * The {@link TableViewer} for this ViewPart
     */
    private CSTableViewer viewer;
    /**
     * The content provider for the {@link TableViewer}
     */
    private TableContentProvider tableContentProvider;
    /**
     * The {@link FilterController} which manages all filter actions
     */
    private FilterController filterController;
    /**
     * Parent of this {@link ViewPart}
     */
    private Composite parent;
    
    /**
     * {@inheritDoc}
     * @see org.eclipse.ui.part.WorkbenchPart#createPartControl(org.eclipse.swt.widgets.Composite)
     * @author Malte Brunnlieb (08.04.2012)
     */
    @Override
    public void createPartControl(Composite parent) {
        this.parent = parent;
        parent.setLayout(new GridLayout());
        TableContentProvider.bind(this);
        
        //add help context
        PlatformUI.getWorkbench().getHelpSystem().setHelp(parent, Activator.PLUGIN_ID + ".TableView"); //TODO adapt help context
    }
    
    /**
     * Creates the full functional view using the {@link CSToolBar} and {@link CSTableViewer}
     * @author Malte Brunnlieb (27.05.2012)
     */
    private void createUI() {
        clearParent();
        toolBar = new CSToolBar(parent);
        
        viewer = new CSTableViewer(parent);
        viewer.setContentProvider(new ArrayContentProvider());
        ColumnComparator comparator = new ColumnComparator();
        viewer.setComparator(comparator);
        SearchFilter commentFilter = new SearchFilter("ALL");
        viewer.addFilter(commentFilter);
        
        viewer.addDoubleClickListener(new ViewController(viewer));
        getSite().setSelectionProvider(viewer);
        
        filterController = new FilterController(toolBar, viewer, commentFilter);
        toolBar.setListeners(filterController);
        getSite().getWorkbenchWindow().getSelectionService().addSelectionListener("org.agilereview.ui.basic.reviewExplorerView", filterController);
        
        TableContentProvider.bind(this);
    }
    
    /**
     * {@inheritDoc}
     * @see org.eclipse.ui.part.WorkbenchPart#setFocus()
     * @author Malte Brunnlieb (08.04.2012)
     */
    @Override
    public void setFocus() {
        toolBar.setFocus();
    }
    
    /**
     * Binds the given {@link TableContentProvider} to the {@link CSTableViewer} instance of this view. If the parameter is net to null, the
     * {@link CommentSummaryView} will display a no {@link IStorageClient} registered message instead of a table
     * @param tableModel model for the {@link CSTableViewer}
     * @author Malte Brunnlieb (27.05.2012)
     */
    public void bindTableModel(TableContentProvider tableModel) {
        tableContentProvider = tableModel;
        if (tableContentProvider != null) {
            if (viewer == null) {
                createUI();
            }
            viewer.setContentProvider(tableContentProvider);
        } else {
            displayStorageDisconnect();
        }
    }
    
    /**
     * Displays a message as no {@link IStorageClient} provides data
     * @author Malte Brunnlieb (27.05.2012)
     */
    private void displayStorageDisconnect() {
        clearParent();
        Label label = new Label(parent, SWT.CENTER);
        label.setText("No data available as currently no StorageClient is connected.");
        GridData gd = new GridData();
        gd.grabExcessHorizontalSpace = true;
        gd.grabExcessVerticalSpace = true;
        gd.horizontalAlignment = GridData.CENTER;
        gd.verticalAlignment = GridData.CENTER;
        label.setLayoutData(gd);
    }
    
    /**
     * Disposes all children of the current parent.
     * @author Malte Brunnlieb (27.05.2012)
     */
    private void clearParent() {
        getSite().getWorkbenchWindow().getSelectionService().removeSelectionListener("org.agilereview.ui.basic.reviewExplorerView", filterController);
        toolBar = null;
        viewer = null;
        for (Control child : parent.getChildren()) {
            child.dispose();
        }
    }
    
    /**
     * {@inheritDoc} Also removes selection listeners
     * @see org.eclipse.ui.part.WorkbenchPart#dispose()
     * @author Malte Brunnlieb (27.05.2012)
     */
    @Override
    public void dispose() {
        getSite().getWorkbenchWindow().getSelectionService().removeSelectionListener("org.agilereview.ui.basic.reviewExplorerView", filterController);
        super.dispose();
    }
}
