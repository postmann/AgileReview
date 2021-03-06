package org.agilereview.editorparser.itexteditor.control;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.agilereview.core.external.preferences.AgileReviewPreferences;
import org.agilereview.core.external.storage.Comment;
import org.agilereview.editorparser.itexteditor.prefs.AuthorReservationPreferences;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.source.Annotation;
import org.eclipse.jface.text.source.IAnnotationModel;
import org.eclipse.jface.text.source.IAnnotationModelExtension;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.texteditor.ITextEditor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class is used to draw and manage annotations for a given text editor
 */
public class AnnotationManager {
    
    /**
     * Logger instance
     */
    private static final Logger LOG = LoggerFactory.getLogger(AnnotationManager.class);
    
    /**
     * The color manager for author color management
     */
    private ColorManager colorManager = new ColorManager();
    /**
     * The texteditor's annotation model
     */
    private final IAnnotationModelExtension annotationModel;
    /**
     * The annotations added by AgileReview to the editor's annotation model
     */
    private final HashMap<String, Annotation> annotationMap = new HashMap<String, Annotation>();
    
    /**
     * Creates a new AgileAnnotationModel
     * @param editor The text editor in which the annotations will be displayed
     */
    AnnotationManager(IEditorPart editor) {
        IEditorInput input = editor.getEditorInput();
        this.annotationModel = (IAnnotationModelExtension) ((ITextEditor) editor).getDocumentProvider().getAnnotationModel(input);
    }
    
    /**
     * Clears all annotations for the attached editor
     * @author Malte Brunnlieb (05.12.2012)
     */
    void clearAnnotations() {
        displayAnnotations(null);
    }
    
    /**
     * Displays the given positions as annotations in the provided editor. Therefore annotations which should not be displayed any more will be
     * removed and not yet drawn annotations will be added to the annotation model.
     * @param keyPositionMap a map of Positions which should be annotated and the comment keys correlated to the positions
     */
    void displayAnnotations(Map<String, Position> keyPositionMap) {
        if (keyPositionMap == null) keyPositionMap = new HashMap<String, Position>();
        
        //add annotations that are not already displayed
        Map<Annotation, Position> annotationsToAdd = new HashMap<Annotation, Position>();
        for (String s : keyPositionMap.keySet()) {
            if (!annotationMap.containsKey(s)) {
                Annotation annotation = createNewAnnotation(s);
                if (annotation != null) {
                    annotationsToAdd.put(annotation, keyPositionMap.get(s));
                }
            }
        }
        //remove annotations that should not be displayed
        ArrayList<Annotation> annotationsToRemove = new ArrayList<Annotation>();
        ArrayList<String> keysToDelete = new ArrayList<String>();
        for (String key : annotationMap.keySet()) {
            if (!keyPositionMap.containsKey(key)) {
                annotationsToRemove.add(annotationMap.get(key));
                keysToDelete.add(key);
            }
        }
        annotationModel.replaceAnnotations(annotationsToRemove.toArray(new Annotation[0]), annotationsToAdd);
        annotationMap.keySet().removeAll(keysToDelete);
    }
    
    /**
     * Updates the given comment annotations in the provided editor to the given positions
     * @param keyPositionMap a map of updated positions and the comment keys correlated to the positions
     */
    void updateAnnotations(Map<String, Position> keyPositionMap) {
        for (String key : keyPositionMap.keySet()) {
            if (annotationMap.get(key) != null) {
                annotationModel.modifyAnnotationPosition(annotationMap.get(key), keyPositionMap.get(key));
            }
        }
    }
    
    /**
     * Adds a new annotation at a given position p.
     * @param commentKey The tag key of the comment for which this annotation holds
     * @param p The position to add the annotation on.
     */
    void addAnnotation(String commentKey, Position p) {
        Annotation annotation = createNewAnnotation(commentKey);
        if (annotation != null) {
            LOG.debug("Add Annotation {} to position {}", annotation, p);
            ((IAnnotationModel) this.annotationModel).addAnnotation(annotation, p);
        }
    }
    
    /**
     * Deletes all annotations correlating to the given comment keys
     * @param tagId unique tag key of the comment annotation which should be deleted
     */
    void deleteAnnotation(String tagId) {
        Annotation a = annotationMap.remove(tagId);
        if (a != null) {
            a.markDeleted(true);
        }
        annotationModel.replaceAnnotations(new Annotation[] { a }, null);
    }
    
    /**
     * Deletes all annotations correlating to the given comment keys
     * @param commentKeys unique tag keys of the comment annotations which should be deleted
     */
    void deleteAnnotations(Set<String> commentKeys) {
        HashSet<Annotation> annotationsToRemove = new HashSet<Annotation>();
        Annotation a;
        for (String key : commentKeys) {
            a = annotationMap.remove(key);
            if (a != null) {
                a.markDeleted(true);
                annotationsToRemove.add(a);
            }
        }
        annotationModel.replaceAnnotations(annotationsToRemove.toArray(new Annotation[0]), null);
    }
    
    /**
     * Returns all comments which are overlapping with the given {@link Position}
     * @param p position
     * @return all comments which are overlapping with the given {@link Position}
     */
    String[] getCommentsByPosition(Position p) {
        HashSet<String> commentKeys = new HashSet<String>();
        Position tmp;
        for (String key : annotationMap.keySet()) {
            tmp = ((IAnnotationModel) this.annotationModel).getPosition(annotationMap.get(key));
            if (tmp.overlapsWith(p.getOffset(), p.getLength())) {
                commentKeys.add(key);
            }
        }
        return commentKeys.toArray(new String[0]);
    }
    
    /**
     * Creates a new annotation for a given comment key
     * @param commentKey for which an annotation will be created
     * @return created annotation or<br>null, if the comment is not known
     */
    private Annotation createNewAnnotation(String commentKey) {
        String annotationType;
        Comment comment = DataManager.getInstance().getComment(commentKey);
        if (comment == null) return null;
        if (colorManager.isMultiColorEnabled() && colorManager.hasCustomizedColor(comment.getAuthor())) {
            annotationType = AgileReviewPreferences.AUTHOR_COLOR_DEFAULT + "_" + new AuthorReservationPreferences().getAuthorTag(comment.getAuthor());
        } else {
            annotationType = AgileReviewPreferences.AUTHOR_COLOR_DEFAULT;
        }
        Annotation annotation = new Annotation(annotationType, true, "Review: " + comment.getReview().getId() + ", Author: " + comment.getAuthor()
                + ", Comment-ID: " + comment.getId());
        this.annotationMap.put(commentKey, annotation);
        return annotation;
    }
    
}