/*
 * SoundListener.java
 *
 *   @author  HarveyD
 *   @version 3.00 Beta
 *
 *   Copyright 2007-2015, all rights reserved
*/
package org.acorns.editor;

import java.awt.Dialog;
import java.awt.FileDialog;
import java.awt.Frame;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.FileNotFoundException;

import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioSystem;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

import org.acorns.audio.Annotation;
import org.acorns.audio.SoundDefaults;
import org.acorns.data.AnnotationData;
import org.acorns.data.Annotations;
import org.acorns.data.SoundData;
import org.acorns.data.SoundUndoRedoData;
import org.acorns.data.UndoRedo;
import org.acorns.language.KeyboardFonts;
import org.acorns.language.LanguageText;
import org.acorns.properties.AcornsProperties;

/**
 *
 * Class to listen for SoundPanel button depressions.
 */
public class SoundListener
{
    private static final int LOAD = 0;
    private static final int SAVE = 1;
    
    protected SoundPanel panel;              // Panel attached to this listener
    protected JLabel     label;              // Label for error messages
    protected JTextField nativeText;         // Text field for native text
    protected JTextField glossText;          // Text for gloss text
    protected JComboBox<String>  combo;              // Combo box with language type
    
    private   SoundProperties properties;    // System properties objects
    private   AcornsProperties acornsProperties;

    protected RootSoundPanel soundPanelProperties;
    
    protected UndoRedo    undoRedo;          // Undo and Redo stack
    
    // The constructyor creates the following; the listener maintains it
    protected String[]    glossNative;       // Array to hold annotation text

    // The following are initialized when the listener is first called
    protected SoundDisplayPanel wavePanel;     // Panel displaying sound waves
    
    // Determine the radio button sound extension chosen during an export
    protected String soundSelect;
    
    // Principle data maintained.
    protected SoundEditor editor;                 
    protected SoundUndoRedoData undoRedoData;
    protected AnnotationData newSound = null;         // Record into a recording.
    private ActionEvent event;      // Is event in process?
    private int lastLayer;          // Did we change layers
    private String chooseFileName;	// The name of the current audio file
    
    /** Creates a new instance of ButtonListener 
     *  @param panel panel to which tis listener is attached
     *  @param label JLabel to display errors and messages.
     *  @param glossText JTextField for entering gloss text annotations.
     *  @param nativeText JTextField for entering native text annotations.
     *  @param combo combo box holding native keyboard mappings.
     */
    public SoundListener(SoundPanel panel, JLabel label
            , JTextField glossText, JTextField nativeText, JComboBox<String> combo) 
    {
        this.panel      = panel;
        this.label      = label;
        this.glossText  = glossText;
        this.nativeText = nativeText;
        this.combo      = combo;
        this.lastLayer  = -1;
        
        glossNative = new String[3]; // gloss, native, and language.
        
        // Get the ACORNS property change listener.
        acornsProperties = AcornsProperties.getAcornsProperties();

        PropertyChangeListener[] pcl 
              = Toolkit.getDefaultToolkit().getPropertyChangeListeners
                                               ("SoundListeners");
        if (pcl.length>0) soundPanelProperties = (RootSoundPanel)pcl[0];

        if (soundPanelProperties != null) 
        {   undoRedo = soundPanelProperties.getUndoRedo();
        }
        if (undoRedo==null) undoRedo = new UndoRedo();

        pcl 
         = Toolkit.getDefaultToolkit().getPropertyChangeListeners("Properties");
       
        properties = null;
        for (int i=0; i<pcl.length && properties==null; i++)
        { 
           try
           {   properties = (SoundProperties)pcl[0]; 
           }
           catch (ClassCastException ic) {}
        }

        label.setText("");   // Clear error message label.
        
    }  // End SoundListener constructor.

    /** Method to set the sound Editor if it is not already set */
    public void setSoundEditor()
    {
        if (editor!=null) return;

        Annotations annotations
                = soundPanelProperties.getAnnotationData(panel);
        setSoundEditor(annotations);
    }

    /** Method to set the sound Editor using an Annotations object */
    public void setSoundEditor(Annotations annotations)
    {   editor = null;
        System.gc();
        annotations.setSoundEditor(undoRedo);
        editor = annotations.getSoundEditor();
    }
    
    /**
     *  Get path name for file path for load and save options.
     *  @param option LOAD or SAVE
     */
    protected String getPath(int option)
    {
        String pathName = properties.getPaths();
        String[] paths  = pathName.split(";");
        
        if (paths.length == 0) return "";
        if (option==LOAD) return paths[0];
        if (paths.length < 2) return "";
        return paths[1];        
    }   // End of getPath()
    
