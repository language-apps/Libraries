/**
 * CategoryRelatedPhrases.java
 *
 *   @author  HarveyD
 *   @version 6.00
 *
 *   Copyright 2007-2015, all rights reserved
*/
package org.acorns.lesson.categories.relatedphrases;

import java.util.*;
import java.io.*;
import java.lang.reflect.Method;
import java.net.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.*;
import javax.swing.border.EtchedBorder;
import javax.swing.border.MatteBorder;
import javax.sound.sampled.*;
import org.w3c.dom.*;

import org.acorns.data.*;
import org.acorns.lesson.*;
import org.acorns.language.*;
import org.acorns.visual.*;

public abstract class CategoryRelatedPhrases extends Lesson implements Serializable
{
    private static final long serialVersionUID = 1;

    public static final int DEFAULT_FONT_SIZE = 20;
    public static final int LAYER_DIGITS = 2;
   
	private static final int SPACE = 5;   // Space in pixels between components
	private static final int ICON  = 25;  // Size of icons
	private static final int IMAGE = 60;  // Size of category image
	private static final Dimension SCORE_SIZE = new Dimension(500,IMAGE);
	private static final Dimension FEEDBACK_SIZE = new Dimension(500, 250);
	
	// Feedback options
	public static final int HELP = 0;
	public static final int CORRECT = 1;
	public static final int CLOSE = 2;
	public static final int INCORRECT = 3;
	public static final int SHOW = 4;


    private Vector<SentenceAudioPictureData>[] sentences;
    private ColorScheme colorScheme;

    private transient PhrasesSetupPanel setupPanel;
 
   /** Constructor to initialize the lesson
    * 
    * @param lessonName The name and category for this lesson separated with
    *           a semicolon
    */
   @SuppressWarnings("unchecked")  
   public CategoryRelatedPhrases(String lessonName)
   {
      super(lessonName);

      sentences = new Vector[AcornsProperties.MAX_LAYERS];
      colorScheme    = new ColorScheme(Color.BLUE, Color.WHITE);
      colorScheme.setSize(DEFAULT_FONT_SIZE);
      for (int i=0; i<AcornsProperties.MAX_LAYERS; i++)
      {
         sentences[i] = new Vector<SentenceAudioPictureData>();
      }
   }  // End CategoryRelatedPhrases()

   /** Constructor with a unique color/font scheme
    * 
    * @param lessonName name of lesson
    * @param scheme Object with colors, pictures, and fonts
    */
   @SuppressWarnings("unchecked")  
   public CategoryRelatedPhrases(String lessonName, ColorScheme scheme)
   {
      super(lessonName);

      sentences = new Vector[AcornsProperties.MAX_LAYERS];
      colorScheme  = scheme;
      colorScheme.setSize(DEFAULT_FONT_SIZE);
      for (int i=0; i<AcornsProperties.MAX_LAYERS; i++)
      {
         sentences[i] = new Vector<SentenceAudioPictureData>();
      }
   }  // End CategoryRelatedPhrases()
   
   

   /** Method to paste from another lesson of this category
    *
    * @param lessonObject The lesson to paste
    * @param lessonName The name of this lesson type
    */
   public CategoryRelatedPhrases(Object lessonObject, String lessonName)
   {
       this(lessonName);
       
       if (lessonObject instanceof CategoryRelatedPhrases)
       {
          CategoryRelatedPhrases lesson = (CategoryRelatedPhrases)lessonObject;
          colorScheme = (ColorScheme)(lesson.colorScheme).clone();

          for (int i=0; i<AcornsProperties.MAX_LAYERS; i++)
          {
             for (SentenceAudioPictureData temp: lesson.sentences[i])
             {
                 sentences[i].add(temp.clone());
             }
          }
       }
   }
   
   
   /** Method to convert from older lesson versions
    *
    * @param lessonObject The lesson to paste
    * @param lessonName The name of this lesson type
    */
   public CategoryRelatedPhrases
       (Vector<SentenceData>[] sentenceVector, ColorScheme c, String lessonName)
   {
       this(lessonName);
       colorScheme = c;
              
       for (int i=0; i<AcornsProperties.MAX_LAYERS; i++)
       {
          for (SentenceData data: sentenceVector[i])
              sentences[i].add(new SentenceAudioPictureData(data));
       }
   }
   
