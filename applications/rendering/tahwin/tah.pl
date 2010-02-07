#!perl -w
use strict;
use warnings;
use Cwd;
use Win32::GUI();
use IPC::Open3;
use Symbol qw(gensym);
use IO::File;
use Win32::Registry;

use threads;
use threads::shared;

#svn co http://svn.openstreetmap.org/applications/rendering/tilesAtHome/

my $dircur					= getcwd; $dircur =~ s/\//\\/g;
my $dirperl					= "$dircur\\perl";
my $dirinkscape				= "$dircur\\inkscape";
my $dirsvn					= "$dircur\\svn";
my $dirpngcrush				= "$dircur\\pngcrush";
my $dirpngnq				= "$dircur\\pngnq";
my $dirzip					= "$dircur\\zip";
my $dirtah					= "$dircur\\tilesAthome";
my $svnserverfile			= "$ENV{APPDATA}\\Subversion\\servers";
my $tahauthenticationfile	= "$dirtah\\authentication.conf";
my $tahconfigurationfile	= "$dirtah\\tilesAtHome.conf";
my $tahauthenticationfileex	= "$dirtah\\authentication.conf.example";
my $tahconfigurationfileex	= "$dirtah\\tilesAtHome.conf.windows";

$ENV{PATH} = "$dirperl\\bin;$dirinkscape;$dirpngnq;$dirsvn\\bin;$dirpngcrush;$dirzip;$ENV{PATH}";

if ( ! -f "$ENV{APPDATA}\\Subversion\\servers") {
	my $cmdout =`"$dircur\\svn\\bin\\svn.exe" help >NUL`;
}

my $http_proxy_enabled = 0;
my $http_proxy_exceptions = "";
my $http_proxy_host = "";
my $http_proxy_port = "";
my $http_proxy_username = "";
my $http_proxy_password = "";

my $authentication_username = "";
my $authentication_password = "";

my $DecimalSeparator = "";

my $console_visible = "";
my $text_to_check = "";
my $batikRasterizer = 0;

my $tahreg;
my $type;

$::HKEY_CURRENT_USER->Create("SOFTWARE\\openstreetmap.org\\tah", $tahreg) or die "Can't create record: $^E";;
$::HKEY_CURRENT_USER->Open("SOFTWARE\\openstreetmap.org\\tah", $tahreg) or die "Can't open proxy: $^E";
$tahreg->QueryValueEx("http_proxy_enable", $type, $http_proxy_enabled);
$tahreg->QueryValueEx("http_proxy_exceptions", $type, $http_proxy_exceptions);
$tahreg->QueryValueEx("http_proxy_host", $type, $http_proxy_host);
$tahreg->QueryValueEx("http_proxy_port", $type, $http_proxy_port);
$tahreg->QueryValueEx("http_proxy_username", $type, $http_proxy_username);
$tahreg->QueryValueEx("http_proxy_password", $type, $http_proxy_password);
$tahreg->QueryValueEx("authentication_username", $type, $authentication_username);
$tahreg->QueryValueEx("authentication_password", $type, $authentication_password);
$tahreg->QueryValueEx("console_visible", $type, $console_visible);
$tahreg->QueryValueEx("batikRasterizer", $type, $batikRasterizer);
$tahreg->Close();

#$::HKEY_LOCAL_MACHINE->SetValue("proxy_enable", REG_BINARY, 0);
#$::HKEY_LOCAL_MACHINE->SetValue("proxy_enable", REG_SZ, 0);

$::HKEY_CURRENT_USER->Open("Control\ Panel\\International", $tahreg) or die "Can't open International Settings: $^E";
$tahreg->QueryValueEx("sDecimal", $type, $DecimalSeparator);
$tahreg->Close();

my $DOS = Win32::GUI::GetPerlWindow();
Win32::GUI::Hide($DOS) if (! $console_visible);

# Primarily a 2 column layout:
my $col1_width = 235;
my $col2_width = 255;

my $padding = 10;
my $margin = 10;

