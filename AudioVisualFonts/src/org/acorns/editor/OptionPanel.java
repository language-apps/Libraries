/*
 * OptionPanel.java
 *
 *   @author  HarveyD
 *   @version 3.00 Beta
 *
 *   Copyright 2007-2015, all rights reserved
 */

/*
 * 1/12/20 : new Long, Integer, Double to use parse methods - they are deprecated
 */
package org.acorns.editor;
        
import javax.swing.*;

import java.awt.*;
import java.awt.event.*;
import java.util.*;

import javax.sound.sampled.*;

import org.acorns.audio.*;
import org.acorns.language.*;

public class OptionPanel extends JPanel implements ActionListener
{
	 private static final long serialVersionUID = 1;
	 
     // Indices to array of combo components
     private final int MIXERS=0, RECORDRATE=1, EXTS=2, CHANNELS=3, ENDIAN=4;
     private final int SIGNED=5, AVG=6, WINDOW=7, ROUND=8, FRAMERATE=9;
     private final int BITS=10, LPCTYPE=11, SPECT=12, PITCH=13, RATEALG=14;
     private final int RATE=15, DTWDIST=16, COLOR=17, MELTYPE=18;
     private final int FILTER=19, COMBOS=20;

     // Indices to array of text components
     private final int ACTWINDOW=0, ACTTHRESH=1;
     private final int FILTERSIZE=2, TRIMSIZE=3, MAXRECORD=4;
     private final int PREEMPHASIS=5, WINSIZE=6, WINSHIFT=7, MELFILTERS=8;
     private final int MINFREQ=9, MAXFREQ=10, CEPSLEN=11, FEATURE=12;
     private final int LPCCOEFS=13, DTWCLOSE=14, DTWCORRECT=15;
     private final int MELVARIANCE=16, TEXTS=17;

     // GUI components
     private JTextField[] fields;  // Array to hold all of the text fields.
     private JComboBox<String>[]  combos;  // Array to hold all the combobox fields.
     private ArrayList<JTextField> weights; // List of DTW weight components
     private JLabel errorLabel;    // Label to display errors.
     private JPanel weightPanel;   // Panel to hold the DTW weights

