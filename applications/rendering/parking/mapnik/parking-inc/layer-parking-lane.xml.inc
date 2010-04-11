<Style name="parkinglane-left-no">
    <!-- left parking lane: no_parking -->
    <Rule>
        &maxscale_zoom15;
        &minscale_zoom16;
        <Filter>[parking:lane:left]='no_parking'</Filter>
        <LinePatternSymbolizer file="&symbols;/park-ln-nopa.png" type="png" width="16" height="16"></LinePatternSymbolizer>
    </Rule>
    <Rule>
        &maxscale_zoom17;
        <Filter>[parking:lane:left]='no_parking'</Filter>
        <LinePatternSymbolizer file="&symbols;/park-ln-nopa.png" type="png" width="16" height="16"></LinePatternSymbolizer>
    </Rule>
    <!-- left parking lane: no_stopping -->
    <Rule>
        &maxscale_zoom15;
        &minscale_zoom16;
        <Filter>[parking:lane:left]='no_stopping'</Filter>
        <LinePatternSymbolizer file="&symbols;/park-ln-nost.png" type="png" width="16" height="16"></LinePatternSymbolizer>
    </Rule>
    <Rule>
        &maxscale_zoom17;
        <Filter>[parking:lane:left]='no_stopping'</Filter>
        <LinePatternSymbolizer file="&symbols;/park-ln-nost.png" type="png" width="16" height="16"></LinePatternSymbolizer>
    </Rule>
    <!-- left parking lane: fire_lane -->
    <Rule>
        &maxscale_zoom15;
        &minscale_zoom16;
        <Filter>[parking:lane:left]='fire_lane'</Filter>
        <LinePatternSymbolizer file="&symbols;/park-ln-fire.png" type="png" width="16" height="16"></LinePatternSymbolizer>
    </Rule>
    <Rule>
        &maxscale_zoom17;
        <Filter>[parking:lane:left]='fire_lane'</Filter>
        <LinePatternSymbolizer file="&symbols;/park-ln-fire.png" type="png" width="16" height="16"></LinePatternSymbolizer>
    </Rule>
</Style>
<Style name="parkinglane-right-no">
    <!-- right parking lane: no_parking -->
    <Rule>
        &maxscale_zoom15;
        &minscale_zoom16;
        <Filter>[parking:lane:right]='no_parking'</Filter>
        <LinePatternSymbolizer file="&symbols;/park-rn-nopa.png" type="png" width="16" height="16"></LinePatternSymbolizer>
    </Rule>
    <Rule>
        &maxscale_zoom17;
        <Filter>[parking:lane:right]='no_parking'</Filter>
        <LinePatternSymbolizer file="&symbols;/park-rn-nopa.png" type="png" width="16" height="16"></LinePatternSymbolizer>
    </Rule>
    <!-- right parking lane: no_stopping -->
    <Rule>
        &maxscale_zoom15;
        &minscale_zoom16;
        <Filter>[parking:lane:right]='no_stopping'</Filter>
        <LinePatternSymbolizer file="&symbols;/park-rn-nost.png" type="png" width="16" height="16"></LinePatternSymbolizer>
    </Rule>
    <Rule>
        &maxscale_zoom17;
        <Filter>[parking:lane:right]='no_stopping'</Filter>
        <LinePatternSymbolizer file="&symbols;/park-rn-nost.png" type="png" width="16" height="16"></LinePatternSymbolizer>
    </Rule>
    <!-- right parking lane: fire_lane -->
    <Rule>
        &maxscale_zoom15;
        &minscale_zoom16;
        <Filter>[parking:lane:right]='fire_lane'</Filter>
        <LinePatternSymbolizer file="&symbols;/park-rn-fire.png" type="png" width="16" height="16"></LinePatternSymbolizer>
    </Rule>
    <Rule>
        &maxscale_zoom17;
        <Filter>[parking:lane:right]='fire_lane'</Filter>
        <LinePatternSymbolizer file="&symbols;/park-rn-fire.png" type="png" width="16" height="16"></LinePatternSymbolizer>
    </Rule>