   /** Method to return the lesson category name */
   public String getCategory() 
   { 
       return "CategoryRelatedPhrases"; 
   }

   public Vector<SentenceAudioPictureData> getSentenceData()
   { 
	   int myLayer = getLayer();
	   return sentences[myLayer-1];
   }
   
   public Vector<SentenceAudioPictureData> getSentenceData(int layer)
   {
	   try
	   {
		   return sentences[layer-1];
	   }
	   catch (Exception e)
	   {
		   return null;
	   }
   }
   
   /** Method to get setup panel data for lesson display
    *
    * @return [0] is the data area and [1] is the control area
    */
   public JPanel[] getLessonData()
   {         
      JPanel[] panels = new JPanel[2];

      if (setupPanel==null) { setupPanel = new PhrasesSetupPanel(this); }
      panels[AcornsProperties.DATA]  = setupPanel;

      panels[AcornsProperties.CONTROLS] = SetupPanel.createSetupPanel
              (this, "RelatedPhrasesSetup", SetupPanel.FGBG + SetupPanel.FONT);
      return panels;
   }

  /** Method to get background/foreground color object (for the setup panel) */
  public ColorScheme getColorScheme() 
  { 
      return colorScheme; 
  }
  
  /** Method to return sentence data. */
  public Vector<SentenceAudioPictureData>[] getSentences()
  {
	  return sentences;
  }

  /** Method to insert a picture to this lesson
   *
   * @param url the source identifying the location of the picture
   * @param scaleFactor how the picture should be scaled
   * @param angle Rotation angle
   * @throws java.io.IOException
   */
  public void insertPicture(URL url, int scaleFactor, int angle)
                                               throws IOException
  {
     int row = -1;
     PictureData picture = new PictureData(url, null);
     picture.setAngle(angle);
     picture.setScale(scaleFactor);
     
     if (setupPanel!=null)  
     {
         row = setupPanel.getMouseRowPosition();
     }

     if (row>=0)
     {  int myLayer = getLayer();
        if (row>=sentences[myLayer-1].size())
        {  Toolkit.getDefaultToolkit().beep(); return; }

        SentenceData sentence = sentences[myLayer-1].get(row);
        LessonActionSentenceData oldData = new LessonActionSentenceData
                        (sentence.clone(), myLayer, row);

        sentence.setPicture(picture);
        pushUndo(oldData);
     }
     else
     {
        colorScheme.setPicture(picture);
        if (setupPanel!=null) setupPanel.repaint();
     }
     setDirty(true);
     setupPanel.repaint();
  }      // End insertPicture()

  /** Method to remove the picture from the lesson
   *
   * @param pictureNum unused
   */
  public void removePicture(int row)
  {
     if (row<0)  row = setupPanel.getSelectedRow();
     if (setupPanel!=null && (row>=0))
     {  int myLayer = getLayer();
        if (row<sentences[myLayer-1].size())
        {  SentenceData sentence = sentences[myLayer-1].get(row);
           LessonActionSentenceData oldData = new LessonActionSentenceData
                        (sentence.clone(), getLayer(), row);
           sentence.setPicture(null);
           pushUndo(oldData);
        }
     }
     else
     {
        try  {  colorScheme.setPicture(null); }
        catch (Exception e) {}  // Should never happen
     }
     setDirty(true);
     setupPanel.repaint();
  }

  /** Method to get the sound associated with this data
    *
    * @param sentence Which sentence within the selected layer
    * @param layer Which layer is it attached to
    * @param p The point x,y position (must be 0,0)
    * @param index which recorded sound attached to this picture
    * @return The sound data object
    */
   public SoundData getSoundData(int sentence, int layer, Point p, int index)
   {  
	  if (index<0) return sentences[layer-1].get(sentence).getSound();
	  
	  PicturesSoundData sounds = sentences[layer-1].get(sentence).getAudio();
      return sounds.getVector().elementAt(index);
   }

