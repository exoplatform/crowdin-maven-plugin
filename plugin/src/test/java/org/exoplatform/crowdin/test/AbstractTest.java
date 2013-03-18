package org.exoplatform.crowdin.test;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.Reader;

public abstract class AbstractTest {

  public static String getFileContent(File file) throws Exception {
    Reader reader = new InputStreamReader(new FileInputStream(file), "UTF-8");
    char[] characters = new char[(int) file.length()];
    reader.read(characters);
    return new String(characters);
  }

  public static boolean isSameContent(File file1, File file2) throws Exception {
    String content1 = getFileContent(file1);
    String content2 = getFileContent(file2);
    return content1.equals(content2);
  }

}