</Style>

<Style name="parkinglane-left-inline">
    <!-- left parking lane: free -->
    <Rule>
        &maxscale_zoom15;
        &minscale_zoom16;
        <Filter>[parking:lane:left]='inline' and [parking:condition:left]='free'</Filter>
        <LinePatternSymbolizer file="&symbols;/park-li-free.png" type="png" width="16" height="16"></LinePatternSymbolizer>
    </Rule>
    <Rule>
        &maxscale_zoom17;
        <Filter>[parking:lane:left]='inline' and [parking:condition:left]='free'</Filter>
        <LinePatternSymbolizer file="&symbols;/park-li-free.png" type="png" width="16" height="16"></LinePatternSymbolizer>
    </Rule>
    <!-- left parking lane: disc -->
    <Rule>
        &maxscale_zoom15;
        &minscale_zoom16;
        <Filter>[parking:lane:left]='inline' and [parking:condition:left]='disc'</Filter>
        <LinePatternSymbolizer file="&symbols;/park-li-disc.png" type="png" width="16" height="16"></LinePatternSymbolizer>
    </Rule>
    <Rule>
        &maxscale_zoom17;
        <Filter>[parking:lane:left]='inline' and [parking:condition:left]='disc'</Filter>
        <LinePatternSymbolizer file="&symbols;/park-li-disc.png" type="png" width="16" height="16"></LinePatternSymbolizer>
    </Rule>
    <!-- left parking lane: ticket -->
    <Rule>
        &maxscale_zoom15;
        &minscale_zoom16;
        <Filter>[parking:lane:left]='inline' and [parking:condition:left]='ticket'</Filter>
        <LinePatternSymbolizer file="&symbols;/park-li-fee.png" type="png" width="16" height="16"></LinePatternSymbolizer>
    </Rule>
    <Rule>
        &maxscale_zoom17;
        <Filter>[parking:lane:left]='inline' and [parking:condition:left]='ticket'</Filter>
        <LinePatternSymbolizer file="&symbols;/park-li-fee.png" type="png" width="16" height="16"></LinePatternSymbolizer>
    </Rule>
    <!-- left parking lane: customers -->
    <Rule>
        &maxscale_zoom15;
        &minscale_zoom16;
        <Filter>[parking:lane:left]='inline' and [parking:condition:left]='customers'</Filter>
        <LinePatternSymbolizer file="&symbols;/park-li-cust.png" type="png" width="16" height="16"></LinePatternSymbolizer>
    </Rule>
    <Rule>
        &maxscale_zoom17;
        <Filter>[parking:lane:left]='inline' and [parking:condition:left]='customers'</Filter>
        <LinePatternSymbolizer file="&symbols;/park-li-cust.png" type="png" width="16" height="16"></LinePatternSymbolizer>
    </Rule>
    <!-- left parking lane: residents -->
    <Rule>
        &maxscale_zoom15;
        &minscale_zoom16;
        <Filter>[parking:lane:left]='inline' and [parking:condition:left]='residents'</Filter>
        <LinePatternSymbolizer file="&symbols;/park-li-resi.png" type="png" width="16" height="16"></LinePatternSymbolizer>
    </Rule>
    <Rule>
        &maxscale_zoom17;
        <Filter>[parking:lane:left]='inline' and [parking:condition:left]='residents'</Filter>
        <LinePatternSymbolizer file="&symbols;/park-li-resi.png" type="png" width="16" height="16"></LinePatternSymbolizer>
    </Rule>
    <!-- left parking lane: private -->
    <Rule>
        &maxscale_zoom15;
        &minscale_zoom16;
        <Filter>[parking:lane:left]='inline' and [parking:condition:left]='private'</Filter>
        <LinePatternSymbolizer file="&symbols;/park-li-priv.png" type="png" width="16" height="16"></LinePatternSymbolizer>
    </Rule>
    <Rule>
        &maxscale_zoom17;
        <Filter>[parking:lane:left]='inline' and [parking:condition:left]='private'</Filter>
        <LinePatternSymbolizer file="&symbols;/park-li-priv.png" type="png" width="16" height="16"></LinePatternSymbolizer>
    </Rule>
