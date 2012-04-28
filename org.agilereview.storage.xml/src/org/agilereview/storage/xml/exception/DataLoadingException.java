package org.agilereview.storage.xml.exception;

import org.agilereview.core.external.definition.IStorageClient;

/**
 * A {@link DataLoadingException} is thrown if data could not be loaded by an {@link IStorageClient}.
 * @author Peter Reuter (28.04.2012)
 */
public class DataLoadingException extends Exception {
	
	/**
	 * Default serialVersionUID. 
	 */
	private static final long serialVersionUID = 1L;

		/**
	 * Constructs a {@link DataLoadingException} with the specified detail message.
	 * @param message
	 * @author Peter Reuter (28.04.2012)
	 */
	public DataLoadingException(String message) {
		super(message);
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Throwable#toString()
	 * @author Peter Reuter (28.04.2012)
	 */
	public String toString() {
		return this.getClass().toString();
	}

}
