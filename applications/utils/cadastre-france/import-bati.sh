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

# répertoire temporaire
[ -d tmp ] || mkdir tmp

# répertoire de sortie
[ -d "$dir" ] || mkdir "$dir"

# répertoire de sortie
[ -d "${dir}/pdf" ] || mkdir "${dir}/pdf"

# répertoire de sortie
[ -d "${dir}/osm" ] || mkdir "${dir}/osm"


# Requete POST
villeHTTP=`echo "${ville}" | sed 's/ /+/g'`
data="numeroVoie=&indiceRepetition=&nomVoie=&lieuDit=&ville=${villeHTTP}&codePostal=&codeDepartement=${departement}&nbResultatParPage=10&x=31&y=11"

# Recuperation du code de la commune
code=`ls "$dir/pdf/${departement}-${ville}-"*.bbox | sed "s/^.*$dir\/pdf\/${departement}-${ville}-\([^-]*\)\.bbox.*$/\1/"`
if [ -z "$code" ] || $force ; then
    curl -c tmp/cookies-$$-1 "http://www.cadastre.gouv.fr/scpc/rechercherPlan.do" > tmp/page-$$-1.html
    curl -b tmp/cookies-$$-1 -c tmp/cookies-$$-2 -d "$data" "http://www.cadastre.gouv.fr/scpc/rechercherPlan.do" > tmp/page-$$-2.html

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

# Recuperation de la bounding-box de la commune
if $force || [ !  -f "$dir/pdf/$baseName.pdf"  ] || [ !  -f "$dir/pdf/$baseName.bbox"  ] ;  then

    curl -b tmp/cookies-$$-2 -c tmp/cookies-$$-3 "http://www.cadastre.gouv.fr/scpc/afficherCarteCommune.do?c=${code}&dontSaveLastForward&keepVolatileSession=" > tmp/page-$$-3.html
    bb=`grep -A4 -m1 'new GeoBox' tmp/page-$$-3.html | tr "[:cntrl:]" " " | tr -s "[:space:]" | sed 's/.* \([0-9.]\+\), \([0-9.]\+\), \([0-9.]\+\), \([0-9.]\+\).*/\1 \2 \3 \4/'`
    echo ${bb} > "$dir/pdf/$baseName.bbox" ;
    x1=`echo ${bb} | awk '{print $1}'`
    x2=`echo ${bb} | awk '{print $3}'`
    y1=`echo ${bb} | awk '{print $2}'`
    y2=`echo ${bb} | awk '{print $4}'`
    l=90000
    h=90000

    curl -b tmp/cookies-$$-2 -c tmp/cookies-$$-3 -d "WIDTH=$l" -d "HEIGHT=$h" -d "MAPBBOX=$x1%2C$y1%2C$x2%2C$y2" -d  "SLD_BODY=" -d "RFV_REF=$code" "http://www.cadastre.gouv.fr/scpc/imprimerExtraitCadastralNonNormalise.do" > "$dir/pdf/$baseName.pdf"

else
    bb=`cat "$dir/pdf/$baseName.bbox"`
fi;

echo BB=${bb}

pdf2svg "$dir/pdf/$baseName.pdf" "$dir/pdf/$baseName.svg" || rm "$dir/pdf/$baseName.pdf"

if [ -f "$dir/pdf/$baseName.svg" ] ; then
    perl svg-parser.pl $IGNF "$dir/pdf/$baseName.svg" $bb > "$dir/osm/$baseName.osm"
    rm "$dir/pdf/$baseName.svg"
fi;