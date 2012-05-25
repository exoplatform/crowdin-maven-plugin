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
# Purpose : Prepare projects structure, clone projects if it is not existed, create branches from previous tags and switch to these branches
#


# eXoProjects directory 
EXO_PROJECTS=`pwd`

projects=( 'platform' 'ecms' 'cs' 'ks' 'social' 'exogtn' 'webos' )
versions=( '3.5.x' '2.3.x' '2.2.x' '2.2.x' '1.2.x' '3.2.x' '2.0.x')
oldtags=( '3.5.3' '2.3.7' '2.2.9' '2.2.9' '1.2.9' '3.2.4-PLF' '2.0.4' )
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
  git branch -D crowdin/${oldtags[${i}]}
  git checkout -b crowdin/${oldtags[${i}]} ${oldtags[${i}]}
  echo "-------------------------Switched to crowdin/${oldtags[${i}]}-------------"
  cd ..
  mv ${projects[${i}]} ${projects[${i}]}-${versions[${i}]}
  echo "-------------------------Renamed ${projects[${i}]} to ${projects[${i}]}-${versions[${i}]}-------------------"

  echo "+++++++++++++++++++++++++Preparing the ${projects[${i}]} project done++++++++++++++++++"
done

echo ""
echo "=========================Projects prepared===================================="


recurse() {
  for i in "$1"/*;do
    if [ -d "$i" ];then
      recurse "$i"
    elif [ -f "$i" ]; then
      to=${i/temp\/crowdin\/translations\//}
      mv ${i} ${to}
      echo ""
      echo "Moved"
      echo "${i}"
      echo "to"
      echo "${to}"
      echo ""
    fi
 done
}

recurse $EXO_PROJECTS/temp/crowdin

$EXO_PROJECTS
echo ""
echo "=======================Committing translations to temporary branches================"
for (( i=0;i<$length;i++)); do
  cd ${projects[${i}]}-${versions[${i}]}
  echo ""
  echo "----------Updating translations from Crowdin to crowdin/${oldtags[${i}]} branch of ${projects[${i}]}------------"
  git add .
  git commit -m "Update translations from Crowdin"
  cd ..
  echo "----------Commited to crowdin/${oldtags[${i}]} branch of ${projects[${i}]}------------"
  echo ""
done
echo "=========================Finished===================================================="
echo ""
echo "Now you need merge temporary branches to current branches manually and fix conflicts!"
echo ""