</Style>
<Style name="parkinglane-right-inline">
    <!-- right parking lane: free -->
    <Rule>
        &maxscale_zoom15;
        &minscale_zoom16;
        <Filter>[parking:lane:right]='inline' and [parking:condition:right]='free'</Filter>
        <LinePatternSymbolizer file="&symbols;/park-ri-free.png" type="png" width="16" height="16"></LinePatternSymbolizer>
    </Rule>
    <Rule>
        &maxscale_zoom17;
        <Filter>[parking:lane:right]='inline' and [parking:condition:right]='free'</Filter>
        <LinePatternSymbolizer file="&symbols;/park-ri-free.png" type="png" width="16" height="16"></LinePatternSymbolizer>
    </Rule>
    <!-- right parking lane: disc -->
    <Rule>
        &maxscale_zoom15;
        &minscale_zoom16;
        <Filter>[parking:lane:right]='inline' and [parking:condition:right]='disc'</Filter>
        <LinePatternSymbolizer file="&symbols;/park-ri-disc.png" type="png" width="16" height="16"></LinePatternSymbolizer>
    </Rule>
    <Rule>
        &maxscale_zoom17;
        <Filter>[parking:lane:right]='inline' and [parking:condition:right]='disc'</Filter>
        <LinePatternSymbolizer file="&symbols;/park-ri-disc.png" type="png" width="16" height="16"></LinePatternSymbolizer>
    </Rule>
    <!-- right parking lane: ticket -->
    <Rule>
        &maxscale_zoom15;
        &minscale_zoom16;
        <Filter>[parking:lane:right]='inline' and [parking:condition:right]='ticket'</Filter>
        <LinePatternSymbolizer file="&symbols;/park-ri-fee.png" type="png" width="16" height="16"></LinePatternSymbolizer>
    </Rule>
    <Rule>
        &maxscale_zoom17;
        <Filter>[parking:lane:right]='inline' and [parking:condition:right]='ticket'</Filter>
        <LinePatternSymbolizer file="&symbols;/park-ri-fee.png" type="png" width="16" height="16"></LinePatternSymbolizer>
    </Rule>
    <!-- right parking lane: customers -->
    <Rule>
        &maxscale_zoom15;
        &minscale_zoom16;
        <Filter>[parking:lane:right]='inline' and [parking:condition:right]='customers'</Filter>
        <LinePatternSymbolizer file="&symbols;/park-ri-cust.png" type="png" width="16" height="16"></LinePatternSymbolizer>
    </Rule>
    <Rule>
        &maxscale_zoom17;
        <Filter>[parking:lane:right]='inline' and [parking:condition:right]='customers'</Filter>
        <LinePatternSymbolizer file="&symbols;/park-ri-cust.png" type="png" width="16" height="16"></LinePatternSymbolizer>
    </Rule>
    <!-- right parking lane: residents -->
    <Rule>
        &maxscale_zoom15;
        &minscale_zoom16;
        <Filter>[parking:lane:right]='inline' and [parking:condition:right]='residents'</Filter>
        <LinePatternSymbolizer file="&symbols;/park-ri-resi.png" type="png" width="16" height="16"></LinePatternSymbolizer>
    </Rule>
    <Rule>
        &maxscale_zoom17;
        <Filter>[parking:lane:right]='inline' and [parking:condition:right]='residents'</Filter>
        <LinePatternSymbolizer file="&symbols;/park-ri-resi.png" type="png" width="16" height="16"></LinePatternSymbolizer>
    </Rule>
    <!-- right parking lane: private -->
    <Rule>
        &maxscale_zoom15;
        &minscale_zoom16;
        <Filter>[parking:lane:right]='inline' and [parking:condition:right]='private'</Filter>
        <LinePatternSymbolizer file="&symbols;/park-ri-priv.png" type="png" width="16" height="16"></LinePatternSymbolizer>
    </Rule>
    <Rule>
        &maxscale_zoom17;
        <Filter>[parking:lane:right]='inline' and [parking:condition:right]='private'</Filter>
        <LinePatternSymbolizer file="&symbols;/park-ri-priv.png" type="png" width="16" height="16"></LinePatternSymbolizer>
    </Rule>
