package com.placement.student.controller;

import com.placement.student.model.Offer;
import com.placement.student.service.OfferService;

import java.util.List;

public class OfferController {

    private final OfferService offerService;

    public OfferController(OfferService offerService) {
        this.offerService = offerService;
    }

    public List<Offer> viewOffers(long studentId) {
        return offerService.getOffers(studentId);
    }

    public boolean accept(long studentId, long offerId) {
        return offerService.acceptOffer(studentId, offerId);
    }

    public boolean reject(long studentId, long offerId) {
        return offerService.rejectOffer(studentId, offerId);
    }
}