   /** Method to get aPictureData object corresponding to particular button
    *
    * @param pictureNum The button number to retrieve
    * @return The PictureData object
    */
   public PictureData getPictureData(int pictureNum)
   {   if (pictureNum==-1) return colorScheme.getPicture();
       else pictureNum -= 1000;

       int layerDivisor = (int)(Math.pow(10, LAYER_DIGITS));
       int pictureLayer = (int)(pictureNum/layerDivisor);
       pictureNum = pictureNum % layerDivisor;
       return sentences[pictureLayer-1].get(pictureNum).getPicture();
   }

   /** Methods for exporting, importing, and printing files */
  /** Method to create XML for file printing and exporting
   *
   * @param document The object holding the XML nodes
   * @param lessonNode The parent object within the document to link to
   * @param directory Path to export directory (null if just printing)
   * @param extensions for image or sound data (null if just printing)
   * @return true if successful, false otherwise
   */
   public boolean print(Document document, Element lessonNode
           , File directory, String[] extensions)
   {
	   return print(null, document, lessonNode, directory, extensions);
   }

   public boolean print(Lesson lesson, Document document, Element lessonNode
                                       , File directory, String[] extensions)
   {  // Get default extensions if object null
      if (extensions == null)
      {   extensions = new String[2];
          extensions[AcornsProperties.SOUND_TYPE] = "wav";
          extensions[AcornsProperties.IMAGE_TYPE] = "jpg";
      }

      // Get the lesson number index in this file
      int lessonNo
          = Integer.parseInt(lessonNode.getAttribute("number")) -1;

      Element imageNode, layerNode, fontNode, categoryNode;
      String[] values;

      PictureData picture = colorScheme.getPicture();
      if (picture != null)
      {
         values = new String[]
              {"-1", "", ""+picture.getScale(), ""+picture.getAngle() };
         imageNode = makeImageNode(document, directory
                           , extensions[AcornsProperties.IMAGE_TYPE]
                           , picture, values, lessonNo);
         lessonNode.appendChild(imageNode);
      }

      fontNode = makeFontNode(document, colorScheme);
      lessonNode.appendChild(fontNode);

      // Create records for each picture.
      PictureData  pictureData;
      PicturesSoundData picturesSounds;
      Vector<SoundData> soundVector;
      Point point = new Point(0,0);
      Vector<SentenceAudioPictureData>thisSentence;
      int size, multiplier = (int)(Math.pow(10,LAYER_DIGITS)), pictureNum;
      SentenceData sentence;

      for (int layerNo=1; layerNo<=sentences.length; layerNo++)
      {  thisSentence = sentences[layerNo-1];
         size = thisSentence.size();
         if (size>0)
         { 
             Class<?>[] params = new Class[1];
             params[0] = int.class;
             String specials = "";
             try
             {   
            	 if (lesson != null)
            	 {
            		 Class<?> lessonClass = lesson.getClass();
            		 Method getSpecials = lessonClass.getMethod
                        ("getSpecials", params);
            		 specials = (String)getSpecials.invoke(lesson,  layerNo);
            	 }
             }
             catch (Exception e)  {}

       	    String language = KeyboardFonts.getLanguageFonts().getLanguage();
               
        	values = new String[] {""+layerNo, language, specials};
            layerNode = makeNode
                    (document, "layer", new String[]{"value", "language", "specials"}, values);
            lessonNode.appendChild(layerNode);

            // Handle all sentences in a layer
            for (int p=0; p<size; p++)
            {  sentence = thisSentence.get(p);
               values = new String[] {""+p, sentence.getSentence()+""};
               categoryNode = makeNode
                    (document, "category", new String[]{"value", "name"}, values);
               layerNode.appendChild(categoryNode);

               pictureData = sentence.getPicture();
               if (pictureData!=null && pictureData.getFrame(0)!=null)
               {  pictureNum = 1000 + (layerNo*multiplier + p);
                  values = new String[] 
                               {""+pictureNum,"",""+pictureData.getScale(),""};
                  imageNode = makeImageNode(document, directory
                                     , extensions[AcornsProperties.IMAGE_TYPE]
                                     , pictureData, values, lessonNo);
                  categoryNode.appendChild(imageNode);
               }
               
               SoundData sound = thisSentence.get(p).getSound();
               if (sound!=null)
               {
            	   makeSoundNode(document, directory, point, categoryNode
            			    , extensions[AcornsProperties.SOUND_TYPE], sound
            	    		, -1, lessonNo, layerNo, p);  
               }

               picturesSounds = sentence.getAudio();
               soundVector = picturesSounds.getVector();
               if (soundVector.size()>0)
               {   makeListOfPointNodes( document, directory, soundVector
                                       , point, categoryNode
                                       , extensions[AcornsProperties.SOUND_TYPE]
                                       , lessonNo, layerNo, p);
               }  // End of handling a point for audio data
            }     // End processing all sentences in a layer
         }        // End if at least one sentence in layer
      }           // End if for layer
      return true;
   }              // End of print method

