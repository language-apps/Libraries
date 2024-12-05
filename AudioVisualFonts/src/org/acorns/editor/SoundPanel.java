/*
 * SoundPanel.java
 *
 *   @author  HarveyD
 *   @version 3.00 Beta
 *
 *   Copyright 2007-2015, all rights reserved
 */
package org.acorns.editor;

import javax.swing.*;
import javax.swing.event.*;

import java.awt.event.*;
import java.awt.*;
import java.beans.*;
import java.net.*;
import java.lang.reflect.*;

import org.acorns.visual.*;
import org.acorns.audio.*;
import org.acorns.language.*;
import org.acorns.data.*;
import org.acorns.properties.*;

/** A panel to display a sound wave in a panel and enable SoundEditing
 * features. It is instantiated in any lesson that displays such a panel.
 */
public class SoundPanel extends JPanel implements ActionListener
{
    private final static int ICONSIZE  = 30;
    private final static int STRUTSIZE = 30;  // South panel size.
    
    private final static Dimension LABELSIZE     = new Dimension(500,20);
    private final static Dimension COMBOSIZE     = new Dimension(150,20);
    private final static Dimension TEXTFIELDSIZE = new Dimension(300,20);
        
    private SoundListener     soundListener;
    private JLabel            message;
    private JTextField        glossText, nativeText;
    private JSlider           annotationSlider;
    private JComboBox<String> languageCombo;
    private RootSoundPanel    rootPanel;
    private SoundPanel        soundPanel;
    private SoundDisplayPanel wavePanel;
    private boolean           removeNotifyStatus;
    
    
    private static final long serialVersionUID=1L;
    
