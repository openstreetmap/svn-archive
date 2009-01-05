Generator velkych map z podkladov na freemap slovakia
=====================================================

usage:

./mapGen.pl X Y Z_FROM Z_TO  W H R G B


X Y Z_FROM - suradnice laveho horneho rohu tile na danom ZOOM
Z_TO - zoom level ktory sa ma stiahnut
W H - sirka vyska v pocete tiles

ak sa uvedie R G B je to v desiatkovej forme uvedena farba podkladu, ikan transparentna

Priklad:

./mapGen.pl 2240 1420 12 16 4 2 230 230 230

ztiahne z fremapu 4x2 tiles od 2240 1420 po 2243 1421 ... z12 az z16 ... a ulozi do adresarov...
tiles budu mat seby podklad RGB 230 230 230 ...

obsah adresara data ... potom pretlacite do PDAcka co adresara tiles OSMtracker-a ... a mozete sa vytesovat...

my $DownLoadURL= 'http://192.168.0.63/freemap';  #urcuje odkial sa budu stahovat tiles
my $Layers = 'tiles,names';   # ktore layes sa budu spajat (ak su transaprentne ako names sa pekne prelozia a spoja dokopy ako na freemap-e)

