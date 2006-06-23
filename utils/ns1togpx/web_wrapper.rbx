#!/usr/bin/env ruby

=begin Copyright (C) 2006 Ben Gimpert (ben@somethingmodern.com)

This program is free software; you can redistribute it and/or
modify it under the terms of the GNU General Public License
as published by the Free Software Foundation; either version 2
of the License, or (at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program; if not, write to the Free Software
Foundation, Inc., 59 Temple Place - Suite 330, Boston,
MA  02111-1307, USA.

=end

require 'cgi'
require 'cgi/session'
require 'open3'

THIS_URL = "http://ns1togpx.somethingmodern.com/"
THIS_DIR = File.expand_path(File.dirname(__FILE__))

$cgi = CGI.new("html3")

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

errors, gpx = '', nil
unless params["ns1_file"].empty?
	IO.popen("#{THIS_DIR}/ns1togpx", "w+") do |io|
#	Open3.popen3("#{THIS_DIR}/ns1togpx") do |stdin, stdout, stderr|
		io.print params["ns1_file"]
		io.close_write
		gpx = io.read
errors = gpx
	end
	Process.wait
end
errors = "<p><font face=\"Courier New,Courier,Monospace\" color=\"red\">" + errors.split(/\n/).join("<br>") + "</font></p>" unless errors.empty?

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
		errors +
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

