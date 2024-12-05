/**
 * LanguageText.java
 * @author HarveyD
 * @version 5.00 Beta
 *
 * Copyright 2009-2015, all rights reserved
 */

package org.acorns.language;

import javax.help.*;
import java.lang.reflect.*;
import java.util.*;

/** Access text from webHelpSets.jar and helpSets.jar
 */
public class LanguageText 
{
    private final static String notFound = "???????";

    private static HashMap<String, Integer> messageIndex;
    private static ArrayList<String>[] messages;
    private static HelpSet helpSet;

    /** Initialize the ACORNS help facilities
     *
     * @param helpSet The helpSet framework object
     */
    public LanguageText(HelpSet helpSet)
    {
    	this(helpSet, null, true);
    }    // End of Constructor

    /** Initialize the ACORNS help facilities
    *
    * @param helpSet The helpSet framework object
    * @param names of help set objects to include or exclude
    * @param include true if to include help set items, false to exclude
    */
   @SuppressWarnings("unchecked")
   public LanguageText(HelpSet helpSet, String[] includes, boolean include)
   {
      LanguageText.helpSet = helpSet;
      Class<?>[] helpParams;
      try
      {
         Class<?> HelpSets = Class.forName("HelpSets");
         Constructor<?> helpConstructor;
         
         Object helpObject;
         if (includes==null)
         {
             helpParams = new Class[] { HelpSet.class };
        	 helpConstructor = HelpSets.getDeclaredConstructor(helpParams);
             helpObject = helpConstructor.newInstance(new Object[] { helpSet } );
         }
         else
         {
             helpParams = new Class[] { HelpSet.class, String[].class, Boolean.class };
       	     helpConstructor = HelpSets.getDeclaredConstructor(helpParams);
             helpObject = helpConstructor.newInstance(new Object[] { helpSet, includes, include });
         }

         Method getMessageIndex, getMessages;
         Class<?> helpClass = helpObject.getClass();
         try
         {
            getMessageIndex = helpClass.getMethod("getMessageIndex");
            messageIndex = (HashMap<String, Integer>)
                            getMessageIndex.invoke(helpObject, new Object[0]);

            getMessages = helpClass.getMethod("getMessages");
            messages = (ArrayList<String>[])
                            getMessages.invoke(helpObject, new Object[0]);
         }
         catch (Exception e)  
         { messageIndex = null; messages = null; }
      } catch (Exception e) {  }
   }    // End of Constructor
   
   /** Method to get a message from this object's category (Class name)
     * @param object The object in question
     * @param msgNo The message number
     * @return Message
     */
    public static String getMessage(Object object, int msgNo)
    {
        String name = object.getClass().getSimpleName();
        return getMessage(name, msgNo);
    }

    /** Method to get a message from the messages.txt file
     * @param category message type
     * @param msgNo The message number
     * @return Message
     */
    public static String getMessage(String category, int msgNo)
    {
         if (messageIndex==null || messages==null || msgNo<=0) return notFound;

         msgNo--;
         Integer data = messageIndex.get(category);
         if (data==null) return notFound;

         int catIndex = data.intValue();
         if (messages[catIndex].size() <= msgNo) return notFound;
         return messages[catIndex].get(msgNo);
    }    // End of getMessage()

   /** Get a message with parameters to replace for the object's category
    *
    * @param object The object in question
    * @param msgNo The index to the message number
    * @param params A list of String parameters to replace
    * @return Message with parameters replaced
    */
    public static String getMessage(Object object, int msgNo, String... params)
    {
       String name = object.getClass().getSimpleName();
       return getMessage(name, msgNo, params);
    }

   /** Get a message with parameters to replace. Messages
    *  containing the characters XXXXX are replaced by parameter values
    *
    * @param category message type
    * @param msgNo The index to the message number
    * @param params A list of String parameters to replace
    * @return Message with parameters replaced
    */
    public static String getMessage(String category,int msgNo,String... params)
    {
       String message = getMessage(category, msgNo);
       int offset;
       for ( String s : params )
       {  offset = message.indexOf("xxxxx");
          if (offset<0) break;
          if (offset==0) message = s + message.substring(5);
          else           message = message.substring(0,offset) 
                                       + s + message.substring(offset+5);
       }
       return message;
    }

    /** Method to get a message list for this object's category. Messages
     *  are split on the semi-colon value and an array returned.
     *
     * @param object The object in question
     * @param msgNo The message number
     * @return A list of messages
     */
    public static String[] getMessageList(Object object, int msgNo)
    {
        String name = object.getClass().getSimpleName();
        return getMessageList(name, msgNo);
    }

  /** Method to return an array of messages. Messages
    *  are split on the semi-colon value and an array returned.
    *
    * @param category message type
    * @param msgNo message number
    * @return array of text messages
    */
    public static String[] getMessageList(String category, int msgNo)
    {
       String message = getMessage(category, msgNo);
       return message.split(";");
    }

    /** Method to return the system help object
     *
     * @return The helpSet object or null if none
     */
    public static HelpSet getHelpSet() { return helpSet; }

}        // End of LanguageText class