    /**
     *  Get path name for file path for load and save options.
     *  @param option LOAD or SAVE
     */
    protected void setPath(File file, int option)
    {
        int lastIndex = 0;
        String fullName, fileName;
        try
        {
           fullName = file.getCanonicalPath();
           fileName = file.getName();
           lastIndex = fullName.lastIndexOf(fileName);
           if (lastIndex<0) 
           {  label.setText(LanguageText.getMessage("soundEditor", 44));
              return;  }
        }
        catch (Exception e) {label.setText(e.toString()); return; }
        
        String path = fullName.substring(0,lastIndex-1);

        // Get original paths.
        String pathName = properties.getPaths();
        String[] paths = pathName.split(";");
        
        // Handle cases when there is no previous path.
        if (paths.length < 2)
        {
            String newPaths[] = new String[2];
            newPaths[0] = newPaths[1] = "";
            if (paths.length == 1){ newPaths[0] = paths[0]; }
            paths = newPaths;
        }
        
        // Update the load and save path names.
        if (option == LOAD) paths[0] = path;
        else                paths[1] = path;
        
        // Join the new path string.
        path = paths[0];
        for (int p=1; p<paths.length; p++)
        {   path += ";" + paths[p]; }
        properties.setPaths(path);
        
    }   // End of setPath()

