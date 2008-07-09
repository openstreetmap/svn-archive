#!/usr/bin/perl -w

use DBI;
use Storable qw(dclone);
use Data::Dumper;
use strict;
my $dbh = DBI->connect("DBI:mysql:database=tah","root","", { RaiseError => 1, AutoCommit => 0 });

use constant LAYER => 1;  # The 'layer' column in tiles_blank and the 'type' column in tiles_meta
use constant TEST => 0;
$Data::Dumper::Indent = 1;

my $stamp = time()-300;

my @work;
# Get tiles completes since the last "stamp" moment but more than 30 seconds old (so we don't go in mid-update
my $get_data = $dbh->prepare( "select x, y, unix_timestamp(date) from tiles_queue where status = 3 and date < from_unixtime(?) and date >= from_unixtime(?) and z=12 order by date limit 10" );
my($tb,$ta) = (0,0);

for(;;)
{
  if( not TEST )
  {
    sleep 3;
    print "Checking for work since $stamp\n";
    $get_data->execute( time()-30, $stamp );
    while( my($x,$y,$time) = $get_data->fetchrow_array )
    {
      $stamp = $time;
      push @work, [$x,$y];
    }
    $dbh->commit;
  }
  else
  {
    push @work, [1064,1608];
  }
  while(@work)
  {
    my($x,$y) = @{ shift @work };

    my ($b,$a) = optimise($x,$y,12,17);
    
    $tb += $b;
    $ta += $a;
    
    print "($x,$y) ($tb -> $ta) ";
    if( $tb > 0 )
    {
      printf "Savings %.2f%%     ", 100*(1- ($ta/$tb));
    }
    print "\n";
  }
  last if TEST;
}

sub optimise
{
  my($x,$y,$minlevel, $maxlevel) = @_;
  
  # Fetch all the relevent tile data from the database
  my $sth = $dbh->prepare( "select x,y,type from tiles_blank where x between ? and ? and y between ? and ? and z = ? and layer = ?" );
  my ($origdata,$origblanks) = FetchTileInfo( $sth, $x, $y, $minlevel, $maxlevel );

  print "Optimising ($x,$y,$minlevel-$maxlevel)\n";
  return (0,0) if $origblanks == 0;
  print "Found $origblanks blank tiles, root=",($origdata->[$minlevel][0][0]||-1),"\n";
  
  # Requires FORCE INDEX 'cause mysql is too dumb to work it out itself
  $sth = $dbh->prepare( "select x,y,type from tiles_meta FORCE INDEX(PRIMARY) where x between ? and ? and y between ? and ? and z = ? and type = ?" );
  my($tiledata, $tilecount) = FetchTileInfo( $sth, $x, $y, $minlevel, $maxlevel );

  print "Found $tilecount real tiles\n";
  
  my $rootvalue = -1;
  if( not defined $origdata->[$minlevel][0][0] )
  {
    my $sth = $dbh->prepare( "select type from tiles_blank where x = ? and y = ? and z = ? and layer = ?" );
    for( my $mz = 1; $mz <= $minlevel and $rootvalue == -1; $mz-- )
    {
      $sth->execute( $x >> $mz, $y >> $mz, $minlevel-$mz, 1 );
      if( my ($type) = $sth->fetchrow_array )
      { $rootvalue = $type }
    }
    print "Determined above root $rootvalue\n";
  }
  # Copy the data into a working copy
  my @data = @{ dclone( $origdata ) };
  
  # Apply the fallback machanism recursivly so every tile has a defined value
  Expand( \@data, $tiledata, $rootvalue, 0, 0, $minlevel, $maxlevel );
  my $test = Dumper( \@data );
  # Optimise all levels <maxlevel so that the type is determined by the type most represented in its decendants
  Optimise( \@data, 0, 0, $minlevel, $maxlevel );
  my $test3 = Dumper( \@data );
  
  my %commands = ( "i" => [], "u" => [], "d" => [] );
  MakeCommands( \@data, $origdata, $tiledata, \%commands, $rootvalue, 0, 0, $minlevel, $maxlevel );

  # Verification step
  Expand( \@data, $tiledata, $rootvalue, 0, 0, $minlevel, $maxlevel );
  my $test2 = Dumper( \@data );
  
#  print Dumper( \%commands );
  if( TEST or $test ne $test2 )
  {
    open my $fh1, ">/tmp/a";
    print $fh1 $test;
    open my $fh2, ">/tmp/b";
    print $fh2 $test2;
    open my $fh3, ">/tmp/c";
    print $fh3 $test3;
    
    print "=== DUMPED OUTPUT ===\n";
    return (0,0) unless TEST;
  }
  
  print "D/U/I: ",scalar( @{ $commands{d} } ),",",scalar( @{ $commands{u} } ),",",scalar( @{ $commands{i} } ),"\n";
  print "Before: ", $origblanks, " After: ", $origblanks+scalar( @{ $commands{i} } )-scalar( @{ $commands{d} } ), "\n";

#  ApplyCommands( \%commands, [$x,$y,$minlevel] );
  
  return($origblanks, $origblanks+scalar( @{ $commands{i} } )-scalar( @{ $commands{d} } ) );
}