my $col1_left       = 0;
my $col1_gb_left    = $col1_left + $padding;
my $col1_ctrl_left  = $col1_gb_left + $padding;
my $col1_gb_right   = $col1_left + $col1_width - ($padding/2);  # collapse padding between columns
my $col1_ctrl_right = $col1_gb_right - $padding;

my $col2_left       = $col1_width;
my $col2_gb_left    = $col2_left + ($padding/2);
my $col2_ctrl_left  = $col2_gb_left + $padding;
my $col2_gb_right   = $col2_left + $col2_width - $padding;
my $col2_ctrl_right = $col2_gb_right - $padding;

my $row1_gb_top = 0;  # no padding at top

my $desk = Win32::GUI::GetDesktopWindow();
my $dw = Win32::GUI::Width($desk);
my $dh = Win32::GUI::Height($desk);
my $w = 400;
my $h = 496;

my $x = ($dw - $w) / 2;
my $y = ($dh - $h) / 2;

my $main = Win32::GUI::Window->new(
	-name  => 'Main',
	-title => 'tiles@Home for Windows (eddi_<at>_dpeddi.com)',
	-size  => [ $w,$h ],
	-resizable   => 0,
	-maximizebox => 0,
	-left  => $x,
	-top => $y
);

my $px = $main->AddGroupbox(
	-name  => "PX",
	-title => "&Proxy Settings",
	-left  => $col1_gb_left,
	-top   => $row1_gb_top,
	-width => $col2_gb_right - $col1_gb_left,
	-group => 1,
);

$main->AddCheckbox(
	-name    => "PXCB",
	-text    => "Use proxy",
	-left    => $col1_ctrl_left,
	-top     => 20,
	-onClick => \&toggle_proxy,
#	-tabstop => 1,
);
$main->PXCB->Checked($http_proxy_enabled);

$main->AddTextfield(
	-name     => "PXPO",
	-text     => $http_proxy_port,
	-prompt   => [ "port", 30 ],
	-left     => $col2_ctrl_right - 70,
	-top      => 20,
	-width    => 40,
	-height   => 20,
#	-tabstop  => 1,
);

$main->AddTextfield(
	-name     => "PXHO",
	-text     => $http_proxy_host,
	-prompt   => [ "host", 30 ],
	-left     => $col1_ctrl_left + $main->PXCB->Width() + $padding,
	-top      => 20,
	-width    => $col2_ctrl_right - $col1_ctrl_left - $main->PXCB->Width() - $main->PXPO->Width() - 70 - ( $padding * 2 ),
	-height   => 20,
#	-tabstop  => 1,
);

my $row1_gb_bottom = $main->PXCB->Top() + $main->PXCB->Height();
my $row2_gb_top = $row1_gb_bottom + $padding;

$main->AddTextfield(
	-name     => "PXUS",
	-text     => $http_proxy_username,
	-prompt   => [ "username", 50 ],
	-left     => $col1_ctrl_left,
	-top      => $row2_gb_top,
	-width    => $col1_gb_right - $col1_ctrl_left - 60,
	-height   => 20,
#	-tabstop  => 1,
);
$main->AddTextfield(
	-name     => "PXPA",
	-text     => $http_proxy_password,
	-prompt   => [ "password", 50 ],
	-left     => $col2_ctrl_left,
	-top      => $row2_gb_top,
	-width    => $col2_gb_right - $col2_ctrl_left - 60,
	-height   => 20,
	-password => 1,
#	-tabstop  => 1,
);

my $row2_gb_bottom = $main->PXUS->Top() + $main->PXUS->Height();
my $row3_gb_top = $row2_gb_bottom + $padding;

$main->AddTextfield(
	-name     => "PXEX",
	-text     => $http_proxy_exceptions,
	-prompt   => [ "exceptions", 50 ],
	-left     => $col1_ctrl_left,
	-top      => $row3_gb_top,
	-width    => $col2_gb_right - $col1_ctrl_left - 60,
	-height   => 20,
#	-tabstop  => 1,
);

