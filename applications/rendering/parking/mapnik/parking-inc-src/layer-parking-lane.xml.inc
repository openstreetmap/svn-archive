<!-- no parking -->
<Style name="parkinglane-left-no">
    <!-- left parking lane: no_parking -->
    <Rule>
        &maxscale_zoom15;
        &minscale_zoom16;
        <Filter>[parking:lane:left]='no_parking'</Filter>
        <LinePatternSymbolizer file="&symbols;/park-ln-nopa.png"/>
    </Rule>
    <Rule>
        &maxscale_zoom17;
        <Filter>[parking:lane:left]='no_parking'</Filter>
        <LinePatternSymbolizer file="&symbols;/park-ln-nopa.png"/>
    </Rule>
    <!-- left parking lane: no_stopping -->
    <Rule>
        &maxscale_zoom15;
        &minscale_zoom16;
        <Filter>[parking:lane:left]='no_stopping'</Filter>
        <LinePatternSymbolizer file="&symbols;/park-ln-nost.png"/>
    </Rule>
    <Rule>
        &maxscale_zoom17;
        <Filter>[parking:lane:left]='no_stopping'</Filter>
        <LinePatternSymbolizer file="&symbols;/park-ln-nost.png"/>
    </Rule>
    <!-- left parking lane: fire_lane -->
    <Rule>
        &maxscale_zoom15;
        &minscale_zoom16;
        <Filter>[parking:lane:left]='fire_lane'</Filter>
        <LinePatternSymbolizer file="&symbols;/park-ln-fire.png"/>
    </Rule>
    <Rule>
        &maxscale_zoom17;
        <Filter>[parking:lane:left]='fire_lane'</Filter>
        <LinePatternSymbolizer file="&symbols;/park-ln-fire.png"/>
    </Rule>
</Style>
<Style name="parkinglane-right-no">
    <!-- right parking lane: no_parking -->
    <Rule>
        &maxscale_zoom15;
        &minscale_zoom16;
        <Filter>[parking:lane:right]='no_parking'</Filter>
        <LinePatternSymbolizer file="&symbols;/park-rn-nopa.png"/>
    </Rule>
    <Rule>
        &maxscale_zoom17;
        <Filter>[parking:lane:right]='no_parking'</Filter>
        <LinePatternSymbolizer file="&symbols;/park-rn-nopa.png"/>
    </Rule>
    <!-- right parking lane: no_stopping -->
    <Rule>
        &maxscale_zoom15;
        &minscale_zoom16;
        <Filter>[parking:lane:right]='no_stopping'</Filter>
        <LinePatternSymbolizer file="&symbols;/park-rn-nost.png"/>
    </Rule>
    <Rule>
        &maxscale_zoom17;
        <Filter>[parking:lane:right]='no_stopping'</Filter>
        <LinePatternSymbolizer file="&symbols;/park-rn-nost.png"/>
    </Rule>
    <!-- right parking lane: fire_lane -->
    <Rule>
        &maxscale_zoom15;
        &minscale_zoom16;
        <Filter>[parking:lane:right]='fire_lane'</Filter>
        <LinePatternSymbolizer file="&symbols;/park-rn-fire.png"/>
    </Rule>
    <Rule>
        &maxscale_zoom17;
        <Filter>[parking:lane:right]='fire_lane'</Filter>
        <LinePatternSymbolizer file="&symbols;/park-rn-fire.png"/>
    </Rule>
</Style>

