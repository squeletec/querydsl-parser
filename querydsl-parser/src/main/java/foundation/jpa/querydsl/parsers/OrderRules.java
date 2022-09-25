/*
 * BSD 2-Clause License
 *
 * Copyright (c) 2020-2022, Ondrej Fischer
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

package foundation.jpa.querydsl.parsers;

import com.querydsl.core.types.Expression;
import com.querydsl.core.types.OrderSpecifier;
import foundation.rpg.Name;
import foundation.rpg.StartSymbol;
import foundation.rpg.common.rules.CommaSeparated;
import foundation.rpg.parser.Token;

import java.util.List;

public interface OrderRules extends ExpressionRules {

    @StartSymbol(parserClassName = "OrderSpecifierParser", lexerClassName = "OrderSpecifierLexer", packageName = "foundation.jpa.querydsl.parsers.order")
    OrderSpecifier<?>[] is1 (@CommaSeparated List<OrderSpecifier<?>> l);

    OrderSpecifier<?> is4(Expression<?> expression);
    OrderSpecifier<?> is1(OrderSpecifier<?> specifier, @Name("nulls") Token nulls, @Name("first") Token last);
    OrderSpecifier<?> is2(OrderSpecifier<?> specifier, @Name("nulls") Token nulls, @Name("last") Token last);
    OrderSpecifier<?> is1(Expression<?> expression, @Name("asc") Token asc);
    OrderSpecifier<?> is2(Expression<?> expression, @Name("desc") Token desc);

}
