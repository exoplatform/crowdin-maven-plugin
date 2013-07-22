#!/bin/bash
#
# Copyright (C) 2003-2013 eXo Platform SAS.
#
# This is free software; you can redistribute it and/or modify it
# under the terms of the GNU Lesser General Public License as
# published by the Free Software Foundation; either version 3 of
# the License, or (at your option) any later version.
#
# This software is distributed in the hope that it will be useful,
# but WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
# Lesser General Public License for more details.
#
# You should have received a copy of the GNU Lesser General Public
# License along with this software; if not, write to the Free
# Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
# 02110-1301 USA, or see the FSF site: http://www.fsf.org.
#

# Purpose : global post-processing for all projects after running update

# import projects declaration
source "../src/config/projects.sh"

# eXoProjects directory 
EXO_PROJECTS=`pwd`

length=${#projects[@]}

for (( i=0;i<$length;i++)); do
  cd ${projects[${i}]}-${versions[${i}]}
  git diff -w > temp.patch && git checkout . && git apply --whitespace=fix temp.patch && rm -f temp.patch
  cd ..
done
