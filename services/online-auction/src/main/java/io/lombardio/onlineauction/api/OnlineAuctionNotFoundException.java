package io.lombardio.onlineauction.api;

public class OnlineAuctionNotFoundException extends RuntimeException {
    public OnlineAuctionNotFoundException(String message) {
        super(message);
    }
}
