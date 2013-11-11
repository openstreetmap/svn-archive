"C:\Program Files (x86)\GnuWin32\bin\xgettext" --files-from=files.txt --from-code=UTF-8 -k -ktrc:1c,2 -kmarktrc:1c,2 -ktr -kmarktr -ktrn:1,2 -ktrnc:1c,2,3
pause
"C:\Program Files (x86)\GnuWin32\bin\msgmerge" "messages_old.po" "messages.po"
pause