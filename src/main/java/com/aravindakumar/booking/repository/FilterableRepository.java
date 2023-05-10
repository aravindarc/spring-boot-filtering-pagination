package com.aravindakumar.booking.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

// this is a generic interface that can provide filtering functionality to any mongo repository
// the implementation is in FilterableRepoImpl.java
// the idea is filtering should be done on the database side, not the application side
// this is because the database is much more efficient at filtering than the application
// and mongodb has a very powerful query language as seen in https://www.mongodb.com/docs/manual/reference/operator/query/
// whenever your database provides such verbose filtering functionality, it is a good idea to push the query building
// to the ui and then pass the query as a parameter to the backend, this is a common pattern in lucene based search engines
public interface FilterableRepository<T> {

    // this method is used to get a page of results from the database with filtering
    Page<T> findAllWithFilter(Class<T> typeParameterClass,
                              Filtering filtering, Pageable pageable);

    // this method is used to get all the possible values for a filter
    // so that the ui can show a dropdown with all the possible values with checkboxes or radio buttons
    List<Object> getAllPossibleValuesForFilter(Class<T> typeParameterClass, Filtering filtering, String filterKey);

    default Query constructQueryFromFiltering(Filtering filtering) {
        Query query = new Query();
        Map<String, Criteria> criteriaMap = new HashMap<>();
        for (Filtering.Filter filter : filtering.getFilterList()) {
            // this switch case is the place where the ui operators are mapped to the database operators
            // the only switch case in this filtering implementation is this one,
            // but it is unavoidable because the operators are not the same in the database and the ui
            // for example, the ui uses "eq" for equals, but mongodb uses "is" and so on
            switch (filter.operator) {
                case eq:
                    criteriaMap.put(filter.key, Criteria.where(filter.key).is(filter.value));
                    break;
                case gt:
                    if (criteriaMap.containsKey(filter.key)) {
                        criteriaMap.get(filter.key).gt(filter.value);
                    } else {
                        criteriaMap.put(filter.key, Criteria.where(filter.key).gt(filter.value));
                    }
                    break;
                case gte:
                    if (criteriaMap.containsKey(filter.key)) {
                        criteriaMap.get(filter.key).gte(filter.value);
                    } else {
                        criteriaMap.put(filter.key, Criteria.where(filter.key).gte(filter.value));
                    }
                    break;
                case in:
                    criteriaMap.put(filter.key, Criteria.where(filter.key).in((HashSet<Object>)filter.value));
                    break;
                case lt:
                    if (criteriaMap.containsKey(filter.key)) {
                        criteriaMap.get(filter.key).lt(filter.value);
                    } else {
                        criteriaMap.put(filter.key, Criteria.where(filter.key).lt(filter.value));
                    }
                    break;
                case lte:
                    if (criteriaMap.containsKey(filter.key)) {
                        criteriaMap.get(filter.key).lte(filter.value);
                    } else {
                        criteriaMap.put(filter.key, Criteria.where(filter.key).lte(filter.value));
                    }
                    break;
                case ne:
                    criteriaMap.put(filter.key, Criteria.where(filter.key).ne(filter.value));
                    break;
                case nin:
                    criteriaMap.put(filter.key, Criteria.where(filter.key).nin((HashSet<Object>)filter.value));
                    break;
                // this is a special case, because this can be used as a search operator in the ui
                // but proper testing is needed to make sure this works as expected
                // because of the special characters in regex, it is possible that this will not work as expected
                // todo test this
                case regex:
                    criteriaMap.put(filter.key, Criteria.where(filter.key).regex((String)filter.value));
                    break;
                default:
                    throw new IllegalArgumentException("Unknown operator: " + filter.operator);
            }
        }
        criteriaMap.values().forEach(query::addCriteria);
        return query;
    }
}

