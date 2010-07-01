echo running TileTabber

rm ./output/*

java -cp ./bin/ TileTabber tilexmin 32753 tilexmax 32756 tileymin 21793 tileymax 21796 tilez 16 tileurl http://tile.cloudmade.com/BC9A493B41014CAABB98F0471D759707/1/256/

echo "DONE (see output folder)"