Relation Tags Osmosis Plugin
Ilya Zverev, Sep 2011

Moves tags from relations to its members.


== Installation ==

Run ant to build relationTagsPlugin.jar, the copy it from build/dist
to your osmosis libraries folder (for example, osmosis/lib/default).


== Invocation ==

Add the following parameters to you osmosis pipe:

	--set-relation-tags
	--rt


== Possible parameters ==

* types
	Comma-separated list of relation types to be processed.
	Default: route,destination_sign,enforcement

* separator
	Separator for new tag values, between relation type and relation tag.
	Default: _

* multi
	Separator for multiple values.
	Default: ;

* sort=yes/no
	Whether to sort multiple tag values. Note: tags will be sorted independently
	(connection tag-value for different tags will be lost).
	Default: no


== Example ==

osmosis --read-pbf test.pbf --rt types=restriction separator=: --write-xml result.osm

  <relation id="21938">
    <member type="node" ref="253885923" role="via"/>
    <member type="way" ref="23430290" role="from"/>
    <member type="way" ref="32013470" role="to"/>
    <tag k="restriction" v="only_right_turn"/>
    <tag k="type" v="restriction"/>
  </relation>

Resulting tags:

  <node id="253885923">
    <tag k="restriction:restriction" v="only_right_turn"/>
    <tag k="restriction:role" v="via"/>
  </node>
  <way id="23430290">
    <tag k="highway" v="tertiary"/>
    <tag k="name" v="Some Street"/>
    <tag k="restriction:restriction" v="only_right_turn"/>
    <tag k="restriction:role" v="from"/>
  </way>
  <way id="32013470" version="12" timestamp="2011-02-11T19:36:08Z" uid="115477" user="wowik" changeset="7258243">
    <tag k="highway" v="secondary"/>
    <tag k="lanes" v="4"/>
    <tag k="name" v="Other Street"/>
    <tag k="restriction:restriction" v="only_right_turn;only_right_turn"/>
    <tag k="restriction:role" v="to;to"/>
  </way>
