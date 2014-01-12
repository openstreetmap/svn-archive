package org.j7zip.SevenZip.Archive.Common;

import org.j7zip.Common.LongVector;

public interface CoderMixer2 {
    
    void ReInit();

    void SetBindInfo(BindInfo bindInfo);

    void SetCoderInfo(int coderIndex,LongVector inSizes, LongVector outSizes);
}
