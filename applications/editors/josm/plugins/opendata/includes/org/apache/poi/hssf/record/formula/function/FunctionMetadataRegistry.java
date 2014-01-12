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

package org.apache.poi.hssf.record.formula.function;

import java.util.Map;
/**
 * Allows clients to get {@link FunctionMetadata} instances for any built-in function of Excel.
 *
 * @author Josh Micich
 */
public final class FunctionMetadataRegistry {

	public static final short FUNCTION_INDEX_EXTERNAL = 255;

	private static FunctionMetadataRegistry _instance;

	private final FunctionMetadata[] _functionDataByIndex;

	private static FunctionMetadataRegistry getInstance() {
		if (_instance == null) {
			_instance = FunctionMetadataReader.createRegistry();
		}
		return _instance;
	}

	/* package */ FunctionMetadataRegistry(FunctionMetadata[] functionDataByIndex, Map<String, FunctionMetadata> functionDataByName) {
		_functionDataByIndex = functionDataByIndex;
	}

	public static FunctionMetadata getFunctionByIndex(int index) {
		return getInstance().getFunctionByIndexInternal(index);
	}

	private FunctionMetadata getFunctionByIndexInternal(int index) {
		return _functionDataByIndex[index];
	}
}
