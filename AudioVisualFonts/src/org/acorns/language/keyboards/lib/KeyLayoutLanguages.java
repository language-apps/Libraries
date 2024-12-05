/*
 *   KeyLayoutLanugages.java
 *
 *   @author  HarveyD
 *   @version 1.00
 *   Dan Harvey - Professor of Computer Science
 *   Southern Oregon University, 1250 Siskiyou Blvd., Ashland, OR 97520-5028
 *   harveyd@sou.edu
 *
 *   Copyright 2010, all rights reserved
 *
 * This software is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * To receive a copy of the GNU Lesser General Public write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 */
package org.acorns.language.keyboards.lib;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

import java.net.*;
import java.io.*;
import java.util.*;

/** This class maintains a list of .keylayout files in a specified folder */
public class KeyLayoutLanguages implements WindowListener
{
    private ArrayList<String> languages;
    private ArrayList<URL>    urls;
    private ArrayList<ImportKeyboard> keyboards;
    private String            selection;
    private int               select;

    private JDialog   jDialog;

    /** Constructor to ascertain the list of .keylayout choices */
    public KeyLayoutLanguages(URL url)
    {   
    	ArrayList<String> strs = new ArrayList<String>();
    	String[] files = null;
        try
        {
        	File file = new File(url.toURI());
        	files = file.list();
        	if (files!=null)
        	{
	        	for (String line: files)
	        	{
	                if (line.toLowerCase().endsWith(".keylayout"))
	                {  strs.add(line);  }
	        	}
        	}
        }
        catch (Exception e) {}
        
        languages = new ArrayList<String>();
        urls      = new ArrayList<URL>();
        keyboards = new ArrayList<ImportKeyboard>();
        URL newURL = null;
        for (int i=0; i<strs.size(); i++)
        {   try
            {   newURL = new URI(url + strs.get(i)).toURL();
            	addLanguage(newURL);
            }
            catch (Exception e) 
        	{
            	System.out.println(e);
        	}
        }
    }
 
    /** Add a new language to the list */
    public void addLanguage(URL url) throws Exception
    {
        ImportKeyboard keyboard;
        String language;

        keyboard = new ImportKeyboard(url);
        language = keyboard.getLanguage();
        if (language!=null)
        {  
           keyboards.add(keyboard);
           languages.add(language);
           urls.add(url);
        }
    }
    
    public void removeLanguage(String language)
    {
        int index = findLanguage(language);
        if (index<0) return;
        
        languages.remove(index);
        urls.remove(index);
    }

    /** Method to return the available languages with key maps
     *  @return array of strings containing the available languages
     */
    public String[] getLanguages()
    {   String[] langArray = new String[languages.size()];
        langArray = languages.toArray(langArray);
        return langArray;
    }

    /** Method to return whether there are .keylayout file choices */
    public boolean isKeyLayouts()
    {  return languages!=null && languages.size()>0; }

    /** Method to get the URL to a .keylayout file
     *
     * @param frame The parent frame
     * @param icon The icon to use with the dialog
     * @return The URL to the .keylayout file or null
     */
    public URL getKeyboardURL(JFrame frame, Icon icon)
    {   if (languages==null || languages.size()==0) return null;

        JDialog dialog = getDialogPane(frame, icon);
        dialog.setVisible(true);
        if (select<0) return null;
        selection = languages.get(select);
        return urls.get(select);
    }   // End of getKeyboardURL

    private JDialog getDialogPane(JFrame frame, Icon icon)
    {   if (jDialog==null)
        {
            if (selection == null) selection = languages.get(0);
            String[] options = new String[languages.size()];
            options = languages.toArray(options);

            /** Make panel for icon and drop down menu */
            JPanel dropPanel = new JPanel();
            dropPanel.setLayout(new BoxLayout(dropPanel, BoxLayout.X_AXIS));
            dropPanel.add(Box.createHorizontalGlue());

            dropPanel.add(new JLabel(icon));
            dropPanel.add(Box.createHorizontalStrut(10));
            final JComboBox<String> box = new JComboBox<String>(options);
            box.setSelectedItem(selection);
            dropPanel.add(box);
            dropPanel.add(Box.createHorizontalGlue());

            /* Make panel for the OK and Cancel buttons */
            JPanel buttonPanel = new JPanel();
            buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.X_AXIS));
            buttonPanel.add(Box.createHorizontalGlue());

            JButton okButton = new JButton("OK");
            okButton.addActionListener(new ActionListener()
            {  public void actionPerformed(ActionEvent event)
               {  select = box.getSelectedIndex();
                  jDialog.setVisible(false);
               }
            });
            buttonPanel.add(okButton);
            buttonPanel.add(Box.createHorizontalStrut(10));

            JButton cancelButton = new JButton("Cancel");
            cancelButton.addActionListener(new ActionListener()
            {  public void actionPerformed(ActionEvent event) 
               {  select = -1;
                  jDialog.setVisible(false);
               }
            });
            buttonPanel.add(cancelButton);
            buttonPanel.add(Box.createHorizontalStrut(25));