</Style>

<Style name="parkinglane-left-diagonal">
    <!-- left parking lane: free -->
    <Rule>
        &maxscale_zoom15;
        &minscale_zoom16;
        <Filter>[parking:lane:left]='diagonal' and [parking:condition:left]='free'</Filter>
        <LinePatternSymbolizer file="&symbols;/park-ld-free.png" type="png" width="16" height="16"></LinePatternSymbolizer>
    </Rule>
    <Rule>
        &maxscale_zoom17;
        <Filter>[parking:lane:left]='diagonal' and [parking:condition:left]='free'</Filter>
        <LinePatternSymbolizer file="&symbols;/park-ld-free.png" type="png" width="16" height="16"></LinePatternSymbolizer>
    </Rule>
    <!-- left parking lane: disc -->
    <Rule>
        &maxscale_zoom15;
        &minscale_zoom16;
        <Filter>[parking:lane:left]='diagonal' and [parking:condition:left]='disc'</Filter>
        <LinePatternSymbolizer file="&symbols;/park-ld-disc.png" type="png" width="16" height="16"></LinePatternSymbolizer>
    </Rule>
    <Rule>
        &maxscale_zoom17;
        <Filter>[parking:lane:left]='diagonal' and [parking:condition:left]='disc'</Filter>
        <LinePatternSymbolizer file="&symbols;/park-ld-disc.png" type="png" width="16" height="16"></LinePatternSymbolizer>
    </Rule>
    <!-- left parking lane: ticket -->
    <Rule>
        &maxscale_zoom15;
        &minscale_zoom16;
        <Filter>[parking:lane:left]='diagonal' and [parking:condition:left]='ticket'</Filter>
        <LinePatternSymbolizer file="&symbols;/park-ld-fee.png" type="png" width="16" height="16"></LinePatternSymbolizer>
    </Rule>
    <Rule>
        &maxscale_zoom17;
        <Filter>[parking:lane:left]='diagonal' and [parking:condition:left]='ticket'</Filter>
        <LinePatternSymbolizer file="&symbols;/park-ld-fee.png" type="png" width="16" height="16"></LinePatternSymbolizer>
    </Rule>
    <!-- left parking lane: customers -->
    <Rule>
        &maxscale_zoom15;
        &minscale_zoom16;
        <Filter>[parking:lane:left]='diagonal' and [parking:condition:left]='customers'</Filter>
        <LinePatternSymbolizer file="&symbols;/park-ld-cust.png" type="png" width="16" height="16"></LinePatternSymbolizer>
    </Rule>
    <Rule>
        &maxscale_zoom17;
        <Filter>[parking:lane:left]='diagonal' and [parking:condition:left]='customers'</Filter>
        <LinePatternSymbolizer file="&symbols;/park-ld-cust.png" type="png" width="16" height="16"></LinePatternSymbolizer>
    </Rule>
    <!-- left parking lane: residents -->
    <Rule>
        &maxscale_zoom15;
        &minscale_zoom16;
        <Filter>[parking:lane:left]='diagonal' and [parking:condition:left]='residents'</Filter>
        <LinePatternSymbolizer file="&symbols;/park-ld-resi.png" type="png" width="16" height="16"></LinePatternSymbolizer>
    </Rule>
    <Rule>
        &maxscale_zoom17;
        <Filter>[parking:lane:left]='diagonal' and [parking:condition:left]='residents'</Filter>
        <LinePatternSymbolizer file="&symbols;/park-ld-resi.png" type="png" width="16" height="16"></LinePatternSymbolizer>
    </Rule>
    <!-- left parking lane: private -->
    <Rule>
        &maxscale_zoom15;
        &minscale_zoom16;
        <Filter>[parking:lane:left]='diagonal' and [parking:condition:left]='private'</Filter>
        <LinePatternSymbolizer file="&symbols;/park-ld-priv.png" type="png" width="16" height="16"></LinePatternSymbolizer>
    </Rule>
    <Rule>
        &maxscale_zoom17;
        <Filter>[parking:lane:left]='diagonal' and [parking:condition:left]='private'</Filter>
        <LinePatternSymbolizer file="&symbols;/park-ld-priv.png" type="png" width="16" height="16"></LinePatternSymbolizer>
    </Rule>
