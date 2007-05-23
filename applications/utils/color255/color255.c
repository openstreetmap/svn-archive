#include <stdio.h>
#include <stdlib.h>
#include <wand/magick-wand.h>

int main(int argc,char **argv)
{
#define ThrowWandException(wand) \
{ \
  char \
    *description; \
 \
  ExceptionType \
    severity; \
 \
  description=MagickGetException(wand,&severity); \
  (void) fprintf(stderr,"%s %s %ld %s\n",GetMagickModule(),description); \
  description=(char *) MagickRelinquishMemory(description); \
  exit(-1); \
}

  MagickBooleanType status;
  MagickWand  *magick_wand;
  int i;

  if (argc == 1) {
	fprintf(stderr, "Usage error:\n\t%s <image1.png> [image2.png] ...\n", argv[0]);
	exit(1);
  }

  MagickWandGenesis();
  magick_wand=NewMagickWand();  

  for (i=1; i<argc; i++) {
	const char *name = argv[i];
	status=MagickReadImage(magick_wand, name);
	if (status == MagickFalse)
		ThrowWandException(magick_wand);
	//printf("Converting: %s\n", name);
	
	MagickResetIterator(magick_wand);

#if 0
  MagickBooleanType MagickQuantizeImage(MagickWand *wand,
    const unsigned long number_colors,const ColorspaceType colorspace,
    const unsigned long treedepth,const MagickBooleanType dither,
    const MagickBooleanType measure_error)
#endif

	status=MagickQuantizeImage(magick_wand, 255, RGBColorspace, 0, 0, 0);
	if (status == MagickFalse)
		ThrowWandException(magick_wand);

	status=MagickWriteImages(magick_wand, name, MagickTrue);
  	if (status == MagickFalse)
    		ThrowWandException(magick_wand);
	
	ClearMagickWand(magick_wand);
  }

  magick_wand=DestroyMagickWand(magick_wand);

  MagickWandTerminus();
  return(0);
}