my $row3_gb_bottom = $main->PXEX->Top() + $main->PXEX->Height();
my $row4_gb_top = $row3_gb_bottom + $padding;

my $au = $main->AddGroupbox(
	-name  => "AU",
	-title => "&Authentication Settings",
	-left  => $col1_gb_left,
	-top   => $row4_gb_top,
	-width => $col2_gb_right - $col1_gb_left,
	-group => 1,
);

$main->AddTextfield(
	-name     => "AUUS",
	-text     => $authentication_username,
	-prompt   => [ "username", 50 ],
	-left     => $col1_ctrl_left,
	-top      => $row4_gb_top + 20,
	-width    => $col1_gb_right - $col1_ctrl_left - 60,
	-height   => 20,
#	-tabstop  => 1,
);
$main->AddTextfield(
	-name     => "AUPA",
	-text     => $authentication_password,
	-prompt   => [ "password", 50 ],
	-left     => $col2_ctrl_left,
	-top      => $row4_gb_top + 20,
	-width    => $col2_gb_right - $col2_ctrl_left - 60,
	-height   => 20,
	-password => 1,
#	-tabstop  => 1,
);

my $row4_gb_bottom = $main->AUUS->Top() + $main->AUUS->Height();
my $row5_gb_top = $row4_gb_bottom + $padding;

my $tah = $main->AddGroupbox(
	-name  => "TAH",
	-title => "&Application",
	-left  => $col1_gb_left,
	-top   => $row5_gb_top,
	-width => $col2_gb_right - $col1_gb_left,
	-group => 1,
);

$main->AddButton(
	-name  => "TAHB1",
	-text  => "update tah",
	-left  => $col1_ctrl_left + 1 * ( $col1_ctrl_right - $col1_ctrl_left - 2 * $padding ) / 6 - 30,
	-width => 60,
	-top   => $row5_gb_top,
	-onClick => \&start_svn,
);

$main->AddButton(
	-name  => "TAHB2",
	-text  => "start tah",
	-left  => $col1_ctrl_left + 3 * ( $col1_ctrl_right - $col1_ctrl_left + 2 * $padding ) / 6 - 30,
	-width => 60,
	-top   => $row5_gb_top,
	-onClick => \&start_tah,
);

$main->AddButton(
	-name  => "TAHB3",
	-text  => "stop tah",
	-left  => $col1_ctrl_left + 5 * ( $col1_ctrl_right - $col1_ctrl_left + 2 * $padding ) / 6 - 30,
	-width => 60,
	-top   => $row5_gb_top,
	-onClick => \&stop_tah,
);

$main->AddCombobox(
	-name         => "BCB",
	-dropdownlist => 1,
	-left         => $col2_ctrl_left,
	-top          => $row5_gb_top,
	-width        => 110,
	-height       => 80,
#	-onChange     => sub { change_balloon("icon" => $_[0]->Text()); },
	-tabstop      => 1,
);
$main->BCB->Add('tilesGen.pl loop', 'tilesGen.pl upload', 'tilesGen.pl ...');
$main->BCB->SetCurSel(0);

$main->AddTextfield(
	-name     => "BCBP",
	-text     => "",
	-left     => $col2_ctrl_right - $main->BCB->Width(),
	-top      => $row5_gb_top,
	-width    => $col2_ctrl_right - $col2_ctrl_left - $main->BCB->Width(),
	-height   => 20,
);

my $row5_gb_bottom = $main->TAHB1->Top() + $main->TAHB1->Height();
my $row6_gb_top = $row5_gb_bottom + $padding;

$main->AddCheckbox(
        -name    => "SCCB",
        -text    => "Show console window (DO NOT CLOSE IT)",
        -left    => $col1_ctrl_left,
        -top     => $row6_gb_top,
        -onClick => \&toggle_console,
);
$main->SCCB->Checked($console_visible);

my $row6_gb_bottom = $main->SCCB->Top() + $main->SCCB->Height();
my $row7_gb_top = $row6_gb_bottom + $padding;