</Style>
<Style name="parkinglane-right-diagonal">
    <!-- right parking lane: free -->
    <Rule>
        &maxscale_zoom15;
        &minscale_zoom16;
        <Filter>[parking:lane:right]='diagonal' and [parking:condition:right]='free'</Filter>
        <LinePatternSymbolizer file="&symbols;/park-rd-free.png" type="png" width="16" height="16"></LinePatternSymbolizer>
    </Rule>
    <Rule>
        &maxscale_zoom17;
        <Filter>[parking:lane:right]='diagonal' and [parking:condition:right]='free'</Filter>
        <LinePatternSymbolizer file="&symbols;/park-rd-free.png" type="png" width="16" height="16"></LinePatternSymbolizer>
    </Rule>
    <!-- right parking lane: disc -->
    <Rule>
        &maxscale_zoom15;
        &minscale_zoom16;
        <Filter>[parking:lane:right]='diagonal' and [parking:condition:right]='disc'</Filter>
        <LinePatternSymbolizer file="&symbols;/park-rd-disc.png" type="png" width="16" height="16"></LinePatternSymbolizer>
    </Rule>
    <Rule>
        &maxscale_zoom17;
        <Filter>[parking:lane:right]='diagonal' and [parking:condition:right]='disc'</Filter>
        <LinePatternSymbolizer file="&symbols;/park-rd-disc.png" type="png" width="16" height="16"></LinePatternSymbolizer>
    </Rule>
    <!-- right parking lane: ticket -->
    <Rule>
        &maxscale_zoom15;
        &minscale_zoom16;
        <Filter>[parking:lane:right]='diagonal' and [parking:condition:right]='ticket'</Filter>
        <LinePatternSymbolizer file="&symbols;/park-rd-fee.png" type="png" width="16" height="16"></LinePatternSymbolizer>
    </Rule>
    <Rule>
        &maxscale_zoom17;
        <Filter>[parking:lane:right]='diagonal' and [parking:condition:right]='ticket'</Filter>
        <LinePatternSymbolizer file="&symbols;/park-rd-fee.png" type="png" width="16" height="16"></LinePatternSymbolizer>
    </Rule>
    <!-- right parking lane: customers -->
    <Rule>
        &maxscale_zoom15;
        &minscale_zoom16;
        <Filter>[parking:lane:right]='diagonal' and [parking:condition:right]='customers'</Filter>
        <LinePatternSymbolizer file="&symbols;/park-rd-cust.png" type="png" width="16" height="16"></LinePatternSymbolizer>
    </Rule>
    <Rule>
        &maxscale_zoom17;
        <Filter>[parking:lane:right]='diagonal' and [parking:condition:right]='customers'</Filter>
        <LinePatternSymbolizer file="&symbols;/park-rd-cust.png" type="png" width="16" height="16"></LinePatternSymbolizer>
    </Rule>
    <!-- right parking lane: residents -->
    <Rule>
        &maxscale_zoom15;
        &minscale_zoom16;
        <Filter>[parking:lane:right]='diagonal' and [parking:condition:right]='residents'</Filter>
        <LinePatternSymbolizer file="&symbols;/park-rd-resi.png" type="png" width="16" height="16"></LinePatternSymbolizer>
    </Rule>
    <Rule>
        &maxscale_zoom17;
        <Filter>[parking:lane:right]='diagonal' and [parking:condition:right]='residents'</Filter>
        <LinePatternSymbolizer file="&symbols;/park-rd-resi.png" type="png" width="16" height="16"></LinePatternSymbolizer>
    </Rule>
    <!-- right parking lane: private -->
    <Rule>
        &maxscale_zoom15;
        &minscale_zoom16;
        <Filter>[parking:lane:right]='diagonal' and [parking:condition:right]='private'</Filter>
        <LinePatternSymbolizer file="&symbols;/park-rd-priv.png" type="png" width="16" height="16"></LinePatternSymbolizer>
    </Rule>
    <Rule>
        &maxscale_zoom17;
        <Filter>[parking:lane:right]='diagonal' and [parking:condition:right]='private'</Filter>
        <LinePatternSymbolizer file="&symbols;/park-rd-priv.png" type="png" width="16" height="16"></LinePatternSymbolizer>
    </Rule>
