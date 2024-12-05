/**
 * LanguageCodes.java
 * @author HarveyD
 * @version 4.00 Beta
 *
 * Copyright 2007-2015, all rights reserved
 */

package org.acorns.language;

import javax.swing.*;
import java.io.*;
import java.net.*;
import java.util.*;

public class LanguageCodes 
{
   // The IPA 639-3 language code list
   static String[] codes;
   static JComboBox<String> combo;
   
   public static JComboBox<String> getJCombo()
   {
       Vector<String>codeVector = new Vector<String>();
       codeVector.add(LanguageText.getMessage("dictionary", 1));
       
       if (combo!=null) return combo;
       
       String fileName = "/data/isoData.txt";
       try
       {
           URL url = LanguageCodes.class.getResource(fileName);
           BufferedReader read
                  = new BufferedReader(new InputStreamReader(url.openStream()));
           
			        String line = read.readLine();
           while (line !=null)  
           { 
               if (line.length()>30) line = line.substring(0,30).trim();
               codeVector.add(line); 
               line = read.readLine(); 
           }
           read.close();
           codes = (String[])codeVector.toArray(new String[codeVector.size()]);
       }
       catch (Exception e) { return null; }
       
       combo = new JComboBox<String>(codes);
       return combo;
   }
   
}   // End of LanguageCodes class
