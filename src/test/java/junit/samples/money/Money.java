package junit.samples.money;

import org.jetbrains.annotations.NotNull;

/**
 * A simple Money.
 */
public class Money implements IMoney {

    private int fAmount;
    private String fCurrency;

    /**
     * Constructs a money from the given amount and currency.
     */
    public Money(int amount, String currency) {
        fAmount = amount;
        fCurrency = currency;
    }

    /**
     * Adds a money to this money. Forwards the request to the addMoney helper.
     */
    public IMoney add(@NotNull IMoney m) {
        return m.addMoney(this);
    }

    public IMoney addMoney(@NotNull Money m) {
        if (m.currency().equals(currency())) {
            return new Money(amount() + m.amount(), currency());
        }
        return MoneyBag.create(this, m);
    }

    public IMoney addMoneyBag(@NotNull MoneyBag s) {
        return s.addMoney(this);
    }

    public int amount() {
        return fAmount;
    }

    public String currency() {
        return fCurrency;
    }

    @Override
    public boolean equals(Object anObject) {
        if (isZero()) {
            if (anObject instanceof IMoney) {
                return ((IMoney) anObject).isZero();
            }
        }
        if (anObject instanceof Money) {
            Money aMoney = (Money) anObject;
            return aMoney.currency().equals(currency())
                    && amount() == aMoney.amount();
        }
        return false;
    }

    @Override
    public int hashCode() {
        if (fAmount == 0) {
            return 0;
        }
        return fCurrency.hashCode() + fAmount;
    }

    public boolean isZero() {
        return amount() == 0;
    }

    @NotNull
    public IMoney multiply(int factor) {
        return new Money(amount() * factor, currency());
    }

    @NotNull
    public IMoney negate() {
        return new Money(-amount(), currency());
    }

    public IMoney subtract(@NotNull IMoney m) {
        return add(m.negate());
    }

    @NotNull
    @Override
    public String toString() {
        return "[" + amount() + " " + currency() + "]";
    }

    public /*this makes no sense*/ void appendTo(@NotNull MoneyBag m) {
        m.appendMoney(this);
    }
}