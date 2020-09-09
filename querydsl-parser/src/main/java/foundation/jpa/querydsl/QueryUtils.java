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
