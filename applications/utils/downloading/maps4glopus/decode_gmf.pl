#! /usr/bin/perl -w

# GMF Format Definition von Peter Kirst
#
#

sub dump_file
{
    my ($file) = @_;
    my $buffer;
    my @Hex;

    print "Reading : $file\n";

    open (IN, "< $file") || die ("Can't open $file: $!\n");
    binmode(IN);
    
    # DWORD Version (momentan 0xff000002)
    read(IN, $buffer, 4);
    @Hex = unpack("H*", $buffer);
    print "DWORD Version : @Hex\n";

    read(IN, $buffer, 4);
    @Hex = unpack("H*", $buffer);
    $num_tiles = unpack("I*", $buffer);
    print "DWORD Anzahl Kacheln: $num_tiles / @Hex\n";

    for ($tile = 0; $tile < $num_tiles; $tile++)
    {
        print "\n";

        read(IN, $buffer, 4);
        @Hex = unpack("H*", $buffer);
        $laenge_kartenname = unpack("I*", $buffer);
        print "    $tile : DWORD Laenge Kartenname : $laenge_kartenname / @Hex\n";

        read(IN, $buffer, $laenge_kartenname * 2);
        @Hex = unpack("A*", $buffer);
        print "    $tile : wchar* Kartenname : @Hex\n";

        # DWORD Startposition der Bilddatei im GMF
        read(IN, $buffer, 4);
        @Hex = unpack("H*", $buffer);
        $bild_startpos = unpack("I*", $buffer);
        print "    $tile : DWORD Bild Startposition : $bild_startpos / @Hex\n";

        # DWORD Größe in x
        read(IN, $buffer, 4);
        @Hex = unpack("H*", $buffer);
        $bild_size_x = unpack("I*", $buffer);
        print "    $tile : DWORD Bild Groesse x : $bild_size_x / @Hex\n";

        # DWORD Größe in y
        read(IN, $buffer, 4);
        @Hex = unpack("H*", $buffer);
        $bild_size_y = unpack("I*", $buffer);
        print "    $tile : DWORD Bild Groesse y : $bild_size_y / @Hex\n";

        # DWORD Anzahl Kalibrierungspunkte // die nächsten 4 Werte entsprechend der Anzahl
        read(IN, $buffer, 4);
        @Hex = unpack("H*", $buffer);
        $num_cals = unpack("I*", $buffer);
        print "    $tile : DWORD Anzahl Kalibrierungspunkte : $num_cals / @Hex\n";    

        for ($cal = 0; $cal < $num_cals; $cal++)
        {
            print "\n";

            # DWORD Kalibrierungspunkt x
            read(IN, $buffer, 4);
            @Hex = unpack("H*", $buffer);
            $cal_x = unpack("I*", $buffer);
            print "    $tile : $cal: DWORD Kalibrierungspunkt x : $cal_x / @Hex\n";

            # DWORD Kalibrierungspunkt y
            read(IN, $buffer, 4);
            @Hex = unpack("H*", $buffer);
            $cal_y = unpack("I*", $buffer);
            print "    $tile : $cal: DWORD Kalibrierungspunkt y : $cal_y / @Hex\n";

            # double Lon
            read(IN, $buffer, 8);
            @Hex = unpack("H*", $buffer);
            $lon = unpack("I*", $buffer);
            print "    $tile : $cal: DWORD Kalibrierungspunkt Longitude : $lon / @Hex\n";

            # double Lat
            read(IN, $buffer, 8);
            @Hex = unpack("H*", $buffer);
            $lat = unpack("I*", $buffer);
            print "    $tile : $cal: DWORD Kalibrierungspunkt Latitude : $lat / @Hex\n";
        }
    }

    print "\n";

    close (IN);
}

dump_file("by-gmf.gmf");

dump_file("by-gmfgenerate.gmf");
