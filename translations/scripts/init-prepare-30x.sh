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


# eXoProjects directory 
EXO_PROJECTS=`pwd`

projects=( 'platform' 'ecms' 'cs' 'ks' 'social' 'exogtn' )
versions=( '3.0.x' '2.1.x' '2.1.x' '2.1.x' '1.1.x' '3.1.x' )
length=${#projects[@]}

echo "=========================Preparing projects structure========================="
echo ""

for (( i=0;i<$length;i++)); do
  if [ -d $EXO_PROJECTS/${projects[${i}]}-${versions[${i}]} ]; then
    mv ${projects[${i}]}-${versions[${i}]} ${projects[${i}]}
    echo "Renamed ${projects[${i}]}-${versions[${i}]} to ${projects[${i}]}"
  fi
done

# Prepare the projects 
for (( i=0;i<$length;i++)); do
  echo ""
  echo "+++++++++++++++++++++++++Preparing the ${projects[${i}]} project+++++++++++++++++++++++"
  
  if [ ! -d $EXO_PROJECTS/${projects[${i}]} ]; then
    echo "--------------Cloning project from url: git@github.com:exodev/${projects[${i}]}.git---"
    git clone git@github.com:exodev/${projects[${i}]}.git
    echo "-------------------------Cloning done----------------------------------------"
  fi
  
  cd ${projects[${i}]}
  echo "-------------------------Fetching Blessed Repository--------------------------"
  git remote add blessed git@github.com:exoplatform/${projects[${i}]}.git
  git fetch blessed
  echo "-------------------------Fetching done----------------------------------------"
  if [ "${projects[${i}]}" != "webos" ]; then
    git checkout remotes/blessed/stable/${versions[${i}]}
    echo "-------------------------Switched to remotes/blessed/stable/${versions[${i}]}-------------"
  else
    git checkout remotes/blessed/master
    echo "-------------------------Switched to remotes/blessed/master-------------"
  fi
  cd ..
  mv ${projects[${i}]} ${projects[${i}]}-${versions[${i}]}
  echo "-------------------------Renamed ${projects[${i}]} to ${projects[${i}]}-${versions[${i}]}-------------------"

  echo "+++++++++++++++++++++++++Preparing the ${projects[${i}]} project done++++++++++++++++++"
done

echo ""
echo "=========================Projects prepared===================================="