<!-- parallel parking -->
<Style name="parkinglane-left-parallel">
    <!-- left parking lane: free -->
    <Rule>
        &maxscale_zoom15;
        &minscale_zoom16;
        <Filter>([parking:lane:left]='parallel' or [parking:lane:left]='inline') and [parking:condition:left]='free'</Filter>
        <LinePatternSymbolizer file="&symbols;/park-li-free.png"/>
    </Rule>
    <Rule>
        &maxscale_zoom17;
        <Filter>([parking:lane:left]='parallel' or [parking:lane:left]='inline') and [parking:condition:left]='free'</Filter>
        <LinePatternSymbolizer file="&symbols;/park-li-free.png"/>
    </Rule>
    <!-- left parking lane: disc -->
    <Rule>
        &maxscale_zoom15;
        &minscale_zoom16;
        <Filter>([parking:lane:left]='parallel' or [parking:lane:left]='inline') and [parking:condition:left]='disc'</Filter>
        <LinePatternSymbolizer file="&symbols;/park-li-disc.png"/>
    </Rule>
    <Rule>
        &maxscale_zoom17;
        <Filter>([parking:lane:left]='parallel' or [parking:lane:left]='inline') and [parking:condition:left]='disc'</Filter>
        <LinePatternSymbolizer file="&symbols;/park-li-disc.png"/>
    </Rule>
    <!-- left parking lane: ticket -->
    <Rule>
        &maxscale_zoom15;
        &minscale_zoom16;
        <Filter>([parking:lane:left]='parallel' or [parking:lane:left]='inline') and [parking:condition:left]='ticket'</Filter>
        <LinePatternSymbolizer file="&symbols;/park-li-fee.png"/>
    </Rule>
    <Rule>
        &maxscale_zoom17;
        <Filter>([parking:lane:left]='parallel' or [parking:lane:left]='inline') and [parking:condition:left]='ticket'</Filter>
        <LinePatternSymbolizer file="&symbols;/park-li-fee.png"/>
    </Rule>
    <!-- left parking lane: customers -->
    <Rule>
        &maxscale_zoom15;
        &minscale_zoom16;
        <Filter>([parking:lane:left]='parallel' or [parking:lane:left]='inline') and [parking:condition:left]='customers'</Filter>
        <LinePatternSymbolizer file="&symbols;/park-li-cust.png"/>
    </Rule>
    <Rule>
        &maxscale_zoom17;
        <Filter>([parking:lane:left]='parallel' or [parking:lane:left]='inline') and [parking:condition:left]='customers'</Filter>
        <LinePatternSymbolizer file="&symbols;/park-li-cust.png"/>
    </Rule>
    <!-- left parking lane: residents -->
    <Rule>
        &maxscale_zoom15;
        &minscale_zoom16;
        <Filter>([parking:lane:left]='parallel' or [parking:lane:left]='inline') and [parking:condition:left]='residents'</Filter>
        <LinePatternSymbolizer file="&symbols;/park-li-resi.png"/>
    </Rule>
    <Rule>
        &maxscale_zoom17;
        <Filter>([parking:lane:left]='parallel' or [parking:lane:left]='inline') and [parking:condition:left]='residents'</Filter>
        <LinePatternSymbolizer file="&symbols;/park-li-resi.png"/>
    </Rule>
    <!-- left parking lane: private -->
    <Rule>
        &maxscale_zoom15;
        &minscale_zoom16;
        <Filter>([parking:lane:left]='parallel' or [parking:lane:left]='inline') and [parking:condition:left]='private'</Filter>
        <LinePatternSymbolizer file="&symbols;/park-li-priv.png"/>
    </Rule>
    <Rule>
        &maxscale_zoom17;
        <Filter>([parking:lane:left]='parallel' or [parking:lane:left]='inline') and [parking:condition:left]='private'</Filter>
        <LinePatternSymbolizer file="&symbols;/park-li-priv.png"/>
    </Rule>
    <!-- left parking lane: unknown -->
    <Rule>
        &maxscale_zoom15;
        &minscale_zoom16;
        <Filter>([parking:lane:left]='parallel' or [parking:lane:left]='inline') and [parking:condition:left]='unknown'</Filter>
        <LinePatternSymbolizer file="&symbols;/park-li-unkn.png"/>
    </Rule>
    <Rule>
        &maxscale_zoom17;
        <Filter>([parking:lane:left]='parallel' or [parking:lane:left]='inline') and [parking:condition:left]='unknown'</Filter>
        <LinePatternSymbolizer file="&symbols;/park-li-unkn.png"/>
    </Rule>
