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

package foundation.jpa.querydsl;

import com.querydsl.core.types.Constant;
import com.querydsl.core.types.EntityPath;
import com.querydsl.core.types.Expression;
import com.querydsl.core.types.Ops;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Expressions;

import static com.querydsl.core.types.dsl.Expressions.constant;
import static java.util.Objects.requireNonNull;

public class QueryUtils {
    public static BooleanExpression resolveOperation(Ops operator, Expression<?> leftOperand, Expression<?> rightOperand, EntityConverter entityConverter) {
        if(leftOperand instanceof EntityPath && rightOperand instanceof Constant) {
            rightOperand = convert(rightOperand, leftOperand, entityConverter);
        } else if(rightOperand instanceof EntityPath && leftOperand instanceof Constant) {
            leftOperand = convert(leftOperand, rightOperand, entityConverter);
        }
        return Expressions.booleanOperation(operator, leftOperand, rightOperand);

    }

    private static Expression<?> convert(Expression<?> constant, Expression<?> toType, EntityConverter entityConverter) {
        Object value = ((Constant<?>) constant).getConstant();
        Class<?> type = toType.getType();
        return type.isInstance(value) ? constant : constant(requireNonNull(
                entityConverter.convert(value, type),
                () -> "No " + type.getSimpleName() + " " + value + " exists!"
        ));
    }
}
