#!/bin/bash
#
# Copyright (C) 2003-2012 eXo Platform SAS.
#
# This program is free software; you can redistribute it and/or
# modify it under the terms of the GNU Affero General Public License
# as published by the Free Software Foundation; either version 3
# of the License, or (at your option) any later version.
#
# This program is distributed in the hope that it will be useful,
# but WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
# GNU General Public License for more details.
#
# You should have received a copy of the GNU General Public License
# along with this program; if not, see<http://www.gnu.org/licenses/>.#
#
#
# Purpose : Prepare projects structure, clone projects if it is not existed, switch to expected version and update source code
#

# import projects declaration
source "../src/config/projects.sh"


# eXoProjects directory 
EXO_PROJECTS=`pwd`

length=${#projects[@]}

echo "=========================Reset local branch========================="
echo ""

# Reset the projects in local repository 
for (( i=0;i<$length;i++)); do
    cd ${projects[${i}]}
    git checkout .
    git clean -f
    git fetch origin
    if [ ${projects[${i}]} == "gatein-portal" ]; then
      git pull origin 3.5.x-PLF
    else
      git pull origin stable/${versions[${i}]}
    fi
    echo "----------------------reset the existing local branch----------------"
    cd ..
done
echo "=========================Finish reseting local branch========================="
