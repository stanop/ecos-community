package ru.citeck.ecos.search.ftsquery;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.search.QueryConsistency;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.SearchParameters;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.Serializable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Full text search query builder
 * To create new query use FTSQuery.create()
 * To search by constructed query use method query(SearchService searchService)
 *
 * @author Pavel Simonov
 */
public class FTSQuery implements OperatorExpected, OperandExpected {

    private static final Log logger = LogFactory.getLog(FTSQuery.class);

    private static final String ISUNSET = "ISUNSET";
    private static final String ISNULL = "ISNULL";
    private static final String ISNOTNULL = "ISNOTNULL";
    private static final String NOT = "NOT";
    private static final String PARENT = "PARENT";
    private static final String TYPE = "TYPE";
    private static final String PATH = "PATH";

    private SearchParameters searchParameters;
    private Group group = new Group();

    private FTSQuery() {
        searchParameters = new SearchParameters();
        searchParameters.setLanguage(SearchService.LANGUAGE_FTS_ALFRESCO);
        searchParameters.addStore(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE);
    }

    private FTSQuery(FTSQuery other) {
        searchParameters = other.searchParameters.copy();
        group = other.group.copy();
    }

    /**
     * Create new query
     */
    public static OperandExpected create() {
        return new FTSQuery();
    }

    /**
     * Create unsafe FTSQuery which return self from build methods
     */
    public static FTSQuery createRaw() {
        return new FTSQuery();
    }

    public FTSQuery values(Map<QName, Serializable> values) {
        return values(values, BinOperator.AND, false);
    }

    public FTSQuery values(Map<QName, Serializable> values, BinOperator joinOperator) {
        return values(values, joinOperator, false);
    }

    public FTSQuery values(Map<QName, Serializable> values, BinOperator joinOperator, boolean exact) {
        int count = values.size();
        if (count == 0) {
            throw new IllegalArgumentException("Values is empty");
        }
        open();
        for (Map.Entry<QName, Serializable> entry : values.entrySet()) {
            value(entry.getKey(), entry.getValue(), exact);
            if (--count > 0) {
                group.setBiOperator(new BinOperatorTerm(joinOperator));
            }
        }
        close();
        return this;
    }

    public FTSQuery any(QName field, Iterable<Serializable> values) {
        Iterator<Serializable> it = values.iterator();
        if (!it.hasNext()) {
            throw new IllegalArgumentException("Values is empty");
        }
        open();
        while (it.hasNext()) {
            value(field, it.next());
            if (it.hasNext()) or();
        }
        close();
        return this;
    }

    public FTSQuery empty(QName field) {
        return open().isNull(field).or().isUnset(field).close();
    }

    public FTSQuery exact(QName field, Serializable value) {
        return value(field, value, true);
    }

    public FTSQuery value(QName field, Serializable value) {
        return value(field, value, false);
    }

    public FTSQuery value(QName field, Serializable value, boolean exact) {
        if (value == null) {
            empty(field);
        } else {
            ValueOperator valueOperator = new ValueOperator();
            valueOperator.field = field;
            valueOperator.value = value;
            valueOperator.exact = exact;
            group.addTerm(valueOperator);
        }
        return this;
    }

    public FTSQuery path(String path) {
        group.addTerm(new SysValueOperator(PATH, path));
        return this;
    }

    public FTSQuery isSet(QName field) {
        return isNotNull(field);
    }

    public FTSQuery isUnset(QName field) {
        group.addTerm(new SysValueOperator(ISUNSET, field));
        return this;
    }

    public FTSQuery isNull(QName field) {
        group.addTerm(new SysValueOperator(ISNULL, field));
        return this;
    }

    public FTSQuery isNotNull(QName field) {
        group.addTerm(new SysValueOperator(ISNOTNULL, field));
        return this;
    }

    public FTSQuery parent(NodeRef parent) {
        group.addTerm(new SysValueOperator(PARENT, parent));
        return this;
    }

    public FTSQuery type(QName typeName) {
        group.addTerm(new SysValueOperator(TYPE, typeName));
        return this;
    }

    public FTSQuery or() {
        group.setBiOperator(new BinOperatorTerm(BinOperator.OR));
        return this;
    }

    public FTSQuery and() {
        group.setBiOperator(new BinOperatorTerm(BinOperator.AND));
        return this;
    }

    public FTSQuery not() {
        group.setUnOperator(new UnOperatorTerm(NOT));
        return this;
    }

