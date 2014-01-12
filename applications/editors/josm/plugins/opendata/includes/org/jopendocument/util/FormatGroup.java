/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2008 jOpenDocument, by ILM Informatique. All rights reserved.
 * 
 * The contents of this file are subject to the terms of the GNU
 * General Public License Version 3 only ("GPL").  
 * You may not use this file except in compliance with the License. 
 * You can obtain a copy of the License at http://www.gnu.org/licenses/gpl-3.0.html
 * See the License for the specific language governing permissions and limitations under the License.
 * 
 * When distributing the software, include this License Header Notice in each file.
 * 
 */

package org.jopendocument.util;

import java.text.FieldPosition;
import java.text.Format;
import java.text.ParsePosition;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

/**
 * A list of related formats.
 * 
 * @author Sylvain CUAZ
 */
@SuppressWarnings("serial")
public class FormatGroup extends Format {

    private final List<? extends Format> formats;
    private int formatIndex;

    public FormatGroup(Format... formats) {
        this(Arrays.asList(formats));
    }

    /**
     * Creates a group, which will try to parse with the given formats. format() is done with the
     * first format.
     * 
     * @param formats a List of Format.
     * @throws IllegalArgumentException if formats is empty.
     */
    public FormatGroup(final List<? extends Format> formats) {
        if (formats.size() == 0)
            throw new IllegalArgumentException("formats must not be empty");
        this.formats = formats;
        this.formatIndex = 0;
    }

    @Override
    public StringBuffer format(Object newVal, StringBuffer toAppendTo, FieldPosition pos) {
        return this.formats.get(this.formatIndex).format(newVal, toAppendTo, pos);
    }

    @Override
    public Object parseObject(String s, ParsePosition pos) {
        if (pos.getErrorIndex() >= 0)
            throw new IllegalArgumentException(pos + " has en error at " + pos.getErrorIndex());

        boolean success = false;
        Object tmpRes = null;
        final ParsePosition tmpPos = new ParsePosition(pos.getIndex());
        final Iterator<? extends Format> iter = this.formats.iterator();
        while (iter.hasNext() && !success) {
            final Format f = iter.next();
            mutateTo(tmpPos, pos);
            tmpRes = f.parseObject(s, tmpPos);
            success = tmpPos.getIndex() != pos.getIndex() && tmpPos.getErrorIndex() < 0;
        }

        final Object res;
        if (!success) {
            // fail with the same as format()
            res = this.formats.get(this.formatIndex).parseObject(s, pos);
        } else {
            res = tmpRes;
            mutateTo(pos, tmpPos);
        }

        return res;
    }

    private void mutateTo(ParsePosition tmpPos, ParsePosition pos) {
        tmpPos.setIndex(pos.getIndex());
        tmpPos.setErrorIndex(pos.getErrorIndex());
    }
}