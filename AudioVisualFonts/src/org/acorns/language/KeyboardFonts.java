/*
 * KeyboardFonts.java
 *
 *   @author  HarveyD
 *   @version 3.00 Beta
 *
 *   Copyright 2007-2015, all rights reserved
 */
package org.acorns.language;

import java.awt.Component;
import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.URL;
import java.util.ArrayList;

import javax.swing.DefaultListCellRenderer;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;
import javax.swing.text.JTextComponent;

import org.acorns.language.keyboards.data.Status;
import org.acorns.language.keyboards.lib.KeyLayoutLanguages;

/** Process indigenous fonts and keyboard layouts */
public class KeyboardFonts implements Cloneable
{  
    private LanguageFont[] fonts;  // Array of language fonts
    private int  activeLanguage;   // Index to the active font
    private int  numberLanguages;  // Number of fonts in the array
    
    private transient static KeyLayoutLanguages keyLayouts;
    private transient static FontLanguages      embeddedFonts;
   
   /** Constructor for initializing the available keyboard fonts
    */
    public KeyboardFonts()
    {    
    	 fonts = new LanguageFont[8];
         fonts[0]        = new LanguageFont("Times New Roman", 12, "English");
         activeLanguage  = 0;
         numberLanguages = 1;  
    }
    
   /** Remove selected language from keyboards available
    *  @param index to the language to remove
    *  @return true if removed or false if cannot
    */
    public boolean removeLanguage(int index)
    { 
        if (index<=0) return false;
      
        if (activeLanguage==index) activeLanguage = 0;
        for (int r=index+1; r<numberLanguages; r++) 
        {
           fonts[r-1]= fonts[r];
           if (activeLanguage==r) activeLanguage = r-1;
        }
        numberLanguages--;
        return true;
    }
   
   /** Add a new native language to the table
    *  @param name The name of the font family
    *  @param size The size of the font in points
    *  @param language The language that applies to this font
    *  @return true if insertion, false if modification
    */
   public boolean addLanguage(String name, int size, String language)
   {   
	   int index = findLanguage(language);
       LanguageFont newFont = new LanguageFont(name, size, language);
       
       if (index>=0) { fonts[index] = newFont; return false; }
       
       if (numberLanguages == fonts.length)
       {   
    	   LanguageFont[] newFonts = new LanguageFont[fonts.length*2];
           System.arraycopy(fonts, 0, newFonts, 0, numberLanguages);
           fonts = newFonts;
       }
       fonts[numberLanguages++] = newFont;
       return true;
    }
   
   /** Set the component to the font for the active language
    *  @param component JComponent to set to the active language
    *  @return true if component font set successfully; always true.
    */
     public boolean setFont(JComponent component)
     {   
    	 Font font = getEmbeddedFont(fonts[activeLanguage].getFont());
         component.setFont(font); 
         hookLanguage(component, fonts[activeLanguage].getLanguage());
         return true;
     }
     
   /** Set the component font to a specified language
    *  @param language font language
    *  @param component the gui component to attach to this language
    *  @return true if successful
    */
     public boolean setFont(String language, Component component)
     {  
    	Font font = getFont(language);
        component.setFont(font);
        hookLanguage(component, language);
        return true;
     }

     /** Method to set a language font, using a particular size
      *
      * @param language The language in question
      * @param component The component to apply the font to
      * @param size The desired size
      * @return true if successful
      */
     public boolean setFont(String language, Component component, int size)
     {
        Font font = getFont(language);

        String name = font.getName();
        int style   = font.getStyle();
        font = new Font(name, style, size);
        component.setFont(font);
        hookLanguage(component, language);
        return true;
     }

     /** Export Embedded fonts to an array of strings
      *    (called when a Lesson web-page is created)
      *
      * @param path Destination File
      */
     public ArrayList<String[]> exportEmbeddedFonts()
     {  
    	ArrayList<String[]>keyboards = new ArrayList<String[]>();
    	
       	keyLayouts = getKeyLayouts();
  
        String[] languages = getLanguages();
        for (String language: languages)
        {
            String xmlString = keyLayouts.exportKeyLayout(language);
            if (xmlString != null)
            {
            	String[] data = new String[2];
            	data[0] = language;
            	data[1] = xmlString;
            	keyboards.add(data);
            }
        }
        
        return keyboards;
     }
     
