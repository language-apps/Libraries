/*
 * AnnotationImage.java
 *
 *   @author  HarveyD
 *   @version 3.00 Beta
 *
 *   Copyright 2007-2015, all rights reserved
*/
package org.acorns.editor;

import java.awt.*;
import java.awt.image.*;
import org.acorns.data.*;
import org.acorns.language.*;
import org.acorns.visual.*;

public class AnnotationImage
{
    private AnnotationData annotation;
    private boolean center;        // Determine if output should center
    private FontMetrics metrics;   // The font metrics for this object
    private int lineSpacing;       // Spacing between lines

    private int leftMargin;        // Left Margin for this annotation
    private int rightMargin;       // Right Margin for this annotation
    private int topMargin;
    private int bottomMargin;      // Bottom of display.
    private int x, y;              // Current row and column offsets.

    int[]   yLengths;              // The metric length of each line
    int     line;                  // Current line number

    public static final int MARGIN    = 5;      // Vertical margin.

    /** Constructor */
    public AnnotationImage() { center = false; }

    /** Method to create an AnnotationImage picture
     *  @param graphics object to draw into
     *  @param width of total panel
     *  @param clip rectangle dimensions of the visible area
     *  @param singleLine true for displaying annotations horizontally
     *  @param start starting annotation to draw
     *  @param colors object holding fonts, background, and font size
     *  @return -1 if completely drawn, index to failed annotation if not
     */    
    public synchronized int drawAnnotation
        (Graphics graphics, AnnotationData annotation
           , int width, Rectangle clip, boolean singleLine, int start, ColorScheme colors)
    {
        this.annotation = annotation;

        // Get the graphics for drawing and the font parameters.
        int fontSize = colors.getSize();
        if (singleLine) fontSize = 12;
        Font font = KeyboardFonts.getLanguageFonts().getFont(annotation.getKeyboard());
        //KeyboardFonts.getLanguageFonts().getActiveFont();
        if (font == null) font = new Font(null, Font.PLAIN, fontSize);
        else
        {  font = font.deriveFont(Font.PLAIN, fontSize);  }

        graphics.setFont(font);
        metrics = graphics.getFontMetrics();
        lineSpacing = metrics.getMaxDescent() + metrics.getLeading() 
                                              + metrics.getMaxAscent();
        int spaceWidth = metrics.charWidth(' ');
 
        // Set the colors.
        Color color = colors.getColor(true);
        graphics.setColor(color);
        graphics.fillRect(clip.x,clip.y, clip.width, clip.height);
        color = colors.getColor(false); // Get foreground color.
        graphics.setColor(color);

        bottomMargin =  clip.y + clip.height;  // Bottom of page.

        long   offset;
        AnnotationNode[] nodes = annotation.getAnnotationNodes();
        if (!annotation.isRecorded()) return -1;
        int count = annotation.getAnnotationSize();

        // Display as a multiple line story
        if (!singleLine)
        {
        	BufferedImage image = colors.getPicture(new Dimension(clip.width, clip.height));
        	if (image!=null) graphics.drawImage(image, clip.x, clip.y, clip.width, clip.height, null);
            bottomMargin -= lineSpacing;
            leftMargin = x = 0;
            rightMargin = clip.width - leftMargin;

            y = clip.y + lineSpacing + MARGIN;
            int lines = (clip.height - MARGIN -lineSpacing)/ lineSpacing;
            int last = computeLocations(nodes, start, count, lines);

            expandLines(yLengths);
            yLengths[line++] = x;
            topMargin = (lines - line)*lineSpacing/2;
            if (topMargin<0) topMargin = 0;

            int stop = count;
            if (last!=-1) stop = last;
            drawAnnotations(graphics, start, stop, nodes, clip);
            return stop;
        }

        annotation.setAllVisible();
        long frames = annotation.getFrames();
        double samplesPerPoint = (double)frames / width;

        for (int i=start; i<count; i++)
        {
           offset = nodes[i].getOffset();
           leftMargin   = (int)(nodes[i-1].getOffset() / samplesPerPoint);
           rightMargin  = (int)(offset / samplesPerPoint) - spaceWidth;
           graphics.drawLine
               (rightMargin, clip.y, rightMargin, clip.y+clip.height);

           y = clip.y + lineSpacing + MARGIN;
           x = leftMargin;

           if (rightMargin - leftMargin >= spaceWidth)
           {  drawAnnotation(graphics, clip, nodes[i], true);
           }
        }     // End for loop
        return -1;
    }     // End main draw method
       
