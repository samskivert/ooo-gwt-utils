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

package com.threerings.gwt.ui;

import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyPressEvent;
import com.google.gwt.event.dom.client.KeyPressHandler;

/**
 * Converts an escape keypress in a text field to a call to a click handler. NOTE: the handler will
 * be passed a null event.
 */
public class EscapeClickAdapter implements KeyPressHandler
{
    public EscapeClickAdapter (ClickHandler onEscape)
    {
        _onEscape = onEscape;
    }

    // from interface KeyPressHandler
    public void onKeyPress (KeyPressEvent event)
    {
        if (event.getCharCode() == KeyCodes.KEY_ESCAPE) {
            _onEscape.onClick(null);
        }
    }

    protected ClickHandler _onEscape;
}
