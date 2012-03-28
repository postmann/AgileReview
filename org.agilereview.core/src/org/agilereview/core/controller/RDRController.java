/**
 * Copyright (c) 2011, 2012 AgileReview Development Team and others.
 * All rights reserved. This program and the accompanying materials are made available under
 * the terms of the Apache License v2.0 which accompanies this distribution, and is available
 * at http://www.apache.org/licenses/LICENSE-2.0.html
 * Contributors: Malte Brunnlieb, Philipp Diebold, Peter Reuter, Thilo Rauch
 */
package org.agilereview.core.controller;

import java.util.LinkedList;
import java.util.List;

import org.agilereview.core.exception.ExceptionHandler;
import org.agilereview.core.external.definition.IReviewDataReceiver;
import org.agilereview.core.external.storage.Review;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.Platform;

/**
 * The {@link RDRController} manages the {@link IReviewDataReceiver}s for the org.agilereview.core.ReviewDataReceiver ExtensionPoint and provides
 * functionality for notifying all registered {@link IReviewDataReceiver} about changed backend data.
 * @author Malte Brunnlieb (28.03.2012)
 */
public class RDRController {
	
	/**
	 * ExtensionPoint id for extensions implementing {@link IReviewDataReceiver}
	 */
	public static final String IREVIEWDATARECEIVER_ID = "org.agilereview.core.ReviewDataReceiver";
	
	/**
	 * Singleton instance of {@link RDRController}
	 */
	private static final RDRController instance = new RDRController();
	/**
	 * Mapping of names to objects of registered {@link IReviewDataReceiver}s
	 */
	private final List<IReviewDataReceiver> registeredClients = new LinkedList<IReviewDataReceiver>();
	
	/**
	 * Creates a new instance of {@link RDRController}
	 * @author Malte Brunnlieb (28.03.2012)
	 */
	private RDRController() {
		checkForNewClients();
	}
	
	/**
	 * Returns the singleton instance of the {@link RDRController}
	 * @return the singleton of {@link RDRController}
	 * @author Malte Brunnlieb (28.03.2012)
	 */
	public static RDRController getInstance() {
		return instance;
	}
	
	/**
	 * Notifies the registered {@link IReviewDataReceiver} with the new data provided by the backend
	 * @param rdr {@link IReviewDataReceiver} which should be notified
	 * @param newData list of {@link Review}s
	 * @author Malte Brunnlieb (28.03.2012)
	 */
	public void notifyClient(IReviewDataReceiver rdr, List<Review> newData) {
		rdr.setReviewData(newData);
	}
	
	/**
	 * Notifies all registered {@link IReviewDataReceiver}s with the new data provided by the backend
	 * @param newData list of {@link Review}s
	 * @author Malte Brunnlieb (28.03.2012)
	 */
	public void notifyAllClients(List<Review> newData) {
		for (IReviewDataReceiver rdr : registeredClients) {
			notifyClient(rdr, newData);
		}
	}
	
	/**
	 * Checks whether the given {@link IReviewDataReceiver} is already registered
	 * @param rdr {@link IReviewDataReceiver} to be checked for registration
	 * @return true, if the given {@link IReviewDataReceiver} is already registered<br>false, otherwise
	 * @author Malte Brunnlieb (28.03.2012)
	 */
	public boolean isRegistered(IReviewDataReceiver rdr) {
		return registeredClients.contains(rdr);
	}
	
	/**
	 * Performs a check for new {@link IReviewDataReceiver}s registered at the ExtensionPoint
	 * @author Malte Brunnlieb (24.03.2012)
	 */
	public void checkForNewClients() { //TODO check for new clients whenever a new plugin was installed
		IConfigurationElement[] config = Platform.getExtensionRegistry().getConfigurationElementsFor(IREVIEWDATARECEIVER_ID);
		if (config.length == 0) {
			registeredClients.clear();
			return;
		}
		
		try {
			for (IConfigurationElement e : config) {
				final Object o = e.createExecutableExtension("class");
				if (o instanceof IReviewDataReceiver) {
					final IReviewDataReceiver rdr = (IReviewDataReceiver) o;
					registeredClients.add(rdr);
				}
			}
		} catch (CoreException ex) {
			ExceptionHandler.notifyUser("An eclipse internal error occurred while determining ReviewDataReceiver", ex);
		}
	}
}