    /* Method to draw an annotation with word wrap */
    private boolean drawAnnotation
            (Graphics graphics, Rectangle clip
                              , AnnotationNode node, boolean singleLine)
    {
        String word   = node.getText();
        if (singleLine) drawNode(graphics, clip, word, node, singleLine);
        else
        {   word = word.replaceAll("\\\\n", "\\\\n ");
            String[] annotate = word.split("\\\\n");
            for (int i=0; i<annotate.length; i++)
            {
                annotate[i] = annotate[i].trim();
                if (!drawNode(graphics, clip, annotate[i], node, false))
                   return false;
                if (i<annotate.length - 1)
                {  if (!setOffset(false)) return false;
                }
            }
        }
       return true;
    }      // End of drawAnnotation()


   private boolean drawNode
     (Graphics graphics, Rectangle clip, String annotate
                       , AnnotationNode node, boolean singleLine)
   {
       String[] words = annotate.split("\\s+");
       boolean show      = node.isVisible();
       boolean highlight = node.isHighlight();

       for (int i=0; i<words.length; i++)
       {
          if (!drawWord(graphics, words[i], show, highlight, singleLine))
                   return false;

          if (!drawWord(graphics, " ", true, false, singleLine))
                  return false;
       }
       return true;
    }      // End drawNode()
    
    /* Method to draw a string wrapping around when reaching the margin */
    private boolean drawWord(Graphics graphics, String word
               , boolean show, boolean highlight, boolean singleLine)
    {
        String str;
        
        if (!singleLine)
        {  return drawString(graphics, word, show, highlight, singleLine);
        }
        else
        {  for (int i=0; i<word.length(); i++)
           {  str = word.substring(i, i+1);
              if (!drawString(graphics, str, show, highlight, singleLine))
                  return false;
           }
        }
        return true;
    }

    /** Method to draw a string
     *
     * @param graphics graphics object
     * @param str  the string object
     * @param show Whether this string is to show
     * @param highlight Whether this string is to be highlighted
     * @param singleLine The display option
     * @return false if beyond bottom of the display
     */
    private boolean drawString(Graphics graphics, String str
            , boolean show, boolean highlight, boolean singleLine )
    {
        Color select = new Color(170,255,140, 150);
        Color color;
        int width = metrics.stringWidth(str);

        if (x+width >= rightMargin)
        {  if (str.equals(" ")) return true;
           if (!setOffset(singleLine)) return false;
        }

        if (show)
        {   graphics.drawString(str, x, y);
        }
        else graphics.fillRect(x, y-lineSpacing/2, width, lineSpacing/2);

        if (highlight)
        {
            color = graphics.getColor();
            graphics.setColor(select);
            graphics.fillRect(x, y-lineSpacing/2
                                     , width, lineSpacing/2);
            graphics.setColor(color);
        }

        x += width;
        return true;
    }

    private void drawAnnotations(Graphics graphics, int start, int stop
         , AnnotationNode[] nodes, Rectangle clip)
    {
        Point location;
        int offset;
        int topOfPage = clip.y + lineSpacing + MARGIN;

        for (int i=start; i<stop; i++)
        {
            location = nodes[i].getDisplayPoint();

            // Compute horizontal offset to center the display
            line = (location.y - topOfPage)/lineSpacing;
            
            if (annotation.isCentered())
            { offset = (rightMargin - leftMargin - yLengths[line])/2; }
            else offset = 0;

            x = location.x + offset;
            y = location.y + topMargin;
            drawAnnotation(graphics, clip, nodes[i], false);
        }

    }

