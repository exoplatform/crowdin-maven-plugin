FILENAME=$1

# Replace : by __COLON
sed -i -e 's/:/__COLON__/g' $FILENAME

