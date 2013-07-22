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

# Purpose : Prepare projects structure, clone projects if it is not existed, switch to expected version and update source code

# import projects declaration
source "../src/config/projects.sh"

# eXoProjects directory 
EXO_PROJECTS=`pwd`

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
      echo "--------------Cloning project from url: git@github.com:exoplatform/${projects[${i}]}.git---"
	git clone --progress git@github.com:exoplatform/${projects[${i}]}.git
    if [ ${projects[${i}]} == "gatein-portal" ]; then
      echo "Only gatein-portal works on 3.5.x-PLF branch"
    else
      cd ${projects[${i}]}
      git checkout -b stable/${versions[${i}]} origin/stable/${versions[${i}]}
      cd ..
    fi
    echo "-------------------------Cloning done----------------------------------------"
  
#    cd ${projects[${i}]}
#      git checkout origin/stable/${versions[${i}]} -b crowdin-stable-${versions[${i}]}
#      echo "-------------------------Switched to origin/stable/${versions[${i}]}-------------"
#    cd ..
  else
    cd ${projects[${i}]}
    git checkout .
    git clean -f
    git fetch origin
    if [ ${projects[${i}]} == "gatein-portal" ]; then
      git pull origin 3.5.x-PLF
    else
      git pull origin stable/${versions[${i}]}
    fi
    echo "----------------------updated the existing local branch----------------"
    cd ..
  fi

  mv ${projects[${i}]} ${projects[${i}]}-${versions[${i}]}
  echo "-------------------------Renamed ${projects[${i}]} to ${projects[${i}]}-${versions[${i}]}-------------------"

  echo "+++++++++++++++++++++++++Preparing the ${projects[${i}]} project done++++++++++++++++++"
done

echo ""
echo "=========================Projects prepared===================================="


