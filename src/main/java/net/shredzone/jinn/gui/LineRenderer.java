/**
 * jinn - A property translation editor
 *
 * Copyright (C) 2009 Richard "Shred" Körber
 *   http://jinn.shredzone.org
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package net.shredzone.jinn.gui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.lang.ref.WeakReference;

import javax.swing.JComponent;
import javax.swing.JList;
import javax.swing.ListCellRenderer;

import net.shredzone.jinn.property.CommentLine;
import net.shredzone.jinn.property.EmptyLine;
import net.shredzone.jinn.property.Line;
import net.shredzone.jinn.property.PropertyLine;
import net.shredzone.jinn.property.PropertyModel;

/**
 * A LineRenderer renders a Line object shown in a JList. It will take care for all the
 * calculations required for a proper layout. The JList must be prepared to show lines
 * with different height, though.
 * <p>
 * Comment lines will be shown in a different color than Property lines. Selection will be
 * honored, but rendering a focussed line is not yet implemented.
 *
 * @author Richard "Shred" Körber
 */
public class LineRenderer extends JComponent implements ListCellRenderer<Line> {
    private static final long serialVersionUID = 4235299926570675081L;

    /* TODO: move those to the Style interface */
    private static final int PADDING = 5; // space between two columns
    private static final Color BG_COMMENT = new Color(0xFF, 0xF0, 0xF0);
    private static final Color BG_PROPERTY = new Color(0xF8, 0xF8, 0xF8);

    private int keyWidth; // width of the key column
    private int currentIndex; // current index (line number)
    private WeakReference<Line> currentLine; // current line
    private int lineWidth; // current width of line

    /**
     * Get the Component that draws the list cell.
     * <p>
     * Note: cell focus is currently ignored. This may change in a later version!
     *
     * @param list
     *            JList referred to
     * @param value
     *            Value to draw, must be a Line object!
     * @param index
     *            Row number
     * @param isSelected
     *            true: the row has been selected
     * @param cellHasFocus
     *            true: the row is focussed
     * @return Component that renders the cell. Usually this.
     */
    @Override
    public Component getListCellRendererComponent(JList<? extends Line> list, Line value, int index, boolean isSelected, boolean cellHasFocus) {
        // --- Remember the Parameters ---
        currentLine = new WeakReference<Line>(value); // We just need it for rendering
        currentIndex = index + 1;

        // --- Get some Constants ---
        final FontMetrics fm = getFontMetrics(list.getFont());

        // --- Compute the width of the key column ---
        // The current solution will iterate through the set of key Strings
        // and compute the key width, every time the getListCellRendererComponent
        // method is invoked. This is a poor solution. The result should be
        // cached somewhere for later use. Very surprisingly, this solution is
        // really fast, so it will surely do for a while.
        keyWidth = maxKeyWidth((PropertyModel) list.getModel(), fm);

        // --- Compute the width of the largest line numbers ---
        /*
         * INFO: For performance reasons, we assume that all ciphers are monospaced, which
         * is true for most fonts, even proportional ones. Thus the largest width is the
         * width of the last line number. Anyhow, the clean solution would be to count
         * from 1 to the last line number, and find the maximum width of all those
         * numbers.
         */
        final String maxLineNo = String.valueOf(list.getModel().getSize());
        lineWidth = fm.stringWidth(maxLineNo);

        // --- Set the Background Color ---
        if (isSelected) {
            // Selection is always in Selection color
            setBackground(list.getSelectionBackground());

        } else if (value instanceof EmptyLine) {
            // Empty lines are in the standard background color
            setBackground(list.getBackground());

        } else if (value instanceof CommentLine) {
            // Comment lines are shown on a BG_COMMENT background
            setBackground(BG_COMMENT);

        } else {
            // Everything else (i.e. PropertyLines) are shown on BG_PROPERTY
            setBackground(BG_PROPERTY);
        }

        // --- Set the Foreground Color ---
        // This color is used for standard texts.
        if (isSelected) {
            setForeground(list.getSelectionForeground());
        } else {
            setForeground(list.getForeground());
        }

        // --- Compute the Preferred Size ---
        int lineCnt = 1;
        int maxWidth = 1;

        if (value instanceof PropertyLine) {
            // A PropertyLine has two colums and may have multiple lines.
            final PropertyLine pl = (PropertyLine) value;

            final String[] keyLines = splitLines(pl.getKey());
            final String[] valLines = splitLines(pl.getValue());

            lineCnt = Math.max(lineCnt, keyLines.length);
            lineCnt = Math.max(lineCnt, valLines.length);
            maxWidth = lineWidth + PADDING + keyWidth + PADDING
                + maxLineWidth(valLines, fm) + PADDING;

        } else if (value instanceof CommentLine) {
            // A CommentLine always has only one column and one line.
            final CommentLine cl = (CommentLine) value;
            maxWidth = lineWidth + PADDING + fm.stringWidth(cl.getComment()) + PADDING;

        }

        final Dimension dim = new Dimension(maxWidth, (lineCnt * fm.getHeight()) + 2);
        setPreferredSize(dim);
        setSize(dim);

        // --- Return the prepared Component ---
        return this;
    }