    /** Creates a new instance of SoundPanel
     *
     * @param annotate The type of panel (panel looks different for each)
     * <pre>
     * 's' sound panel
     * 'a' text annotations
     * 'i' picture annotatoins
     * 'b' story book annotaions
     * </pre>
     * @param colors foreground and background colors, font size
     * @param panelSize The size of the panel
     */
    public SoundPanel(char annotate, ColorScheme colors, Dimension panelSize) 
    {
        this.removeNotifyStatus = true;
        soundPanel = this;
        
        setLayout(new BorderLayout());
        setBackground(SoundDefaults.DARKBACKGROUND);
        if (panelSize!=null) 
        {
            setPreferredSize(panelSize);
            setSize(panelSize);
        }
        
        // Create the label for error messages.
        message = new JLabel("");
        message.setForeground(SoundDefaults.ERROR);
        message.setToolTipText(LanguageText.getMessage("soundEditor", 128));
        message.setName("" + annotate);

        // Create text fields to add annotation text.
        glossText  = new JTextField("");
        glossText.setPreferredSize(new Dimension(TEXTFIELDSIZE));
        glossText.setToolTipText(LanguageText.getMessage("soundEditor", 129));
           
        nativeText = new JTextField("");
        if (annotate=='a') nativeText.setName("annotations");
        if (annotate=='s') nativeText.setName("soundeditor");
        if (annotate=='i') nativeText.setName("imageannotations");
        if (annotate=='b')  nativeText.setName("soundeditor");
        nativeText.setPreferredSize(new Dimension(TEXTFIELDSIZE));
        nativeText.setToolTipText(LanguageText.getMessage("soundEditor", 130));
        setLanguageFont(nativeText);
        
        // Create the listener to respond to button pushes.
        if (annotate=='a')
        {  soundListener = new AnnotationListener(this, message, glossText, nativeText
                                 , getLanguageCombo());
        }
        if (annotate=='s' || annotate=='b')
        { soundListener = new WaveListener(this, message, glossText, nativeText
                                 , getLanguageCombo());
        }
        if (annotate=='i')
        {  
        	try
        	{
        		Class<?> className = Class.forName("org.acorns.editor.ImageListener");
        		Class<?>[] params = { SoundPanel.class, JLabel.class
        							, JTextField.class, JTextField.class, JComboBox.class };
        		
        		Constructor<?> constr = className.getConstructor( params );
        		
        		Object[] args = {this, message, glossText, nativeText, getLanguageCombo()};
        		soundListener = (SoundListener)constr.newInstance(args);
        	}
        	catch (Throwable t)
        	{
           	    Frame frame = JOptionPane.getRootFrame();
        		JOptionPane.showMessageDialog(frame, t.toString());
        	}
        }
        
        // Create buttons to go into the north section of the panel.
        // First string is the text to show in the button.
        // Second string is the tooltip text.
        // Third string indicates the button group option and the button group.
        //    For example: "rf" indicates the reset command in the file group.
        // Fourth string indicates whether to create the button.
        //    "a" for annotation mode, "s" for sound editing mode
        String[] north =
        {  "","","", "asi",
           "reset", LanguageText.getMessage("soundEditor", 131),  "rf", "asib",
           "browse", LanguageText.getMessage("soundEditor", 132), "lf", "asib",
           "export", LanguageText.getMessage("soundEditor", 133), "ef", "as",
           "import", LanguageText.getMessage("soundEditor", 134), "if", "as",
           "save", LanguageText.getMessage("soundEditor", 135),   "sf", "s",
           "", "", "","asib",
           "copy", LanguageText.getMessage("soundEditor", 136),   "ce","sib",
           "cut", LanguageText.getMessage("soundEditor", 137),    "xe","sib",
           "paste", LanguageText.getMessage("soundEditor", 138),  "pe","sib",
           "", "", "","sib",
           "delete", LanguageText.getMessage("soundEditor", 139),   "ke","asib",
           "duplicate", LanguageText.getMessage("soundEditor", 140),"de","asib",
           "", "", "","sib",
           "trim", LanguageText.getMessage("soundEditor", 141),    "te","asib",
           "silence", LanguageText.getMessage("soundEditor", 142), "se","asib",
           "filter", LanguageText.getMessage("soundEditor", 190), "fs","s",
           "", "", "","asib",
           "speedup", LanguageText.getMessage("soundEditor", 143),  "ss","s",
           "slowdown", LanguageText.getMessage("soundEditor", 144), "ws","s",
           "", "", "","s",
           "volumeup", LanguageText.getMessage("soundEditor", 145),  "us","asib",
           "volumedown", LanguageText.getMessage("soundEditor", 146),"ds","asib",
           "normalize", LanguageText.getMessage("soundEditor", 147), "ns","asib",
           "", "", "","asib",
           "time", LanguageText.getMessage("soundEditor", 148),   "td","s",
           "spectrograph", LanguageText.getMessage("soundEditor", 149),"sd","s",
           "fft", LanguageText.getMessage("soundEditor", 150),    "fd","s",
           "speech", LanguageText.getMessage("soundEditor", 151), "vd", "s",
           "ruler", LanguageText.getMessage("soundEditor", 195), "dd", "s", 
           "", "", "","as",
           "selectfront", LanguageText.getMessage("soundEditor", 152),"fp","asib",
           "selectall", LanguageText.getMessage("soundEditor", 153), "ap","asib",
           "selectback", LanguageText.getMessage("soundEditor", 154),"bp","asib",
           "", "", "","sib"};
        buttonPanel(north,BorderLayout.NORTH,BoxLayout.X_AXIS, annotate, null);
         
        // Create buttons to go into the west section of the panel.
        String[] west = 
        {  "first", LanguageText.getMessage("soundEditor", 155),  "fm","asib",
           "record", LanguageText.getMessage("soundEditor", 156), "rm","asib",
           "play", LanguageText.getMessage("soundEditor", 157),   "pm","asib",
           "stop", LanguageText.getMessage("soundEditor", 158),   "sm","asib",
           "last", LanguageText.getMessage("soundEditor", 159),   "lm","asib",
           "", "", "","asib" };
        
        buttonPanel(west,BorderLayout.WEST,BoxLayout.Y_AXIS, annotate, null);
		      
        // Create buttons to go into the east section of the panel.
        int[] eastHots =
             { -1, -1, -1, -1, -1, KeyEvent.VK_INSERT, -1, KeyEvent.VK_DELETE};
        
        String[] east = 
        {  "zoomin", LanguageText.getMessage("soundEditor", 160), "iz","asib",
           "zoomout", LanguageText.getMessage("soundEditor", 161), "oz","asib",
           "zoomselect", LanguageText.getMessage("soundEditor",162),"sz","asib",
           "zoomall",LanguageText.getMessage("soundEditor", 163), "az","asib",
           "", "", "","asib",
           "add", LanguageText.getMessage("soundEditor", 164),"aa", "a",
           "edit", LanguageText.getMessage("soundEditor", 165), "ea", "a",
           "garbage", LanguageText.getMessage("soundEditor", 166), "ra", "ai"};
	       buttonPanel(east,BorderLayout.EAST,BoxLayout.Y_AXIS, annotate, eastHots);
        
        // Create dialog panel for the south section.
        JPanel sDialogPanel = new JPanel();
        sDialogPanel.setLayout(new BoxLayout(sDialogPanel, BoxLayout.X_AXIS));
        sDialogPanel.setBackground(SoundDefaults.BACKGROUND);

        String[] msgs = LanguageText.getMessageList("soundEditor", 167);
        if (annotate=='s')
        {
          // Add label and text field for Gloss text.
          JLabel glossLabel = new JLabel(" " + msgs[0] + " ");
          glossLabel.setToolTipText(msgs[1]);
          sDialogPanel.add(glossLabel);
          sDialogPanel.add(glossText);

          // Add some space.
          sDialogPanel.add(Box.createHorizontalStrut(STRUTSIZE));
       }
        
        if (annotate=='a' || annotate=='b')
        {  msgs = LanguageText.getMessageList("soundEditor", 168);
           sDialogPanel.add(new JLabel(" " + msgs[0] + " "));
           getAnnotationSlider(1);
           sDialogPanel.add(annotationSlider);
        }

        // Create panel for the south section.
        JPanel sPanel = new JPanel();
        sPanel.setLayout(new BoxLayout(sPanel, BoxLayout.Y_AXIS));
        sPanel.setBackground(SoundDefaults.BACKGROUND);
        if (annotate!='i')
        {
            // Add the combo box.
            if (annotate=='b')
            {
              sDialogPanel.add(Box.createHorizontalGlue());
              sDialogPanel.add(new JLabel(msgs[1] + " "));
            }
            if (getLanguageCombo()!=null) sDialogPanel.add(getLanguageCombo());

            msgs = LanguageText.getMessageList("soundEditor", 168);
            if (annotate!='b')
            {
                // Add native label and native text components to the south panel.
                String nativeLabelDescription = " " + msgs[2] + " ";
                if (annotate=='a')  
                {  nativeLabelDescription = " " + msgs[3] + " ";  }
                JLabel nativeLabel = new JLabel(nativeLabelDescription);
                nativeLabel.setToolTipText(msgs[4]);
                sDialogPanel.add(nativeLabel);
                sDialogPanel.add(nativeText);
            }
            sDialogPanel.add(Box.createHorizontalStrut(STRUTSIZE));
            sDialogPanel.add(Box.createHorizontalGlue());

            msgs = LanguageText.getMessageList("soundEditor", 169);
            makeButton(sDialogPanel, "undo", msgs[0], "uo", -1);
            makeButton(sDialogPanel, "redo", msgs[1], "ro", -1);

            sPanel.add(sDialogPanel);
        }
        
        // Create message panel and add it.
        JPanel mPanel = new JPanel();
        mPanel.setPreferredSize(LABELSIZE);
        mPanel.setMinimumSize(LABELSIZE);
        mPanel.setBackground(SoundDefaults.BACKGROUND);
        mPanel.add(message);
        sPanel.add(mPanel);
        
        // Add the south panel to the frame.
        add(sPanel, BorderLayout.SOUTH);
        
        // Now create the scrollPane and add to the center.
        JScrollPane scrollPane = new JScrollPane();
        scrollPane.getHorizontalScrollBar().setUnitIncrement(30);

        scrollPane.getViewport().setBackground(SoundDefaults.DARKBACKGROUND);
        scrollPane.setVerticalScrollBarPolicy
                (ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);
        add(scrollPane, BorderLayout.CENTER);
        wavePanel = new SoundDisplayPanel(this, message, colors);
        scrollPane.setViewportView(wavePanel);
        scrollPane.addComponentListener(wavePanel);
         
         if (annotate=='s' || annotate=='b')
              wavePanel.setDisplayType(SoundDisplayPanel.TIME);              
         if (annotate=='a')
              wavePanel.setDisplayType(SoundDisplayPanel.ANNOTATE);
         if (annotate=='i')
              wavePanel.setDisplayType(SoundDisplayPanel.IMAGE);              
 
        soundListener.setSoundDisplayPanel(wavePanel);       
    }   // End of constructor.