    /** Creates a new instance of OptionPanel */
    @SuppressWarnings("unchecked")
	public OptionPanel() 
    {
        fields = new JTextField[TEXTS]; // Create an array to hold the text fields
        combos = new JComboBox[COMBOS];   // Create an array to hold the combo boxes

        weights = new ArrayList<JTextField>();  // Create a list to hold DTW weights
        weightPanel = new JPanel();     // Panel to hold DTW weights
        weightPanel.setBackground(SoundDefaults.BACKGROUND);

        setLayout(new BorderLayout());
        setBackground(SoundDefaults.BACKGROUND);
        String[] user = null;

        // Create Left Side.
        JPanel left = makeColumn(1);

        user = SoundDefaults.getSoundsSupported();
        makeDropPanel(left, 2, EXTS, user, SoundDefaults.getAudioCompression());
        makeDropPanel
             (left, 3, RECORDRATE, null, ""+(int)SoundDefaults.getRecordRate());
        
        user = SoundDefaults.getMixers(SoundDefaults.createAudioFormat(true));
        if (user.length > 0)
        {
        	makeDropPanel(left,173,MIXERS, user, user[SoundDefaults.getRecordDevice()]);
        }
        else
        {
            combos[MIXERS] = new JComboBox<String>();
            String title = LanguageText.getMessage("soundEditor", 196);
            left.add(new JLabel(title));
        }
        makePanel(left, ACTWINDOW, 5, ""+SoundDefaults.getActivationWindow());
        makePanel(left, ACTTHRESH, 6, ""+SoundDefaults.getActivationThreshold());
        makeDropPanel
                  (left,7,FRAMERATE, null,""+(int)SoundDefaults.getFrameRate());
        makeDropPanel(left,8,BITS, null,""+(int)SoundDefaults.getBits());
        makeDropPanel(left,9, CHANNELS, null, "" + SoundDefaults.getChannels());
        makeDropPanel(left,10,ENDIAN,null, (SoundDefaults.getBigEndian())? 1:0);
        makeDropPanel
                 (left,11,SIGNED, null, SoundDefaults.getEncoding().toString());
        makeDropPanel(left,12,AVG, null, SoundDefaults.getChannelOption());
        left.add(Box.createVerticalStrut(10));
        makePanel(left, TRIMSIZE, 17, ""+SoundDefaults.getTrimSize());
        makeDropPanel(left,18,ROUND,null, SoundDefaults.getRoundToZero()?1:0);
        makePanel(left, MAXRECORD, 19, ""+SoundDefaults.getMaxMin());
        left.add(Box.createVerticalGlue());
        add(left, BorderLayout.WEST);
        
        // Create Middle.
        JPanel middle = makeColumn(13);
        makeDropPanel(middle, 14, WINDOW, null, SoundDefaults.geWindowType());
        makePanel(middle, FILTERSIZE, 15, ""+SoundDefaults.getFilterSize());
        makeDropPanel(middle, 191, FILTER, null, SoundDefaults.getFilterType());
        middle.add(Box.createVerticalStrut(10));
        makePanel(middle,PREEMPHASIS, 21, ""+SoundDefaults.getEmphasisFactor());
        makePanel(middle, WINSIZE, 22, ""+SoundDefaults.getWindowSize());
        makePanel(middle, WINSHIFT, 23, ""+SoundDefaults.getWindowShift());
        middle.add(Box.createVerticalStrut(10));
        makeDropPanel(middle, 176, LPCTYPE, null, SoundDefaults.getLPCType());
        makePanel(middle, LPCCOEFS, 177, ""+SoundDefaults.getLPCCoefficients());
        middle.add(Box.createVerticalStrut(10));
        makeDropPanel(middle, 178, SPECT, null
                                , SoundDefaults.isNarrowBandSpectrograph()?1:0);
        makeDropPanel(middle,188,COLOR,null,SoundDefaults.isColorPalette()?1:0);
        middle.add(Box.createVerticalStrut(10));
        makeDropPanel(middle,183,RATEALG,null,SoundDefaults.getRateAlgorithm());
        makeDropPanel(middle, 179, PITCH, null, SoundDefaults.getPitchDetect());
        makeDropPanel
              (middle, 180,  RATE, null, ""+(int)SoundDefaults.getRateChange());
        middle.add(Box.createVerticalGlue());
        add(middle, BorderLayout.CENTER);
    
        // Create Right.
        JPanel right = makeColumn(20);
        makePanel
             (right, MELFILTERS, 24, ""+SoundDefaults.getNumberOfMelFilters());
        makeDropPanel(right,189,MELTYPE,null,SoundDefaults.getMelFilterType());
        makePanel
            (right, MELVARIANCE, 16, ""+SoundDefaults.getMelGaussianDampingFactor());
        makePanel(right, CEPSLEN, 27, ""+SoundDefaults.getCepstrumLength());
        makePanel(right, FEATURE, 28, ""+SoundDefaults.getFeatureMask());
        right.add(Box.createVerticalStrut(25));
        makePanel(right, MINFREQ, 25, ""+SoundDefaults.getMinFreq());
        makePanel(right, MAXFREQ, 26, ""+SoundDefaults.getMaxFreq());
        right.add(Box.createVerticalStrut(25));
        makeDropPanel(right,184,DTWDIST,null,SoundDefaults.getDTWDistance());
        makePanel(right,DTWCORRECT, 181
                , ""+SoundDefaults.getDTWCorrectness(SoundDefaults.CORRECT));
        makePanel(right,DTWCLOSE, 182
                , ""+SoundDefaults.getDTWCorrectness(SoundDefaults.CLOSE));

        String title = LanguageText.getMessage("soundEditor", 185);
        right.add(new JLabel(title));
        configureWeightPanel();
        right.add(weightPanel);

        right.add(Box.createVerticalGlue());
        add(right, BorderLayout.EAST);

        // Create error label
        errorLabel = new JLabel();
        errorLabel.setForeground(Color.RED);
        errorLabel.setPreferredSize(new Dimension(600, 25));
        add(errorLabel, BorderLayout.SOUTH);

    }  // End OptionPanel()