    /** Compute the positions of all the words on the disply
     *
     * @param annotation The annotation data object
     * @param start The starting node to display
     * @param lines The maximum number of lines
     * @return The first node that couldn't fit on the display
     *              ( -1 if all fit)
     */
     private int computeLocations
            (AnnotationNode[] nodes, int start, int count, int lines)
    {
        line       = 0;
        int   spaceWidth = metrics.charWidth(' ');

        yLengths  = new int[5];
 
        for (int i=start; i<count; i++)
        {  if (rightMargin - leftMargin >= spaceWidth)
           {  if (!computeNodeLocation(nodes[i], lines))
              {   return i; }
           }
        }     // End for loop
        return -1;
    }

    /** Compute a nodes starting position and space needed to display
     *
     * @param node The node in question
     * @param lines The maximum number of lines
     * @return true if successful, false otherwise
     */
    private boolean computeNodeLocation
              (AnnotationNode node, int lines)
    {
        // Set the display point for this node
        Point location = new Point(x, y + line*lineSpacing);
        node.setDisplayPoint(location);

        // Compute the point at the end of the display
        String word   = node.getText();
        word = word.replaceAll("\\\\n", "\\\\n ");
        String[] annotate = word.split("\\\\n");
        for (int i=0; i<annotate.length; i++)
        {
            annotate[i] = annotate[i].trim();
            if (!computeNodeLine(node, annotate[i], lines))
                return false;
            if (i<annotate.length - 1)
            {  yLengths = expandLines(yLengths);
               yLengths[line++] = x;
               x = leftMargin;
            }
        }   // End for
        return true;
    }

    /** Method to compute a portion before a new line of node text
     *
     * @param node The node in question
     * @param annotate The annotation data for a line of text
     * @param lines The maximum number of lines

     * @return true if successful, false otherwise
     */
    private boolean computeNodeLine(AnnotationNode node
                            , String annotate, int lines)
    {
       String[] words = annotate.split("\\s+");
       for (int i=0; i<words.length; i++)
       {  computeWordLocation(words[i], lines);
          if (words[i].length()!=0) computeWordLocation(" ", lines);
          if (line>=lines) return false;
       }
       return true;
    }                // End computeNodeLine()

    /** Method to compute the position of a string within node text
     *
     * @param word A string portion of a node text
     * @param lines The maximum number of lines
     */
    private void computeWordLocation(String word, int lines)
    {   int width = metrics.stringWidth(word);
        if (x+width >= rightMargin)
        {   yLengths = expandLines(yLengths);
            yLengths[line++] = x;
            x = leftMargin;
            if (word.equals(" ")) return;
        }
        x += width;
    }

    /** Method to expand the integer array to double its size */
    private int[] expandLines(int[] lines)
    {
        if (line<lines.length-1) return lines;
        int[] newLines = new int[lines.length*2];
        System.arraycopy(lines, 0, newLines, 0, lines.length);
        return newLines;
    }


    /** Method to set the margins based on the display mode
     *
     * @param singleLine true if displaying as a single output
     */
    private boolean setOffset(boolean singleLine)
    {
        if (singleLine) x = leftMargin;
        else
        {  line++;
           if (annotation.isCentered())
                x = leftMargin
                     + (rightMargin-leftMargin-yLengths[line])/2;
           else x = leftMargin;
        }
        y+=lineSpacing;
        return (y<bottomMargin);
    }

    public boolean isCentered() { return center; }
    
    /** Method to center or left justify the output (left is default)
     *
     * @param center true if to be centered, false otherwise
     */
    public void setCenter(boolean center) {  this.center = center; }

}   // End of AnnotationImage class