    /** Method to return the SoundDisplayPanel object
     * 
     * @return SoundDisplayPanel object
     */
    public SoundDisplayPanel getDisplayPanel()  { return wavePanel; }
    
    // Method to create a panel of buttons.
    private JPanel buttonPanel(String[] gifs
            , String position, int layout, char option, int[] hots)
    {
        int hotKey;
        
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, layout));
        panel.setBackground(SoundDefaults.BACKGROUND);
        
        if (layout==BoxLayout.X_AXIS)  panel.add(Box.createHorizontalGlue());
        else                           panel.add(Box.createVerticalGlue());
        
        int spacer;
        for (int i=0; i<gifs.length; i+=4)
        { 
            if (gifs[i+3].indexOf(option)>=0)
            {  if (gifs[i].equals(""))  
               {   if (layout==BoxLayout.X_AXIS)
                         panel.add(Box.createHorizontalGlue());
                   else  panel.add(Box.createVerticalGlue());
               }
               else if (gifs[i].equals("s"))
               {
                  try
                  {  spacer = Integer.parseInt(gifs[i+1]);
                     if (layout==BoxLayout.X_AXIS)
                           panel.add(Box.createHorizontalStrut(spacer));
                     else  panel.add(Box.createVerticalStrut(spacer));
                  }
                  catch (Exception e) {}
               }
               else   
               { 
                   if (hots==null) hotKey = -1;
                   else            hotKey = hots[i/4];
                   makeButton(panel, gifs[i], gifs[i+1], gifs[i+2], hotKey);  
               }
            }
        }    
        panel.setLayout(new BoxLayout(panel, layout));
        if (layout==BoxLayout.X_AXIS)  panel.add(Box.createHorizontalGlue());
        else                           panel.add(Box.createVerticalGlue());

        add(panel, position);
        return panel;
    }
    // Method to create a button with an Icon image and add it to a JPanel.
    private boolean makeButton(JPanel panel, String icon, 
                                    String toolTip, String buttonName, int hotKey)    
    {
        // Create the Help Button.
         URL url = getClass().getResource("/data/" + icon + ".png");
         if (url==null) return false;
         
         Image newImage  = Toolkit.getDefaultToolkit().getImage(url);
			      newImage = newImage.getScaledInstance
                 (ICONSIZE, ICONSIZE, Image.SCALE_REPLICATE);
         
         JButton button = new JButton(new ImageIcon(newImage));
         int adjustHeight = 1;
         if (icon.equals("record"))
         {
        	 adjustHeight = 10;
         }
         
         button.setName(buttonName);
         button.setPreferredSize(new Dimension(ICONSIZE+1, ICONSIZE+adjustHeight));
         button.setMaximumSize(new Dimension(ICONSIZE+1, ICONSIZE+adjustHeight));
         button.setMinimumSize(new Dimension(ICONSIZE+1, ICONSIZE+adjustHeight));
         button.setToolTipText(toolTip);
         if (hotKey!=-1) button.setMnemonic(hotKey);
         button.setBackground(SoundDefaults.BACKGROUND);
         button.addActionListener((ActionListener)soundListener);
         panel.add(button);
         return true;
		  }   
    
    /** Respond to combo box selections
     *  @param ae triggering the event
     */
    public void actionPerformed(ActionEvent ae)
    {
        @SuppressWarnings("rawtypes")
		JComboBox combo    = (JComboBox)ae.getSource();
        String    language = (String)combo.getSelectedItem();
        changeLanguage(language);
        message.setText
                (LanguageText.getMessage("soundEditor", 170) + " " + language);
        repaint();
    }
    
    /** Overridden method of JComponent so can kill any active recordings */
   public @Override void removeNotify()
   {
       super.removeNotify();
       if (!removeNotifyStatus) return;

       RootSoundPanel soundPanelProperties = null;

       PropertyChangeListener[] pcl 
              = Toolkit.getDefaultToolkit().getPropertyChangeListeners
                                               ("SoundListeners");
        if (pcl.length>0) soundPanelProperties = (RootSoundPanel)pcl[0];
        else return;
        
        Annotations annotations 
                    = soundPanelProperties.getAnnotationData(this);
            
        SoundData sound = annotations.getSound();
        sound.stopSound();
        message.setText("");         
   }

   /** Method to enable or disable whether SoundPanel should listen for
    *  panel SoundData events
    *
    * @param status true by stand-alone sound-editor, false in lessons
    */    
   public void removeListenerStatus(boolean status)
   {
       removeNotifyStatus = status;
   }
        
    /** Method to get the annotation slider and set its initial value
     *
     * @param startLayer initial layer value
     * @return JSlider object
     */
    public final JSlider getAnnotationSlider(int startLayer)
    {  
      if (annotationSlider == null)
      {
           // Create GUI component for determining layer.  
          try
          {
          annotationSlider 
               = new JSlider(JSlider.HORIZONTAL, AcornsProperties.MIN_LAYERS, 
                             AcornsProperties.MAX_LAYERS,startLayer);
          } catch (Exception e) 
            {  
        	  	Frame frame = JOptionPane.getRootFrame();
        	  	JOptionPane.showMessageDialog(frame, e.toString());  
        	}
          
          annotationSlider.setMajorTickSpacing(3);
          annotationSlider.setMinorTickSpacing(1);
          annotationSlider.setMaximumSize(new Dimension(200,50));
          annotationSlider.setPaintTicks(true);
          annotationSlider.setToolTipText
                  (LanguageText.getMessage("soundEditor", 171));
          //annotationSlider.addChangeListener(this);
          annotationSlider.setBackground(new Color(200,200,200));
          annotationSlider.setSize(new Dimension(100, ICONSIZE+5));
          annotationSlider.setPreferredSize(annotationSlider.getSize());
          annotationSlider.setMinimumSize(annotationSlider.getSize());
          
          // Set the slider language.
          int    level = 1;
          String language = KeyboardFonts.getLanguageFonts().getLanguage();
          RootSoundPanel properties = getProperties();
          if (properties!=null)
          {
              Annotations annotations = properties.getAnnotationData(this);
              SoundData data = (SoundData)annotations.getSound();
              if (data!=null)
              {
                  level = annotations.getAnnotationLevel() + 1;
                  if (annotations instanceof AnnotationData)
                  {  AnnotationData annotationData = (AnnotationData)annotations;
                     language = annotationData.getKeyboard();
                  }
              }
          }
          annotationSlider.setValue(level);
          changeLanguage(language);                          
          
          // Attach the listener.
          annotationSlider.addChangeListener(             
            new ChangeListener()
            {  
                public void stateChanged(ChangeEvent event)
                { 
                   RootSoundPanel properties = getProperties();
                   if (properties!=null)
                   {
                      Annotations annotations 
                              = properties.getAnnotationData(soundPanel);
                      SoundData data = (SoundData)annotations.getSound();
                      if (data!=null)
                      {
                          if (annotations.getAnnotationLevel()
                                  ==annotationSlider.getValue()-1)   return;

                          annotations.setAnnotationLevel(annotationSlider.getValue()-1);
                          properties.setAnnotationData(annotations, soundPanel);
                          if (annotations instanceof AnnotationData)
                          {
                             AnnotationData annotationData = (AnnotationData)annotations;
                             String language = annotationData.getKeyboard();
                             changeLanguage(language);
                          }
                          soundListener.setSoundDisplayPanel(wavePanel);
                      }
                      repaint();
                   }
                }  // End of state changed.
             }    // End of ChangeListener
          );
      }           // End of if slider == null
      return annotationSlider;
      
    } // End of getAnnotationSlider()
    
    //------------------------------------------------------------
    //  Method to get the language combo box
    //------------------------------------------------------------
    private JComboBox<String> getLanguageCombo()
    {
        
        if (languageCombo!= null) return languageCombo;
        
        // Create the Key Map
        try
        {
           languageCombo = KeyboardFonts.getLanguageFonts().createLanguageComboBox(null, false);
           languageCombo.setToolTipText
                   (LanguageText.getMessage("soundEditor", 172));
           languageCombo.setPreferredSize(COMBOSIZE);
           languageCombo.setMaximumSize(COMBOSIZE);
           languageCombo.setMinimumSize(COMBOSIZE);
           
           languageCombo.setBackground(SoundDefaults.COMBOCOLOR);
           languageCombo.addActionListener(this);
        }
        catch (Exception e)  {languageCombo = null;}

        return languageCombo;
        
    }   // End of getLanguageCombo().
    
    /** Method to get the root sound panel property object
     *
     */
    public RootSoundPanel getProperties()
    {
        if (rootPanel==null)
        {
            // Get the ACORNS property change listener.
            PropertyChangeListener[] pcl 
              = Toolkit.getDefaultToolkit().getPropertyChangeListeners
                                               ("SoundListeners");
            if (pcl.length>0) rootPanel = (RootSoundPanel)pcl[0];
        }
        return rootPanel;
    }   // End of getProperties()

    /** Method to make sure the Sound Editor slider value is correct*/
    public void setAnnotationSlider()
   {
      RootSoundPanel properties = getProperties();
      
      Annotations annotations = properties.getAnnotationData(this);
      int layer = annotations.getAnnotationLevel();
      JSlider slider = getAnnotationSlider(1);
      slider.setValueIsAdjusting(true);
      slider.setValue(layer+1);

      if (annotations instanceof AnnotationData)
      {
         AnnotationData data = (AnnotationData)annotations;
         String language = data.getKeyboard();
         changeLanguage(language);
      }
       
      slider.setValueIsAdjusting(false);
      slider.repaint(); 
      properties.setAnnotationData(annotations, this);
      repaint();
   }
    
    /** Set the embedded font and key layout for the native text field */
    private void setLanguageFont(JComponent component)
    {   RootSoundPanel properties = getProperties();
        if (properties!=null)
        {   Annotations annotations = properties.getAnnotationData(this);
            if (annotations instanceof AnnotationData)
            {
               AnnotationData data = (AnnotationData)annotations.getSound();
               if (data!=null)
               {  String language = data.getKeyboard();
                  KeyboardFonts.getLanguageFonts().setFont(language,component);
               }
            }
        }
    }

    /** Method to change selected language without changing the default.
     *  @param language desired language to change to.
     */
    public void changeLanguage(String language)
    {
        RootSoundPanel properties = getProperties();
        if (properties!=null)
        {
            KeyboardFonts.getLanguageFonts().setFont(language, nativeText);
            
            Annotations annotations = properties.getAnnotationData(this);
            if (annotations instanceof AnnotationData)
            {
               AnnotationData data = (AnnotationData)annotations.getSound();
               if (data!=null)  data.setKeyboard(language);
               repaint();
               properties.setAnnotationData(annotations, this);
            }
        }
        
        JComboBox<String> combo = getLanguageCombo();  // Change panel combo.
        combo.setSelectedItem(language);
    }

    /** Get the text entered into the text field */
    public JTextField getNativeText() { return nativeText; }

    /** Get the listener object attached to this panel */
    public SoundListener getListener() { return soundListener; }

         
}   // End of SoundPanel.
