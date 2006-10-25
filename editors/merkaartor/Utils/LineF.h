#ifndef MERKAARTOR_LINEF_
#define MERKAARTOR_LINEF_

#include <QtCore/QPointF>

#include <math.h>

inline double distance(const QPointF& A, const QPointF& B)
{
	double dx = A.x()-B.x();
	double dy = A.y()-B.y();
	return sqrt( dx*dx+dy*dy );
}

class LineF
{
public:
	LineF(const QPointF& aP1, const QPointF& aP2)
		: P1(aP1), P2(aP2), Valid(true)
	{
		A = P2.y()-P1.y();
		B = -P2.x()+P1.x();
		C = -P1.y()*B-P1.x()*A;
		double F = sqrt(A*A+B*B);
		if (F<0.0001)
			Valid=false;
		else
		{
			A/=F;
			B/=F;
			C/=F;
		}		
	}

	double distance(const QPointF& P) const
	{
		if (Valid)
			return fabs(A*P.x()+B*P.y()+C);
		else
			return sqrt( (P.x()-P1.x())*(P.x()-P1.x()) + (P.y()-P1.y())*(P.y()-P1.y()) );
	}

	double capDistance(const QPointF& P) const
	{
		if (Valid)
		{
			double dx = P2.x()-P1.x();
			double dy = P2.y()-P1.y();
			double px = P.x()-P1.x();
			double py = P.y()-P1.y();
			if ( (dx*px+dy*py) < 0)
				return ::distance(P,P1);
			px = P.x()-P2.x();
			py = P.y()-P2.y();
			if ( (dx*px+dy*py) > 0)
				return ::distance(P,P2);
			return fabs(A*P.x()+B*P.y()+C);
		}
		else
			return sqrt( (P.x()-A)*(P.x()-A) + (P.y()-B)*(P.y()-B) );
	}


private:
	QPointF P1, P2;
	bool Valid;
	double A,B,C;
};

#endif