</Style>
<Style name="parkinglane-right-parallel">
    <!-- right parking lane: free -->
    <Rule>
        &maxscale_zoom15;
        &minscale_zoom16;
        <Filter>([parking:lane:right]='parallel' or [parking:lane:right]='inline') and [parking:condition:right]='free'</Filter>
        <LinePatternSymbolizer file="&symbols;/park-ri-free.png"/>
    </Rule>
    <Rule>
        &maxscale_zoom17;
        <Filter>([parking:lane:right]='parallel' or [parking:lane:right]='inline') and [parking:condition:right]='free'</Filter>
        <LinePatternSymbolizer file="&symbols;/park-ri-free.png"/>
    </Rule>
    <!-- right parking lane: disc -->
    <Rule>
        &maxscale_zoom15;
        &minscale_zoom16;
        <Filter>([parking:lane:right]='parallel' or [parking:lane:right]='inline') and [parking:condition:right]='disc'</Filter>
        <LinePatternSymbolizer file="&symbols;/park-ri-disc.png"/>
    </Rule>
    <Rule>
        &maxscale_zoom17;
        <Filter>([parking:lane:right]='parallel' or [parking:lane:right]='inline') and [parking:condition:right]='disc'</Filter>
        <LinePatternSymbolizer file="&symbols;/park-ri-disc.png"/>
    </Rule>
    <!-- right parking lane: ticket -->
    <Rule>
        &maxscale_zoom15;
        &minscale_zoom16;
        <Filter>([parking:lane:right]='parallel' or [parking:lane:right]='inline') and [parking:condition:right]='ticket'</Filter>
        <LinePatternSymbolizer file="&symbols;/park-ri-fee.png"/>
    </Rule>
    <Rule>
        &maxscale_zoom17;
        <Filter>([parking:lane:right]='parallel' or [parking:lane:right]='inline') and [parking:condition:right]='ticket'</Filter>
        <LinePatternSymbolizer file="&symbols;/park-ri-fee.png"/>
    </Rule>
    <!-- right parking lane: customers -->
    <Rule>
        &maxscale_zoom15;
        &minscale_zoom16;
        <Filter>([parking:lane:right]='parallel' or [parking:lane:right]='inline') and [parking:condition:right]='customers'</Filter>
        <LinePatternSymbolizer file="&symbols;/park-ri-cust.png"/>
    </Rule>
    <Rule>
        &maxscale_zoom17;
        <Filter>([parking:lane:right]='parallel' or [parking:lane:right]='inline') and [parking:condition:right]='customers'</Filter>
        <LinePatternSymbolizer file="&symbols;/park-ri-cust.png"/>
    </Rule>
    <!-- right parking lane: residents -->
    <Rule>
        &maxscale_zoom15;
        &minscale_zoom16;
        <Filter>([parking:lane:right]='parallel' or [parking:lane:right]='inline') and [parking:condition:right]='residents'</Filter>
        <LinePatternSymbolizer file="&symbols;/park-ri-resi.png"/>
    </Rule>
    <Rule>
        &maxscale_zoom17;
        <Filter>([parking:lane:right]='parallel' or [parking:lane:right]='inline') and [parking:condition:right]='residents'</Filter>
        <LinePatternSymbolizer file="&symbols;/park-ri-resi.png"/>
    </Rule>
    <!-- right parking lane: private -->
    <Rule>
        &maxscale_zoom15;
        &minscale_zoom16;
        <Filter>([parking:lane:right]='parallel' or [parking:lane:right]='inline') and [parking:condition:right]='private'</Filter>
        <LinePatternSymbolizer file="&symbols;/park-ri-priv.png"/>
    </Rule>
    <Rule>
        &maxscale_zoom17;
        <Filter>([parking:lane:right]='parallel' or [parking:lane:right]='inline') and [parking:condition:right]='private'</Filter>
        <LinePatternSymbolizer file="&symbols;/park-ri-priv.png"/>
    </Rule>
    <!-- right parking lane: unknown -->
    <Rule>
        &maxscale_zoom15;
        &minscale_zoom16;
        <Filter>([parking:lane:right]='parallel' or [parking:lane:right]='inline') and [parking:condition:right]='unknown'</Filter>
        <LinePatternSymbolizer file="&symbols;/park-ri-unkn.png"/>
    </Rule>
    <Rule>
        &maxscale_zoom17;
        <Filter>([parking:lane:right]='parallel' or [parking:lane:right]='inline') and [parking:condition:right]='unknown'</Filter>
        <LinePatternSymbolizer file="&symbols;/park-ri-unkn.png"/>
    </Rule>
</Style>

<!-- diagonal parking -->
<Style name="parkinglane-left-diagonal">
    <!-- left parking lane: free -->
    <Rule>
        &maxscale_zoom15;
        &minscale_zoom16;
        <Filter>[parking:lane:left]='diagonal' and [parking:condition:left]='free'</Filter>
        <LinePatternSymbolizer file="&symbols;/park-ld-free.png"/>
    </Rule>
    <Rule>
        &maxscale_zoom17;
        <Filter>[parking:lane:left]='diagonal' and [parking:condition:left]='free'</Filter>
        <LinePatternSymbolizer file="&symbols;/park-ld-free.png"/>
    </Rule>
    <!-- left parking lane: disc -->
    <Rule>
        &maxscale_zoom15;
        &minscale_zoom16;
        <Filter>[parking:lane:left]='diagonal' and [parking:condition:left]='disc'</Filter>
        <LinePatternSymbolizer file="&symbols;/park-ld-disc.png"/>
    </Rule>
    <Rule>
        &maxscale_zoom17;
        <Filter>[parking:lane:left]='diagonal' and [parking:condition:left]='disc'</Filter>
        <LinePatternSymbolizer file="&symbols;/park-ld-disc.png"/>
    </Rule>
    <!-- left parking lane: ticket -->
    <Rule>
        &maxscale_zoom15;
        &minscale_zoom16;
        <Filter>[parking:lane:left]='diagonal' and [parking:condition:left]='ticket'</Filter>
        <LinePatternSymbolizer file="&symbols;/park-ld-fee.png"/>
    </Rule>
    <Rule>
        &maxscale_zoom17;
        <Filter>[parking:lane:left]='diagonal' and [parking:condition:left]='ticket'</Filter>
        <LinePatternSymbolizer file="&symbols;/park-ld-fee.png"/>
    </Rule>
    <!-- left parking lane: customers -->
    <Rule>
        &maxscale_zoom15;
        &minscale_zoom16;
        <Filter>[parking:lane:left]='diagonal' and [parking:condition:left]='customers'</Filter>
        <LinePatternSymbolizer file="&symbols;/park-ld-cust.png"/>
    </Rule>
    <Rule>
        &maxscale_zoom17;
        <Filter>[parking:lane:left]='diagonal' and [parking:condition:left]='customers'</Filter>
        <LinePatternSymbolizer file="&symbols;/park-ld-cust.png"/>
    </Rule>
    <!-- left parking lane: residents -->
    <Rule>
        &maxscale_zoom15;
        &minscale_zoom16;
        <Filter>[parking:lane:left]='diagonal' and [parking:condition:left]='residents'</Filter>
        <LinePatternSymbolizer file="&symbols;/park-ld-resi.png"/>
    </Rule>
    <Rule>
        &maxscale_zoom17;
        <Filter>[parking:lane:left]='diagonal' and [parking:condition:left]='residents'</Filter>
        <LinePatternSymbolizer file="&symbols;/park-ld-resi.png"/>
    </Rule>
    <!-- left parking lane: private -->
    <Rule>
        &maxscale_zoom15;
        &minscale_zoom16;
        <Filter>[parking:lane:left]='diagonal' and [parking:condition:left]='private'</Filter>
        <LinePatternSymbolizer file="&symbols;/park-ld-priv.png"/>
    </Rule>
    <Rule>
        &maxscale_zoom17;
        <Filter>[parking:lane:left]='diagonal' and [parking:condition:left]='private'</Filter>
        <LinePatternSymbolizer file="&symbols;/park-ld-priv.png"/>
    </Rule>
    <!-- left parking lane: unknown -->
    <Rule>
        &maxscale_zoom15;
        &minscale_zoom16;
        <Filter>[parking:lane:left]='diagonal' and [parking:condition:left]='unknown'</Filter>
        <LinePatternSymbolizer file="&symbols;/park-ld-unkn.png"/>
    </Rule>
    <Rule>
        &maxscale_zoom17;
        <Filter>[parking:lane:left]='diagonal' and [parking:condition:left]='unknown'</Filter>
        <LinePatternSymbolizer file="&symbols;/park-ld-unkn.png"/>
    </Rule>