    /* Create a panel with options for writing different audio types */
    private JPanel makeFileTypeOptionPanel(JFileChooser chooser)
    {
        // Supported sound types.
        String[] soundExtensions = SoundDefaults.getAudioWriterExtensions();
        String audioDefault = SoundDefaults.getAudioCompression();

        // Create group of buttons for the types of sound files.    
        JPanel soundButtons = new JPanel();
        BoxLayout box = new BoxLayout(soundButtons, BoxLayout.Y_AXIS);
        soundButtons.setLayout(box);
        soundButtons.setBorder(BorderFactory.createEtchedBorder());
        ButtonGroup soundGroup = new ButtonGroup();

        soundButtons.add
                (new JLabel(LanguageText.getMessage("soundEditor", 46)));
        JRadioButton button;
        soundSelect = audioDefault;
        for (int index=0; index<soundExtensions.length; index++)
        {
            button = new JRadioButton(soundExtensions[index]);
            
            button.setName(soundExtensions[index]);
            button.addActionListener(
                new ActionListener()
                {             
                   public void actionPerformed(ActionEvent event)
                   {   
                       JRadioButton button = (JRadioButton)event.getSource();
                       soundSelect = button.getName();
                       File file = chooser.getSelectedFile();
                       if (file!=null)
                       {
                           String fileName = file.getAbsolutePath();
        	        	   int i = fileName.lastIndexOf('.');
        	        	   String name = "";
        	        	   if (i>0)
        	        		   name = fileName.substring(0,i);
        	
        	        	    File newFile = new File(name + "." + soundSelect);
        	                chooser.setSelectedFile(newFile);
                       }
                	 }
                 });
            
            soundButtons.add(button);
            soundGroup.add(button);
         }  // end for.

         JPanel dialog = new JPanel();
         dialog.add(soundButtons);
         return dialog;
    }
  
    
    //  Write sound file from disk
     protected File writeFile(SoundData sound, int titleNo)
     {
         // Declare variables needed.
         Frame frame = (Frame)SwingUtilities.getAncestorOfClass(JFrame.class, panel);
         String title = LanguageText.getMessage("soundEditor", titleNo);
         String path  = getPath(SAVE);
         String dialogTitle = LanguageText.getMessage("soundEditor",  197);
         String[] extensions = SoundDefaults.getAudioWriterExtensions();
         AudioDialogFilter dialogFilter = new AudioDialogFilter(dialogTitle, extensions);
         String[] soundExtensions = SoundDefaults.getAudioWriterExtensions();
         int returnVal;    // Variable indicating dialog returns status.
         soundSelect = SoundDefaults.getAudioCompression();

         File file = null; 
         String fileName, fullName; 

         // Create filter for finding audio files that can be read.
         String osName = System.getProperty("os.name");
         if (osName.equals("Mac OS X")) 
         {
             FileDialog fd = new FileDialog(new Dialog(frame), title, FileDialog.SAVE);
             fd.setDirectory(path);
             if (this.chooseFileName != null)
            	 fd.setFile(chooseFileName);
             
             fd.setFilenameFilter(dialogFilter);

             fd.setVisible(true);
             fileName = fd.getFile();
             String directory = fd.getDirectory();
             
             if (fileName == null || fileName.length()==0)
             {
            	 file = null;
             }
             else
             {
	             if (SoundDefaults.isSandboxed() && !SoundDefaults.isValidForSandbox(directory))
	             {
	            	 String extension = "";
	            	 int lastIndex = fileName.lastIndexOf(".");
	            	 if (lastIndex>0)
	            		 extension = fileName.substring(fileName.lastIndexOf("."));
	            	 if (extension.length() <= 0)
					 {
	            		 label.setText(LanguageText.getMessage("commonHelpSets", 97));
	            		 return null;
					 }
	             }
	             file = new File(directory + fileName);
             }
         } 
         else 
         {
             JFileChooser fc = new JFileChooser(path);
             fc.setDialogTitle(title);
             fc.setFileFilter(  dialogFilter );

        	 JPanel dialog = makeFileTypeOptionPanel(fc);
        	 fc.setAccessory(dialog);
             fc.setVisible(true);
             if (this.chooseFileName != null)
             {
            	 fc.setSelectedFile(new File(chooseFileName));
             }
             
             returnVal = fc.showSaveDialog(frame);
             if (returnVal == JFileChooser.APPROVE_OPTION)
             {
                 if (returnVal == JFileChooser.APPROVE_OPTION)
                 { 
                     file = fc.getSelectedFile();
                 }
             }
         }
         
         if (file == null)
         {
        	 label.setText(LanguageText.getMessage("soundEditor",52));
             return null;
         }
 
         try 
         {  
             fullName = file.getCanonicalPath();
             fileName = file.getName();
             int lastIndex = fullName.lastIndexOf(fileName);
             if (lastIndex<0) 
             {  
            	 label.setText(LanguageText.getMessage("soundEditor", 44));
                 return null;  
             }

             if (sound.getFrames()==0)
             {  
            	 label.setText(LanguageText.getMessage("soundEditor", 45));
                 return null;
             }

             // Construct the name of the file to write
           	 for (int i=0; i<soundExtensions.length; i++)
           	 {
           	    if (fullName.endsWith("." + soundExtensions[i])) 
           	    {
           		    soundSelect = soundExtensions[i];
           		    break;
           	    }
           	 }

             if (!fullName.endsWith("." + soundSelect))
             {
            	 lastIndex = fullName.lastIndexOf(".");
            	 String extension = "";
            	 if (lastIndex>0)
            		 extension = fullName.substring(fullName.lastIndexOf("."));
            	 if (extension.length() > 0)
				 {
            		 label.setText(LanguageText.getMessage("soundEditor", 202));
            		 return null;
				 }

                 fullName = fullName + "." + soundSelect;
                 file = new File(fullName);
             }

             // Verify that it is ok to replace an existing file.
             if (file.exists()) 
             {  
           	     int answer = JOptionPane.showConfirmDialog(panel, 
                             fullName + " " + 
                             LanguageText.getMessage("soundEditor", 48),
                             LanguageText.getMessage("soundEditor", 49),
                             JOptionPane.OK_OPTION);

                 if (answer != JOptionPane.OK_OPTION) 
                 {   
            	    label.setText(LanguageText.getMessage("soundEditor",50));
                    return null;
                 }   
                
                 if (!file.delete()) 
                 {  
            	    label.setText(LanguageText.getMessage("soundEditor",51) 
                           + " "  + fullName);
                    return null; 
                 }
             }     // End if file.exists();
            
             // Set the path to the file and the file name.
             setPath(file, SAVE);

             if (!sound.writeFile(fullName))
             {
                label.setText(LanguageText.getMessage("soundEditor",53));
                return null;
             }
         }        // End if if APPROVE_OPTION.
 		 catch (FileNotFoundException nfe)
 		 {
 			label.setText(LanguageText.getMessage("commonHelpSets", 96));
 			return null;
 		 }
         catch (Exception iox) 
         {  
        	label.setText(LanguageText.getMessage("soundEditor",54) 
                    + " " + iox.toString());
            return null;
         }
         return file;
     }    // End of write file. 
     
     
      //  Read sound file from disk
     protected File chooseFile(SoundData sound, int titleNo)
     {
         // Declare variables needed.
         Frame frame = (Frame)SwingUtilities.getAncestorOfClass(JFrame.class, panel);
         String title = LanguageText.getMessage("soundEditor", titleNo);
         String path  = getPath(LOAD);
         String dialogTitle = LanguageText.getMessage("soundEditor",  197);
         String[] extensions = SoundDefaults.getAudioWriterExtensions();
         AudioDialogFilter dialogFilter = new AudioDialogFilter(dialogTitle, extensions);
         
         int returnVal;    // Variable indicating dialog returns status.
         File file;        // Declaration for file names.

         // Create filter for finding audio files that can be read.
         String osName = System.getProperty("os.name");
         if (osName.equals("Mac OS X")) 
         {
             FileDialog fd = new FileDialog(new Dialog(frame), title, FileDialog.LOAD);
             fd.setDirectory(path);
             fd.setFilenameFilter(dialogFilter);
             fd.setVisible(true);
             String fileName = fd.getFile();
             String directory = fd.getDirectory();

     		 if (fileName == null || fileName.length()==0)
     		 {
            	 file = null;
     		 }
     		 else
     		     file = new File(directory + fileName);
         } 
         else 
         {
             JFileChooser fc = new JFileChooser(path);
             fc.setDialogTitle(title);
             fc.setFileFilter(  dialogFilter );
             fc.setVisible(true);
             returnVal = fc.showOpenDialog(frame);
             if (returnVal == JFileChooser.APPROVE_OPTION)
             {
                 file = fc.getSelectedFile();
             }
             else file  = null;
         }
         
         if (file == null)
         {
        	 label.setText(LanguageText.getMessage("soundEditor",58));
             return null;
         }
 
         if (!file.exists()) 
         {   
        	 label.setText(file.getName() + " " +
                     LanguageText.getMessage("soundEditor",56));
             return null;  
         }

         try 
         {  
        	chooseFileName = file.getAbsolutePath();
        	setPath(file, LOAD);
            return file;
         }
         catch (Exception exception)
         {  
        	label.setText(LanguageText.getMessage("soundEditor",57));
            return null;  
         }
     }   // end choseFile()

