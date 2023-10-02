/*
 * Copyright Elasticsearch B.V. and/or licensed to Elasticsearch B.V. under one
 * or more contributor license agreements. Licensed under the Elastic License
 * 2.0; you may not use this file except in compliance with the Elastic License
 * 2.0.
 */

package org.elasticsearch.compute.aggregation;

import org.elasticsearch.common.util.BigArrays;
import org.elasticsearch.compute.data.Block;
import org.elasticsearch.compute.data.BlockFactory;
import org.elasticsearch.compute.data.DoubleBlock;
import org.elasticsearch.compute.data.Page;
import org.elasticsearch.compute.operator.LongDoubleTupleBlockSourceOperator;
import org.elasticsearch.compute.operator.SourceOperator;
import org.elasticsearch.core.Tuple;

import java.util.List;
import java.util.OptionalDouble;
import java.util.stream.LongStream;

import static org.hamcrest.Matchers.equalTo;

public class MinDoubleGroupingAggregatorFunctionTests extends GroupingAggregatorFunctionTestCase {
    @Override
    protected SourceOperator simpleInput(BlockFactory blockFactory, int end) {
        return new LongDoubleTupleBlockSourceOperator(
            LongStream.range(0, end).mapToObj(l -> Tuple.tuple(randomLongBetween(0, 4), randomDouble()))
        );
    }

    @Override
    protected AggregatorFunctionSupplier aggregatorFunction(BigArrays bigArrays, List<Integer> inputChannels) {
        return new MinDoubleAggregatorFunctionSupplier(bigArrays, inputChannels);
    }

    @Override
    protected String expectedDescriptionOfAggregator() {
        return "min of doubles";
    }

    @Override
    protected void assertSimpleGroup(List<Page> input, Block result, int position, Long group) {
        OptionalDouble min = input.stream().flatMapToDouble(p -> allDoubles(p, group)).min();
        if (min.isEmpty()) {
            assertThat(result.isNull(position), equalTo(true));
            return;
        }
        assertThat(result.isNull(position), equalTo(false));
        assertThat(((DoubleBlock) result).getDouble(position), equalTo(min.getAsDouble()));
    }
}