$main->AddTextfield(
	-name => 'LB',
	-left     => $col1_ctrl_left,
	-width    => $col2_ctrl_right - $col1_ctrl_left,
	-top      => $row7_gb_top,
	-height   => 240,
	-multiline   => 1,
	-autovscroll => 1,
	-vscroll   => 1,
	-readonly => 1,
        -onChange => \&check_full,
);

my $row7_gb_bottom = $main->LB->Top() + $main->LB->Height();
my $row8_gb_top = $row7_gb_bottom + $padding;

$main->PX->Height($row3_gb_bottom - $row1_gb_top + $padding);
$main->AU->Height($row7_gb_bottom - $row4_gb_top + $padding);
my $sb = $main->AddStatusBar();

my $icon = new Win32::GUI::Icon('tah.ico');
my $ni = $main->AddNotifyIcon(
    -name => "NI",
    -icon => $icon,
    -tip => "tiles\@Home",
);

my $ncw = $main->Width() - $main->ScaleWidth();
my $nch = $main->Height() - $main->ScaleHeight();
$main->Resize($ncw + $col1_width + $col2_width,
$nch + $main->PX->Top() + $main->PX->Height() + $main->AU->Height() + $padding + 20);

&toggle_proxy;
$main->TAHB3->Enable(0);

$main->Show(); #read registry if yes show
Win32::GUI::Dialog();

Win32::GUI::Show($DOS);

undef $main;
exit(0);

sub Main_Terminate {
	&configure_proxy;
	&configure_authentication;
	return -1;
}

sub Main_Resize {
	$sb->Move( 0, ($main->ScaleHeight() - $sb->Height()) );
	$sb->Resize( $main->ScaleWidth(), $sb->Height() );
	$sb->Text( "Window size: " . $main->Width() . "x" . $main->Height() );
	return 0;
}

sub Main_Minimize {
    $main->Disable();
    $main->Hide();
    return 1;
}

sub NI_Click {
    $main->Enable();
    $main->Show();
    return 1;
}

sub toggle_console {
        $console_visible =         $main->SCCB->Checked;
        if ($console_visible) {
		Win32::GUI::Show($DOS);
	} else {
        	Win32::GUI::Hide($DOS);
	}

}

sub toggle_proxy {
	my $pxcb_state = 	$main->PXCB->Checked;

	$main->PXHO->Enable($pxcb_state);
	$main->PXPO->Enable($pxcb_state);
	$main->PXUS->Enable($pxcb_state);
	$main->PXPA->Enable($pxcb_state);
	$main->PXEX->Enable($pxcb_state);
}

sub check_full {
        $text_to_check = $main->LB->Text;

        if (length($text_to_check) > ($main->LB->LimitText) * 3 / 4) {
                $main->LB->Text(substr($text_to_check,(($main->LB->LimitText)-($main->LB->LimitText)%2)/2));
        };
}

sub thread_tah {

	my $item = $main->BCB->GetCurSel();
    my $cmd = "\"".$dirperl."\\bin\\perl.exe\" ".$main->BCB->GetLBText($item);
#	print $item;
    if ($item == 2) {
	    $cmd =~ s/\.\.\.//;
	    $cmd .= $main->BCBP->Text();
    }

	chdir $dirtah;
	select STDERR; $| = 1;      # make unbuffered
	select STDOUT; $| = 1;      # make unbuffered
	select PH; $| = 1;      # make unbuffered
	my $pid = open3(gensym, \*PH, ">&STDOUT", $cmd);
	#my $pid = open3(gensym, \*PH, \*PH, $cmd);
	$main->LB->Text("Processing...\r\n");
	while( <PH> ) {
#	    print "$_";
		s/[\r\n]*$//;
		$main->LB->Append("$_\r\n");
		$main->LB->SendMessage (0x115, 7, 0);
		$main->DoEvents() >= 0 or die "Window was closed during processing";
		#sleep 1;
	}
	waitpid($pid, 0);
	$main->LB->Append("Completed!");
	$main->DoEvents();
	#sleep 1;
	$main->TAHB1->Enable(1);
	$main->TAHB2->Enable(1);
	$main->TAHB3->Enable(0);
}

