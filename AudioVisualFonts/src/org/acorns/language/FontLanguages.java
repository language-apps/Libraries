/*
 *   FontLanugages.java
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
package org.acorns.language;

import java.awt.Font;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;

/** This class maintains a list of files in a spaceified folder */
public class FontLanguages
{   
	private ArrayList<String> families;
    private ArrayList<Font>   fonts;
    private ArrayList<URL>    urls;

    /** Constructor to ascertain the list of embedded ACORNS fonts
     *
     * @param url URL of the embedded font folder or file with font names
     */
    public FontLanguages(URL url)
    {   
    	ArrayList<String> strs = new ArrayList<String>();
        String line;
        try
        {
            URLConnection con = url.openConnection();
            con.connect();
            BufferedReader in = new java.io.BufferedReader
                        (new java.io.InputStreamReader(con.getInputStream()));
            for (; (line = in.readLine()) != null; )
            {   if (line.toLowerCase().endsWith(".ttf"))
                {  strs.add(line);  }
            }
            in.close();
        }
        catch (Exception e) {}

        families = new ArrayList<String>();
        urls     = new ArrayList<URL>();
        fonts    = new ArrayList<Font>();
        
        String family;
        URL newURL;
        Font font;
        for (int i=0; i<strs.size(); i++)
        {   try
            { 
        	    newURL = new URI(url + strs.get(i)).toURL();
                font = readFont(newURL);
                if (font==null) continue;
                family = font.getName();
                if (families.indexOf(family)>=0) continue;

                fonts.add(font);
                families.add(family);
                urls.add(newURL);
            }
            catch (Exception e) {}
        }
    }

    /** Method to create an embedded font by reading it from a URL
     *
     * @param url The URL object
     * @return A font object or null if fails
     */
    public final Font readFont(URL url)
    {   InputStream stream = null;
        Font font = null;

        try
        {  stream = url.openStream();
           ByteArrayOutputStream  out = new ByteArrayOutputStream();
           byte[] aoBuffer = new byte[512];
           int nBytesRead;

           while ((nBytesRead = stream.read(aoBuffer)) > 0)
           {  out.write(aoBuffer, 0, nBytesRead);  }
           byte[] byteArray = out.toByteArray();

           ByteArrayInputStream input = new ByteArrayInputStream(byteArray);
           font = Font.createFont(Font.TRUETYPE_FONT, input);
        }
        catch (Exception e) { return null; }
        try { stream.close(); } catch (Exception e) {}
        return font;
    }

    /** Method to find the embedded font of a selected family
     *  @return Font object or none if no embedded font exzists
     */
    public Font getFont(String family)
    {   int index = families.indexOf(family);
        if (index<0) return null;
        return fonts.get(index);
    }

    /** Copy all of the embedded fonts to a destination directory
     *
     * @param dest The destination directory
     * @throws IOException
     */
    public void copyFonts(String dest) throws IOException
    {  if (urls==null) return;

       String separator = System.getProperty("file.separator");
       String base = dest + separator + "fonts";
       File file = new File(base);
       URL url;
       String name;
       writeFontNames(file);
       for (int i=0; i<urls.size(); i++)
       {   url = urls.get(i);
           name = url.getPath();
           name = name.substring(name.lastIndexOf("/")+1);
           file = new File(base + name);
           copyFont(urls.get(i), file);
       }
    }

    /** Method to write a file of embedded font names
     *  @param dest destination location
     */
    private void writeFontNames(File dest) throws IOException
    {   FileOutputStream output = new FileOutputStream(dest);
        BufferedWriter out = new BufferedWriter(new OutputStreamWriter(output));

        String name;
        URL url;
        for (int i=0; i<urls.size(); i++)
        {  url = urls.get(i);
           name = url.getPath();
           name = name.substring(name.lastIndexOf("/")+1);
           out.write(name);
           out.newLine();
        }
        out.close();
    }

    /** Method to make a copy of an embedded font
     *
     * @param source Fonts source location
     * @param file destination location
     * @throws IOException
     */
    public void copyFont(URL source, File file) throws IOException
    {   
    	InputStream input = source.openStream();
        OutputStream output = new FileOutputStream(file);

        byte[] buf = new byte[1024];
        int len;
        while ((len=input.read(buf)) > 0)  { output.write(buf, 0, len); }
        input.close();
        output.close();
    }
}       // End of FontLanguages class
