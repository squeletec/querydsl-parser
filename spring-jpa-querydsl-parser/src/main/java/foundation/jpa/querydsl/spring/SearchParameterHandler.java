package foundation.jpa.querydsl.spring;

import com.querydsl.core.types.EntityPath;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Predicate;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import foundation.jpa.querydsl.QueryContext;
import foundation.jpa.querydsl.order.OrderByParser;
import foundation.jpa.querydsl.order.OrderFactory;
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

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.net.URI;
import java.util.List;

import javax.persistence.EntityManager;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static org.springframework.web.context.request.RequestAttributes.SCOPE_SESSION;

public class SearchParameterHandler implements HandlerMethodArgumentResolver {

    private final JPAQueryFactory factory;
    private final QueryContext queryContext;
    private final String queryParameterName;
    private final String sortParameterName;
    private final String pageParameterName;
    private final String sizeParameterName;
    private final int defaultPageSize;
    private final int defaultPage;

    public SearchParameterHandler(EntityManager manager,
                                  QueryContext queryContext,
                                  String queryParameterName,
                                  String sortParameterName,
                                  String pageParameterName,
                                  String sizeParameterName,
                                  int defaultPageSize,
                                  int defaultPage) {
        this.factory = new JPAQueryFactory(manager);
        this.queryContext = queryContext;
        this.queryParameterName = queryParameterName;
        this.sortParameterName = sortParameterName;
        this.pageParameterName = pageParameterName;
        this.sizeParameterName = sizeParameterName;
        this.defaultPageSize = defaultPageSize;
        this.defaultPage = defaultPage;
    }

    @Override
    public boolean supportsParameter(MethodParameter methodParameter) {
        return Search.class.equals(methodParameter.getParameterType());
    }

    @Override
    public Object resolveArgument(MethodParameter methodParameter, ModelAndViewContainer modelAndViewContainer, NativeWebRequest nativeWebRequest, WebDataBinderFactory webDataBinderFactory) throws Exception {
        CacheQuery cacheQuery = methodParameter.getParameterAnnotation(CacheQuery.class);
        String typeName = ((ParameterizedType) methodParameter.getGenericParameterType()).getActualTypeArguments()[0].getTypeName();
        String query = get(nativeWebRequest, queryParameterName, cacheQuery, typeName, defaultQuery(methodParameter));
        String sort = get(nativeWebRequest, sortParameterName, cacheQuery, typeName, defaultSort(methodParameter));
        Pageable pageable = pageable(methodParameter.getMethodAnnotation(PageableDefault.class), nativeWebRequest);
        return execute(getEntityPath(methodParameter), query, sort, pageable, URI.create(""), methodParameter.getParameterAnnotation(ImplicitQuery.class));
    }

    private String defaultQuery(MethodParameter methodParameter) {
        return methodParameter.hasParameterAnnotation(DefaultQuery.class) ? methodParameter.getParameterAnnotation(DefaultQuery.class).value() : "";
    }

    private String defaultSort(MethodParameter methodParameter) {
        return methodParameter.hasParameterAnnotation(DefaultSort.class) ? methodParameter.getParameterAnnotation(DefaultSort.class).value() : "";
    }

    private String get(NativeWebRequest nativeWebRequest, String name, CacheQuery cacheQuery, String typeName, String defaultValue) {
        String value = nativeWebRequest.getParameter(name);
        if(nonNull(cacheQuery)) {
            String cacheKey = name + ":" + cacheQuery.value() + typeName;
            if("delete".equals(nativeWebRequest.getParameter("cache")))
                nativeWebRequest.removeAttribute(cacheKey, SCOPE_SESSION);
            if(isNull(value))
                value = (String) nativeWebRequest.getAttribute(cacheKey, SCOPE_SESSION);
            else
                nativeWebRequest.setAttribute(cacheKey, value, SCOPE_SESSION);
        }
        if(isNull(value))
            value = defaultValue;
        return value;
    }

    private <E> OrderSpecifier<?>[] sort(EntityPath<E> path, String sort) throws IOException {
        return new OrderByParser(new OrderFactory(path)).parseString(sort);
    }

    private <E, Q extends EntityPath<E>> Search<E, Q> execute(EntityPath<E> type, String query, String sort, Pageable pageable, URI uri, ImplicitQuery implicitQuery) {
        try {
            JPAQuery<E> jpaQuery = factory.selectFrom(type);
            if(nonNull(implicitQuery)) {
                jpaQuery.where(queryContext.parse(type, implicitQuery.value()));
            }
            Predicate predicate = queryContext.parse(type, query);
            OrderSpecifier<?>[] specifiers = sort(type, sort);
            jpaQuery = jpaQuery.where(predicate).orderBy(specifiers).offset(pageable.getOffset()).limit(pageable.getPageSize());
            long count = jpaQuery.fetchCount();
            List<E> data = jpaQuery.fetch();
            return Search.search(query, sort, predicate, specifiers, new PageImpl<>(data, pageable, count), null, uri);
        } catch (Throwable e) {
            return Search.search(query, sort, null, null, Page.empty(), e, uri);
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
        int page = defaultPage;
        int size = defaultPageSize;
        if(nonNull(pageableDefault)) {
            page = pageableDefault.page();
            size = pageableDefault.size();
        }
        if(nonNull(request.getParameter(pageParameterName))) {
            page = Integer.parseInt(request.getParameter(pageParameterName));
        }
        if(nonNull(request.getParameter(sizeParameterName))) {
            size = Integer.parseInt(request.getParameter(sizeParameterName));
        }
        return PageRequest.of(page, size);
    }

}
