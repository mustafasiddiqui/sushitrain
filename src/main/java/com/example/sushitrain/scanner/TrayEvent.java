package com.example.sushitrain.scanner;

import java.util.Date;

public record TrayEvent(Date date, int scannedIn, int scannedOut) {
}