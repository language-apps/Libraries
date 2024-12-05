/*
 * AnnotationNode.java
 *
 *   @author  HarveyD
 *   @version 3.00 Beta
 *
 *   Copyright 2007-2015, all rights reserved
 */
package org.acorns.data;

import java.io.*;
import javax.swing.*;
import java.awt.*;

/** A single annotation attached to an audio object */
public class AnnotationNode implements Serializable, Cloneable
{   /** Java file's serial version number */
    private static final long serialVersionUID = 1;


    private Object  object;      // The object that annotates part of a sound wave
    private long    soundOffset; // The offset to the sound wave sample
   
    // true if this annotation should be drawn
    private transient boolean status = true;

    // true if this annotation should be highlighted
    private transient boolean highlight = false;

    // Position of draw point
    private transient Point location;
    
    /** Constructor to create an annotation node
     *  @param object Object to attach to this annotation
     *  @param soundOffset Ending sound wave offset for this annotation
     */
    public AnnotationNode(Object object, long soundOffset)
    {  this.object = object;
       this.soundOffset = soundOffset; 
       this.status = true;
    }

    /** Method to retrieve the annotation node object
     *  @return The annotation node object
     */
    public Object getObject() { return object; }
    
    /** Method to change the annotation node object */
    public void setObject(Object object) { this.object = object; }
          
    /** Method to retrieve the text for this annotation
     *  @return annotation text
     */
    public String getText()  
    {  if (object == null) return "";
       return (String)object; 
    }
    
    /** Method to change the text for this annotation
     *  @param text string value of updated text
     */
    public void setText(String text) {this.object = text; }

    /** Method to retrieve a picture for this annotation
     * 
     * @return PictureData object attached to this annotation.
     */
   // public Object getPicture()  { return object; }
    
    /** Method to change the object corresponding to this annotation
     * 
     * @param object to attach
     */
   // public void setPicture(Object picture) { this.object = picture; }
    
    /** Method to return the offset into a sound wave for this annotation
     *  @return ending sound wave offset for this annotation
     */
    public long getOffset()  {  return soundOffset; }
    
    /** Method to change the offset into a sound wave for this annotation
     * @param offset ending sound wave offset for this annotation
     */
    public void setOffset(long offset) { soundOffset = offset; }
   
    /** Method to get the point on the display to draw this node 
     * 
     * @return Point to display
     */
    public Point getDisplayPoint() { return location; }

    /** Method to set the current draw point for this node
     * 
     * @param location The x,y location for drawing this node
     */
    public void setDisplayPoint(Point location)
    {this.location = location; }

    /** Method to set visible status
     *  @param status set whether this node should be visible
     */
    public void setVisible(boolean status)
    {  this.status = status; }
    
    /** Method to return visible status
     *  @return true if this annotation is to be visible
     */
    public boolean isVisible() { return status; }

    /** Method to set highlight status
     *  @param highlight set whether this node should be highlighted
     */
    public void setHighlight(boolean highlight)
    {  this.highlight = highlight; }

    /** Method to return highlight status
     *  @return true if this annotation is to be highlighted
     */
    public boolean isHighlight() { return highlight; }

    
    /** Make an identical copy of this object. */
    public @Override AnnotationNode clone()
    {   AnnotationNode newData = null;
       
       try
       {   newData = (AnnotationNode)super.clone();
       }
       catch (Exception e) 
       {   
      	  Frame frame = JOptionPane.getRootFrame();
      	  JOptionPane.showMessageDialog(frame, "Couldn't clone AnnotationNode");  
       }
       return newData;
    } 
    
}   // End of AnnotationNode class