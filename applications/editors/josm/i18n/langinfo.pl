#! /usr/bin/perl -w

my $data;
foreach my $file (@ARGV)
{
  if(open FILE,"<:raw",$file)
  {
    my $miss = 0;
    my $missm = 0;
    my $i = 1;
    my $num = 0;
    for(;;)
    {
      read FILE,$data,2;
      my $len = unpack("n",$data);
      last if $len == 65535;
      if($len == 65534)
      {
        printf("%4d +++++\n", $i);
        ++$num;
      }
      elsif($len)
      {
        ++$num;
        read FILE,$data,$len;
        $data =~ s/[\r\n]/./g;
        printf("%4d %5d %.50s\n", $i, $len, $data);
      }
      else
      {
        printf("%4d -----\n", $i);
        ++$miss;
      }
      ++$i;
    }
    my $mul = 0;
    my $tot = 0;
    my $max = 0;
    my $comp = 0;
    print "multi:\n";
    $i = 1;
    for(;;)
    {
      last if !read FILE,$data,1;
      my $cnt = unpack("C",$data);
      ++$mul if $cnt;
      if($cnt == 0xFE)
      {
        ++$comp;
        $tot += 2;
        $cnt = 0;
        printf("%4d +++++\n",$i);
      }
      else
      {
        if($cnt > $max)
        {
          $comp = 0;
          $max = $cnt;
        }
        ++$comp if $cnt == $max;
        $tot += $cnt;
        printf("%4d -----\n",$i) if(!$cnt);
      }
      while($cnt--)
      {
        read FILE,$data,2;
        my $len = unpack("n",$data);
        if($len)
        {
          read FILE,$data,$len;
          $data =~ s/[\r\n]/./g;
          printf("%4d %5d %.50s\n", $i, $len, $data);
        }
        else
        {
          ++$missm;
        }
      }
      ++$i;
    }
    close FILE;
    printf("Status: Missing %d/%d - $num,$mul,$tot,$max,$comp\n",$miss,$missm);
  }
  else
  {
    print STDERR "Could not load language file $file.\n";
  }
}
