package com.lmkr.hesco.survey.service;

import com.lmkr.hesco.survey.exception.DuplicateGpsNumberException;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * GPS Number generation/validation (SRS §8.3.1 body + Appendix A).
 *
 * NOTE (flagged, not resolved): the SRS body's worked example
 * ("65432111612240014" = UserNo 654321 + Day 11 + Month 06 + Year 24 +
 * Serial 0014) and Appendix A's worked example
 * ("1025202607010000" = UserNo 1025 + Date 010726 + Serial 0000) do not
 * use the same field widths for UserNo — this needs a written answer from
 * HESCO/LMKR before mobile-app generation logic is finalized. This class
 * implements Appendix A's widths (4-digit UserNo + DDMMYY + 4-digit
 * Serial = 14 chars total) since it's the more recent/formal appendix
 * reference; swapping to the body's width is a one-constant change here,
 * not a redesign.
 */
@Service
public class GpsNumberService {

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("ddMMyy");
    private static final int USER_NO_WIDTH = 4;
    private static final int SERIAL_WIDTH = 4;

    private final GpsNumberSequenceRepository sequenceRepository;

    public GpsNumberService(GpsNumberSequenceRepository sequenceRepository) {
        this.sequenceRepository = sequenceRepository;
    }

    /**
     * Generated client-side on the mobile app for offline capability (a DB
     * sequence wouldn't work offline); this server-side version exists for
     * (a) parity/testing and (b) the sync-time uniqueness check below.
     */
    public String generate(String userNo, LocalDate surveyDate, int serial) {
        String paddedUserNo = String.format("%" + USER_NO_WIDTH + "s", userNo).replace(' ', '0');
        String datePart = surveyDate.format(DATE_FMT);
        String paddedSerial = String.format("%0" + SERIAL_WIDTH + "d", serial);
        return paddedUserNo + datePart + paddedSerial;
    }

    /**
     * Server-side uniqueness check performed at sync time (mobile-generated
     * GPS numbers can collide across devices/offline windows). Throws if
     * the number already exists so the sync layer can prompt the mobile
     * app to regenerate with the next serial rather than silently
     * overwriting an existing survey_form row.
     */
    public void assertUniqueOnSync(String gpsNumber) {
        if (sequenceRepository.existsByGpsNumber(gpsNumber)) {
            throw new DuplicateGpsNumberException(
                "GPS Number " + gpsNumber + " already exists — collision on sync, regenerate with next serial");
        }
    }
}
