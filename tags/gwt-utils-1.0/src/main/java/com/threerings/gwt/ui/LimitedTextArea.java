//
// $Id$

package com.threerings.gwt.ui;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.VerticalPanel;

/**
 * A text area with a character limit that displays the limit to the user.
 */
public class LimitedTextArea extends VerticalPanel
{
    public LimitedTextArea (int maxChars, int width, int height)
    {
        setStyleName("gwt-LimitedTextArea");
        _maxChars = maxChars;

        add(_area = new TextArea());
        _area.addKeyUpHandler(_limiter);
        if (width > 0) {
            _area.setCharacterWidth(width);
        }
        _area.setVisibleLines(height);
        setHorizontalAlignment(ALIGN_RIGHT);
        add(_remaining = new Label());
        _remaining.setStyleName("Remaining");
        updateRemaining();
    }

    public void setText (String text)
    {
        // we'd use a change listener here but browsers conveniently don't report a change event
        // when a text area's text changes, awesome!
        _area.setText(text);
        updateRemaining();
    }

    public String getText ()
    {
        return _area.getText();
    }

    /**
     * Returns the text area being limited. Don't call {@link TextArea#setText} directly on this
     * area. Use {@link #setText}.
     */
    public TextArea getTextArea ()
    {
        return _area;
    }

    protected void updateRemaining ()
    {
        String text = _area.getText();
        if (text.length() > _maxChars) {
            _area.setText(text = text.substring(0, _maxChars));
        }
        _remaining.setText(_msgs.charRemaining(String.valueOf(_maxChars - text.length())));
    }

    protected KeyUpHandler _limiter = new KeyUpHandler() {
        public void onKeyUp (KeyUpEvent event) {
            updateRemaining();
        }
    };

    protected int _maxChars;
    protected TextArea _area;
    protected Label _remaining;

    protected static final UIMessages _msgs = GWT.create(UIMessages.class);
}