sub start_tah {
	&configure_proxy;
	&configure_authentication;

	open(F,"<$tahconfigurationfileex");
	my @rows=<F>;
	s/[\r\n]+//g foreach(@rows);
	close(F);

	open(F,">$tahconfigurationfile");
	my $configstr = "";
	foreach (@rows) {
		$configstr = $_;
		$configstr =~ s/\\subversion/\\svn\\bin/;
		$configstr =~ s/c:\\program files/$dircur/;
		$configstr =~ s/c:\\temp/$dircur\\tmp/;
		$configstr =~ s/zip.exe/7za.exe/;
		$configstr =~ s/7zipWin=0/7zipWin=1/;

		$configstr =~ s/\# InkscapeLocale = german/InkscapeLocale = german/ if ($DecimalSeparator !~ /\./);

   		$configstr =~ s/Rasterizer=Inkscape/Rasterizer=Batik/ if ($batikRasterizer);

		#print "$configstr\n";
		print F "$configstr\n";
	}
	close(F);

	$main->TAHB1->Enable(0);
	$main->TAHB2->Enable(0);
	$main->TAHB3->Enable(1);

	unlink "$dirtah/stopfile.txt";

	threads->new(\&thread_tah);

}

sub thread_stop {
	#$main->TAHB1->Enable(1);
	#$main->TAHB2->Enable(1);
	#$main->TAHB3->Enable(0);
}

sub stop_tah {
	$main->TAHB3->Enable(0);
	open(FILE, ">$dirtah/stopfile.txt");
	close(FILE);
	#threads->new(\&thread_stop);
}

sub thread_svn {
	my $cmd = "\"$dirsvn\\bin\\svn.exe\" up";
	chdir $dirtah;
	select STDERR; $| = 1;      # make unbuffered
	select STDOUT; $| = 1;      # make unbuffered
	my $pid = open3(gensym, \*PH, ">&STDOUT", $cmd);
	#my $pid = open3(gensym, \*PH, \*PH, $cmd);
	$main->LB->Text("Processing...\r\n");
	while( <PH> ) {
#	    print "$_";
		s/[\r\n]*$//;
		$main->LB->Append("$_\r\n");
		$main->LB->SendMessage (0x115, 7, 0);
		$main->DoEvents() >= 0 or die "Window was closed during processing";
		#sleep 1;
	}
	waitpid($pid, 0);
	$main->LB->Append("Completed!");
	$main->TAHB1->Enable(1);
	$main->TAHB2->Enable(1);
	$main->TAHB3->Enable(0);

}
sub start_svn {
	$main->TAHB1->Enable(0);
	$main->TAHB2->Enable(0);

	threads->new(\&thread_svn);
}

