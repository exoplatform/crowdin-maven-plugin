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
package org.exoplatform.crowdin.model;

import java.io.File;

public class CrowdinFile {

  /**
   * The File on the file system
   */
  private File file;
  /**
   * The type of the file: properties or android or strings (iOS) or ... (eXo's XML and gadgets' XML)
   */
  private String type;
  /**
   * The project + version in which this file belongs
   */
  private String project;
  /**
   * The name on the file on Crowdin
   */
  private String name;
  /**
   * Indicate this file should be cleaned or not
   */
  private boolean shouldBeCleaned;

  public CrowdinFile(File _file, String _name, String _type, String _project, boolean _shouldBeCleaned) {
    file = _file;
    name = _name;
    type = _type;
    project = _project;
    shouldBeCleaned = _shouldBeCleaned;
  }

	/*
   * Getters
	 */

  public String getType() {
    return type;
  }

  public File getFile() {
    return file;
  }

  public String getProject() {
    return project;
  }

  public String getCrowdinPath() {
    return project + name;
  }

  public String getName() {
    return name;
  }

  public boolean isShouldBeCleaned() {
    return shouldBeCleaned;
  }

  public enum Type {
    PORTLET, GADGET
  }

}
