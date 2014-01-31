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

import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HasAlignment;
import com.google.gwt.user.client.ui.Widget;

/**
 * Extends {@link FlexTable} and provides a fluent interface for adjusting the styles of cells.
 */
public class FluentTable extends FlexTable
{
    /** Used to format cells. Returned by all methods that configure cells. */
    public static class Cell
    {
        /** The row we're formatting. */
        public final int row;

        /** The column we're formatting. */
        public final int column;

        /** Sets the text of this cell to the string value of the supplied object. */
        public Cell setText (Object text, String... styles) {
            _table.setText(row, column, String.valueOf(text));
            return setStyles(styles);
        }

        /** Sets the HTML in this cell to the supplied value. Be careful! */
        public Cell setHTML (String text, String... styles) {
            _table.setHTML(row, column, String.valueOf(text));
            return setStyles(styles);
        }

        /** Sets the contents of this cell to the specified widget. */
        public Cell setWidget (Widget widget, String... styles) {
            _table.setWidget(row, column, widget);
            return setStyles(styles);
        }

        /** Sets the contents of this cell to a FlowPanel that contains the specified widgets. */
        public Cell setWidgets (Widget... widgets) {
            _table.setWidget(row, column, Widgets.newFlowPanel(widgets));
            return this;
        }

        /** Makes the cell we're formatting align top. */
        public Cell alignTop () {
            _table.getFlexCellFormatter().setVerticalAlignment(
                row, column, HasAlignment.ALIGN_TOP);
            return this;
        }

        /** Makes the cell we're formatting align bottom. */
        public Cell alignBottom () {
            _table.getFlexCellFormatter().setVerticalAlignment(
                row, column, HasAlignment.ALIGN_BOTTOM);
            return this;
        }

        /** Makes the cell we're formatting align middle. */
        public Cell alignMiddle () {
            _table.getFlexCellFormatter().setVerticalAlignment(
                row, column, HasAlignment.ALIGN_MIDDLE);
            return this;
        }

        /** Makes the cell we're formatting align left. */
        public Cell alignLeft () {
            _table.getFlexCellFormatter().setHorizontalAlignment(
                row, column, HasAlignment.ALIGN_LEFT);
            return this;
        }

        /** Makes the cell we're formatting align right. */
        public Cell alignRight () {
            _table.getFlexCellFormatter().setHorizontalAlignment(
                row, column, HasAlignment.ALIGN_RIGHT);
            return this;
        }

        /** Makes the cell we're formatting align center. */
        public Cell alignCenter () {
            _table.getFlexCellFormatter().setHorizontalAlignment(
                row, column, HasAlignment.ALIGN_CENTER);
            return this;
        }

        /** Sets the rowspan of the cell we're formatting. */
        public Cell setRowSpan (int rowSpan) {
            _table.getFlexCellFormatter().setRowSpan(row, column, rowSpan);
            return this;
        }

        /** Sets the colspan of the cell we're formatting. */
        public Cell setColSpan (int colSpan) {
            _table.getFlexCellFormatter().setColSpan(row, column, colSpan);
            return this;
        }

        /** Configures the specified style names on our cell. The first style is set as the primary
         * style and additional styles are added onto that. */
        public Cell setStyles (String... styles) {
            int idx = 0;
            for (String style : styles) {
                if (idx++ == 0) {
                    _table.getFlexCellFormatter().setStyleName(row, column, style);
                } else {
                    _table.getFlexCellFormatter().addStyleName(row, column, style);
                }
            }
            return this;
        }

        /** Returns the cell to the right of this cell, (row, column+1). */
        public Cell right () {
            return new Cell(_table, row, column+1);
        }

        /** Returns the cell below this cell, (row+1, column). */
        public Cell down () {
            return new Cell(_table, row+1, column);
        }

        /** Returns the cell below this cell at column 0, (row+1, 0). */
        public Cell add () {
            return new Cell(_table, row+1, 0);
        }

        /** Returns the table associated with this cell. Useful for chaining the construction and
         * configuration of a table into a single expression:
         * <code>new FluentTable().add().setText("Foo").right().setText("Bar").table()</code> */
        public FluentTable table () {
            return _table;
        }

        protected Cell (FluentTable table, int row, int column)
        {
            _table = table;
            this.row = row;
            this.column = column;
        }

        protected FluentTable _table;
    }

    /**
     * Creates an empty table with no styles and the default cell padding and spacing.
     */
    public FluentTable ()
    {
    }

    /**
     * Creates a table with the specified styles and the default cell padding and spacing.
     */
    public FluentTable (String... styles)
    {
        Widgets.setStyleNames(this, styles);
    }

    /**
     * Creates a table with the specified cell pading and spacing and no styles.
     */
    public FluentTable (int cellPadding, int cellSpacing)
    {
        setCellPadding(cellPadding);
        setCellSpacing(cellSpacing);
    }

    /**
     * Creates a table with the specified styles and cell padding and spacing.
     */
    public FluentTable (int cellPadding, int cellSpacing, String... styles)
    {
        this(styles);
        setCellPadding(cellPadding);
        setCellSpacing(cellSpacing);
    }

    /**
     * Returns the specified cell.
     */
    public Cell at (int row, int column)
    {
        return new Cell(this, row, column);
    }

    /**
     * Returns a {@link Cell} at the current row count and column zero (effectively adding a row to
     * the table).
     */
    public Cell add ()
    {
        return new Cell(this, getRowCount(), 0);
    }
}
