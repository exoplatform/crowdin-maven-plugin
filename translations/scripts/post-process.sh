#!/bin/sh
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


# eXoProjects directory 
EXO_PROJECTS=`pwd`/

echo "=========================Restoring projects structure========================="

mv platform-3.5.x platform
echo "-------------------------Renamed platform-3.5.x to platform-------------------"
mv ecms-2.3.x ecms
echo "-------------------------Renamed ecms-2.3.x to ecms---------------------------"
mv cs-2.2.x cs
echo "-------------------------Renamed cs-2.2.x to cs-------------------------------"
mv ks-2.2.x ks
echo "-------------------------Renamed ks-2.2.x to ks-------------------------------"
mv social-1.2.x social
echo "-------------------------Renamed social-1.2.x to social-----------------------"

echo "=========================Finished=============================================="