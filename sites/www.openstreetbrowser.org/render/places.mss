.places_db[place=continent][zoom>=2][zoom<4] name,
.places_db[place=city_large][zoom>=10][zoom<15] name,
.places_db[place=city_medium][zoom>=10][zoom<15] name,
.places_db[place=city][zoom>=10][zoom<15] name,
.places_db[place=town_large][zoom>=13][zoom<15] name,
.places_db[place=town][zoom>=13][zoom<15] name,
.places_db[place=suburb][zoom>=15] name,
.places_db[place=village][zoom>=15] name
{
  text-size: 12;
  text-placement: point;
  text-face-name: "DejaVu Sans Book";
  text-fill: #000000;
  text-avoid-edges: true;
  text-halo-radius: 2;
  point-allow-overlap: false;
}
.places_db[place=continent][zoom>=2][zoom<4] name_en,
.places_db[place=city_large][zoom>=10][zoom<15] name_en,
.places_db[place=city_medium][zoom>=10][zoom<15] name_en,
.places_db[place=city][zoom>=10][zoom<15] name_en,
.places_db[place=town_large][zoom>=13][zoom<15] name_en,
.places_db[place=town][zoom>=13][zoom<15] name_en,
.places_db[place=suburb][zoom>=15] name_en,
.places_db[place=village][zoom>=15] name_en
{
  text-size: 10;
  text-placement: point;
  text-face-name: "DejaVu Sans Book";
  text-fill: #000000;
  text-avoid-edges: true;
  text-halo-radius: 2;
  point-allow-overlap: false;
  text-dy: 14;
}
.places_db[place=continent][zoom=1] name,
.places_db[place=suburb][zoom>=13][zoom<15] name,
.places_db[place=hamlet][zoom>=15] name
{
  text-size: 10;
  text-placement: point;
  text-face-name: "DejaVu Sans Book";
  text-fill: #000000;
  text-avoid-edges: true;
  text-halo-radius: 1;
  point-allow-overlap: false;
}
.places_db[place=continent][zoom=1] name_en,
.places_db[place=suburb][zoom>=13][zoom<15] name_en,
.places_db[place=hamlet][zoom>=15] name_en
{
  text-size: 8;
  text-placement: point;
  text-face-name: "DejaVu Sans Book";
  text-fill: #000000;
  text-avoid-edges: true;
  text-halo-radius: 1;
  point-allow-overlap: false;
  text-dy: 10;
}
.places_db[place=suburb][zoom>=11][zoom<13] name,
.places_db[place=locality][zoom>=15] name
{
  text-size: 8;
  text-placement: point;
  text-face-name: "DejaVu Sans Book";
  text-fill: #222222;
  text-avoid-edges: true;
  text-halo-radius: 1;
  point-allow-overlap: false;
}
.places_db[place=hamlet][zoom>=13][zoom<15] name_en,
.places_db[place=locality][zoom>=15] name_en
{
  text-size: 6;
  text-placement: point;
  text-face-name: "DejaVu Sans Book";
  text-fill: #222222;
  text-avoid-edges: true;
  text-halo-radius: 1;
  point-allow-overlap: false;
  text-dy: 8;
}
.places_db[place=country][zoom>=7][zoom<10] name,
.places_db[place=state][zoom>=9][zoom<12] name,
.places_db[place=region][zoom>=12][zoom<15] name
{
  text-size: 12;
  text-placement: point;
  text-face-name: "DejaVu Sans Bold";
  text-fill: #000000;
  text-avoid-edges: true;
  text-halo-radius: 2;
  point-allow-overlap: false;
}
.places_db[place=country][zoom>=7][zoom<10] name_en,
.places_db[place=state][zoom>=9][zoom<12] name_en,
.places_db[place=region][zoom>=12][zoom<15] name_en
{
  text-size: 10;
  text-placement: point;
  text-face-name: "DejaVu Sans Bold";
  text-fill: #000000;
  text-avoid-edges: true;
  text-halo-radius: 2;
  point-allow-overlap: false;
  text-dy: 14;
}

