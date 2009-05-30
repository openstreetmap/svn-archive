/*
 * Copyright the original author or authors.
 * 
 * Licensed under the MOZILLA PUBLIC LICENSE, Version 1.1 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.mozilla.org/MPL/MPL-1.1.html
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.idmedia.as3commons.util {
  
  /**
   * A class can implement the <code>Observer</code> interface when it
   * wants to be informed of changes in observable objects.
   *
   * @author sleistner
   */
  public interface Observer {
    
    /**
     * This method is called whenever the observed object is changed. An
     * application calls an <tt>Observer</tt> object's
     * <code>notifyObservers</code> method to have all the object's
     * observervables notified of the change.
     *
     * @param   observable     the observable object.
     * @param   args   an argument passed to the <code>notifyObservers</code>
     *                 method.
     */
    function update(observable:Observable, args:*):void;		
  }
}