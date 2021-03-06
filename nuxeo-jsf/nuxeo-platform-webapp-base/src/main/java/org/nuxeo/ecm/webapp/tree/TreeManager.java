/*
 * (C) Copyright 2006-2007 Nuxeo SA (http://nuxeo.com/) and others.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Contributors:
 *     Nuxeo - initial API and implementation
 *
 * $Id$
 */

package org.nuxeo.ecm.webapp.tree;

import java.io.Serializable;

import org.nuxeo.ecm.core.api.Filter;
import org.nuxeo.ecm.core.api.Sorter;

/**
 * Interface for the tree manager service.
 *
 * @author Anahide Tchertchian
 */
public interface TreeManager extends Serializable {

    /**
     * Returns filter to use for given plugin names.
     */
    Filter getFilter(String pluginName);

    /**
     * Returns leaf filter to use for given plugin names.
     */
    Filter getLeafFilter(String pluginName);

    /**
     * Returns sorter to use for given plugin name.
     */
    Sorter getSorter(String pluginName);

    /**
     * Returns the page provider name for given plugin name.
     *
     * @since 5.4.2
     */
    String getPageProviderName(String pluginName);

}
