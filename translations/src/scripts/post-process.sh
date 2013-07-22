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
# Purpose : Restore projects structure
#

# import projects declaration
source "../src/config/projects.sh"

# eXoProjects directory 
EXO_PROJECTS=`pwd`

length=${#projects[@]}

echo "=========================Restoring projects structure========================="
echo ""

for (( i=0;i<$length;i++)); do
  if [ -d $EXO_PROJECTS/${projects[${i}]}-${versions[${i}]} ]; then
    mv ${projects[${i}]}-${versions[${i}]} ${projects[${i}]}
    rm -f ${projects[${i}]}/temp.patch
    echo "Renamed ${projects[${i}]}-${versions[${i}]} to ${projects[${i}]}"
  fi
done

echo "=========================Pushing to github========================="
echo ""

## arg1: PLF-XXXX
## arg2: en,fr (list of languages)
## arg3: W29
## Commit message "PLF-XXXX: inject en,fr translation W29"
##MESSAGE_COMMIT="$arg1: inject $arg2 translation $arg3"
##echo " This is message commit: $MESSAGE_COMMIT "

##for (( i=0;i<$length;i++)); do
#  if [ -d $EXO_PROJECTS/${projects[${i}]}]; then    
#	echo "For ${projects[${i}]}"
#	git checkout stable/${versions[${i}]}
#	git remote rm exodev
#	git remote add exodev git@github.com:exodev/${projects[${i}]}.git       
#	git fetch exodev

	##for each project 
#		if [ -n "$(git status --porcelain)" ]; then 
#		echo "there are changes"; 
		
#			if [ $arg2 =="en,fr"]; then
				##if en,fr is used
					## Commit message "PLF-XXXX: inject en,fr translation W29"
#					git add . ; git commit -m $MESSAGE_COMMIT
#					git checkout -b fix/${versions[${i}]}/PLF-XXXX
#					git push exodev fix/${versions[${i}]}/PLF-XXXX
#					echo "Push to exodev"
#					git checkout stable/${versions[${i}]}
#			else	
				##if other language
#					git branch -D feature/${versions[${i}]}-translation
					## Commit message "PLF-XXXX: inject en,fr translation W29"
#					git add . ; git commit -m $MESSAGE_COMMIT				
#					git checkout -b feature/${versions[${i}]}-translation remotes/exodev/feature/${versions[${i}]}-translation
#					git cherry-pick HEAD@{1}
#					echo "Push to feature/${versions[${i}]}-translation"
#					git push exodev feature/${versions[${i}]}-translation
#					git checkout stable/${versions[${i}]}					
#			fi							
#		else 
#		  echo "no changes, no commit ";
#		fi
#  fi
#done


echo ""
echo "=========================Finished=============================================="
