#ifndef MERKAARTOR_PAINTING_H_
#define MERKAARTOR_PAINTING_H_

class Projection;
class Way;

class QPainter;
class QPen;

/// draws way with oneway markers
void draw(QPainter& thePainter, QPen& thePen, Way* W, double theWidth, const Projection& theProjection);
/// draw way without oneway markers (as in focus)
void draw(QPainter& thePainter, QPen& thePen, Way* W, const Projection& theProjection);

#endif


