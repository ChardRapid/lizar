package org.lizar.web;

import org.lizar.exception.LifecycleException;


/**
 * Common interface for component life cycle methods.  Lizar components
 * may, but are not required to, implement this interface (as well as the
 * appropriate interface(s) for the functionality they support) in order to
 * provide a consistent mechanism to start and stop the component.
 */
public interface Lifecycle {



    /**
     * Prepare for the beginning of active use of the public methods of this
     * component.  This method should be called before any of the public
     * methods of this component are utilized.
     *
     * @exception LifecycleException if this component detects a fatal error
     *  that prevents this component from being used
     * @throws Exception 
     */
    public void start() throws LifecycleException, Exception;


    /**
     * Gracefully terminate the active use of the public methods of this
     * component.  This method should be the last one called on a given
     * instance of this component. 
     *
     * @exception LifecycleException if this component detects a fatal error
     *  that needs to be reported
     */
    public void stop() throws LifecycleException;


}