     /** Method to hook a keyboard layout to a component
      * 
      * @param component The component to hook
      * @param language The language for this layout
      */
     private void hookLanguage(Component component, String language)
     {  
    	 int index = findLanguage(language);
         if (index<0) return;
         
         keyLayouts = getKeyLayouts();
         if (keyLayouts==null) return;
         if (component instanceof JTextComponent)
         {  
             keyLayouts.hookComponent(component, language);
         }
     }

     /** Get object containing embedded keyboards */
     public static KeyLayoutLanguages getKeyLayouts()
     {   
    	 if (keyLayouts==null)
         {   
    		 URL url = getEmbeddedURL(false);
             keyLayouts = new KeyLayoutLanguages(url);
         }
         return keyLayouts;
     }

     /** Get object containing embedded fonts */
     private FontLanguages getEmbeddedFonts()
     {  
    	if (embeddedFonts==null)
        {   URL url = getEmbeddedURL(true);
            embeddedFonts = new FontLanguages(url);
        }
        return embeddedFonts;
     }

     /** Method to get the URL to embedded keylayouts or fonts
      *
      * @param ttf true if url to fonts, false for keylayouts
      * @return URL or null if fail
      */
     private static URL getEmbeddedURL(boolean ttf)
     {   
    	 URL url = null;
         try
         {   
        	File file = Status.getDefaultPath(ttf);
            url = file.toURI().toURL();
         }
        catch (Exception e) {}
     return url;
     }
     
   /** Set the active language
    *  @param language the desired language
    *  @return true if successful, false otherwise
    */
     public boolean setLanguage(String language)
     {  int index = findLanguage(language);
        if (index<0) return false;
        activeLanguage = index;
        return true;
     }
     
   /** Get the active language */
     public String getLanguage()
     {  return fonts[activeLanguage].getLanguage(); }
     
   /** Get the language at a particular index 
     * @param index The index of this language
     */
     public String getLanguage(int index)
     {  return fonts[index].getLanguage(); }
     
     
   /** Set the active language based on its index
    *
    *  @param index which language in the table
    *  @return true if successful
    */
    public boolean setLanguage(int index)
    {
       if (index<0 || index>numberLanguages) return false;
       activeLanguage = index;
       return true;
    }
   
   /** Get array of available languages
    *  @return array of strings containing available languages
    */
   public String[] getLanguages()
   {  
      String[] languages = new String[numberLanguages];
      for (int f=0; f<numberLanguages; f++)
      {
         languages[f] = fonts[f].getLanguage();
      }
      return languages;
   }
   
   /** Get the font for the active language */
   public Font getActiveFont() 
   { return getEmbeddedFont(fonts[activeLanguage].getFont()); }
   
   /** Get the font for a particular language */
    public Font getFont(int index) 
    { 
    	if (index<0) return null;
    	return getEmbeddedFont(fonts[index].getFont()); 
    }
    
    /** Get the font for a particular language */
    public Font getFont(String language)
    {
        int index = findLanguage(language);
        
        Font font = getFont(index);
        if (font==null)
           return new Font(null, Font.PLAIN, 12);
        return font;
    }
    
  /** Get the special keys for a particular lnguage */
  public String getSpecials(String language)
  {
      int index = findLanguage(language);
      if (index<0) return "";
      
	  LanguageFont langFont = fonts[index];
	  return langFont.getSpecials();
  }
  
  public void setSpecials(String language, String data)
  {
      int index = findLanguage(language);
      if (index<0) return;
      
	  LanguageFont langFont = fonts[index];
	  langFont.setSpecials(data);
  }

    /** Replace the font with the embedded object if it exists
     *
     * @param font The font in question
     */
    private Font getEmbeddedFont(Font font)
    {
       embeddedFonts = getEmbeddedFonts();
       Font embedded = embeddedFonts.getFont(font.getName());
       if (embedded!=null)
       {   embedded = embedded.deriveFont(Font.PLAIN, font.getSize());
           font = embedded;
       }
       return font;
    }
   
   /** Get the index to the active language */
   public int getActiveIndex()  { return activeLanguage;  }