</Style>








<Style name="parkinglane-left-orthogonal">
    <!-- left parking lane: free -->
    <Rule>
        &maxscale_zoom15;
        &minscale_zoom16;
        <Filter>[parking:lane:left]='orthogonal' and [parking:condition:left]='free'</Filter>
        <LinePatternSymbolizer file="&symbols;/park-lo-free.png" type="png" width="16" height="16"></LinePatternSymbolizer>
    </Rule>
    <Rule>
        &maxscale_zoom17;
        <Filter>[parking:lane:left]='orthogonal' and [parking:condition:left]='free'</Filter>
        <LinePatternSymbolizer file="&symbols;/park-lo-free.png" type="png" width="16" height="16"></LinePatternSymbolizer>
    </Rule>
    <!-- left parking lane: disc -->
    <Rule>
        &maxscale_zoom15;
        &minscale_zoom16;
        <Filter>[parking:lane:left]='orthogonal' and [parking:condition:left]='disc'</Filter>
        <LinePatternSymbolizer file="&symbols;/park-lo-disc.png" type="png" width="16" height="16"></LinePatternSymbolizer>
    </Rule>
    <Rule>
        &maxscale_zoom17;
        <Filter>[parking:lane:left]='orthogonal' and [parking:condition:left]='disc'</Filter>
        <LinePatternSymbolizer file="&symbols;/park-lo-disc.png" type="png" width="16" height="16"></LinePatternSymbolizer>
    </Rule>
    <!-- left parking lane: ticket -->
    <Rule>
        &maxscale_zoom15;
        &minscale_zoom16;
        <Filter>[parking:lane:left]='orthogonal' and [parking:condition:left]='ticket'</Filter>
        <LinePatternSymbolizer file="&symbols;/park-lo-fee.png" type="png" width="16" height="16"></LinePatternSymbolizer>
    </Rule>
    <Rule>
        &maxscale_zoom17;
        <Filter>[parking:lane:left]='orthogonal' and [parking:condition:left]='ticket'</Filter>
        <LinePatternSymbolizer file="&symbols;/park-lo-fee.png" type="png" width="16" height="16"></LinePatternSymbolizer>
    </Rule>
    <!-- left parking lane: customers -->
    <Rule>
        &maxscale_zoom15;
        &minscale_zoom16;
        <Filter>[parking:lane:left]='orthogonal' and [parking:condition:left]='customers'</Filter>
        <LinePatternSymbolizer file="&symbols;/park-lo-cust.png" type="png" width="16" height="16"></LinePatternSymbolizer>
    </Rule>
    <Rule>
        &maxscale_zoom17;
        <Filter>[parking:lane:left]='orthogonal' and [parking:condition:left]='customers'</Filter>
        <LinePatternSymbolizer file="&symbols;/park-lo-cust.png" type="png" width="16" height="16"></LinePatternSymbolizer>
    </Rule>
    <!-- left parking lane: residents -->
    <Rule>
        &maxscale_zoom15;
        &minscale_zoom16;
        <Filter>[parking:lane:left]='orthogonal' and [parking:condition:left]='residents'</Filter>
        <LinePatternSymbolizer file="&symbols;/park-lo-resi.png" type="png" width="16" height="16"></LinePatternSymbolizer>
    </Rule>
    <Rule>
        &maxscale_zoom17;
        <Filter>[parking:lane:left]='orthogonal' and [parking:condition:left]='residents'</Filter>
        <LinePatternSymbolizer file="&symbols;/park-lo-resi.png" type="png" width="16" height="16"></LinePatternSymbolizer>
    </Rule>
    <!-- left parking lane: private -->
    <Rule>
        &maxscale_zoom15;
        &minscale_zoom16;
        <Filter>[parking:lane:left]='orthogonal' and [parking:condition:left]='private'</Filter>
        <LinePatternSymbolizer file="&symbols;/park-lo-priv.png" type="png" width="16" height="16"></LinePatternSymbolizer>
    </Rule>
    <Rule>
        &maxscale_zoom17;
        <Filter>[parking:lane:left]='orthogonal' and [parking:condition:left]='private'</Filter>
        <LinePatternSymbolizer file="&symbols;/park-lo-priv.png" type="png" width="16" height="16"></LinePatternSymbolizer>
    </Rule>