</Style>
<Style name="parkinglane-right-diagonal">
    <!-- right parking lane: free -->
    <Rule>
        &maxscale_zoom15;
        &minscale_zoom16;
        <Filter>[parking:lane:right]='diagonal' and [parking:condition:right]='free'</Filter>
        <LinePatternSymbolizer file="&symbols;/park-rd-free.png"/>
    </Rule>
    <Rule>
        &maxscale_zoom17;
        <Filter>[parking:lane:right]='diagonal' and [parking:condition:right]='free'</Filter>
        <LinePatternSymbolizer file="&symbols;/park-rd-free.png"/>
    </Rule>
    <!-- right parking lane: disc -->
    <Rule>
        &maxscale_zoom15;
        &minscale_zoom16;
        <Filter>[parking:lane:right]='diagonal' and [parking:condition:right]='disc'</Filter>
        <LinePatternSymbolizer file="&symbols;/park-rd-disc.png"/>
    </Rule>
    <Rule>
        &maxscale_zoom17;
        <Filter>[parking:lane:right]='diagonal' and [parking:condition:right]='disc'</Filter>
        <LinePatternSymbolizer file="&symbols;/park-rd-disc.png"/>
    </Rule>
    <!-- right parking lane: ticket -->
    <Rule>
        &maxscale_zoom15;
        &minscale_zoom16;
        <Filter>[parking:lane:right]='diagonal' and [parking:condition:right]='ticket'</Filter>
        <LinePatternSymbolizer file="&symbols;/park-rd-fee.png"/>
    </Rule>
    <Rule>
        &maxscale_zoom17;
        <Filter>[parking:lane:right]='diagonal' and [parking:condition:right]='ticket'</Filter>
        <LinePatternSymbolizer file="&symbols;/park-rd-fee.png"/>
    </Rule>
    <!-- right parking lane: customers -->
    <Rule>
        &maxscale_zoom15;
        &minscale_zoom16;
        <Filter>[parking:lane:right]='diagonal' and [parking:condition:right]='customers'</Filter>
        <LinePatternSymbolizer file="&symbols;/park-rd-cust.png"/>
    </Rule>
    <Rule>
        &maxscale_zoom17;
        <Filter>[parking:lane:right]='diagonal' and [parking:condition:right]='customers'</Filter>
        <LinePatternSymbolizer file="&symbols;/park-rd-cust.png"/>
    </Rule>
    <!-- right parking lane: residents -->
    <Rule>
        &maxscale_zoom15;
        &minscale_zoom16;
        <Filter>[parking:lane:right]='diagonal' and [parking:condition:right]='residents'</Filter>
        <LinePatternSymbolizer file="&symbols;/park-rd-resi.png"/>
    </Rule>
    <Rule>
        &maxscale_zoom17;
        <Filter>[parking:lane:right]='diagonal' and [parking:condition:right]='residents'</Filter>
        <LinePatternSymbolizer file="&symbols;/park-rd-resi.png"/>
    </Rule>
    <!-- right parking lane: private -->
    <Rule>
        &maxscale_zoom15;
        &minscale_zoom16;
        <Filter>[parking:lane:right]='diagonal' and [parking:condition:right]='private'</Filter>
        <LinePatternSymbolizer file="&symbols;/park-rd-priv.png"/>
    </Rule>
    <Rule>
        &maxscale_zoom17;
        <Filter>[parking:lane:right]='diagonal' and [parking:condition:right]='private'</Filter>
        <LinePatternSymbolizer file="&symbols;/park-rd-priv.png"/>
    </Rule>
    <!-- right parking lane: unknown -->
    <Rule>
        &maxscale_zoom15;
        &minscale_zoom16;
        <Filter>[parking:lane:right]='diagonal' and [parking:condition:right]='unknown'</Filter>
        <LinePatternSymbolizer file="&symbols;/park-rd-unkn.png"/>
    </Rule>
    <Rule>
        &maxscale_zoom17;
        <Filter>[parking:lane:right]='diagonal' and [parking:condition:right]='unknown'</Filter>
        <LinePatternSymbolizer file="&symbols;/park-rd-unkn.png"/>
    </Rule>
