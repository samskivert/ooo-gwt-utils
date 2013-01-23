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

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.HasAllMouseHandlers;
import com.google.gwt.event.dom.client.MouseDownEvent;
import com.google.gwt.event.dom.client.MouseDownHandler;
import com.google.gwt.event.dom.client.MouseMoveEvent;
import com.google.gwt.event.dom.client.MouseMoveHandler;
import com.google.gwt.event.dom.client.MouseUpEvent;
import com.google.gwt.event.dom.client.MouseUpHandler;
import com.google.gwt.event.logical.shared.CloseEvent;
import com.google.gwt.event.logical.shared.CloseHandler;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.FocusWidget;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.Widget;

import com.threerings.gwt.util.Preconditions;
import com.threerings.gwt.util.Value;

/**
 * Popup-related utility methods.
 */
public class Popups
{
    /** Used to defer the creation of a popup panel. */
    public interface Thunk {
        /** Creates and returns a deferred popup panel. */
        PopupPanel createPopup ();
    }

    /** Positions supported by {@link #show} and other methods. */
    public enum Position { ABOVE, OVER, BELOW, RIGHT };

    /**
     * Displays an info message centered in the browser window.
     */
    public static InfoPopup info (String message)
    {
        return new InfoPopup(message).showCentered();
    }

    /** @deprecated Use {@link #infoBelow} */ @Deprecated
    public static void infoNear (String message, Widget target)
    {
        info(message, Position.BELOW, target);
    }

    /**
     * Displays an info message below the specified widget.
     */
    public static InfoPopup infoAbove (String message, Widget target)
    {
        return info(message, Position.ABOVE, target);
    }

    /**
     * Displays an info message below the specified widget.
     */
    public static InfoPopup infoBelow (String message, Widget target)
    {
        return info(message, Position.BELOW, target);
    }

    /**
     * Displays an info message centered horizontally on the page and centered vertically on the
     * specified target widget.
     */
    public static InfoPopup infoOn (String message, Widget target)
    {
        return centerOn(new InfoPopup(message), target);
    }

    /**
     * Displays an info message below the specified widget.
     */
    public static InfoPopup info (String message, Position pos, Widget target)
    {
        return show(new InfoPopup(message), pos, target);
    }

    /**
     * Displays error feedback to the user in a non-offensive way.
     */
    public static InfoPopup error (String message)
    {
        return new InfoPopup(message).toError().showCentered();
    }

    /** @deprecated Use {@link #errorBelow} */ @Deprecated
    public static void errorNear (String message, Widget source)
    {
        if (source instanceof FocusWidget) {
            ((FocusWidget)source).setFocus(true);
        }
        new InfoPopup(message).toError().showNear(source);
    }

    /**
     * Displays error feedback to the user in a non-offensive way. The error feedback is displayed
     * near the supplied component and if the component supports focus, it is focused.
     */
    public static InfoPopup errorAbove (String message, Widget source)
    {
        return error(message, Position.ABOVE, source);
    }

    /**
     * Displays error feedback to the user in a non-offensive way. The error feedback is displayed
     * near the supplied component and if the component supports focus, it is focused.
     */
    public static InfoPopup errorBelow (String message, Widget source)
    {
        return error(message, Position.BELOW, source);
    }

    /**
     * Displays error feedback to the user in a non-offensive way. The error feedback is displayed
     * near the supplied component and if the component supports focus, it is focused.
     */
    public static InfoPopup error (String message, Position pos, Widget source)
    {
        if (source instanceof FocusWidget) {
            ((FocusWidget)source).setFocus(true);
        }
        return show(new InfoPopup(message).toError(), pos, source);
    }