    /** Method to create a column panel with a centered title
     *
     * @param parent The parent component to add this title to
     * @param msg The message number to put in the title
     * @return The created title panel
     */
    private JPanel makeColumn(int msg)
    {
        JPanel column = new JPanel();
        column.setLayout(new BoxLayout(column, BoxLayout.Y_AXIS));
        column.setAlignmentX(0);
        column.setBackground(SoundDefaults.BACKGROUND);
        column.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel panel = new JPanel();
        panel.setAlignmentX(0);
        panel.setBackground(SoundDefaults.BACKGROUND);
        panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
        
        String title = LanguageText.getMessage("soundEditor", msg);
        JLabel titleLabel = new JLabel(title);

        panel.add(Box.createHorizontalGlue());
        panel.add(titleLabel);
        panel.add(Box.createHorizontalGlue());

        column.add(panel);
        column.add(Box.createVerticalStrut(10));
        column.add(Box.createVerticalGlue());
        return column;
    }

    /** Make a general text panel
     *
     * @param addPanel The panel to contain created panel
     * @param which  Array index to the text panel to create
     * @param msg Sound Help Set text containing parsing information
     * @param defaults The default value
     */
    private JTextField makePanel
            (JPanel addPanel, int which, int msg, String defaults)
    {
        String[] user = LanguageText.getMessageList("soundEditor", msg);
        String name = LanguageText.getMessage("soundEditor", msg);
        JPanel panel = new JPanel();
        JTextField field = makePanel(panel, name, user[1], defaults);
        fields[which] = (JTextField)field;
        addPanel.add(panel);
        addPanel.add(Box.createVerticalStrut(5));
        return field;
    }
        
    /** Make a text panel
     *
     * @param fieldPanel The JPanel to configure
     * @param title Prompt for the text field
     * @param toolTip A tool tip help string
     * @param defaults The minimum;maximum;type value
     * @return The text field that is part of this panel
     */
    private JTextField makePanel
            (JPanel fieldPanel, String title, String toolTip, String defaults)
    {
        fieldPanel.setBackground(SoundDefaults.BACKGROUND);
        fieldPanel.setLayout(new BoxLayout(fieldPanel, BoxLayout.X_AXIS));
        JLabel label = new JLabel(title.split(";")[0]);
        
        label.setToolTipText(toolTip);
        fieldPanel.add(label);
        fieldPanel.add(Box.createHorizontalStrut(5));
        fieldPanel.setAlignmentX(0);

        JTextField component = new JTextField();
        component.setMinimumSize(new Dimension(100,20));
        component.setMaximumSize(new Dimension(100,20));
        component.setPreferredSize(new Dimension(100,20));
        component.setName(title + ";" + defaults);
        component.setToolTipText(toolTip);
        component.setText(defaults);
        component.addActionListener(this);
        
        fieldPanel.add(component);
        fieldPanel.add(Box.createHorizontalGlue());
        return component;
   }

    /** Make a drop down menu
     *
     * @param panel Panel to add this drop down to
     * @param toolTip tool tip to create
     * @param items drop down selections
     * @param select initial selected item
     */
    private void makeDropPanel
          (JPanel panel, int msg, int which, String[] items, String item)
    {
        int select = 0;
        if (items==null)
        {
            String[] title = LanguageText.getMessageList("soundEditor", msg);
            items = new String[title.length - 2];
            for (int i=0; i<items.length; i++) items[i] = title[i+2];
        }
        for (int i=0; i<items.length; i++)
        {
            if (items[i].trim().equals(item)) {select = i; break; }
        }
        makeDropPanel(panel, msg, which, items, select);
    }

