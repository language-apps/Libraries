/**
 * HelpSets.java
 * @author HarveyD
 * @version 6.00 Beta
 *
 * Copyright 2009-2015, all rights reserved
 */

import javax.help.*;
import java.net.*;
import java.io.*;
import java.util.*;

/** Class to merge multiple help sets to the master set */
public class HelpSets 
{
    private HashMap<String, Integer> messageIndex;
    private ArrayList<String>[] messages;

    /** Constructor to initialize Master help set objects */
    public HelpSets(HelpSet helpSet)
    {
    	this(helpSet, null, true);
        try
        {  URL helpFolderURL = HelpSets.class.getResource("helpSetFiles");
           if (helpFolderURL==null) return;

           ArrayList<String>categoryList = readTextFile(helpFolderURL, null, true);
           readMessages(categoryList);
           mergeHelpSets(categoryList, helpSet);

        }
        catch (Exception e)   
        { System.out.println(e); }
   }    // End of Constructor
    
    /** Constructor to merge helps sets
     * 
     * @param helpSet The java help class
     * @param fileNames List of files to include or exclude
     * @param include true if to include, exclude otherwise
     */
    public HelpSets(HelpSet helpSet, String[] fileNames, Boolean include)
    {
        try
        {  URL helpFolderURL = HelpSets.class.getResource("helpSetFiles");
           if (helpFolderURL==null) return;

           ArrayList<String>categoryList = readTextFile(helpFolderURL, null, true);
           readMessages(categoryList);

           if (fileNames == null)
           {
        	   mergeHelpSets(categoryList, helpSet);
           }
           else
           {
               categoryList = readTextFile(helpFolderURL, new ArrayList<String>(Arrays.asList(fileNames)), include);
               mergeHelpSets(categoryList, helpSet);
           }
        }
        catch (Exception e)   
        { System.out.println(e); }
   }    // End of Constructor

   /** Method to combine category help sets
    *  @param names  ArrayList of category names
    *  @param helpSet System master help set
    */
   private void mergeHelpSets(ArrayList<String> names, HelpSet helpSet)
                                   throws HelpSetException
   {
      if (helpSet==null) return;

      URL mergeURL;
      HelpSet mergeSet;
      String helpName;
      ClassLoader loader = HelpSets.class.getClassLoader();
      for (int i=0; i<names.size(); i++)
      {    helpName = names.get(i) + "/helpData/acorns.hs";
           mergeURL = HelpSets.class.getResource(helpName);
           try
           {   
               mergeSet = new HelpSet(loader, mergeURL);
               helpSet.add(mergeSet);
           }
           catch(Exception e) 
           {
               System.out.println(e);
           }
      }
   }    // End of mergeHelpSets()

    /** Method to read a set of text messages
    *  @param names  ArrayList of category names
    */
   @SuppressWarnings("unchecked")  
   private void readMessages(ArrayList<String> names)
   {
       String name, messageName;
       URL url;

       messageIndex = new HashMap<String, Integer>();
       messages = new ArrayList[names.size()];
       for (int i=0; i<names.size(); i++)
       {   
    	   name = names.get(i);
           messageIndex.put(name, i);

           messageName = name + "/helpData/messages.txt";
           url  =  HelpSets.class.getResource(messageName);
           messages[i] = readTextFile(url, null, true);
     }
   }       // End of readMessages()
   
   /** Method to read the list of lines from a text file
     *
     * @param source The name of the text file containing the file list
     * @param list of help set names to include/exclude or null to include all
     * @param include true to include help set names, false to exclude
     * @return The list of folders containing help data
     */
   private ArrayList<String> readTextFile(URL source, ArrayList<String> includes, boolean include)
   {
       ArrayList<String> helpList = new ArrayList<String>();
       try
       {
          URLConnection connection = source.openConnection();
          InputStream stream = connection.getInputStream();
          BufferedReader in = new BufferedReader(new InputStreamReader(stream));
          String line;
          boolean contains;

          while ( (line = in.readLine()) != null)
          {
             if (line.length()>0 && !line.startsWith("//"))
             {
            	 if (includes == null)
            		 helpList.add(line);
            	 
            	 else
            	 {
            		 contains = includes.contains(line);
            		 if (contains == include)
            			 helpList.add(line);
            	 }
             }
          }
          in.close();
       }
       catch (Exception e) {}
       return helpList;
   }   // End of readHelpFolderNames()

   public HashMap<String, Integer> getMessageIndex() { return messageIndex; }
   public ArrayList<String>[] getMessages() { return messages; }

}  // End of HelpSets class
