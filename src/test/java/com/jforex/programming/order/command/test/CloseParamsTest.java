package com.jforex.programming.order.command.test;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.jforex.programming.init.JForexUtil;
import com.jforex.programming.order.command.CloseParams;
import com.jforex.programming.test.common.QuoteProviderForTest;

public class CloseParamsTest extends QuoteProviderForTest {

    private CloseParams closeParams;

    private final double amount = 0.12;
    private static final double defaultCloseSlippage = JForexUtil.platformSettings.defaultCloseSlippage();

    @Test
    public void defaultParamsAreCorrect() {
        closeParams = CloseParams
            .newBuilder(buyOrderEURUSD)
            .build();

        assertThat(closeParams.order(), equalTo(buyOrderEURUSD));
        assertThat(closeParams.amount(), equalTo(0.0));
        assertFalse(closeParams.maybePrice().isPresent());
        assertTrue(Double.isNaN(closeParams.slippage()));
    }

    @Test
    public void paramsOnlyWithAmountAreCorrect() {
        closeParams = CloseParams
            .newBuilder(buyOrderEURUSD)
            .withAmount(amount)
            .build();

        assertThat(closeParams.order(), equalTo(buyOrderEURUSD));
        assertThat(closeParams.amount(), equalTo(amount));
        assertFalse(closeParams.maybePrice().isPresent());
        assertTrue(Double.isNaN(closeParams.slippage()));
    }

    @Test
    public void paramsWithPriceAreCorrect() {
        closeParams = CloseParams
            .newBuilder(buyOrderEURUSD)
            .withAmount(amount)
            .withPrice(askEURUSD)
            .build();

        assertThat(closeParams.order(), equalTo(buyOrderEURUSD));
        assertThat(closeParams.amount(), equalTo(amount));
        assertTrue(closeParams.maybePrice().isPresent());
        assertTrue(Double.isNaN(closeParams.slippage()));
    }

    @Test
    public void paramsWithNegativeSlippageReturnsPlatformSlippage() {
        closeParams = CloseParams
            .newBuilder(buyOrderEURUSD)
            .withAmount(amount)
            .withPrice(askEURUSD)
            .withSlippage(-1)
            .build();

        assertThat(closeParams.order(), equalTo(buyOrderEURUSD));
        assertThat(closeParams.amount(), equalTo(amount));
        assertTrue(closeParams.maybePrice().isPresent());
        assertThat(closeParams.slippage(), equalTo(defaultCloseSlippage));
    }
}