    /** Make a drop down menu
     *
     * @param panel Panel to add this drop down to
     * @param toolTip tool tip to create
     * @param items drop down selections
     * @param select initial selected item number
     */
    private void makeDropPanel
          (JPanel panel, int msg, int which, String[] items, int select)
    {
        String[] title = LanguageText.getMessageList("soundEditor", msg);
        JLabel label = new JLabel(title[0]);

        if (items==null)
        {   items = new String[title.length - 2];
            for (int i=0; i<items.length; i++) items[i] = title[i+2];
        }

        label.setToolTipText(title[1]);
        label.setAlignmentX(0);
        panel.add(label);

        combos[which] = new JComboBox<String>(items);
        combos[which].setMinimumSize(new Dimension(100,20));
        combos[which].setMaximumSize(new Dimension(200,20));
        combos[which].setPreferredSize(new Dimension(200,20));
        combos[which].setAlignmentX(0);
        if (select<0 || select>=items.length) select = 0;
        combos[which].setSelectedIndex(select);
        panel.add(combos[which]);
        panel.add(Box.createVerticalStrut(5));
    }

    /** Create or update the panel of weights */
    private void configureWeightPanel()
    {   
        try
        {
           String data =  LanguageText.getMessage("soundEditor", 187);
           String toolTip = data.split(";")[0];
           double[] DTWWeights = SoundDefaults.getDTWWeights();

           weightPanel.removeAll();

           // A weight for cepstrals, delta cepstrals, and delta delta cepstrals
           // A weight for energy, delta energy, and delta delta energy
           int cepstrals = Integer.parseInt(fields[CEPSLEN].getText());
           int columns = 6;
           int numWeights = 3 * (cepstrals + 1);
           int rows = (numWeights + columns - 1)/columns;
           weightPanel.setLayout(new GridLayout(rows,columns));
           weightPanel.setAlignmentX(0);

           JPanel inputPanel;
           String info, defaults, padded;
           JTextField field;

           for (int i=0; i<numWeights; i++)
           {   padded = "  " + i;
               info = padded.substring(padded.length() - 2) + ";" + data;
               if (i>=DTWWeights.length) defaults = "0.0";
               else defaults = "" + DTWWeights[i];

               inputPanel = new JPanel();
               if (i>=weights.size())
               {
                  field = makePanel(inputPanel, info, toolTip, defaults);
                  weights.add(field);
               }
               else field = makePanel
                          (inputPanel, info, toolTip, weights.get(i).getText());

               field.setPreferredSize(new Dimension(45,20));
               weightPanel.add(inputPanel);
           }
           weightPanel.repaint();
        }

        catch (Exception e) {}
    }

    /** Update the drop down menu of available recording devices */
    private void updateDisplay() throws NumberFormatException
    {
        AudioFormat.Encoding encoding = SoundDefaults.getEncoding();
        int bits = Integer.parseInt((String)combos[BITS].getSelectedItem());
        int channels = Integer.parseInt((String)combos[CHANNELS].getSelectedItem());
        float recordRate = Integer.parseInt((String)combos[RECORDRATE].getSelectedItem());
        boolean big = combos[ENDIAN].getSelectedIndex() != 0;
        int recordDevice = combos[MIXERS].getSelectedIndex();
        String recordItem = (String)combos[MIXERS].getSelectedItem();

        AudioFormat format = new AudioFormat
           (encoding,recordRate,bits,channels,bits*channels/8,recordRate,big);
                    
        String[] mixers = SoundDefaults.getMixers(format);
        combos[MIXERS].removeAllItems();

        if (mixers.length ==0)
        {
        	return;
        }

        // Valid, update the drop down menu
        for (int m=0; m<mixers.length; m++) combos[0].addItem(mixers[m]);
        if (recordDevice>=mixers.length) recordDevice--;
        
        if (recordItem != null)
        	combos[MIXERS].setSelectedItem(recordItem);
        if (combos[MIXERS].getSelectedItem()==null)  
        	combos[MIXERS].setSelectedIndex(0);
    }
    