     /** Set wave display panel so listeners can become active.
      */
     public void setSoundDisplayPanel(SoundDisplayPanel wavePanel)
     {  this.wavePanel = wavePanel;  

        Annotations annotations 
                = soundPanelProperties.getAnnotationData(panel);
        SoundData sound = annotations.getSound();
        
        undoRedo = soundPanelProperties.getUndoRedo();
        if (undoRedo==null) undoRedo = new UndoRedo();

        annotations.setSoundEditor(undoRedo);
        if (sound.getFrames()>0)
        {
           if (!wavePanel.drawSound( annotations, true))
           {    label.setText(LanguageText.getMessage("soundEditor",59)); }
        }
        editor = null;

      } // End of setSoundDisplayPanel
     
         
    /**  Listener to respond to sound commands in a separate thread
     *   @param actionEvent Event triggered by a sound command
     */
    public synchronized void processThread(ActionEvent actionEvent)
    {
        if (event!=null)
        {
            label.setText(LanguageText.getMessage("soundEditor",60));
            return;
        }
                 
        event = actionEvent;
        label.setText(LanguageText.getMessage("soundEditor",61));
    	try
    	{
            String result = processCommand(event);
            if (result!=null) 
               if (!result.equals("")) label.setText(result);
                else label.setText(LanguageText.getMessage("soundEditor",63));
            wavePanel.enableDisplay(true);
    	}
    	catch (Exception e)
    	{
    		try
    		{
        		StackTraceElement stack = e.getStackTrace()[0];
        		int lineNo = stack.getLineNumber();
        		String name = stack.getClassName();
        		String msg = e.toString();
        		JButton button = (JButton)event.getSource();
        		String command = button.getName();
        		String output = "Command: " + command + " Line: " + lineNo + " - " + "Object: " + name
        				+ " " + msg;
        		label.setText(output);
    		}
    		catch (Exception ex)
    		{
    			label.setText(ex.toString());
    		}
    	}
        event = null;
    }