</Style>

<!-- perpendicular parking -->
<Style name="parkinglane-left-perpendicular">
    <!-- left parking lane: free -->
    <Rule>
        &maxscale_zoom15;
        &minscale_zoom16;
        <Filter>([parking:lane:left]='perpendicular' or [parking:lane:left]='orthogonal') and [parking:condition:left]='free'</Filter>
        <LinePatternSymbolizer file="&symbols;/park-lo-free.png"/>
    </Rule>
    <Rule>
        &maxscale_zoom17;
        <Filter>([parking:lane:left]='perpendicular' or [parking:lane:left]='orthogonal') and [parking:condition:left]='free'</Filter>
        <LinePatternSymbolizer file="&symbols;/park-lo-free.png"/>
    </Rule>
    <!-- left parking lane: disc -->
    <Rule>
        &maxscale_zoom15;
        &minscale_zoom16;
        <Filter>([parking:lane:left]='perpendicular' or [parking:lane:left]='orthogonal') and [parking:condition:left]='disc'</Filter>
        <LinePatternSymbolizer file="&symbols;/park-lo-disc.png"/>
    </Rule>
    <Rule>
        &maxscale_zoom17;
        <Filter>([parking:lane:left]='perpendicular' or [parking:lane:left]='orthogonal') and [parking:condition:left]='disc'</Filter>
        <LinePatternSymbolizer file="&symbols;/park-lo-disc.png"/>
    </Rule>
    <!-- left parking lane: ticket -->
    <Rule>
        &maxscale_zoom15;
        &minscale_zoom16;
        <Filter>([parking:lane:left]='perpendicular' or [parking:lane:left]='orthogonal') and [parking:condition:left]='ticket'</Filter>
        <LinePatternSymbolizer file="&symbols;/park-lo-fee.png"/>
    </Rule>
    <Rule>
        &maxscale_zoom17;
        <Filter>([parking:lane:left]='perpendicular' or [parking:lane:left]='orthogonal') and [parking:condition:left]='ticket'</Filter>
        <LinePatternSymbolizer file="&symbols;/park-lo-fee.png"/>
    </Rule>
    <!-- left parking lane: customers -->
    <Rule>
        &maxscale_zoom15;
        &minscale_zoom16;
        <Filter>([parking:lane:left]='perpendicular' or [parking:lane:left]='orthogonal') and [parking:condition:left]='customers'</Filter>
        <LinePatternSymbolizer file="&symbols;/park-lo-cust.png"/>
    </Rule>
    <Rule>
        &maxscale_zoom17;
        <Filter>([parking:lane:left]='perpendicular' or [parking:lane:left]='orthogonal') and [parking:condition:left]='customers'</Filter>
        <LinePatternSymbolizer file="&symbols;/park-lo-cust.png"/>
    </Rule>
    <!-- left parking lane: residents -->
    <Rule>
        &maxscale_zoom15;
        &minscale_zoom16;
        <Filter>([parking:lane:left]='perpendicular' or [parking:lane:left]='orthogonal') and [parking:condition:left]='residents'</Filter>
        <LinePatternSymbolizer file="&symbols;/park-lo-resi.png"/>
    </Rule>
    <Rule>
        &maxscale_zoom17;
        <Filter>([parking:lane:left]='perpendicular' or [parking:lane:left]='orthogonal') and [parking:condition:left]='residents'</Filter>
        <LinePatternSymbolizer file="&symbols;/park-lo-resi.png"/>
    </Rule>
    <!-- left parking lane: private -->
    <Rule>
        &maxscale_zoom15;
        &minscale_zoom16;
        <Filter>([parking:lane:left]='perpendicular' or [parking:lane:left]='orthogonal') and [parking:condition:left]='private'</Filter>
        <LinePatternSymbolizer file="&symbols;/park-lo-priv.png"/>
    </Rule>
    <Rule>
        &maxscale_zoom17;
        <Filter>([parking:lane:left]='perpendicular' or [parking:lane:left]='orthogonal') and [parking:condition:left]='private'</Filter>
        <LinePatternSymbolizer file="&symbols;/park-lo-priv.png"/>
    </Rule>
    <!-- left parking lane: unknown -->
    <Rule>
        &maxscale_zoom15;
        &minscale_zoom16;
        <Filter>([parking:lane:left]='perpendicular' or [parking:lane:left]='orthogonal') and [parking:condition:left]='unknown'</Filter>
        <LinePatternSymbolizer file="&symbols;/park-lo-unkn.png"/>
    </Rule>
    <Rule>
        &maxscale_zoom17;
        <Filter>([parking:lane:left]='perpendicular' or [parking:lane:left]='orthogonal') and [parking:condition:left]='unknown'</Filter>
        <LinePatternSymbolizer file="&symbols;/park-lo-unkn.png"/>
    </Rule>
