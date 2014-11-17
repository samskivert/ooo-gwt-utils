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

import java.util.List;

import com.google.common.collect.Lists;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.ButtonBase;
import com.google.gwt.user.client.ui.FocusWidget;
import com.google.gwt.user.client.ui.HasAlignment;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.PushButton;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;

import com.threerings.gwt.ui.EnterClickAdapter;
import com.threerings.gwt.ui.Popups;
import com.threerings.gwt.ui.SmartTable;
import com.threerings.gwt.util.Console;

/**
 * Allows one to wire up a button and a service call into one concisely specified little chunk of
 * code. Be sure to call <code>super.onSuccess()</code> and <code>super.onFailure()</code> if you
 * override those methods so that they can automatically reenable the trigger button.
 *
 * <p> When using the ClickCallback on a Label, the callback automatically adds the style
 * <code>actionLabel</code> to the label and removes the style when the label is disabled during
 * the service call. This style should at a minimum add 'text-decoration: underline; cursor: hand'
 * to the label to indicate to the user that it is clickable.
 */
public abstract class ClickCallback<T>
    implements AsyncCallback<T>
{
    /**
     * Creates a callback for the supplied trigger (the constructor will automatically add this
     * callback to the trigger as a click listener). Failure will automatically be reported.
     */
    public ClickCallback (HasClickHandlers trigger, TextBox...onEnters)
    {
        _trigger = trigger;
        _onEnters = onEnters;
        setEnabled(true); // this will wire up all of our bits
    }

    // from interface AsyncCallback
    public void onSuccess (T result)
    {
        setEnabled(gotResult(result));
    }

    // from interface AsyncCallback
    public void onFailure (Throwable cause)
    {
        Console.log("Callback failure", "for", _trigger, cause);
        setEnabled(true);
        reportFailure(cause);
    }

    /**
     * Configures this callback with a plain text confirmation message.
     */
    public ClickCallback<T> setConfirmText (String message)
    {
        _confirmMessage = message;
        _confirmHTML = false;
        return this;
    }

    /**
     * Configures this callback with an HTML confirmation message.
     */
    public ClickCallback<T> setConfirmHTML (String message)
    {
        _confirmMessage = message;
        _confirmHTML = true;
        return this;
    }

    /**
     * Configures the choices to be shown in the confirmation dialog.
     */
    public ClickCallback<T> setConfirmChoices (String confirm, String abort)
    {
        _confirmChoices = new String[] { confirm, abort };
        return this;
    }

    /**
     * Programatically clicks the button. This is a workaround for gwt's {@link
     * com.google.gwt.user.client.ui.Button#click()} not doing anything.
     */
    public void click ()
    {
        takeAction(false);
    }

    /**
     * This method is called when the trigger button is clicked. Pass <code>this</code> as the
     * {@link AsyncCallback} to a service method. Return true from this method if a service request
     * was initiated and the button that triggered it should be disabled. If an
     * {@link InputException} is thrown, the button will be reenabled so the user can try again.
     * @throws InputException if the service could not be called due to user input
     */
    protected abstract boolean callService ();

    /**
     * This method will be called when the service returns successfully. Return true if the trigger
     * should now be reenabled, false to leave it disabled.
     */
    protected abstract boolean gotResult (T result);

    /**
     * Formats the error indicated by the supplied throwable. The default implementation returns
     * {@link Throwable#getMessage}.
     */
    protected String formatError (Throwable cause)
    {
        return cause.getMessage();
    }

    /**
     * Displays a popup reporting the specified error, near the specified widget if it is non-null.
     */
    protected void showError (Throwable cause, Widget near)
    {
        if (near == null) {
            Popups.error(formatError(cause));
        } else {
            Popups.errorBelow(formatError(cause), near);
        }
    }

    protected void takeAction (boolean confirmed)
    {
        updateConfirmMessage();

        // if we have no confirmation message or are already confirmed, do the deed
        if (confirmed || _confirmMessage == null) {
            try {
                if (callService()) {
                    setEnabled(false);
                }
            } catch (InputException ex) {
                setEnabled(true);
            }
            return;
        }

        // otherwise display a confirmation panel
        setEnabled(false);
        displayConfirmPopup();
    }

    /**
     * Updates the confirmation message just before taking action. This allows subclasses to set
     * the confirmation message based on the state of the other form fields without explicitly
     * updating it each time. By default does nothing so that the previously configured
     * confirmation message is used.
     */
    protected void updateConfirmMessage ()
    {
    }

    protected void displayConfirmPopup ()
    {
        final PopupPanel confirm = new PopupPanel();
        confirm.setStyleName("gwt-ConfirmPopup");
        SmartTable contents = new SmartTable(5, 0);
        int row = addConfirmPopupMessage(contents, 0);
        contents.setWidget(row, 0, createButton(_confirmChoices[1], new ClickHandler() {
            public void onClick (ClickEvent event) {
                confirm.hide(); // abort!
                onAborted();
            }
        }));
        contents.getFlexCellFormatter().setHorizontalAlignment(row, 0, HasAlignment.ALIGN_CENTER);
        contents.setWidget(row, 1, createConfirmButton(_confirmChoices[0], new ClickHandler() {
            public void onClick (ClickEvent event) {
                confirm.hide();
                onConfirmed();
            }
        }));
        contents.getFlexCellFormatter().setHorizontalAlignment(row, 1, HasAlignment.ALIGN_CENTER);
        confirm.setWidget(contents);
        Widget near = getPopupNear();
        if (near == null) {
            confirm.center(); // this shows the popup
        } else {
            Popups.centerOn(confirm, near).show();
        }
    }

    /**
     * Adds the message area for the confirmation popup to the given row and returns the row to
     * insert next.
     */
    protected int addConfirmPopupMessage (SmartTable contents, int row)
    {
        if (_confirmHTML) {
            contents.setHTML(row, 0, _confirmMessage, 2, "Message");
        } else {
            contents.setText(row, 0, _confirmMessage, 2, "Message");
        }
        return row + 1;
    }

    protected void onConfirmed ()
    {
        takeAction(true);
    }

    protected void onAborted ()
    {
        setEnabled(true);
    }

    /**
     * Creates the confirm button that is added to the popup confirmation dialog. This is exposed
     * in order to allow subclasses to make changes.
     */
    protected ButtonBase createConfirmButton (String text, ClickHandler onClick)
    {
        return createButton(text, onClick);
    }

    protected ButtonBase createButton (String text, ClickHandler onClick)
    {
        return new PushButton(text, onClick);
    }

    protected void setEnabled (boolean enabled)
    {
        // set the enabled status of our trigger widget
        if (_trigger instanceof FocusWidget) {
            ((FocusWidget)_trigger).setEnabled(enabled);

        } else if (_trigger instanceof Label) {
            Label tlabel = (Label)_trigger;
            tlabel.removeStyleName("actionLabel");
            if (enabled) {
                tlabel.addStyleName("actionLabel");
            }
        }

        // set the enabled status of our associated text box if we've got one
        for (TextBox onEnter : _onEnters) {
            onEnter.setEnabled(enabled);
        }

        // always remove first so that if we do end up adding, we don't doubly add
        for (HandlerRegistration reg : _regs) {
            reg.removeHandler();
        }
        _regs.clear();
        if (enabled) {
            _regs.add(_trigger.addClickHandler(_onClick));
            for (TextBox onEnter : _onEnters) {
                _regs.add(onEnter.addKeyDownHandler(new EnterClickAdapter(_onClick)));
            }
        }
    }

    protected void reportFailure (Throwable cause)
    {
        if (_onEnters.length > 0) {
            _onEnters[0].setFocus(true);
        }
        showError(cause, getPopupNear());
    }

    protected Widget getPopupNear ()
    {
        if (_onEnters.length > 0) {
            return _onEnters[0];
        } else if (_trigger instanceof Widget) {
            return (Widget)_trigger;
        } else {
            return null;
        }
    }

    protected ClickHandler _onClick = new ClickHandler() {
        public void onClick (ClickEvent event) {
            takeAction(false);
        }
    };

    protected final HasClickHandlers _trigger;
    protected final List<HandlerRegistration> _regs = Lists.newArrayList();
    protected final TextBox[] _onEnters;

    protected String _confirmMessage;
    protected boolean _confirmHTML;
    protected String[] _confirmChoices = { "Yes", "No" };
}