# Fetches all the relevent tileinto for tile (x,y,minlevel) down to level maxlevel
# The database statement is given prepared, we just execute it
sub FetchTileInfo
{
  my($sth,$x,$y,$minlevel,$maxlevel) = @_;
  
  my @data;
  my $blanks = 0;
  for my $i ($minlevel..$maxlevel)
  {
    my $scale = 1 << ($i-$minlevel);
    
    $sth->execute( $x * $scale, ($x+1) * $scale - 1, 
                   $y * $scale, ($y+1) * $scale - 1,
                  $i, LAYER );
    $data[$i] = [];
    while( my($tx,$ty,$type) = $sth->fetchrow_array )
    {
      $tx -= $x * $scale;
      $ty -= $y * $scale;
      
#      print "-- $i,$tx,$ty = $type\n";
      $data[$i][$tx][$ty] = $type;
      $blanks++;
    }
  }
  
  return(\@data,$blanks);
}

# Apply the fallback mechanism, but remove entries where there is a real
# tile. So the result contains values for all the places that need them and
# -1 for the rest.
sub Expand
{
  my($data, $tiledata, $value, $x, $y, $currlevel, $maxlevel ) = @_;
  return if $currlevel > $maxlevel;
  if( defined $data->[$currlevel][$x][$y] and $data->[$currlevel][$x][$y] != -1 )
  {
    $value = $data->[$currlevel][$x][$y];
  }
  else
  {
    $data->[$currlevel][$x][$y] = $value;
  }
  Expand( $data, $tiledata, $value, 2*$x  , 2*$y  , $currlevel+1, $maxlevel );
  Expand( $data, $tiledata, $value, 2*$x+1, 2*$y  , $currlevel+1, $maxlevel );
  Expand( $data, $tiledata, $value, 2*$x  , 2*$y+1, $currlevel+1, $maxlevel );
  Expand( $data, $tiledata, $value, 2*$x+1, 2*$y+1, $currlevel+1, $maxlevel );
  
  if( defined $tiledata->[$currlevel][$x][$y] )
  { $data->[$currlevel][$x][$y] = -1 }
}

# Optimise the tree to make maximum use of fallback mechanism at lowest levels
sub Optimise
{
  my($data, $x, $y, $currlevel, $maxlevel ) = @_;
  # Return a count of one for final level
  if( $currlevel == $maxlevel )
  { return { $data->[$currlevel][$x][$y] => 1 } }
  
  my $s1 = Optimise( $data, 2*$x  , 2*$y  , $currlevel+1, $maxlevel );
  my $s2 = Optimise( $data, 2*$x+1, 2*$y  , $currlevel+1, $maxlevel );
  my $s3 = Optimise( $data, 2*$x  , 2*$y+1, $currlevel+1, $maxlevel );
  my $s4 = Optimise( $data, 2*$x+1, 2*$y+1, $currlevel+1, $maxlevel );
  
  my %res;
  
  # Sum the totals of each of the children
  map { my $a = $_; map { $res{$_} += $a->{$_} } keys %$a } ($s1,$s2,$s3,$s4);
  
  # If we have a choice for this point, choose the optimal
  if( $data->[$currlevel][$x][$y] == -1 )
  {
    # Find the type (>0) most represented
    my $max = undef;
    for my $key (keys %res)
    {
      next if $key == -1;
      if( not defined $max )
      { $max = $key; next }
      if( $res{$key} > $res{$max} )
      { $max = $key }
    }
    if( not defined $max )
    { $max = -1 }
    
    # Set this level to this type
    $data->[$currlevel][$x][$y] = $max;
  }
  # And return our totals
  return \%res;
}

