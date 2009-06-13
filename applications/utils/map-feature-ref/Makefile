SHELL = /bin/sh
FOP = fop
XSLTPROC = xsltproc

XSLT_SOURCES = mapfeatures-11-tab-sec-fo.xsl mapfeatures-1n-tab-sec-fo.xsl
FO_TARGETS = $(XSLT_SOURCES:-fo.xsl=.fo)
PDF_TARGETS = $(XSLT_SOURCES:-fo.xsl=.pdf)
PS_TARGETS = $(XSLT_SOURCES:-fo.xsl=.ps)


.PHONY: all pdf ps clean clean-pdf clean-ps

all: pdf ps

pdf: $(PDF_TARGETS)

ps: $(PS_TARGETS)

clean: clean-pdf clean-ps

clean-pdf:
	-rm $(PDF_TARGETS)

clean-ps:
	-rm $(PS_TARGETS)

dist-clean: clean
	-rm $(FO_TARGETS)


# Rules specific to mapfeatures

mapfeatures.xml:
	perl mapfeatures.xslfo.pl >$@

mapfeatures-%.fo: mapfeatures-%-fo.xsl mapfeatures.xml
	$(XSLTPROC) --output $@ $^


# General implicit rules for generating and processing formatting objects

%.pdf: %.fo
	$(FOP) -fo $< -pdf $@

%.ps: %.fo
	$(FOP) -fo $< -ps $@

%.fo: %-fo.xsl %.xml
	$(XSLTPROC) --output $@ $^


