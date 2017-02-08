package com.jforex.programming.order.task.params.basic;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.Collection;

import com.jforex.programming.order.task.params.BasicTaskParamsBase;
import com.jforex.programming.order.task.params.TaskParamsType;

public class BatchParams extends BasicTaskParamsBase {

    private final Collection<BasicTaskParamsBase> paramsCollection;

    private BatchParams(final Builder builder) {
        super(builder);

        this.paramsCollection = builder.paramsCollection;
    }

    public Collection<BasicTaskParamsBase> paramsCollection() {
        return paramsCollection;
    }

    public static Builder setBatchWith(Collection<BasicTaskParamsBase> paramsCollection) {
        checkNotNull(paramsCollection);

        return new Builder(paramsCollection);
    }

    public static class Builder extends BasicParamsBuilder<Builder> {

        private final Collection<BasicTaskParamsBase> paramsCollection;

        public Builder(Collection<BasicTaskParamsBase> paramsCollection) {
            this.paramsCollection = paramsCollection;
        }

        public BatchParams build() {
            return new BatchParams(this);
        }
    }

    @Override
    public TaskParamsType type() {
        return TaskParamsType.BATCH;
    }
}