</Style>
<Style name="parkinglane-right-perpendicular">
    <!-- right parking lane: free -->
    <Rule>
        &maxscale_zoom15;
        &minscale_zoom16;
        <Filter>([parking:lane:right]='perpendicular' or [parking:lane:right]='orthogonal') and [parking:condition:right]='free'</Filter>
        <LinePatternSymbolizer file="&symbols;/park-ro-free.png"/>
    </Rule>
    <Rule>
        &maxscale_zoom17;
        <Filter>([parking:lane:right]='perpendicular' or [parking:lane:right]='orthogonal') and [parking:condition:right]='free'</Filter>
        <LinePatternSymbolizer file="&symbols;/park-ro-free.png"/>
    </Rule>
    <!-- right parking lane: disc -->
    <Rule>
        &maxscale_zoom15;
        &minscale_zoom16;
        <Filter>([parking:lane:right]='perpendicular' or [parking:lane:right]='orthogonal') and [parking:condition:right]='disc'</Filter>
        <LinePatternSymbolizer file="&symbols;/park-ro-disc.png"/>
    </Rule>
    <Rule>
        &maxscale_zoom17;
        <Filter>([parking:lane:right]='perpendicular' or [parking:lane:right]='orthogonal') and [parking:condition:right]='disc'</Filter>
        <LinePatternSymbolizer file="&symbols;/park-ro-disc.png"/>
    </Rule>
    <!-- right parking lane: ticket -->
    <Rule>
        &maxscale_zoom15;
        &minscale_zoom16;
        <Filter>([parking:lane:right]='perpendicular' or [parking:lane:right]='orthogonal') and [parking:condition:right]='ticket'</Filter>
        <LinePatternSymbolizer file="&symbols;/park-ro-fee.png"/>
    </Rule>
    <Rule>
        &maxscale_zoom17;
        <Filter>([parking:lane:right]='perpendicular' or [parking:lane:right]='orthogonal') and [parking:condition:right]='ticket'</Filter>
        <LinePatternSymbolizer file="&symbols;/park-ro-fee.png"/>
    </Rule>
    <!-- right parking lane: customers -->
    <Rule>
        &maxscale_zoom15;
        &minscale_zoom16;
        <Filter>([parking:lane:right]='perpendicular' or [parking:lane:right]='orthogonal') and [parking:condition:right]='customers'</Filter>
        <LinePatternSymbolizer file="&symbols;/park-ro-cust.png"/>
    </Rule>
    <Rule>
        &maxscale_zoom17;
        <Filter>([parking:lane:right]='perpendicular' or [parking:lane:right]='orthogonal') and [parking:condition:right]='customers'</Filter>
        <LinePatternSymbolizer file="&symbols;/park-ro-cust.png"/>
    </Rule>
    <!-- right parking lane: residents -->
    <Rule>
        &maxscale_zoom15;
        &minscale_zoom16;
        <Filter>([parking:lane:right]='perpendicular' or [parking:lane:right]='orthogonal') and [parking:condition:right]='residents'</Filter>
        <LinePatternSymbolizer file="&symbols;/park-ro-resi.png"/>
    </Rule>
    <Rule>
        &maxscale_zoom17;
        <Filter>([parking:lane:right]='perpendicular' or [parking:lane:right]='orthogonal') and [parking:condition:right]='residents'</Filter>
        <LinePatternSymbolizer file="&symbols;/park-ro-resi.png"/>
    </Rule>
    <!-- right parking lane: private -->
    <Rule>
        &maxscale_zoom15;
        &minscale_zoom16;
        <Filter>([parking:lane:right]='perpendicular' or [parking:lane:right]='orthogonal') and [parking:condition:right]='private'</Filter>
        <LinePatternSymbolizer file="&symbols;/park-ro-priv.png"/>
    </Rule>
    <Rule>
        &maxscale_zoom17;
        <Filter>([parking:lane:right]='perpendicular' or [parking:lane:right]='orthogonal') and [parking:condition:right]='private'</Filter>
        <LinePatternSymbolizer file="&symbols;/park-ro-priv.png"/>
    </Rule>
    <!-- right parking lane: unknown -->
    <Rule>
        &maxscale_zoom15;
        &minscale_zoom16;
        <Filter>([parking:lane:right]='perpendicular' or [parking:lane:right]='orthogonal') and [parking:condition:right]='unknown'</Filter>
        <LinePatternSymbolizer file="&symbols;/park-ro-unkn.png"/>
    </Rule>
    <Rule>
        &maxscale_zoom17;
        <Filter>([parking:lane:right]='perpendicular' or [parking:lane:right]='orthogonal') and [parking:condition:right]='unknown'</Filter>
        <LinePatternSymbolizer file="&symbols;/park-ro-unkn.png"/>
    </Rule>
