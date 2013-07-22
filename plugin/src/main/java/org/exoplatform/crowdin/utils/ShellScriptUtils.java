/*
 * Copyright (C) 2003-2013 eXo Platform SAS.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 3 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.exoplatform.crowdin.utils;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * 
 * This Util help execute .sh files that store in .jar file. These .sh files is resource of .jar file
 * 
 */
public class ShellScriptUtils {
  
  /**
   * Copy file from .jar resource to system file
   * @param resname
   * @return
   * @throws IOException
   */
  public static String makeFileFromResource(String resname) throws IOException {
    InputStream is = ShellScriptUtils.class.getClassLoader().getResourceAsStream(resname);
 
    // If no source stream is able to be located
    if (is == null) {
      throw new IOException("Unable to load resource:" + resname);
    }
 
    File tmpfile = File.createTempFile("xec", ".sh");
 
    String tmpfilename=tmpfile.getAbsolutePath();
 
    BufferedInputStream bis = new BufferedInputStream(is, 1024);
    FileOutputStream os = new FileOutputStream(tmpfile);
    BufferedOutputStream bos = new BufferedOutputStream(os, 1024);
    byte buffer[] = new byte[1024];
    while (true) {
      int n = bis.read(buffer);
      if (n <= 0) {
        break;
      }
      bos.write(buffer, 0, n);
    }
    bos.close();
    bis.close();
    return tmpfilename;
  }
 
  /**
   * Execute Shell script without Argument
   * @param pathname
   * @return
   * @throws IOException
   */
  public static int execShellscript(String pathname) throws IOException {
    String tmpfilename = makeFileFromResource(pathname);
    String command;
 
    command = "sh " + tmpfilename;
 
    Process p=Runtime.getRuntime().exec(command);
    int r=-1;
    try {
      r = p.waitFor();
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
    new File(tmpfilename).delete();
    return r;
  }
 
/**
 * Execute Shell script with Argument
 * @param pathname
 * @param fileArgument
 * @return
 * @throws IOException
 */
  public static int execShellscript(String pathname, String fileArgument) throws IOException {
    String tmpfilename = makeFileFromResource(pathname);
    
    String command;
 
    command = "chmod 600 " + fileArgument;
 
    Process p=Runtime.getRuntime().exec(command);
    try {
      p.waitFor();
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
    command = "sh " + tmpfilename + " " + fileArgument;
    System.out.println ("\n\n\n\n" + command + "\n\n\n\n");

    p=Runtime.getRuntime().exec(command);
    int r=-1;
    try {
      r = p.waitFor();
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
    new File(tmpfilename).delete();
    return r;
  }
}