   /** Method to write multimedia in a standard format to a subdirectory
    *
    * @param directoryName Directory to hold the file to write
    * @param formats The audio and picture formats
    * @param lesson The lesson number
    * @return true if successful, false otherwise
    * @throws IOException
    */
   public boolean export
           (File directoryName, String[] formats, int lesson) 
        		   throws Exception
   {
      Vector<SoundData> soundVector;
      SoundData         sound;
      Point point = new Point(0,0);

      PictureData pictureData = colorScheme.getPicture();
      if (pictureData!=null)
      {   writeImage(pictureData, directoryName,
              lesson, -1, formats[AcornsProperties.IMAGE_TYPE]);
      }

      int size, pictureNum;
      int pictureMultiplier = (int)(Math.pow(10, LAYER_DIGITS));

      for (int layerNo=1; layerNo<=sentences.length; layerNo++)
      {  size = sentences[layerNo-1].size();
         for (int i=0; i<size; i++)
         {  pictureData = sentences[layerNo-1].get(i).getPicture();
            if (pictureData != null)
            {  pictureNum = 1000 + (i + layerNo*pictureMultiplier);
               writeImage(pictureData, directoryName,
                      lesson, pictureNum, formats[AcornsProperties.IMAGE_TYPE]);
            }
            
            sound = sentences[layerNo-1].get(i).getSound();
            if (sound != null)
            {
            	writeSound(sound, directoryName, lesson, i, layerNo
            		, point, -1, formats[AcornsProperties.SOUND_TYPE]);
            }
            soundVector = sentences[layerNo-1].get(i).getAudio().getVector();
            for (int j=0; j<soundVector.size(); j++)
            {    sound = (SoundData)soundVector.elementAt(j);
                 writeSound(sound, directoryName, lesson, i, layerNo,
                            point, j, formats[AcornsProperties.SOUND_TYPE]);
            }  // End of for loop through sound vector
         }     // End of for loop throught sentences
      }         // End of loop through the layers
      return true;
   }  // End of export method.

   /** Method to import a record to add the the lesson.
    *
    * @param myLayer The layer number
    * @param point The point applying to this import record
    * @param file The URL for the multimedia
    * @param data An array of data if this is a point object
    * @param type The type of import
    * @throws IOException
    * @throws UnsupportedAudioFileException
    */
   private transient int catNo = -1;
	public void importData
            (int myLayer, Point point, URL file, String[] data, int type)
	                            throws IOException, UnsupportedAudioFileException
	{
	   PicturesSoundData picturesSounds;
       Vector<SoundData> soundVector;
       SoundData         sound;

       // Set link if this is link data.
 	   if (type == AcornsProperties.LINK)   {  link = data[0];  }
       if (type == AcornsProperties.FONT)
       {
          if (!data[0].equals(""))
              colorScheme.changeColor(getColor(data[0]), true);
          if (!data[1].equals(""))
              colorScheme.changeColor(getColor(data[1]), false);
          if (!data[2].equals(""))
              colorScheme.setSize(Integer.parseInt(data[2]));
       }

       if (type==AcornsProperties.PICTURE)
       {  PictureData picture = new PictureData(file, null);
          picture.setScale(Integer.parseInt(data[1]));
          picture.setAngle(Integer.parseInt(data[2]));

          if (catNo<0)
          {  colorScheme.setPicture(new PictureData(file, null)); }
          else {  sentences[myLayer-1].get(catNo).setPicture(picture); }
       }

       if (type == AcornsProperties.CATEGORY)
       {  catNo = Integer.parseInt(data[0]);
          sentences[myLayer-1].add(new SentenceAudioPictureData(data[1]));
       }
       if (type != AcornsProperties.SOUND)  {  return; }

       // Otherwise process sound data.
       else
	   {  
    	   picturesSounds = sentences[myLayer-1].get(catNo).getAudio();
           sound = new SoundData(data);
	       if (file != null) sound.readFile(file);
	       
	       if (point.y < 0) sentences[myLayer-1].get(point.x).setSound(sound);
	       else
	       {
              soundVector = picturesSounds.getVector();
	          soundVector.add(sound);
	       }
       }
   }    // End of import method.