    /**
     * Split a string into its lines. The returned String array contains an entry for each
     * line, without the line terminator. Empty lines result in an empty string.
     *
     * @param str
     *            String to split
     * @return Array of String with an entry for each line
     */
    private String[] splitLines(String str) {
        /* TODO: also split at CRLF and CR */
        return str.split("\\n", -2);
    }

    /**
     * Compute the maximum width of the keys of a PropertyModel.
     *
     * @param model
     *            PropertyModel to look at
     * @param fm
     *            FontMetrics used to render the keys
     * @return Maximum width of the key strings, in pixels
     */
    private int maxKeyWidth(PropertyModel model, FontMetrics fm) {
        int width = 0;
        for (String line : model.getResourceMap().keySet()) {
            String[] keyLines = splitLines(line);
            width = Math.max(width, maxLineWidth(keyLines, fm));
        }
        return width;
    }

    /**
     * Compute the maximum width of an array of lines.
     *
     * @param lines
     *            Array of Strings, one for each line
     * @param fm
     *            FontMetrics used to render the strings
     * @return Maximum width of the lines, in pixels
     */
    private int maxLineWidth(String[] lines, FontMetrics fm) {
        int width = 0;
        for (String line : lines) {
            width = Math.max(width, fm.stringWidth(line));
        }
        return width;
    }

    /**
     * Draw an array of lines, from top to bottom. The y position will always be
     * <code>0 + fm.getMaxAscent()</code>, i.e. the first line will be aligned to y=0 and
     * will never draw above it.
     *
     * @param g2d
     *            Graphics context to use.
     * @param fm
     *            FontMetrics to use.
     * @param xleft
     *            X position of the left edge.
     * @param lines
     *            Array of Strings, one for each line.
     */
    private void drawLines(Graphics2D g2d, FontMetrics fm, int xleft, String[] lines) {
        /* TODO: also accept RTL text direction! */
        int y = fm.getMaxAscent();

        for (String line : lines) {
            g2d.drawString(line, xleft, y);
            y += fm.getHeight();
        }
    }

    /**
     * Paint the component.
     *
     * @param g
     *            Graphics context.
     */
    @Override
    protected void paintComponent(Graphics g) {
        /*
         * TODO: also accept RTL text direction! The columns need to be mirrored and right
         * aligned!
         */
        super.paintComponent(g);

        // --- Get the line to be drawn ---
        final Line line = currentLine.get();
        if (line == null) {
            return; // Line object does not exist anymore
        }

        // --- Get some common variables ---
        final Graphics2D g2d = (Graphics2D) g.create();
        final Dimension size = getSize();
        final FontMetrics fm = getFontMetrics(getFont());

        // --- Draw the background ---
        // Fill with background paint
        g2d.setColor(getBackground());
        g2d.fillRect(0, 0, size.width, size.height);

        // Draw a bottom border line with darker background color
        g2d.setColor(getBackground().darker());
        g2d.drawLine(0, size.height - 1, size.width - 1, size.height - 1);

        // Draw a column line after the line number column
        final int colX = lineWidth + (PADDING / 2);
        g2d.drawLine(colX, 0, colX, size.height - 1);

        // Draw a column line after the key column of PropertyLines
        if (line instanceof PropertyLine) {
            final int colValX = lineWidth + PADDING + keyWidth + (PADDING / 2);
            g2d.drawLine(colValX, 0, colValX, size.height - 1);
        }

        // --- Draw the line number ---
        final String lnr = String.valueOf(currentIndex);
        final int lw = fm.stringWidth(lnr);
        g2d.setColor(Color.GRAY);
        g2d.drawString(lnr, // Line number as String
        lineWidth - lw + 1, // Right aligned
        fm.getMaxAscent() // Correct baseline
        );

        // --- Draw the content ---
        g2d.setColor(getForeground());
        if (line instanceof CommentLine) {
            // We got a CommentLine
            final CommentLine cl = (CommentLine) line;
            g2d.drawString(cl.getComment(), // Comment text
            lineWidth + PADDING, // Content column
            fm.getMaxAscent() // Correct baseline
            );

        } else if (line instanceof PropertyLine) {
            // We got a PropertyLine
            final PropertyLine pl = (PropertyLine) line;
            drawLines(g2d, // Context
            fm, // Font Metrics
            lineWidth + PADDING, // Content column
            splitLines(pl.getKey()) // Splitted key lines
            );
            drawLines(g2d, // Context
            fm, // Font Metrics
            lineWidth + PADDING + keyWidth + PADDING, // Value column
            splitLines(pl.getValue()) // Splitted value lines
            );

        } else {
            // We got an EmptyLine
            // just draw nothing...
        }

        // --- Dispose Graphics2D Context ---
        g2d.dispose();
    }

}
