package foundation.jpa.querydsl.spring;

import com.querydsl.core.types.EntityPath;
import foundation.jpa.querydsl.spring.impl.SearchCriteriaImpl;
import org.springframework.core.MethodParameter;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static org.springframework.web.context.request.RequestAttributes.SCOPE_SESSION;

public class SearchCriteriaHandler implements HandlerMethodArgumentResolver {

    private final String queryParameterName;
    private final String sortParameterName;
    private final String pageParameterName;
    private final String sizeParameterName;
    private final int defaultPageSize;
    private final int defaultPage;

    public SearchCriteriaHandler(String queryParameterName,
                                 String sortParameterName,
                                 String pageParameterName,
                                 String sizeParameterName,
                                 int defaultPageSize,
                                 int defaultPage) {
        this.queryParameterName = queryParameterName;
        this.sortParameterName = sortParameterName;
        this.pageParameterName = pageParameterName;
        this.sizeParameterName = sizeParameterName;
        this.defaultPageSize = defaultPageSize;
        this.defaultPage = defaultPage;
    }

    @Override
    public boolean supportsParameter(MethodParameter methodParameter) {
        return SearchCriteria.class.equals(methodParameter.getParameterType());
    }

    @Override
    public SearchCriteria<? extends EntityPath<?>> resolveArgument(MethodParameter methodParameter, ModelAndViewContainer modelAndViewContainer, NativeWebRequest nativeWebRequest, WebDataBinderFactory webDataBinderFactory) throws Exception {
        CacheQuery cacheQuery = methodParameter.getParameterAnnotation(CacheQuery.class);
        String typeName = ((ParameterizedType) methodParameter.getGenericParameterType()).getActualTypeArguments()[0].getTypeName();
        return new SearchCriteriaImpl<>(
                get(nativeWebRequest, queryParameterName, cacheQuery, typeName, defaultQuery(methodParameter)),
                get(nativeWebRequest, sortParameterName, cacheQuery, typeName, defaultSort(methodParameter)),
                pageable(methodParameter.getMethodAnnotation(PageableDefault.class), nativeWebRequest),
                getEntityPath(methodParameter)
        );
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

    private <E> EntityPath<E> getEntityPath(MethodParameter parameter) throws IllegalAccessException {
        Class<?> entityPathClass = (Class<?>) ((ParameterizedType) parameter.getGenericParameterType()).getActualTypeArguments()[0];
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
