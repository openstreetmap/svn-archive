#python osray-gen.py && gwenview scene-osray.png
#szczecin 14.55345 53.42308, 14.56641 53.4288
#nürnberg 11.07504 49.44747, 11.0829 49.45365
#helsinki 24.91568 60.17077, 24.93139 60.18023
python osray.py --bbox '24.91568 60.17077,24.93139 60.18023' -W400 -H400 -R -Q
#gwenview scene-osray.png
