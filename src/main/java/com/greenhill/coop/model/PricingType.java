package com.greenhill.coop.model;

/**
 * How a catalogue product is priced.
 * UNIT   - sold as a whole item (a box of eggs, a jar of olive oil).
 * WEIGHT - sold by weight, fractional quantities allowed (1.5 kg of oats).
 */
public enum PricingType {
    UNIT, WEIGHT
}