   /** Get array of font information as a string
    *  @return array of strings with font family, font size, and language for each font.
    */
    public String[] getLanguageInfo()
    {
    	return getLanguageInfo(false);
    }
    public String[] getLanguageInfo(boolean includeSpecials)
    {
       String[] languages = new String[numberLanguages];
       String   active    = "";
       
       for (int f=0; f<numberLanguages; f++)
       {
           if (f==activeLanguage) active = "--> ";
           else active = "    ";
           languages[f] = active + fonts[f].toString(includeSpecials);
       }
       return languages;
    }
   
   /** Method to find the index for the language.
    *  @return index of language or -1 if not found
    */ 
   private int findLanguage(String language)
   {    language = language.toLowerCase();
        for (int f=0; f<numberLanguages; f++)
        {  if (fonts[f].getLanguage().toLowerCase().equals(language)) 
           {  return f;  }
        }
        return -1;
   }  // End of findLanguage()
   
   /** Method to create a clone of this object */
   public @Override Object clone()
   {  try 
      {  KeyboardFonts result = (KeyboardFonts) super.clone();
			      result.fonts = new LanguageFont[fonts.length];
         for (int f=0; f<numberLanguages; f++)
         {  result.fonts[f] = (LanguageFont)fonts[f].clone();
         }
         return result;
      } catch (CloneNotSupportedException e) { return null; }
  }
   
  /* Static methods to set or retrieve available keyboard fonts for indigenous languages */
  private static KeyboardFonts keyboards = null;
   
   /** Set the languages that are available 
    *  @param fonts KeyboardFonts object or null if it doesn't exist
    */
   public static void setLanguageFonts(KeyboardFonts fonts)
   {   if (fonts == null) keyboards = new KeyboardFonts();
       else               keyboards = fonts;
   }
   
   /** Get the  keyboard fonts that are available for indigenous languages */
   public static KeyboardFonts getLanguageFonts()   
   {  if (keyboards==null) 
	   	   setLanguageFonts(null);
      keyLayouts = KeyboardFonts.getKeyLayouts();
      return keyboards;  
   }
   
   /*** write the keyboard fonts to disk in text format */
   public static void writeFonts(String path)
   {   PrintWriter out = null;
       try
       {
          out = new PrintWriter(new File(path), "UTF-8");
         
          String[] fontInfo = keyboards.getLanguageInfo(true);
          for (int i=0; i<fontInfo.length; i++)  
          { out.println(fontInfo[i]); }
       }
       catch (Exception e) {}
       try { out.close(); } catch (Exception e) {}       
   }
   
   /** Read keyfoard fonts from disk using specified path */
   public static void readFonts()
   {
       File file = Status.getDefaultPath(false);
       String path = file.getAbsolutePath();
	   
	   String separator = File.separator;
	   path += separator + "keylayouts.txt";
	   readFonts(path);
   }
   
   /** Read the keyboard fonts from disk and initialize the keyboard language array */
   public static void readFonts(String path)
   {   
       String languageData;
       String[] extract;
       boolean  active;
       int      size;
	   boolean success = true;
       
       setLanguageFonts(null);

       try (BufferedReader in = new BufferedReader(
			   new InputStreamReader(
	                      new FileInputStream(path ), "UTF8"));)
       {  
   	      while (true)
          {
             languageData = in.readLine();
             active  = (languageData.substring(0,4).equals("--> "));
             extract = LanguageFont.extractToString(languageData.substring(4));
             try
             {  size = Integer.parseInt(extract[1]); }  
             catch (Exception e) { size = 12; }
                     
             keyboards.addLanguage(extract[0], size, extract[2]);
             if (active) keyboards.setLanguage(extract[2]);
             
             int index = keyboards.findLanguage(extract[2]);
             if (index>=0) 
            	 keyboards.fonts[index].setSpecials(extract[3]);            	 
          }
       }
       catch (NullPointerException npe) {}
       catch (Exception e) // No acorns font files specified, look in the keylayouts folder
       {
    	   success = false;
       }
       
       if (!success)
       {
            boolean first = true;
            try (BufferedReader in = new BufferedReader(
     			   new InputStreamReader(
     	                      new FileInputStream(path), "UTF8"));)
            {  
            	while (true)
            	{
	                languageData = in.readLine();
	                if (languageData.trim().length()==0)
	                	continue;
	                
	                extract = languageData.split(",");
	                
	                active = first;
	                first = false;
	                
	                try
	                {  
	                	size = Integer.parseInt(extract[1].trim()); 
	                }  
	                catch (Exception e) { size = 12; }
	                        
	                keyboards.addLanguage(extract[0].trim(), size, extract[2].trim());
	                if (active) keyboards.setLanguage(extract[2]);
            	}
    	    }
            catch (Exception e) {}
       }
   }
   
