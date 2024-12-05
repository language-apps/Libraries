/*
 * PicturesSoundData.java
 *
 *   @author  HarveyD
 *   @version 3.00 Beta
 *
 *   Copyright 2007-2015, all rights reserved
 */
package org.acorns.data;

import java.awt.Component;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.FileDialog;
import java.awt.Frame;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.Serializable;
import java.util.Vector;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;

import org.acorns.audio.Annotation;
import org.acorns.audio.AudioDropTarget;
import org.acorns.audio.PlayBack;
import org.acorns.audio.SoundDefaults;
import org.acorns.language.KeyboardFonts;
import org.acorns.language.LanguageText;
import org.acorns.lesson.AcornsProperties;
import org.acorns.lesson.AcornsTransfer;
import org.acorns.lesson.Lesson;
import org.acorns.lesson.categories.multiplepictures.CategoryMultiplePictures;
import org.acorns.lib.DialogFilter;

/** Hold a vector of SoundData objects and the associated gloss/native text.
    It also holds objects for linking to other lessons (please refer to
    Picture and Sound lessons with links that can appear on a picture
 */
public class PicturesSoundData extends UndoRedoData 
                              implements ActionListener, Serializable, Cloneable
{
   /** Java serial file version number. */
   private static final long serialVersionUID = 1;

   /** Standard dialog without culturally relevant data */
   public static final int STANDARD = 0;
   /* Enter culturally relevant data */
   public static final int DESCRIPTION = 1;
   
   private static final int GLOSS_SIZE = 30, IPA_SIZE = 30, STRUT_SIZE=10;
   private static final int DESC_ROWS = 6, DESC_COLS = 60;
   private static final int MAX_SOUNDS = 999;
   
   private Vector<SoundData> soundVector;
   private boolean soundFlag;
   private String  text;
   
   // Temporary data used by the methods.
   private transient SoundData sound; 
   private transient JButton recordButton, playButton, stopButton, browseButton;
   private transient JButton copyButton, pasteButton;
   private transient Lesson lesson;
   private transient JTextField glossText, ipaText;
   private transient JTextArea  description;
   private transient JPanel dialogPanel;
   private transient JComboBox<String> combo;
   private transient String[] soundText;
   private transient String descText;
   
   private transient JCheckBox excludeCheck;
   
   /** Constructor for objects linking to lessons (no audio).
    *
    * @param text The name of a lesson to link to
    */
   public PicturesSoundData(String text)
   {
      soundFlag = false;
      this.text = text;
      soundVector = null;
   }
	
   /** Constructor for objects with sound data. */
   public PicturesSoundData()
   {
      soundFlag   = true;
      text        = null;
      soundVector = new Vector<SoundData>();
   }
   
   /** Constructor for text with or without sound
    *
    * @param text The text associated with  this object
    * @param hasSound Link or Audio object (true if audio)
    */
   public PicturesSoundData(String text, boolean hasSound)
   {  soundFlag = hasSound;
      this.text = text;
      
      if(soundFlag) soundVector = new Vector<SoundData>();
      else soundVector = null;
   }

   /** Method to return the vector of SoundData objects */
   public Vector<SoundData> getVector()	{ return soundVector; }
	
  /** Method to return the text String associated with this object. */
  public String getText() { return text;    }
	
   /** Method to set a text String associated with this object */
   public void   setText(String text)	{ this.text = text; }
	
   /** Method to return true if this is a sound, false if it is a link */
   public boolean isSound()  { return soundFlag; }
   
   /** Method needed to complete an UndoRedoData abstract method */
   public PicturesSoundData getData() {return this;}

   /** Handle recoding sounds with annotated gloss and native data
    *  @param lesson object
    *  @return result of the dialog
    *    JOptionPane.YES_OPTION - new recording
    *    JOptionPane.NO_OPTION  - deleted recording
    *    JOptionPane.CANCEL_OPTION - canceled dialog
    */
   public int pictureDialog(Lesson lesson, Component root)
   { return pictureDialog(lesson, root, STANDARD); }

   public int pictureDialog(Lesson lesson, int additionalOption)
   {
       Frame root = lesson.getRootFrame();
       return pictureDialog(lesson, root, additionalOption);
   }

   /* Handle recoding sounds with annotated gloss and native data
    *  @param lesson object
    *  @param root Frame to use to attach dialog to
    *  @param additionalOptions
    *     PictureSoundData.DESCRIPTION for entry of description data
    *  @return result of the dialog
    *    JOptionPane.YES_OPTION - new recording
    *    JOptionPane.NO_OPTION  - deleted recording
    *    JOptionPane.CANCEL_OPTION - canceled dialog
    */
   public int pictureDialog(Lesson lesson, Component root, int additionalOptions)
   {
      SoundData oldSound = null;
      boolean exclude = lesson instanceof CategoryMultiplePictures;
  
      // Error if this is not an acorn.
      if (!isSound())    {  Toolkit.getDefaultToolkit().beep();
                            return JOptionPane.CANCEL_OPTION;
                         }
      
      // Get a paste object, if one exists
      AcornsTransfer transfer = null;
      try
      {
        transfer = AcornsTransfer.getCopiedObject
        		              (AcornsTransfer.ACORNTYPE);
        if (transfer.getPicturesSoundData() == null)
			transfer = null;
      }
      catch (Exception e) {}

      int selection = soundVector.size();
      if (selection == MAX_SOUNDS)
      {
          JOptionPane.showMessageDialog
           (root, LanguageText.getMessage("commonHelpSets", 46, ""+MAX_SOUNDS));
          return JOptionPane.CANCEL_OPTION;
      }
      int result;
      this.lesson = lesson;
      
      // Create panel for dialog.
      dialogPanel = new JPanel();

      new AudioDropTarget(dialogPanel, this);
      BoxLayout box = new BoxLayout(dialogPanel, BoxLayout.Y_AXIS);
      dialogPanel.setLayout(box);
        
      String[] recordingText = LanguageText.getMessageList("commonHelpSets",57);
      pasteButton = new JButton(lesson.getIcon(AcornsProperties.PASTE, 20));
      pasteButton.addActionListener(new ActionListener() 
      {
          public void actionPerformed(ActionEvent e) 
          {
              JOptionPane pane = getOptionPane((JComponent)e.getSource());
              pane.setValue(pasteButton); 
          }
      });
      pasteButton.setToolTipText(recordingText[4]);
      
      if (exclude)
      {
    	  excludeCheck = new JCheckBox(recordingText[5], false);
    	  excludeCheck.addActionListener(
    			  new ActionListener()
    			  {
    				  public void actionPerformed(ActionEvent event)
    				  {
    					  JCheckBox box = (JCheckBox)(event.getSource());
    		              text = (box.isSelected()) ? "yes" : "no";
    				  }
    			  });
    	  
    	  if (text!=null && text.equals("yes")) 
    		  excludeCheck.setSelected(true);
      }

      if (selection !=0)
      {
         // Create component holding title for list of sounds.
         JPanel descPanel = new JPanel();
         box = new BoxLayout(descPanel, BoxLayout.X_AXIS);
         descPanel.setLayout(box);
          
         JLabel listDescription 
                 = new JLabel(LanguageText.getMessage("commonHelpSets", 47));
         descPanel.add(Box.createHorizontalGlue());
         descPanel.add(listDescription);
         descPanel.add(Box.createHorizontalGlue());
         dialogPanel.add(descPanel);
            
         String[] dataArray  = new String[soundVector.size()];
         
         // Create scrollable selection list.
         String[] pictureSoundText;
         for (int i=0; i<soundVector.size(); i++)
         {  
            pictureSoundText = (soundVector.elementAt(i)).getSoundText();
            if (pictureSoundText[0]==null || pictureSoundText[0].trim().length()==0) 
                 dataArray[i] = LanguageText.getMessage("commonHelpSets", 50);
            else dataArray[i] = pictureSoundText[0];
         }
         JList<String> jlist = new JList<String>(dataArray);
         jlist.setToolTipText(LanguageText.getMessage("commonHelpSets", 48));

         jlist.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
         jlist.clearSelection();
         JScrollPane scroll = new JScrollPane(jlist);
         scroll.getVerticalScrollBar().setUnitIncrement(30);
         scroll.setPreferredSize(new Dimension(150,100));
         dialogPanel.add(scroll);
             
         // Create title for dialog.
         String title = LanguageText.getMessage("commonHelpSets", 49);
             
         // Customize the option buttons.
         copyButton = new JButton(lesson.getIcon(AcornsProperties.COPY, 20));
         copyButton.addActionListener(new ActionListener() 
         {
             public void actionPerformed(ActionEvent e) 
             {
                 JOptionPane pane = getOptionPane((JComponent)e.getSource());
                 pane.setValue(copyButton); 
             }
         });
         copyButton.setToolTipText(recordingText[3]);
         
         Object[] opts = LanguageText.getMessageList("commonHelpSets", 51);
         Object options[] = new Object[opts.length + 1];
         
         if (transfer!=null)
         {
        	 options = new Object[opts.length + 2];
         }
 
         if (exclude)
         {
        	 options = new Object[options.length + 1];
             options[options.length-1] = (Object)excludeCheck;
         }
 
         for (int i=0; i<opts.length; i++)
         {
        	 options[i] = opts[i];
         }
         options[opts.length] = (Object)copyButton;
         
         if (transfer!=null)
         {
        	 options[opts.length+1] =  (Object)pasteButton;
         }
              
         // Loop until a valid input is received.
         do
         {
            // Display dialog and get user response. 
            result = JOptionPane.showOptionDialog(root, dialogPanel, title
                  , JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE
                  , null, options, options[2]);
            
            if (result==opts.length)
            {
            	transfer = new AcornsTransfer(this);
            	transfer.copyObject();
            	return JOptionPane.CANCEL_OPTION;
            }
            
            /** Execute the paste option */
            if (result==opts.length+1)  
            { 
            	return pasteObject(transfer); 
            }
            
            // We're done if the user cancels.                    
            if (result == JOptionPane.CANCEL_OPTION) return result;
            if (result < 0) return result;

            selection = jlist.getSelectedIndex();
            if (result == JOptionPane.YES_OPTION) 
               selection = soundVector.size(); 
                 
            if (jlist.isSelectionEmpty() && result!=JOptionPane.YES_OPTION)
            {
               lesson.setText(LanguageText.getMessage("commonHelpSets", 52));
            }                 
             
         }  while (jlist.isSelectionEmpty() && result!=JOptionPane.YES_OPTION);
      }  // End of if entry contains sounds.
              
      // Process modify or insert.
      dialogPanel.removeAll();
            
      // Configure work area for maintaining sound data.
      soundText = new String[3];
      soundText[0] = "";
      soundText[1] = "";
      soundText[2] = KeyboardFonts.getLanguageFonts().getLanguage();

      sound = null;
      
      if (selection != soundVector.size()) 
      {
         sound= soundVector.elementAt(selection);
         String[] newSoundText = sound.getSoundText();
         {  for (int i=0; i<soundText.length; i++)
            {  if (i<newSoundText.length)
               {  soundText[i] = newSoundText[i]; }
            }
            oldSound = sound.clone();
         }
      }
	  else sound = new SoundData(soundText);

      //Drop down menu for native language selections.
      JPanel comboPanel = new JPanel();
      comboPanel.setLayout(new BoxLayout(comboPanel, BoxLayout.X_AXIS));
      comboPanel.add(Box.createHorizontalGlue());
      String[] labelText = LanguageText.getMessageList("commonHelpSets", 54);
      JLabel fonts = new JLabel(labelText[0]);
      comboPanel.add(fonts);

      // Create combo box with the selected language selected.
      combo = KeyboardFonts.getLanguageFonts().createLanguageComboBox(soundText[2], false);
      Dimension COMBOSIZE = new Dimension(150,20);
      combo.setToolTipText(LanguageText.getMessage("commonHelpSets", 53));
      combo.setPreferredSize(COMBOSIZE);
      combo.setSize(COMBOSIZE);
      combo.setMaximumSize(COMBOSIZE);
      combo.setMinimumSize(COMBOSIZE);
      
      // Add the listener for combo selections.
      combo.addActionListener(
              new ActionListener()
              {
                 public void actionPerformed(ActionEvent ae)
                 {
                    soundText[2] = (String)combo.getSelectedItem();
                    KeyboardFonts.getLanguageFonts().setFont(soundText[2], ipaText);
                    ipaText.repaint();                                        
                 }
             }
          );

      comboPanel.add(combo);
      dialogPanel.add(comboPanel);
      dialogPanel.add(Box.createVerticalStrut(STRUT_SIZE));
     
      // Create text or label fields.
      JPanel textPanel = new JPanel();
      box = new BoxLayout(textPanel, BoxLayout.Y_AXIS);
      textPanel.setLayout(box);

      JPanel panel = new JPanel();
      panel.add(new JLabel(labelText[1]));
      textPanel.add(panel);
      glossText = new JTextField(soundText[0], GLOSS_SIZE);
      glossText.setToolTipText(LanguageText.getMessage("commonHelpSets", 55));
      textPanel.add(glossText);
      textPanel.add(Box.createVerticalStrut(STRUT_SIZE));

      panel = new JPanel();
      panel.add(new JLabel(labelText[2]));
      textPanel.add(panel);
      ipaText   = new JTextField(soundText[1], IPA_SIZE);
      ipaText.setToolTipText
              (LanguageText.getMessage("commonHelpSets", 56));

      // Set the keyboard for the native language component. 
      KeyboardFonts.getLanguageFonts().setFont(soundText[2], ipaText);
      textPanel.add(ipaText);
      textPanel.add(Box.createVerticalStrut(STRUT_SIZE));

      if ( (additionalOptions&DESCRIPTION)!=0)
      {  panel = new JPanel();
         panel.add(new JLabel(labelText[4]));
         textPanel.add(panel);
         description = new JTextArea(DESC_ROWS, DESC_COLS);
         description.setText(sound.getSoundText(SoundData.DESC));
         description.setLineWrap(true);
         description.setWrapStyleWord(true);
         description.setToolTipText
              (LanguageText.getMessage("commonHelpSets", 75));

         textPanel.add(new JScrollPane(description));
      }
      dialogPanel.add(textPanel);
  
      // Create a panel of buttons for the sound file.
      ImageIcon[] icons = new ImageIcon[4];
      icons[0] = lesson.getIcon(AcornsProperties.RECORD, 30);
      icons[1] = lesson.getIcon(AcornsProperties.PLAY,   30);
      icons[2] = lesson.getIcon(AcornsProperties.STOP,   30);
      icons[3] = lesson.getIcon(AcornsProperties.BROWSE, 30);
         
      JPanel buttons = new JPanel();
      box = new BoxLayout(buttons, BoxLayout.X_AXIS);
      recordButton = new JButton(icons[0]);
      recordButton.addActionListener(this);
      recordingText = LanguageText.getMessageList("commonHelpSets",57);
      recordButton.setToolTipText(recordingText[0]);
      buttons.add(recordButton);
      playButton   = new JButton(icons[1]);
      playButton.setToolTipText(recordingText[1]);
      playButton.addActionListener(this);
      buttons.add(playButton);
      stopButton   = new JButton(icons[2]);
      stopButton.setToolTipText(recordingText[2]);
      stopButton.addActionListener(this);
      buttons.add(stopButton);
      browseButton = new JButton(labelText[3], icons[3]);
      browseButton.setToolTipText(LanguageText.getMessage("commonHelpSets",59));
      browseButton.addActionListener(this);
      buttons.add(browseButton);
      dialogPanel.add(buttons);
      
      // Display dialog and get user response.           
      // Customize the option buttons
      String[] opts = LanguageText.getMessageList("commonHelpSets", 58);
      Object options[] = new Object[opts.length];
      if (transfer!=null && pasteButton != null)
      {
     	 options = new Object[opts.length + 1];
      }
      
      if (exclude)
      {
    	  options = new Object[options.length + 1];
          options[options.length-1] = (Object)excludeCheck;
      }
  
      for (int i=0; i<opts.length; i++) { options[i] = opts[i]; }

      if (transfer!=null)
      {
     	 options[opts.length] =  (Object)pasteButton;
      }
 
      String title = LanguageText.getMessage("commonHelpSets",49);
      result  = JOptionPane.showOptionDialog(root, dialogPanel, title
                    , JOptionPane.OK_OPTION, JOptionPane.PLAIN_MESSAGE
                    , null, options, options[2]);

      // Stop recording and any play back that is active.
      if (sound.isActive()) sound.stopSound();
      PlayBack.stopPlayBack();
      
      if (result==opts.length) 
      { 
    	  return pasteObject(transfer);
      }
 
      // We're done if the user cancels.                    
      if (result == JOptionPane.CANCEL_OPTION) 
      {   if (selection!=soundVector.size())
          {  soundVector.setElementAt(oldSound, selection);
          }
          return result;
      }

      soundText[0] = glossText.getText();
      soundText[1] = ipaText.getText();

      sound.setSoundText(soundText);
      if (description!=null)
      {  descText = description.getText();
         sound.setSoundText(SoundData.DESC, descText);
      }

      if (result == JOptionPane.YES_OPTION)
      {
         // Process insert option.  
         if (selection == soundVector.size())
         {  soundVector.add(sound); }
         else  // Modify an element.
         {  soundVector.setElementAt(sound, selection);   }
      }  // End of insert or modify option.
         
      // Process delete option.
      if (result == JOptionPane.NO_OPTION)
      {
         if (selection != soundVector.size())
         {   soundVector.removeElementAt(selection);   }
      }  // End of delete option.
 
      return result;
     
   }  // End of picturesDialog method.

   /** Internal Method to handle user drag & drop of audio file
    *
    * @param audio The dropped file object
    */
   public void audioDropped(File audio)
   {  if (sound.isActive()) { sound.stopSound(); }
      try { 
    	  sound.readFile(audio); 
       }
      catch (Exception ex) { Toolkit.getDefaultToolkit().beep(); }
   }

   /** Internal method to respond to mouse clicks */
   public void actionPerformed(ActionEvent event)
   {
      if (event.getSource() == recordButton)
      {
         if (sound == null)
         {
             lesson.setText(LanguageText.getMessage("commonHelpSets", 60));
             return;
         }
         if (sound.isActive()) sound.stopSound();
         sound.record(recordButton);
      } 
        
      if (event.getSource() == playButton)
      {
         if (sound!=null && sound.isActive()) sound.stopSound();
         if (sound==null || !sound.playBack(null,0,-1))
         {
            lesson.setText(LanguageText.getMessage("commonHelpSets", 27));
            return;
         }
      }
       
      if (event.getSource() == stopButton)
      {
         if (sound==null || !sound.isActive())
         {           
            lesson.setText(LanguageText.getMessage("commonHelpSets", 61));
         }
         else sound.stopSound();
      }
         
      if (event.getSource() == browseButton)
      {
         if (sound != null && sound.isActive())
         {   
             lesson.setText(LanguageText.getMessage("commonHelpSets", 62));
             return;
         }
			      
         File file = chooseFile();
            
         if (file != null)
         {
            if (!file.exists()) 
            {
               lesson.setText
                (LanguageText.getMessage("commonHelpSets", 64, file.getName()));
               return;
            }

            try 
            { 
               String fullName = file.getCanonicalPath();
               
               // See if an annotation file exists.
               int extensionSpot = fullName.lastIndexOf(".");
               if (extensionSpot>0)
               {   String xmlName = fullName.substring(0,extensionSpot)+".xml";
                   File xmlFile = new File(xmlName);
                   if (xmlFile.exists())
                   {  Annotation annotation = new Annotation();
                      try
                      {
                         sound =  annotation.importXML(xmlFile);
                         String[] annotations = sound.getSoundText();
                         if (annotations.length>2)
                         {
                             String[] languages 
                              = KeyboardFonts.getLanguageFonts().getLanguages();
                             for (int k=0; k<languages.length; k++)   
                             {   if (languages[k].toLowerCase()
                                         .equals(annotations[2].toLowerCase())) 
                                     combo.setSelectedIndex(k);
                             }
                         }
                         if (annotations.length>0)
                             glossText.setText(annotations[0]);
                         if (annotations.length>1)
                             ipaText.setText(annotations[1]);
                         dialogPanel.validate();                    
                         dialogPanel.validate();
                      } catch (Exception e) {}
                   }
                   else sound.readFile(file);
               }
               String name = file.getName();
               int lastIndex = fullName.lastIndexOf(name);
               if (lastIndex<0) 
               {
                  lesson.setText(LanguageText.getMessage("commonHelpSets", 65));
                  return;
               }
               String path = fullName.substring(0,lastIndex-1);
               lesson.setPath(AcornsProperties.SOUNDS, path);
            }
            catch(Exception e)
            {  
               lesson.setText(LanguageText.getMessage("commonHelpSets", 66));
            }
         }
      }  // End of if browse button
   }     // End of actionPerformed()
   
   /** Method to convert from this object to the new version 
    *     Nothing to do, was already converted by Tools.PictureSoundData     
    **/
   public org.acorns.data.PicturesSoundData convert(float version)
   {
       return this;
   }
   
   /** Create an identical copy of this object */
   @SuppressWarnings("unchecked")
   public @Override PicturesSoundData clone()
   {
       try
       {   PicturesSoundData newObject = (PicturesSoundData)super.clone();
           newObject.soundVector = (Vector<SoundData>)soundVector.clone();
           for (int i=0; i<soundVector.size(); i++)
           {  newObject.soundVector.set(i, soundVector.get(i).clone());  }
           
           // Clear the temporary variables.
           newObject.sound = null; 
           newObject.recordButton = newObject.playButton
                                  = newObject.stopButton = null;
           newObject.browseButton = null;
           newObject.lesson = null;
           newObject.glossText = newObject.ipaText = null;
           newObject.dialogPanel = null;
           newObject.combo = null;
           newObject.soundText = null;

           return (PicturesSoundData)newObject; 
       }
       catch (Exception e) 
       { JOptionPane.showMessageDialog
                 (null, "Couldn't clone PicturesSoundData"); }
       return null;
   }
   
   /** Paste a copied PicturesSoundData object into this object
    * 
    * @param transfer The object containing transfer data
    * @return Dialog cancel code
    */
   private int pasteObject(AcornsTransfer transfer)
   {
	   PicturesSoundData data = transfer.getPicturesSoundData();
   	   soundVector = data.getVector();
   	   soundFlag = data.isSound();
   	   text = data.getText();
   	   return JOptionPane.YES_OPTION;
   } 
   
   private JOptionPane getOptionPane(JComponent parent) 
   {
       JOptionPane pane = null;
       if (!(parent instanceof JOptionPane)) 
       {
           pane = getOptionPane((JComponent)parent.getParent());
       } else 
       {
           pane = (JOptionPane) parent;
       }
       return pane;
   }
   
   /** Configure the file chooser to be compatible with both MacOS and Windows
    * 
    * @param title The text to appear at the top of the dialog
    * @param dialog The appropriate file filter
    * @return Selected File object or null if cancelled
    */
   private File chooseFile()
   { 
       String dialogTitle = LanguageText.getMessage("soundEditor",  197);
       String[] extensions = SoundDefaults.getAudioWriterExtensions();
       DialogFilter dialog = new DialogFilter(dialogTitle, extensions);

       String title = LanguageText.getMessage("commonHelpSets", 63);
	   
	   File file = null;
	   String path = lesson.getPath(AcornsProperties.SOUNDS);
	   Frame root = lesson.getRootFrame();

	   String osName = System.getProperty("os.name");
       if (!osName.contains("Mac")) 
       {  
     	   final JFileChooser fc = new JFileChooser(path);
           fc.setDialogTitle(title);
           fc.addChoosableFileFilter(dialog);
           fc.setAcceptAllFileFilterUsed(false);
 
           int returnVal;
    	   returnVal = fc.showOpenDialog(root);

     	   if (returnVal == JFileChooser.APPROVE_OPTION)
           {
               file = fc.getSelectedFile();
           }
      }
      else
      {   
          FileDialog fd = new FileDialog(new Dialog(root), title,FileDialog.LOAD);
          fd.setDirectory(path);
          fd.setFilenameFilter(dialog);
          fd.setVisible(true);
          
          String fileName = fd.getFile();
          String directory = fd.getDirectory();
          
          if (fileName != null && fileName.length()!=0)
          {
              file = new File(directory + fileName);
          }
      }
      
      return file;
   }
    
 }        // End of PicturesSoundData class
