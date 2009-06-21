LINE=""
while true ; do
echo -n "${LINE}"|wc -c
echo -n "${LINE}"|bzip2|od -t x1
LINE="${LINE} "
done
