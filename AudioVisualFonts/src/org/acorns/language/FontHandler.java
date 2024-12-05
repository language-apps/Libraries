package org.acorns.language;

import java.io.File;
import java.lang.reflect.Method;
import java.awt.Font;

import org.acorns.language.keyboards.data.Status;

public class FontHandler 
{
	private static FontHandler handler;

	public static FontHandler getHandler()
	{
		if (handler==null) handler = new FontHandler();
		return handler;
	}
	
	public FontHandler() {}
	
	/** Get the path to the file containing the font .ttf file
	 * 
	 * @param font Desired font
	 * @return Path to font or null if not found
	 */
	private File getPath(Font font)
	{ 
	    File fontDirectory = Status.getDefaultPath(true);
	    String name = font.getName();
	    name = name.replaceAll("\\s", "") + ".ttf";
	    File filePath = new File(fontDirectory, name);
	    if (filePath.exists()) return filePath;
	    return null;
	}

    /** Get the path to the font file
     * 
     * @param font Desired font
     * @return path to specified font
     */
    public File getFontPath(Font font)
    {
    	String font2D;

    	File fontPath = getPath(font);
    	if (fontPath != null)
    		return fontPath;
		try
		{
			Method method = font.getClass().getDeclaredMethod("getFont2D");
			method.setAccessible(true);
			font2D = method.invoke(font).toString();
		}
		catch (Exception e)
		{
			return null;
		}
		
		if (font2D==null) 
		{
			return null;
		} 
		
		if (font2D.contains(" fileName=")) 
		{
			font2D = font2D.substring(font2D.indexOf(" fileName=")+10).replace("\r\n", "\n")+"\n";
			font2D = font2D.substring(0, font2D.indexOf("\n"));
		}

		File source = new File(font2D);
		if (!source.isFile()) 
		{
			return null;
		} 
		return source;
    }
}
