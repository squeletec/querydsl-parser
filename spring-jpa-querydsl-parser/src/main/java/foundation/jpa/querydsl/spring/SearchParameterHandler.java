package foundation.jpa.querydsl.spring;

import com.querydsl.core.types.EntityPath;
import com.querydsl.jpa.impl.JPAQuery;
import foundation.jpa.querydsl.QueryContext;
import org.springframework.core.MethodParameter;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.net.URI;
import java.util.List;

import javax.persistence.EntityManager;

import static java.util.Objects.nonNull;

public class SearchParameterHandler implements HandlerMethodArgumentResolver {

    private final EntityManager entityManager;
    private final QueryContext queryContext;

    public SearchParameterHandler(EntityManager entityManager, QueryContext queryContext) {
        this.entityManager = entityManager;
        this.queryContext = queryContext;
    }

    @Override
    public boolean supportsParameter(MethodParameter methodParameter) {
        return Search.class.equals(methodParameter.getParameterType());
    }

    @Override
    public Object resolveArgument(MethodParameter methodParameter, ModelAndViewContainer modelAndViewContainer, NativeWebRequest nativeWebRequest, WebDataBinderFactory webDataBinderFactory) throws Exception {
        String query = nativeWebRequest.getParameter("query");
        Pageable pageable = pageable(methodParameter.getMethodAnnotation(PageableDefault.class), nativeWebRequest);
        return execute(getEntityPath(methodParameter), query, pageable, URI.create(""));
    }



    private <E, Q extends EntityPath<E>> Search<E, Q> execute(EntityPath<E> type, String query, Pageable pageable, URI uri) {
        try {
            JPAQuery<E> select = new JPAQuery<E>(entityManager).from(type).where(queryContext.parse(type, query)).offset(pageable.getOffset()).limit(pageable.getPageSize());
            long count = select.fetchCount();
            List<E> data = select.fetch();
            return Search.search(query, new PageImpl<>(data, pageable, count), null, uri);
        } catch (Throwable e) {
            return Search.search(query, Page.empty(), e, uri);
        }
    }


    private <E> EntityPath<E> getEntityPath(MethodParameter parameter) throws IllegalAccessException {
        Class<?> entityPathClass = (Class<?>) ((ParameterizedType) parameter.getGenericParameterType()).getActualTypeArguments()[2];
        for(Field field : entityPathClass.getFields())
            if(entityPathClass.equals(field.getType()))
                return (EntityPath<E>) field.get(null);
        throw new IllegalArgumentException("Method parameter " + parameter + " not proper Querydsl generated entity path.");
    }


    private Pageable pageable(PageableDefault pageableDefault, NativeWebRequest request) {
        int page = 1;
        int size = 10;
        if(nonNull(pageableDefault)) {
            page = pageableDefault.page();
            size = pageableDefault.size();
        }
        if(nonNull(request.getParameter("page"))) {
            page = Integer.parseInt(request.getParameter("page"));
        }
        if(nonNull(request.getParameter("size"))) {
            size = Integer.parseInt(request.getParameter("size"));
        }
        return PageRequest.of(page, size);
    }

}
