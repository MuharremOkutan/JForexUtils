package com.jforex.programming.order;

import static com.jforex.programming.misc.JForexUtil.pfs;
import static com.jforex.programming.misc.JForexUtil.uss;

import com.dukascopy.api.IEngine.OrderCommand;
import com.dukascopy.api.Instrument;

public final class OrderParams {

    private final String label;
    private final Instrument instrument;
    private final OrderCommand orderCommand;
    private final double amount;

    private final double price;
    private final double slippage;
    private final double stopLossPrice;
    private final double takeProfitPrice;
    private final long goodTillTime;
    private final String comment;

    private OrderParams(final Builder builder) {
        label = builder.label;
        instrument = builder.instrument;
        orderCommand = builder.orderCommand;
        amount = builder.amount;
        price = builder.price;
        slippage = builder.slippage;
        stopLossPrice = builder.stopLossPrice;
        takeProfitPrice = builder.takeProfitPrice;
        goodTillTime = builder.goodTillTime;
        comment = builder.comment;
    }

    public final String label() {
        return label;
    }

    public final Instrument instrument() {
        return instrument;
    }

    public final OrderCommand orderCommand() {
        return orderCommand;
    }

    public final double amount() {
        return amount;
    }

    public final double price() {
        return price;
    }

    public final double slippage() {
        return slippage;
    }

    public final double stopLossPrice() {
        return stopLossPrice;
    }

    public final double takeProfitPrice() {
        return takeProfitPrice;
    }

    public final long goodTillTime() {
        return goodTillTime;
    }

    public final String comment() {
        return comment;
    }

    @Override
    public Clone clone() {
        return new Builder(this);
    }

    public static WithOrderCommand forInstrument(final Instrument instrument) {
        return new Builder(instrument);
    }

    public interface WithOrderCommand {
        public WithAmount withOrderCommand(OrderCommand orderCommand);
    }

    public interface WithAmount {
        public WithLabel withAmount(double amount);
    }

    public interface WithLabel {
        public WithOptions withLabel(String label);
    }

    public interface WithOptions {
        public WithOptions price(double price);

        public WithOptions slippage(double slippage);

        public WithOptions stopLossPrice(double stopLossPrice);

        public WithOptions takeProfitPrice(double takeProfitPrice);

        public WithOptions goodTillTime(long goodTillTime);

        public WithOptions comment(String comment);

        public OrderParams build();
    }

    public interface Clone {
        public Clone withLabel(final String label);

        public Clone forInstrument(Instrument instrument);

        public Clone withOrderCommand(OrderCommand orderCommand);

        public Clone withAmount(double amount);

        public Clone price(double price);

        public Clone slippage(double slippage);

        public Clone stopLossPrice(double stopLossPrice);

        public Clone takeProfitPrice(double takeProfitPrice);

        public Clone goodTillTime(long goodTillTime);

        public Clone comment(String comment);

        public OrderParams build();
    }

    private static class Builder implements WithOrderCommand,
                                 WithAmount,
                                 WithLabel,
                                 WithOptions,
                                 Clone {

        private String label;
        private Instrument instrument;
        private OrderCommand orderCommand;
        private double amount;

        private double price;
        private double slippage;
        private double stopLossPrice;
        private double takeProfitPrice;
        private long goodTillTime;
        private String comment;

        private Builder(final Instrument instrument) {
            this.instrument = instrument;
            price = uss.ORDER_DEFAULT_PRICE();
            slippage = uss.ORDER_DEFAULT_SLIPPAGE();
            stopLossPrice = pfs.NO_STOP_LOSS_PRICE();
            takeProfitPrice = pfs.NO_TAKE_PROFIT_PRICE();
            goodTillTime = uss.ORDER_DEFAULT_GOOD_TILL_TIME();
            comment = uss.ORDER_DEFAULT_COMMENT();
        }

        private Builder(final OrderParams orderSpecification) {
            label = orderSpecification.label;
            instrument = orderSpecification.instrument;
            orderCommand = orderSpecification.orderCommand;
            amount = orderSpecification.amount;
            price = orderSpecification.price;
            slippage = orderSpecification.slippage;
            stopLossPrice = orderSpecification.stopLossPrice;
            takeProfitPrice = orderSpecification.takeProfitPrice;
            goodTillTime = orderSpecification.goodTillTime;
            comment = orderSpecification.comment;
        }

        @Override
        public Builder withLabel(final String label) {
            this.label = label;
            return this;
        }

        @Override
        public Builder forInstrument(final Instrument instrument) {
            this.instrument = instrument;
            return this;
        }

        @Override
        public Builder withOrderCommand(final OrderCommand orderCommand) {
            this.orderCommand = orderCommand;
            return this;
        }

        @Override
        public Builder withAmount(final double amount) {
            this.amount = amount;
            return this;
        }

        @Override
        public Builder price(final double price) {
            this.price = price;
            return this;
        }

        @Override
        public Builder slippage(final double slippage) {
            this.slippage = slippage;
            return this;
        }

        @Override
        public Builder stopLossPrice(final double stopLossPrice) {
            this.stopLossPrice = stopLossPrice;
            return this;
        }

        @Override
        public Builder takeProfitPrice(final double takeProfitPrice) {
            this.takeProfitPrice = takeProfitPrice;
            return this;
        }

        @Override
        public Builder goodTillTime(final long goodTillTime) {
            this.goodTillTime = goodTillTime;
            return this;
        }

        @Override
        public Builder comment(final String comment) {
            this.comment = comment;
            return this;
        }

        @Override
        public OrderParams build() {
            return new OrderParams(this);
        }
    }
}