   /** Update Sound Parameters.
    *  @return false if fails, otherwise true;
    */
   public boolean updateValues()
   {   StringBuilder builder = new StringBuilder();
       
       for (int i=0; i<TEXTS; i++)
       {   try  {  verifyFieldData(fields[i]); }
           catch (Exception e) {  builder.append((e.getMessage()+"\n"));  }
       }

       int cepstrals = Integer.parseInt(fields[CEPSLEN].getText());
       int numWeights = 3 * (cepstrals + 1);
       JTextField weightField;
       for (int i=0; i<numWeights; i++)
       {
    	   if (i<weights.size())
    	   {
    		   weightField = weights.get(i);
    		   try { verifyFieldData(weightField); }
    		   catch (Exception e) { builder.append((e.getMessage() + "\n")); }
    	   }
       }

       // Extra validation
       double minFreq = Double.parseDouble(fields[MINFREQ].getText());
       double maxFreq = Double.parseDouble(fields[MAXFREQ].getText());
       double winSize = Double.parseDouble(fields[WINSIZE].getText());
       double winShift = Double.parseDouble(fields[WINSHIFT].getText());
       double DTWClose = Double.parseDouble(fields[DTWCLOSE].getText());
       double DTWCorrect = Double.parseDouble(fields[DTWCORRECT].getText());
       if (minFreq>maxFreq) 
       {  builder.append((LanguageText.getMessage("soundEditor",174)+"\n"));  }
       if (winShift>winSize)
       {  builder.append((LanguageText.getMessage("soundEditor",175)+"\n"));  }
       if (DTWClose>DTWCorrect)
       {  builder.append((LanguageText.getMessage("soundEditor",186)+"\n")); }
       updateDisplay();

       if (builder.length()>0)
       {  JOptionPane.showMessageDialog(this, builder.toString());
          return false;
       }

       // set values from drop down menus
       String stringValue = "";
       int index = 0, value = 0;
       for (int i = 0; i<COMBOS; i++)
       {   stringValue = (String)combos[i].getSelectedItem();
           index = combos[i].getSelectedIndex();
           try  { value = Integer.parseInt(stringValue); }
           catch (Exception e) {}

           switch (i)
           {   case MIXERS:    SoundDefaults.setRecordDevice(index);
                               break;
               case RECORDRATE:SoundDefaults.setRecordRate(value);
                               break;
               case EXTS:      SoundDefaults.setAudioCompression(stringValue);
                               break;
               case CHANNELS:  SoundDefaults.setChannels(value);
                               break;
               case ENDIAN:    SoundDefaults.setBigEndian(index!=0);
                               break;
               case SIGNED:    AudioFormat.Encoding[] encodings =
                                     {AudioFormat.Encoding.PCM_SIGNED,
                                      AudioFormat.Encoding.PCM_UNSIGNED,
                                      AudioFormat.Encoding.ULAW,
                                      AudioFormat.Encoding.ALAW };
                               SoundDefaults.setEncoding(encodings[index]);
                               break;
               case AVG:       SoundDefaults.setChannelOption(index);
                               break;
               case WINDOW:    SoundDefaults.seWindowType(index);
                               break;
               case FILTER:    SoundDefaults.setFilterType(index);
                               break;
               case ROUND:     SoundDefaults.setRoundToZero(index!=0);
                               break;
               case FRAMERATE: SoundDefaults.setFrameRate(value);
                               break;
               case BITS:      SoundDefaults.setBits(value);
                               SoundDefaults.setFrameBytes
                                          (value*SoundDefaults.getChannels()/8);
                               break;
               case LPCTYPE:   SoundDefaults.setLPCType(index);
                               break;
               case SPECT:     SoundDefaults.setNarrowBandSpectrograph(index==1);
                               break;
               case COLOR:     SoundDefaults.setColorPalette(index==1);
               				   break;
               case PITCH:     SoundDefaults.setPitchDetect(index);
                               break;
               case RATEALG:   SoundDefaults.setRateAlgorithm(index);
                               break;
               case RATE:      SoundDefaults.setRateChange(value);
                               break;
               case DTWDIST:   SoundDefaults.setDTWDistance(index);
                               break;
               case MELTYPE:   SoundDefaults.setMelFilterType(index);
                               break;
           }
       }

       // set values obtained from text fields
       float floatValue = 0;
       double doubleValue = 0;
       long   longValue = 0;
       for (int i=0; i<TEXTS; i++)
       {   stringValue = fields[i].getText();
           try { value = Integer.parseInt(stringValue); } catch (Exception e) {}
           try { longValue = Long.parseLong(stringValue); } catch (Exception e) {}
           try { floatValue = Float.parseFloat(stringValue); } catch (Exception e) {}
           try { doubleValue = Double.parseDouble(stringValue); } catch (Exception e) {}

           switch (i)
           {   case ACTWINDOW:   SoundDefaults.setActivationWindow(value);
                                 break;
               case ACTTHRESH:   SoundDefaults.setActivationThreshold(value);
                                 break;
               case FILTERSIZE:  SoundDefaults.setFilterSize(value);
                                 break;
               case TRIMSIZE:    SoundDefaults.setTrimSize(value);
                                 break;
               case MAXRECORD:   SoundDefaults.setMaxMins(value);
                                 break;
               case PREEMPHASIS: SoundDefaults.setEmphasisFactor(floatValue);
                                 break;
               case WINSIZE:     SoundDefaults.setWindowSize(floatValue);
                                 break;
               case WINSHIFT:    SoundDefaults.setWindowShift(floatValue);
                                 break;
               case MELFILTERS:  SoundDefaults.setNumberOfMelFilters(value);
                                 break;
               case MELVARIANCE: SoundDefaults.setMelGaussianVariance(doubleValue);
                                 break;
               case MINFREQ:     SoundDefaults.setMinFreq(doubleValue);
                                 break;
               case MAXFREQ:     SoundDefaults.setMaxFreq(doubleValue);
                                 break;
               case CEPSLEN:     SoundDefaults.setCepstrumLength(value);
                                 break;
               case FEATURE:     SoundDefaults.setFeatureMask(longValue);
                                 break;
               case LPCCOEFS:    SoundDefaults.setLPCCoefficients(value);
                                 break;
               case DTWCLOSE:    SoundDefaults.setDTWCorrectness
                                           (SoundDefaults.CLOSE, doubleValue);
                                 break;
               case DTWCORRECT:  SoundDefaults.setDTWCorrectness
                                           (SoundDefaults.CORRECT, doubleValue);
                                 break;
           }
       }

       double[] weightData = new double[weights.size()];
       for (int i=0; i<numWeights; i++)
       {  
          try
          {
                 weightData[i] = Double.parseDouble(weights.get(i).getText());
          }
          catch (Exception e) {}
       }
       SoundDefaults.setDTWWeights(weightData);
       configureWeightPanel();
       return true;
    
   }   // End of update values.

