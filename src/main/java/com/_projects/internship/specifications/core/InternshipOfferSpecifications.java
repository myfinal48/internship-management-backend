package com._projects.internship.specifications.core;

import org.springframework.data.jpa.domain.Specification;

import com._projects.internship.model.core.InternshipOffer;
import com._projects.internship.model.core.OfferStatus;
import com._projects.internship.model.core.Sector;
import com._projects.internship.model.security.Role;
import com._projects.internship.model.security.User;

import jakarta.persistence.criteria.Join;

public class InternshipOfferSpecifications {

  public static Specification<InternshipOffer> withSectorName(String sectorName) {
    return (root, query, cb) -> {
      if (sectorName == null) return null;
      Join<InternshipOffer, Sector> sectorJoin = root.join("sector");
      return cb.equal(cb.lower(sectorJoin.get("name")), sectorName.toLowerCase());
    };
  }

  public static Specification<InternshipOffer> withLocation(String location) {
    return (root, query,
        cb) -> location != null ? cb.like(cb.lower(root.get("location")), "%" + location.toLowerCase() + "%") : null;
  }

  public static Specification<InternshipOffer> withLength(Integer length) {
    return (root, query, cb) -> length != null ? cb.equal(root.get("length"), length) : null;
  }

  public static Specification<InternshipOffer> withStatus(OfferStatus status) {
    return (root, query, cb) -> (status == null || status == OfferStatus.ALL) ? null
        : cb.equal(root.get("status"), status);
  }

  public static Specification<InternshipOffer> withCompany(Long companyId) {
    return (root, query, cb) -> {
      if (companyId == null)
        return null;
      Join<InternshipOffer, User> companyJoin = root.join("company");
      return cb.and(
          cb.equal(companyJoin.get("id"), companyId),
          cb.equal(companyJoin.get("role"), Role.COMPANY));
    };
  }
}
