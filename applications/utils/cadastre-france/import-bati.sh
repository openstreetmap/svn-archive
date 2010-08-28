#!/bin/sh
# Repris du script (r-cadastre-client) de Frédéric Rodrigo, copyleft 2009 - GPL 2.0
if [ $# -lt 4 ] || [ $# -gt 5 ] ; then
    cat <<EOF
Usage: import-bati.sh [OPTION] [dept] [ville] [repertoire] [IGNF]
   -f,--force    force un retéléchargement des fichiers
  dept           numéro de département sur 3 chiffres
  ville          nom de la ville tel qu'il figure sur le site
  repertoire     repertoire de sortie
  IGNF           code IGNF correspondant à la ville
                   LAMB[E1-4], RGF93CC[42-50], GUADFM49U20, GUAD48UTM20,
                   MART38UTM20, RGR92UTM40S, UTM22RGFG95
EOF
    exit
fi;

force=false
if [ $1 = "-f" ] || [ $1 = "--force" ] ; then
    force=true
    shift 1
fi;
departement=$1
ville=$2
dir=$3
IGNF=$4
# Pour obtenir une bonne précision sur les communes étendues, on prend
# des rectangles de l_deg x h_deg (en degrés) pour les imprimer sur
# une zone de l_pix x h_pix (en pixels)
l_deg=3000
h_deg=3000
l_pix=9000
h_pix=9000
IFS="\n"

# répertoire temporaire
[ -d tmp ] || mkdir tmp

# répertoire de sortie
[ -d "$dir" ] || mkdir "$dir"

# répertoire de sortie
[ -d "${dir}/pdf" ] || mkdir "${dir}/pdf"

# répertoire de sortie
[ -d "${dir}/osm" ] || mkdir "${dir}/osm"


# Requête POST
villeHTTP=`echo "${ville}" | sed 's/ /+/g'`
data="numeroVoie=&indiceRepetition=&nomVoie=&lieuDit=&ville=${villeHTTP}&codePostal=&codeDepartement=${departement}&nbResultatParPage=10&x=31&y=11"

# Récupération du code de la commune
code=`find "$dir/pdf/" -depth -name "${departement}-${ville}-*.bbox" | head -n1 \
    | sed "s/^$dir\/pdf\/${departement}-${ville}-\([^-]*\)\.bbox.*$/\1/"`
if [ -z "$code" ] || $force ; then
    curl -c tmp/cookies-$$-1 \
	"http://www.cadastre.gouv.fr/scpc/rechercherPlan.do" > tmp/page-$$-1.html
    curl -b tmp/cookies-$$-1 \
	-c tmp/cookies-$$-2 \
	-d "$data" \
	"http://www.cadastre.gouv.fr/scpc/rechercherPlan.do" > tmp/page-$$-2.html

    code=`grep 'afficherCarteCommune.do?c=' tmp/page-$$-2.html | sed 's/.*afficherCarteCommune.do?c=\([A-Z0-9]*\).*/\1/'`
    if [ -z "$code" ]; then
	code=`grep -o -E "<option value=\"S[0-9]+\" >${ville} - [0-9]+</option>" tmp/page-$$-2.html | cut -d '"' -f 2`
    fi
    if [ -z "$code" ]; then
	echo "Pas de code de commune"
	exit
    fi
fi
echo CODE=$code
baseName=${departement}-${ville}-${code}

if $force || [ !  -f "$dir/pdf/$baseName-0-0.pdf"  ] || [ !  -f "$dir/pdf/$baseName.bbox"  ] ;  then
# Récupération de la bounding-box de la commune
    curl -b tmp/cookies-$$-2 \
	-c tmp/cookies-$$-3 \
	"http://www.cadastre.gouv.fr/scpc/afficherCarteCommune.do?c=${code}&dontSaveLastForward&keepVolatileSession=" \
	> tmp/page-$$-3.html
    bb=`grep -A4 'new GeoBox' tmp/page-$$-3.html | head -n5 \
	| tr "[:cntrl:]" " " | tr -s "[:space:]" \
	| sed -r 's/.*\( ([0-9.]+), ([0-9.]+), ([0-9.]+), ([0-9.]+)\).*/\1 \2 \3 \4/'`
    echo ${bb} > "$dir/pdf/$baseName.bbox";
else
    bb=`cat "$dir/pdf/$baseName.bbox"`
fi;
[ -z "$bb" ] && rm "$dir/pdf/$baseName.bbox"

echo BB=${bb}

xmin=`echo ${bb} | awk '{print $1}'`
xmin=`echo "$xmin - 10" | bc`
xmax=`echo ${bb} | awk '{print $3}'`
xmax=`echo "$xmax + 10" | bc`
ymin=`echo ${bb} | awk '{print $2}'`
ymin=`echo "$ymin - 10" | bc`
ymax=`echo ${bb} | awk '{print $4}'`
ymax=`echo "$ymax + 10" | bc`

# Découpe la bbox en m x n rectangles
m=`echo "($xmax-$xmin-1)/$l_deg+1" | bc`
n=`echo "($ymax-$ymin-1)/$h_deg+1" | bc`
i=0
while [ $i -lt $m ] ;
do
    j=0
    while [ $j -lt $n ] ;
    do
	l_pix2=$l_pix
	h_pix2=$h_pix
	x1=`echo "scale = 2; $xmin + $i * $l_deg" | bc`
	x2=`echo "scale = 2; $x1 + $l_deg" | bc`
	if [ `echo "$x2 > $xmax" | bc` -eq 1 ]
	then
	    x2=$xmax
	    l_pix2=`echo "scale = 0; ($x2-$x1) * $l_pix / $l_deg" | bc`
	fi
	y1=`echo "scale = 2; $ymin + $j * $h_deg" | bc`
	y2=`echo "scale = 2; $y1 + $h_deg" | bc`
	if [ `echo "$y2 > $ymax" | bc` -eq 1 ]
	then
	    y2=$ymax
	    h_pix2=`echo "scale = 0; ($y2-$y1) * $h_pix / $h_deg" | bc`
	fi

	if $force || [ ! -f "$dir/pdf/$baseName-$i-$j.svg" ] ; then
	    curl -b tmp/cookies-$$-2 \
		-c tmp/cookies-$$-3 \
		-d "WIDTH=$l_pix2" \
		-d "HEIGHT=$h_pix2" \
		-d "MAPBBOX=$x1%2C$y1%2C$x2%2C$y2" \
		-d "SLD_BODY=" \
		-d "RFV_REF=$code" \
		"http://www.cadastre.gouv.fr/scpc/imprimerExtraitCadastralNonNormalise.do" \
		> "$dir/pdf/$baseName-$i-$j.pdf"

	    pdf2svg "$dir/pdf/$baseName-$i-$j.pdf" "$dir/pdf/$baseName-$i-$j.svg" \
		|| rm "$dir/pdf/$baseName-$i-$j.pdf"
	fi
# Stocke le nom de chaque fichier suivi de sa bbox
	fichiers[$((5*($n*$i+$j)))]="$dir/pdf/$baseName-$i-$j.svg"
	fichiers[$((5*($n*$i+$j)+1))]=$x1
	fichiers[$((5*($n*$i+$j)+2))]=$y1
	fichiers[$((5*($n*$i+$j)+3))]=$x2
	fichiers[$((5*($n*$i+$j)+4))]=$y2

	j=$((j+1))
    done;
    i=$((i+1))
done;

perl svg-parser.pl $IGNF ${fichiers[*]} > "$dir/osm/$baseName.osm"
while [ $i -lt $m ] ;
do
    j=0
    while [ $j -lt $n ] ;
    do
	rm "$dir/pdf/$baseName-$i-$j.svg"
	j=$((j+1))
    done;
    i=$((i+1))
done;
