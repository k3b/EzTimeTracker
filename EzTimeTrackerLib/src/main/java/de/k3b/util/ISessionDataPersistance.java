package de.k3b.util;

import java.io.Serializable;

/**
 * Persists session state to local file.
 * 
 * @param <T>
 *            Datatype to be persisted
 */

public interface ISessionDataPersistance<T extends Serializable> {

	public abstract void save(final T sessionData);

	public abstract T load();

}