        /**  Listener to respond to sound commands
         *   @param e triggered by a sound command
         *   @return string to set in label, "" if none, null if command unknown
         */
         public String processCommand(ActionEvent e)
         {
            Annotations annotations 
                    = soundPanelProperties.getAnnotationData(panel);
            
            SoundData sound = annotations.getSound();
            
            if (sound==null)  
            {
                sound = new AnnotationData(null, label);
                if (soundPanelProperties!=null)
                    soundPanelProperties.setAnnotationData
                                                    (annotations, panel);
            }
            else
            {  if (annotations.getSoundEditor()==null)
                   setSoundEditor(annotations);
            }
            
            int layer = annotations.getSoundLayer();
            if (layer != lastLayer && lastLayer != -1) 
            {
                wavePanel.resetSelection();
                lastLayer = layer;
            }

            sound.setLabel(label);
            JButton whichButton = (JButton)e.getSource();
            
            // Update the annotation Data
            String[] text = {"","","","",""};
            
            if (!nativeText.getName().equals("annotation"))
            {
                text[0] = glossText.getText();
                text[1] = nativeText.getText();
                text[2] = (String)combo.getSelectedItem();
            }
            sound.setSoundText(text);
        
            String buttonName   = whichButton.getName();
            char category = buttonName.charAt(1);
            char option   = buttonName.charAt(0);

             if (sound.isActive() || (newSound!=null && newSound.isActive()))
             {   if ("fesodzc".indexOf(category)>=0 
                         || category=='m' && "rpfl".indexOf(option)>=0)
                 {   return LanguageText.getMessage("soundEditor",64); }             }

             // Set text into the sound data text array.
             glossNative[0] = glossText.getText();
             if (!nativeText.getName().equals("annotations"))
                 glossNative[1] = nativeText.getText();
             if (combo==null) glossNative[2] = "English";
             else  glossNative[2] = (String)combo.getSelectedItem();

             switch(category)
             {
                 case 'm':              // Media commands.
                    switch (option)
                    {
                       case 'r':        // Record.
                         newSound = null;
                         System.gc();
                         if (sound.isRecorded() 
                                 && wavePanel.getSelectedFrames(annotations) != null)
                         {
                            newSound = new AnnotationData
                                    (sound.getSoundText(), label);
                            newSound.record(whichButton);
                         }
                         else
                         {  sound.record(whichButton);
                            annotations.delete(new Point(-1, -1), true);
                            undoRedo.resetRedoUndo();                     
                         }  
                         return LanguageText.getMessage("soundEditor",65);
                         
                       case 's':        // Stop.
                         if (newSound!=null)
                         {    if (!newSound.isActive()) 
                              {  return LanguageText.getMessage
                                                         ("soundEditor",66); }
                         }
                         else if (!sound.isActive())    
                              {  return LanguageText.getMessage
                                                         ("soundEditor",67);
                         }

                         if (newSound!=null)
                         {
                             newSound.stopSound();

                             // Change the record rate to be compatible 
                             //     with original sound.
                             newSound.setSoundEditor(undoRedo);
                             SoundEditor newEditor = newSound.getSoundEditor();
                             newSound = null;
                             System.gc();
                             if (newEditor.copy(-1,-1) == false)
                             {  return LanguageText.getMessage("soundEditor",68);
                             }

                             Point select 
                                     = wavePanel.getSelectedFrames(annotations);
                             if (select==null) 
                             {   editor = newEditor;
                                 sound  = (AnnotationData)editor.getSound();
                             }
                             else  
                             {  if (editor==null) setSoundEditor(annotations);
                                if (editor.paste(select.x, select.y) == false)
                                {  return LanguageText.getMessage
                                                       ("soundEditor",69);
                                }
                             }
                         }
                         else
                         {
                            sound.stopSound();
                            setSoundEditor(annotations);
                         }

                         if (!wavePanel.drawSound( annotations, true))
                              return LanguageText.getMessage("soundEditor",70);
                         else return LanguageText.getMessage("soundEditor",71);

                       case 'p':        // Play.
                         Point select = wavePanel.getSelectedFrames(annotations);
                         if (select==null || select.x==select.y) 
                             select = new Point(-1,-1);

                         if (!sound.playBack(wavePanel, select.x, select.y))
                               return LanguageText.getMessage("soundEditor",72);
                         return "";

                       case 'f':        // First.
                         if (!wavePanel.scrollToFront(sound))
                         {  return LanguageText.getMessage("soundEditor",73); }
                         break;

                       case 'l':        // Last.
                         if (!wavePanel.scrollToBack(sound))
                         {  return LanguageText.getMessage("soundEditor",74); }
                         break;                     
                       default: return LanguageText.getMessage("soundEditor", 62);
                 }
                 break;  // End of media commands.

                 case 'f':              // File commands.
                    switch (option)
                    {
                       case 'r':        // Reset.
                         nativeText.setText("");
                         sound.setDirty();
                         setSoundEditor();
                         chooseFileName = null;
                         if (editor==null)
                         {
                            sound.reset(null);
                            setSoundEditor(annotations);
                         }
                         else
                         {
                            undoRedo.pushUndo
                                  (new SoundUndoRedoData(annotations.clone()));
                         }
                         if (!editor.resetSound())
                         {  return LanguageText.getMessage("soundEditor",75); }

                         if (!wavePanel.drawSound( annotations, true))
                         {  return LanguageText.getMessage("soundEditor",76); }
                         wavePanel.drawSound( annotations, true);
                         break;

                       case 'l':        // Load.
                           File loadFile = chooseFile(sound, 55);
                           if (loadFile==null) return null;

                           try
                           {
                               // Disable repaint unti drawSound() call
                               wavePanel.enableDisplay(false);

                               System.gc();
                               int frames = sound.getFrames();
                               sound.readFile(loadFile);

                               setSoundEditor(annotations);
                               annotations.delete(new Point(-1,frames), true);
                               undoRedo.resetRedoUndo();
                               if (acornsProperties!=null)
                                      acornsProperties.setFileDirty();
                               sound.setDirty();

                               if (!wavePanel.drawSound( annotations, true))
                               {    return LanguageText.getMessage
                                                   ("soundEditor",77);
                               }
                           }   
                           catch (Exception ex) 
                           {   return LanguageText.getMessage
                                       ("soundEditor",78, loadFile.getName());
                           }
                           return LanguageText.getMessage
                                       ("soundEditor",79, loadFile.getName());

                       case 'i':        // Import.
                            File importName = chooseFile(sound, 198);
                            if (importName==null) return null;

                            Annotation importAnnotate = new Annotation();
                            try
                            {
                                AnnotationData annotate
                                      = importAnnotate.importXML(importName);
                                sound = annotate;
                                annotations = annotate;

                                glossNative = sound.getSoundText();
                                glossText.setText(glossNative[0]);
                                if (!nativeText.getName().equals("annotations"))
                                     nativeText.setText(glossNative[1]);
                                else nativeText.setText("");
                                if (combo!=null)
                                { 
                                    String language = annotate.getKeyboard();
                                    combo.setSelectedItem(language);
                                    KeyboardFonts.getLanguageFonts().setFont
                                                         (language, nativeText);                               
                                }

                                panel.getAnnotationSlider(1).setValue(1);

                                soundPanelProperties.setAnnotationData
                                                        (annotations, panel);
                                
                                setSoundEditor(annotations);
                                undoRedo.resetRedoUndo();
                                if (!wavePanel.drawSound( annotations, true))
                                {  label.setText
                                   (LanguageText.getMessage("soundEditor",80));
                                }
                                return LanguageText.getMessage
                                       ("soundEditor",79, importName.getName());
                            }
                            catch (FileNotFoundException fnf)
                            {
                            	return LanguageText.getMessage("soundEditor",204);
                            }
                            catch (Exception ex)
                            {  
                            	return LanguageText.getMessage("soundEditor",81);
                            }

                       case 'e':        // Export.
                            if (editor==null || sound==null || !sound.isRecorded())
                            { return LanguageText.getMessage("soundEditor",72);}

                            File exportName = writeFile(sound, 199);
                            if (exportName == null) return null;

                            try
                            {  Annotation exportAnnotate = new Annotation();
                               if (!exportAnnotate.makeXML
                                     (exportName, (AnnotationData)annotations))
                                                throw new Exception();
                               return LanguageText.getMessage
                                       ("soundEditor",201, exportName.getName());
                            }
                            catch (FileNotFoundException ex)
                            {
                            	return LanguageText.getMessage("soundEditor", 203);
                            }
                            catch (Exception ex)
                            {  
                            	return LanguageText.getMessage("soundEditor", 82);
                            }

                       default: return LanguageText.getMessage("soundEditor", 62);
                 }
                 break;   // End of File Commands.

                case 'e':              // Edit commands.
                    setSoundEditor();
                    if (editor==null || sound==null || !sound.isRecorded())
                    { return LanguageText.getMessage("soundEditor",72);  }

                    Point select = wavePanel.getSelectedFrames(annotations);
                    if ((option=='c') && (select==null || select.x==select.y))
                        select = new Point(-1,-1);
                    if (select==null)
                    { return LanguageText.getMessage("soundEditor",84);   }

                    switch (option)
                    {
                      case 'c':        // Copy.
                        wavePanel.enableDisplay(false);
                        if (editor.copy(select.x, select.y)==false)
                         {  return LanguageText.getMessage("soundEditor",85); }
                         if (!wavePanel.drawSound( annotations, false))
                         {  return LanguageText.getMessage("soundEditor",86); }
                         else 
                         {  return LanguageText.getMessage("soundEditor",87); }

                       case 'x':        // Cut.
                         wavePanel.enableDisplay(false);
                         if (editor.cut(select.x, select.y)==false)
                         {  return LanguageText.getMessage("soundEditor",88); }

                         if (!wavePanel.drawSound( annotations, true))
                         {  return LanguageText.getMessage("soundEditor",89); }
                         break;

                       case 'p':        // Paste.
                         wavePanel.enableDisplay(false);
                         if (editor.paste(select.x, select.y)==false)
                         {  return LanguageText.getMessage("soundEditor",90); }

                         if (!wavePanel.drawSound( annotations, true))
                         {  return LanguageText.getMessage("soundEditor",91); }
                         break;

                       case 'k':        // Delete (Kill).
                         wavePanel.enableDisplay(false);
                         if (editor.delete(select.x, select.y)==false)
                         {  return LanguageText.getMessage("soundEditor",92);   }

                         if (!wavePanel.drawSound( annotations, true))
                         {  return LanguageText.getMessage("soundEditor",93); }
                         break;

                       case 'd':        // Duplicate.
                         wavePanel.enableDisplay(false);
                         if (editor.duplicate(select.x, select.y)==false)
                         {  return LanguageText.getMessage("soundEditor",94);  }

                         if (!wavePanel.drawSound( annotations, true))
                         {  return LanguageText.getMessage("soundEditor",91); }
                         break;

                       case 't':        // Trim.
                         wavePanel.enableDisplay(false);
                         if (editor.trim(select.x, select.y)==false)
                         {  return LanguageText.getMessage("soundEditor",95);  }

                         if (!wavePanel.drawSound( annotations, false))
                         {  return LanguageText.getMessage("soundEditor",91); }
                         break;

                       case 's':        // Silence. 
                         wavePanel.enableDisplay(false);
                         if (editor.silence(select.x, select.y)==false)
                         {  return LanguageText.getMessage("soundEditor",96);   }

                         if (!wavePanel.drawSound( annotations, false))
                         {  return LanguageText.getMessage("soundEditor",91); }
                         break;  
                           
                       default: return LanguageText.getMessage("soundEditor", 62);
                 }
                 break;  // End of edit commands

                 case 's':              // Sound commands.
                    setSoundEditor();
                    if (editor==null || sound==null || !sound.isRecorded())
                    { return LanguageText.getMessage("soundEditor",72);  }

                    select = wavePanel.getSelectedFrames(annotations);
                    // Assume all if nothing selected.
                    if (select==null || select.x==select.y) 
                        select=new Point(-1,-1);

                    switch (option)
                    {
                       case 'u':        // Amplify volume.
                         wavePanel.enableDisplay(false);
                        if (editor.volume(select.x, select.y, 2.0f)==false)
                         {  return LanguageText.getMessage("soundEditor",97);  }

                         if (!wavePanel.drawSound( annotations, false))
                         {  return LanguageText.getMessage("soundEditor",91); }
                         else return LanguageText.getMessage("soundEditor",98);

                       case 'd':      // Reduce volume.
                         wavePanel.enableDisplay(false);
                         if (editor.volume(select.x, select.y, 0.5f)==false)
                         {  return LanguageText.getMessage("soundEditor",99);  }

                         if (!wavePanel.drawSound( annotations, false))
                         {   return LanguageText.getMessage("soundEditor",91); }
                         else return LanguageText.getMessage("soundEditor",100);

                       case 'n':        // Normalize.
                         wavePanel.enableDisplay(false);
                         if (editor.normalize(select.x, select.y)==false)
                         {  return LanguageText.getMessage("soundEditor",101); }

                         if (!wavePanel.drawSound( annotations, false))
                         {  return LanguageText.getMessage("soundEditor",102); }
                         else return LanguageText.getMessage("soundEditor",103);

                       default: return LanguageText.getMessage("soundEditor", 62);
                }

                case 'p':  // Commands to pick selected areas.
                    switch (option)
                    {
                       case 'f':        
                         if (!wavePanel.selectWave( annotations, true, false))
                         {  return LanguageText.getMessage("soundEditor",104);
                         }
                         break;

                       case 'a':
                         if (!wavePanel.selectWave( annotations, true, true))
                         {  return LanguageText.getMessage("soundEditor",104);
                         }
                         break;

                        case 'b':
                         if (!wavePanel.selectWave( annotations, false, true))
                         {  return LanguageText.getMessage("soundEditor",104);
                         }
                         break; 

                        default: return LanguageText.getMessage("soundEditor", 62);

                    }
                    return "";

                case 'o':              // Redo and undo previous operations.
                    switch (option)
                    {  
                       case 'r':        // Redo.
                         if (undoRedo.isRedoEmpty()) 
                             return LanguageText.getMessage("soundEditor",105);
                         undoRedoData = (SoundUndoRedoData)undoRedo.redo
                                           (new SoundUndoRedoData(annotations));
                         break;

                       case 'u':        // Undo.
                         if (undoRedo.isUndoEmpty()) 
                             return LanguageText.getMessage("soundEditor",106);

                         undoRedoData = (SoundUndoRedoData)undoRedo.undo
                                           (new SoundUndoRedoData(annotations));
                         break;

                       default: return LanguageText.getMessage("soundEditor", 62);
                    }

                    // Finish up the command.
                    if (undoRedoData==null) 
                    {   if (option=='r') 
                             LanguageText.getMessage("soundEditor",105);
                        else LanguageText.getMessage("soundEditor",106);
                        break;
                    }

                    annotations = undoRedoData.getData();
                    soundPanelProperties.setAnnotationData(annotations, panel);

                    // Free up some memory.
                    sound = null;
                    System.gc();
                    setSoundEditor(annotations);
                    sound = annotations.getSound();

                    glossNative = sound.getSoundText();
                    glossText.setText(glossNative[0]);
                    if (!nativeText.getName().equals("annotations"))
                        nativeText.setText(glossNative[1]);
                    if (combo!=null 
                            && !combo.getSelectedItem().equals(glossNative[2]))
                       combo.setSelectedItem(glossNative[2]);

                    // Display wave after the operation was redone or undone.
                    if (!wavePanel.drawSound( annotations, true))
                    {  return LanguageText.getMessage("soundEditor",91); }

                break;  // End of operation commands.

                case 'z':              // Zoom options.
                    if (!sound.isRecorded())
                    { return LanguageText.getMessage("soundEditor",72); }

                    switch (option)
                    {
                       case 'i':        // In.
                         if (!wavePanel.drawSound( annotations, 2.0))
                         {  return LanguageText.getMessage("soundEditor",107); }
                         break;

                       case 'o':        // Out.
                         if (!wavePanel.drawSound( annotations, 0.5))
                         {  return LanguageText.getMessage("soundEditor",108); }
                         break;

                       case 's':        // Selection.
                         if (!wavePanel.drawSelect( annotations ))
                         {  return LanguageText.getMessage("soundEditor",109); }
                         break;

                       case 'a':        // All.
                         if (!wavePanel.drawSound( annotations, .0001))
                         {  return LanguageText.getMessage("soundEditor",110); }
                         break;

                       default: return LanguageText.getMessage("soundEditor", 62);
                }
                 break;  // End of zoom commands.

                default: return LanguageText.getMessage("soundEditor", 62);
             }   // End of command categories.

             return "";

         }   // End actionPerformed()

//    }     // End of ProcessThread class

