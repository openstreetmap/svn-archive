#/bin/bash

# Directory you want the .osc files moved to
changedir="/home/user/change/"
# Osmosis --rii workingDirectory
workdir="/home/user/work/"

#Get initial sequence number

OLDSEQUENCE=`grep sequenceNumber ${workdir}state.txt|cut -d= -f2`

while true; do

	./osmosis --rri workingDirectory=$workdir --wxc "temp.osc.gz"

	SEQUENCE=`grep sequenceNumber ${workdir}state.txt|cut -d= -f2`

# Get the osmosis timestamp so we can add it to the filename so trpcs knows the timestamp to write after this changeset is applied to the db
	STAMP=`grep timestamp ${workdir}state.txt|cut -d= -f2|cut -c1-4,6,7,9,10,12,13,16,17` 

# Check to see if the sequence number is the same as the last run through the loop.  
# If it is, that means there were no new replicate diffs to download.

	if [ $OLDSEQUENCE != $SEQUENCE ] ; then
		filename="${changedir}${OLDSEQUENCE}-${SEQUENCE}-${STAMP}.osc.gz"

		mv "temp.osc.gz" $filename
		
		OLDSEQUENCE=$SEQUENCE 	
	else
# If the sequence numbers match, then this .osc file contains no data, so delete it
		rm "temp.osc.gz"
	fi

	sleep 120
done
