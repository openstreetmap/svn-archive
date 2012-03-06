/* ====================================================================
   Licensed to the Apache Software Foundation (ASF) under one or more
   contributor license agreements.  See the NOTICE file distributed with
   this work for additional information regarding copyright ownership.
   The ASF licenses this file to You under the Apache License, Version 2.0
   (the "License"); you may not use this file except in compliance with
   the License.  You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
==================================================================== */

package org.apache.poi.ss.util;


/**
 * For working with the internals of IEEE 754-2008 'binary64' (double precision) floating point numbers
 *
 * @author Josh Micich
 */
final class IEEEDouble {
	private static final int  EXPONENT_SHIFT = 52;
	public static final long FRAC_MASK = 0x000FFFFFFFFFFFFFL;
	public static final long FRAC_ASSUMED_HIGH_BIT = ( 1L<<EXPONENT_SHIFT );
}
