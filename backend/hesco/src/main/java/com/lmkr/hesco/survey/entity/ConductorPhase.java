package com.lmkr.hesco.survey.entity;

/**
 * Conductor phase (SRS §8.3.4). R/Y/B are live phases, available in
 * both HT and LT systems. N (Neutral) is LT-only — never displayed or
 * accepted for an HT survey form.
 */
public enum ConductorPhase {
    R, Y, B, N
}
