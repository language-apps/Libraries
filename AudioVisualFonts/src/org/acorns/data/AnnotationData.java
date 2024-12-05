/*
 * AnnotationData.java
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
import org.acorns.editor.*;
import org.acorns.language.*;
import javax.sound.sampled.*;
import org.acorns.audio.*;
import org.acorns.properties.*;

/** Extends the SoundData class to add text annotations to
 *  a portion of an audio recording
 */
public class AnnotationData extends SoundData 
        implements Serializable, Cloneable, Annotations
{ 
    private static final long serialVersionUID = 1;
    
    private AnnotationNodeArray[] data; // Table of annotation data
    private String[] keyboards;         // Keyboard map for each level.
    private int layer;                  // The current lesson layer

    private transient  AcornsProperties acornsProperties;
    
    /** Standard constructor
     * 
     * @param text Array of gloss, native, language, frame rate, file name. See
     *        the AcorndProperties class for the offset constants.
     * @param label Error label to attach to this object
     */
    public AnnotationData(String[] text, JLabel label)
    {  super(text, label);
       
       data = new AnnotationNodeArray[AcornsProperties.MAX_LAYERS];
       
       String language = KeyboardFonts.getLanguageFonts().getLanguage();
       keyboards   = new String[AcornsProperties.MAX_LAYERS];
       layer = 0;
       
       for (int i=0; i<AcornsProperties.MAX_LAYERS; i++)  
       {
           data[i] = new AnnotationNodeArray();
           keyboards[i] = language;
       }
    }
    
    /** Instantiate with a SoundData object
     * 
     * @param soundData The soundData object
     */
    
    public AnnotationData(SoundData soundData)
    {
    	super(soundData);
    	
    	if (soundData instanceof AnnotationData)
    	{
    	    AnnotationData annotationData = (AnnotationData)soundData;
    	    this.data = annotationData.data;
    	    this.keyboards = annotationData.keyboards;
    	    this.layer = Annotation.LAYER;
    	}
    }
    
    /** Conversion constructor from version 3.0
     * 
     * @param text The SoundData text array
     * @param format The SoundData AudioFormat
     * @param audioData The SoundData PCM byte array
     * @param data The table of annotations and layers
     * @param annotations The number of annotations per layer
     * @param keyboards The language per annotation layer
     * @param layer The current layer
     */
    public AnnotationData(String[] text, AudioFormat format, byte[] audioData, 
            AnnotationNode[][] data, int[] annotations, String[] keyboards, int layer)
    {
        super(text, format, audioData);

        this.keyboards = keyboards;
        this.data      = new AnnotationNodeArray[AcornsProperties.MAX_LAYERS];
        this.layer     = layer;
        
        String language = KeyboardFonts.getLanguageFonts().getLanguage();
        for (int i=0; i<AcornsProperties.MAX_LAYERS; i++)
        {  if (data[i] !=null)
           {   
               this.data[i] = new AnnotationNodeArray(data[i], annotations[i]);
           } 
           else this.data[i] = new AnnotationNodeArray();
           
           if (keyboards[i] != null) this.keyboards[i] = keyboards[i];
           else {  this.keyboards[i] = language;  } 
        }    
    }
    
    /** Method to get AcornsProperties object
     *
     * @return AcornsProperties object or null
     */
    private void setFileDirty()
    {  if (acornsProperties==null)
          acornsProperties = AcornsProperties.getAcornsProperties();
       if (acornsProperties!=null) acornsProperties.setFileDirty();
    }

    /** Method to insert a new annotation at the current level
     *  @param text   annotation associated with this part of a sound wave
     *  @param point  selection for this annotation
     *  @return true if successful, false otherwise
     */
    public boolean insertAnnotation(String text, Point point)
    {   if (point.y>getFrames()) return false;
        setFileDirty();
        return data[layer].insert(text, new Point(point));  
    } 
   
    /** Method to insert a new annotation at the current level
     *  @param text   annotation associated with this part of a sound wave
     *  @param point  selection for this annotation
     *  @param sampleRate sample rate of imported file
     *  @return true if successful, false otherwise
     */
    public boolean insertAnnotation(String text, Point point, int sampleRate)
    {           
        point.x = getAnnotationOffset(point.x, sampleRate);
        point.y = getAnnotationOffset(point.y, sampleRate);
        if (point.y>getFrames()) return false;

        setFileDirty();
        return data[layer].insert(text, new Point(point));  
    } 
    
    /** Method to scale the annotations to the current SoundData object
     * 
     * @param sampleRate the sample rate present in the imported file
     * @return the computed offset accounting for possible changes in the frame rate.
     */
    private int getAnnotationOffset(int offset, int sampleRate)
    {
       double ratio = getFrameRate()/sampleRate;
       return (int)(ratio * offset);
    }

    /**  Method to modify an annotation
     *  @param text   annotation associated with this part of a sound wave
     *  @param point  selection for this annotation
     *  @return true if modify succeeded, false otherwise
     */
    public boolean modify(String text, Point point)
    {   if (point.y>getFrames()) return false;
        setFileDirty();
        return data[layer].modify(text, new Point(point));   }

    /** Method to get the annotation text at a given selection
     *
     * @param point The point in the sound wave
     * @return The corresponding text
     */
    public String getString(Point point)
    {   if (point.y>getFrames()) return "";
        return (String)data[layer].findObject(point);
    }
    
    /** Method to shift annotations based for a copy operation
     *  @param point  selection for this annotation
     *  @param all true if all levels should be affected
     *  @param direction true if shift right, false otherwise
     */
    public boolean shift(Point point, boolean all, boolean direction)
    {   if (point.y>getFrames()) return false;
        
        // Set the applicable layers.
        int startlayer = layer;
        int endlayer   = layer;
        if (all) { startlayer = 0; endlayer = AcornsProperties.MAX_LAYERS-1; }
        
        for (int i=startlayer; i<=endlayer; i++)
              data[i].shift(new Point(point), direction);

        setFileDirty();
        return true;
    }
    
    /** Method to remove a single annotation
     * 
     * @param offset audio frame offset of annotation to remove
     * @return true if succeeds
     */
    public synchronized boolean delete(int offset)
    {
        int frames = getFrames();
        if (frames==0) return false;
        setFileDirty();
        return data[layer].delete(offset); 
    }
    
    
    /** Method to remove an annotation
     *  @param point  selection for this annotation
     *  @param all true if all levels should be affected
     *  @return true if deletion succeeded, false otherwise
     */
    public synchronized boolean delete(Point point, boolean all)
    {   
        int startlayer = layer;
        int endlayer   = layer;
        if (point.y < 0) point.y = getFrames();
        if (all) { startlayer = 0; endlayer = AcornsProperties.MAX_LAYERS-1; }
         
        Point newPoint;
        for (int i=startlayer; i<=endlayer; i++)
        {   newPoint = new Point(point);
            if (point.y < 0) newPoint.y = getFrames();
            data[i].delete(newPoint);
        }
        setFileDirty();
        return true; 
    }
        
    /** Method to remove an annotation
     *  @param point  selection for this annotation
     *  @return true if deletion succeeded, false otherwise
     */
    public boolean delete(Point point)
    {   
        int frames = getFrames();
        if (frames==0) return false;
        if (point.y < 0) point.y = getFrames();
        setFileDirty();
        return data[layer].delete(point); 
    }

    
    /** Method to set the active annotation level
     *  @param layer desired annotation level
     */
    public void setAnnotationLevel(int layer) {  this.layer = layer; }
    
    /** Method to return the active annotation level
     *  @return active annotation level
     */
    public int getAnnotationLevel() { return layer; }
    
    /** Method to get which sound in this annotation object
     * 
     * @return The sound wave in question
     */
    public int getSoundLayer()  
    { return 0; // Only one sound wave 
    }
    
    /** Method to return the annotationData for the active level
     *  @return an array of annotations for this level, or null if none
     */
    public AnnotationNode[] getAnnotationNodes()  
    { return data[layer].getAnnotationNodes(); }
    
    /** Method to return the annotationData for a particular level 
     *      scaled for export
     * 
     *  @param level The level identifying which group of nodes to return
     *  @param extension The type of sound file being created
     *  @return an array of annotations for this level, or null if none
     */
    public AnnotationNode[] getScaledAnnotationNodes
                                        (int level, String extension)  
    {   float sampleRate = getFrameRate();
        float newRate = SoundIO.getDesiredFrameRate(extension);
        float ratio = 1.0f;
        if (newRate>0) ratio = newRate / sampleRate;
 
        return data[level].getScaledAnnotationNodes(ratio);
    }
 
    /** Method to return the count of non null annotation nodes
     *  @return count of non-null nodes
     */
    public int getAnnotationCount()
    {   int totalCount = 0;
        
        AnnotationNode[] nodes = data[layer].getAnnotationNodes();
        int size = data[layer].getAnnotationSize();
        for (int n=0; n<size; n++) 
        {   if (nodes[n].getText().length() != 0) totalCount++;  }
        return totalCount;        
    }

    /** Extract text nodes into a single string
     * 
     * @return The combined text extracted
     */
    public String getAnnotationText(int layer)
    {
    	StringBuilder build = new StringBuilder();

        int totalCount = 0;
    	AnnotationNode[] nodes = data[layer].getAnnotationNodes();
        int size = data[layer].getAnnotationSize();
        String text = "";
        for (int n=0; n<size; n++) 
        {  
        	text = nodes[n].getText();
        	text = nodes[n].getText().trim();
        	text = text.replaceAll("\\\\n", "\n");
        	if (text.length()>0) 
        	{
            	if (totalCount>0) build.append(" ");
            	build.append(text);
        		totalCount++;
        	}
        }
        
        text = build.toString();
        text = text.replaceAll("\\n ", "\n");
    	return text;
    }
    
    /** Method to return the count of non null annotation nodes for a level
     * @param level level of nodes in question
     * @return count of non null nodes
     */
     public int getAnnotationCount(int level)
    {   int totalCount = 0;
        
        AnnotationNode[] nodes = data[level].getAnnotationNodes();
        int size = data[level].getAnnotationSize();
        for (int n=0; n<size; n++) 
        {   if (nodes[n].getText().length() != 0) totalCount++;  }
        return totalCount;        
    }
     
    /** Method to return the count of annotations at the active level
     *  @return number of annotations
     */
    public int getAnnotationSize()  
    { return data[layer].getAnnotationSize(); }
    
    /** Method to set the current keyboard language */
    public void setKeyboard(String language)
    {   keyboards[getAnnotationLevel()] = language;   }
    
    /** Method to return the current keyboard font */
    public String getKeyboard()
    {
        if (keyboards[layer] == null) 
            keyboards[layer] = KeyboardFonts.getLanguageFonts().getLanguage();
        return keyboards[layer];
    }
    
    /** Method to set all nodes visible */
    public void setAllVisible()
    {   AnnotationNode[] nodes = data[layer].getAnnotationNodes();
        
        int size = data[layer].getAnnotationSize();
        for (int n=0; n<size; n++) 
        { if (nodes[n]!=null) nodes[n].setVisible(true); }
    }

    /** Method to turn the highlight off all nodes */
    public void clearAllHighlights()
    { AnnotationNode[] nodes = data[layer].getAnnotationNodes();

        int size = data[layer].getAnnotationSize();
        for (int n=0; n<size; n++)
        { if (nodes[n]!=null) nodes[n].setHighlight(false); }
    }
    
    /** Method to get the total size in characters
     *  @return total number of characters
     */
    public int getTotalCharacters()
    {   int textSize, totalSize = 0;
        
        AnnotationNode[] nodes = data[layer].getAnnotationNodes();
        int size = data[layer].getAnnotationSize();
        for (int n=0; n<size; n++) 
        {   textSize = nodes[n].getText().length();
            if (textSize != 0) textSize+=1;
            totalSize += textSize;
        }
        return totalSize;
    }
   
   // Editor object with time domain data.
   private transient SoundEditor soundEditor;
   
  /** Set Sound Editor object */
   public void setSoundEditor(UndoRedo redoUndo)
   {   soundEditor = new SoundEditor(this, this, redoUndo); }
   
   /** Retrieve the soundEditor object */
   public SoundEditor getSoundEditor()
   {   return soundEditor;
   }
   
   /** Return this object for getSound */
   public AnnotationData getSound()
   { return this; }

   /** Set the text justification mode */
   public void setCentered(boolean justify)
   {   
	   setFileDirty();
       data[layer].setCentered(justify);
   }

   /** Determine if the current layer of annotation text is to be centered
    *  on display
    *
    * @return true if yes, false otherwise
    */
   public boolean isCentered() { return data[layer].isCentered(); }

   /** Determine if the indicated layer of text is to be centered on display
    *
    * @param thisLayer The layer number
    * @return true if yes, false otherwise
    */
   public boolean isCentered(int thisLayer)
   {return data[thisLayer].isCentered(); }
   
   /** Return the undo redo data for this object. */
   public UndoRedoData undoRedoObject()
   {   AnnotationData dataClone = this.clone();
       dataClone.soundEditor = null;
       return new SoundUndoRedoData(dataClone);
   }
   
   /** Make an identical copy of this object. */
   public @Override AnnotationData clone()
   {   AnnotationData newData = null;

       try
       {   newData = (AnnotationData)super.clone();
           newData.data = new AnnotationNodeArray[AcornsProperties.MAX_LAYERS];
           newData.keyboards = new String[AcornsProperties.MAX_LAYERS];
           
           if (data !=null)
           {
               for (int i=0; i<AcornsProperties.MAX_LAYERS; i++)
               {
                   newData.data[i] = data[i].clone();
                   newData.keyboards[i] = keyboards[i];
               }
           }
       }
       catch (Exception e)   
       { 
      	   Frame frame = JOptionPane.getRootFrame();
    	   JOptionPane.showMessageDialog(frame, "Couldn't clone AnnotationData"); 
       }
       return newData;
   }    
}   // End of AnnotationData class

