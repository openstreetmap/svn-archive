use GD;
use strict;
use parseFlashSequence;
use renderFlashSequence;

sub flashSequenceToGifAnim
{
  my($Seq, $GifFile) = @_;
  
  my %Palette = (
          white=>[255,255,255],
          black=>[0,0,0],
          red=>[255,128,128],
          yellow=>[255,255,64],
          green=>[64,255,64],
          blue=>[64,64,255]);
          
  my $im = new GD::Image(40,40);
  
  my %Colours;
  while(my($col,$rgb) = each(%Palette))
  {
    $Colours{$col} = $im->colorAllocate($rgb->[0],$rgb->[1],$rgb->[2]);
    $im->filledRectangle(0,0,39,39,$Colours{$col});
  }
  
  my $gif = $im->gifanimbegin(1,0);
  
  $im->filledRectangle(0,0,39,39,$Colours{black});
  
  foreach my $Part(split(/\|/, $Seq))
  {
    my ($col, $len) = split(/,/, $Part);
    
    $im->filledRectangle(10,10,30,30,$Colours{$col});
    $gif .= $im->gifanimadd(1,0,0,int(20 * $len));
  
  }
  
  $gif .= $im->gifanimend();
  
  open OUT, ">$GifFile";
  print OUT $gif;
  close OUT;
}
1
