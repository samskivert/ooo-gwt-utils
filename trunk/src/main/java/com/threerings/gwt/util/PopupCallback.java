//
// $Id$
//
// OOO GWT Utils - utilities for creating GWT applications
// Copyright (C) 2009-2010 Three Rings Design, Inc., All Rights Reserved
// http://code.google.com/p/ooo-gwt-utils/
//
// This library is free software; you can redistribute it and/or modify it
// under the terms of the GNU Lesser General Public License as published
// by the Free Software Foundation; either version 2.1 of the License, or
// (at your option) any later version.
//
// This library is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
// Lesser General Public License for more details.
//
// You should have received a copy of the GNU Lesser General Public
// License along with this library; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA

package com.threerings.gwt.util;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Widget;

import com.threerings.gwt.ui.Popups;

/**
 * A base class for callbacks that automatically report errors via {@link Popups#error} or {@link
 * Popups#errorNear} as well as logs the raw error to the {@link Console}.
 */
public abstract class PopupCallback<T> implements AsyncCallback<T>
{
    // from AsyncCallback<T>
    public void onFailure (Throwable cause)
    {
        if (_errorNear == null) {
            Popups.error(formatError(cause));
        } else {
            Popups.errorBelow(formatError(cause), _errorNear);
        }
        Console.log("Service request failed", cause);
    }

    /**
     * Creates a callback that will display its error in the middle of the page.
     */
    protected PopupCallback ()
    {
    }

    /**
     * Creates a callback that will display its error near the supplied widget.
     */
    protected PopupCallback (Widget errorNear)
    {
        _errorNear = errorNear;
    }

    /**
     * Formats the error indicated by the supplied throwable. The default implementation simply
     * returns {@link Throwable#getMessage}.
     */
    protected String formatError (Throwable cause)
    {
        return cause.getMessage();
    }

    protected Widget _errorNear;
}
