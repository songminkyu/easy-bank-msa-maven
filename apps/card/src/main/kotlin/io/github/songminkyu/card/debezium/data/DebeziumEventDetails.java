package io.github.songminkyu.card.debezium.data;

import java.util.Map;

public record DebeziumEventDetails<T extends Object>(
    Map<String, Object> schema,
    Payload<T> payload) {
}