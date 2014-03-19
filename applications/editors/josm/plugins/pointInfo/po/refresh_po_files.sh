ls -1 ../src/org/openstreetmap/josm/plugins/pointinfo/*.java >files_list
xgettext --files-from=files_list -d pointInfo --from-code=UTF-8 -k -ktrc:1c,2 -kmarktrc:1c,2 -ktr -kmarktr -ktrn:1,2 -ktrnc:1c,2,3

msgmerge -U cs.po pointInfo.po
