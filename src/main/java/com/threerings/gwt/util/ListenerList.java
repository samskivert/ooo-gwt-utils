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

import java.util.ArrayList;
import java.util.Map;

/**
 * A handy class for dispatching notifications to listeners.
 */
public class ListenerList<T> extends ArrayList<T>
{
    /** Used by {@link ListenerList#notify}. */
    public static interface Op<L>
    {
        /** Delivers a notification to the supplied listener. */
        void notify (L listener);
    }

    /**
     * Adds the supplied listener to the supplied list. If the list is null, a new listener list
     * will be created. The supplied or newly created list as appropriate will be returned.
     */
    public static <L> ListenerList<L> addListener (ListenerList<L> list, L listener)
    {
        if (list == null) {
            list = new ListenerList<L>();
        }
        list.add(listener);
        return list;
    }

    /**
     * Adds a listener to the listener list in the supplied map. If no list exists, one will be
     * created and mapped to the supplied key.
     */
    public static <L, K> void addListener (Map<K, ListenerList<L>> map, K key, L listener)
    {
        ListenerList<L> list = map.get(key);
        if (list == null) {
            map.put(key, list = new ListenerList<L>());
        }
        list.add(listener);
    }

    /**
     * Removes a listener from the supplied list in the supplied map.
     */
    public static <L, K> void removeListener (Map<K, ListenerList<L>> map, K key, L listener)
    {
        ListenerList<L> list = map.get(key);
        if (list != null) {
            list.remove(listener);
        }
    }

    /**
     * Applies a notification to all listeners in this list.
     */
    public void notify (Op<T> op)
    {
        for (T listener : this) {
            try {
                op.notify(listener);
            } catch (Exception e) {
                e.printStackTrace();
            } 
        }
    }
}