# Determine the commands to get from the current state to the optimal state.
# At this point data is completely filled in (no -1's) with the optimal
# states, now the goal is to express this state in the minimal number of
# blank tiles. We recurse down the taking passing what we would get if the
# fallback mechanism were used.
sub MakeCommands
{
  my($data, $origdata, $tiledata, $commands, $value, $x, $y, $currlevel, $maxlevel ) = @_;
  return if $currlevel > $maxlevel;
  
  my $newvalue = $data->[$currlevel][$x][$y];
  my $oldvalue = $origdata->[$currlevel][$x][$y] || -1;
  
  if( $newvalue == $value )
  #and not( $currlevel == 15 and defined $tiledata->[$currlevel][$x][$y] ))  # If same, new value is "deleted"
  { $newvalue = $data->[$currlevel][$x][$y] = -1 }
  elsif( $newvalue != -1 )   # If the old value is forced, we must use it
  { $value = $newvalue }
  
  if( $newvalue != $oldvalue )
  {
    if( $newvalue == -1 and $oldvalue != -1 )
    { push @{$commands->{d}}, [$x,$y,$currlevel,$oldvalue] }
    elsif( $newvalue != -1 and $oldvalue == -1 )
    { push @{$commands->{i}}, [$x,$y,$currlevel,$newvalue] }
    else
    { push @{$commands->{u}}, [$x,$y,$currlevel,$newvalue] }
  }
  MakeCommands( $data, $origdata, $tiledata, $commands, $value, 2*$x  , 2*$y  , $currlevel+1, $maxlevel );
  MakeCommands( $data, $origdata, $tiledata, $commands, $value, 2*$x+1, 2*$y  , $currlevel+1, $maxlevel );
  MakeCommands( $data, $origdata, $tiledata, $commands, $value, 2*$x  , 2*$y+1, $currlevel+1, $maxlevel );
  MakeCommands( $data, $origdata, $tiledata, $commands, $value, 2*$x+1, 2*$y+1, $currlevel+1, $maxlevel );
}

sub Fix
{
  my ($x,$y,$minlevel) = @{$_[0]};
  my ($tx,$ty,$z,$type) = @{$_[1]};
  
  my $scale = 1 << ($z-$minlevel);
  return [$x * $scale + $tx, $y * $scale + $ty, $z, $type];
}

# Gives the calculated set of commands, commit them to the DB.
sub ApplyCommands
{
  my ($commands,$base) = @_;
  
  my $q = "DELETE FROM tiles_blank WHERE layer=? AND (%s)";
  
  my @args = map { sprintf "(x=%d and y=%d and z=%d)", @$_ } map { Fix($base,$_) } @{ $commands->{d} };
  
  $dbh->do( sprintf( $q, join(" OR ",@args)), undef, LAYER ) if @args;

  $q = "INSERT INTO tiles_blank (x,y,z,type,layer,user,date) VALUES %s";
  
  @args = map { sprintf "(%d,%d,%d,%d,%d,-1,now())", @$_, LAYER } map { Fix($base,$_) } @{ $commands->{i} };
  
  $dbh->do( sprintf $q, join(", ",@args) ) if @args;

  for my $type (0,1)
  {
    $q = "UPDATE tiles_blank set type=$type WHERE layer=? and (%s)";
    
    @args = map { sprintf "(x=%d and y=%d and z=%d)", @$_ } map { Fix($base,$_) } grep { $_->[3] == $type } @{ $commands->{u} };
    
    $dbh->do( sprintf( $q, join(", ",@args)), undef, LAYER ) if @args;
  }
  
  $dbh->commit;
}