   public void actionPerformed(ActionEvent event)
   {
       JTextField field = (JTextField)event.getSource();
       String  name     = field.getName();
       int     lastX    = name.lastIndexOf(";")+1;

       try
       {  verifyFieldData(field);
          updateDisplay();
          if (field==fields[CEPSLEN])  configureWeightPanel();

          // Update the default value
          String  stringValue = field.getText().toLowerCase().trim();
          field.setName(name.substring(0, lastX)+ stringValue);
          errorLabel.setText("");

      }   // End of try
      catch (Exception ex)
      {  String msg = LanguageText.getMessage("soundEditor",30)+ex.getMessage();
         errorLabel.setText(msg);

         String  originalValue = name.substring(lastX);
         field.setText(originalValue);
      }
   }     // End of actionPerformed()

   /** Verify that the contents of a text field is legal
    *
    * @param field The JTextField component
    * @throws NumberFormatException
    */
   private void verifyFieldData(JTextField field) throws NumberFormatException
   {
      String[]   bounds = field.getName().split(";");
      String     stringValue = field.getText().toLowerCase().trim();
      NumberFormatException error
                   = new NumberFormatException(bounds[0] + ": " + bounds[1]);

      double low = Double.parseDouble(bounds[2]);
      double high = Double.parseDouble(bounds[3]);
      switch(bounds[4].charAt(0))
      {   case 'i':  int value = Integer.parseInt(stringValue);
                     if (value<low || value>high) throw error;
                     break;
          case 'f':  float floatValue = Float.parseFloat(stringValue);
                     if (floatValue<low || floatValue>high) throw error;
                     break;
          case 'd':  double doubleValue = Double.parseDouble(stringValue);
                     if (doubleValue<low || doubleValue>high) throw error;
                     break;

          case 'l':  long longValue = Long.parseLong(stringValue);
                     if (longValue<low || longValue>high) throw error;
                     break;
      }
   }  // End of verifyFieldData
}