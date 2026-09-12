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

    /**
     * Finds the objects that exist in the first list but do not exist in the second list.
     *
     * <p>This method performs a one-way comparison. It returns only the items from
     * {@code firstList} that are not contained in {@code secondList}. It does not return
     * items that exist in {@code secondList} but not in {@code firstList}.</p>
     *
     * <p>If {@code firstList} is {@code null} or empty, an empty list is returned.
     * If {@code secondList} is {@code null} or empty, a copy of {@code firstList} is
     * returned.</p>
     *
     * <p>Assumptions:</p>
     * <ul>
     *     <li>Objects of type {@code T} have a meaningful {@link Object#equals(Object)}
     *         implementation, as {@link List#contains(Object)} is used for comparison.</li>
     *     <li>No duplicate filtering is performed; duplicate values in {@code firstList}
     *         may also appear in the returned list.</li>
     * </ul>
     *
     * @param <T> the type of objects contained in the lists
     * @param firstList the list containing the objects to check
     * @param secondList the list to compare against
     * @return a list of objects that are present in {@code firstList} but not in
     *         {@code secondList}, or an empty list if no differences are found
     */
    public static <T> List<T> findDifferentObjects(List<T> firstList, List<T> secondList) {
        if (firstList == null || firstList.isEmpty()) {
            return new ArrayList<>();
        }

        if (secondList == null || secondList.isEmpty()) {
            return new ArrayList<>(firstList);
        }

        return firstList.stream()
                .filter(item -> !secondList.contains(item))
                .toList();
    }
}