    public FTSQuery open() {
        group.startGroup();
        return this;
    }

    public FTSQuery close() {
        group.stopGroup();
        return this;
    }

    public FTSQuery transactionalIfPossible() {
        return consistency(QueryConsistency.TRANSACTIONAL_IF_POSSIBLE);
    }

    public FTSQuery transactional() {
        return consistency(QueryConsistency.TRANSACTIONAL);
    }

    public FTSQuery eventual() {
        return consistency(QueryConsistency.EVENTUAL);
    }

    public FTSQuery consistency(QueryConsistency consistency) {
        searchParameters.setQueryConsistency(consistency);
        return this;
    }

    public String getQuery() {
        return group.getQuery();
    }

    public Optional<NodeRef> queryOne(SearchService searchService) {
        return query(searchService).stream()
                                   .findFirst();
    }

    public Optional<NodeRef> queryOne(SearchService searchService, Predicate<NodeRef> filter) {
        return query(searchService).stream()
                                   .filter(filter)
                                   .findFirst();
    }

    public List<NodeRef> query(SearchService searchService, Predicate<NodeRef> filter) {
        return query(searchService).stream()
                                   .filter(filter)
                                   .collect(Collectors.toList());
    }

    public List<NodeRef> query(SearchService searchService) {

        String query = group.getQuery();

        if (logger.isDebugEnabled()) {
            logger.debug("FTSQuery: " + query);
        }

        searchParameters.setQuery(query);

        ResultSet resultSet = null;

        try {
            resultSet = searchService.query(searchParameters);
            return resultSet.getNodeRefs();
        } finally {
            if (resultSet != null) {
                resultSet.close();
            }
        }
    }

    public FTSQuery copy() {
        return new FTSQuery(this);
    }

    @Override
    public boolean equals(Object o) {

        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        FTSQuery query = (FTSQuery) o;

        return searchParameters.equals(query.searchParameters) && group.equals(query.group);
    }

    @Override
    public int hashCode() {
        int result = searchParameters.hashCode();
        result = 31 * result + group.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return group.getQuery();
    }

    private interface Term<T> {
        void toString(StringBuilder builder);
        T copy();
    }

    private interface Operand<T> extends Term<T> {
    }

    private class UnOperatorTerm implements Term<UnOperatorTerm> {

        String operator;
        Term term;

        UnOperatorTerm(String operator) {
            this.operator = operator;
        }

        public void toString(StringBuilder builder) {
            builder.append(operator).append(' ');
            term.toString(builder);
        }

        @Override
        public UnOperatorTerm copy() {
            UnOperatorTerm result = new UnOperatorTerm(operator);
            result.term = term;
            return result;
        }

        @Override
        public boolean equals(Object o) {

            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            UnOperatorTerm that = (UnOperatorTerm) o;

            return operator.equals(that.operator) && term.equals(that.term);
        }

        @Override
        public int hashCode() {
            int result = operator.hashCode();
            result = 31 * result + term.hashCode();
            return result;
        }
    }

    private class BinOperatorTerm implements Term<BinOperatorTerm> {

        Term term0;
        Term term1;
        BinOperator operator;

        BinOperatorTerm(BinOperator operator) {
            this.operator = operator;
        }

        @Override
        public BinOperatorTerm copy() {
            BinOperatorTerm result = new BinOperatorTerm(operator);
            result.term0 = term0;
            result.term1 = term1;
            return result;
        }

        @Override
        public void toString(StringBuilder builder) {
            term0.toString(builder);
            builder.append(' ').append(operator).append(' ');
            term1.toString(builder);
        }

        @Override
        public boolean equals(Object o) {

            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            BinOperatorTerm operator1 = (BinOperatorTerm) o;

            return term0.equals(operator1.term0) &&
                   term1.equals(operator1.term1) &&
                   operator.equals(operator1.operator);
        }

        @Override
        public int hashCode() {
            int result = term0.hashCode();
            result = 31 * result + term1.hashCode();
            result = 31 * result + operator.hashCode();
            return result;
        }
    }

    private class SysValueOperator implements Operand<SysValueOperator> {

        String field;
        Serializable value;

        SysValueOperator(String field, Serializable value) {
            this.field = field;
            this.value = value;
        }

        @Override
        public SysValueOperator copy() {
            return new SysValueOperator(field, value);
        }