.places_db[place=country][zoom>=4][zoom<7] name,
.places_db[place=state][zoom>=7][zoom<9] name,
.places_db[place=region][zoom>=9][zoom<12] name
{
  text-size: 10;
  text-placement: point;
  text-face-name: "DejaVu Sans Bold";
  text-fill: #222222;
  text-avoid-edges: true;
  text-halo-radius: 1;
  point-allow-overlap: false;
}
.places_db[place=country][zoom>=4][zoom<7] name_en,
.places_db[place=state][zoom>=7][zoom<9] name_en,
.places_db[place=region][zoom>=9][zoom<12] name_en
{
  text-size: 8;
  text-placement: point;
  text-face-name: "DejaVu Sans Bold";
  text-fill: #222222;
  text-avoid-edges: true;
  text-halo-radius: 1;
  point-allow-overlap: false;
  text-dy: 10;
}
.places_db[place=country][zoom>=3][zoom<4] name
{
  text-size: 8;
  text-placement: point;
  text-face-name: "DejaVu Sans Bold";
  text-fill: #333333;
  text-avoid-edges: true;
  text-halo-radius: 1;
  point-allow-overlap: false;
}
.places_db[place=country][zoom>=3][zoom<4] name_en
{
  text-size: 6;
  text-placement: point;
  text-face-name: "DejaVu Sans Bold";
  text-fill: #333333;
  text-avoid-edges: true;
  text-halo-radius: 1;
  point-allow-overlap: false;
  text-dy: 8;
}
.places_db[place=city_large][zoom>=4][zoom<6] name,
.places_db[place=city_medium][zoom>=6][zoom<8] name,
.places_db[place=city][zoom>=7][zoom<8] name,
.places_db[place=town_large][zoom>=8][zoom<10] name,
.places_db[place=town][zoom>=9][zoom<10] name,
.places_db[place=village][zoom>=11][zoom<13] name,
.places_db[place=hamlet][zoom>=13][zoom<15] name
{
  text-dy: 8;
  point-file: url('img/city5.png');
  text-size: 8;
  text-placement: point;
  text-face-name: "DejaVu Sans Book";
  text-fill: #222222;
  text-avoid-edges: false;
  text-halo-radius: 1;
  point-allow-overlap: true;
}
.places_db[place=city_large][zoom>=4][zoom<6] name_en,
.places_db[place=city_medium][zoom>=6][zoom<8] name_en,
.places_db[place=city][zoom>=7][zoom<8] name_en,
.places_db[place=town_large][zoom>=8][zoom<10] name_en,
.places_db[place=town][zoom>=9][zoom<10] name_en,
.places_db[place=village][zoom>=11][zoom<13] name_en,
.places_db[place=hamlet][zoom>=13][zoom<15] name_en
{
  text-size: 6;
  text-placement: point;
  text-face-name: "DejaVu Sans Book";
  text-fill: #222222;
  text-avoid-edges: false;
  text-halo-radius: 1;
  point-allow-overlap: true;
  text-dy: 16;
}
.places_db[place=city_large][zoom>=6][zoom<10] name,
.places_db[place=city_medium][zoom>=8][zoom<10] name,
.places_db[place=city][zoom>=8][zoom<10] name,
.places_db[place=town_large][zoom>=10][zoom<13] name,
.places_db[place=town][zoom>=10][zoom<13] name,
.places_db[place=village][zoom>=13][zoom<15] name
{
  text-dy: 10;
  text-size: 10;
  point-file: url('img/city7.png');
  text-placement: point;
  text-face-name: "DejaVu Sans Book";
  text-fill: #000000;
  text-avoid-edges: false;
  text-halo-radius: 1;
  point-allow-overlap: true;
}
.places_db[place=city_large][zoom>=6][zoom<10] name_en,
.places_db[place=city_medium][zoom>=8][zoom<10] name_en,
.places_db[place=city][zoom>=8][zoom<10] name_en,
.places_db[place=town_large][zoom>=10][zoom<13] name_en,
.places_db[place=town][zoom>=10][zoom<13] name_en,
.places_db[place=village][zoom>=13][zoom<15] name_en
{
  text-size: 8;
  text-placement: point;
  text-face-name: "DejaVu Sans Book";
  text-fill: #000000;
  text-avoid-edges: false;
  text-halo-radius: 1;
  point-allow-overlap: true;
  text-dy: 20;
}
