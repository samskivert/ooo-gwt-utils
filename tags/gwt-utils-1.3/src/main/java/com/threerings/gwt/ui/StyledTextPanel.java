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

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.ui.Widget;

/**
 * Formats user supplied text based on a simple, wiki-like markup syntax.
 */
public class StyledTextPanel extends Widget 
{
    /**
     * Parses incoming (untrusted) text, and builds up a DOM tree around markup, keeping the
     * untrusted text rendered only as text (not as HTML).
     *
     * This class extends {@link Widget} instead of Panel in order to prevent arbitrary additions
     * of other Widgets.
     */
    public StyledTextPanel (String plainText) 
    {
        setElement(DOM.createDiv());
        PIPELINE[0].parse(plainText, getElement(), 0);
    }

    protected static void passDownPipe (String plainText, Element parent, int thisStage)
    {
        if (thisStage + 1 < PIPELINE.length) {
            PIPELINE[thisStage + 1].parse(plainText, parent, thisStage + 1);

        } else {
            // end of the pipeline has been reached - GWT DOM access is a little deficient in that
            // you cannot append a text node to an element.  This is a bit of a hack to get around
            // that by creating an inline div and appending that (with the child text contained)
            // instead.
            Element div = DOM.createDiv();
            DOM.setInnerText(div, plainText);
            DOM.setStyleAttribute(div, "display", "inline"); 
            DOM.appendChild(parent, div); 
        }
    }

    protected static interface Parser
    {
        /**
         * Parses the given plain text, adding the resulting elements to parent, with the knowledge
         * that we are currently at the given pipeline stage.
         */
        void parse (String plainText, Element parent, int stage);
    }

    protected static class ParagraphListParser implements Parser
    {
        public void parse (String plainText, Element parent, int stage) 
        {
            List<Element> lists = new ArrayList<Element>();
            int depth = 0;
            String[] paragraphs = plainText.split("\n");
            for (int ii = 0; ii < paragraphs.length; ii++) {
                paragraphs[ii] = paragraphs[ii].trim();
                // find the number of stars prepending this line
                for (depth = 0; paragraphs[ii].startsWith("*"); 
                     paragraphs[ii] = paragraphs[ii].substring(1), depth++) { /* loop! */ }

                // make sure the current list stack contains the right number of lists
                if (depth < lists.size()) {
                    while (depth < lists.size()) {
                        lists.remove(lists.size() - 1);
                    }

                } else {
                    while (depth > lists.size()) {
                        Element newList = DOM.createElement("ul");
                        if (lists.size() > 0) {
                            Element list = lists.get(lists.size() - 1);
                            Element item = null;
                            if (DOM.getChildCount(list) == 0) {
                                item = DOM.createElement("li");
                                DOM.appendChild(list, item);
                            } else {
                                item = DOM.getChild(list, DOM.getChildCount(list) - 1);
                            }
                            DOM.appendChild(item, newList);
                        } else {
                            DOM.appendChild(parent, newList);
                        }
                        lists.add(newList);
                    }
                }

                // if we're in a list, add to it - otherwise treat this as a paragraph
                if (depth != 0) {
                    Element item = DOM.createElement("li");
                    DOM.appendChild(lists.get(lists.size() - 1), item);
                    passDownPipe(paragraphs[ii], item, stage);
                } else if (paragraphs[ii].length() > 0) {
                    Element p = DOM.createElement("p");
                    passDownPipe(paragraphs[ii].trim(), p, stage);
                    DOM.appendChild(parent, p);
                }
            }
        }
    }

    protected static class TextStyleParser implements Parser
    {
        public void parse (String plainText, Element parent, int stage)
        {
            String[] styles = { "i", "b", "u" };
            String stylesRegex = "[ibu]";
            if (plainText.matches(".*\\[" + stylesRegex + "\\].*\\[/" + stylesRegex + 
                                  "\\].*")) {
                int smallestIndex = plainText.length();
                int smallestIndexIndex = 0;
                for (int ii = 0; ii < styles.length; ii++) {
                    int index = plainText.indexOf("[" + styles[ii] + "]");
                    index = index == -1 ? plainText.length() : index;
                    if (index < smallestIndex) {
                        smallestIndexIndex = ii;
                        smallestIndex = index;
                    }
                }
                String close = "[/" + styles[smallestIndexIndex] + "]";
                String styleAttribute = "";
                String styleValue = "";
                switch (smallestIndexIndex) {
                case 0: // italic
                    styleAttribute = "fontStyle";
                    styleValue = "italic";
                    break;
                case 1: // bold
                    styleAttribute = "fontWeight";
                    styleValue = "bold";
                    break;
                case 2: // underline
                    styleAttribute = "textDecoration";
                    styleValue = "underline";
                    break;
                }
                // this will make things look goofy if one style is nested in another instance
                // of itelf - but that shouldn't be done anyway.
                int end = plainText.indexOf(close, smallestIndex);
                passDownPipe(plainText.substring(0, smallestIndex), parent, stage);
                Element styled = DOM.createDiv();
                DOM.setStyleAttribute(styled, "display", "inline");
                DOM.setStyleAttribute(styled, styleAttribute, styleValue);
                // needs to pass through this parser again, in case styles are nested
                passDownPipe(plainText.substring(smallestIndex + close.length() - 1, end), 
                             styled, stage - 1);
                DOM.appendChild(parent, styled);
                // might be more styles down the line as well
                passDownPipe(plainText.substring(end + close.length()), parent, stage - 1);
            } else {
                passDownPipe(plainText, parent, stage);
            }
        }
    }

    protected static class LinkParser implements Parser
    {
        public void parse (String plainText, Element parent, int stage)
        {
            // TODO: add sophistication.  This currently does a boring search for http: - in the
            // future it should recognize less formal URLs in text.  A super-awesome regex will
            // probably be needed to make sure nobody is doing some tricky hackery in the URL (and
            // to avoid snafu's like periods and tabs at the end of URLs).
            String parseText = plainText;
            int ii = 0;
            while (ii >= 0) {
                ii = parseText.indexOf("http:");
                if (ii >= 0) {
                    passDownPipe(parseText.substring(0, ii), parent, stage);
                    int nextSpace = parseText.indexOf(" ", ii);
                    nextSpace = nextSpace == -1 ? parseText.length() : nextSpace;
                    String URL = parseText.substring(ii, nextSpace);
                    Element anchor = DOM.createAnchor();
                    DOM.setElementProperty(anchor, "href", URL);
                    DOM.setInnerText(anchor, URL);
                    DOM.appendChild(parent, anchor);
                    parseText = parseText.substring(nextSpace);
                }
            }
            if (parseText.length() > 0) {
                passDownPipe(parseText, parent, stage);
            }
        }
    }

    /** An array of parsers, wherein each parser needs to accept text, and parse it for what it is
     * interested in, and then pass the parts its not interested in down the pipeline.  This action
     * proceeds recursively, in order to make it easy to create HTML structures. */
    protected static final Parser[] PIPELINE = {
        new ParagraphListParser(), new TextStyleParser(), new LinkParser()
    };
}
