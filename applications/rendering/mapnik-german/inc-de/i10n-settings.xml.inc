<!-- Settings for german localization of placenames -->

<!--
If you don't want german localization just uncomment the following:
-->

<!-- <!ENTITY get_name  'name'> -->


<!--
This will use a custom PL/pgSQL function for selecting the most
appropriate placename (from the perspective of a native german speaker)
-->
<!ENTITY get_name   'get_localized_name(name,"name:de",int_name,"name:en") as name'>