</Style>
<Style name="parkinglane-right-orthogonal">
    <!-- right parking lane: free -->
    <Rule>
        &maxscale_zoom15;
        &minscale_zoom16;
        <Filter>[parking:lane:right]='orthogonal' and [parking:condition:right]='free'</Filter>
        <LinePatternSymbolizer file="&symbols;/park-ro-free.png" type="png" width="16" height="16"></LinePatternSymbolizer>
    </Rule>
    <Rule>
        &maxscale_zoom17;
        <Filter>[parking:lane:right]='orthogonal' and [parking:condition:right]='free'</Filter>
        <LinePatternSymbolizer file="&symbols;/park-ro-free.png" type="png" width="16" height="16"></LinePatternSymbolizer>
    </Rule>
    <!-- right parking lane: disc -->
    <Rule>
        &maxscale_zoom15;
        &minscale_zoom16;
        <Filter>[parking:lane:right]='orthogonal' and [parking:condition:right]='disc'</Filter>
        <LinePatternSymbolizer file="&symbols;/park-ro-disc.png" type="png" width="16" height="16"></LinePatternSymbolizer>
    </Rule>
    <Rule>
        &maxscale_zoom17;
        <Filter>[parking:lane:right]='orthogonal' and [parking:condition:right]='disc'</Filter>
        <LinePatternSymbolizer file="&symbols;/park-ro-disc.png" type="png" width="16" height="16"></LinePatternSymbolizer>
    </Rule>
    <!-- right parking lane: ticket -->
    <Rule>
        &maxscale_zoom15;
        &minscale_zoom16;
        <Filter>[parking:lane:right]='orthogonal' and [parking:condition:right]='ticket'</Filter>
        <LinePatternSymbolizer file="&symbols;/park-ro-fee.png" type="png" width="16" height="16"></LinePatternSymbolizer>
    </Rule>
    <Rule>
        &maxscale_zoom17;
        <Filter>[parking:lane:right]='orthogonal' and [parking:condition:right]='ticket'</Filter>
        <LinePatternSymbolizer file="&symbols;/park-ro-fee.png" type="png" width="16" height="16"></LinePatternSymbolizer>
    </Rule>
    <!-- right parking lane: customers -->
    <Rule>
        &maxscale_zoom15;
        &minscale_zoom16;
        <Filter>[parking:lane:right]='orthogonal' and [parking:condition:right]='customers'</Filter>
        <LinePatternSymbolizer file="&symbols;/park-ro-cust.png" type="png" width="16" height="16"></LinePatternSymbolizer>
    </Rule>
    <Rule>
        &maxscale_zoom17;
        <Filter>[parking:lane:right]='orthogonal' and [parking:condition:right]='customers'</Filter>
        <LinePatternSymbolizer file="&symbols;/park-ro-cust.png" type="png" width="16" height="16"></LinePatternSymbolizer>
    </Rule>
    <!-- right parking lane: residents --> -->
    <Rule>
        &maxscale_zoom15;
        &minscale_zoom16;
        <Filter>[parking:lane:right]='orthogonal' and [parking:condition:right]='residents'</Filter>
        <LinePatternSymbolizer file="&symbols;/park-ro-resi.png" type="png" width="16" height="16"></LinePatternSymbolizer>
    </Rule>
    <Rule>
        &maxscale_zoom17;
        <Filter>[parking:lane:right]='orthogonal' and [parking:condition:right]='residents'</Filter>
        <LinePatternSymbolizer file="&symbols;/park-ro-resi.png" type="png" width="16" height="16"></LinePatternSymbolizer>
    </Rule>
    <!-- right parking lane: private -->
    <Rule>
        &maxscale_zoom15;
        &minscale_zoom16;
        <Filter>[parking:lane:right]='orthogonal' and [parking:condition:right]='private'</Filter>
        <LinePatternSymbolizer file="&symbols;/park-ro-priv.png" type="png" width="16" height="16"></LinePatternSymbolizer>
    </Rule>
    <Rule>
        &maxscale_zoom17;
        <Filter>[parking:lane:right]='orthogonal' and [parking:condition:right]='private'</Filter>
        <LinePatternSymbolizer file="&symbols;/park-ro-priv.png" type="png" width="16" height="16"></LinePatternSymbolizer>
    </Rule>
