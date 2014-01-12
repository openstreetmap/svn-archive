package org.j7zip.SevenZip.Archive.SevenZip;

import org.j7zip.Common.ByteBuffer;

class AltCoderInfo {
    public MethodID MethodID;
    public ByteBuffer Properties;
    
    public AltCoderInfo() {
        MethodID = new MethodID();
        Properties = new ByteBuffer();
    } 
}
