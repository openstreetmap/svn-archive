select getorcreate_country(make_standard_name('uk'), 'gb');
select getorcreate_country(make_standard_name('united states'), 'us');
select count(*) from (select getorcreate_country(make_standard_name(country_code), country_code) from country_name where country_code is not null) as x;

select count(*) from (select getorcreate_country(make_standard_name(get_name_by_language(country_name.name,ARRAY['name'])), country_code) from country_name where get_name_by_language(country_name.name, ARRAY['name']) is not null) as x;
select count(*) from (select getorcreate_country(make_standard_name(get_name_by_language(country_name.name,ARRAY['name:en'])), country_code) from country_name where get_name_by_language(country_name.name, ARRAY['name:en']) is not null) as x;
select count(*) from (select getorcreate_country(make_standard_name(get_name_by_language(country_name.name,ARRAY['name:fr'])), country_code) from country_name where get_name_by_language(country_name.name, ARRAY['name:fr']) is not null) as x;
select count(*) from (select getorcreate_country(make_standard_name(get_name_by_language(country_name.name,ARRAY['name:de'])), country_code) from country_name where get_name_by_language(country_name.name, ARRAY['name:ed']) is not null) as x;
select count(*) from (select getorcreate_country(make_standard_name(get_name_by_language(country_name.name,ARRAY['name:es'])), country_code) from country_name where get_name_by_language(country_name.name, ARRAY['name:es']) is not null) as x;
select count(*) from (select getorcreate_country(make_standard_name(get_name_by_language(country_name.name,ARRAY['name:cy'])), country_code) from country_name where get_name_by_language(country_name.name, ARRAY['name:cy']) is not null) as x;

select getorcreate_amenity(make_standard_name('pub'), 'amenity','pub');
select getorcreate_amenity(make_standard_name('pubs'), 'amenity','pub');
select getorcreate_amenity(make_standard_name('restaurant'),'amenity','restaurant');
select getorcreate_amenity(make_standard_name('restaurants'),'amenity','restaurant');
select getorcreate_amenity(make_standard_name('cafe'),'amenity','cafe');
select getorcreate_amenity(make_standard_name('cafes'),'amenity','cafe');
select getorcreate_amenity(make_standard_name('bank'),'amenity','bank');
select getorcreate_amenity(make_standard_name('banks'),'amenity','bank');
select getorcreate_amenity(make_standard_name('school'),'amenity','school');
select getorcreate_amenity(make_standard_name('schools'),'amenity','school');
select getorcreate_amenity(make_standard_name('church'),'amenity','place_of_worship');
select getorcreate_amenity(make_standard_name('place of worship'),'amenity','place_of_worship');
select getorcreate_amenity(make_standard_name('park'),'leisure','park');
select getorcreate_amenity(make_standard_name('wood'),'natural','wood');
select getorcreate_amenity(make_standard_name('post box'),'amenity','post_box');
select getorcreate_amenity(make_standard_name('post boxes'),'amenity','post_box');
select getorcreate_amenity(make_standard_name('postbox'),'amenity','post_box');
select getorcreate_amenity(make_standard_name('post office'),'amenity','post_office');
select getorcreate_amenity(make_standard_name('post offices'),'amenity','post_office');
select getorcreate_amenity(make_standard_name('postoffices'),'amenity','post_office');
select getorcreate_amenity(make_standard_name('postoffice'),'amenity','post_office');
select getorcreate_amenity(make_standard_name('cinema'),'amenity','cinema');
select getorcreate_amenity(make_standard_name('cinemas'),'amenity','cinema');
select getorcreate_amenity(make_standard_name('kino'),'amenity','cinema');
select getorcreate_amenity(make_standard_name('train station'),'railway','station');
select getorcreate_amenity(make_standard_name('railway station'),'railway','station');
select getorcreate_amenity(make_standard_name('rail station'),'railway','station');
select getorcreate_amenity(make_standard_name('station'),'railway','station');
select getorcreate_amenity(make_standard_name('hospital'),'amenity','hospital');
select getorcreate_amenity(make_standard_name('zoo'),'tourism','zoo');
select getorcreate_amenity(make_standard_name('tree'),'natural','tree');
select getorcreate_amenity(make_standard_name('trees'),'natural','tree');
select getorcreate_amenity(make_standard_name('bridge'),'bridge','yes');
select getorcreate_amenity(make_standard_name('tunnel'),'tunnel','yes');
