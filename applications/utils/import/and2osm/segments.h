struct attachedWays * attachway(struct attachedWays * p, struct ways * s);
struct attachedWays * detachway(struct attachedWays * p, struct ways * s);
void saveSegments();
void deleteSegment(struct segments * p);
struct segments * addSegment(struct nodes * from, struct nodes * to);
void init_segments();


