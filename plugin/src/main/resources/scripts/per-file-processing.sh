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
