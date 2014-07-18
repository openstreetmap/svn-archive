<!-- Settings for german localization of placenames -->

<!--
If you don't want german localization just uncomment the following:
-->

<!-- <!ENTITY get_name        'name'> -->
<!-- <!ENTITY get_placename   'name'> -->
<!-- <!ENTITY get_streetname  'name'> -->
<!-- <!ENTITY get_watername   'name'> -->
<!-- <!ENTITY get_railwayname 'name'> -->




<!--
This will use a custom PL/pgSQL function for selecting the most
appropriate name (from the perspective of a native german speaker)
-->
<!ENTITY get_name        'get_localized_name_without_brackets(name,"name:de",int_name,"name:en") as name'>
<!ENTITY get_placename   'get_localized_placename(name,"name:de",int_name,"name:en",false) as name'>
<!ENTITY get_watername   'get_localized_placename(name,"name:de",int_name,"name:en",true) as name'>
<!ENTITY get_streetname  'get_localized_streetname(name,"name:de",int_name,"name:en",true) as name'>
<!ENTITY get_railwayname 'get_latin_name(name,"name:de",int_name,"name:en") as name'>
