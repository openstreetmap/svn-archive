#!/bin/bash
# Healthwhere, a web service to find local pharmacies and hospitals
# Copyright (C) 2009-2010 Russell Phillips (russ@phillipsuk.org)

cd $(dirname $0)

# Remove/rename existing files
rm -f allpostcodes.csv healthware.db.gz
gzip healthware.db
# Get postcodes into a single csv file
unzip -p codepo_gb.zip data/CSV/* > allpostcodes.csv
# Run makedb.php to create DB
./makedb.php
rm -f allpostcodes.csv