</Style>


<!-- parking layers -->
<Layer name="parkinglane-left" srs="&osm2pgsql_projection;" status="1" clear-label-cache="0">
    <StyleName>parkinglane-left-diagonal</StyleName>
    <StyleName>parkinglane-left-perpendicular</StyleName>
    <StyleName>parkinglane-left-parallel</StyleName>
    <StyleName>parkinglane-left-no</StyleName>
    <Datasource>
        <Parameter name="table">
        (select way,
                COALESCE(tags->'parking:lane:left',tags->'parking:lane:both') as "parking:lane:left",
                COALESCE(tags->'parking:condition:left',tags->'parking:condition:both','unknown') as "parking:condition:left"
                from &prefix;_line
                where (tags ? 'parking:lane:left') or (tags ? 'parking:lane:both')
        ) as roads
        </Parameter>
        &datasource-settings;
    </Datasource>
</Layer>

<Layer name="parkinglane-right" srs="&osm2pgsql_projection;" status="1" clear-label-cache="0">
    <StyleName>parkinglane-right-diagonal</StyleName>
    <StyleName>parkinglane-right-perpendicular</StyleName>
    <StyleName>parkinglane-right-parallel</StyleName>
    <StyleName>parkinglane-right-no</StyleName>
    <Datasource>
        <Parameter name="table">
        (select way,
                COALESCE(tags->'parking:lane:right',tags->'parking:lane:both') as "parking:lane:right",
                COALESCE(tags->'parking:condition:right',tags->'parking:condition:both','unknown') as "parking:condition:right"
                from &prefix;_line
                where (tags ? 'parking:lane:right') or (tags ? 'parking:lane:both')
        ) as roads
        </Parameter>
        &datasource-settings;
    </Datasource>
</Layer>


<!-- maxstay layer -->

<Style name="parkinglane-maxstay-right">
    <!-- right parking lane: free -->
    <Rule>
        &maxscale_zoom15;
        <Filter>[parking:condition:right]='free'</Filter>
        <TextSymbolizer face-name="DejaVu Sans Bold" size="12" dy="15" placement="line" wrap-width="35" allow-overlap="1">'r:'+[maxstay]</TextSymbolizer>
    </Rule>
    <!-- right parking lane: disc -->
    <Rule>
        &maxscale_zoom15;
        <Filter>[parking:condition:right]='disc'</Filter>
        <TextSymbolizer face-name="DejaVu Sans Bold" size="12" dy="15" placement="line" wrap-width="35" allow-overlap="1">'r:'+[maxstay]</TextSymbolizer>
    </Rule>
    <!-- right parking lane: ticket -->
    <Rule>
        &maxscale_zoom15;
        <Filter>[parking:condition:right]='ticket'</Filter>
        <TextSymbolizer face-name="DejaVu Sans Bold" size="12" dy="15" placement="line" wrap-width="35" allow-overlap="1">'r:'+[maxstay]</TextSymbolizer>
    </Rule>
    <!-- right parking lane: customers -->
    <Rule>
        &maxscale_zoom15;
        <Filter>[parking:condition:right]='customers'</Filter>
        <TextSymbolizer face-name="DejaVu Sans Bold" size="12" dy="15" placement="line" wrap-width="35" allow-overlap="1">'r:'+[maxstay]</TextSymbolizer>
    </Rule>
    <!-- right parking lane: residents -->
    <Rule>
        &maxscale_zoom15;
        <Filter>[parking:condition:right]='residents'</Filter>
        <TextSymbolizer face-name="DejaVu Sans Bold" size="12" dy="15" placement="line" wrap-width="35" allow-overlap="1">'r:'+[maxstay]</TextSymbolizer>
    </Rule>
    <!-- right parking lane: private -->
    <Rule>
        &maxscale_zoom15;
        <Filter>[parking:condition:right]='private'</Filter>
        <TextSymbolizer face-name="DejaVu Sans Bold" size="12" dy="15" placement="line" wrap-width="35" allow-overlap="1">'r:'+[maxstay]</TextSymbolizer>
    </Rule>
    <!-- right parking lane: unknown -->
    <Rule>
        &maxscale_zoom15;
        <Filter>[parking:condition:right]='unknown'</Filter>
        <TextSymbolizer face-name="DejaVu Sans Bold" size="12" dy="15" placement="line" wrap-width="35" allow-overlap="1">'r:'+[maxstay]</TextSymbolizer>
    </Rule>
