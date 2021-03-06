package org.agilereview.ui.basic.reviewExplorer;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.agilereview.core.external.storage.Comment;
import org.agilereview.core.external.storage.Review;
import org.agilereview.core.external.storage.ReviewSet;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.viewers.ITreePathContentProvider;
import org.eclipse.jface.viewers.TreePath;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.widgets.Display;

/**
 * The ReviewExplorerContentProvider provides the content for the tree viewer of the {@link ReviewExplorerView}
 * 
 * @author Thilo Rauch (28.03.2012)
 */
public class REContentProvider implements ITreePathContentProvider, PropertyChangeListener {
    
    /**
     * Reviews received from the core
     */
    private ReviewSet reviews;
    
    /**
     * The tree viewer of the RevieExlorer
     */
    private TreeViewer viewer;
    
    /**
     * Creates a new instance of the {@link REContentProvider} for the given {@link TreeViewer}
     * @param viewer {@link TreeViewer} for which this {@link REContentProvider} should deliver contents
     * @author Malte Brunnlieb (17.01.2013)
     */
    REContentProvider(TreeViewer viewer) {
        this.viewer = viewer;
    }
    
    @Override
    public void dispose() {
    }
    
    @Override
    public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
        // Deregister listener
        if (oldInput instanceof ReviewSet) {
            ((ReviewSet) oldInput).removePropertyChangeListener(this);
        }
        
        if (newInput instanceof ReviewSet) {
            reviews = (ReviewSet) newInput;
            // Add PropertyChangeListener
            reviews.addPropertyChangeListener(this);
        }
    }
    
    @Override
    public Object[] getElements(Object inputElement) {
        Object[] result = new Object[0];
        // Assume inputElement is a list of reviews
        if (inputElement instanceof ReviewSet) {
            result = ((ReviewSet) inputElement).toArray();
        }
        return result;
    }
    
    @Override
    public Object[] getChildren(TreePath parentPath) {
        Object[] result = new Object[0];
        Object elem = parentPath.getLastSegment();
        // Special case: the root node
        if (elem instanceof Review) {
            // Check all projects
            result = filterResourcesWithComment(ResourcesPlugin.getWorkspace().getRoot().getProjects(), (Review) elem);
        } else if (elem instanceof IContainer) {
            try {
                result = filterResourcesWithComment(((IContainer) elem).members(), (Review) parentPath.getFirstSegment());
            } catch (CoreException e) {
                // if the element is not existent or not open, we also show no children (but should not happen)
            }
        }
        return result;
    }
    
    @Override
    public TreePath[] getParents(Object element) {
        TreePath[] result = { TreePath.EMPTY };
        if (element instanceof IProject) {
            List<TreePath> pathList = new ArrayList<TreePath>();
            // For a project, all reviews have to be checked whether they contain a comment on one of this project's files
            for (Review r : reviews) {
                for (Comment c : r.getComments()) {
                    if (c.getCommentedFile().getProject().equals(element)) {
                        pathList.add(new TreePath(new Object[] { r }));
                        // we only need to find one
                        break;
                    }
                }
            }
            result = pathList.toArray(new TreePath[pathList.size()]);
        } else if (element instanceof IResource) {
            // For a resource, just get the parent's parent paths and add the parent
            TreePath[] parentPaths = getParents(((IResource) element).getParent());
            for (int i = 0; i < parentPaths.length; i++) {
                parentPaths[i] = parentPaths[i].createChildPath(((IResource) element).getParent());
            }
            result = parentPaths;
        }
        return result;
    }
    
    @Override
    public boolean hasChildren(TreePath path) {
        boolean result = false;
        if (path.getLastSegment() instanceof Review) {
            result = !((Review) path.getLastSegment()).getComments().isEmpty();
        } else if (path.getLastSegment() instanceof IContainer) {
            result = getChildren(path).length > 0;
        }
        return result;
    }
    
    /* (non-Javadoc)
     * @see java.beans.PropertyChangeListener#propertyChange(java.beans.PropertyChangeEvent)
     * @author Thilo Rauch (14.07.2012)
     */
    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if ((evt.getSource() instanceof Review && (evt.getPropertyName().equals("comments") || evt.getPropertyName().equals("isOpen") || evt
                .getPropertyName().equals("isActive")))
                || (evt.getSource() instanceof ReviewSet && evt.getPropertyName().equals("reviews"))
                || (evt.getSource() instanceof Comment && evt.getPropertyName().equals("commentedFile"))) {
            updateViewer();
        }
    }
    
    /**
     * Filters from the given input only elements which themselves or one of their descendants has a comment from the given review
     * @param input possible set of resources
     * @param review review to check against
     * @return filtered input
     * @author Thilo Rauch (14.07.2012)
     */
    private Object[] filterResourcesWithComment(IResource[] input, Review review) {
        List<IResource> result = new LinkedList<IResource>();
        for (IResource r : input) {
            for (Comment c : review.getComments()) {
                if (r.getFullPath().isPrefixOf(c.getCommentedFile().getFullPath())) {
                    result.add(r);
                    break;
                }
            }
        }
        return result.toArray();
    }
    
    /**
     * Updates the viewer by setting the contents again. Due to the lifecycle of the used ReviewSet, it might happen that the objects are recreated.
     * Only refreshing the tree will cause widget disposed exceptions.
     * @author Malte Brunnlieb (17.01.2013)
     */
    private void updateViewer() {
        if (viewer != null) {
            Display.getDefault().syncExec(new Runnable() {
                @Override
                public void run() {
                    viewer.setInput(reviews);
                }
            });
        }
    }
}