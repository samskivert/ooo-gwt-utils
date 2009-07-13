//
// $Id$

package com.threerings.gwt.util;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FocusWidget;
import com.google.gwt.user.client.ui.HasAlignment;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;

import com.threerings.gwt.ui.EnterClickAdapter;
import com.threerings.gwt.ui.Popups;
import com.threerings.gwt.ui.SmartTable;

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
    public ClickCallback (HasClickHandlers trigger)
    {
        this(trigger, null);
    }

    /**
     * Creates a callback for the supplied trigger (the constructor will automatically add this
     * callback to the trigger as a click listener). Failure will automatically be reported.
     */
    public ClickCallback (HasClickHandlers trigger, TextBox onEnter)
    {
        _trigger = trigger;
        _onEnter = onEnter;
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
     * This method is called when the trigger button is clicked. Pass <code>this</code> as the
     * {@link AsyncCallback} to a service method. Return true from this method if a service request
     * was initiated and the button that triggered it should be disabled.
     */
    protected abstract boolean callService ();

    /**
     * This method will be called when the service returns successfully. Return true if the trigger
     * should now be reenabled, false to leave it disabled.
     */
    protected abstract boolean gotResult (T result);

    /**
     * Formats the error indicated by the supplied throwable. The default implementation simply
     * returns {@link Throwable#getMessage}.
     */
    protected String formatError (Throwable cause)
    {
        return cause.getMessage();
    }

    /**
     * If a callback wishes to require confirmation it can override this method and return a
     * message that will be displayed to confirm the action before it is taken.
     */
    protected String getConfirmMessage ()
    {
        return null;
    }

    /**
     * Returns the choices given to the user when confirming the callback. The default choices on
     * the confirm dialog are "No", "Yes".
     */
    protected String[] getConfirmChoices ()
    {
        return new String[] { "No", "Yes" };
    }

    /**
     * Override this method and return true if you wish your confirm message to be interpreted as
     * HTML. Be careful!
     */
    protected boolean confirmMessageIsHTML ()
    {
        return false;
    }

    protected void takeAction (boolean confirmed)
    {
        // if we have no confirmation message or are already confirmed, do the deed
        String confmsg = getConfirmMessage();
        if (confirmed || confmsg == null) {
            if (callService()) {
                setEnabled(false);
            }
            return;
        }

        // otherwise display a confirmation panel
        setEnabled(false);
        displayConfirmPopup();
    }

    protected void displayConfirmPopup ()
    {
        final PopupPanel confirm = new PopupPanel();
        confirm.setStyleName("gwt-ConfirmPopup");
        SmartTable contents = new SmartTable(5, 0);
        if (confirmMessageIsHTML()) {
            contents.setHTML(0, 0, confmsg, 2, "Message");
        } else {
            contents.setText(0, 0, confmsg, 2, "Message");
        }
        String[] choices = getConfirmChoices();
        contents.setWidget(1, 0, new Button(choices[0], new ClickHandler() {
            public void onClick (ClickEvent event) {
                confirm.hide(); // abort!
                onAborted();
            }
        }));
        contents.getFlexCellFormatter().setHorizontalAlignment(1, 0, HasAlignment.ALIGN_CENTER);
        contents.setWidget(1, 1, new Button(choices[1], new ClickHandler() {
            public void onClick (ClickEvent event) {
                confirm.hide();
                onConfirmed();
            }
        }));
        contents.getFlexCellFormatter().setHorizontalAlignment(1, 1, HasAlignment.ALIGN_CENTER);
        confirm.setWidget(contents);
        confirm.center();
    }

    protected void onConfirmed ()
    {
        takeAction(true);
    }

    protected void onAborted ()
    {
        setEnabled(true);
    }

    protected void setEnabled (boolean enabled)
    {
        if (_trigger instanceof FocusWidget) {
            ((FocusWidget)_trigger).setEnabled(enabled);

        } else if (_trigger instanceof Label) {
            Label tlabel = (Label)_trigger;
            tlabel.removeStyleName("actionLabel");
            if (enabled) {
                tlabel.addStyleName("actionLabel");
            }
        }

        // always remove first so that if we do end up adding, we don't doubly add
        if (_clickreg != null) {
            _clickreg.removeHandler();
            _clickreg = null;
        }
        if (_enterreg != null) {
            _enterreg.removeHandler();
            _enterreg = null;
        }
        if (enabled) {
            _clickreg = _trigger.addClickHandler(_onClick);
            if (_onEnter != null) {
                _enterreg = _onEnter.addKeyPressHandler(new EnterClickAdapter(_onClick));
            }
        }
    }

    protected void reportFailure (Throwable cause)
    {
        Widget near = _onEnter;
        if (near == null && _trigger instanceof Widget) {
            near = (Widget)_trigger;
        }
        if (_onEnter != null) {
            _onEnter.setFocus(true);
        }
        if (near == null) {
            Popups.error(formatError(cause));
        } else {
            Popups.errorNear(formatError(cause), near);
        }
    }

    protected ClickHandler _onClick = new ClickHandler() {
        public void onClick (ClickEvent event) {
            takeAction(false);
        }
    };

    protected HasClickHandlers _trigger;
    protected HandlerRegistration _clickreg;

    protected TextBox _onEnter;
    protected HandlerRegistration _enterreg;
}
