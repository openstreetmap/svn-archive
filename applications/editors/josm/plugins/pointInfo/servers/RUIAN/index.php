<?php
require("config.php");
$lat=$_REQUEST['lat'];
$lon=$_REQUEST['lon'];
if ( !is_numeric($lat) or !is_numeric($lon) ) die;
header('Content-Type: application/json');

$data = array();

$data["coordinates"] = array( "lat" => "$lat", "lon" => "$lon");
$data["source"] = "cuzk:ruian";


// building
$query="
  select s.kod,
        s.pocet_podlazi, a.nazev, s.plati_od, s.pocet_bytu, s.dokonceni,
        a.osmtag_k, a.osmtag_v
  from rn_stavebni_objekt s
      left outer join osmtables.zpusob_vyuziti_objektu a on s.zpusob_vyuziti_kod = a.kod
  where st_contains(s.hranice,st_transform(st_geomfromtext('POINT(".$lon." ".$lat.")',4326),900913))
  and not s.deleted
  limit 1;
";
$result=pg_query($CONNECT,$query);

if (pg_num_rows($result) > 0)
{
  $row = pg_fetch_array($result, 0);


  $data["stavebni_objekt"] =
    array( "ruian_id" => $row["kod"],
           "pocet_podlazi" => $row["pocet_podlazi"],
           "zpusob_vyuziti" => $row["nazev"],
           "zpusob_vyuziti_key" => $row["osmtag_k"],
           "zpusob_vyuziti_val" => $row["osmtag_v"],
           "pocet_bytu" => $row["pocet_bytu"],
           "dokonceni" => $row["dokonceni"],
           "plati_od" => $row["plati_od"]
           );
} else
    $data["stavebni_objekt"] = array();

