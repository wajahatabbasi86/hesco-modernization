package com.lmkr.hesco.adminbound.service;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;

/**
 * Plain COUNT queries backing AdminBoundService.assertDeletable() (SRS
 * §3.1.5). These replace the earlier v_sub_division_dependency_counts /
 * v_division_dependency_counts / v_circle_dependency_counts DB views —
 * per the architecture decision, this kind of read is issued directly by
 * the service layer rather than via a DB-side view, so there's exactly
 * one place (Java) that decides what "has dependents" means.
 *
 * Not a full Spring Data repository interface (no entity/id type) since
 * every method here is a scalar count against a different root table;
 * kept as a thin @Query-only interface for that reason.
 */
public interface AdminBoundDependencyRepository extends Repository<Object, Long> {

    @Query("select count(u) from AppUser u where u.subDivision.id = :subDivisionId")
    long countUsersInSubDivision(Long subDivisionId);

    @Query("select count(f) from Feeder f where f.subDivision.id = :subDivisionId")
    long countFeedersInSubDivision(Long subDivisionId);

    @Query("select count(w) from WorkOrder w where w.feeder.subDivision.id = :subDivisionId")
    long countWorkOrdersInSubDivision(Long subDivisionId);

    @Query("select count(u) from AppUser u where u.division.id = :divisionId")
    long countUsersInDivision(Long divisionId);

    @Query("select count(sd) from SubDivision sd where sd.division.id = :divisionId")
    long countSubDivisionsInDivision(Long divisionId);

    @Query("select count(u) from AppUser u where u.circle.id = :circleId")
    long countUsersInCircle(Long circleId);

    @Query("select count(d) from Division d where d.circle.id = :circleId")
    long countDivisionsInCircle(Long circleId);
}
