package foundation.jpa.querydsl;

import com.querydsl.core.types.Constant;
import com.querydsl.core.types.EntityPath;
import com.querydsl.core.types.Expression;
import com.querydsl.core.types.Ops;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Expressions;

import static com.querydsl.core.types.dsl.Expressions.constant;

public class QueryUtils {
    public static BooleanExpression resolveOperation(Ops operator, Expression<?> leftOperand, Expression<?> rightOperand, EntityConverter entityConverter) {
        if(leftOperand instanceof EntityPath && rightOperand instanceof Constant) {
            rightOperand = constant(entityConverter.convert(((Constant<?>) rightOperand).getConstant(), leftOperand.getType()));
        } else if(rightOperand instanceof EntityPath && leftOperand instanceof Constant) {
            leftOperand = constant(entityConverter.convert(((Constant<?>) leftOperand).getConstant(), rightOperand.getType()));
        }
        return Expressions.booleanOperation(operator, leftOperand, rightOperand);

    }

}