sub configure_proxy {

	my $tahreg;
	my $type;
	my $garbage;

	$http_proxy_enabled = $main->PXCB->Checked;
	$http_proxy_exceptions = $main->PXEX->Text;
	$http_proxy_host = $main->PXHO->Text;
	$http_proxy_port = $main->PXPO->Text;
	$http_proxy_username = $main->PXUS->Text;
	$http_proxy_password = $main->PXPA->Text;

	$::HKEY_CURRENT_USER->Open("SOFTWARE\\openstreetmap.org\\tah", $tahreg) or die "Can't open proxy: $^E";
	$tahreg->SetValueEx("http_proxy_enable", $garbage, REG_SZ, $http_proxy_enabled);
	$tahreg->SetValueEx("http_proxy_exceptions", $garbage, REG_SZ, $http_proxy_exceptions);
	$tahreg->SetValueEx("http_proxy_host", $garbage, REG_SZ, $http_proxy_host);
	$tahreg->SetValueEx("http_proxy_port", $garbage, REG_SZ, $http_proxy_port);
	$tahreg->SetValueEx("http_proxy_username", $garbage, REG_SZ, $http_proxy_username);
	$tahreg->SetValueEx("http_proxy_password", $garbage, REG_SZ, $http_proxy_password);
	$tahreg->Close();

	open(F,"<$svnserverfile");
	my @servers=<F>;
	s/[\r\n]+//g foreach(@servers);
	close(F);

#	my $found_global = 0;
#	foreach (@servers) {
#		$found_global=0 if (/^\[/);
#		$found_global=1 if (/\[global\]/);
#		if ($found_global == 1) {
#			print $_."\n";
#			if (/^([\#\ ]*)http-proxy-host\s*=\s*(.*)/) {
#				$http_proxy_enabled = 0 if ($1 =~ /^\#/);
#				$http_proxy_host = $2;
#			}
#			$http_proxy_exceptions = $1 	if (/^.*http-proxy-exceptions\s*=\s*(.*)$/);
#			$http_proxy_port = $1 			if (/^.*http-proxy-port\s*=\s*(.*)$/);
#			$http_proxy_username = $1 		if (/^.*http-proxy-username\s*=\s*(.*)$/);
#			$http_proxy_password = $1 		if (/^.*http-proxy-password\s*=\s*(.*)$/);
#		}
#	}

	open(F,">$svnserverfile");
	my $pound = "# ";
	my $configstr = "";
	my $found_global = 0;
	$pound = "" if $http_proxy_enabled;
	foreach (@servers) {
		$configstr = $_;
		$found_global=0 if (/^\[/);
		$found_global=1 if (/\[global\]/);
		if ($found_global == 1) {
			$configstr = $pound.$1.$http_proxy_exceptions	if (/^.*(http-proxy-exceptions\s*=\s*)(.*)$/);
			$configstr = $pound.$1.$http_proxy_host	if (/^.*(http-proxy-host\s*=\s*)(.*)$/);
			$configstr = $pound.$1.$http_proxy_port	if (/^.*(http-proxy-port\s*=\s*)(.*)$/);
			$configstr = $pound.$1.$http_proxy_username	if (/^.*(http-proxy-username\s*=\s*)(.*)$/);
			$configstr = $pound.$1.$http_proxy_password	if (/^.*(http-proxy-password\s*=\s*)(.*)$/);
		}
		#print "$configstr\r\n";
		print F "$configstr\r\n";
	}
	close(F);

	if ($http_proxy_enabled) {
		$main->LB->Append("Exporting http_proxy...\r\n");
		$ENV{http_proxy}="http://$http_proxy_username:$http_proxy_password\@$http_proxy_host:$http_proxy_port/";
	} else {
		delete $ENV{http_proxy};
	}
}

sub configure_authentication {

	my $tahreg;
	my $type;
	my $garbage;

	$authentication_username = $main->AUUS->Text;
	$authentication_password = $main->AUPA->Text;

	$::HKEY_CURRENT_USER->Open("SOFTWARE\\openstreetmap.org\\tah", $tahreg) or die "Can't open proxy: $^E";
	$tahreg->SetValueEx("authentication_username", $garbage, REG_SZ, $authentication_username);
	$tahreg->SetValueEx("authentication_password", $garbage, REG_SZ, $authentication_password);
        $tahreg->SetValueEx("console_visible", $garbage, REG_SZ, $console_visible);
	$tahreg->Close();

	open(F,"<$tahauthenticationfileex");
	my @rows=<F>;
	s/[\r\n]+//g foreach(@rows);
	close(F);

	open(F,">$tahauthenticationfile");
	my $configstr = "";
	foreach (@rows) {
		$configstr = $_;
		$configstr = $1.$authentication_username	if (/^.*(UploadUsername\s*=\s*)(.*)$/);
		$configstr = $1.$authentication_password	if (/^.*(UploadPassword\s*=\s*)(.*)$/);
		#print "$configstr\n";
		print F "$configstr\n";
	}
	close(F);

}

__END__