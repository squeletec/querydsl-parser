/*
 * BSD 2-Clause License
 *
 * Copyright (c) 2020-2020, Ondrej Fischer
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 */

package foundation.jpa.querydsl.spring.impl;

import com.querydsl.core.types.EntityPath;
import foundation.jpa.querydsl.spring.SearchCriteria;
import org.springframework.data.domain.Pageable;

public class SearchCriteriaImpl<Q extends EntityPath<?>> implements SearchCriteria<Q> {

    private final String parameterName;
    private final String query;
    private final String sort;
    private final Pageable pageable;
    private final Q entityPath;

    public SearchCriteriaImpl(String parameterName, String query, String sort, Pageable pageable, Q entityPath) {
        this.parameterName = parameterName;
        this.query = query;
        this.sort = sort;
        this.pageable = pageable;
        this.entityPath = entityPath;
    }

    @Override
    public String getParameterName() {
        return parameterName;
    }

    @Override
    public String getQuery() {
        return query;
    }

    @Override
    public String getSort() {
        return sort;
    }

    @Override
    public Pageable getPageable() {
        return pageable;
    }

    @Override
    public Q getEntityPath() {
        return entityPath;
    }

}