    /**
     * Shows the supplied popup in the specified position relative to the specified target widget.
     */
    public static <T extends PopupPanel> T show (T popup, Position pos, Widget target)
    {
        popup.setVisible(false);
        popup.show();

        int left, top;
        switch (pos) {
        case RIGHT:
            left = target.getAbsoluteLeft() + target.getOffsetWidth() + NEAR_GAP;
            break;
        default:
            left = target.getAbsoluteLeft();
            break;
        }
        if (left + popup.getOffsetWidth() > Window.getClientWidth()) {
            left = Math.max(0, Window.getClientWidth() - popup.getOffsetWidth());
        }

        switch (pos) {
        case ABOVE:
            top = target.getAbsoluteTop() - popup.getOffsetHeight() - NEAR_GAP;
            break;
        case OVER:
            top = target.getAbsoluteTop();
            break;
        case RIGHT:
            top = target.getAbsoluteTop() + (target.getOffsetHeight() - popup.getOffsetHeight())/2;
            break;
        default:
        case BELOW:
            top = target.getAbsoluteTop() + target.getOffsetHeight() + NEAR_GAP;
            break;
        }

        popup.setPopupPosition(left, top);
        popup.setVisible(true);
        return popup;
    }

    /** @deprecated Use {@link #showBelow} */ @Deprecated
    public static void showNear (PopupPanel popup, Widget target)
    {
        show(popup, Position.BELOW, target);
    }

    /**
     * Shows the supplied popup panel near the specified target.
     */
    public static <T extends PopupPanel> T showAbove (T popup, Widget target)
    {
        return show(popup, Position.ABOVE, target);
    }

    /**
     * Shows the supplied popup panel over the specified target.
     */
    public static <T extends PopupPanel> T showOver (T popup, Widget target)
    {
        return show(popup, Position.OVER, target);
    }

    /**
     * Shows the supplied popup panel below the specified target.
     */
    public static <T extends PopupPanel> T showBelow (T popup, Widget target)
    {
        return show(popup, Position.BELOW, target);
    }

    /**
     * Centers the supplied vertically on the supplied trigger widget. The popup's showing state
     * will be preserved.
     */
    public static <T extends PopupPanel> T centerOn (T popup, Widget centerOn)
    {
        return centerOn(popup, centerOn.getAbsoluteTop() + centerOn.getOffsetHeight()/2);
    }

    /**
     * Centers the supplied vertically on the supplied trigger widget. The popup's showing state
     * will be preserved.
     *
     * @return the supplied popup.
     */
    public static <T extends PopupPanel> T centerOn (T popup, int ypos)
    {
        boolean wasHidden = !popup.isShowing();
        boolean wasVisible = popup.isVisible();
        if (wasVisible) {
            popup.setVisible(false);
        }
        if (wasHidden) {
            popup.show();
        }
        int left = (Window.getClientWidth() - popup.getOffsetWidth()) >> 1;
        int top = ypos - popup.getOffsetHeight()/2;
        // bound the popup into the visible browser area if possible
        if (popup.getOffsetHeight() < Window.getClientHeight()) {
            top = Math.min(Math.max(0, top), Window.getClientHeight() - popup.getOffsetHeight());
        }
        popup.setPopupPosition(left, top);
        if (wasHidden) {
            popup.hide();
        }
        if (wasVisible) {
            popup.setVisible(true);
        }
        return popup;
    }

    /**
     * Creates and returns a new popup with the specified style name and contents.
     */
    public static PopupPanel newPopup (String styleName, Widget contents)
    {
        PopupPanel panel = new PopupPanel();
        panel.setStyleName(styleName);
        panel.setWidget(contents);
        return panel;
    }

    /**
     * Creates a new popup with the specified style name and contents and shows it near the
     * specified target widget. Returns the newly created popup.
     */
    public static PopupPanel newPopup (String styleName, Widget contents,
                                       Position pos, Widget target)
    {
        PopupPanel panel = newPopup(styleName, contents);
        show(panel, pos, target);
        return panel;
    }

    /**
     * Creates a click handler that hides the specified popup. Useful when creating popups that
     * behave like menus.
     */
    public static ClickHandler createHider (final PopupPanel popup)
    {
        return new ClickHandler() {
            public void onClick (ClickEvent event) {
                popup.hide();
            }
        };
    }

    /**
     * Binds the popped up state of a popup to the supplied boolean value and vice versa (i.e. if
     * the popup is popped down, the value will be updated to false). The popup is not created
     * until the first time the value is true.
     */
    public static void bindPopped (final Value<Boolean> popped, final Thunk thunk)
    {
        Preconditions.checkNotNull(thunk, "thunk");
        popped.addListenerAndTrigger(new Value.Listener<Boolean>() {
            public void valueChanged (Boolean visible) {
                if (visible) {
                    popped.removeListener(this);
                    bindPopped(popped, thunk.createPopup());
                }
            }
        });
    }