        @Override
        public void toString(StringBuilder builder) {
            builder.append(field).append(":\"").append(value).append('\"');
        }

        @Override
        public boolean equals(Object o) {

            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            SysValueOperator that = (SysValueOperator) o;

            return (field != null ? field.equals(that.field) : that.field == null) &&
                   (value != null ? value.equals(that.value) : that.value == null);
        }

        @Override
        public int hashCode() {
            int result = field != null ? field.hashCode() : 0;
            result = 31 * result + (value != null ? value.hashCode() : 0);
            return result;
        }
    }

    private class ValueOperator implements Operand<ValueOperator> {

        QName field;
        Serializable value;
        boolean exact = false;

        @Override
        public ValueOperator copy() {
            ValueOperator result = new ValueOperator();
            result.field = field;
            result.value = value;
            result.exact = exact;
            return result;
        }

        @Override
        public void toString(StringBuilder builder) {
            QueryConsistency consistency = searchParameters.getQueryConsistency();
            char prefix = exact || consistency.equals(QueryConsistency.TRANSACTIONAL) ? '=' : '@';
            if (value instanceof Boolean) {
                builder.append(prefix).append(field).append(":").append(value);
            } else {
                builder.append(prefix).append(field).append(":\"").append(value).append('\"');
            }
        }

        @Override
        public boolean equals(Object o) {

            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            ValueOperator operator = (ValueOperator) o;

            return exact == operator.exact &&
                   (field != null ? field.equals(operator.field) : operator.field == null) &&
                   (value != null ? value.equals(operator.value) : operator.value == null);
        }

        @Override
        public int hashCode() {
            int result = field != null ? field.hashCode() : 0;
            result = 31 * result + (value != null ? value.hashCode() : 0);
            result = 31 * result + (exact ? 1 : 0);
            return result;
        }
    }

    private class Group implements Operand<Group> {

        UnOperatorTerm unOperator = null;
        BinOperatorTerm biOperator = null;
        Group group = null;
        Term term = null;

        private String query = null;
        private int hash = 0;

        void startGroup() {
            if (group != null) {
                group.startGroup();
            } else {
                Group group = new Group();
                addTerm(group);
                this.group = group;
            }
            query = null;
        }

        void stopGroup() {
            if (group == null) {
                throw new IllegalStateException("Bracket not open");
            }
            Group finalGroup = this;
            while (finalGroup.group.group != null) {
                finalGroup = finalGroup.group;
            }
            finalGroup.group = null;
            query = null;
        }

        void setUnOperator(UnOperatorTerm operator) {
            if (group != null) {
                group.setUnOperator(operator);
            } else {
                unOperator = operator;
            }
            query = null;
        }

        void setBiOperator(BinOperatorTerm operator) {
            if (group != null) {
                group.setBiOperator(operator);
            } else {
                if (term == null) {
                    throw new IllegalStateException("Binary operator can't be used without left operand");
                }
                biOperator = operator;
                biOperator.term0 = term;
                term = null;
            }
            query = null;
        }

        void addTerm(Term term) {
            if (group != null) {
                group.addTerm(term);
            } else {
                Term result = term;
                if (unOperator != null) {
                    unOperator.term = result;
                    result = unOperator;
                    unOperator = null;
                }
                if (biOperator != null) {
                    biOperator.term1 = result;
                    result = biOperator;
                    biOperator = null;
                } else if (this.term != null) {
                    throw new IllegalStateException("Search query building error. You should specify binary operator to combine two terms");
                }
                this.term = result;
            }
            query = null;
        }

        public String getQuery() {

            if (query != null) {
                return query;
            }

            StringBuilder sb = new StringBuilder();
            term.toString(sb);
            query = sb.toString();

            return query;
        }

        @Override
        public Group copy() {
            Group result = new Group();
            result.unOperator = unOperator;
            result.biOperator = biOperator;
            result.group = group;
            result.term = term;
            return result;
        }

        @Override
        public void toString(StringBuilder builder) {
            if (term instanceof Operand) {
                term.toString(builder);
            } else {
                builder.append('(');
                term.toString(builder);
                builder.append(')');
            }
        }

        @Override
        public boolean equals(Object o) {

            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            Group group = (Group) o;

            return term.equals(group.term);
        }

        @Override
        public int hashCode() {
            if (hash == 0) {
                hash = term.hashCode();
            }
            return hash;
        }
    }
}