            /* Make panel to hold the entire dialog */
            JPanel envelope = new JPanel();
            envelope.setLayout(new BoxLayout(envelope, BoxLayout.Y_AXIS));
            envelope.add(Box.createVerticalStrut(5));
            JLabel label =  new JLabel("Please select your language");
            label.setAlignmentX(0.5f);
            envelope.add(label);
            envelope.add(Box.createVerticalStrut(5));
            envelope.add(dropPanel);
            envelope.add(Box.createVerticalStrut(5));
            envelope.add(buttonPanel);
            envelope.add(Box.createVerticalStrut(5));

            /* Now create the dialog */
            jDialog = new JDialog(frame, "Language selection dialog", true);
            jDialog.setContentPane(envelope);
            jDialog.addWindowListener(this);
            jDialog.setResizable(false);
            jDialog.pack();
            jDialog.setLocationRelativeTo(null);

        }
        return jDialog;
    }

    /** Method to get a KeyboardHandler object corresponding to a language
     *
     * @param language The language for the handler object
     * @return The KeyboardHandler object or null if none for the language
     */
    private KeyboardHandler getHandler(String language)
    {  
       int index = findLanguage(language);
       if (index<0) return null;
       KeyboardHandler handler;
       try  {  handler = new KeyboardHandler(language, keyboards.get(index));  }
       catch (Exception e) { handler = null; }
       return handler;
    }

    /** Attempt to add the key listener to this component
     *       If none available, just return
     *
     * @param component Component  to hook
     * @param language The language in question
     * @param handler Keyboard handler object to use
     * @return the KeyboardHandler object
     */
    public void hookComponent
            (Component component, String language)
    {  
        unhookComponent(component);

        int index = findLanguage(language);
        if (index<0) return;  // No such language

   	    try  
   	    {  
    		  KeyboardHandler handler = getHandler(language); 
   	  		  if (handler!=null) component.addKeyListener(handler);
   	    }
	    catch (Exception e) {}
    }

    /** Unhook the  key listener from this component if it is hooked
     *
     * @param component The component  to unhook
     */
    public void unhookComponent(Component component)
    {   
    	KeyListener[] listeners = component.getKeyListeners();
        for  (int i=0; i<listeners.length; i++)
        {  if (listeners[i] instanceof KeyboardHandler)
           {  component.removeKeyListener(listeners[i]);  }
        }
    }

    /** Copy all of the .keylayout files to a destination directory */
    public void copyKeyLayouts(String dest) throws IOException
    {  if (urls==null) return;

       String separator = System.getProperty("file.separator");
       String base = dest + separator + "languages";
       File file = new File(base);
       writeLanguageNames(file);
       for (int i=0; i<urls.size(); i++)
       {   file = new File(base + languages.get(i) + ".keylayout");
           copyKeyLayout(urls.get(i), file);
       }
    }

    /** Method to write a file of language names */
    private void writeLanguageNames(File dest) throws IOException
    {   FileOutputStream output = new FileOutputStream(dest);
        BufferedWriter out = new BufferedWriter(new OutputStreamWriter(output));

        String line;
        for (int i=0; i<languages.size(); i++)
        {  line = languages.get(i) + ".keylayout";
           out.write(line);
           out.newLine();
        }
        out.close();
    }

    /** Method to make a copy of a .keylayout file */
    public void copyKeyLayout(URL source, File file) throws IOException
    {   
    	InputStream input = source.openStream();
        OutputStream output = new FileOutputStream(file);

        byte[] buf = new byte[1024];
        int len;
        while ((len=input.read(buf)) > 0)  { output.write(buf, 0, len); }
        input.close();
        output.close();
    }
    
    /** Method to convert KeyLayout XML to a String */
    public String exportKeyLayout(String language)
    {
    	int index = findLanguage(language);
    	if (index<0) return null;
    	
    	String result = null;
    	try
    	{
    		ImportKeyboard keyboard = keyboards.get(index);
    		result = keyboard.toJSONString();
    		result = result.replaceAll("\\n+", "");
    	}
    	catch (Exception e) 
    	{ result = null; }
    	return result;
    }

    /** Method to find the index of a language in the list ignoring case
     *
     * @param language The language to find
     * @return return index >=0 or -1 if not found
     */
    private int findLanguage(String language)
    {  int index = -1;
       String selectL;
       language = language.toLowerCase();
       for (int i=0; i<languages.size(); i++)
       {   selectL = languages.get(i).toLowerCase();
           if (selectL.equals(language))  { index = i; break; }
       }
       return index;
    }

   public void  windowActivated(WindowEvent e) {}
   public void  windowClosing(WindowEvent e) { System.exit(0); }
   public void  windowDeactivated(WindowEvent e) {}
   public void  windowDeiconified(WindowEvent e) {}
   public void  windowIconified(WindowEvent e) {}
   public void  windowOpened(WindowEvent e) {}
   public void  windowClosed(WindowEvent e) {}
}       // End of KeyLayoutLanguages class