</Style>

<Layer name="parkinglane-maxstay-right" srs="&osm2pgsql_projection;" status="1" clear-label-cache="0">
    <StyleName>parkinglane-maxstay-right</StyleName>
    <Datasource>
        <Parameter name="table">
        (select way,
                COALESCE(tags->'parking:lane:right',tags->'parking:lane:both') as "parking:lane:right",
                COALESCE(tags->'parking:condition:right',tags->'parking:condition:both') as "parking:condition:right",
                COALESCE(tags->'parking:condition:right:maxstay',tags->'parking:condition:both:maxstay') as "maxstay"
                from &prefix;_line
                where ((tags ? 'parking:lane:right') or (tags ? 'parking:lane:both')) and (tags ? 'parking:condition:right:maxstay')
        ) as roads
        </Parameter>
        &datasource-settings;
    </Datasource>
</Layer>

<Style name="parkinglane-maxstay-left">
    <!-- left parking lane: free -->
    <Rule>
        &maxscale_zoom15;
        <Filter>[parking:condition:left]='free'</Filter>
        <TextSymbolizer face-name="DejaVu Sans Bold" size="12" dy="-15" placement="line" wrap-width="35" allow-overlap="1">'l:'+[maxstay]</TextSymbolizer>
    </Rule>
    <!-- left parking lane: disc -->
    <Rule>
        &maxscale_zoom15;
        <Filter>[parking:condition:left]='disc'</Filter>
        <TextSymbolizer face-name="DejaVu Sans Bold" size="12" dy="-15" placement="line" wrap-width="35" allow-overlap="1">'l:'+[maxstay]</TextSymbolizer>
    </Rule>
    <!-- left parking lane: ticket -->
    <Rule>
        &maxscale_zoom15;
        <Filter>[parking:condition:left]='ticket'</Filter>
        <TextSymbolizer face-name="DejaVu Sans Bold" size="12" dy="-15" placement="line" wrap-width="35" allow-overlap="1">'l:'+[maxstay]</TextSymbolizer>
    </Rule>
    <!-- left parking lane: customers -->
    <Rule>
        &maxscale_zoom15;
        <Filter>[parking:condition:left]='customers'</Filter>
        <TextSymbolizer face-name="DejaVu Sans Bold" size="12" dy="-15" placement="line" wrap-width="35" allow-overlap="1">'l:'+[maxstay]</TextSymbolizer>
    </Rule>
    <!-- left parking lane: residents -->
    <Rule>
        &maxscale_zoom15;
        <Filter>[parking:condition:left]='residents'</Filter>
        <TextSymbolizer face-name="DejaVu Sans Bold" size="12" dy="-15" placement="line" wrap-width="35" allow-overlap="1">'l:'+[maxstay]</TextSymbolizer>
    </Rule>
    <!-- left parking lane: private -->
    <Rule>
        &maxscale_zoom15;
        <Filter>[parking:condition:left]='private'</Filter>
        <TextSymbolizer face-name="DejaVu Sans Bold" size="12" dy="-15" placement="line" wrap-width="35" allow-overlap="1">'l:'+[maxstay]</TextSymbolizer>
    </Rule>
    <!-- left parking lane: unknown -->
    <Rule>
        &maxscale_zoom15;
        <Filter>[parking:condition:left]='unknown'</Filter>
        <TextSymbolizer face-name="DejaVu Sans Bold" size="12" dy="-15" placement="line" wrap-width="35" allow-overlap="1">'l:'+[maxstay]</TextSymbolizer>
    </Rule>
</Style>

<Layer name="parkinglane-maxstay-left" srs="&osm2pgsql_projection;" status="1" clear-label-cache="0">
    <StyleName>parkinglane-maxstay-left</StyleName>
    <Datasource>
        <Parameter name="table">
        (select way,
                COALESCE(tags->'parking:lane:left',tags->'parking:lane:both') as "parking:lane:left",
                COALESCE(tags->'parking:condition:left',tags->'parking:condition:both') as "parking:condition:left",
                COALESCE(tags->'parking:condition:left:maxstay',tags->'parking:condition:both:maxstay') as "maxstay"
                from &prefix;_line
                where ((tags ? 'parking:lane:left') or (tags ? 'parking:lane:both')) and (tags ? 'parking:condition:left:maxstay')
        ) as roads
        </Parameter>
        &datasource-settings;
    </Datasource>
</Layer>
