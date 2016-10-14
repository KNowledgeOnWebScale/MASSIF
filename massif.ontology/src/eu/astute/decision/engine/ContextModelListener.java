package eu.astute.decision.engine;

public interface ContextModelListener {
	/**
	 * Called when an object is updated in the context
	 * */
	void contextObjectUpdated(Object o);

	/**
	 * Called when an object is removed from the context (deleted)
	 * */
	void contextObjectRemoved(Object o);

	/**
	 * Called when a new context object is created
	 * */
	void contextObjectCreated(Object o);
	
	/**
	 * Called to test if the object already exists in working memory
	 * */
	boolean contextObjectExists(Object o);

}