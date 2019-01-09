package ru.citeck.ecos.records.request.query;

import ru.citeck.ecos.records.request.RequestResDebug;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

public class RecordsResult<T> extends RequestResDebug {

    private List<T> records = new ArrayList<>();
    private boolean hasMore = false;
    private long totalCount = 0;

    public RecordsResult() {
    }

    public RecordsResult(RecordsResult<T> other) {
        super(other);
        records = new ArrayList<>(other.getRecords());
        hasMore = other.hasMore;
        totalCount = other.totalCount;
    }

    public <K> RecordsResult(RecordsResult<K> other, Function<K, T> mapper) {
        super(other);
        records = other.getRecords().stream().map(mapper).collect(Collectors.toList());
        hasMore = other.hasMore;
        totalCount = other.totalCount;
    }

    public void merge(RecordsResult<T> other) {
        super.merge(other);

        List<T> records = new ArrayList<>();
        records.addAll(this.records);
        records.addAll(other.getRecords());
        this.records = records;

        hasMore = other.getHasMore();
        totalCount += other.getTotalCount();
    }

    public List<T> getRecords() {
        return records;
    }

    public void setRecords(List<T> records) {
        if (records != null) {
            this.records = new ArrayList<>(records);
        } else {
            this.records = new ArrayList<>();
        }
    }

    public boolean getHasMore() {
        return hasMore;
    }

    public void setHasMore(boolean hasMore) {
        this.hasMore = hasMore;
    }

    public long getTotalCount() {
        return totalCount;
    }

    public void setTotalCount(long totalCount) {
        this.totalCount = totalCount;
    }

    @Override
    public String toString() {
        String recordsStr = records.stream()
                                   .map(Object::toString)
                                   .collect(Collectors.joining(",\n    "));
        if (!recordsStr.isEmpty()) {
            recordsStr = "\n    " + recordsStr + "\n  ";
        }
        String debugStr = getDebug() != null ? ",\n  \"debug\": " + getDebug() : "";
        return "{\n" +
                "  \"records\": [" + recordsStr +
                "],\n  \"hasMore\": " + hasMore +
                ",\n  \"totalCount\": " + totalCount +
                debugStr +
                "\n}";
    }
}