// Addresses
if ($data["stavebni_objekt"]["ruian_id"] > 0)
{
  $query="
  select am.kod as adresni_misto_kod,
         am.stavobj_kod,
         st_asgeojson(st_transform(am.definicni_bod, 4326)) as pozice,
         CASE
           WHEN s.typ_kod = 1 THEN 'Číslo popisné'
           WHEN s.typ_kod = 2 THEN 'Číslo evidenční'
           WHEN s.typ_kod = 3 THEN 'bez č.p./č.e.'
           ELSE ''
         END cislo_typ,
         am.cislo_domovni,
         am.cislo_orientacni_hodnota || coalesce(am.cislo_orientacni_pismeno, '') cislo_orientacni,
         am.adrp_psc psc, ul.nazev ulice, c.nazev cast_obce,
         momc.nazev mestska_cast,
         ob.nazev obec, ok.nazev okres, vu.nazev kraj
   from ruian.rn_adresni_misto am
        left outer join rn_stavebni_objekt s on am.stavobj_kod = s.kod and not s.deleted
        left outer join osmtables.zpusob_vyuziti_objektu a on s.zpusob_vyuziti_kod = a.kod
        left outer join rn_ulice ul on am.ulice_kod = ul.kod and not ul.deleted
        left outer join rn_cast_obce c on c.kod = s.cobce_kod and not c.deleted
        left outer join rn_momc momc on momc.kod = s.momc_kod and not momc.deleted
        left outer join rn_obec ob on coalesce(ul.obec_kod, c.obec_kod)  = ob.kod and not ob.deleted
        left outer join rn_okres ok on ob.okres_kod = ok.kod and not ok.deleted
        left outer join rn_vusc vu on ok.vusc_kod = vu.kod and not vu.deleted
    where am.stavobj_kod = ".$data["stavebni_objekt"]["ruian_id"]."
    and not am.deleted
    order by st_distance( (st_transform(am.definicni_bod,4326))::geography,
                          (st_setsrid(st_makepoint(".$lon.",".$lat."),4326))::geography)
  ;
  ";

  $result=pg_query($CONNECT,$query);
  $error= pg_last_error($CONNECT);
  if (pg_num_rows($result) > 0)
  {
    $am = array();
    for ($i = 0; $i < pg_num_rows($result); $i++)
    {
      $row = pg_fetch_array($result, $i);
      $geometry=json_decode($row["pozice"], true);
      array_push($am,
                  array("ruian_id" => $row["adresni_misto_kod"],
                        "pozice" => $geometry['coordinates'],
                        "budova_kod" => $row["stavobj_kod"],
                        "cislo_typ" => $row["cislo_typ"],
                        "cislo_domovni" => $row["cislo_domovni"],
                        "cislo_orientacni" => $row["cislo_orientacni"],
                        "ulice" => $row["ulice"],
                        "cast_obce" => $row["cast_obce"],
                        "mestska_cast" => $row["mestska_cast"],
                        "obec" => $row["obec"],
                        "okres" => $row["okres"],
                        "kraj" => $row["kraj"],
                        "psc" => $row["psc"]
                        ));
    }
      $data["adresni_mista"] = $am;
  } else
  {
  //   echo "error: $error\n";
    $data["adresni_mista"] = array();
  }
}
else
{
  $query="
  select am.kod as adresni_misto_kod,
         am.stavobj_kod,
         st_asgeojson(st_transform(am.definicni_bod, 4326)) as pozice,
         CASE
           WHEN s.typ_kod = 1 THEN 'Číslo popisné'
           WHEN s.typ_kod = 2 THEN 'Číslo evidenční'
           WHEN s.typ_kod = 3 THEN 'bez č.p./č.e.'
           ELSE ''
         END cislo_typ,
         am.cislo_domovni,
         am.cislo_orientacni_hodnota || coalesce(am.cislo_orientacni_pismeno, '') cislo_orientacni,
         am.adrp_psc psc, ul.nazev ulice, c.nazev cast_obce,
         momc.nazev mestska_cast,
         ob.nazev obec, ok.nazev okres, vu.nazev kraj
  from ( select kod, stavobj_kod,
                cislo_domovni, cislo_orientacni_hodnota, cislo_orientacni_pismeno,
                ulice_kod, adrp_psc,
                definicni_bod
        from ruian.rn_adresni_misto
        where not deleted
        order by definicni_bod <->
                st_transform(st_setsrid(st_makepoint(".$lon.", ".$lat."),4326),900913)
        limit 100) as am
      left outer join rn_stavebni_objekt s on am.stavobj_kod = s.kod and not s.deleted
      left outer join osmtables.zpusob_vyuziti_objektu a on s.zpusob_vyuziti_kod = a.kod
      left outer join rn_ulice ul on am.ulice_kod = ul.kod and not ul.deleted
      left outer join rn_cast_obce c on c.kod = s.cobce_kod and not c.deleted
      left outer join rn_momc momc on momc.kod = s.momc_kod and not momc.deleted
      left outer join rn_obec ob on coalesce(ul.obec_kod, c.obec_kod)  = ob.kod and not ob.deleted
      left outer join rn_okres ok on ob.okres_kod = ok.kod and not ok.deleted
      left outer join rn_vusc vu on ok.vusc_kod = vu.kod and not vu.deleted
  where st_distance( (st_transform(am.definicni_bod,4326))::geography, (st_setsrid(st_makepoint(".$lon.", ".$lat."),4326))::geography ) < 100
  order by st_distance( (st_transform(am.definicni_bod,4326))::geography,
                        (st_setsrid(st_makepoint(".$lon.", ".$lat."),4326))::geography)
  limit 5
  ;
  ";

  $result=pg_query($CONNECT,$query);
  $error= pg_last_error($CONNECT);
  if (pg_num_rows($result) > 0)
  {
    $am = array();
    for ($i = 0; $i < pg_num_rows($result); $i++)
    {
      $row = pg_fetch_array($result, $i);
      $geometry=json_decode($row["pozice"], true);
      array_push($am,
                  array("ruian_id" => $row["adresni_misto_kod"],
                        "pozice" => $geometry['coordinates'],
                        "budova_kod" => $row["stavobj_kod"],
                        "cislo_typ" => $row["cislo_typ"],
                        "cislo_domovni" => $row["cislo_domovni"],
                        "cislo_orientacni" => $row["cislo_orientacni"],
                        "ulice" => $row["ulice"],
                        "cast_obce" => $row["cast_obce"],
                        "mestska_cast" => $row["mestska_cast"],
                        "obec" => $row["obec"],
                        "okres" => $row["okres"],
                        "kraj" => $row["kraj"],
                        "psc" => $row["psc"]
                        ));
    }
    $data["adresni_mista"] = $am;
  } else
  {
  //   echo "error: $error\n";
    $data["adresni_mista"] = array();
  }

}