   /** Initialize the available keyboard languages based on a string of fonts */
   public static void setFonts(String[] info)
   {
       setLanguageFonts(null);       
       if (info==null) return;       
       String[] extract;
       boolean  active;
       int      size;
       
       for (int f=0; f<info.length; f++)
       {   active  = (info[f].substring(0,4).equals("--> "));
           if (active) info[f] = info[f].substring(4);
           
           extract = LanguageFont.extractToString(info[f]);
           try
           {  size = Integer.parseInt(extract[1]); }  
           catch (Exception e) { size = 12; }
                     
           keyboards.addLanguage(extract[0], size, extract[2]);
           if (active) keyboards.setLanguage(extract[2]);  
       }       
   }
   
   /** Create a combo box of languages
    * 
    * @param select The language to select or null to use the active language
    * @param all true if add all languages, false to add only registered languages
    * @return Created JComboBox
    */
   public JComboBox<String> createLanguageComboBox(String select, boolean all)
   {
	   String[] fontList = getLanguages();
	   if (all) fontList = getDisplayableFonts();
	   if (fontList == null) fontList = new String[0];

	   final JComboBox<String> fontCombo = new JComboBox<String>(fontList);
 	   fontCombo.setEditable(false);
	   
	   if (select == null) select = getLanguage();
	   
	   setFont(select, fontCombo);
	   fontCombo.setSelectedItem(select);

	   fontCombo.setRenderer(new FontCellRenderer(all, fontCombo));
	   
	   fontCombo.addItemListener( new ItemListener() {
		     public void itemStateChanged(ItemEvent e1)
		      {
		         if(e1.getStateChange() == ItemEvent.SELECTED)
		         {
		       	 	String language = (String)(String)e1.getItem();
		      	 	setFont(language, fontCombo); 
		         }
		      }
	   });
   	   return fontCombo;
   }
   
   
   private String[] getDisplayableFonts()
   {
	      GraphicsEnvironment env = 
	              GraphicsEnvironment.getLocalGraphicsEnvironment();
	      String[] allFonts = env.getAvailableFontFamilyNames();
	      
	      ArrayList<String> fontData = new ArrayList<String>();
	      Font font;
	      for (int i=0; i<allFonts.length; i++)
	      {
	    	  font = new Font(allFonts[i], Font.PLAIN, 12);
	    	  for (char c='a'; c<='z'; c++)
	    	  {
	    		  if (font.canDisplay(c))
	    		  {
	    			  fontData.add(allFonts[i]);
	    			  break;
	    				  
	    		  }
	    	  }
	      }
	      return fontData.toArray(new String[fontData.size()]);
   }

   private class FontCellRenderer 
			implements ListCellRenderer<String> 
	{
	
		private DefaultListCellRenderer renderer = null;
		private Boolean all;
		private Component component;
		
		
		public FontCellRenderer(Boolean all, Component component)
		{
			this.all = all;
			this.component = component;
		}
		
		protected DefaultListCellRenderer getRenderer()
		{ 
			if (renderer == null)
			{	
				renderer = new DefaultListCellRenderer();
			}
			return renderer;
		}
		
		public Component getListCellRendererComponent(
				JList<? extends String> list, String fontName, int index,
				boolean isSelected, boolean cellHasFocus) 
		{
			final JLabel result = (JLabel)getRenderer().getListCellRendererComponent(
			  list, fontName, index, isSelected, cellHasFocus);

			  Font font = null;
			  if (all)
					font = new Font(fontName, Font.PLAIN, 12);
			  else
					font = KeyboardFonts.getLanguageFonts().getFont(fontName);
			  result.setFont(font);
			  component.setFont(font);
			  return result;
		}
	}		// End of FontCellRenderer
   
}   // End of KeyboardFonts class