   /** Method to convert between ACORNS versions (none necessary)
    *
    * @param version The current version number
    * @return The same lesson object unconverted
    */
   public Lesson convert(float version) { return this; }

   /** Methods for managing the undo and redo options */
   /** Create a current undo redo object (null because will be overridden) */
   public  UndoRedoData getData()  {  return null;  }

   /** Redo an operation that was undone */
   public void redo(UndoRedoData dataRecord)
   { processUndoRedo(dataRecord, true); }

   /** Undo an operation that was incorrectly processed */
   public void undo(UndoRedoData dataRecord) 
   { processUndoRedo(dataRecord, false); }

   /** Method to process the undo or redo operation
    *
    * @param dataRecord The record data to restore
    * @param undo The stack to set
    */
   private void processUndoRedo(UndoRedoData dataRecord, boolean undo)
   {
      LessonActionSentenceData undoRedoData
                                   = (LessonActionSentenceData)dataRecord;
      SentenceAudioPictureData sentence  
                             = (SentenceAudioPictureData)undoRedoData.getData();
      int myLayer = undoRedoData.getLayer();
      int row = undoRedoData.getRow();

      SentenceAudioPictureData current;
      int rows = sentences[myLayer-1].size();
      if (row>=rows) current = null;
      else current = sentences[myLayer-1].get(row);

      LessonActionSentenceData newData
              = new LessonActionSentenceData(current, myLayer, row);

      getUndoRedoStack().replaceUndoRedoTop(newData, undo);

      if (sentence==null) sentences[myLayer-1].remove(row);
      else
      {
         if (row>=rows) sentences[myLayer-1].add(sentence);
         else sentences[myLayer-1].setElementAt(sentence, row);
      }
      setDirty(true);
      if (setupPanel != null) { setupPanel.repaint();  }
   }

  /** Determine if the lesson is playable
   *     There must be at least one magnet found
   *
   * @param layer The layer in question
   * @return error message if no, or null if yes
   */
   public String isPlayable(int layer)
   {  
	  Vector<SentenceAudioPictureData> sentenceData = getSentenceData(layer);
      PicturesSoundData audio;
      Vector<SoundData> soundData;
      String nativeText;

      if (sentenceData.size()==0)
    	  return LanguageText.getMessage("commonHelpSets", 95);

      for (int i=0; i<sentenceData.size(); i++)
      {  
    	 audio = sentenceData.get(i).getAudio();
         soundData = audio.getVector();
         if (soundData.size()==0)
         	return LanguageText.getMessage("commonHelpSets", 80);
        	 
         for (int j=0; j<soundData.size(); j++)
         { 
        	nativeText = soundData.get(j).getSoundText()[SoundData.NATIVE];
            if (nativeText==null || nativeText.length()==0) 
            	return LanguageText.getMessage("commonHelpSets", 94);
         	if (nativeText.split(" ").length < 2)
         		return LanguageText.getMessage("commonHelpSets", 94);
         }
      }
      return null;
   }

   /** Determine if related phrases have gloss, indigenous, and audio recorded
    * 
    * @param row The particular group of related phrases
    * @return true if complete, false otherwise
    */
   public boolean isRecordingComplete(int row)
   {
       Vector<SentenceAudioPictureData> data = getSentenceData();
       SentenceAudioPictureData sentence = data.get(row);
       PicturesSoundData pictureSound = sentence.getAudio();
       Vector<SoundData> soundVector = pictureSound.getVector();
       if (soundVector.isEmpty()) return false;
       
       for (SoundData sound: soundVector)
       {
           if (sound.getSoundText(SoundData.GLOSS).length()==0) return false;
           if (sound.getSoundText(SoundData.NATIVE).length()==0) return false;
           if (!sound.isRecorded()) return false;
       }
       return true;
   }
   
