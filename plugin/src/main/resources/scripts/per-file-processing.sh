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

FILENAME=$1
echo "Processing $FILENAME..."

# Restoring replaced special chracters
sed -i -e 's/__COLON__/:/g' $FILENAME

# Restoring escaped characters (:#!=)
sed -i -e 's|\\\([:#!=/,"]\)|\1|g' $FILENAME

# Convert unicode characters to native
#native2ascii -encoding UTF8 -reverse $FILENAME $FILENAME

# Remove standalone="no" in xml files
#sed -i -e 's|" standalone="no"?>|"?>|g' $FILENAME

# Remove blank lines
#sed -i -e '/^$/d' $FILENAME

# Remove \t after at the end of text
sed -i -e 's/\(.*\w\|\s\)\(\t\|\\t\)/\1/g' $FILENAME

#Remove blank before and after =
sed -i -e 's/ *= */=/g' $FILENAME
#echo "Done!"
