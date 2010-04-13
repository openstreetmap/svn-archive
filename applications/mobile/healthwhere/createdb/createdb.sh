#!/bin/bash
cd $(dirname $0)

# Delete existing files
rm -f allpostcodes.csv healthware.db
# Get postcodes into a single csv file
unzip -p codepo_gb.zip data/CSV/* > allpostcodes.csv
# Run makedb.php to create DB
./makedb.php