   /** Method to return play mode options
   *
   * @return array of options (scale factor in index 0).
   */
  public int[] getPlayOptions()
  {
     int[] options = new int[2];
     options[0] = colorScheme.getSize();
     return options;
  }
  
 /** Method to set options for play mode
   *
   * @param options array of lesson play mode options (none)scale factor for picture in index 0
   */
  public void setPlayOptions(int[] options)
  {
     if (options.length>=1)
     {
        if (options[0]>=ColorScheme.MIN_FONT_SIZE
                  && options[0]<=ColorScheme.MAX_FONT_SIZE)
        {  colorScheme.setSize(options[0]);  }
     }
  }
  
  /** Reformat based on some of the control characters when different phrases are merged
   * 
   * @param controlString The text to altered
   * @param left true if to alter the front part of the string, false if alter the back part
   * @return Altered control string
   */
  
  public String formatControlString(String controlString, boolean left)
  {
	   if (left)
	   {
		    // replace (stuff between) at front of control string by nothing
			controlString = controlString.replaceAll("(^[+]?[-]?)\\([^\\[\\]\\( ]*\\)(.*)", "$1$2");

			// replace [stuff between](stuff between) at front of control string by stuff between
			controlString = controlString.replaceAll("(^[+]?[-]?)\\[([^\\(\\)\\[ ]*)\\]\\([^\\[\\]\\( ]*\\)(.*)", "$1$2$3");

			// replace [stuff between] at front of control string by stuff between
			controlString = controlString.replaceAll("^([+]?[-]?)\\[([^\\(\\)\\[ ]*)\\](.*)", "$1$2$3");
	   }
	   else
	   {
		    // replace (stuff between) at back of control string by nothing
			controlString = controlString.replaceAll("(.*)\\([^\\[\\]\\( ]*\\)([-]?$)", "$1$2");

			// replace (stuff between)[stuff between] at back of control string by stuff between
			controlString = controlString.replaceAll("(.*)\\([^\\[\\]\\( ]*\\)\\[([^\\(\\)\\[ ]*)\\]([-]?$)", "$1$2$3");

			// replace [stuff between] at back of control string by stuff between
			controlString = controlString.replaceAll("(.*)\\[([^\\(\\)\\[ ]*)\\]([-]?$)", "$1$2$3");
	   }
	   return controlString;
  }

  
  /** Get the words of the sentence and format them for display
   * 
   * @param String to format
   * @param finish true to ignore words starting with '+'
   * @param first initial word of sentence
   * @param last word of sentence
   * @return Formatted string ready for display
   */
  public String getPhrasesForDisplay(String sentence, boolean finish, boolean first, boolean last)
  {
   	if (finish)
   	{
			sentence = sentence.replaceAll("\\+\\S*", "");
			sentence = sentence.replaceAll("\\([^\\(\\)\\] ]*\\)", "");
			sentence = sentence.replaceAll("[-]\\s+[-]", "");
	       	sentence = sentence.replaceAll("\\s+[-]", "");
	       	sentence = sentence.replaceAll("[-]\\s+", "");
	       	sentence = sentence.replaceAll("(\\S)\\.{3}(\\S)", "$1$2");
	       	sentence = sentence.replaceAll("  ", " ");
   	}
   	else
   	{
   		sentence = sentence.replaceAll("\\[[^\\(\\)\\[ ]*\\]", "");
   		sentence = sentence.replaceAll("^\\+", "");
   	}
   	
      	if (first) 
      	{
      		sentence = sentence.replaceAll("^[-]", "");
          	sentence.replaceAll("^\\.{3}", "");
      	}
      	if (last) {
      		sentence = sentence.replaceAll("[-]$", "");
          	sentence = sentence.replaceAll("\\.{3}$","");
      	}
      	
      	sentence = sentence.replaceAll("[\\(\\)\\[\\]]*", "");
      	sentence = sentence.replaceAll("\\.{3}\\s+", "");
      	sentence = sentence.replaceAll("\\s+\\.{3}", "");
		sentence = sentence.replaceAll("\\s+", " ");

       return sentence.trim();
  }
  
