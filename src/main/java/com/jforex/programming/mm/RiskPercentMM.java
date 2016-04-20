package com.jforex.programming.mm;

import com.jforex.programming.misc.CalculationUtil;
import com.jforex.programming.misc.MathUtil;

import com.dukascopy.api.IAccount;
import com.dukascopy.api.Instrument;
import com.dukascopy.api.OfferSide;

public final class RiskPercentMM {

    private final IAccount account;
    private final CalculationUtil calculationUtil;

    public RiskPercentMM(final IAccount account,
                         final CalculationUtil calculationUtil) {
        this.account = account;
        this.calculationUtil = calculationUtil;
    }

    public final double percentOfEquity(final double percent) {
        return MathUtil.roundAmount(account.getEquity() * percent / 100);
    }

    public final double amountForRisk(final Instrument instrument,
                                      final OfferSide offerSide,
                                      final double riskPercent,
                                      final double pipsToSL) {
        return amountForBetSize(instrument, offerSide, percentOfEquity(riskPercent), pipsToSL);
    }

    public final double amountForFixMargin(final Instrument instrument,
                                           final OfferSide offerSide,
                                           final double fixMargin,
                                           final double pipsToSL) {
        return amountForBetSize(instrument, offerSide, fixMargin, pipsToSL);
    }

    private final double amountForBetSize(final Instrument instrument,
                                          final OfferSide offerSide,
                                          final double betSize,
                                          final double pipsToSL) {
        final double pipValueAmountInAccountCurrency = betSize / pipsToSL;
        final double pipValueAmountInQuoteCurrency =
                calculationUtil.convertAmount(pipValueAmountInAccountCurrency,
                                              account.getAccountCurrency(),
                                              instrument.getSecondaryJFCurrency(),
                                              offerSide);
        final double scaledAmount = pipValueAmountInQuoteCurrency / instrument.getPipValue();
        return CalculationUtil.scaleToPlatformAmount(scaledAmount);
    }
}