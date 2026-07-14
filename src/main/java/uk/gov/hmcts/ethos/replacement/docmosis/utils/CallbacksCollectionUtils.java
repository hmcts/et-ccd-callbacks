package uk.gov.hmcts.ethos.replacement.docmosis.utils;

import org.apache.commons.collections4.CollectionUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

public final class CallbacksCollectionUtils {

    private CallbacksCollectionUtils() {
        // Utility classes should not have a public or default constructor.
    }

    /**
     * Compares two collections by extracting a key from each element and checking
     * whether both collections contain the same set of extracted keys.
     * <p>
     * The comparison is order-independent and ignores duplicate elements within
     * each collection, as the extracted keys are collected into {@link java.util.Set}s.
     * </p>
     *
     * <p>
     * If either collection is {@code null} or empty, or if the {@code keyExtractor}
     * is {@code null}, this method returns {@code false}.
     * </p>
     *
     * @param <T> the type of elements in the collections
     * @param <K> the type of the key extracted from each element
     * @param collection1 the first collection to compare
     * @param collection2 the second collection to compare
     * @param keyExtractor a function used to extract a comparison key from each element
     * @return {@code true} if both collections contain the same set of extracted keys;
     *         {@code false} otherwise
     */
    public static <T, K> boolean sameByKey(Collection<T> collection1,
                                           Collection<T> collection2,
                                           Function<T, K> keyExtractor) {
        if (CollectionUtils.isEmpty(collection1) || CollectionUtils.isEmpty(collection2) || keyExtractor == null) {
            return CollectionUtils.isEmpty(collection1) && CollectionUtils.isEmpty(collection2)
                    || CollectionUtils.isNotEmpty(collection1) && CollectionUtils.isNotEmpty(collection2)
                    && CollectionUtils.isEqualCollection(collection1, collection2);
        }
        return collection1.stream().map(keyExtractor).collect(Collectors.toSet())
                .equals(collection2.stream().map(keyExtractor).collect(Collectors.toSet()));
    }

    /**
     * Merges two lists into a single list while removing duplicate values.
     * <p>
     * The order of elements is preserved based on their first occurrence.
     *
     * @param firstList the first list
     * @param secondList the second list
     * @return a merged list containing unique elements from both lists
     */
    public static List<String> mergeListsWithoutDuplicates(
            List<String> firstList,
            List<String> secondList) {
        Set<String> mergedSet = new LinkedHashSet<>();
        if (CollectionUtils.isNotEmpty(firstList)) {
            mergedSet.addAll(firstList);
        }
        if (CollectionUtils.isNotEmpty(secondList)) {
            mergedSet.addAll(secondList);
        }
        return new ArrayList<>(mergedSet);
    }
}
