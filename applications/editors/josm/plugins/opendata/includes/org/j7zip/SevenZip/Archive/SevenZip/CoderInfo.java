package org.j7zip.SevenZip.Archive.SevenZip;

import org.j7zip.Common.ObjectVector;

class CoderInfo {
    
    int NumInStreams;
    int NumOutStreams;
    public ObjectVector<AltCoderInfo> AltCoders = new org.j7zip.Common.ObjectVector<AltCoderInfo>();
    
    boolean IsSimpleCoder() { return (NumInStreams == 1) && (NumOutStreams == 1); }
    
    public CoderInfo() {
        NumInStreams = 0;
        NumOutStreams = 0;
    }
}
