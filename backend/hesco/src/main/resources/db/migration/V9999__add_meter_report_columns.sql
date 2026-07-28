-- NOTE: rename V9999 to the next real Flyway version number in this branch
-- before applying. Adds the two fields the Meter Report (SRS §3.15.2.4)
-- needs that aren't captured by the survey form today: Sanctioned Load
-- and Meter Make. meter_number/consumer_reference already exist.
ALTER TABLE meter_detail
    ADD COLUMN sanctioned_load numeric(10, 2),
    ADD COLUMN meter_make varchar(100);
