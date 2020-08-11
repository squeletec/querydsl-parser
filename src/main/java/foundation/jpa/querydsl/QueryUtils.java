package foundation.jpa.querydsl;

import com.querydsl.core.types.Constant;
import com.querydsl.core.types.Expression;
import com.querydsl.core.types.Ops;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.BooleanOperation;
import com.querydsl.core.types.dsl.Expressions;

import static com.querydsl.core.types.dsl.Expressions.constant;

public class QueryUtils {
    public static BooleanExpression resolve(Ops operator, Expression<?> leftOperand, Expression<?> rightOperand, EntityConverter entityConverter) {
        return operation(operator, leftOperand, constant(entityConverter.convert(((Constant<?>) rightOperand).getConstant(), leftOperand.getType())));
    }

    public static BooleanOperation operation(Ops operator, Expression<?>... operands) {
        return Expressions.booleanOperation(operator, operands);
    }

}
