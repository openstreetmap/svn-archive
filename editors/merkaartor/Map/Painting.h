#ifndef MERKAARTOR_PAINTING_H_
#define MERKAARTOR_PAINTING_H_

class Projection;
class Way;

class QPainter;
class QPen;

void draw(QPainter& thePainter, QPen& thePen, Way* W, const Projection& theProjection);

#endif


