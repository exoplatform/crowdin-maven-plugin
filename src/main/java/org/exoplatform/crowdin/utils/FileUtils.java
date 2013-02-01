package org.exoplatform.crowdin.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class FileUtils {
  /**
   * Replace all String "regex" by String "replacement" in file "filePath"
   * @param filePath
   * @param regex
   * @param replacement
   * @return
   */
  public static boolean replaceStringInFile(String filePath,String regex, String replacement)
  {

    try
    {
      File file = new File(filePath);
      BufferedReader reader = new BufferedReader(new FileReader(file));
      String line = "";
      StringBuffer oldtext = new StringBuffer("");
      while((line = reader.readLine()) != null)
      {
        oldtext.append(line + "\r\n");
      }
      reader.close();
      // replace a word in a file
      String newtext = oldtext.toString().replaceAll(regex, replacement);
      FileWriter writer = new FileWriter(filePath);
      writer.write(newtext);
      writer.close();
    }
    catch (IOException ioe)
    {
      ioe.printStackTrace();
      return false;
    }
    return true;
  }
  
  /**
   * Replace special characters of "filePath" by others
   * @param filePath
   * @return
   */
  public static boolean fileContentProcessing(String filePath)
  {
    
    try
    {
      File file = new File(filePath);
      BufferedReader reader = new BufferedReader(new FileReader(file));
      String line = "";
      StringBuffer oldtext = new StringBuffer("");
      while((line = reader.readLine()) != null)
      {
        oldtext.append(line + "\r\n");
      }
      reader.close();
      // replace a word in a file
      
      //Restoring replaced special characters
      String newtext = oldtext.toString().replaceAll("__COLON__",":");
      
      //Restoring escaped characters (:#!=)
      newtext=newtext.replaceAll("\\:",":");
      newtext=newtext.replaceAll("\\#","#");
      newtext=newtext.replaceAll("\\!","!");
      newtext=newtext.replaceAll("\\=","=");

      //Remove \t after at the end of text
      newtext=newtext.replaceAll("\t","");
      
      //Remove blank before and after =
      newtext=newtext.replaceAll(" =","=");
      newtext=newtext.replaceAll("= ","=");

      
      FileWriter writer = new FileWriter(filePath);
      writer.write(newtext);
      writer.close();
    }
    catch (IOException ioe)
    {
      ioe.printStackTrace();
      return false;
    }
    return true;
  }
}