    /**
     * Binds the popped up state of a popup to the supplied boolean value and vice versa (i.e. if
     * the popup is popped down, the value will be updated to false).
     */
    public static void bindPopped (final Value<Boolean> popped, final PopupPanel panel)
    {
        Preconditions.checkNotNull(panel, "panel");
        panel.addCloseHandler(new CloseHandler<PopupPanel>() {
            public void onClose (CloseEvent<PopupPanel> event) {
                popped.update(false);
            }
        });
        popped.addListenerAndTrigger(new Value.Listener<Boolean>() {
            public void valueChanged (Boolean visible) {
                if (visible) {
                    panel.center();
                } else {
                    panel.hide();
                }
            }
        });
    }

    /**
     * Binds the popped up state of a popup to the supplied boolean value and vice versa (i.e. if
     * the popup is popped down, the value will be updated to false). The popup is not created
     * until the first time the value is true.
     */
    public static void bindPopped (final Value<Boolean> popped, final Position pos,
                                   final Widget target, final Thunk thunk)
    {
        Preconditions.checkNotNull(target, "target");
        Preconditions.checkNotNull(thunk, "thunk");
        popped.addListenerAndTrigger(new Value.Listener<Boolean>() {
            public void valueChanged (Boolean visible) {
                if (visible) {
                    popped.removeListener(this);
                    bindPopped(popped, pos, target, thunk.createPopup());
                }
            }
        });
    }

    /**
     * Binds the popped up state of a popup to the supplied boolean value and vice versa (i.e. if
     * the popup is popped down, the value will be updated to false).
     */
    public static void bindPopped (final Value<Boolean> popped, final Position pos,
                                   final Widget target, final PopupPanel panel)
    {
        Preconditions.checkNotNull(target, "target");
        Preconditions.checkNotNull(panel, "panel");
        panel.addCloseHandler(new CloseHandler<PopupPanel>() {
            public void onClose (CloseEvent<PopupPanel> event) {
                popped.update(false);
            }
        });
        popped.addListenerAndTrigger(new Value.Listener<Boolean>() {
            public void valueChanged (Boolean visible) {
                if (visible) {
                    show(panel, pos, target);
                } else {
                    panel.hide();
                }
            }
        });
    }

    /**
     * Adds mouse handlers to the specified drag handle that cause the supplied target popup to be
     * dragged around the display. The drag handle is assumed to be a child of the popup.
     */
    public static void makeDraggable (HasAllMouseHandlers dragHandle, PopupPanel target)
    {
        DragHandler dragger = new DragHandler(target);
        dragHandle.addMouseDownHandler(dragger);
        dragHandle.addMouseUpHandler(dragger);
        dragHandle.addMouseMoveHandler(dragger);
    }

    protected static class DragHandler implements MouseDownHandler, MouseUpHandler, MouseMoveHandler
    {
        public DragHandler (PopupPanel popup) {
            _popup = popup;
        }

        public void onMouseDown (MouseDownEvent event) {
            _dragging = ((Widget)event.getSource()).getElement();
            DOM.setCapture(_dragging);
            _dragStartX = event.getX();
            _dragStartY = event.getY();
        }

        public void onMouseMove (MouseMoveEvent event) {
            if (_dragging != null) {
                int absX = event.getX() + _popup.getAbsoluteLeft();
                int absY = event.getY() + _popup.getAbsoluteTop();
                // TODO: prevent moving out of the window
                _popup.setPopupPosition(absX - _dragStartX, absY - _dragStartY);
            }
        }

        public void onMouseUp (MouseUpEvent event) {
            DOM.releaseCapture(_dragging);
            _dragging = null;
        }

        protected PopupPanel _popup;
        protected Element _dragging;
        protected int _dragStartX, _dragStartY;
        protected int _popupStartX, _popupStartY;
    }

    protected static final int NEAR_GAP = 5;
}