// land
$query="
  select s.id, a.nazev as druh_pozemku, b.nazev as zpusob_vyuziti, s.plati_od
  from rn_parcela s
      left outer join osmtables.druh_pozemku a on s.druh_pozemku_kod = a.kod
      left outer join osmtables.zpusob_vyuziti_pozemku b on s.zpusob_vyu_poz_kod = b.kod
  where st_contains(s.hranice,st_transform(st_geomfromtext('POINT(".$lon." ".$lat.")',4326),900913))
  and not s.deleted
  limit 1;
";

$result=pg_query($CONNECT,$query);
$error= pg_last_error($CONNECT);
if (pg_num_rows($result) > 0)
{
  $row = pg_fetch_array($result, 0);

  $data["parcela"] =
    array( "ruian_id" => $row["id"],
           "druh_pozemku" => $row["druh_pozemku"],
           "zpusob_vyuziti" => $row["zpusob_vyuziti"],
           "plati_od" => $row["plati_od"]
         );
} else
{
//   echo "error: $error\n";
  $data["parcela"] = array();
}

// ulice
$query="
  select u.kod, u.nazev as jmeno
  from ( select kod, nazev, definicni_cara
        from ruian.rn_ulice
        where not deleted
        order by definicni_cara <->
                st_transform(st_setsrid(st_makepoint(".$lon.",".$lat."),4326),900913)
        limit 500) as u
  where st_distance( (st_transform(u.definicni_cara,4326))::geography, (st_setsrid(st_makepoint(".$lon.",".$lat."),4326))::geography ) < 10
  order by st_distance( (st_transform(u.definicni_cara,4326))::geography,
                        (st_setsrid(st_makepoint(".$lon.",".$lat."),4326))::geography)
  limit 1
  ;
";

$result=pg_query($CONNECT,$query);
$error= pg_last_error($CONNECT);
if (pg_num_rows($result) > 0)
{
  $row = pg_fetch_array($result, 0);

  $data["ulice"] =
    array( "ruian_id" => $row["kod"],
          "jmeno" => $row["jmeno"]);
} else
{
//   echo "error: $error\n";
  $data["ulice"] = array();
}

// cadastral area
$query="
  select ku.kod, ku.nazev,
         ob.nazev obec, ok.nazev okres, vu.nazev kraj
  from rn_katastralni_uzemi ku
      left outer join rn_obec ob on ku.obec_kod = ob.kod and not ob.deleted
      left outer join rn_okres ok on ob.okres_kod = ok.kod and not ok.deleted
      left outer join rn_vusc vu on ok.vusc_kod = vu.kod and not vu.deleted
  where st_contains(ku.hranice,st_transform(st_geomfromtext('POINT(".$lon." ".$lat.")',4326),900913))
  and not ku.deleted
  limit 1;
";

$result=pg_query($CONNECT,$query);
$error= pg_last_error($CONNECT);
if (pg_num_rows($result) > 0)
{
  $row = pg_fetch_array($result, 0);

  $data["katastr"] =
    array( "ruian_id" => $row["kod"],
           "nazev" => $row["nazev"],
           "obec" => $row["obec"],
           "okres" => $row["okres"],
           "kraj" => $row["kraj"],
           );
} else
{
  $data["katastr"] = array();
}

// cadastral area
$query="
  select ku.kod, ku.nazev,
         ob.nazev obec, ok.nazev okres, vu.nazev kraj
  from rn_katastralni_uzemi ku
      left outer join rn_obec ob on ku.obec_kod = ob.kod and not ob.deleted
      left outer join rn_okres ok on ob.okres_kod = ok.kod and not ok.deleted
      left outer join rn_vusc vu on ok.vusc_kod = vu.kod and not vu.deleted
  where st_contains(ku.hranice,st_transform(st_geomfromtext('POINT(".$lon." ".$lat.")',4326),900913))
  and not ku.deleted
  limit 1;
";

$result=pg_query($CONNECT,$query);
$error= pg_last_error($CONNECT);
if (pg_num_rows($result) > 0)
{
  $row = pg_fetch_array($result, 0);

  $data["katastr"] =
    array( "ruian_id" => $row["kod"],
           "nazev" => $row["nazev"],
           "obec" => $row["obec"],
           "okres" => $row["okres"],
           "kraj" => $row["kraj"],
           );
} else
{
  $data["katastr"] = array();
}

echo json_encode($data);

?>
