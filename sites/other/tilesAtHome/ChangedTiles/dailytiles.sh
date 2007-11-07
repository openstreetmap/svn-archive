#!/bin/bash

. ~/.changedtiles_rc

if [[ -z "$ChangedTiles_Dir" ]]; then
  ChangedTiles_Dir="~/public_html/ChangedTiles/"
fi

cd "$ChangedTiles_Dir"

php index.php
