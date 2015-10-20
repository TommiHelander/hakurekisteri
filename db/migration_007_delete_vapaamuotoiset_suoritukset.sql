INSERT INTO suoritus (resource_id, komo, myontaja, tila, valmistuminen, henkilo_oid, yksilollistaminen, suoritus_kieli, inserted, deleted, source, kuvaus, vuosi, tyyppi, "index", vahvistettu)
  SELECT resource_id, komo, myontaja, tila, valmistuminen, henkilo_oid, yksilollistaminen, suoritus_kieli, floor(extract(epoch from now()) * 1000), true, source, kuvaus, vuosi, tyyppi, "index", vahvistettu
  FROM v_suoritus WHERE tyyppi = 'kkTutkinto';