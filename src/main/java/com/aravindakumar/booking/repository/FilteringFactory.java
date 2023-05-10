package com.aravindakumar.booking.repository;

import lombok.NoArgsConstructor;

import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.*;

@NoArgsConstructor(access = lombok.AccessLevel.PRIVATE)
public class FilteringFactory {

    interface Converter {
        Object convert(String value);
    }

    private static final Map<Class, Converter> converterForClass = new HashMap<>();

    // add converters for all types that you want to support
    // keep it to primitive types, don't add converters for your own classes
    static {
        converterForClass.put(String.class, (value) -> value);
        converterForClass.put(Integer.class, Integer::valueOf);
        converterForClass.put(int.class, Integer::valueOf);
        converterForClass.put(long.class, Long::valueOf);
        converterForClass.put(Long.class, Long::valueOf);
        converterForClass.put(Double.class, Double::valueOf);
        converterForClass.put(double.class, Double::valueOf);
        converterForClass.put(Float.class, Float::valueOf);
        converterForClass.put(float.class, Float::valueOf);
        converterForClass.put(Boolean.class, Boolean::valueOf);
        converterForClass.put(boolean.class, Boolean::valueOf);
        converterForClass.put(LocalDateTime.class, LocalDateTime::parse);
    }

    public static <T> Filtering parseFromParams(List<String> filter, Class<T> typeParameterClass) {
        Filtering filtering = new Filtering();

        // a filter is in the format: key|operator|value
        for (String filterString : filter) {
            // first split by | to get the key, operator and value
            String[] filterSplit = filterString.split("\\|");

            // check if the filter is in the correct format
            if (filterSplit.length != 3) {
                throw new IllegalArgumentException("Filtering parameter is not in the correct format");
            }

            try {
                // parse the operator
                Filtering.Operator operator = Filtering.Operator.fromString(filterSplit[1]);

                // the key can be nested, so we split by . to get the nested keys
                // example1: key1.key2.key3 will result in nested = [key1, key2, key3]
                // example2: key1 will result in nested = [key1]
                String[] nested = filterSplit[0].split("\\.");

                // if the operator is "in" or "nin", we need to split the value by ; to get the list of values
                // "in" and nin are the only operators that can have multiple values
                // "in" checks if the value is in the list of values
                // "nin" checks if the value is not in the list of values
                if (operator == Filtering.Operator.in || operator == Filtering.Operator.nin) {
                    Set<Object> list = new HashSet<>();
                    for (String value : filterSplit[2].split(";")) {
                        list.add(nestedObject(typeParameterClass, value, nested));
                    }

                    // add the filter to the filtering object
                    filtering.addFilter(filterSplit[0], operator, list);
                } else {

                    // add the filter to the filtering object
                    filtering.addFilter(filterSplit[0], Filtering.Operator.fromString(filterSplit[1]),
                            nestedObject(typeParameterClass, filterSplit[2], nested));
                }
            } catch (NoSuchFieldException e) {
                throw new IllegalArgumentException("Filtering parameter not allowed: " + filterSplit[0]);
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }
        return filtering;
    }

    // this method uses recursion to get the last primitive type in the nested keys by travelling down the object tree
    // this recursion method is safe because in the worst case, the number of recursive calls is equal to the number of nested keys
    // in contradiction to the popular belief, recursion is not always bad and when you need to travel down an object tree, it is the best solution
    private static Object nestedObject(Class classParameter, String value, String[] nested) throws NoSuchFieldException, IllegalAccessException {
        Field field = classParameter.getDeclaredField(nested[0]);
        if (nested.length > 1) {
            // if there are more nested keys, we need to travel down the object tree
            // we do this by calling this method again by removing the first nested key and passing the type of the field
            // along with the value and the remaining nested keys
            return nestedObject(field.getType(), value, Arrays.copyOfRange(nested, 1, nested.length));
        }

        // when nested.length == 1, we have reached the last nested key
        // not only have reached the last nested key, but we also have the type of the field
        // using the type of the field, we can get the correct converter from the map and convert the value
        return Optional.of(converterForClass.get(field.getType()).convert(value)).orElseThrow(() ->
                new IllegalArgumentException("Filtering not supported for: ." + nested[0]));
    }
}

