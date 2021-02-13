package com.github.mhewedy.expressions;

/**
 * List of operators supported and used in the {@link Expressions}
 */
public enum Operator {

    $eq(false),          // col = val   (if val is null then => col is null)
    $ne(false),          // col <> val  (if val is null then => col is not null)
    $ieq(false),         // lower(col) = lower(val)

    $gt(false),          // col > val
    $gte(false),         // col >= val
    $lt(false),          // col < val
    $lte(false),         // col <= val

    $start(false),       // col like 'val%'
    $end(false),         // col like '%val'
    $contains(false),    // col like '%val%'
    $istart(false),      // lower(col) like 'lower(val)%'
    $iend(false),        // lower(col) like '%lower(val)'
    $icontains(false),   // lower(col) like '%lower(val)%'

    $in(true),           // col in (val1, val2, ...)
    $nin(true),          // col not in (val1, val2, ...)

    $or(false),          // expr1 or expr2
    $and(false);         // expr1 and expr2

    public final boolean isList;

    Operator(boolean isList) {
        this.isList = isList;
    }
}