    /** Method to handle an audio file being dropped into the panel
     *
     * @param audio The audio file object
     */
    public void audioDropped(File audio)
    {
       // Get the audioFormat type corresponding to this extension.
       AudioFileFormat.Type[] typesSupported = AudioSystem.getAudioFileTypes();
       AudioFileFormat.Type   audioType = null;

       String name = audio.getName();
       String extension = name.substring(name.lastIndexOf(".") + 1);
       String typeExtension = extension.toLowerCase();
       if (extension.equalsIgnoreCase("ogg")) typeExtension = "wav";
       for (int i=0; i<typesSupported.length; i++)
       {  
    	  if (typesSupported[i].getExtension().equals(typeExtension))
 	      {   
        	  audioType = typesSupported[i];
        	  break;
 	      }
       }
        
       if (audioType == null) 
       {
        	label.setText(LanguageText.getMessage
                    ("soundEditor",77, audio.getName()));
        	return; // Cannot open with this file type
       }
	
       wavePanel.enableDisplay(false);

       Annotations annotations
                    = soundPanelProperties.getAnnotationData(panel);

       SoundData sound = annotations.getSound();
       System.gc();
       try
       {
          int frames = sound.getFrames();
          sound.readFile(audio);

          setSoundEditor(annotations);
          annotations.delete(new Point(-1,frames), true);
          undoRedo.resetRedoUndo();
          if (acornsProperties!=null) acornsProperties.setFileDirty();
          sound.setDirty();

          if (!wavePanel.drawSound( annotations, true))
          {    label.setText(LanguageText.getMessage("soundEditor",77));
               wavePanel.enableDisplay(true);
               return;
          }
      }
      catch (Exception ex)
      {   label.setText(LanguageText.getMessage
                                    ("soundEditor",78, audio.getName()));
          wavePanel.enableDisplay(true);
          return;
      }
      label.setText(LanguageText.getMessage("soundEditor",79, audio.getName()));
      wavePanel.enableDisplay(true);
    }

}         // End ButtonListener.
