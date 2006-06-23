#!/usr/bin/env ruby

require 'cgi'
require 'cgi/session'

THIS_URL = "http://ns1togpx.somethingmodern.com/"

$cgi = CGI::CGI.new("html3")

style = <<'EOF'
<style>
a { text-decoration: none; }
a:hover { color: blue; }
</style>
EOF

params = {}
$cgi.params.each do |param|
	key = param.first
	value = $cgi[key]
	if value.respond_to?(:read)
		params[key] = $cgi[key].read
	else
		params[key] = value.to_s.strip
	end
	params[key].untaint
end

$cgi.out { $cgi.html {
	$cgi.head { $cgi.title { "ns1togpx" } + style } +
	$cgi.body("bgcolor" => "#A0A0C0", "link" => "white", "vlink" => "white") { $cgi.multipart_form(THIS_URL) {
		"<font face='Arial,Helvetica,Verdana,Sans-serif'>" +
		$cgi.h2 { $cgi.a(THIS_URL) { "ns1togpx" } } +
		$cgi.h5 { <<EOF
Online conversion of a NetStumbler (.NS1) log file into the GPS Exchange Format
(.GPX) for use in mapping software.  This converter effectively strips the
wireless access point information from the NetStumbler file, leaving just a
track of GPS &ldquo;breadcrumbs&rdquo; for community mapping projects like
<a href="http://www.openstreetmap.org/">OpenStreetMap</a>.
EOF
		} +
		$cgi.hr +
		$cgi.p {
			$cgi.file_field("ns1_file", 80) + $cgi.br + $cgi.br +
			$cgi.submit("Upload & Convert Your .NS1 File")
		} +
		$cgi.p("align" => "right") {
			"<font size='-2'>" +
			$cgi.a("http://svn.openstreetmap.org/utils/ns1togpx/") { "GPL'ed source code" } +
			" by " +
			$cgi.a("mailto:ben@somethingmodern.com") { "Ben Gimpert" } +
			", 2006" +
			"</font>"
		} +
		"</font>"
	} }
} }

