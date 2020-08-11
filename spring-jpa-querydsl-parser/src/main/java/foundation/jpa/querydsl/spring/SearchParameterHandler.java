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
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.net.URI;
import java.util.List;

import javax.persistence.EntityManager;

import static java.util.Objects.isNull;
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
        String query = query(methodParameter, nativeWebRequest);
        Pageable pageable = pageable(methodParameter.getMethodAnnotation(PageableDefault.class), nativeWebRequest);
        return execute(getEntityPath(methodParameter), query, pageable, URI.create(""), methodParameter.getParameterAnnotation(ImplicitQuery.class));
    }



    private String query(MethodParameter methodParameter, NativeWebRequest nativeWebRequest) {
        String query = nativeWebRequest.getParameter("query");
        if(methodParameter.hasParameterAnnotation(QueryCacheName.class)) {
            String name = methodParameter.getParameterAnnotation(QueryCacheName.class).value();
            if(isNull(query)) {
                query = (String) nativeWebRequest.getAttribute(name, WebRequest.SCOPE_SESSION);
                //nativeWebRequest.getAttribute("")
                // Load from session
            } else {
                nativeWebRequest.setAttribute(name, query, WebRequest.SCOPE_SESSION);
                // Store in session
            }
        }
        if(isNull(query)) {
            DefaultQuery defaultQuery = methodParameter.getParameterAnnotation(DefaultQuery.class);
            if(nonNull(defaultQuery)) {
                query = defaultQuery.value();
            }
        }
        return query;
    }

    private <E, Q extends EntityPath<E>> Search<E, Q> execute(EntityPath<E> type, String query, Pageable pageable, URI uri, ImplicitQuery implicitQuery) {
        try {
            JPAQuery<E> jpaQuery = new JPAQuery<E>(entityManager).from(type);
            if(nonNull(implicitQuery)) {
                jpaQuery.where(queryContext.parse(type, implicitQuery.value()));
            }
            jpaQuery = jpaQuery.where(queryContext.parse(type, query)).offset(pageable.getOffset()).limit(pageable.getPageSize());
            long count = jpaQuery.fetchCount();
            List<E> data = jpaQuery.fetch();
            return Search.search(query, new PageImpl<>(data, pageable, count), null, uri);
        } catch (Throwable e) {
            return Search.search(query, Page.empty(), e, uri);
        }
    }


    private <E> EntityPath<E> getEntityPath(MethodParameter parameter) throws IllegalAccessException {
        Class<?> entityPathClass = (Class<?>) ((ParameterizedType) parameter.getGenericParameterType()).getActualTypeArguments()[1];
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