	/** Display answer and ask user for whether to skip the question or proceed
   * 
   * @param type Correct, close, accent issue, or incorrect
   * @param title The text to display at the top
   * @param category The category containing a set of sentences
   * @param sentence The current sentence or null for all
   * @return The user response to the dialog
   */
	public int feedback(JPanel parent, int type, String title, SentenceAudioPictureData category, SoundData sentence)
	{
		// Get data pertaining to this category
		SoundData categoryAudio = category.getSound(); 
        PictureData picture = category.getPicture();
	
        // Create the wrapping panel
        JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
		Dimension size = (type==CORRECT) ? SCORE_SIZE : FEEDBACK_SIZE;
		panel.setPreferredSize(size);
 
		// Configure the score panel
		JPanel topPanel = new JPanel();
		topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.X_AXIS));
		topPanel.add(Box.createHorizontalGlue());
		topPanel.setPreferredSize(SCORE_SIZE);
		topPanel.setMaximumSize(SCORE_SIZE);
		topPanel.setMinimumSize(SCORE_SIZE);
		topPanel.setSize(SCORE_SIZE);
		
     int fontSize = colorScheme.getSize();
     Font glossFont = new Font("", Font.PLAIN, fontSize);

     if (categoryAudio.isRecorded())
     {
        String tip = LanguageText.getMessage("commonHelpSets", 76);
        JButton audioButton  = toolTipButton(this, AcornsProperties.PLAY, tip);
		   audioButton.addActionListener( new ActionListener() {
			
		       @Override
		       public void actionPerformed(ActionEvent e) {
					categoryAudio.playBack(null,  -1,  -1);
		       }
		   });
		   topPanel.add(audioButton);
		   topPanel.add(Box.createHorizontalStrut(SPACE));
     }

		if (picture!=null)
		{
			ImageIcon icon = picture.getIcon(new Dimension(IMAGE, IMAGE));
			JLabel scoreIcon = new JLabel(icon);
			topPanel.add(scoreIcon);
		    topPanel.add(Box.createHorizontalStrut(SPACE));
		}

		double[] scores = Score.getScores();
		String answers = (scores[0]<=1) ? "" : "s";
		String results = "<html>" + LanguageText.getMessage("commonHelpSets",  89, 
				""+(int)scores[Score.CORRECT], 
				answers,
				""+(int)scores[Score.TOTAL], 
				""+(int)scores[Score.PERCENT]) + "</html>";

		JLabel scoreLabel = new JLabel(results);
	    scoreLabel.setFont(glossFont);
	    topPanel.add(scoreLabel);
		topPanel.add(Box.createHorizontalGlue());
	    topPanel.setBorder(new EtchedBorder(EtchedBorder.RAISED));
	    panel.add(topPanel);

	    // Get all of the sentences
	    Vector<SoundData> sentences = category.getAudio().getVector();
	    if (sentence!=null)
	    {
	       sentences = new Vector<SoundData>();
		   sentences.add(sentence);
	    }

        JPanel sentencePanel = new JPanel();

	    if (type != CORRECT)
	    {
	       boolean first = true;
	 	   for (SoundData audio: sentences)
	 	   {
	 		    String indigenous = audio.getSoundText(SoundData.NATIVE);
	 		    indigenous = getPhrasesForDisplay(indigenous, true, true, true);
	
	 		    String gloss = audio.getSoundText(SoundData.GLOSS);
	 		    String description = audio.getSoundText(SoundData.DESC);
	 		    String language = audio.getSoundText(SoundData.LANGUAGE);
			
				// Get native and normal fonts
		        Font indigenousFont = KeyboardFonts.getLanguageFonts().getFont(language);
	    
			    // Create the description panel components
			    JTextArea glossArea = new JTextArea(gloss);
		        glossArea.setLineWrap(true);
		        glossArea.setEditable(false);
		        glossArea.setFont(glossFont);
		        
		        if (!first)
		        {
			        MatteBorder border = new MatteBorder(5,0,0,0, Color.BLACK);
			        glossArea.setBorder(border);
		        }
		        first = false;
		    
		        JPanel textPanel = new JPanel();
		        textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.X_AXIS));
		        
		        JTextArea indigenousArea = new JTextArea(indigenous);
		        indigenousArea.setText(indigenous);
		        indigenousArea.setLineWrap(true);
		        indigenousArea.setEditable(false);
		        indigenousFont = new Font(indigenousFont.getFontName(), Font.PLAIN, fontSize);
		        indigenousArea.setFont(indigenousFont);
		        
		        String tip = LanguageText.getMessage("commonHelpSets", 76);
		        JButton audioButton  = toolTipButton(this, AcornsProperties.PLAY, tip);
				   audioButton.addActionListener( new ActionListener() {
					
				       @Override
				       public void actionPerformed(ActionEvent e) {
							audio.playBack(null,  -1,  -1);
				       }
				   });
				textPanel.add(audioButton);
				textPanel.add(indigenousArea);

		        JTextArea descriptionArea = new JTextArea(description);
				if (description.length()>0)
				{
		        	descriptionArea.setLineWrap(true);
		        	descriptionArea.setEditable(false);
		        	descriptionArea.setFont(glossFont);
				}
				
		        sentencePanel.setLayout(new BoxLayout(sentencePanel, BoxLayout.Y_AXIS));
		        sentencePanel.add(glossArea);
		        sentencePanel.add(Box.createVerticalStrut(SPACE));
		        sentencePanel.add(textPanel);
		        sentencePanel.add(Box.createVerticalStrut(SPACE));
		        
		        if (description.length()>0)
		        {
		        	sentencePanel.add(descriptionArea);
		        	sentencePanel.add(Box.createVerticalGlue());
		        }
		        
		        
		    }	// End of loop through sentences
	    }		// End if type
	   
	    if (type != CORRECT)
	    {
	        JScrollPane mainPanel = new JScrollPane(sentencePanel);
	        javax.swing.SwingUtilities.invokeLater(new Runnable() {
	        	  public void run() {
	        	    mainPanel.getVerticalScrollBar().setValue(0);
	        	    Rectangle bounds = mainPanel.getVisibleRect();
	        	    
	        	    int width = sentencePanel.getWidth();
	        	    int height = sentencePanel.getHeight();
	        	    int newWidth = bounds.width - 50;
	        	    int newHeight = (width * height) / newWidth;
	        	    Dimension newSize = new Dimension(newWidth, newHeight);
	        	    sentencePanel.setPreferredSize(newSize);
	        	  }		    
	        });
	        panel.add(Box.createVerticalStrut(SPACE));
		    panel.add(mainPanel);
	    }
	    
	    panel.add(Box.createVerticalGlue());

	    // Configure JOptionPane call
      ImageIcon icon = getIcon(AcornsProperties.ACORN, ICON);
    
      String[] options = {""};
      int optionType = JOptionPane.OK_OPTION;
      switch (type)
      {
      	case CORRECT:
      		options = LanguageText.getMessageList("commonHelpSets",87);
  	      	optionType = JOptionPane.YES_NO_OPTION;
    	    	break;
          case INCORRECT:
          	optionType = JOptionPane.YES_NO_OPTION;
          	options = LanguageText.getMessageList("commonHelpSets", 91);
          	break;
          case CLOSE:
          case HELP:
          case SHOW:
          	options = LanguageText.getMessageList("commonHelpSets", 90);
          	break;
          	
    }
    
    int choice = JOptionPane.showOptionDialog(parent, panel,
            title,
            optionType,
            JOptionPane.PLAIN_MESSAGE,
            icon,
            options,
            options[0]);
    
    return choice;
 }  // End of Feedback()
	   
 /** Create a button object with a string tooltip */
 public JButton toolTipButton(Lesson lesson, int iconNo, String toolTip)
 {
	   JButton button = new JButton();
 	   ImageIcon icon = lesson.getIcon(iconNo, ICON);
       button.setIcon(icon);
       button.setToolTipText(toolTip);
	   button.setMargin(new Insets(0, 0, 0, 0));
	   return button;
 }


}  // End of CategoryRelatedPhrases class