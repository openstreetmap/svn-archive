#ifndef MERKAARTOR_EXPORTOSM_H_
#define MERKAARTOR_EXPORTOSM_H_

class Relation;
class Road;
class TrackPoint;

#include <QtCore/QString>

QString exportOSM(const TrackPoint& Pt, const QString&  ChangesetId);
QString exportOSM(const Road& R, const QString&  ChangesetId);
QString exportOSM(const Relation& R, const QString&  ChangesetId);

QString wrapOSM(const QString& S, const QString& ChangeSetId);

#endif


