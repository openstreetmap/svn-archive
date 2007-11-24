struct attachedWays * attachway(struct attachedWays * p, struct ways * s);
struct attachedWays * detachway(struct attachedWays * p, struct ways * s);
struct attachedRels * attachrelation(struct attachedRels * p, struct relations * s);
void saveRelations();
void addWay2Relation( struct ways *w, struct relations *rel);
void addNode2Relation( struct nodes *w,  struct relations *rel);
void deleteRelation(struct relations * p);
struct relations * addRelation();
void init_relations();


