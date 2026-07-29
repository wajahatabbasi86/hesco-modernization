package com.lmkr.hesco.auth.exception;

/**
 * Thrown when role.requiresImei == true — assumption: those roles are
 * mobile-surveyor roles per the SRS's Mobile Survey App (IMEI-bound) vs.
 * Web GIS App split, so they don't authenticate through this web login
 * endpoint. Flag if that assumption is wrong.
 */
public class MobileLoginNotAllowedException extends RuntimeException {
    public MobileLoginNotAllowedException(String message) {
        super(message);
    }
}
