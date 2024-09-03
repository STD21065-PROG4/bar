package com.example.app.endpoint.event.consumer.model;

import com.example.app.PojaGenerated;
import com.example.app.endpoint.event.model.PojaEvent;


@PojaGenerated
public record TypedEvent(String typeName, PojaEvent payload) {}
