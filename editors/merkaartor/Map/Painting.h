#ifndef MERKAARTOR_PAINTING_H_
#define MERKAARTOR_PAINTING_H_

class Projection;
class Road;
class Way;

class QPainter;
class QPen;

/// draws way with oneway markers
void draw(QPainter& thePainter, QPen& thePen, Way* W, double theWidth, const Projection& theProjection);
/// draw way without oneway markers (as in focus)
void draw(QPainter& thePainter, QPen& thePen, Way* W, const Projection& theProjection);
/// draws a road as an area if appropriate tags are set
void drawPossibleArea(QPainter& thePainter, Road* R, const Projection& theProjection);

#endif