</Style>


<!-- parkinglayers -->
<Layer name="parkinglane-left" srs="&osm2pgsql_projection;" status="1" clear_label_cache="0">
    <StyleName>parkinglane-left-diagonal</StyleName>
    <StyleName>parkinglane-left-orthogonal</StyleName>
    <StyleName>parkinglane-left-inline</StyleName>
    <StyleName>parkinglane-left-no</StyleName>
    <Datasource>
        <Parameter name="table">
<!--
        (select way,(tags->'parking:lane:left') as "parking:lane:left",COALESCE(tags->'parking:condition:left',tags->'parking:condition:both') as "parking:condition:left"
          from &prefix;_line
          where (tags ? 'parking:lane:left') AND (tags->'parking:lane:left') in ('diagonal','orthogonal','inline','no_parking','no_stopping','fire_lane')
        ) as roads
-->
        (select way,
                COALESCE(tags->'parking:lane:left',tags->'parking:lane:both') as "parking:lane:left",
                COALESCE(tags->'parking:condition:left',tags->'parking:condition:both') as "parking:condition:left"
                from &prefix;_line
                where (tags ? 'parking:lane:left') or (tags ? 'parking:lane:both')
        ) as roads
        </Parameter>
        &datasource-settings;
    </Datasource>
</Layer>

<Layer name="parkinglane-right" srs="&osm2pgsql_projection;" status="1" clear_label_cache="0">
    <StyleName>parkinglane-right-diagonal</StyleName>
    <StyleName>parkinglane-right-orthogonal</StyleName>
    <StyleName>parkinglane-right-inline</StyleName>
    <StyleName>parkinglane-right-no</StyleName>
    <Datasource>
<!--
        <Parameter name="table">
        (select way,(tags->'parking:lane:right') as "parking:lane:right",(tags->'parking:condition:right') as "parking:condition:right"
          from &prefix;_line
          where (tags ? 'parking:lane:right') AND (tags->'parking:lane:right') in ('diagonal','orthogonal','inline','no_parking','no_stopping','fire_lane')
        ) as roads
-->
        (select way,
                COALESCE(tags->'parking:lane:right',tags->'parking:lane:both') as "parking:lane:right",
                COALESCE(tags->'parking:condition:right',tags->'parking:condition:both') as "parking:condition:right"
                from &prefix;_line
                where (tags ? 'parking:lane:right') or (tags ? 'parking:lane:both')
        ) as roads
        </Parameter>
        &datasource-settings;
    </Datasource>
</Layer>


