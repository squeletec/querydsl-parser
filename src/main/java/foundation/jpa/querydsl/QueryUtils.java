package foundation.jpa.querydsl;

import com.querydsl.core.types.Constant;
import com.querydsl.core.types.Expression;
import com.querydsl.core.types.Ops;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.BooleanOperation;
import com.querydsl.core.types.dsl.Expressions;
import org.springframework.core.convert.ConversionService;

import static com.querydsl.core.types.dsl.Expressions.constant;

public class QueryUtils {
    public static BooleanExpression resolve(Expression<?> leftOperand, Expression<?> rightOperand, ConversionService conversionService) {
        return operation(Ops.EQ, leftOperand, constant(conversionService.convert(((Constant<?>) rightOperand).getConstant(), leftOperand.getType())));
    }

    public static BooleanOperation operation(Ops operator, Expression<?>... operands) {
        return Expressions.booleanOperation(operator, operands);
    }

}
