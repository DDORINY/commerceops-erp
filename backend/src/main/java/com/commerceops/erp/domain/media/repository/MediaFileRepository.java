package com.commerceops.erp.domain.media.repository;

import com.commerceops.erp.domain.media.entity.MediaFile;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MediaFileRepository extends JpaRepository<MediaFile, Long> {
}
