package com.company.base2.endpoint.event.consumer.model;

import com.company.base2.PojaGenerated;
import com.company.base2.endpoint.event.model.PojaEvent;


@PojaGenerated
public record TypedEvent(String typeName, PojaEvent payload) {}
