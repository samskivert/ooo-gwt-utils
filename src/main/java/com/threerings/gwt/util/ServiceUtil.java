//
// $Id$

package com.threerings.gwt.util;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.ServiceDefTarget;

/**
 * A utility method for setting up {@link RemoteService} handles.
 */
public class ServiceUtil
{
    /**
     * Binds the supplied service to the specified entry point.
     */
    public static <T> T bind (T service, String entryPoint)
    {
        ((ServiceDefTarget)service).setServiceEntryPoint(entryPoint);
        return service;
    }
}
